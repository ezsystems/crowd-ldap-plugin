package no.ez.crowd.customattributes;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.naming.directory.SearchControls;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.crowd.directory.ldap.LDAPPropertiesMapperImpl;
import com.atlassian.crowd.directory.ldap.mapper.attribute.AttributeMapper;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;



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
			try {
				init(crowdInitReader);
			} finally {
				crowdInitReader.close();
			}
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
    
	
	/** Returns the LDAP names for all the attributes (mixed user and group attributes).
	 * 
	 *  @return
	 *  	never <code>null</code>, but can be empty.
	 */
	@Nonnull
	protected Set<String> getOperationalAttributeLdapNames(long directoryId, @Nullable String directoryUrl) {
		
		Collection<CustomAttribute> userAttrs  = configuration.readUserAttributes(directoryId, directoryUrl);
		Collection<CustomAttribute> groupAttrs = configuration.readGroupAttributes(directoryId, directoryUrl);
		
		Set<String> result = new HashSet<String>();
		result.addAll(CustomAttribute.unwrapLdapNames(CustomAttribute.filterOperationalOnly(userAttrs)));
		result.addAll(CustomAttribute.unwrapLdapNames(CustomAttribute.filterOperationalOnly(groupAttrs)));
		
		return result;
	}
	
	
	/** Puts all the operational attribute LDAP names to the specified {@link SearchControls}
	 *  objects. If no attributes specified, appends the "*" to get all the attributes and
	 *  operational attributes.
	 */
	public void putOperationalAttribute(@Nonnull SearchControls result, long directoryId, @Nullable String directoryUrl) {
    	
    	Set<String> ops = getOperationalAttributeLdapNames(directoryId, directoryUrl);
    	
    	if ( ! ops.isEmpty()) {
    		String[] attrs = result.getReturningAttributes();
    		List<String> newAttrs = new ArrayList<String>(); 
    		if (attrs == null) {
    			newAttrs.add("*");
    		} else {
				newAttrs.addAll(Arrays.asList(attrs));
    		}
    		newAttrs.addAll(ops);
    		result.setReturningAttributes(newAttrs.toArray(new String[newAttrs.size()]));
    	}
	}
	
    
	@Nonnull
    public List<? extends AttributeMapper> createGroupAttributeMappers(long directoryId, @Nullable String directoryUrl) {
		
		Collection<CustomAttribute> attrs = configuration.readGroupAttributes(directoryId, directoryUrl);
		
		List<? extends AttributeMapper> result = CustomAttributeMapper.createList(attrs);
		return result;
	}
	
	
	/** Adds all the binary attribute names to the property 
	 *  {@value LDAPPropertiesMapperImpl#CONNECTION_BINARY_ATTRIBUTES}
	 *  as defined in {@linkplain "http://docs.oracle.com/javase/jndi/tutorial/ldap/misc/attrs.html"}  
	 * 
	 *  @param properties
	 *  	environment properties. Only the specified property will be altered (and added, if needed).
	 *  	It must be not <code>null</code>.
	 */
	public void putBinaryAttributes(@Nonnull Map<String, String> env, long directoryId, @Nullable String directoryUrl){
    	
    	String binProperty = env.get(LDAPPropertiesMapperImpl.CONNECTION_BINARY_ATTRIBUTES);
    	
    	List<String> binaryAttributes;
    	
    	if (binProperty != null && binProperty.length() != 0) {
    		binaryAttributes = Lists.newArrayList(binProperty.split(" "));
    	} else {
    		binaryAttributes = new ArrayList<String>();
    	}
    	
    	Set<CustomAttribute> binAttrs = configuration.readBinaryAttributes(directoryId, directoryUrl);
    	
    	List<String> keys = CustomAttribute.unwrapLdapNames(binAttrs);
    	binaryAttributes.addAll(keys);
    	
    	binProperty = Joiner.on(' ').join(binaryAttributes);
    	
    	env.put(LDAPPropertiesMapperImpl.CONNECTION_BINARY_ATTRIBUTES, binProperty);
    }
    
}










