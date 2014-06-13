package QuizPackets;

import java.io.Serializable;

public class QuizInterfacePacket implements Serializable{
	
	public String activeGroupName;
	public String activeGroupLeaderID;
	
	public QuizInterfacePacket(String grpName, String activeGroupID)
	{
		activeGroupName = grpName;
		activeGroupLeaderID = activeGroupID;
	}
}
