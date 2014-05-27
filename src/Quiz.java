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
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.net.*;

import com.mysql.jdbc.Connection;
import com.mysql.jdbc.PreparedStatement;

public class Quiz {
	private static int port = Utilities.recvPort;
	private int noOfStudents;
	private int noOfGroups;
	private int noOfStudentsInGroup;
	private String subject;
	private String teacherName;
	private Date date;
	private String timeStamp;
	private ArrayList<Student> studentsList;		// for all the students participating
	private List[] groups;				// for all the groups for the session
	private DatagramSocket sendSocket;  // Socket used for sending, which has ephemeral port number
	private DatagramSocket recvSocket;  // Socket used for receiving 
	private int currentSeqNo;
	private Connection con;
	private int seqno;
	public Quiz(int noOfStudents,int noOfgroups,int noOfStudentsInGroup,String subject,String teacherName,Date date, Connection c)
	{
		this.seqno = 0;
		this.con = c;
		this.noOfGroups = noOfgroups;
		this.noOfStudents = noOfStudents;
		this.noOfStudentsInGroup = noOfStudentsInGroup;
		this.teacherName = teacherName;
		this.studentsList = new ArrayList<Student>();
		this.date = date;
		timeStamp = new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime());
		try {
			
			sendSocket = new DatagramSocket();
			recvSocket = new DatagramSocket(null);
			// Set the socket to reuse the address
			recvSocket.setReuseAddress(true);
			recvSocket.bind(new InetSocketAddress(Utilities.recvPort));
			
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
		    Packet data_packet = (Packet)Utilities.deserialize(buffer);
		    
		    // Deserialize the data string to an appropriate object based on the flags present in the packet received
		    Object obj = Utilities.deserialize(data_packet.data);
	    	
	    	AuthPacket auth_packet = null;
	    	
		    if( data_packet.auth_packet == true )
		    {	
		    	auth_packet = (AuthPacket)obj;
		    	System.out.println("Auth packet it is!!  Username: "+auth_packet.userName+" Password : "+auth_packet.password);
		    }
		    else
		    {
		    	return;
		    }

		    if( verifyDetails(auth_packet.userName, auth_packet.password) == true )
		    {
		    	grantAccess(true);
		    	addStudent(pack.getAddress(),auth_packet.userName);
		    }
		    else
		    {
		    	grantAccess(false);
		    }
		}
		catch (SocketException e)
		{
			e.printStackTrace();
		} 
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	private void grantAccess(boolean flag)
	{
		Packet p = new Packet(seqno++,flag,false,false);
		byte[] buf = Utilities.serialize(p);
		
		DatagramPacket pack = new DatagramPacket(buf, buf.length);
		try {
			sendSocket.send(pack);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public boolean verifyDetails(String id, String password)
	{
		try {
			// Prepare the statement to be executed on the database with the necessary query string
			PreparedStatement p = (PreparedStatement)con.prepareStatement("select * from student_info where roll_number='"+id+
																"' and password='"+password+"'");
			ResultSet result = p.executeQuery();
			// result will initially point to the record before the 1st record. To access the 1st record, use result.next().
			// If it returns null, then the teacher won't be authenticated with the given details
			if( result.next() )
			{
				// After getting the matched record from the database, we extract the Teacher name and subject name
				return true;
			}
			else
			{
				// UserID and Password doesn't exist in the database
				return false;
			}
		} 
		catch (SQLException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Error in the database query ");
		System.exit(0);// included for completeness
		return false;
	}
	
	void addStudent(InetAddress ip, String uname)
	{
		// Add student to the list
		Student s = new Student(ip, uname);
		if( !studentsList.contains(s) )
		{
			studentsList.add(s);
		}
	}
}
