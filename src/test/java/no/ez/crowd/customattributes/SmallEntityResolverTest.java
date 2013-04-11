package no.ez.crowd.customattributes;

import java.io.IOException;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;

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
	public void testEntity() throws SAXException, IOException {

		SmallEntityResolver resolver = new SmallEntityResolver("test");
		
		InputSource source = resolver.resolveEntity(null, "http://ez.no/crowd/ldap.custom-attributes.xsd");
		
		Assert.assertNotNull(source);
		
		try {
			resolver.resolveEntity(null, "unknown_entity");
			Assert.fail();
		} catch (SAXException e) {
			// fine.
		}
	}
	
	
	@Test
	public void testUrl() throws TransformerException {
		
		SmallEntityResolver resolver = new SmallEntityResolver("test");
		
		Source source = resolver.resolve("http://ez.no/crowd/ldap.custom-attributes.xsd", null);
		
		Assert.assertNotNull(source);
		
		try {
			resolver.resolve("unknown_entity", null);
			Assert.fail();
		} catch (TransformerException e) {
			// fine.
		}
	}

}
