package no.ez.crowd.customattributes;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nonnull;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;



/** Reads attribute descriptions for LDAP custom attributes.
 *  Rereads the configuration file, if it was changed since the last access.
 * 
 *  @author rodion.alukhanov
 */
class ConfigurationReader {
	
	
	private final Logger logger = LoggerFactory.getLogger(ConfigurationReader.class);

	
	private static final String FEATURE_NAMESPACES = "http://xml.org/sax/features/namespaces";
	 
	
	private JAXBContext context;
	
	private Configuration config;
	private DateTime configLastLoad;
	
	private File source;
	
	
	public ConfigurationReader(File source) {
		initJAXBContext();
		this.source = source;
	}
	
	
	private void initJAXBContext() {
		try {
			context = JAXBContext.newInstance(Configuration.class);
		} catch (JAXBException e) {
			throw new RuntimeException("Unexpected. Error creating JAXB context for reading the configuration file.", e);
		}
	}

	
	@Nonnull
	public Collection<CustomAttribute> readUserAttributes(long directoryId, String directoryUrl) {
		return readAttributes(directoryId, directoryUrl).userAttrs;
	}
	
	
	@Nonnull
	public Collection<CustomAttribute> readGroupAttributes(long directoryId, String directoryUrl) {
		return readAttributes(directoryId, directoryUrl).groupAttrs;
	}
	

	protected DoubleList readAttributes(long directoryId, String directoryUrl) {

		try {
			Configuration cfg = loadConfig();
			
			DoubleList result = new DoubleList();
			
			logger.debug("Searching for custom attribute configuration for directory [" + directoryId + "; " + directoryUrl + "].");
	
			for (Directory dir : cfg.getDirectories()) {
				if (dir.isServer(directoryId, directoryUrl)) {
					Collection<CustomAttribute> ulist = dir.getUserAttributes();
					Collection<CustomAttribute> glist = dir.getGroupAttributes();
					
					logger.debug("Adding attributes from directory [" + dir.getName() + "].");
					
					if (ulist != null) {
						result.userAttrs.addAll(ulist);	
					}
					
					if (glist != null) {
						result.groupAttrs.addAll(glist);	
					}
				}
			}
			
			return result;
		} catch (IOException e) {
			throw new RuntimeException("Error reading custom attribute configuration. No cache found to fallback.", e);
		}
	}

	
	
	protected Configuration loadConfig() throws IOException {
		
		try {
			if ( ! source.isFile() || ! source.canRead()) {
				throw new IOException("LDAP custom attributes configuration file [" + source + "] cannot be read. Check, if it exists and is readable.");	
			}
			
			long lastmod = source.lastModified();
			
			if (lastmod <= 0L) {
				throw new IOException("Error retriving last modified date for the LDAP custom attributes configuration file [" + lastmod + "]. Autorefresh is disabled.");
			} 
			
			long lastload = configLastLoad != null ? configLastLoad.getMillis() : 0L;
			
			if (lastload > lastmod + 1000) { // 1 sec. tolerance
				logger.debug("Reading custom attribute configuration from cache. Last update was at " + configLastLoad + ".");

			} else {
				logger.info("Reading custom attribute configuration from file [" + source + "].");
				
				Unmarshaller um = context.createUnmarshaller();
				
				try {
					XMLReader xmlreader = XMLReaderFactory.createXMLReader();
					xmlreader.setFeature(FEATURE_NAMESPACES, false);
					xmlreader.setEntityResolver(new SmallEntityResolver());
					
					Source xmlsource = new SAXSource(xmlreader, new InputSource(new FileInputStream(source)));
				
					Configuration result = (Configuration)um.unmarshal(xmlsource);
				
					config = result; // updating cache.
					configLastLoad = new DateTime();
					
					logger.info("Custom attribute configuration parsed. Loaded directories: " + result.getDirectories() + ".");
					
				} catch (SAXException e) {
					throw new JAXBException("XML parsing error. " + e.getMessage(), e);
				}
			}
			
		} catch (IOException e) {
			if (config == null) {
				throw new IOException("Fatal error. Error reading custom attribute configuration file.", e);
			} else {
				logger.error("Error reading custom attribute configuration file. Using the old version.", e);
			}
		} catch (JAXBException e) {
			if (config == null) {
				throw new IOException("Fatal error. Error reading custom attribute configuration file.", e);
			} else {
				logger.error("Error reading custom attribute configuration file. Using the old version.", e);
			}
		}
		
		return config;
	}

	
	/** User and group attributes in a single object.
	 */
	protected static class DoubleList {
		@Nonnull
		final List<CustomAttribute> userAttrs = new ArrayList<CustomAttribute>();
		@Nonnull
		final List<CustomAttribute> groupAttrs = new ArrayList<CustomAttribute>();
	}
	
	
}











