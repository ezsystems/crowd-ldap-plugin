package no.ez.crowd.customattributes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlType;



/** A directory-tag of the configuration file.
 * 
 *  @author rodion.alukhanov
 */
@XmlType(name="directory")
@XmlAccessorType(XmlAccessType.FIELD)
public class Directory {
	
	
	@XmlAttribute(name="name", required=true)
	private String name;

	@XmlElement(name="server")
	@CheckForNull
	private List<String> servers;

	
	@XmlElementWrapper(name="user")
	@XmlElement(name="attribute")
	@CheckForNull
	private List<CustomAttribute> userAttributes;
	
	
	@XmlElementWrapper(name="group")
	@XmlElement(name="attribute")
	@CheckForNull
	private List<CustomAttribute> groupAttributes;


	public String getName() {
		return name;
	}
	
	
	@Nonnull
	public List<String> getServers() {
		List<String> result = servers;
		
		if (result == null) {
			return new ArrayList<String>(); 
		}
		
		return result;
	}

	
	@Nonnull
	public Collection<CustomAttribute> getUserAttributes() {
		
		List<CustomAttribute> result = userAttributes;
		
		if (result == null) {
			return new ArrayList<CustomAttribute>(); 
		}
		
		return Collections.unmodifiableList(result);
	}
	

	@Nonnull
	public Collection<CustomAttribute> getGroupAttributes() {
		List<CustomAttribute> result = groupAttributes;
		
		if (result == null) {
			return new ArrayList<CustomAttribute>(); 
		}
		
		return Collections.unmodifiableList(result);
	}
	

	
	/** Returns <code>true</code>, if the specified directory ID or URL is
	 *  one of the server defined in this directory.
	 */
	public boolean isServer(long directoryId, @Nullable String directoryUrl) {
		if (servers == null) {
			return false;
		}
		for (String server : servers) {
			if (server.equalsIgnoreCase(directoryId + "") || equalUrls(server, directoryUrl)) {
				return true;
			}
		}
		return false;
	}
	
	
	private static boolean equalUrls(String a, String b) {
		a = removeLastSlash(a);
		b = removeLastSlash(b);
		
		return a.equalsIgnoreCase(b);
	}
	
	
	private static String removeLastSlash(String a) {
		if (a == null || a.length() == 0) {
			return "";
		}
		if (a.charAt(a.length() - 1) == '/') {
			a = a.substring(0, a.length() - 1);
		}
		return a;
	}
	
	
	@Override
	public String toString() {
		return getName() + ":" + getServers();
	}
}



