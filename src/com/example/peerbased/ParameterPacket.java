package com.example.peerbased;
import java.io.Serializable;

// This is the format of the packet to be sent after the entire connection process is completed
public class ParameterPacket implements Serializable {
	static final long serialVersionUID = 422L;
	private byte noOfOnlineStudents;
	private byte noOfLeaders;
	private byte sizeOfGroup;
	private byte noOfRounds;
	private String subject;
	public ParameterPacket(byte no_studs, byte no_grps, byte size_grp, byte noOfRounds, String subject) {
		noOfOnlineStudents = no_studs;
		noOfLeaders = no_grps;
		sizeOfGroup = size_grp;
		this.subject = subject;
		this.noOfRounds = noOfRounds;
	}
}
