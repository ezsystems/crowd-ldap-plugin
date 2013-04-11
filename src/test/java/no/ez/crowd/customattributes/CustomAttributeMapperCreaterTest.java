package no.ez.crowd.customattributes;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.naming.directory.SearchControls;

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
public class CustomAttributeMapperCreaterTest {
	
	
	private AttributeMapperCreater mapper;
	
	
	@Before
	public void before() throws IOException {
		
		Reader properties = AttributeMapperCreaterTest.emulateInitPropertyFile();
		
		mapper = new AttributeMapperCreater(properties, "ldap.custom-attributes-1.xml");
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
		
		// repeat for groups
		
		List<? extends AttributeMapper> g0 = mapper.createGroupAttributeMappers(0, null);
		
		Assert.assertTrue(g0.isEmpty());
		
		List<? extends AttributeMapper> g2 = mapper.createGroupAttributeMappers(123456789, "");
		
		AttributeMapper mapper1 = CustomAttributeMapper.findByKey("displayName", g2);
		Assert.assertNull(mapper1);
		
		AttributeMapper mapper2 = CustomAttributeMapper.findByKey("groupName", g2);
		Assert.assertEquals(mapper2.getKey(), "groupName");
	}
	

	@Test
	public void testOperationalAttributes1() throws Exception {
		
		SearchControls controls = new SearchControls();
		
		mapper.putOperationalAttribute(controls, 0, "http://sdfasdf.no:8000");
		
		List<String> names = Arrays.asList(controls.getReturningAttributes());
		
		Assert.assertTrue(names.size() == 3);
		Assert.assertTrue(names.contains("*"));
		Assert.assertTrue(names.contains("createTime"));
		Assert.assertTrue(names.contains("modifyTime"));
	}
	
	
	@Test
	public void testOperationalAttributes2() throws Exception {
		
		SearchControls controls = new SearchControls();
		
		controls.setReturningAttributes(new String[] {"a", "b"});
		
		mapper.putOperationalAttribute(controls, 0, "http://sdfasdf.no:8000");
		
		List<String> names = Arrays.asList(controls.getReturningAttributes());
		
		Assert.assertTrue(names.size() == 4);
		Assert.assertTrue(names.contains("a"));
		Assert.assertTrue(names.contains("b"));
		Assert.assertTrue(names.contains("createTime"));
		Assert.assertTrue(names.contains("modifyTime"));
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
	
	
	
	
	static <T> void compareSets(Set<T> a, Set<T> b) {
		
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
	


			
}
