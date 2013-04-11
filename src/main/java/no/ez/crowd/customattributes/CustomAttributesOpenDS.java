package no.ez.crowd.customattributes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.crowd.directory.OpenDS;
import com.atlassian.crowd.directory.ldap.mapper.attribute.AttributeMapper;
import com.atlassian.crowd.password.factory.PasswordEncoderFactory;
import com.atlassian.crowd.search.ldap.LDAPQueryTranslater;
import com.atlassian.crowd.util.InstanceFactory;
import com.atlassian.event.api.EventPublisher;


/** A extension of {@link OpenDS} LDAP connector which supports custom attributes.<br><br>
 * 
 *  The name of this class must be set in the database. The configuration file must
 *  be created in the Crowd home directory based on <code>ldap.custom-attributes.dtd</code>
 *  which must be found in the root of class path (usually just in the jar file).<br><br>
 *  
 *  DO NOT MOVE OR RENAME THIS CLASS. Crowd configuration references this class by class name. 
 * 
 *  @author rodion.alukhanov
 */
public class CustomAttributesOpenDS extends OpenDS {
	
	
	private final Logger logger = LoggerFactory.getLogger(CustomAttributesOpenDS.class);
	

	private final AttributeMapperCreater attributesMapperCreator;
	

	public CustomAttributesOpenDS(
			LDAPQueryTranslater ldapQueryTranslater, 
			EventPublisher eventPublisher, 
			InstanceFactory instanceFactory, 
			PasswordEncoderFactory passwordEncoderFactory) {
		
		super(ldapQueryTranslater, eventPublisher, instanceFactory, passwordEncoderFactory);
		
		attributesMapperCreator = new AttributeMapperCreater();
	}
	
	
    public static String getStaticDirectoryType() {
        return "OpenDS with custom attributes support.";
    }

    
    @Override
	public String getDescriptiveName() {
        return CustomAttributesOpenDS.getStaticDirectoryType();
    }
    
    
    @Override
	protected Map<String, String> getBaseEnvironmentProperties(){
    	Map<String, String> env = ldapPropertiesMapper.getEnvironment();
    	
    	if (env == null) { // who knows
    		logger.warn("No environment properties got from LdapPropertiesMapper [" + ldapPropertiesMapper + "]. Creating new property map.");
    		env = new HashMap<String, String>();
    	}
    	
    	long directoryId = getDirectoryId();
		String directoryUrl = ldapPropertiesMapper.getConnectionURL();
		
    	attributesMapperCreator.putBinaryAttributes(env, directoryId, directoryUrl);
    	
    	return env;
    }
    
    
//    @Override
//	protected SearchControls getSubTreeSearchControl() {
//    	SearchControls result = super.getSubTreeSearchControl();
//    	
//    	long directoryId = getDirectoryId();
//		String directoryUrl = ldapPropertiesMapper.getConnectionURL();
//
//    	attributesMapperCreator.putOperationalAttribute(result, directoryId, directoryUrl);
//
//    	return result;
//	}


	@Override
	protected List<AttributeMapper> getCustomUserAttributeMappers() {
		
		// creating a new list. In the Crowd version 2.6 the super implementation generates immutable lists. 
		List<AttributeMapper> result = new ArrayList<AttributeMapper>(super.getCustomUserAttributeMappers());
		
		long directoryId = getDirectoryId();
		String directoryUrl = ldapPropertiesMapper.getConnectionURL();
		
		List<? extends AttributeMapper> customMappers = attributesMapperCreator.createUserAttributeMappers(directoryId, directoryUrl);
		result.addAll(customMappers);
		
		if (logger.isDebugEnabled()) {
			logger.debug(
					"Finished creating user custom attribute mappers. " + customMappers.size() + " mappers created " +
					"for following attributes: " + CustomAttributeMapper.unwrapKeys(customMappers));
		}
	
		return result;
	}
	
    
    @Override
	protected List<AttributeMapper> getCustomGroupAttributeMappers() {
		
    	// creating a new list. In the Crowd version 2.6 the super implementation generates immutable lists.
		List<AttributeMapper> result = new ArrayList<AttributeMapper>(super.getCustomGroupAttributeMappers());
		
		long directoryId = getDirectoryId();
		String directoryUrl = ldapPropertiesMapper.getConnectionURL();
		
		List<? extends AttributeMapper> customMappers = attributesMapperCreator.createGroupAttributeMappers(directoryId, directoryUrl);
		result.addAll(customMappers);
		
		if (logger.isDebugEnabled()) {
			logger.debug(
					"Finished creating group custom attribute mappers. " + customMappers.size() + " mappers created " +
					"for following attributes: " + CustomAttributeMapper.unwrapKeys(customMappers));
		}
	
		return result;
	}
	
}

