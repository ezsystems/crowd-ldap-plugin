package no.ez.crowd.customattributes;

import java.util.Collection;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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
	

	@XmlElement(name="ldap-name")
	private String ldapName;
	
	
	/** Do not delete. JAXB needs it! */
	protected CustomAttribute() {
		// nothing
	}
	
	
	/** This class should by created by JAXB. This constructor is for testing.
	 **/
	public CustomAttribute(String key, AttributeType type, String ldapName) {
		this.key = key;
		this.type = type;
		this.ldapName = ldapName;
	}


	public String getKey() {
		return key;
	}

	
	public AttributeType getType() {
		return type;
	}

	
	public String getLdapName() {
		return ldapName;
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
	
	
}
