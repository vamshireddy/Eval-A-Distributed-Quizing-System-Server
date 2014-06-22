package QuizPackets;
import java.io.Serializable;
import java.sql.Date;

public class Question implements Serializable {
	public static final long serialVersionUID = 19L;
	public String question;
	public String answer;
	public Date date;
	public String subject;
	public int level;
	public String standard;
	public Question(String ques, String ans, String sub, int level, String standard )
	{
		question = ques;
		answer = ans;
		date = new Date(0);
		subject = sub;
		this.level = level;
		this.standard = standard;
	}
}
