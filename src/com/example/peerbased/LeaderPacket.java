package com.example.peerbased;

import java.io.Serializable;

public class LeaderPacket implements Serializable {
	static final long serialVersionUID = 1242L;
	String uID;
	boolean granted;
	public LeaderPacket() {
		granted = false;
	}
	public LeaderPacket(boolean t)
	{
		granted = t;
	}
}
