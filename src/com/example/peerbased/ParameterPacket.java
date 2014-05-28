package com.example.peerbased;
import java.io.Serializable;
import java.net.InetAddress;
import java.util.ArrayList;

// This is the minimal view of the Student to the clients
class StudentMin implements Serializable
{
	static final long serialVersionUID = 412L;
	String name;
	InetAddress IP;
	public StudentMin(String name, InetAddress IP) {
		this.name = name;
		this.IP = IP;
	}
}

// This is the format of the packet to be sent after the entire connection process is completed
public class ParameterPacket implements Serializable {
	static final long serialVersionUID = 422L;
	private byte noOfOnlineStudents;
	private byte noOfLeaders;
	private byte sizeOfGroup;
	private ArrayList<StudentMin> onlineList;
	public ParameterPacket(byte no_studs, byte no_grps, byte size_grp, ArrayList<Student> s) {
		noOfOnlineStudents = no_studs;
		noOfLeaders = no_grps;
		sizeOfGroup = size_grp;
		onlineList = new ArrayList<StudentMin>();
		addStudents(s);
	}
	
	private void addStudents(ArrayList<Student> s) 
	{
		for(int i =0 ;i<s.size();i++ )
		{
			Student stud = s.get(i);
			onlineList.add(new StudentMin(stud.uname,stud.IP));
		}
	}
}
