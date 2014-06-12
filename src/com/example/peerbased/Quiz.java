package com.example.peerbased;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Scanner;
import java.net.*;
import QuizPackets.*;

import javax.swing.text.html.HTMLDocument.Iterator;


import QuizPackets.QuizInterfacePacket;

import com.mysql.jdbc.Connection;

public class Quiz extends Thread{	
	/* Classroom Parameters */
	private byte noOfStudents;
	private byte noOfGroups;
	private byte noOfStudentsInGroup;
	private byte noOfRounds;
	private ArrayList<Student> studentsList;
	private ArrayList<String> leaderList;
	private ArrayList<Group> groups;
	private int questionTimelimitInSeconds;
	private int AnswerTimeLimitInSeconds;
	
	/* Teacher Parameters */
	private String subject;
	private String teacherName;
	private String standard;
	
	/* Date and time of the quiz */
	private Date date;
	private String timeStamp;
		
	/* Network Parameters */
	private DatagramSocket sendSocket;  
	private DatagramSocket recvSocket;
	private InetAddress broadcastIP;
	
	/* Database Parameters */
	private Connection con;
	/*
	 * Question array
	 */
	private ArrayList<Question> questions;

	private boolean running = true;
	
	private InetAddress getBroadcastIP()
	{
		/* Get it from the interface */
		// To be filled
		return null;
	}
	
	
	/* Constructor */
	public Quiz(byte noOfStudents,byte noOfgroups,byte noOfStudentsInGroup,String subject,String teacherName,Date date, Connection c, byte noOfrnds,
				String standard)
	{
		/* Initialize the parameters which are passed from the previous class */
		this.con = c;
		this.noOfGroups = noOfgroups;
		this.noOfStudents = noOfStudents;
		this.noOfStudentsInGroup = noOfStudentsInGroup;
		this.teacherName = teacherName;
		this.subject = subject;
		this.noOfRounds = noOfrnds;
		this.studentsList = StudentListHandler.getList();
		this.questions = new ArrayList<>();
		this.standard = standard;
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
		System.out.println("All the Students are logged in!.\nEnter the time in seconds for the Leader Request Session");
		long time_limit = Utilities.scan.nextLong()*1000;
		System.out.println("Enter the duration of the group selection phase in seconds : ");
		long grp_sel_time = Utilities.scan.nextLong();
		//Send the OnlineStudents status and also the configuration parameters of the Quiz session to the clients
		
		ParameterPacket param_pack = new ParameterPacket(noOfStudents, noOfGroups, noOfStudentsInGroup, noOfRounds, subject);
		Packet packy = new Packet(PacketSequenceNos.QUIZ_START_BCAST_SERVER_SEND, false, true, false,Utilities.serialize(param_pack), true); // param_pack flag is true
		
		// Broadcast n times
		System.out.println("Enter the desired reliability (0-10) for the broadcast packets which are about to be sent!");
		int noOfBroadcastMessages = Utilities.scan.nextInt();
		
		for(int i=0;i<noOfBroadcastMessages;i++)
		{
			broadcastQuizStartMessageAndSleep(packy);
		}
		
		System.out.println("Sent Configuration Parameters to everyone in the network!");
		
		//Start leader Session
		cleanServerBuffer();
		/* Clean the server buffer before starting the leader session, so that all the previous unnecessary packets are discarded! */
		LeaderSession ls = new LeaderSession(studentsList, sendSocket, recvSocket, noOfGroups, time_limit, grp_sel_time, noOfStudentsInGroup);
		ls.startLeaderSession();
		// Leader session ends
		cleanServerBuffer();
		
		/*
		 * Get the data structure of groups from the leadersession object
		 */
		groups = ls.getGroups();
		printGroups();
		
		System.out.println("\nEnter any key to continue\n");
		
		int a = Utilities.scan.nextInt();
		
		try {
			recvSocket.setSoTimeout(1000);
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		sendGroupsToStudents(groups);
		
		startQuiz();
		
	}
	
	private void startQuiz()
	{
		/*
		 * Quiz Starts here
		 * Get the parameters required for Quiz.
		 */
		System.out.println("Enter the number of rounds : ");
		noOfRounds = Utilities.scan.nextByte();
		System.out.println("Enter the time for asking question: ");
		questionTimelimitInSeconds = Utilities.scan.nextInt();
		System.out.println("Enter the time for answering the question: ");
		AnswerTimeLimitInSeconds = Utilities.scan.nextInt();
		
		/*
		 * Clean the server buffer so that all the previous packets which are accumulated in the buffer are destroyed!
		 */
		System.out.println("Cleaning the server buffer\n");
		cleanServerBuffer();
		
		int questionSeqNo = 1234;
		
		for(int i=0;i<noOfRounds;i++)
		{
			/*
			 * Rounds
			 */
			for (int j=0;j<groups.size();j++)
			{
				/*
				 * Turns
				 */
				sendInterfacePacketBCast(j);
				/*
				 * Now receive the questions from active group
				 */
				// Start the timer for question session
				String answer = receiveAndSendQuestions(questionSeqNo);
				if( answer == null )
				{
					/*
					 * No question is formed, make next group as active
					 */
					continue;
				}
				ArrayList<String> answeredStuds = getResponses(questionSeqNo, answer);
				calculateMarks(answeredStuds);
				questionSeqNo++;
			}
		}
	}
	
	private void calculateMarks(ArrayList<String> studs)
	{
		for(int i=0;i<studentsList.size();i++)
		{
			boolean flag = false;
			Student s = studentsList.get(i);
			for(int j=0;j<studs.size();j++)
			{
				if( s.name.equals(studs.get(i)) )
				{
					flag = true;
					s.noOfAnswers++;
					s.marks = s.marks + 2;
					s.noOfQuestions++;
					break;
				}
			}
			if( flag == false )
			{
				s.noOfQuestions++;
			}
		}
	}
	
	private ArrayList<String> getResponses(int qseq_no, String answer)
	{
		ArrayList<String> answeredStudIDs = new ArrayList<>();
		int count = 0;
		byte[] b  = new byte[Utilities.MAX_BUFFER_SIZE];
		DatagramPacket p = new DatagramPacket(b, b.length);
		
		while( true )
		{
			try {
				recvSocket.receive(p);
			}
			catch( SocketTimeoutException e )
			{
				count++;
				if( count >= AnswerTimeLimitInSeconds )
				{
					break;
				}
				continue;
			}
			catch (IOException e) {
				break;
			}
			/*
			 * Response is received
			 */
			InetAddress clientIP = p.getAddress();
			Packet packy = (Packet)Utilities.deserialize(b);
			if( packy.seq_no == PacketSequenceNos.QUIZ_RESPONSE_CLIENT_SEND && packy.quizPacket == true )
			{
				ResponsePacket rp = (ResponsePacket)Utilities.deserialize(packy.data);
				if( rp.questionSequenceNo == qseq_no )
				{
					if( rp.answer.equals(answer) )
					{
						/*
						 * Add student ID to the list
						 */
						answeredStudIDs.add(rp.uID);
						/* 
						 * Send ack to the response
						 */
						sendResponseAck( clientIP, rp );
					}
					else
					{
						sendResponseAck( clientIP, rp );
					}
				}
			}
		}
		return answeredStudIDs;
	}
	
	private void sendResponseAck(InetAddress ip, ResponsePacket rp)
	{
		rp.ack = true;
		Packet p = new Packet(PacketSequenceNos.QUIZ_RESPONSE_SERVER_ACK, false, false, false, Utilities.serialize(rp));
		sendDatagramPacket(sendSocket, ip, Utilities.clientPort, p);
	}
	
	private String receiveAndSendQuestions(int qseq_no)
	{
		int count = 0;
		byte[] b  = new byte[Utilities.MAX_BUFFER_SIZE];
		DatagramPacket p = new DatagramPacket(b, b.length);
		
		while( true )
		{
			try {
				recvSocket.receive(p);
			}
			catch( SocketTimeoutException e )
			{
				count++;
				if( count >= questionTimelimitInSeconds )
				{
					break;
				}
				continue;
			}
			catch (IOException e) {
				break;
			}
			/*
			 * Question is received
			 */
			InetAddress clientIP = p.getAddress();
			Packet packy = (Packet)Utilities.deserialize(b);
			if( packy.seq_no == PacketSequenceNos.QUIZ_QUESTION_PACKET_CLIENT_SEND && packy.quizPacket == true )
			{
				QuestionPacket qp = (QuestionPacket)Utilities.deserialize(packy.data);
				
				if( qp.questionAuthenticated == false )
				{
					if( qp.questionType == 1 )
					{
						System.out.println("---------------------------Question-------------------------------\n"+qp.question);
						for(int i=0;i<4;i++)
						{
							System.out.print(qp.options[i]);
						}
						System.out.println("Answer is : "+qp.correctAnswerOption+"\n\n");
					}
					else if( qp.questionType == 2 )
					{
						System.out.println("---------------------------Question-------------------------------\n"+qp.question);
						for(int i=0;i<2;i++)
						{
							System.out.print(qp.options[i]);
						}
						System.out.println("Answer is : "+qp.correctAnswerOption+"\n\n");
					}
					else if( qp.questionType == 3 )
					{
						System.out.println("---------------------------Question-------------------------------\n"+qp.question);
						System.out.println("Answer is : "+qp.correctAnswerOption+"\n\n");
					}
					
					System.out.println("Is the question valid ? Press 1 for accepting it, 2 for rejecting it");
					int a = Utilities.scan.nextInt();
					
					if( a==1 )
					{
						/*
						 * Send him positive reply saying that his question is selected
						 */
						qp.questionAuthenticated = true;
						Packet qpack = new Packet(PacketSequenceNos.QUIZ_QUESTION_PACKET_SERVER_ACK, false, false, false, Utilities.serialize(qp));
						sendDatagramPacket(sendSocket,clientIP, Utilities.clientPort, qpack);
						/*
						 * Now teacher enters the level of the question
						 */
						System.out.println("Enter the level of the question : ");
						byte level = Utilities.scan.nextByte();
						questions.add(new Question(qp.question, qp.correctAnswerOption, subject, level, standard));
						/*
						 * Copy the level to the packet
						 */
						qp.level = level;
						/*
						 * Send question to everyone
						 */
						qp.questionAuthenticated = true;
						/*
						 * Use the sequence number for each new question so that the previous packets can be ignored
						 */
						qp.questionSeqNo = qseq_no;
						qpack = new Packet(PacketSequenceNos.QUIZ_QUESTION_BROADCAST_SERVER_SEND, false, false, false, Utilities.serialize(qp));
						sendDatagramPacket(sendSocket,Utilities.broadcastIP, Utilities.clientPort, qpack);
						return qp.correctAnswerOption;
					}
					else if( a==2 )
					{
						/*
						 * Send reject packet
						 */
						qp.questionAuthenticated = false;
						Packet qpack = new Packet(PacketSequenceNos.QUIZ_QUESTION_PACKET_SERVER_ACK, false, false, false, Utilities.serialize(qp));
						sendDatagramPacket(sendSocket,clientIP, Utilities.clientPort, qpack);
					}
				}
			}
		}
		return null;
	}
	
	private void sendInterfacePacketBCast(int grpIndex)
	{
		/*
		 * Make group 'g' as the active group and all others as passive 
		 */
		Group g = groups.get(grpIndex);
		QuizInterfacePacket qip = new QuizInterfacePacket(g.groupName, g.leaderID);
		
		Packet pack = new Packet(PacketSequenceNos.QUIZ_INTERFACE_PACKET_SERVER_SEND, false, true, false, Utilities.serialize(g));
		pack.quizPacket = true;
		
		/*
		 *  Send the packet
		 */
		sendDatagramPacket(sendSocket, broadcastIP, Utilities.clientPort, pack);
		
	}

	private void sendGroupsToStudents(ArrayList<Group> grp)
	{
		for(int i=0;i<grp.size();i++)
		{
			Group g = grp.get(i);
			System.out.println("\n\nGroup "+g.groupName);
			ArrayList<Student> teammembers = g.teamMembers;
			Student leader = g.leaderRecord;
			sendToLeader(g.groupName, leader , teammembers);
			sendToTeamMem(g.groupName, leader, teammembers);
		}
	}
	
	private void sendToLeader(String gname, Student leader, ArrayList<Student> team )
	{
		System.out.println("Sending to "+leader.name);
		SelectedGroupPacket sgp = new SelectedGroupPacket(gname , leader, team);
		Packet p = new Packet(PacketSequenceNos.FORMED_GROUP_SERVER_SEND, false, false, false, Utilities.serialize(sgp),
				false, false, true, false);
		byte[] bytes = Utilities.serialize(p);
		
		DatagramPacket dp = new DatagramPacket(bytes, bytes.length, leader.IP, Utilities.clientPort);
		
		try {
			sendSocket.send(dp);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void sendToTeamMem(String gname , Student leader, ArrayList<Student> team)
	{
		for(int i=0;i<team.size();i++)
		{
			Student stud = team.get(i);
			SelectedGroupPacket sgp = new SelectedGroupPacket(gname , leader, team);
			Packet p = new Packet(PacketSequenceNos.FORMED_GROUP_SERVER_SEND, false, false, false, Utilities.serialize(sgp),
					false, false, true, false);
			byte[] bytes = Utilities.serialize(p);
			
			DatagramPacket dp = new DatagramPacket(bytes, bytes.length, stud.IP, Utilities.clientPort);
			try {
				sendSocket.send(dp);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
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
	
	private void printGroups()
	{
		System.out.println("The groups are : ");
		for(int i=0;i<groups.size();i++)
		{
			Group g = groups.get(i);
			System.out.println("\nLeader : "+g.leaderID+" GroupName: "+g.groupName);
			for(int j=0;j<g.teamMembers.size();j++)
			{
				System.out.println("Student : "+g.teamMembers.get(j).name);
			}
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
				/*
				 * This exception occurs when there are no packets for the specified timeout period.
				 * Buffer is clean!!
				 */
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
	void sendDatagramPacket(DatagramSocket sock,InetAddress ip, int port, Packet p)
	{
		byte[] buff = Utilities.serialize(p);
		DatagramPacket packet = new DatagramPacket(buff, buff.length, ip, port);
		try
		{
			sock.send(packet);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	void broadcastQuizStartMessageAndSleep(Packet p)
	{
		sendDatagramPacket(sendSocket, broadcastIP, Utilities.clientPort, p);
		System.out.println("Sent Configuration Parameters to everyone in the network!");
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
