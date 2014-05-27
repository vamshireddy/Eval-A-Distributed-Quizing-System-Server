import java.awt.List;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.sql.Date;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.Calendar;


public class Quiz {
	private static int port = Utilities.recvPort;
	private int noOfStudents;
	private int noOfGroups;
	private int noOfStudentsInGroup;
	private String subject;
	private String teacherName;
	private Date date;
	private String timeStamp;
	private Student[] students;			// for all the students participating
	private List[] groups;				// for all the groups for the session
	private DatagramSocket sendSocket;  // Socket used for sending, which has ephemeral port number
	private DatagramSocket recvSocket;  // Socket used for receiving 
	private int currentSeqNo;
	private ByteArrayInputStream bais;
	private ObjectInputStream ois;
	private ByteArrayOutputStream baos;
	private ObjectOutputStream oos;
	
	public Quiz(int noOfStudents,int noOfgroups,int noOfStudentsInGroup,String subject,String teacherName,Date date)
	{
		this.noOfGroups = noOfgroups;
		this.noOfStudents = noOfStudents;
		this.noOfStudentsInGroup = noOfStudentsInGroup;
		this.teacherName = teacherName;
		this.date = date;
		timeStamp = new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime());
		try {
			
			sendSocket = new DatagramSocket();
			recvSocket = new DatagramSocket(port);
			
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}
	public void start()
	{
		// This method is called when the teacher starts the quiz session
		// Initial phase: Receive authentication packets from the students
		receiveAuthPackets();
	}
	
	public void receiveAuthPackets()
	{
		try 
		{
			// Allocate the buffer of MAX_BUFFER_SIZE, which is defined in the Utilities class
		    byte[] buffer = new byte[Utilities.MAX_BUFFER_SIZE];
		    DatagramPacket pack =  new DatagramPacket(buffer, buffer.length);
		    recvSocket.receive(pack);
		    
		    // Deserialize the Packet object and store in the object 'p'
		    bais = new ByteArrayInputStream(buffer);
		    ois = new ObjectInputStream(bais);
		    Packet data_packet = (Packet) ois.readObject();
		    
		    // Deserialize the data string to an appropriate object based on the flags present in the packet received
		    bais = new ByteArrayInputStream(data_packet.data);
	    	ois = new ObjectInputStream(bais);
	    	
	    	
		    if( data_packet.auth_packet == true )
		    {	
		    	AuthPacket auth_packet = (AuthPacket) ois.readObject();
		    	System.out.println("Auth packet it is!!  Username: "+auth_packet.userName+" Password : "+auth_packet.password);
		    }
			
		}
		catch (SocketException e)
		{
			e.printStackTrace();
		} 
		catch (IOException e)
		{
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void sendAck(int no)
	{
		AckPacket ack_pack = new AckPacket(no);
		
	    
	    try {
	    	baos = new ByteArrayOutputStream();
	    	oos = new ObjectOutputStream(baos);
			oos.writeObject(ack_pack);
			oos.flush();
			byte[] send_ack = baos.toByteArray();
			
			DatagramPacket pack = new DatagramPacket(send_ack, send_ack.length, InetAddress.getByName("localhost"),12345);
			sendSocket.send(pack);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	   
	    // get the byte array of the object
	    
	}
}
