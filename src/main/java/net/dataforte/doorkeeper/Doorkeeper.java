package net.dataforte.doorkeeper;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.annotation.PostConstruct;

import net.dataforte.commons.resources.ResourceFinder;
import net.dataforte.commons.resources.ServiceFinder;
import net.dataforte.doorkeeper.account.provider.AccountProvider;
import net.dataforte.doorkeeper.annotations.Property;
import net.dataforte.doorkeeper.authenticator.Authenticator;

import org.apache.commons.beanutils.PropertyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Doorkeeper {
	private static final String AUTHENTICATOR = "authenticator";
	private static final String ACCOUNTPROVIDER = "accountprovider";

	static final Logger log = LoggerFactory.getLogger(Doorkeeper.class);

	private static final String DOORKEEPER_PROPERTIES = "doorkeeper.properties";
	private Map<String, Class<? extends Authenticator>> authenticators = new HashMap<String, Class<? extends Authenticator>>();
	private Map<String, Class<? extends AccountProvider>> accountProviders = new HashMap<String, Class<? extends AccountProvider>>();

	private List<Authenticator> authenticatorChain;
	private List<AccountProvider> accountProviderChain;

	public Doorkeeper() {
		init();
		load();
	}

	public Map<String, Class<? extends Authenticator>> getAuthenticators() {
		return authenticators;
	}

	public Map<String, Class<? extends AccountProvider>> getAccountProviders() {
		return accountProviders;
	}

	/**
	 * Scan the classpath for SPIs
	 */
	private void init() {
		List<Class<? extends Authenticator>> authenticatorSPIs = ServiceFinder.findServices(Authenticator.class);
		processAnnotations(authenticatorSPIs, authenticators);
		authenticators = Collections.unmodifiableMap(authenticators);

		List<Class<? extends AccountProvider>> providerSPIs = ServiceFinder.findServices(AccountProvider.class);
		processAnnotations(providerSPIs, accountProviders);
		accountProviders = Collections.unmodifiableMap(accountProviders);

	}

	public List<Authenticator> getAuthenticatorChain() {
		return authenticatorChain;
	}

	public List<AccountProvider> getAccountProviderChain() {
		return accountProviderChain;
	}

	/**
	 * Load the properties and initializing the chains
	 */
	private void load() {
		try {
			Properties props = new Properties();
			props.load(ResourceFinder.getResource(DOORKEEPER_PROPERTIES));

			authenticatorChain = buildChain(AUTHENTICATOR, props, authenticators);
			accountProviderChain = buildChain(ACCOUNTPROVIDER, props, accountProviders);
		} catch (Exception e) {
			log.error("Could not load configuration '" + DOORKEEPER_PROPERTIES + "'", e);
		}
	}
	
	private static <T> List<T> buildChain(String prefix, Properties props, Map<String, Class<? extends T>> spiMap) throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		String chainName = prefix+".chain";
		if (!props.containsKey(chainName)) {
			throw new IllegalStateException("Missing '" + chainName + "' property in configuration file");
		}
		List<T> spiChain = new ArrayList<T>();
		String chain = props.getProperty(chainName);
		String[] chainLinks = chain.split(",[\\s]*");
		for (String chainLink : chainLinks) {
			String spiName;
			String spiType;
			// Support the notation name:type
			if(chainLink.contains(":")) {
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
					PropertyUtils.setProperty(spi, name, props.getProperty(propertyName));
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
		return Collections.unmodifiableList(spiChain);
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
}
