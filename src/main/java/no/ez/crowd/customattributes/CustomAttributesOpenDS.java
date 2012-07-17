package no.ez.crowd.customattributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.crowd.directory.OpenDS;
import com.atlassian.crowd.directory.ldap.LDAPPropertiesMapperImpl;
import com.atlassian.crowd.directory.ldap.mapper.attribute.AttributeMapper;
import com.atlassian.crowd.password.factory.PasswordEncoderFactory;
import com.atlassian.crowd.search.ldap.LDAPQueryTranslater;
import com.atlassian.crowd.util.InstanceFactory;
import com.atlassian.event.api.EventPublisher;


/** A extension of {@link OpenDS} LDAP connector which supports custom attributes.<br><br>
 * 
 *  The name of this class must be set in the database. The configuration file must
 *  be created in the Crowd home directory based on <code>ldap.custom-attributes.dtd</code>
 *  which must be found in the root of class path (usually just in the jar file).  
 * 
 *  @author rodion.alukhanov
 */
public class CustomAttributesOpenDS extends OpenDS {
	
	
	private final Logger logger = LoggerFactory.getLogger(CustomAttributesOpenDS.class);
	

	private final AttributeMapperCreater customAttributesMapperCreator;
	

	public CustomAttributesOpenDS(
			LDAPQueryTranslater ldapQueryTranslater, 
			EventPublisher eventPublisher, 
			InstanceFactory instanceFactory, 
			PasswordEncoderFactory passwordEncoderFactory) {
		
		super(ldapQueryTranslater, eventPublisher, instanceFactory, passwordEncoderFactory);
		
		customAttributesMapperCreator = new AttributeMapperCreater();
	}
	
	
    public static String getStaticDirectoryType() {
        return "OpenDS with custom attributes support.";
    }

    
    @Override
	public String getDescriptiveName() {
        return CustomAttributesMicrosoftActiveDirectory.getStaticDirectoryType();
    }
    
    
    @Override
	protected Map<String, String> getBaseEnvironmentProperties(){
    	Map<String, String> result = ldapPropertiesMapper.getEnvironment();
    	
    	if (result == null) { // who knows
    		result = new HashMap<String, String>();
    	}
    	
    	String binAttributes = result.get(LDAPPropertiesMapperImpl.CONNECTION_BINARY_ATTRIBUTES);
    }
	
    
    @Override
	protected List<AttributeMapper> getCustomUserAttributeMappers() {
		
		List<AttributeMapper> result = super.getCustomUserAttributeMappers();
		
		long directoryId = getDirectoryId();
		String directoryUrl = ldapPropertiesMapper.getConnectionURL();
		
		List<? extends AttributeMapper> customMappers = customAttributesMapperCreator.createUserAttributeMappers(directoryId, directoryUrl);
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
		
		List<AttributeMapper> result = super.getCustomGroupAttributeMappers();
		
		long directoryId = getDirectoryId();
		String directoryUrl = ldapPropertiesMapper.getConnectionURL();
		
		List<? extends AttributeMapper> customMappers = customAttributesMapperCreator.createGroupAttributeMappers(directoryId, directoryUrl);
		result.addAll(customMappers);
		
		if (logger.isDebugEnabled()) {
			logger.debug(
					"Finished creating group custom attribute mappers. " + customMappers.size() + " mappers created " +
					"for following attributes: " + CustomAttributeMapper.unwrapKeys(customMappers));
		}
	
		return result;
	}
	
}

