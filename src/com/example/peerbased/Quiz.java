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
import java.net.*;
import QuizPackets.*;

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
	private byte questionLevel;

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
		
		 // param_pack flag is true
		
		// Broadcast n times
		/*System.out.println("Enter the desired reliability (0-10) for the broadcast packets which are about to be sent!");
		int noOfBroadcastMessages = Utilities.scan.nextInt();
		
		for(int i=0;i<noOfBroadcastMessages;i++)
		{
			broadcastQuizStartMessageAndSleep(packy);
		}*/
		
		/*
		 * Set socket timeout to 1 second
		 */
		try {
			recvSocket.setSoTimeout(1000);
		} catch (SocketException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		for(int i=0;i<studentsList.size();i++)
		{
			/*
			 * For sending the packet
			 */
			ParameterPacket param_pack = new ParameterPacket(noOfStudents, noOfGroups, noOfStudentsInGroup, noOfRounds, subject);
			Packet packy = new Packet(Utilities.seqNo, false, true, false,Utilities.serialize(param_pack), true);
			
			packy.type = PacketTypes.QUIZ_TURN_SCREEN;
			packy.ack = false;
			/*
			 * For receiving the packet
			 */
			Student s = studentsList.get(i);
			System.out.println("\nI am sending to "+s.name+"\n");
			if( sendToClient_Reliable(sendSocket, recvSocket, s.IP, packy) == -1 )
			{
				System.out.println("Client couldn't connect");
				System.exit(0);
			}
		}
		
		
		System.out.println("-----------------------------------------------\nSent Configuration Parameters to everyone in the network!");
		
		/*
		 * Start leader Session
		 */
		cleanServerBuffer();
		
		/* Clean the server buffer before starting the leader session, so that all the previous unnecessary packets are discarded! */
		
		LeaderSession ls = new LeaderSession(studentsList, sendSocket, recvSocket, noOfGroups, time_limit, grp_sel_time, noOfStudentsInGroup);
		ls.startLeaderSession();
		
		/*
		 *  Leader session ends
		 */
		cleanServerBuffer();
		
		/*
		 * Get the data structure of groups from the leader session object
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
	
	public static int sendToClient_Reliable(DatagramSocket sendSock, DatagramSocket recvSock, InetAddress IP, Packet packy)
	{
		boolean ackFlag = false;
		
		byte[] b  = new byte[Utilities.MAX_BUFFER_SIZE];
		DatagramPacket recvPacky = new DatagramPacket(b, b.length);
		
		for(int j=0;j<Utilities.noOfAttempts;j++)
		{
			System.out.println("Attempt "+(j+1));
			sendDatagramPacket(sendSock, IP, Utilities.clientPort, packy);
			/*
			 * Now try to receive the ack
			 */
			while( true )
			{
				try {
					recvSock.receive(recvPacky);
				}
				catch ( SocketTimeoutException ste )
				{
					System.out.println("Timeout!");
					break;
				}
				catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					System.exit(0);
				}
				/*
				 * Now check whether the recvd packet seqNo is matching
					Utilities.seqNo++;		 */
				Packet recvPacket = (Packet)Utilities.deserialize(b);
				if( recvPacket.seq_no == Utilities.seqNo && recvPacket.ack == true )
				{
					System.out.println("I got a reply from client "+recvPacky.getAddress());
					ackFlag = true;
					/*
					 * Got reply
					 */
					break;
				}
				else
				{
					System.out.println("I got a packet from client "+recvPacky.getAddress()+" But its something else");
					continue;
				}
			}
			
			if( ackFlag == true )
			{
				/*
				 * Get out of this place
				 */
				Utilities.seqNo++;
				return Utilities.SUCCESS;
			}	
			/*
			 * If it is false, then it will continue
			 */
		}
		/*
		 * No reply from the client, get out of this place
		 */
		Utilities.seqNo++;
		return Utilities.FAIL;
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
				Group g = groups.get(j);
				
				System.out.println("Sending the quiz start packet to the group "+j+" ! ");
				sendInterfacePacketBCast(g);
				/*
				 * 
				 * Now receive the questions from active group
				 */
				// Start the timer for question session
				String answer = receiveAndSendQuestions(questionSeqNo,g);
				System.out.println("\nRecvd question!!\n");
				if( answer == null )
				{
					/*
					 * No question is formed, make next group as active
					 */
					System.out.println("OMG!!!!!!!!!!!!!!!!!!!!!!!!");
					continue;
				}
				ArrayList<String> answeredStuds = getResponses(questionSeqNo, answer);
				/*
				 * Calculate the marks according to the level
				 */
				calculateMarks(answeredStuds);
				questionSeqNo++;
				cleanBuffer();
			}
		}
	}
	
	private void calculateMarks(ArrayList<String> studs)
	{	
		
		for(int i=0;i<studentsList.size();i++)
		{
			Student s = studentsList.get(i);
			for(int j=0;j<studs.size();j++)
			{
				if( s.uID.equals(studs.get(j)) )
				{
					s.noOfAnswers++;
					s.marks = s.marks + questionLevel;
					break;
				}
			}
		}
		for(int i=0;i<studentsList.size();i++)
		{
			Student s = studentsList.get(i);
			System.out.println("Student name : "+s.name+"  Marks : "+s.marks+" Attempted : "+s.noOfQuestions+" Answered correct :"+s.noOfAnswers);
		}
	}
	
	private ArrayList<String> getResponses(int qseq_no, String answer)
	{
		/*
		 * Arraylist for storing the ID's of the students who correctly responded to the question which is sent by the server
		 */
		ArrayList<String> answeredStudIDs = new ArrayList<>();
		
		int count = 0;
		
		byte[] b  = new byte[Utilities.MAX_BUFFER_SIZE];
		DatagramPacket p = new DatagramPacket(b, b.length);
		
		try {
			recvSocket.setSoTimeout(1000);
		} catch (SocketException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		while(true)
		{
			try {
				System.out.println("\nWaiting fro responses!!!!!!!!!!!!\n");
				recvSocket.receive(p);
				System.out.println("\nGot response!!!!!!!!!!!!\n");
			}
			catch( SocketTimeoutException e )
			{
				count++;
				System.out.println("timeout!");
				if( count >= AnswerTimeLimitInSeconds )
				{
					System.out.println("done!");
					break;
				}
				continue;
			}
			catch (IOException e) {
				e.printStackTrace();
				System.exit(0);
			}
			/*
			 * Response is received
			 */
			InetAddress clientIP = p.getAddress();
			Packet packy = (Packet)Utilities.deserialize(b);
			
			System.out.println("Pack : "+packy.seq_no+" qp : "+packy.quizPacket);
			
			if( packy.seq_no == PacketSequenceNos.QUIZ_RESPONSE_CLIENT_SEND && packy.quizPacket == true)
			{
				System.out.println("packet seqno correct and its a quiz packet!!!");
				ResponsePacket rp = (ResponsePacket)Utilities.deserialize(packy.data);
				if( rp.questionSequenceNo == qseq_no )
				{
					System.out.println("question Seqno correct!!!");
					System.out.println("Ans : "+answer+" rp.ans: "+rp.answer);
					if( rp.answer == null )
					{
						/*
						 * Its a wrong answer which is sent by the student
						 */
						sendResponseAck( false , clientIP, rp );
						continue;
					}
					if( rp.answer.equals(answer) )
					{
						/*
						 * Add student ID to the list
						 */
						System.out.println("answer is correct!!!");
						if( !answeredStudIDs.contains(rp.uID) )
						{
							/*
							 * avoiding redundant entries
							 */
							answeredStudIDs.add(rp.uID);
						}
						/* 	answeredStudIDs.add(rp.uID);
						 * Send ack to the response
						 */
						sendResponseAck( true, clientIP, rp );
					}
					else
					{
						sendResponseAck( false , clientIP, rp );
					}
				}
				else
				{
					continue;
				}
			}
			else
			{
				continue;
			}
		}
		return answeredStudIDs;
	}
	
	private void sendResponseAck(boolean res, InetAddress ip, ResponsePacket rp)
	{
		System.out.println("Sent packkkky!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
		rp.ack = true;
		rp.result = res;
		Packet p = new Packet(PacketSequenceNos.QUIZ_RESPONSE_SERVER_ACK, false, false, false, Utilities.serialize(rp));
		p.quizPacket = true;
		sendDatagramPacket(sendSocket, ip, Utilities.clientPort, p);
	}
	
	private String receiveAndSendQuestions(int qseq_no,Group currentGrp)
	{
		byte[] b  = new byte[Utilities.MAX_BUFFER_SIZE];
		DatagramPacket p = new DatagramPacket(b, b.length);
		
		while( true )
		{
			int count = 0;
			Packet packy = null;
			InetAddress clientIP = null;
			
			while( true )
			{
				try {
					recvSocket.receive(p);
				}
				catch( SocketTimeoutException e )
				{
					if( count < questionTimelimitInSeconds )
					{
						count++;
						continue;
					}
					else
					{
						/*
						 * Timeout done
						 */
						if( packy == null )
						{
							System.out.println("None of the questions are formed from current group.\nPress any key to give a chance again to the same group");
							String useless_input = Utilities.scan.nextLine();
							/*
							 * Ask the time limit again for the question formation and give them a chance again
							 */
							System.out.println("Please enter the new timelimit for forming the question: ");
							questionTimelimitInSeconds = Utilities.scan.nextInt();
							count = 0;
							continue;
						}
						break;
					}
				}
				catch (IOException e) {
					e.printStackTrace();
					System.exit(0);
				}
				/*
				 * Question is received
				 * send an ack immediately
				 */
				clientIP = p.getAddress();
				packy = (Packet)Utilities.deserialize(b);
				
				/*
				 * Send an ack back now
				 */
				Packet ackPacket = new Packet(PacketSequenceNos.SEQ_NOT_NEEDED, false, false, false, null);
				ackPacket.type = PacketTypes.QUESTION_ACK;
				ackPacket.ack = true;
				
				byte[] ackPacketBytes = Utilities.serialize(ackPacket);	
				DatagramPacket ackPackDgram = new DatagramPacket(ackPacketBytes, ackPacketBytes.length,clientIP,Utilities.clientPort);
				try {
					sendSocket.send(ackPackDgram);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			/*
			 * Now ack is sent and session time is over, Do the processing
			 */
			
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
							System.out.println((i+1)+" : "+qp.options[i]);
						}
						System.out.println("Answer is : "+qp.correctAnswerOption+"\n\n");
					}
					else if( qp.questionType == 2 )
					{
						System.out.println("---------------------------Question-------------------------------\n"+qp.question);
						for(int i=0;i<2;i++)
						{
							System.out.println((i+1)+" : "+qp.options[i]);
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
						
						Packet qpack = new Packet(Utilities.seqNo, false, false, false, Utilities.serialize(qp));
						qpack.quizPacket = true;
						qpack.ack = false;
						qpack.type = PacketTypes.QUESTION_VALIDITY;
						
						/*
						 * Send to every one in that group
						 * 1st send to leader, then to every one in that group
						 */
						
						sendToClient_Reliable(sendSocket, recvSocket,clientIP, qpack);
						
						for(int i=0;i<currentGrp.teamMembers.size();i++)
						{
							Student s = currentGrp.teamMembers.get(i);
							qpack.seq_no = Utilities.seqNo;
							sendToClient_Reliable(sendSocket, recvSocket,s.IP, qpack);
						}
						
						/*
						 * Everyone is being notified in that group
						 */
						
						/*
						 * Now teacher enters the level of the question
						 */
						System.out.println("Enter the level of the question : ");
						byte level = Utilities.scan.nextByte();
						
						questionLevel = level;
						
						questions.add(new Question(qp.question, qp.correctAnswerOption, subject, level, standard));
						/*
						 * Copy the level to the packet
						 */
						qp.level = level;
						/*
						 * Send question to everyone
						 */
						/*
						 * Use the sequence number for each new question so that the previous packets can be ignored
						 */
						qp.questionSeqNo = qseq_no;
						qp.questionAuthenticated = true;
						
						qpack = new Packet(Utilities.seqNo, false, false, false, Utilities.serialize(qp));
						qpack.quizPacket = true;
						qpack.ack = false;
						qpack.type = PacketTypes.QUESTION_BROADCAST;
						
						for( int i=0;i<groups.size();i++ )
						{
							Group g = groups.get(i);
							if( g.equals(currentGrp))
							{
								continue;
							}
							/*
							 * Send to the leader of the group
							 */
							qpack.seq_no = Utilities.seqNo;
							if( sendToClient_Reliable(sendSocket, recvSocket,g.leaderRecord.IP, qpack) == Utilities.SUCCESS )
							{
								/*
								 * If the question is sent successfully then increment the no of questions attempted for the leader
								 */
								g.leaderRecord.noOfQuestions++;
							}
							/*
							 * Send to team members of the group
							 */
							for( int j=0;j<g.teamMembers.size();j++ )
							{
								Student stud = g.teamMembers.get(j);
								qpack.seq_no = Utilities.seqNo;
								if( sendToClient_Reliable(sendSocket, recvSocket,stud.IP, qpack) == Utilities.SUCCESS )
								{
									/*
									 * If the question is sent successfully then increment the no of questions attempted for the student
									 */
									stud.noOfQuestions++;
								}
							}
						}
						questionLevel = level;
						return qp.correctAnswerOption;
					}
					else if( a==2 )
					{
						/*
						 * Send reject packet
						 */
						qp.questionAuthenticated = false;
						
						Packet qpack = new Packet(Utilities.seqNo, false, false, false, Utilities.serialize(qp));
						qpack.type = PacketTypes.QUESTION_VALIDITY;
						qpack.ack = false;
						qpack.quizPacket = true;

						sendToClient_Reliable(sendSocket, recvSocket,clientIP, qpack);
						
					}
				}
			}
		}
	}
	
	private void sendInterfacePacketBCast(Group g)
	{
		/*
		 * Make group 'g' as the active group and all others as passive 
		 */
		
		try {
			recvSocket.setSoTimeout(1000);
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		QuizInterfacePacket qip = new QuizInterfacePacket(g.groupName, g.leaderID);
		Packet pack = new Packet(Utilities.seqNo, false, true, false, Utilities.serialize(qip));
		
		/*
		 *  Send the packet
		 */
		for(int i=0;i<studentsList.size();i++)
		{
			pack.seq_no = Utilities.seqNo;
			pack.ack = false;
			pack.type = PacketTypes.QUIZ_INTERFACE_START_PACKET;
			
			Student s = studentsList.get(i);
			sendToClient_Reliable(sendSocket,recvSocket,s.IP,pack);
		}
		
		System.out.println("Sent all the packets !!. Hope the clients interface is changed!!");
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
		System.out.println("inside packet size is : "+Utilities.serialize(sgp).length);
		
		Packet sendPacky = new Packet(Utilities.seqNo, false, false, false, Utilities.serialize(sgp),
				false, false, true, false);
		
		sendPacky.ack = false;
		sendPacky.type = PacketTypes.GROUP_DETAILS_MESSAGE;
		
		sendToClient_Reliable(sendSocket, recvSocket, leader.IP, sendPacky);
		
	}
	
	private void sendToTeamMem(String gname , Student leader, ArrayList<Student> team)
	{
		for(int i=0;i<team.size();i++)
		{
			Student stud = team.get(i);
			SelectedGroupPacket sgp = new SelectedGroupPacket(gname , leader, team);
			System.out.println("inside packet size is : "+Utilities.serialize(sgp).length);
			Packet p = new Packet(Utilities.seqNo, false, false, false, Utilities.serialize(sgp),
					false, false, true, false);
			p.type = PacketTypes.GROUP_DETAILS_MESSAGE;
			p.ack = false;
			
			sendToClient_Reliable(sendSocket, recvSocket, stud.IP, p);
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

	public void cleanBuffer()
	{
		int initTimeout = 1;
		try {
			initTimeout = recvSocket.getSoTimeout();
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
					recvSocket.setSoTimeout(initTimeout);
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
	
	public static void sendDatagramPacket(DatagramSocket sock,InetAddress ip, int port, Packet p)
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
}
