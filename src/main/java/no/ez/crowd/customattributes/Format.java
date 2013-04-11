package no.ez.crowd.customattributes;

import javax.annotation.CheckForNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;



/** A format tag.
 * 
 *  @author rodion.alukhanov
 */
@XmlType(name="format")
@XmlAccessorType(XmlAccessType.FIELD)
public class Format {
	
	
	@XmlAttribute(name="pattern", required=true)
	private String pattern;
	
	
	@XmlAttribute(name="time-zone", required=false)
	private String timezone;
	
	
	/** For JAX. Do not delete! */
	protected Format() {
		// nothing
	}
	

	public Format(String pattern, String timezone) {
		this.pattern = pattern;
		this.timezone = timezone;
	}
	
	
	public Format(String pattern) {
		this.pattern = pattern;
	}


	public String getPattern() {
		return pattern;
	}


	@CheckForNull
	public String getTimezone() {
		return timezone;
	}

}
