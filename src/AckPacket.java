import java.io.Serializable;


public class AckPacket implements Serializable{
	static final long serialVersionUID = 22L;
	int seq_no;
	public AckPacket(int no)
	{
		seq_no = no;
	}
}
