package no.ez.crowd.customattributes;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;




/** Tests for {@link SmallEntityResolver} class
 * 
 * @author rodion.alukhanov
 */
public class SmallEntityResolverTest {
	
	
	@Test
	public void testMain() throws SAXException, IOException {

		SmallEntityResolver resolver = new SmallEntityResolver();
		
		InputSource source = resolver.resolveEntity(null, "http://ez.no/crowd/ldap.custom-attributes.dtd");
		
		Assert.assertNotNull(source);
		
		try {
			resolver.resolveEntity(null, "unknown_entity");
			Assert.fail();
		} catch (SAXException e) {
			// fine.
		}
	}

}
