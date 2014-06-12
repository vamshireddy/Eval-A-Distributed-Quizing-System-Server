package QuizPackets;

public class QuizInterfacePacket {
	
	public String activeGroupName;
	public String activeGroupLeaderID;
	
	public QuizInterfacePacket(String grpName, String activeGroupID)
	{
		activeGroupName = grpName;
		activeGroupLeaderID = activeGroupID;
	}
}
