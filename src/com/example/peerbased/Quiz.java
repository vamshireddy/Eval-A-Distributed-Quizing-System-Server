package com.example.peerbased;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.net.*;


import com.mysql.jdbc.Connection;
import com.mysql.jdbc.PreparedStatement;

public class Quiz extends Thread{	
	/* Classroom Parameters */
	private byte noOfStudents;
	private byte noOfGroups;
	private byte noOfStudentsInGroup;
	private byte noOfRounds;
	private ArrayList<Student> studentsList;
	private ArrayList<String> leaderList;
	
	/* Teacher Parameters */
	private String subject;
	private String teacherName;
	
	/* Date and time of the quiz */
	private Date date;
	private String timeStamp;
		
	/* Network Parameters */
	private DatagramSocket sendSocket;  
	private DatagramSocket recvSocket;
	private InetAddress broadcastIP;
	
	/* Sequence numbers of the packets */
	//private int currentSeqNo;
	private int localSeqNo;
	
	/* Database Parameters */
	private Connection con;

	private boolean running = true;
	
	private InetAddress getBroadcastIP()
	{
		// To be filled
		return null;
	}
	
	
	/* Constructor */
	public Quiz(byte noOfStudents,byte noOfgroups,byte noOfStudentsInGroup,String subject,String teacherName,Date date, Connection c, byte noOfrnds)
	{
		/* Initialize the parameters which are passed from the previous class */
		this.localSeqNo = 0;
		this.con = c;
		this.noOfGroups = noOfgroups;
		this.noOfStudents = noOfStudents;
		this.noOfStudentsInGroup = noOfStudentsInGroup;
		this.teacherName = teacherName;
		this.subject = subject;
		this.noOfRounds = noOfrnds;
		this.studentsList = StudentListHandler.getList();
		// Set the student list hadler so that all classes can access it!
		
		this.date = date;
		timeStamp = new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime());
		
		try {
			sendSocket = new DatagramSocket();
			recvSocket = new DatagramSocket(null);
			// Set the socket to reuse the address
			recvSocket.setReuseAddress(true);
			recvSocket.bind(new InetSocketAddress(Utilities.servPort));
			// Set the broadcast IP, H
			broadcastIP = InetAddress.getByName("192.168.1.255");
			
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/* This is the method which performs the crucial function of this class */
	public void startQuizSession()
	{
		while( studentsList.size() < noOfStudents )
		{
			System.out.println("Waiting for students to log in!");
			try {
				sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.out.println("All the Students are logged in!. Enter the time limit to process to leader request session");
		long time_limit = Utilities.scan.nextLong();
		//Send the OnlineStudents status and also the configuration parameters of the Quiz session to the clients
		ParameterPacket param_pack = new ParameterPacket(noOfStudents, noOfGroups, noOfStudentsInGroup, noOfRounds, subject);
		Packet packy = new Packet(101010, false, true, false,Utilities.serialize(param_pack), true); // param_pack flag is true
		byte[] ser_bytes = Utilities.serialize(packy);
		// Broadcast 3 times
		InetAddress s=null;
		try {
			s = InetAddress.getByName("192.168.1.255");
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		sendDatagramPacket(sendSocket, ser_bytes, s, Utilities.clientPort);
		System.out.println("Sent Configuration Parameters to everyone in the network!");
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		sendDatagramPacket(sendSocket, ser_bytes, s, Utilities.clientPort);
		System.out.println("Sent Configuration Parameters to everyone in the network!");
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		sendDatagramPacket(sendSocket, ser_bytes, s, Utilities.clientPort);
		System.out.println("Sent Configuration Parameters to everyone in the network!");
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		sendDatagramPacket(sendSocket, ser_bytes, s, Utilities.clientPort);
		System.out.println("Sent Configuration Parameters to everyone in the network!");
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		sendDatagramPacket(sendSocket, ser_bytes, s, Utilities.clientPort);
		System.out.println("Sent Configuration Parameters to everyone in the network!");
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		sendDatagramPacket(sendSocket, ser_bytes, s, Utilities.clientPort);
		System.out.println("Sent Configuration Parameters to everyone in the network!");
		//Start leader Session
		cleanServerBuffer();
		LeaderSession ls = new LeaderSession(studentsList, sendSocket, recvSocket, localSeqNo, noOfGroups, time_limit);
		ls.startLeaderSession();
		// Leader session ends
	}
	
	private void formGroups() {
		
	}


	private void startProbing() {
		Probe p = new Probe(studentsList);
		p.start();
		try {
			p.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
		    
		    // Send the reply back by incrementing the seqno of the received packet
		    localSeqNo = data_packet.seq_no+1;
		    
		    // Deserialize the data string to an appropriate object based on the flags present in the packet
			//System.out.println("data is "+data_packet.data);
		    Object obj = Utilities.deserialize(data_packet.data);
		    
	    	AuthPacket auth_packet = null;
	    	
		    if( data_packet.auth_packet == true && data_packet.bcast == false 
		    	&& data_packet.probe_packet == false && data_packet.data!=null)
		    {	
		    	auth_packet = (AuthPacket)obj;
		    	System.out.println("Authentication Request recieved from the Client\n" +
		    			"Username: "+auth_packet.userID+" \nPassword : "+auth_packet.password+"\n");
		    }
		    else
		    {
		    	// Send denyAccess to the client, so that the client would send the request again
		    	grantAccess(false,clientIP, Utilities.INVALID_REQUEST);
		    	return;
		    }
		    System.out.println("Now checking in the Database for the client record...\n");
		    if( verifyDetails(auth_packet.userID, auth_packet.password) == true)
		    {
		    	Student pres_stud = isPresent(auth_packet.userID);
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
		    		addStudent(clientIP,auth_packet.userID);
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
			if( running == true )
			{
				e.printStackTrace();
			}
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
			
			/* TODO : Its hardcoded for now. Student name is to fetched from database later */
			
			ap.studentName = "student";
		}
		else
		{
			ap = new AuthPacket(true, flag);
		}
		
		Packet p = new Packet(localSeqNo,true,false,false,Utilities.serialize(ap));
		
		byte[] buf = Utilities.serialize(p);
		DatagramPacket pack = new DatagramPacket(buf, buf.length, clientIP, Utilities.clientPort);
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
			int id_int;
			try
			{
				 id_int = Integer.parseInt(id);
			}
			catch( NumberFormatException e )
			{
				return false;
			}
			PreparedStatement p = (PreparedStatement)con.prepareStatement("select * from student_info where roll_number='"+id_int+
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
			if( s.uID.equals(new String(name)))
			{
				return s;
			}
		}
		return null;
	}
	public void run()
	{
		/* Receive the packets in a loop, untill all the students are logged in ( added to studentsList ) */
		while( running == true)
		{
			System.out.println("waiting!!!! \n"+studentsList.size());
			receiveAuthPackets();
			if( studentsList.size() == noOfStudents )
			{
				System.out.println("All students have logged in!. To proceed please press 1.\nIf someone is unable to connect please wait untill he/she connects\n");
			}
		}
		
	}
	public void shutdown()
	{
		running = false;
	}
	public void closeSockets()
	{
		sendSocket.close();
		recvSocket.close();
	}
	
	public void cleanServerBuffer()
	{
		try {
			// 1 second
			recvSocket.setSoTimeout(1000);
		} catch (SocketException e1) {
			e1.printStackTrace();
		}
		while(true)
		{
			byte[] b  = new byte[Utilities.MAX_BUFFER_SIZE];
			DatagramPacket p = new DatagramPacket(b, b.length);
			try {
				recvSocket.receive(p);
			}
			catch( SocketTimeoutException e1)
			{
				// This exception occurs when there are no packets for the specified timeout period.
				// Buffer is clean!!
				try {
					recvSocket.setSoTimeout(0); // infinete timeout
				} catch (SocketException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return;
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
