package com.example.peerbased;

import java.io.Serializable;
import java.util.ArrayList;

public class LeaderPacket implements Serializable {
	static final long serialVersionUID = 1242L;
	String uID;
	boolean granted;
	boolean grpNameRequest;
	boolean selectedLeadersList;
	ArrayList<String> leaders;
	String groupName;
	
	public LeaderPacket() {
		granted = false;
		grpNameRequest = false;
		selectedLeadersList = false;
		leaders = null;
		uID = "";
		groupName = "";
	}
	public LeaderPacket(boolean t)
	{
		granted = t;
	}
}
