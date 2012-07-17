package no.ez.crowd.customattributes;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlType;




@XmlType(name="directory")
@XmlAccessorType(XmlAccessType.FIELD)
public class Directory {
	
	
	@XmlAttribute(name="name", required=true)
	private String name;
	
	public String getName() {
		return name;
	}


	@XmlElement(name="server")
	private List<String> servers;

	@XmlElementWrapper(name="user")
	@XmlElement(name="attribute")
	private List<CustomAttribute> userAttributes;
	
	
	@XmlElementWrapper(name="group")
	@XmlElement(name="attribute")
	private List<CustomAttribute> groupAttributes;	

	
	public List<String> getServers() {
		return servers;
	}

	
	public Collection<CustomAttribute> getUserAttributes() {
		return Collections.unmodifiableList(userAttributes);
	}
	
	
	public Collection<CustomAttribute> getGroupAttributes() {
		return Collections.unmodifiableList(groupAttributes);
	}
	

	
	/** Returns <code>true</code>, if the specified directory ID or URL is
	 *  one of the server defined in this directory.
	 */
	public boolean isServer(long directoryId, @Nullable String directoryUrl) {
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



