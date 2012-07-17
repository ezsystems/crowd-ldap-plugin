package no.ez.crowd.customattributes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ldap.core.DirContextAdapter;

import com.atlassian.crowd.directory.ldap.mapper.attribute.AttributeMapper;






/** Attribute mapper, which gets the LDAP attribute from {@link DirContextAdapter}
 *  and converts it based on configuration defined in {@link CustomAttribute} object.
 * 
 *  @author rodion.alukhanov
 */
class CustomAttributeMapper implements AttributeMapper {
	
	
	private final Logger logger = LoggerFactory.getLogger(CustomAttributeMapper.class);
	
	
	private final CustomAttribute attribute;
	

	/** Create a mapper for the specified attribute. */
	public CustomAttributeMapper(@Nonnull CustomAttribute attribute) {
		this.attribute = attribute;
	}
	
	
	/** Create a list of mappers for the specified attributes. */
	@Nonnull
	public static List<CustomAttributeMapper> createList(Collection<CustomAttribute> attrs) {
		
		List<CustomAttributeMapper> result = new ArrayList<CustomAttributeMapper>();
		
		for (CustomAttribute attr : attrs) {
			CustomAttributeMapper cam = new CustomAttributeMapper(attr);
			result.add(cam);
		}
		
		return result;
	}

	
	@Override
	public String getKey() {
		return attribute.getKey();
	}
	

	@Override
	public Set<String> getValues(DirContextAdapter ctx) {
		
		Set<String> result = new LinkedHashSet<String>();
		
		AttributeType type = attribute.getType();
		
		String ldapName = attribute.getLdapName();
		
		Object[] attrs = ctx.getObjectAttributes(ldapName);
		
		if (attrs != null) { // bit unusual, but possible.
			for (Object attr : attrs) {
				if (attr == null) {
					continue;
				}
				
				switch (type) {
					case BINARY:
						readAsBinary(ctx, attr, result);
						break;
					case STRING:
						String attrStr = attr.toString();
						result.add(attrStr);
						break;
					case INTEGER:
						readAsInteger(ctx, attr, result);
						break;
					case DOUBLE:
						readAsDouble(ctx, attr, result);
						break;
					default:
						// unreachable
						logger.error("Unsupported custom attribute type [" + type + "] for attribute key [" + attribute.getKey() + "].");
				}
			}
		}

		return result;
	}
	
	
	private void readAsBinary(DirContextAdapter ctx, Object attr, Set<String> result) {
		String ldapName = attribute.getLdapName();
		
		if (attr instanceof byte[]) {
			String attrStr = Base64.encodeBase64String((byte[])attr);
			result.add(attrStr);	
		} else {
			logger.error(
					"Error reading a custom attribute [" + ldapName + "] = [" + attrValue(attr) + "] from the LDAP context [" + ctx.getDn() + "]. " +
					"Unable to cast from type " + attr.getClass() + " to byte[].");
		}
	}
	
	
	private void readAsInteger(DirContextAdapter ctx, Object attr, Set<String> result) {
		String ldapName = attribute.getLdapName();
		
		if (attr instanceof Number) {
			String attrStr = ((Number)attr).intValue() + "";
			result.add(attrStr);
		} else {
			try {
				String attrStr = new Integer(attr.toString()) + "";
				result.add(attrStr);
			} catch (NumberFormatException e) {
				logger.error(
						"Error reading a custom attribute [" + ldapName + "] = [" + attrValue(attr) + "] from the LDAP context [" + ctx.getDn() + "]. " +
						"Unable to cast from type " + attr.getClass() + " to Number.");
			}
		}
	}
	
	
	private void readAsDouble(DirContextAdapter ctx, Object attr, Set<String> result) {
		String ldapName = attribute.getLdapName();
		
		if (attr instanceof Number) {
			String attrStr = ((Number)attr).doubleValue() + "";
			result.add(attrStr);
		} else {
			try {
				String attrStr = new Double(attr.toString()) + "";
				result.add(attrStr);
			} catch (NumberFormatException e) {
				logger.error(
						"Error reading a custom attribute [" + ldapName + "] = [" + attrValue(attr) + "] from the LDAP context [" + ctx.getDn() + "]. " +
						"Unable to cast from type " + attr.getClass() + " to Double.");
			}
		}
	}
	
	
	
	private final static int MAX_ATTR_LENGTH_FOR_LOG = 256;
	
	
	@Nullable
	private static CharSequence attrValue(Object obj) {
		if (obj == null) {
			return null;
		}
		String raw = obj.toString();
		if (raw == null) { // who knows, what for objects are returned from LDAP.
			return null;
		}
		
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < raw.length(); i++) {
			if (i > MAX_ATTR_LENGTH_FOR_LOG) {
				result.append("...");
				break;
			} else {
				char c = raw.charAt(i);
				if (c >= 32) {
					result.append(c);		
				} else {
					result.append("?");
				}
			}
		}
		
		return result;
	}
	
	
	/** Returns the first element with the specified key found in the collection.
	 *  
	 *  @param key
	 *  	A key to search for. Must be not <code>null</code>.
	 *  @param mappers
	 *  	A collection to search it.
	 *  @return
	 * 		<code>null</code>, if not found.
	 */
	@Nullable
	public static <T extends AttributeMapper> T findByKey(@Nonnull String key, Collection<T> mappers) {
		if (mappers == null) {
			return null;
		}
		for (T mapper : mappers) {
			if (key.equals(mapper.getKey())) {
				return mapper;
			}
		}
		return null;
	}


	@Nonnull
	public static List<String> unwrapKeys(@Nonnull Collection<? extends AttributeMapper> mappers) {
		List<String> result = new ArrayList<String>(mappers.size());
		for (AttributeMapper mapper : mappers) {
			result.add(mapper.getKey());	
		}
		return result;
	}


	@Override
	public String toString() {
		return "Mapper{" + attribute + "}";
	}
	
}