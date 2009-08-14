package Sirius.server.newuser;

public class UserException extends Exception implements java.io.Serializable //java.rmi.ServerException
{
	private boolean wrongUserName = false;
	private boolean wrongPassword = false;
	private boolean wrongUserGroup = false;
	private boolean wrongLocalServer = false;
	

	public UserException(String detailMessage)
	{
		super(detailMessage);
	}

	public UserException(String detailMessage, boolean wrongUserName, boolean wrongPassword, boolean wrongUserGroup, boolean wrongLocalServer)
	{
		super(detailMessage);
		this.wrongUserName = wrongUserName;
		this.wrongPassword = wrongPassword;
		this.wrongUserGroup = wrongUserGroup;
		this.wrongLocalServer = wrongLocalServer;
	}

	public boolean wrongUserName() {return wrongUserName;}

	public boolean wrongPassword() {return wrongPassword;}
	
	public boolean wrongUserGroup() {return wrongUserGroup;}
	
	public boolean wrongLocalServer() {return wrongLocalServer;}
}
