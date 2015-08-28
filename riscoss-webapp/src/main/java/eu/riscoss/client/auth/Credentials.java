package eu.riscoss.client.auth;

import java.io.Serializable;

import com.google.gwt.user.client.rpc.IsSerializable;

public class Credentials implements Serializable, IsSerializable
{
	private static final long	serialVersionUID	= -2634261966288443788L;
	
	protected String			userId				= "";
	protected String			password			= "";
	
	public Credentials()
	{
	}
	
	public Credentials( String userId, String password )
	{
		this.setUserId( userId );
		this.setPassword( password );
	}
	
	protected Credentials( Credentials cred )
	{
		this.userId = cred.userId;
		this.password = cred.password;
	}
	
	/**
	 * @param userId
	 *            the userId to set
	 */
	private void setUserId( String userId )
	{
		this.userId = userId;
	}
	
	/**
	 * @return the userId
	 */
	public String getUserId()
	{
		return userId;
	}
	
	/**
	 * @param password
	 *            the password to set
	 */
	private void setPassword( String password )
	{
		this.password = password;
	}
	
	public boolean isAnonymous()
	{
		return userId.compareTo( "" ) == 0;
	}

	public String getPassword()
	{
		return password;
	}
}
