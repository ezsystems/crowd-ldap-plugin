package no.ez.crowd.customattributes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.codec.binary.Base64;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ldap.core.DirContextAdapter;

import com.atlassian.crowd.directory.ldap.mapper.attribute.AttributeMapper;
import com.google.common.collect.Lists;






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
	
	
	private List<Format> DEFAULT_DATE_FORMAT = Collections.unmodifiableList(Lists.newArrayList(
				new Format("yyyyMMddHHmmssZ", "UTC"),
				new Format("yyyyMMddHHmmss.SSSZ", "UTC")
			));
	
	
	private List<Format> DEFAULT_LOCAL_DATE_FORMAT = Collections.unmodifiableList(Lists.newArrayList(
			new Format("yyyyMMddHHmmss"),
			new Format("yyyyMMddHHmmss.SSS")
		));
	

	@Override
	public Set<String> getValues(DirContextAdapter ctx) {
		
		Set<String> result = new LinkedHashSet<String>();
		
		AttributeType type = attribute.getType();
		
		String ldapName = attribute.getLdapName();
		
		Object[] attrs = ctx.getObjectAttributes(ldapName);
		
		List<Format> formats = attribute.getFormats();

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
					case DATETIME:
						if (formats.isEmpty()) {
							formats = DEFAULT_DATE_FORMAT;
						}
						readAsDateTime(ctx, attr, result, formats);
						break;
					case LOCAL_DATETIME:
						if (formats.isEmpty()) {
							formats = DEFAULT_LOCAL_DATE_FORMAT;
						}
						readAsLocalDateTime(ctx, attr, result, formats);
						break;						
					default:
						// unreachable
						logger.error("Unsupported custom attribute type [" + type + "] for attribute key [" + attribute.getKey() + "].");
				}
			}
		}

		return result;
	}
	

	@Override
	public Set<String> getRequiredLdapAttributes() {
		Set<String> result = new HashSet<String>();
		result.add(attribute.getLdapName());
		return result;
	}
	
	
	private void readAsBinary(DirContextAdapter ctx, Object attr, Set<String> result) {
		String ldapName = attribute.getLdapName();
		
		if (attr instanceof byte[]) {
			String attrStr = Base64.encodeBase64String((byte[])attr);
			result.add(attrStr);	
		} else {
			logger.warn(
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
				logger.warn(
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
				logger.warn(
						"Error reading a custom attribute [" + ldapName + "] = [" + attrValue(attr) + "] from the LDAP context [" + ctx.getDn() + "]. " +
						"Unable to cast from type " + attr.getClass() + " to Double.");
			}
		}
	}
	
	
	protected void readAsDateTime(DirContextAdapter ctx, Object attr, Set<String> result, List<Format> formats) {
		String ldapName = attribute.getLdapName();
		
		String attrStr = attr.toString();

		Iterator<Format> it = formats.iterator();
		
		while (it.hasNext()) {
			
			Format format = it.next();
			
			try {
				DateTimeFormatter fmt = DateTimeFormat.forPattern(format.getPattern());
				
				DateTime r = fmt.parseDateTime(attrStr);
				
				String timeZone = format.getTimezone();
				
				DateTimeZone zone;
				if (timeZone != null) {
					zone = DateTimeZone.forID(timeZone);
				} else {
					zone = DateTimeZone.UTC;
				}
				r = r.withZone(zone);
				
				DateTimeFormatter output = ISODateTimeFormat.dateTime();
				
				result.add(output.print(r));
				
				return; // fine parsing successful			
				
			} catch (IllegalArgumentException e) {
				String msg = 
						"Error converting a custom attribute [" + ldapName + "] = [" + attrValue(attr) + "] from the LDAP context [" + ctx.getDn() + "]. " +
						"Unable to cast to the datetime using pattern [" + format.getPattern() +"]."; 
				
				if (it.hasNext()) {
					logger.debug(msg + " " + e.getMessage() + " Lets try the next format...");	
				} else {
					logger.warn(msg);
				}
			}
		}
	}
	
	
	protected void readAsLocalDateTime(DirContextAdapter ctx, Object attr, Set<String> result, List<Format> formats) {
		String ldapName = attribute.getLdapName();
		
		String attrStr = attr.toString();

		Iterator<Format> it = formats.iterator();
		
		while (it.hasNext()) {
			
			Format format = it.next();
			
			try {
				DateTimeFormatter fmt = DateTimeFormat.forPattern(format.getPattern());
				
				LocalDateTime r = fmt.parseLocalDateTime(attrStr);
				
				DateTimeFormatter output = ISODateTimeFormat.dateTime();
				
				result.add(output.print(r));
				
				return; // fine parsing successful			
				
			} catch (IllegalArgumentException e) {
				String msg = 
						"Error converting a custom attribute [" + ldapName + "] = [" + attrValue(attr) + "] from the LDAP context [" + ctx.getDn() + "]. " +
						"Unable to cast to the local datetime using pattern [" + format.getPattern() +"]."; 
				
				if (it.hasNext()) {
					logger.debug(msg + " " + e.getMessage() + " Lets try the next format...");
				} else {
					logger.warn(msg);
				}
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