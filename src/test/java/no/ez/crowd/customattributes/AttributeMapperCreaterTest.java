package no.ez.crowd.customattributes;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.junit.Assert;
import org.junit.Test;

import com.atlassian.crowd.directory.ldap.LDAPPropertiesMapperImpl;



/** Tests for {@link AttributeMapperCreater} class.
 * 
 * @author rodion.alukhanov
 */
public class AttributeMapperCreaterTest {


	@Test
	public void testBinaryAttributesAdding() throws IOException {
		
		Reader properties = AttributeMapperCreaterTest.emulateInitPropertyFile();
		AttributeMapperCreater mapper = new AttributeMapperCreater(properties, "ldap.custom-attributes-1.xml");
		
		Map<String, String> env = new HashMap<String, String>();
		env.put(LDAPPropertiesMapperImpl.CONNECTION_BINARY_ATTRIBUTES, "a b c");
		
		mapper.putBinaryAttributes(env, 123456789, "");
		String binProperty = env.get(LDAPPropertiesMapperImpl.CONNECTION_BINARY_ATTRIBUTES);
		
		Assert.assertEquals("a b c jpegPhoto", binProperty);
	}
	
	
	@Test
	public void testBinaryAttributesCreating() throws IOException {
		
		Reader properties = AttributeMapperCreaterTest.emulateInitPropertyFile();
		AttributeMapperCreater mapper = new AttributeMapperCreater(properties, "ldap.custom-attributes-1.xml");
		
		Map<String, String> env = new HashMap<String, String>();
		
		mapper.putBinaryAttributes(env, 123456789, "");
		String binProperty = env.get(LDAPPropertiesMapperImpl.CONNECTION_BINARY_ATTRIBUTES);
		
		Assert.assertEquals("jpegPhoto", binProperty);
	}
	

	static Reader emulateInitPropertyFile() throws IOException {
		
		URL config = CustomAttributeMapperCreaterTest.class.getResource("/ldap.custom-attributes-1.xml");

		File file = new File(config.getPath());
		
		Properties properties = new Properties();
		
		properties.put("crowd.home", file.getParentFile().getPath());
		
		ByteArrayOutputStream prop = new ByteArrayOutputStream();
		
		properties.store(prop, "Test");
	
		return new InputStreamReader(new ByteArrayInputStream(prop.toByteArray()), "ISO-8859-1"); // it is the only one possible encoding for properties
		
	}
}
