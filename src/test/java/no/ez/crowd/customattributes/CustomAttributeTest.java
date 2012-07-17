package no.ez.crowd.customattributes;

import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.Sets;



/**
 * Tests for {@link CustomAttribute} class
 * 
 * @author rodion.alukhanov
 */
public class CustomAttributeTest {
	
	@Test
	public void testDeafultConstructor() {
		CustomAttribute test = new CustomAttribute(); // <-- we need it for JAXB.
		test.toString();
		test.hashCode();
	}
	
	
	@Test
	public void testEqual() {
		
		CustomAttribute a1 = new CustomAttribute("a", AttributeType.STRING, "aa");
		CustomAttribute a2 = new CustomAttribute("a", AttributeType.BINARY, "aa");
		CustomAttribute b1 = new CustomAttribute("b", AttributeType.STRING, "aa");
		
		Assert.assertEquals(a1, a1);
		Assert.assertEquals(a1.hashCode(), a1.hashCode());
				
		Assert.assertEquals(a1, a2);
		Assert.assertEquals(a2, a1);
		Assert.assertEquals(a1.hashCode(), a2.hashCode());
		
		Assert.assertFalse(a1.equals(null));
		
		Assert.assertFalse(a1.equals(b1));
		Assert.assertFalse(b1.equals(a1));
	}
	
	
	@Test
	public void testFind() {
		
		CustomAttribute a = new CustomAttribute("a", AttributeType.STRING, "aa");
		CustomAttribute b = new CustomAttribute("b", AttributeType.BINARY, "bb");
		CustomAttribute c = new CustomAttribute("c", AttributeType.STRING, "cc");
		
		Set<CustomAttribute> attrs = Sets.newHashSet(a, b, c);
		
		Assert.assertEquals(a, CustomAttribute.findByKey("a", attrs));
		Assert.assertEquals(b, CustomAttribute.findByKey("b", attrs));
		Assert.assertEquals(null, CustomAttribute.findByKey("d", attrs));
		Assert.assertEquals(null, CustomAttribute.findByKey("d", null));
	}

}
