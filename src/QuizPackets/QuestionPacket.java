package QuizPackets;

import java.io.Serializable;

public class QuestionPacket implements Serializable{
	public static final long serialVersionUID = 191L;
	public int questionSeqNo;
	public String groupName;
	public byte questionType;
	public String question;
	public String[] options;
	public String correctAnswerOption;
	public byte level;
	public boolean questionAuthenticated;
	public QuestionPacket(String groupName, byte type) {
		questionType = type;
		questionSeqNo = -1;
		groupName = "";
		question = "";
		if( questionType == 1 )
		{
			// 4 option question
			options = new String[4];
		}
		else if( questionType == 2 )
		{
			// true or false question
			options = new String[2];
			options[0] = "true";
			options[1] = "false";
		}
		else if( questionType == 3 )
		{
			// One word question
			options = null;
		}
		correctAnswerOption = "";
		level = 0;
		questionAuthenticated = false;
	}
}
