package com.example.peerbased;

import java.io.Serializable;
import java.util.ArrayList;

public class LeaderPacket implements Serializable {
	public static final long serialVersionUID = 1242L;
	public String uID;
	public String uName;
	public boolean granted;
	public boolean grpNameRequest;
	public boolean selectedLeadersList;
	public ArrayList<Leader> leaders;
	public String groupName;
	
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
