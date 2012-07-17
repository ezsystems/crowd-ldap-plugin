package no.ez.crowd.customattributes;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;




@XmlRootElement(name="custom-attributes")
@XmlAccessorType(XmlAccessType.FIELD)
public class Configuration {
	
	
	@XmlElement(name="directory")
	private List<Directory> directories;

	
	public Collection<Directory> getDirectories() {
		return Collections.unmodifiableList(directories);
	}
	
}
