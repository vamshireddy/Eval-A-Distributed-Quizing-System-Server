package com.example.peerbased;
import java.io.Serializable;


public class AuthPacket implements Serializable{
	
	static final long serialVersionUID = 1234L;
	boolean changePass; // This flag is set when the 'password change' request is sent by the student
	boolean grantAccess; // This flag is set by the teacher, when the credentials are correct
	String userID;	// UserName entered by the student on the tablet
	String password;	// password entered by the student on the tablet
	String studentName;  
	byte errorCode;	// This will be used only when the grant access flag is false
	
	public AuthPacket()
	{
		userID = "";
		studentName = "";
		password = "";
		changePass = false;
		changePass = false;
		errorCode = -1;
	}
	public AuthPacket(boolean changePass, boolean grantAccess)
	{
		this();
		this.changePass = changePass;
		this.grantAccess = grantAccess;
	}
	public AuthPacket(boolean changePass, boolean grantAccess, byte error)
	{
		this(changePass,grantAccess);
		errorCode = error;
	}
}
