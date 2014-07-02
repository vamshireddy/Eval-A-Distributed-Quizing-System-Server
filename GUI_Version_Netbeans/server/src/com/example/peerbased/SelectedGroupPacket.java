package com.example.peerbased;

import java.io.Serializable;
import java.util.ArrayList;

public class SelectedGroupPacket implements Serializable{
	
	static final long serialVersionUID = 124132L;
	byte groupAssigned;
	public SelectedGroupPacket(byte flag)
	{
		groupAssigned = flag;
	}
}