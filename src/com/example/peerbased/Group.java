package com.example.peerbased;

import java.io.Serializable;
import java.util.ArrayList;

public class Group implements Serializable{
	public static final long serialVersionUID = 112231L;
	public String groupName;
	public String leaderName;
	public String leaderID;
	public Student leaderRecord;
	public ArrayList<Student> teamMembers;
	
	public Group(String gname, String lname, String lid, Student leaderRecord) {
		groupName = gname;
		leaderName = lname;
		leaderID = lid;
		teamMembers = new ArrayList<>();
		this.leaderRecord = leaderRecord;
	}
}