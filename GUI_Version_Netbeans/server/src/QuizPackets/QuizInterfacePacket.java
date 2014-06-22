package QuizPackets;

import java.io.Serializable;

public class QuizInterfacePacket implements Serializable{
	public static final long serialVersionUID = 1191L;
	public String activeGroupName;
	public String activeGroupLeaderID;
	
	public QuizInterfacePacket(String grpName, String activeGroupID)
	{
		activeGroupName = grpName;
		activeGroupLeaderID = activeGroupID;
	}
}
