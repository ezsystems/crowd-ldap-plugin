package no.ez.crowd.customattributes;



/** Custom attribute type. If some attribute read from LDAP cannot 
 *  be parsed according to the specified type, if will be skipped and
 *  warning will be printed in Crowd log.
 * 
 *  @see #isBinary()
 * 
 *  @author rodion.alukhanov
 */
public enum AttributeType {
	
	STRING(false), BINARY(true), INTEGER(false), DOUBLE(false), DATETIME(false), LOCAL_DATETIME(false);
	
	private final boolean binary;

	
	private AttributeType(boolean binary) {
		this.binary = binary;
	}


	/** If the attribute must be handeled by LDAP JNDI as String or byte array.
	 *  See {@linkplain "http://docs.oracle.com/javase/jndi/tutorial/ldap/misc/attrs.html"}
	 *  for more information.
	 **/
	public boolean isBinary() {
		return binary;
	}
	
}
