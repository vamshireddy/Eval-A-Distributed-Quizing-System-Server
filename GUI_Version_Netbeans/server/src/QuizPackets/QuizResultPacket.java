package QuizPackets;

import java.io.Serializable;

public class QuizResultPacket implements Serializable{
	
	public static final long serialVersionUID = 1912431L;
	public int noOfQuesAttempted;
	public int noOfQuesCorrect;
	public int marks;
	public QuizResultPacket(int q,int a, int m)
	{
		noOfQuesCorrect = q;
		noOfQuesAttempted = a;
		marks = m;
	}
}
