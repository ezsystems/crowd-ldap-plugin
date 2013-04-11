package no.ez.crowd.customattributes;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.internal.stubbing.answers.ThrowsException;
import org.springframework.ldap.core.DirContextAdapter;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;




/** Tests for {@link CustomAttributeMapper} class.
 * 
 * @author rodion.alukhanov
 */
public class CustomAttributeMapperTest {
	
	@Test
	public void testUnwrapKeys() {
		
		CustomAttribute a = new CustomAttribute("a", AttributeType.STRING, "aa");
		CustomAttribute b = new CustomAttribute("b", AttributeType.BINARY, "bb");
		CustomAttribute c = new CustomAttribute("c", AttributeType.STRING, "cc");
		
		List<CustomAttribute> attrs = Lists.newArrayList(a, b, c);
		
		List<CustomAttributeMapper> mappers = CustomAttributeMapper.createList(attrs);
		
		List<String> keys = CustomAttributeMapper.unwrapKeys(mappers);
		
		Assert.assertArrayEquals(new String[] {"a", "b", "c"}, keys.toArray());
	}
	
	
	@Test
	public void testNumber() throws Exception {
		
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
		
		CustomAttributeMapper m1 = new CustomAttributeMapper(new CustomAttribute("a", AttributeType.INTEGER, "int"));
		CustomAttributeMapper m2 = new CustomAttributeMapper(new CustomAttribute("b", AttributeType.DOUBLE, "double"));
		
		CustomAttributeMapperCreaterTest.compareSets(Sets.newHashSet("123", "456", "789"), m1.getValues(dca));
		CustomAttributeMapperCreaterTest.compareSets(Sets.newHashSet("123.123", "456.456"), m2.getValues(dca));
	}
	
	
	@Test
	public void testParseError() throws InvalidNameException {
		
		DirContextAdapter dca = mock(DirContextAdapter.class, new ThrowsException(new RuntimeException("Unexpected invocation")));		
		
		Set<Object> integers = new HashSet<Object>();
		integers.add("--error--");
		integers.add("123");

		Set<Object> doubles = new HashSet<Object>();
		doubles.add("--error--");
		doubles.add("123.123");
		
		doReturn(new LdapName("cn=test-dn")).when(dca).getDn();
		doReturn(integers.toArray()).when(dca).getObjectAttributes("int");
		doReturn(doubles.toArray()).when(dca).getObjectAttributes("double");
		
		CustomAttributeMapper m1 = new CustomAttributeMapper(new CustomAttribute("a", AttributeType.INTEGER, "int"));
		CustomAttributeMapper m2 = new CustomAttributeMapper(new CustomAttribute("b", AttributeType.DOUBLE, "double"));
		
		CustomAttributeMapperCreaterTest.compareSets(Sets.newHashSet("123"), m1.getValues(dca));
		CustomAttributeMapperCreaterTest.compareSets(Sets.newHashSet("123.123"), m2.getValues(dca));
	}
	
	
	@Test
	public void testDateTimeDefaultPattern() throws InvalidNameException {
		
		String d1 = readValue("20120723085603.000Z", AttributeType.DATETIME);
		
		String d2 = readValue("20120723085603Z", AttributeType.DATETIME);
		
		String d3 = readValue("", AttributeType.DATETIME);
		
		String d4 = readValue("20060204001506Z", AttributeType.DATETIME);
				
		Assert.assertEquals("2012-07-23T08:56:03.000Z", d1);
		Assert.assertEquals("2012-07-23T08:56:03.000Z", d2);
		Assert.assertNull(d3);
		
		Assert.assertEquals("2006-02-04T00:15:06.000Z", d4);
	}
	
	
	@Test
	public void testDateTimeCustomPattern() throws InvalidNameException {
		
		String d1 = readValue("2012-07-23T08:56:03+02:00", AttributeType.DATETIME, new Format("yyyy-MM-dd'T'kk:mm:ssZZ"));
		
		String d2 = readValue("2012-07-23T08:56:03+02:00", AttributeType.DATETIME, new Format("yyyy-MM-dd'T'kk:mm:ssZZ", "+01:00"));
		
		String d3 = readValue("", AttributeType.DATETIME, new Format("yyyy-MM-dd'T'kk:mm:ssZZ", "+01:00"));
		
		Assert.assertEquals("2012-07-23T06:56:03.000Z", d1);
		Assert.assertEquals("2012-07-23T07:56:03.000+01:00", d2);
		Assert.assertNull(d3);
	}
	
	
	
	@Test
	public void testLocalDateTimeDefaultPattern() throws InvalidNameException {
		
		String d1 = readValue("20120723085603.000", AttributeType.LOCAL_DATETIME);
		
		String d2 = readValue("20120723085603", AttributeType.LOCAL_DATETIME);
		
		String d3 = readValue("", AttributeType.DATETIME);
				
		Assert.assertEquals("2012-07-23T08:56:03.000", d1);
		Assert.assertEquals("2012-07-23T08:56:03.000", d2);
		Assert.assertNull(d3);
	}
	
	
	@Test
	public void testLocalDateTimeCustomPattern() throws InvalidNameException {
		
		String d1 = readValue("2012-07-23T08:56:03", AttributeType.LOCAL_DATETIME, new Format("yyyy-MM-dd'T'kk:mm:ss"));
		
		Assert.assertEquals("2012-07-23T08:56:03.000", d1);
	}
	
	
	
	private static String readValue(Object value, AttributeType type, Format... ff) throws InvalidNameException {
		
		DirContextAdapter dca = mock(DirContextAdapter.class, new ThrowsException(new RuntimeException("Unexpected invocation")));		

		Set<Object> values = new HashSet<Object>();
		values.add(value);
		
		doReturn(new LdapName("cn=test-dn")).when(dca).getDn();
		doReturn(values.toArray()).when(dca).getObjectAttributes("test");
		
		List<Format> formats = new ArrayList<Format>();
		if (ff != null) {
			formats.addAll(Arrays.asList(ff));
		}
		
		CustomAttributeMapper m1 = new CustomAttributeMapper(new CustomAttribute("a", type, "test", formats));
		
		Set<String> result = m1.getValues(dca);
		
		return result.isEmpty() ? null : result.iterator().next(); 
	}
	
	
	@Test
	public void testTimeZones() {
		
		System.out.println(
				new DateTime(DateTimeZone.UTC)
			);
		
		System.out.println(
		DateTimeZone.getAvailableIDs()
		);
	}
	

}





