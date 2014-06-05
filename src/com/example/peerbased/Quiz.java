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
		cleanServerBuffer();
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
}
