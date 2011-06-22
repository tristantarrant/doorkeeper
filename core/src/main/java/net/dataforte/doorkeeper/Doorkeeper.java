package net.dataforte.doorkeeper;

import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;
import javax.servlet.ServletContext;

import net.dataforte.commons.resources.ClassLoaderResourceResolver;
import net.dataforte.commons.resources.IResourceResolver;
import net.dataforte.commons.resources.ServiceFinder;
import net.dataforte.commons.resources.WebAppResourceResolver;
import net.dataforte.commons.slf4j.LoggerFactory;
import net.dataforte.doorkeeper.account.AccountManager;
import net.dataforte.doorkeeper.account.provider.AccountProvider;
import net.dataforte.doorkeeper.annotations.Property;
import net.dataforte.doorkeeper.authenticator.Authenticator;
import net.dataforte.doorkeeper.authorizer.Authorizer;
import net.dataforte.doorkeeper.utils.JSONUtils;

import org.apache.commons.beanutils.PropertyUtils;
import org.json.JSONException;
import org.slf4j.Logger;

public class Doorkeeper {
	public static final String DEFAULT_CHAIN = "default";
	private static final String AUTHENTICATOR = "authenticator";
	private static final String AUTHORIZER = "authorizer";
	private static final String ACCOUNTPROVIDER = "accountprovider";

	static final Logger log = LoggerFactory.make();

	private static final String DOORKEEPER_PROPERTIES = "doorkeeper.properties";
	private Map<String, Class<? extends Authenticator>> authenticators = new HashMap<String, Class<? extends Authenticator>>();
	private Map<String, Class<? extends Authorizer>> authorizers = new HashMap<String, Class<? extends Authorizer>>();
	private Map<String, Class<? extends AccountProvider>> accountProviders = new HashMap<String, Class<? extends AccountProvider>>();

	private Map<String, List<Authenticator>> authenticatorChain;
	private Map<String, List<Authorizer>> authorizerChain;
	private Map<String, List<AccountProvider>> accountProviderChain;
	private Map<String, AccountManager> accountManager;

	private Properties properties;
	
	IResourceResolver resourceResolver;

	/**
	 * Default constructor which looks for a doorkeeper.properties file in the
	 * classpath
	 */
	public Doorkeeper() {
		resourceResolver = new ClassLoaderResourceResolver();
		init();
		load(resourceResolver.getResource(DOORKEEPER_PROPERTIES));
	}

	/**
	 * Constructor which reads the configuration properties from the specified
	 * {@link InputStream}
	 * 
	 * @param propertiesStream
	 */
	public Doorkeeper(InputStream propertiesStream) {
		init();
		load(propertiesStream);
	}

	/**
	 * Constructor which reads the configuration properties from the specified
	 * filename
	 * 
	 * @param propertiesFileName
	 */
	public Doorkeeper(String propertiesFileName) {
		resourceResolver = new ClassLoaderResourceResolver();
		init();
		load(resourceResolver.getResource(propertiesFileName));
	}

	public Doorkeeper(ServletContext servletContext) {
		resourceResolver = new WebAppResourceResolver(servletContext);
		init();
		load(resourceResolver.getResource(DOORKEEPER_PROPERTIES));
	}

	/**
	 * 
	 * @param sc
	 * @return
	 */
	public synchronized static Doorkeeper getInstance(ServletContext sc) {		
		Doorkeeper instance = (Doorkeeper) sc.getAttribute(Doorkeeper.class.getName());
		if (instance == null) {
			instance = new Doorkeeper(sc);
			sc.setAttribute(Doorkeeper.class.getName(), instance);
		}
		return instance;
	}

	/**
	 * Scan the classpath for SPIs
	 */
	private void init() {
		// Load all authenticator SPIs
		List<Class<? extends Authenticator>> authenticatorSPIs = ServiceFinder.findServices(Authenticator.class);
		processAnnotations(authenticatorSPIs, authenticators);
		authenticators = Collections.unmodifiableMap(authenticators);

		List<Class<? extends Authorizer>> authorizerSPIs = ServiceFinder.findServices(Authorizer.class);
		processAnnotations(authorizerSPIs, authorizers);
		authorizers = Collections.unmodifiableMap(authorizers);

		List<Class<? extends AccountProvider>> providerSPIs = ServiceFinder.findServices(AccountProvider.class);
		processAnnotations(providerSPIs, accountProviders);
		accountProviders = Collections.unmodifiableMap(accountProviders);
	}

	public Map<String, Class<? extends Authenticator>> getAuthenticators() {
		return authenticators;
	}

	public Map<String, Class<? extends Authorizer>> getAuthorizers() {
		return authorizers;
	}

	public Map<String, Class<? extends AccountProvider>> getAccountProviders() {
		return accountProviders;
	}

	/**
	 * Retrieves the authenticator chain for the specified context
	 */
	public List<Authenticator> getAuthenticatorChain(String chain) {
		return authenticatorChain.get(chain);
	}

	/**
	 * Retrieves the full authenticator chain
	 * 
	 * @return
	 */
	public List<Authenticator> getAuthenticatorChain() {
		return getAuthenticatorChain(DEFAULT_CHAIN);
	}

	public List<Authorizer> getAuthorizerChain(String chain) {
		return authorizerChain.get(chain);
	}

	public List<Authorizer> getAuthorizerChain() {
		return getAuthorizerChain(DEFAULT_CHAIN);
	}
	
	public List<AccountProvider> getAccountProviderChain(String chain) {
		return accountProviderChain.get(chain);
	}

	public List<AccountProvider> getAccountProviderChain() {
		return getAccountProviderChain(DEFAULT_CHAIN);
	}

	public AccountManager getAccountManager(String chain) {
		return accountManager.get(chain);
	}
	
	public AccountManager getAccountManager() {
		return getAccountManager(DEFAULT_CHAIN);
	}

	/**
	 * Load the properties and initialize the chains
	 */
	private void load(InputStream propertiesStream) {
		try {
			properties = new Properties();
			properties.load(propertiesStream);

			authenticatorChain = new HashMap<String, List<Authenticator>>();
			authorizerChain = new HashMap<String, List<Authorizer>>();
			accountProviderChain = new HashMap<String, List<AccountProvider>>();
			accountManager = new HashMap<String, AccountManager>();
			
			// Find all of the chains
			Set<String> chains = findChains(properties);
			for(String chain : chains) {
				authenticatorChain.put(chain, buildChain(chain, AUTHENTICATOR, properties, authenticators));
				
				authorizerChain.put(chain, buildChain(chain, AUTHORIZER, properties, authorizers));
				
				accountProviderChain.put(chain, buildChain(chain, ACCOUNTPROVIDER, properties, accountProviders));
				
				accountManager.put(chain, new AccountManager(getAccountProviderChain(chain)));
				
				if(log.isInfoEnabled()) {
					log.info("Loaded chain ["+chain+"]");
				}
			}
			
		} catch (Exception e) {
			log.error("Could not load configuration '" + DOORKEEPER_PROPERTIES + "'", e);
		}
	}

	private Set<String> findChains(Properties props) {
		Set<String> chains = new HashSet<String>();
		Pattern chainPattern = Pattern.compile("(\\w+)\\.chain\\.?(\\w+)?");
		for (String propertyName : props.stringPropertyNames()) {
			Matcher matcher = chainPattern.matcher(propertyName);
			if(matcher.matches()) {
				String chain = matcher.group(2);
				if(chain==null)
					chains.add(DEFAULT_CHAIN);
				else
					chains.add(chain);				
			}
		}
		return chains;
	}

	private static <T> List<T> buildChain(String chainName, String prefix, Properties props, Map<String, Class<? extends T>> spiMap) throws InstantiationException, IllegalAccessException, InvocationTargetException,
			NoSuchMethodException, JSONException {
		String chainPropertyName = prefix + ".chain"+(DEFAULT_CHAIN.equals(chainName)?"":"."+chainName);
		if (!props.containsKey(chainPropertyName)) {
			throw new IllegalStateException("Missing '" + chainPropertyName + "' property in configuration file");
		}
		List<T> spiChain = new ArrayList<T>();
		String chain = props.getProperty(chainPropertyName, "").trim();
		if (chain.length() > 0) {
			String[] chainLinks = chain.split(",[\\s]*");
			for (String chainLink : chainLinks) {
				String spiName;
				String spiType;
				// Support the notation name:type
				if (chainLink.contains(":")) {
					String[] s = chainLink.split(":");
					spiName = s[0];
					spiType = s[1];
				} else {
					spiName = spiType = chainLink;
				}
				Class<? extends T> spiClass = spiMap.get(spiType);
				if(spiClass==null) {
					throw new IllegalArgumentException("The specified SPI "+spiType+" does not exist");
				}
				// Instantiate the SPI (this should not fail as the
				// ServiceFinder has already returned a list of instantiatable
				// SPIs)
				T spi = spiClass.newInstance();
				String propertyPrefix = prefix + "." + spiName + ".";
				for (String propertyName : props.stringPropertyNames()) {
					if (propertyName.startsWith(propertyPrefix)) {
						String name = propertyName.substring(propertyPrefix.length());
						Class<?> propertyType = PropertyUtils.getPropertyType(spi, name);
						if (String.class == propertyType) {
							PropertyUtils.setProperty(spi, name, props.getProperty(propertyName));
						} else if (Map.class == propertyType) {
							PropertyUtils.setProperty(spi, name, JSONUtils.json2map(props.getProperty(propertyName)));
						} else {
							log.warn("Unhandled property {} on class {}", name, spiType);
						}
					}
				}
				// Invoke the PostConstruct methods
				for (Method method : spiClass.getMethods()) {
					if (method.getAnnotation(PostConstruct.class) != null) {
						method.invoke(spi);
					}
				}
				spiChain.add(spi);

			}
		}
		return Collections.unmodifiableList(spiChain);
	}

	public void applyConfiguration(String prefix, Object obj) {
		String propertyPrefix = prefix + ".";
		for (String propertyName : properties.stringPropertyNames()) {
			if (propertyName.startsWith(propertyPrefix)) {
				String name = propertyName.substring(propertyPrefix.length());
				try {
					Class<?> propertyType = PropertyUtils.getPropertyType(obj, name);
					if (String.class == propertyType) {
						PropertyUtils.setProperty(obj, name, properties.getProperty(propertyName));
					} else if (Map.class == propertyType) {
						PropertyUtils.setProperty(obj, name, JSONUtils.json2map(properties.getProperty(propertyName)));
					} else {
						log.warn("Unhandled property {} of type {} on class {}", new String[] {name, propertyType.getName(), obj.getClass().getName()});
					}
				} catch (Exception e) {
					log.warn("Unhandled property {} on class {}", name, obj.getClass().getName());
				}
			}
		}
	}

	/**
	 * Scan all of the provided SPIs in the list for Property annotations with a
	 * 'name' name and add the SPI to a map keyed by name
	 * 
	 * @param <T>
	 * @param spiList
	 * @param spiMap
	 */
	private static <T> void processAnnotations(List<Class<? extends T>> spiList, Map<String, Class<? extends T>> spiMap) {
		for (Class<? extends T> spi : spiList) {
			Property annotation = spi.getAnnotation(Property.class);
			if (annotation != null) {
				if ("name".equals(annotation.name())) {
					spiMap.put(annotation.value(), spi);
				}
			} else {
				log.warn("SPI Class '" + spi.getName() + "' does not have a Property annotation");
			}
		}
	}

	public void close() {

	}
}
