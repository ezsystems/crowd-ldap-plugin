package no.ez.crowd.customattributes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.naming.directory.SearchControls;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import com.google.common.base.Objects;


/** Configuration for a single attribute.<br><br>
 * 
 *  An LDAP attribute {@link #ldapName} must be converted to 
 *  the Crowd custom attribute {@link #key}.
 * 
 *  @author rodion.alukhanov
 */
@XmlType(name="attribute")
@XmlAccessorType(XmlAccessType.FIELD)
public class CustomAttribute {

	@XmlAttribute(name="key", required=true)
	private String key;
	
	
	@XmlAttribute(name="type", required=true)
	private AttributeType type;
	
	
	/** @see #isOperational() */
	@XmlAttribute(name="operational", required=false)
	private boolean operational = false;
	

	@XmlElement(name="ldap-name")
	private String ldapName;

	
	@XmlElement(name="format")
	@CheckForNull
	private List<Format> formats; 
	
	
	/** Do not delete. JAXB needs it! */
	protected CustomAttribute() {
		// nothing
	}
	
	
	/** This class should by created by JAXB. This constructor is for testing.
	 */
	public CustomAttribute(String key, AttributeType type, String ldapName) {
		this.key = key;
		this.type = type;
		this.ldapName = ldapName;
	}
	
	
	/** This class should by created by JAXB. This constructor is for testing.
	 */
	public CustomAttribute(String key, AttributeType type, String ldapName, List<Format> formats) {
		this.key = key;
		this.type = type;
		this.ldapName = ldapName;
		this.formats = formats;
	}


	public String getKey() {
		return key;
	}

	
	@Nonnull
	public List<Format> getFormats() {
		List<Format> result = formats;
		if (result == null) {
			result = new ArrayList<Format>();
		}
		return result;
	}
	
	
	public AttributeType getType() {
		return type;
	}

	
	public String getLdapName() {
		return ldapName;
	}
	
	
	/** Returns <code>true</code> for operational attributes. Special attribute type
	 *  introduced in LDAP3, which are not an attribute of any class. Such an attributes
	 *  are usually not returned by default and must be requested explicitly using 
	 *  {@link SearchControls}.
	 */
	public boolean isOperational() {
		return operational;
	}


	@Override
	public int hashCode() {
		return Objects.hashCode(key);
	}


	@Override
	public String toString() {
		return key + "<-" + ldapName;
	}


	@Override
	public boolean equals(Object obj) {
		if (obj instanceof CustomAttribute) {
			CustomAttribute oo = (CustomAttribute)obj;
			return Objects.equal(key, oo.key);
		} else {
			return false;
		}
	}
	
	
	/** Unwraps values of the field {@link CustomAttribute#key} from all the specified
	 *  attribute objects. 
	 *  
	 *  @see #unwrapLdapNames(Set)
	 *  */
	@Nonnull
	public static List<String> unwrapKeys(@Nonnull Collection<? extends CustomAttribute> attrs) {
		List<String> result = new ArrayList<String>(attrs.size());
		for (CustomAttribute attr : attrs) {
			result.add(attr.getKey());	
		}
		return result;
	}
	
	
	/** Unwraps values of the field {@link CustomAttribute#ldapName} from all the specified
	 *  attribute objects. 
	 *  
	 *  @see #unwrapKeys(Collection)
	 *  */
	public static List<String> unwrapLdapNames(Collection<CustomAttribute> attrs) {
		List<String> result = new ArrayList<String>(attrs.size());
		for (CustomAttribute attr : attrs) {
			result.add(attr.getLdapName());	
		}
		return result;
	}
	
	
	/** Returns the first element with the specified key found in the collection.
	 *  
	 *  @param key
	 *  	A key to search for. Must be not <code>null</code>.
	 *  @param attrs
	 *  	A collection to search it.
	 *  @return
	 * 		<code>null</code>, if not found.
	 */
	@Nullable
	public static CustomAttribute findByKey(@Nonnull String key, Collection<CustomAttribute> attrs) {
		if (attrs == null) {
			return null;
		}
		for (CustomAttribute attr : attrs) {
			if (key.equals(attr.getKey())) {
				return attr;
			}
		}
		return null;
	}

	
	/** Returns a new created list with only operational attributes.
	 *  
	 *  @return
	 *  	never <code>null</code>.
	 */
	@Nonnull
	public static List<CustomAttribute> filterOperationalOnly(@Nonnull Collection<CustomAttribute> attrs) {
		List<CustomAttribute> result = new ArrayList<CustomAttribute>();
		for (CustomAttribute attr : attrs) {
			if (attr.isOperational()) {
				result.add(attr);
			}
		}
		return result;
	}

	
}
