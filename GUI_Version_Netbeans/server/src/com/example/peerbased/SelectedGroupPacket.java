package com.example.peerbased;

import java.io.Serializable;
import java.util.ArrayList;

public class SelectedGroupPacket implements Serializable{
	
	static final long serialVersionUID = 124132L;
	String groupName;
	Student leader;
	ArrayList<Student> team;
	public SelectedGroupPacket(String groupName, Student l, ArrayList<Student> team)
	{
		this.groupName = groupName;
		this.leader = l;
		this.team = team;
	}
}
