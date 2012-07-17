package no.ez.crowd.customattributes;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.internal.stubbing.answers.ThrowsException;
import org.springframework.ldap.core.DirContextAdapter;

import com.atlassian.crowd.directory.ldap.mapper.attribute.AttributeMapper;
import com.google.common.collect.Sets;



/** Tests for {@link AttributeMapperCreater} and {@link CustomAttributeTest} classes. 
 * 
 * @author rodion.alukhanov
 */
public class AttributeMapperCreaterTest {
	
	
	private AttributeMapperCreater mapper;
	
	
	@Before
	public void before() throws IOException {
		mapper = new AttributeMapperCreater(emulateInitPropertyFile(), "ldap.custom-attributes-1.xml");
	}
	
	
	/** Tests if directories can be found by ID and URL
	 * */
	@Test
	public void testFindDirectory() {
		
		List<? extends AttributeMapper> m0 = mapper.createUserAttributeMappers(0, null);
		
		Assert.assertTrue(m0.isEmpty());
		
		List<? extends AttributeMapper> m1 = mapper.createUserAttributeMappers(0, "http://sdfasdf.no:8000");
		
		Assert.assertTrue(m1.size() > 0);
		
		List<? extends AttributeMapper> m2 = mapper.createUserAttributeMappers(123456789, "");
		
		AttributeMapper dmcm = CustomAttributeMapper.findByKey("displayName", m2);

		Assert.assertEquals(dmcm.getKey(), "displayName");
	}
	
	
	@Test
	public void testMapperLogic() throws Exception {
		
		List<? extends AttributeMapper> mappers = mapper.createUserAttributeMappers(0, "http://sdfasdf.no:8000");
		DirContextAdapter dca = mock(DirContextAdapter.class, new ThrowsException(new RuntimeException("Unexpected invocation")));
		
		Set<byte[]> photo       = Sets.newHashSet("abcdÖÄ123".getBytes("ISO-8859-1"));
		Set<String> photoBase64 = Sets.newHashSet("YWJjZNbEMTIz"); // same as "abcdÖÄ123" encoded.
		Set<String> displayName = Sets.newHashSet("Mustermann");
		Set<String> description = Sets.newHashSet("description 1", "description 2", "description 3");
		
		doReturn(photo.toArray()).      when(dca).getObjectAttributes("jpegPhoto");
		doReturn(displayName.toArray()).when(dca).getObjectAttributes("displayName");
		doReturn(description.toArray()).when(dca).getObjectAttributes("description");
		
		AttributeMapper m1 = CustomAttributeMapper.findByKey("photo",       mappers);
		AttributeMapper m2 = CustomAttributeMapper.findByKey("displayName", mappers);
		AttributeMapper m3 = CustomAttributeMapper.findByKey("description", mappers);
		
		Set<String> photoMapped       = m1.getValues(dca);
		Set<String> displayNameMapped = m2.getValues(dca);
		Set<String> descriptionMapped = m3.getValues(dca);
		
		compareSets(photoBase64, photoMapped);
		compareSets(displayName, displayNameMapped);
		compareSets(description, descriptionMapped);
	}
	
	
	
	@Test
	public void testNullAsLDAPResponse() throws Exception {
		
		List<? extends AttributeMapper> mappers = mapper.createUserAttributeMappers(0, "http://sdfasdf.no:8000");
		DirContextAdapter dca = mock(DirContextAdapter.class, new ThrowsException(new RuntimeException("Unexpected invocation")));
		
		doReturn(null).when(dca).getObjectAttributes(Matchers.anyString());
		
		AttributeMapper m1 = CustomAttributeMapper.findByKey("photo", mappers);
		AttributeMapper m2 = CustomAttributeMapper.findByKey("description", mappers);
		AttributeMapper m3 = CustomAttributeMapper.findByKey("someInt", mappers);
		
		m1.getValues(dca); // no NPE
		m2.getValues(dca); // no NPE
		m3.getValues(dca); // no NPE
	}
	
	
	
	@Test
	public void testNumber() throws Exception {
		
		List<? extends AttributeMapper> mappers = mapper.createUserAttributeMappers(0, "http://sdfasdf.no:8000");
		DirContextAdapter dca = mock(DirContextAdapter.class, new ThrowsException(new RuntimeException("Unexpected invocation")));		
		
		Set<Object> integers = new HashSet<Object>();
		integers.add(123);
		integers.add(456);
		integers.add("789");

		Set<Object> doubles = new HashSet<Object>();
		doubles.add(123.123);
		doubles.add("456.456");
		
		doReturn(integers.toArray()).when(dca).getObjectAttributes("int");
		doReturn(doubles.toArray()).when(dca).getObjectAttributes("double");
		
		AttributeMapper m1 = CustomAttributeMapper.findByKey("someInt",       mappers);
		AttributeMapper m2 = CustomAttributeMapper.findByKey("someDouble", mappers);
		
		compareSets(Sets.newHashSet("123", "456", "789"), m1.getValues(dca));
		compareSets(Sets.newHashSet("123.123", "456.456"), m2.getValues(dca));
	}
	
	
	private static <T> void compareSets(Set<T> a, Set<T> b) {
		
		Iterator<T> itA = new TreeSet<T>(a).iterator();
		Iterator<T> itB = new TreeSet<T>(b).iterator();
		
		while (itA.hasNext() && itB.hasNext()) {
			T item1 = itA.next();
			T item2 = itB.next();
			
			Assert.assertEquals(item1, item2);
		}
		
		Assert.assertFalse("Set " + a + " has more elements than than " + b + ".", itA.hasNext());
		Assert.assertFalse("Set " + b + " has more elements than than " + a + ".", itB.hasNext());
	}
	

	private Reader emulateInitPropertyFile() throws IOException {
		
		URL config = getClass().getResource("/ldap.custom-attributes-1.xml");

		File file = new File(config.getPath());
		
		Properties properties = new Properties();
		
		properties.put("crowd.home", file.getParentFile().getPath());
		
		ByteArrayOutputStream prop = new ByteArrayOutputStream();
		
		properties.store(prop, "Test");
	
		return new InputStreamReader(new ByteArrayInputStream(prop.toByteArray()), "ISO-8859-1"); // it is the only one possible encoding for properties
		
	}
	
	
	

			
			
}
