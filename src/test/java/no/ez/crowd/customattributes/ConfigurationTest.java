package no.ez.crowd.customattributes;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.internal.stubbing.answers.ThrowsException;


/** Tests for {@link ConfigurationReader} class.
 * 
 * @author rodion.alukhanov
 */
public class ConfigurationTest  {
	
	
	@Test
	public void testReadConfig() throws URISyntaxException {
		
		URI testConfig = getClass().getResource("/ldap.custom-attributes-1.xml").toURI();
		
		File source = new File(testConfig);
		ConfigurationReader configuration = new ConfigurationReader(source);
		
		Collection<CustomAttribute> attrs = configuration.readUserAttributes(100, "http://sdfasdf.no:8000");

		CustomAttribute photoAttr = CustomAttribute.findByKey("photo", attrs);
		
		Assert.assertEquals("photo", photoAttr.getKey());
		Assert.assertEquals(AttributeType.BINARY, photoAttr.getType());
		Assert.assertEquals("jpegPhoto", photoAttr.getLdapName());
		Assert.assertEquals(false, photoAttr.isOperational());
		
		CustomAttribute createdAttr = CustomAttribute.findByKey("created", attrs);
		Assert.assertEquals(true, createdAttr.isOperational());
		
		CustomAttribute attrNull = CustomAttribute.findByKey("unknown", attrs);
		Assert.assertNull(attrNull);
	}
	
	
	@Test
	public void testReadGroupAttrs() throws URISyntaxException {
		
		URI testConfig = getClass().getResource("/ldap.custom-attributes-1.xml").toURI();
		
		File source = new File(testConfig);
		ConfigurationReader configuration = new ConfigurationReader(source);
		
		Collection<CustomAttribute> attrs = configuration.readGroupAttributes(100, "http://sdfasdf.no:8000");

		CustomAttribute attr = CustomAttribute.findByKey("groupName", attrs);
		
		Assert.assertEquals("groupName", attr.getKey());
		Assert.assertEquals(AttributeType.STRING, attr.getType());
		Assert.assertEquals("displayName", attr.getLdapName());
	}
	
	
	@Test
	public void testReadConfigReloading() throws IOException, URISyntaxException {
		
		URI testConfig1 = getClass().getResource("/ldap.custom-attributes-1.xml").toURI();
		File source1 = spy(new File(testConfig1)); // spying. Full mock would be too complex.
		
		ConfigurationReader configuration = new ConfigurationReader(source1);
		Collection<CustomAttribute> attrs = configuration.readUserAttributes(100, "http://sdfasdf.no:8000");

		CustomAttribute attr = CustomAttribute.findByKey("photo", attrs);
		Assert.assertEquals("jpegPhoto", attr.getLdapName());
		
		URI testConfig2 = getClass().getResource("/ldap.custom-attributes-2.xml").toURI();
		File source2 = spy(new File(testConfig2)); // spying. Full mock would be to complex.
		
		doReturn(source2.getPath()).when(source1).getPath();
		doReturn(source2.getAbsoluteFile()).when(source1).getAbsoluteFile();
		doReturn(source2.getAbsolutePath()).when(source1).getAbsolutePath();
		doReturn(source2.getCanonicalPath()).when(source1).getCanonicalPath();
		doReturn(source2.getCanonicalFile()).when(source1).getCanonicalFile();
		doReturn(source2.getName()).when(source1).getName();
		
		Collection<CustomAttribute> attrs2 = configuration.readUserAttributes(100, "http://sdfasdf.no:8000"); // no reloading yet.
		CustomAttribute attr2 = CustomAttribute.findByKey("photo", attrs2); 
		Assert.assertEquals("jpegPhoto", attr2.getLdapName());
		
		doReturn(System.currentTimeMillis()).when(source1).lastModified();
		
		Collection<CustomAttribute> attrs3 = configuration.readUserAttributes(100, "http://sdfasdf.no:8000");
		CustomAttribute attr3 = CustomAttribute.findByKey("photo", attrs3); // reloaded.
		Assert.assertEquals("jpegPhoto-updated", attr3.getLdapName());
	}
	
	
	@Test
	public void testReadConfigBrokenFile() {
		File source = mock(File.class, new ThrowsException(new RuntimeException("Unexpected invocation")));
		
		doReturn("Mocked-File").when(source).toString();
		doReturn(true).when(source).isFile();
		doReturn(true).when(source).canRead();
		doReturn(System.currentTimeMillis() - 10000).when(source).lastModified();
		
		doReturn("C:\\mock.txt").when(source).getAbsolutePath();
	}
	

}
