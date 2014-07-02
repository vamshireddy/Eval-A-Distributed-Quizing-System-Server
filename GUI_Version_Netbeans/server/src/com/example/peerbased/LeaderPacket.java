package com.example.peerbased;

import java.io.Serializable;
import java.util.ArrayList;

public class LeaderPacket implements Serializable {
	public static final long serialVersionUID = 1242L;
	/*
	 * uID, uName will be used by the students when they want to become a leaders
	 * These are also used by the students ( non-leaders ) for their selection towards a leader.
	 * Leader name will be included with the ID when a student selects a leader
	 */
	public String uID;
	public String uName;
	public boolean granted;
	
	public LeaderPacket() {
		granted = false;
		uID = null;
                uName = null;
	}
	public LeaderPacket(boolean t)
	{
		granted = t;
	}
}
