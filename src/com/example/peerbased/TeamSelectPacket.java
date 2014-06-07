package com.example.peerbased;

import java.io.Serializable;

public class TeamSelectPacket implements Serializable {
	public static final long serialVersionUID = 121242L;
	
	public String leaderID;
	public String name;
	public String ID;
	public boolean accepted;
	
	public TeamSelectPacket(String leaderId, String studentName, String studentID)
	{
		leaderID = leaderId;
		name = studentName;
		ID = studentID;
		accepted = false;
	}	
}
