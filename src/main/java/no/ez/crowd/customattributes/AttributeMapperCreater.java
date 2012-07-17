package no.ez.crowd.customattributes;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.crowd.directory.ldap.mapper.attribute.AttributeMapper;



/** Creates a {@link AttributeMapper} instances for custom attributes.
 * 
 *  Reads the Crowd init configuration property file [crowd-init.properties] to 
 *  find the Crowd home directory and the custom attribute configuration file 
 *  [ldap.custom-attributes.xml]. 
 *   
 *  @author rodion.alukhanov
 */
class AttributeMapperCreater {
	
	private final Logger logger = LoggerFactory.getLogger(AttributeMapperCreater.class);
	
	
	private static final String CROWD_INIT_PROPERTIES = "/crowd-init.properties";
	
	private static final String CROWD_HOME_PROPERTY = "crowd.home";
	
	private static final String PROPERTIES_FILE_NAME_DEFAULT = "ldap.custom-attributes.xml";
	
	
	private String propertiesFileName = PROPERTIES_FILE_NAME_DEFAULT;
	
	
	private ConfigurationReader configuration;
	
	
	/** Creates a mapper using the specified Crowd init configuration 
	 * 
	 *  @param
	 *  	the property file must contain the property {@value #CROWD_HOME_PROPERTY}
	 *  	which refers to the Crowd home directory, where the custom attribute
	 *   	configuration file is located.
	 */
	public AttributeMapperCreater(Reader crowdInitProperties) throws IOException {
		init(crowdInitProperties);
	}
	
	
	/** Like {@link #AttributeMapperCreater(Reader)}, but overrides the property filename.
	 *  Used for testing. 
	 */
	protected AttributeMapperCreater(Reader crowdInitProperties, String propertiesFileName) throws IOException {
		this.propertiesFileName = propertiesFileName;
		init(crowdInitProperties);
	}
	

	/** Creates a mapper reading the Crowd init configuration 
	 *  from classpath resource {@value #CROWD_INIT_PROPERTIES}.
	 */
	public AttributeMapperCreater() {
		try {	
			
			Charset propEncoding = Charset.forName("ISO-8859-1");
			
			InputStream crowdInit = getClass().getResourceAsStream(CROWD_INIT_PROPERTIES);
			
			if (crowdInit == null) {
				throw new RuntimeException("Crowd initialization property file [" + CROWD_INIT_PROPERTIES + "] is not found in the classpath.");
			}
			
			Reader crowdInitReader = new InputStreamReader(crowdInit, propEncoding);
			
			init(crowdInitReader);
		} catch (IOException e) {
			throw new RuntimeException(
					"Error accessing Crown home directory, specified in Crowd initialization property file [" + CROWD_INIT_PROPERTIES + "].", e);
		}
	}
	
	
	private void init(@Nonnull Reader crowdInit) throws IOException {
		
		if (crowdInit == null) {
			throw new IllegalArgumentException("Argument 'crowdInit' must be not null.");
		}
		
		Properties prop = new Properties();
	
		prop.load(crowdInit);
		
		String crowdHomeStr = (String)prop.get(CROWD_HOME_PROPERTY);
		
		if (crowdHomeStr == null) {
			throw new IOException(
					"Crowd home directory property [" + CROWD_HOME_PROPERTY + "] was not defined " +
					"in the initialization property file.");
		}
		
		logger.info("Crowd home directory found [" + crowdHomeStr + "].");
		
		File crowdHome = new File(crowdHomeStr);
		
		if (! crowdHome.isDirectory()) {
			throw new IOException(
					"Crowd home directory [" + crowdHome + "] specified in the initialization property file " +
					"doesn't exist or not a directory.");
		}
		
		File source = new File(crowdHome, propertiesFileName);
		
		logger.info("Custom attribute configuration file was set to [" + source + "]");
		
		configuration = new ConfigurationReader(source);
		configuration.loadConfig(); // <-- tries to load configuration.
		
		logger.info("Custom attribute configuration file parsed successfully.");
	}
	

	@Nonnull
    public List<? extends AttributeMapper> createUserAttributeMappers(long directoryId, @Nullable String directoryUrl) {
		
		Collection<CustomAttribute> attrs = configuration.readUserAttributes(directoryId, directoryUrl);
		
		List<? extends AttributeMapper> result = CustomAttributeMapper.createList(attrs);
		return result;
	}
    
    
	@Nonnull
    public List<? extends AttributeMapper> createGroupAttributeMappers(long directoryId, @Nullable String directoryUrl) {
		
		Collection<CustomAttribute> attrs = configuration.readGroupAttributes(directoryId, directoryUrl);
		
		List<? extends AttributeMapper> result = CustomAttributeMapper.createList(attrs);
		return result;
	}
    
    

}










