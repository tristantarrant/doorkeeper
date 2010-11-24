package net.dataforte.doorkeeper;

import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.servlet.ServletContext;

import net.dataforte.commons.resources.ClassLoaderResourceResolver;
import net.dataforte.commons.resources.IResourceResolver;
import net.dataforte.commons.resources.ServiceFinder;
import net.dataforte.commons.resources.ServletContextResourceResolver;
import net.dataforte.commons.resources.WebAppResourceResolver;
import net.dataforte.doorkeeper.account.AccountManager;
import net.dataforte.doorkeeper.account.provider.AccountProvider;
import net.dataforte.doorkeeper.annotations.Property;
import net.dataforte.doorkeeper.authenticator.Authenticator;
import net.dataforte.doorkeeper.authorizer.Authorizer;

import org.apache.commons.beanutils.PropertyUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Doorkeeper {
	private static final String AUTHENTICATOR = "authenticator";
	private static final String AUTHORIZER = "authorizer";
	private static final String ACCOUNTPROVIDER = "accountprovider";

	static final Logger log = LoggerFactory.getLogger(Doorkeeper.class);

	private static final String DOORKEEPER_PROPERTIES = "doorkeeper.properties";
	private Map<String, Class<? extends Authenticator>> authenticators = new HashMap<String, Class<? extends Authenticator>>();
	private Map<String, Class<? extends Authorizer>> authorizers = new HashMap<String, Class<? extends Authorizer>>();
	private Map<String, Class<? extends AccountProvider>> accountProviders = new HashMap<String, Class<? extends AccountProvider>>();

	private List<Authenticator> authenticatorChain;
	private List<Authorizer> authorizerChain;
	private List<AccountProvider> accountProviderChain;
	private AccountManager accountManager;

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
	public List<Authenticator> getAuthenticatorChain(String context) {
		return authenticatorChain;
	}

	/**
	 * Retrieves the full authenticator chain
	 * 
	 * @return
	 */
	public List<Authenticator> getAuthenticatorChain() {
		return authenticatorChain;
	}

	public List<Authorizer> getAuthorizerChain(String context) {
		return authorizerChain;
	}

	public List<Authorizer> getAuthorizerChain() {
		return authorizerChain;
	}

	public List<AccountProvider> getAccountProviderChain() {
		return accountProviderChain;
	}

	public AccountManager getAccountManager() {
		return accountManager;
	}

	/**
	 * Load the properties and initialize the chains
	 */
	private void load(InputStream propertiesStream) {
		try {
			properties = new Properties();
			properties.load(propertiesStream);

			authenticatorChain = buildChain(AUTHENTICATOR, properties, authenticators);
			authorizerChain = buildChain(AUTHORIZER, properties, authorizers);
			accountProviderChain = buildChain(ACCOUNTPROVIDER, properties, accountProviders);

			accountManager = new AccountManager(accountProviderChain);
		} catch (Exception e) {
			log.error("Could not load configuration '" + DOORKEEPER_PROPERTIES + "'", e);
		}
	}

	private static <T> List<T> buildChain(String prefix, Properties props, Map<String, Class<? extends T>> spiMap) throws InstantiationException, IllegalAccessException, InvocationTargetException,
			NoSuchMethodException, JSONException {
		String chainName = prefix + ".chain";
		if (!props.containsKey(chainName)) {
			throw new IllegalStateException("Missing '" + chainName + "' property in configuration file");
		}
		List<T> spiChain = new ArrayList<T>();
		String chain = props.getProperty(chainName, "").trim();
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
							PropertyUtils.setProperty(spi, name, json2map(props.getProperty(propertyName)));
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

	public static Map<String, ?> json2map(String s) throws JSONException {
		JSONObject json = new JSONObject(s);
		Map<String, Object> map = new LinkedHashMap<String, Object>();
		for (Iterator<String> it = json.keys(); it.hasNext();) {
			String key = it.next();
			Object value = json.get(key);
			if (value.getClass() == String.class) {
				map.put(key, value);
			} else if (value.getClass() == JSONArray.class) {
				List<String> l = new ArrayList<String>();
				JSONArray a = (JSONArray) value;
				for (int i = 0; i < a.length(); i++) {
					l.add(a.getString(i));
				}
				map.put(key, l);
			}
		}
		return map;
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
						PropertyUtils.setProperty(obj, name, json2map(properties.getProperty(propertyName)));
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
