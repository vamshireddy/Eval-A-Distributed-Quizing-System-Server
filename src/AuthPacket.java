import java.io.Serializable;


public class AuthPacket implements Serializable{
	
	static final long serialVersionUID = 1234L;
	boolean changePass; // This flag is set when the 'password change' request is sent by the student
	boolean grantAccess; // This flag is set by the teacher, when the credentials are correct
	String userName;	// UserName entered by the student on the tablet
	String password;	// password entered by the student on the tablet
	
	public AuthPacket()
	{
		userName = "";
		password = "";
		changePass = false;
		changePass = false;
	}
	public AuthPacket(boolean changePass, boolean grantAccess)
	{
		this();
		this.changePass = changePass;
		this.grantAccess = grantAccess;
	}
}
