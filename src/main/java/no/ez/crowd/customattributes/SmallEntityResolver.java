package no.ez.crowd.customattributes;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;



/** Simple implementation for {@link EntityResolver} and {@link ErrorHandler} to fetch the
 *  Schema for the configuration file from the classpath and reports all the errors as exceptions.
 * 
 *  @author rodion.alukhanov
 */
public class SmallEntityResolver implements EntityResolver, URIResolver, ErrorHandler {
	
	private final Logger logger = LoggerFactory.getLogger(SmallEntityResolver.class);
	
	private static final String SCHEMA_URI = "http://ez.no/crowd/ldap.custom-attributes.xsd";
	
	private static final String SCHEMA_CLASSPATH = "/ldap.custom-attributes.xsd";
	
	
	private final String task;
	
	
	/**
	 * @param task 
	 * 		name of the task. Added to the error message.
	 */
	public SmallEntityResolver(String task) {
		this.task = task;
	}
	
	
	public InputStream getSchema() {
		
		InputStream dtd = getClass().getResourceAsStream(SCHEMA_CLASSPATH);
		
		if (dtd == null) {
			throw new RuntimeException("LDAP custom attribute XSD not found as a classpath resource [" + SCHEMA_CLASSPATH + "].");
		}
		
		return dtd;
	}
	

	@Override
	public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
		if (SCHEMA_URI.equals(systemId)) {
			InputStream dtd = getSchema();
			return new InputSource(dtd);
		} else {
			throw new SAXException("Error executing task [" + task + "]. Unknown entity [" + (systemId != null ? systemId : publicId) + "].");
		}
	}


	@Override
	public Source resolve(String href, String base) throws TransformerException {
		if (SCHEMA_URI.equals(href)) {
			InputStream dtd = getSchema();
			return new StreamSource(dtd);
			
		} else {
			throw new TransformerException("Error executing task [" + task + "]. Unknown entity [" + href + "].");
		}
	}

	@Override
	public void warning(SAXParseException exception) throws SAXException {
		logger.warn("Problem processing task [" + task + "].", exception);
	}

	
	@Override
	public void error(SAXParseException exception) throws SAXException {
		throw new SAXException("Error processing task [" + task + "].", exception);
	}

	
	@Override
	public void fatalError(SAXParseException exception) throws SAXException {
		throw new SAXException("Fatal error processing task [" + task + "].", exception);
	}

}
