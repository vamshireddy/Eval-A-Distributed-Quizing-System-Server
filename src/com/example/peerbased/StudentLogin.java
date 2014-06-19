package com.example.peerbased;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import com.mysql.jdbc.Connection;
import com.mysql.jdbc.PreparedStatement;

public class StudentLogin extends Thread{
	
	private DatagramSocket sock;
	private Connection con;
	private ArrayList<Student> studentsList;
	
	public StudentLogin(Connection databaseConnection) {
		try 
		{
			con = databaseConnection;
			sock = new DatagramSocket(null);
			// Set the socket to reuse the address
			sock.setReuseAddress(true);
			sock.bind(new InetSocketAddress(Utilities.authServerPort));
			studentsList = StudentListHandler.getList();
		} 
		catch (SocketException e){
			e.printStackTrace();
		}
	}
	public void receiveAuthPackets()
	{
		try 
		{
		    byte[] buffer = new byte[Utilities.MAX_BUFFER_SIZE];
		    DatagramPacket pack =  new DatagramPacket(buffer, buffer.length);
		    
		    sock.receive(pack);
		    
		    InetAddress clientIP = pack.getAddress();
		    
		    Packet data_packet = (Packet)Utilities.deserialize(buffer);

	    	AuthPacket auth_packet = (AuthPacket)Utilities.deserialize(data_packet.data);
	    	
		    if( data_packet.auth_packet == true && data_packet.seq_no == PacketSequenceNos.AUTHENTICATION_SEND_CLIENT )
		    {	
		    	System.out.println("Name : "+auth_packet.studentName+" Uid : "+auth_packet.userID+" pass : "+auth_packet.password);
		    }
		    else
		    {
		    	/*
		    	 *  Send denyAccess to the client, so that the client would send the request again
		    	 */
		    	grantAccess(false,clientIP, Utilities.INVALID_REQUEST, "");
		    	return;
		    }
		    
		    String studentName = verifyDetails(auth_packet.userID, auth_packet.password);
		    
		    if( studentName != null )
		    {
		    	/*
		    	 * Student is authentic, Now check if the student's record has already been stored
		    	 */
		    	Student pres_stud = isPresent(auth_packet.userID);
		    	if( pres_stud != null )
		    	{
		    		/*
		    		 * Student record is already present in the students list.
		    		 */
		    		if( pres_stud.IP.equals(clientIP) )
			    	{
		    			grantAccess(true,clientIP, Utilities.NO_ERROR, studentName);
		    			return;
			    	}
			    	else
			    	{
			    		/*
			    		 * Student is logging in from different Tablet(IP)
			    		 */
			    		grantAccess(false, clientIP, Utilities.ALREADY_LOGGED, studentName);
			    	}
		    	}
		    	else
		    	{
		    		/*
		    		 * Student request is new, Add an entry into the list, and send an ack to him
		    		 */
		    		grantAccess(true, clientIP, Utilities.NO_ERROR, studentName);
		    		addStudent(clientIP,auth_packet.userID, studentName);
		    	}
		    }
		    else
		    {
		    	/*
		    	 * Entered credentials by the student didn't match with any of the records in the database
		    	 * Send him -ve reply
		    	 */
		    	grantAccess(false,clientIP, Utilities.INVALID_USER_PASS, studentName);
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
	
	private void grantAccess(boolean flag,InetAddress clientIP, byte errorCode, String name)
	{
		AuthPacket ap = null;
		if( flag == false )
		{
			/*
			 * Use error code to convey the type of error
			 */
			ap = new AuthPacket(true, flag, errorCode);
		}
		else
		{
			ap = new AuthPacket(true, flag);
		}
		ap.studentName = name;
		
		Packet p = new Packet(PacketSequenceNos.AUTHENTICATION_SEND_SERVER,true,false,false,Utilities.serialize(ap));
		
		byte[] buf = Utilities.serialize(p);
		
		DatagramPacket pack = new DatagramPacket(buf, buf.length, clientIP, Utilities.authClientPort);
		
		try {
			sock.send(pack);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public String verifyDetails(String id, String password)
	{
		String name = null;
		try {
			// Prepare the statement to be executed on the database with the necessary query string
			int id_int;
			try
			{
				 /*
				  * Check if the UID String is an Integer
				  */
				 id_int = Integer.parseInt(id);
			}
			catch( NumberFormatException e )
			{
				return null;
			}
			PreparedStatement p = (PreparedStatement)con.prepareStatement("select * from student_info where roll_number='"+id_int+
																"' and password='"+password+"'");
			ResultSet result = p.executeQuery();
			// result will initially point to the record before the 1st record. To access the 1st record, use result.next().
			// If it returns null, then the teacher won't be authenticated with the given details
			if( result.next() )
			{
				// After getting the matched record from the database, we extract the Teacher name and subject name
				name = result.getString("name");
				return name;
			}
			else
			{
				// UserID and Password doesn't exist in the database
				return null;
			}
		} 
		catch (SQLException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Error in the database query ");
		System.exit(0);// included for completeness
		return null;
	}
	
	void addStudent(InetAddress ip, String uid, String name)
	{
		// Add student to the list
		Student s = new Student(ip, uid, name);
		studentsList.add(s);
		//System.out.println("Students list count : "+studentsList.size());
	}
	Student isPresent(String id)
	{
		for(int i=0;i<studentsList.size();i++)
		{
			Student s = studentsList.get(i);
			if( s.uID.equals(new String(id)))
			{
				return s;
			}
		}
		return null;
	}
	public void run()
	{
		/* Authenticate the students */
		while(true)
		{
			receiveAuthPackets();
		}
	}
}
