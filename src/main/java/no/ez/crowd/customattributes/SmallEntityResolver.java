package no.ez.crowd.customattributes;

import java.io.IOException;
import java.io.InputStream;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;



/** Simple implementation for {@link EntityResolver} to fetch the
 *  DTD for the configuration file from the classpath.
 * 
 *  @author rodion.alukhanov
 */
public class SmallEntityResolver implements EntityResolver {
	
	
	private static final String LDAP_CUSTOM_ATTRIBUTES_SYSTEM_ID = "http://ez.no/crowd/ldap.custom-attributes.dtd";
	

	@Override
	public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
		if (LDAP_CUSTOM_ATTRIBUTES_SYSTEM_ID.equals(systemId)) {
			
			String cpath = "/ldap.custom-attributes.dtd"; 
			
			InputStream dtd = getClass().getResourceAsStream(cpath);
			
			if (dtd == null) {
				throw new RuntimeException("LDAP custom attribute DTD not found as a classpath resource [" + cpath + "].");
			}
			
			return new InputSource(dtd);
			
		} else {
			throw new SAXException("Unknown entity [" + (systemId != null ? systemId : publicId) + "].");
		}
	}

}
