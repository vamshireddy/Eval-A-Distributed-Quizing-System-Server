package com.example.peerbased;
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
import java.net.UnknownHostException;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.net.*;

import javax.jws.Oneway;
import javax.print.DocFlavor.STRING;

import com.mysql.jdbc.Connection;
import com.mysql.jdbc.PreparedStatement;

public class Quiz {	private static int port = Utilities.recvPort;
	private byte noOfStudents;
	private byte noOfGroups;
	private byte noOfStudentsInGroup;
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
	private InetAddress broadcastIP;
	public Quiz(byte noOfStudents,byte noOfgroups,byte noOfStudentsInGroup,String subject,String teacherName,Date date, Connection c)
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
			// Set the broadcast IP
			broadcastIP = InetAddress.getByName("192.168.1.255");
			
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void start()
	{
		// This method is called when the teacher starts the quiz session
		// Initial phase: Receive authentication packets from the 
		System.out.println("There are "+noOfStudents+" No of students ");
		System.out.println("Students list count : "+studentsList.size());

		while( studentsList.size() < noOfStudents )
		{
			receiveAuthPackets();
		}
		
		
		
		// Send the OnlineStudents status and also the configuration parameters of the Quiz session to the clients
		/*ParameterPacket param_pack = new ParameterPacket(noOfStudents, noOfGroups, noOfStudentsInGroup, studentsList);
		Packet packy = new Packet(seqno++, false, true, false,Utilities.serialize(param_pack), true); // param_pack flag is true
		byte[] ser_bytes = Utilities.serialize(packy);
		sendDatagramPacket(sendSocket, ser_bytes, broadcastIP , Utilities.clientPort);
		System.out.println("Sent Configuration Parameters to everyone in the network!");*/
		System.out.println("Initial Session Complete. There are "+studentsList.size()+" students logged in\n");
		for(int i=0;i<studentsList.size();i++)
		{
			Student s = studentsList.get(i);
			System.out.println(s.uname+" : "+s.IP);
		}
		System.out.println("Initial Session Complete. There are "+studentsList.size()+" students logged in\n");
		System.exit(1);
		System.out.println("Exited!!");
	}
	
	public void receiveAuthPackets()
	{
		try 
		{
			// Allocate the buffer of MAX_BUFFER_SIZE, which is defined in the Utilities class
		    byte[] buffer = new byte[Utilities.MAX_BUFFER_SIZE];
		    DatagramPacket pack =  new DatagramPacket(buffer, buffer.length);
		    // Wait for the client's auth packet
		    recvSocket.receive(pack);
		    
		    InetAddress clientIP = pack.getAddress();
		    // Deserialize the Packet object and store in the object 'p'
		    Packet data_packet = (Packet)Utilities.deserialize(buffer);
		    // Deserialize the data string to an appropriate object based on the flags present in the packet
			//System.out.println("data is "+data_packet.data);
		    Object obj = Utilities.deserialize(data_packet.data);
	    	
	    	AuthPacket auth_packet = null;
	    	
		    if( data_packet.auth_packet == true && data_packet.bcast == false 
		    	&& data_packet.probe_packet == false && data_packet.data!=null)
		    {	
		    	auth_packet = (AuthPacket)obj;
		    	if( auth_packet.userName == null || auth_packet.password == null )
		    	{
		    		// Checks if the username, password are not null and also makes sure that client is not sending grantaccess as true
		    		grantAccess(false,clientIP, Utilities.INVALID_FIELDS);
		    		return;
		    	}
		    	System.out.println("Authentication Request recieved from the Client\n" +
		    			"Username: "+auth_packet.userName+" \nPassword : "+auth_packet.password+"\n");
		    }
		    else
		    {
		    	// Send denyAccess to the client, so that the client would send the request again
		    	grantAccess(false,clientIP, Utilities.INVALID_REQUEST);
		    	return;
		    }
		    
		    System.out.println("Now checking in the Database for the client record...\n");
		    if( verifyDetails(auth_packet.userName, auth_packet.password) == true)
		    {
		    	Student pres_stud = isPresent(auth_packet.userName);
		    	if( pres_stud!=null )
		    	{
		    		if( pres_stud.IP.equals(clientIP) )
			    	{
		    			System.out.println("ALREADY LOGGED SAME IP!");
		    			grantAccess(true,clientIP, Utilities.NO_ERROR);
		    			return;
			    	}
			    	else
			    	{
			    		System.out.println("ALREADY LOGGED!");
			    		grantAccess(false, clientIP, Utilities.ALREADY_LOGGED);
			    	}
		    	}
		    	else
		    	{
		    		System.out.println("User is valid...Granted access.. Now sending reply..\n");
		    		grantAccess(true, clientIP, Utilities.NO_ERROR);
		    		addStudent(clientIP,auth_packet.userName);
		    	}
		    }
		    else
		    {
		    	System.out.println("Access denied!");
		    	grantAccess(false,clientIP, Utilities.INVALID_USER_PASS);
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
	
	private void grantAccess(boolean flag,InetAddress clientIP, byte errorCode)
	{
		AuthPacket ap = null;
		if( flag == false )
		{
			ap = new AuthPacket(true, flag, errorCode);
		}
		else
		{
			ap = new AuthPacket(true, flag);
		}
		
		Packet p = new Packet(seqno++,true,false,false,Utilities.serialize(ap));
		
		byte[] buf = Utilities.serialize(p);
		DatagramPacket pack = new DatagramPacket(buf, buf.length, clientIP, Utilities.clientPort);
		try {
			sendSocket.send(pack);
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
		studentsList.add(s);
		System.out.println("Students list count : "+studentsList.size());
	}
	
	void sendDatagramPacket(DatagramSocket sock, byte[] buff, InetAddress ip, int port)
	{
		DatagramPacket packet = new DatagramPacket(buff, buff.length, ip, port);
		try {
			sock.send(packet);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	Student isPresent(String name)
	{
		for(int i=0;i<studentsList.size();i++)
		{
			Student s = studentsList.get(i);
			if( s.uname.equals(new String(name)))
			{
				return s;
			}
		}
		return null;
	}
}
