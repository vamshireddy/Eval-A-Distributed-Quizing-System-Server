package com.example.peerbased;
import GUI.LevelFrame;
import GUI.MultipleChoiceFrame;
import GUI.OneWordFrame;
import GUI.QuestionTimeExtension;
import GUI.QuestionWaitPage;
import GUI.QuizParametersGUI;
import GUI.QuizStartPage;
import GUI.QuizStats;
import GUI.QuizTestStartPage;
import GUI.ResponseStatistics;
import GUI.ResponseWait;
import GUI.TrueOrFalseFrame;
import GUI.waitPageGUI;
import QuizPackets.*;
import QuizPackets.QuizInterfacePacket;
import com.mysql.jdbc.Connection;
import com.mysql.jdbc.PreparedStatement;
import static com.sun.corba.se.impl.util.Utility.printStackTrace;
import java.io.IOException;
import java.net.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.sql.Date;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;


public class Quiz extends Thread
{
    
        //public static Quiz staticVar;
	/* Classroom Parameters */
	private byte noOfStudents;
	private byte noOfGroups;
	private byte noOfStudentsInGroup;
	private byte noOfRounds;
	private ArrayList<Student> studentsList;    // Students List stores the records of all the students logged in
	private ArrayList<String> leaderList;       // Leaders list will store the leader ID's requests.
        private ArrayList<String> answeredStuds;
	private ArrayList<Group> groups;            // Group contains a leader and team members
	private int questionTimelimitInSeconds;     // Question asking time limit which is assigned by teacher
	private int AnswerTimeLimitInSeconds;       // Answer time limit for the Questions asked
	
	/* Teacher Parameters */
	private String subject;                     // Subject of the teacher
	private String teacherName;                 // Teacher Name
	private String standard;                    // Teacher standard

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
        
        /*
            GUI Pages
        */
	private QuestionWaitPage qwp;
        private MultipleChoiceFrame mcf;
        private TrueOrFalseFrame tff;
        private OneWordFrame owf;
        private LevelFrame lf;
        private ResponseWait rw;
        private QuestionTimeExtension ext;
        
	/* Constructor */
	public Quiz(String subject,String teacherName, Connection c)
	{
            
                /*
                    GUI FRAME INITIALIZATION
                */
                qwp = new QuestionWaitPage();
                mcf = new MultipleChoiceFrame();
                tff = new TrueOrFalseFrame();
                owf = new OneWordFrame();
                ext = new QuestionTimeExtension();
                lf = new LevelFrame();
                rw = new ResponseWait();
                ext = new QuestionTimeExtension();
                qwp = new QuestionWaitPage();
                /*
                    GUI FRAME INITIALIZATION
                */
                
                /*
                Create a GUI thread
                */
                System.out.println("waiting for parameters");
                
                QuizStartPage qsp = new QuizStartPage(this);
                qsp.setVisible(true);
                
                
                while( qsp.fieldsEntered == false )
                {
                    try
                    {
                        /*
                            Wait until the user enters the parameters
                        */
                        Thread.sleep(200);
                        
                    } catch (InterruptedException ex) {
                        Logger.getLogger(Quiz.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                qsp.setVisible(false);
                
                /*
                    Get the parametere from the GUI and set it
                */
                setParameters(qsp.getStudents(), qsp.getGroups(), qsp.getStudentsInGroup(), qsp.getStandard());
                
                System.out.println("Got parameters studets:"+noOfStudents+"\n");
		/* Initialize the parameters which are passed from the previous class */
		this.con = c;
		this.teacherName = teacherName;
		this.subject = subject;
		this.studentsList = StudentListHandler.getList();
		this.questions = new ArrayList<>();
		// Set the student list hadler so that all classes can access it!
		
		
		try {
			sendSocket = new DatagramSocket();
			recvSocket = new DatagramSocket(null);
			// Set the socket to reuse the address
			recvSocket.setReuseAddress(true);
			recvSocket.bind(new InetSocketAddress(Utilities.servPort));
			// Set the broadcast IP, H
			broadcastIP = InetAddress.getByName("192.168.1.255");
                        
                        recvSocket.setSoTimeout(500);
                        
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
                qsp.setVisible(false);
	}
        
        public void setParameters(byte students, byte groups, byte studentsingroup, String standard)
        {
                System.out.println(" setting students  = "+students);
                noOfGroups = groups;
                noOfStudents = students;
                noOfStudentsInGroup = studentsingroup;
                this.standard = standard;
        }
	
        private InetAddress getBroadcastIP()
	{
		/* Get it from the interface */
		// To be filled
		return null;
	}
	
        
	/* This is the method which performs the crucial function of this class */
	public void startQuizSession()
	{
                /*
                    Show the interface for teacher
                */
                waitPageGUI wp = null;
                if( studentsList.size() < noOfStudents )
                {
                     wp = new waitPageGUI();
                     wp.setVisible(true);
                }
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
                if( wp != null )
                { 
                    wp.setVisible(false);
                }
                

                QuizParametersGUI qp = new QuizParametersGUI();
                qp.setVisible(true);
                /*
                    Loop untill the values are entered
                */
                while( qp.fieldsEntered == false )
                {
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(Quiz.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                /*
                    Got the values
                */
                qp.setVisible(false);
                
		long time_limit = qp.leaderSessionTime;
		long grp_sel_time = qp.groupSelectionTime;
                
                System.out.println("values are "+time_limit+" "+grp_sel_time);
                
                /* GUI PART */
                if( wp == null )
                {
                    wp = new waitPageGUI();
                    wp.setText("Please wait");
                    wp.setVisible(true);
                }
                else
                {
                    wp.setVisible(true);
                    wp.setText("Please wait");
                }
                /* GUI PART */
		
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
		
                wp.setText("Sent Configuration Parameters to everyone in the network!");
		System.out.println("-----------------------------------------------\nSent Configuration Parameters to everyone in the network!");
		
                /*
		 * Start leader Session
		 */
		cleanBuffer();
		/* Clean the server buffer before starting the leader session, so that all the previous unnecessary packets are discarded! */
		
                wp.setVisible(false);
		LeaderSession ls = new LeaderSession(studentsList, sendSocket, recvSocket, noOfGroups, time_limit, grp_sel_time, noOfStudentsInGroup);
		ls.startLeaderSession();
		
		/*
		 *  Leader session ends
		 */
		cleanBuffer();
		
		/*
		 * Get the data structure of groups from the leader session object
		 */
		groups = ls.getGroups();
		
                
                
//		System.out.println("\nEnter any key to Start the quiz\n");
//		
//		int a = Utilities.scan.nextInt();

		sendGroupsToStudents(groups);
                /*
		 * Quiz Starts here
		 * Get the parameters required for Quiz.
		 */
//		System.out.println("Enter the number of rounds : ");
//		noOfRounds = Utilities.scan.nextByte();
//		System.out.println("Enter the time for asking question: ");
//		questionTimelimitInSeconds = Utilities.scan.nextInt();
//		System.out.println("Enter the time for answering the question: ");
//		AnswerTimeLimitInSeconds = Utilities.scan.nextInt();
                /*
                    GUI
                */
                QuizTestStartPage qtsp = new QuizTestStartPage();
                qtsp.setVisible(true);
                while( qtsp.getWaitStatus() == true )
                {
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(Quiz.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                qtsp.setVisible(false);
                noOfRounds = qtsp.getRounds();
                questionTimelimitInSeconds = qtsp.getQuesTime();
                AnswerTimeLimitInSeconds = qtsp.getAnsTime();
                
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
		 * Clean the server buffer so that all the previous packets which are accumulated in the buffer are destroyed!
		 */
                 
 
                qwp.setVisible(true);
            
		System.out.println("Cleaning the server buffer\n");
		cleanBuffer();
		
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
                                qwp.setVisible(true);
                                
				HashMap<String,String> questionFormed = receiveAndSendQuestions(questionSeqNo,g);
                                
                                qwp.setVisible(false);
                                
				System.out.println("\nRecvd question!!\n");
                                
                                
				if( questionFormed == null )
				{
					/*
					 * No question is formed, make next group as active
					 */
					System.out.println("OMG!!!!!!!!!!!!!!!!!!!!!!!!\n\n\n");
                                        qwp.setVisible(true);
					continue;
				}
                                
                                
				HashMap<String,Double> stats = getResponses(questionSeqNo, questionFormed);  
				/*
				 * Calculate the marks according to the level
				 */
				calculateMarks(answeredStuds,g);
                                
                                /*
                                    Print stats to teacher
                                */
                                
                                showQuestionStats(questionFormed,stats);
                                
				questionSeqNo++;
				cleanBuffer();
			}
		}
                
                QuizStats qstat = new QuizStats();
                qstat.setVisible(true);
                /*
                    Quiz is completed. Send results
                */
                Packet packy = new Packet(Utilities.seqNo, false, true, false,null, true);
                packy.type = PacketTypes.QUIZ_END_PACKET;
                packy.ack = false;
                
                for(Student s: studentsList)
		{
			/*
			 * For sending the packet
			 */
                        QuizResultPacket qrp = new QuizResultPacket(s.noOfQuestions, s.noOfAnswers, s.marks);
			packy.seq_no = Utilities.seqNo;
                        packy.data = Utilities.serialize(qrp);
			/*
			 * For receiving the packet
			 */
			System.out.println("\nI am sending to "+s.name+"\n");
			if( sendToClient_Reliable(sendSocket, recvSocket, s.IP, packy) == -1 )
			{
				System.out.println("Client couldn't connect");
				System.exit(0);
			}
                        /*
                            Update the record
                        */
                        try
                        {
                            String query = "insert into student_performance values ( '"+s.uID+"','"+s.name+"','"+subject+"','"+standard+"',"
                                    +"CURDATE()"+",'"+s.noOfQuestions+"','"+s.noOfAnswers+"','"+s.marks+"')";
                            System.out.println("Query is "+query);
                            PreparedStatement ps  = (PreparedStatement)con.prepareStatement(query);
                            ps.executeUpdate();  
                            System.out.println("Student "+s.name+" record stored succesfully");
                        } catch (SQLException ex) {
                            Logger.getLogger(Quiz.class.getName()).log(Level.SEVERE, null, ex);
                            System.out.println("Error in storing the questions");
                        }
                }
                /*
                    Show bar chart about groups.
                */
                
                /*
                    Show the Home Page GUI
                */
	}
        
        private void showQuestionStats(HashMap<String,String> questionFormed, HashMap<String,Double> stats)
        {
            /*
                Display GUI 
            */
            
            ResponseStatistics statsGUI = new ResponseStatistics();

            statsGUI.InitChart(questionFormed.get("question"), stats, Byte.parseByte(questionFormed.get("type")));

            statsGUI.setVisible(true);       
            
            while( statsGUI.getWaitStatus() == true )
            {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException ex) {
                    Logger.getLogger(Quiz.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
            statsGUI.setVisible(false);
            statsGUI.reset();
        }
	
	private void calculateMarks(ArrayList<String> studs, Group g)
	{	
		/*
                    Allot marks to the people who answered the question
                */
		for(int i=0;i<studentsList.size();i++)
		{
			Student s = studentsList.get(i);
			for(int j=0;j<studs.size();j++)
			{
				if( s.uID.equals(studs.get(j)) )
				{
					s.noOfAnswers++;
                                        // +2 for those who answered
					s.marks = s.marks + 2;
					break;
				}
			}
		}
                /*
                    Allot marks to the group who asked the question
                */
                
                g.leaderRecord.marks = g.leaderRecord.marks + questionLevel;
                
                for(Student s : g.teamMembers)
                {
                    s.marks = s.marks + questionLevel;
                }
                /*
                    Print
                */
                
		for(int i=0;i<studentsList.size();i++)
		{
			Student s = studentsList.get(i);
			System.out.println("Student name : "+s.name+"  Marks : "+s.marks+" Attempted : "+s.noOfQuestions+" Answered correct :"+s.noOfAnswers);
		}
	}
	
	private HashMap<String,Double> getResponses(int qseq_no, HashMap<String,String> questionFormed)
	{
                /*
                    Start the GUI for the reponse listening
                */
                
                HashMap<String,Double> stats = new HashMap<>();
                
                String answer = questionFormed.get("answer");
                
                byte type = Byte.parseByte(questionFormed.get("type"));
                
                /*
                    Create an array to collect responses
                */
                
                int responseCount[] = null;
                String responseOptions[] = null;
                
                if( answer == null )
                {
                    printStackTrace();
                    System.exit(0);
                }
                
                switch(type)
                {
                    case 1 :    responseCount = new int[4];
                                responseOptions = new String[4];
                                for(int i=0;i<4;i++)
                                {
                                    responseOptions[i] = questionFormed.get("option"+(i+1));
                                    responseCount[i] = 0;
                                }
                                break;
                    case 2 :    responseCount = new int[2];
                                responseOptions = new String[2];
                                for(int i=0;i<2;i++)
                                {
                                    responseOptions[i] = questionFormed.get("option"+(i+1));
                                    responseCount[i] = 0;
                                }
                                break;
                    case 3 :    responseCount = new int[2];
                                responseOptions = new String[2];

                                responseOptions[0] = questionFormed.get("option1");
                                responseCount[0] = 0;
                                /*
                                    This indicated the incorrect answered percentage
                                */
                                responseOptions[1] = "Incorrect";
                                responseCount[1] = 0;
                                break;
                }
                
                /*
                    Initialise them
                */
                
                
            
                rw.setVisible(true);
		/*
		 * Arraylist for storing the ID's of the students who correctly responded to the question which is sent by the server
		 */
		ArrayList<String> answeredStudIDs = new ArrayList<>();
		
		int count = 0;
		
		byte[] b  = new byte[Utilities.MAX_BUFFER_SIZE];
		DatagramPacket p = new DatagramPacket(b, b.length);
		
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
                                        
                                        /*
                                            Collect response statistics
                                        */
                                        
                                        String reply = rp.answer;
                                        
                                        System.out.println("\n\nThe Reply is : "+reply+"\n\n");
                                        
                                        switch( type )
                                        {
                                            case 1 :   for(int i=0;i<4;i++)
                                                       {
                                                           if( responseOptions[i].equals(reply) )
                                                           {
                                                               responseCount[i]++;
                                                           }
                                                       }
                                                       break;
                                            case 2  :  for(int i=0;i<2;i++)
                                                       {
                                                           if( responseOptions[i].equals(reply) )
                                                           {
                                                               responseCount[i]++;
                                                           }
                                                       }
                                                       break;
                                            case 3  :  
                                                       if( responseOptions[0].equals(reply) )
                                                       {
                                                            responseCount[0]++;
                                                       }
                                                       else
                                                       {
                                                            responseCount[1]++;
                                                       }
                                                       break;
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
                
                switch( type )
                {
                    case 1 :   for(int i=0;i<4;i++)
                               {
                                   double a = ((float)responseCount[i]/studentsList.size())*100;
                                   stats.put(responseOptions[i],a);
                               }
                               break;
                        
                    case 2  :  for(int i=0;i<2;i++)
                               {
                                   double a = ((float)responseCount[i]/studentsList.size())*100;
                                   stats.put(responseOptions[i],a);
                               }
                               break;
                        
                    case 3  :  for(int i=0;i<2;i++)
                               {
                                   double a = ((float)responseCount[i]/studentsList.size())*100;
                                   stats.put(responseOptions[i],a);
                               }
                               break;
                }
                
                /*
                    Set class variable for answered students
                */
                answeredStuds = answeredStudIDs;
                
                rw.setVisible(false);
		return stats;
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
	
        /*
            This method will receive the question from the client, Authenticates it and sends the question to everyone except the group which asked the question
        */
	private HashMap<String,String> receiveAndSendQuestions(int qseq_no,Group currentGrp)
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
//							System.out.println("None of the questions are formed from current group.\nPress any key to give a chance again to the same group");
//							String useless_input = Utilities.scan.nextLine();
//							/*
//							 * Ask the time limit again for the question formation and give them a chance again
//							 */
//							System.out.println("Please enter the new timelimit for forming the question: ");
//							questionTimelimitInSeconds = Utilities.scan.nextInt();
//                                                    
//							count = 0;
                                                            
                                                        
                                                        ext.setVisible(true);
                                                        
                                                        while( ext.getWaitStatus() == true )
                                                        {
                                                            try {
                                                                Thread.sleep(200);
                                                            } catch (InterruptedException ex) {
                                                                Logger.getLogger(Quiz.class.getName()).log(Level.SEVERE, null, ex);
                                                            }
                                                        }
                                                        ext.reset();
                                                        if( ext.getSelectedButton() == 2 )
                                                        {
                                                            /*
                                                                Give a chance to the next team
                                                            */
                                                            
                                                            ext.setVisible(false);
                                                            return null;
                                                        }
                                                        System.out.println("REPEATING");
                                                        /*
                                                            Repeat the session
                                                        */
                                                        ext.setVisible(false);
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
				Packet ackPacket = new Packet(PacketSequenceNos.SEQ_NOT_NEEDED, false, false, false, packy.data);
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
			
                        HashMap<String,String> questionFormed = new HashMap<>();
                        
			if( packy.seq_no == PacketSequenceNos.QUIZ_QUESTION_PACKET_CLIENT_SEND && packy.quizPacket == true )
			{
				QuestionPacket qp = (QuestionPacket)Utilities.deserialize(packy.data);
				
                                /*
                                    Client should set this flag to false
                                */
				if( qp.questionAuthenticated == false )
				{
                                        int a = -1;
                                        /*
                                            Add to question to the hashmap
                                        */
                                        questionFormed.put("question",qp.question);
                                        
					if( qp.questionType == 1 )
					{
                                                /*
                                                    Add the type to the HashMap
                                                */
                                                
                                                questionFormed.put("type","1");
                                                
                                                /*
                                                    Create a new JFrame for Multiple Choice questions and set it.
                                                */
						System.out.println("---------------------------Question-------------------------------\n"+qp.question);
						for(int i=0;i<4;i++)
						{
							System.out.println((i+1)+" : "+qp.options[i]);
                                                        /*
                                                            Add options to the Hasmap
                                                        */
                                                        questionFormed.put("option"+(i+1),qp.options[i]);
						}
                                                
                                                /*
                                                    Add answer to HashMap
                                                */
                                                questionFormed.put("answer", qp.correctAnswerOption);
                                                
						System.out.println("Answer is : "+qp.correctAnswerOption+"\n\n");
                                                
                                                mcf.setFields(qp.question, qp.options[0], qp.options[1], qp.options[2], qp.options[3], qp.correctAnswerOption);
                                                mcf.setVisible(true);
                                                
                                                /*
                                                    Wait for the button event
                                                */
                                                
                                                while( mcf.getWaitStatus() == true )
                                                {
                                                     try {
                                                        Thread.sleep(200);
                                                    } catch (InterruptedException ex) {
                                                        Logger.getLogger(Quiz.class.getName()).log(Level.SEVERE, null, ex);
                                                    }
                                                }
                                                
                                                a = mcf.getSelectedOption();
                                                mcf.reset();
					}
					else if( qp.questionType == 2 )
					{
                                            
                                                   
                                                /*
                                                    Add the question type to the HashMap
                                                */
                                                
                                                questionFormed.put("type","2");
                                                
						System.out.println("---------------------------Question-------------------------------\n"+qp.question);
						for(int i=0;i<2;i++)
						{
							System.out.println((i+1)+" : "+qp.options[i]);
                                                        
                                                        /*
                                                            Add options to hashmap
                                                        */
                                                        questionFormed.put("option"+(i+1),qp.options[i]);
                                                        
						}
						System.out.println("Answer is : "+qp.correctAnswerOption+"\n\n");
                                                
                                                /*
                                                    Add answer to the hashmap
                                                */
                                                questionFormed.put("answer",qp.correctAnswerOption);
                                                
                                                tff.setFields(qp.question, qp.options[0], qp.options[1], qp.correctAnswerOption);
                                                tff.setVisible(true);
                                                
                                                /*
                                                    Wait for the button event
                                                */
                                                
                                                while( tff.getWaitStatus() == true )
                                                {
                                                    try {
                                                        Thread.sleep(200);
                                                    } catch (InterruptedException ex) {
                                                        Logger.getLogger(Quiz.class.getName()).log(Level.SEVERE, null, ex);
                                                    }
                                                }
                                                
                                                a = tff.getSelectedOption();
                                                tff.reset();
					}
					else if( qp.questionType == 3 )
					{
                                            
                                                /*
                                                    Add the question type to the HashMap
                                                */
                                                
                                                questionFormed.put("type","3");
                                            
						System.out.println("---------------------------Question-------------------------------\n"+qp.question);
						System.out.println("Answer is : "+qp.correctAnswerOption+"\n\n");
                                                
                                                questionFormed.put("option1",qp.correctAnswerOption);
                                                /*
                                                    Add the answer to hashmap
                                                */
                                                questionFormed.put("answer",qp.correctAnswerOption);
                                                
                                                owf.setFields(qp.question, qp.correctAnswerOption);
                                                owf.setVisible(true);
                                                
                                                /*
                                                    Wait for the button event
                                                */
                                                while( owf.getWaitStatus() == true )
                                                {
                                                     try {
                                                        Thread.sleep(200);
                                                    } catch (InterruptedException ex) {
                                                        Logger.getLogger(Quiz.class.getName()).log(Level.SEVERE, null, ex);
                                                    }
                                                }
                                                
                                                a = owf.getSelectedOption();
                                                owf.reset();
					}
					
                                        owf.setVisible(false);
                                        tff.setVisible(false);
                                        mcf.setVisible(false);
                                        
//					System.out.println("Is the question valid ? Press 1 for accepting it, 2 for rejecting it");
//					int a = Utilities.scan.nextInt();
					
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
                                                
                                                lf.setVisible(true);
                                                while( lf.getWaitStatus() == true )
                                                {
                                                    try {
                                                        Thread.sleep(200);
                                                    } catch (InterruptedException ex) {
                                                        Logger.getLogger(Quiz.class.getName()).log(Level.SEVERE, null, ex);
                                                    }
                                                }
                                                
                                                byte level = lf.getSelectedChoice();
                                                lf.reset();
//						System.out.println("Enter the level of the question : ");
//						byte level = Utilities.scan.nextByte();
						lf.setVisible(false);
                                                
						questionLevel = level;
					
                                                
                                                /*
                                                    Display packet sending wait page 
                                                */
                                               
                                                
                                                try
                                                {
                                                    PreparedStatement ps  = (PreparedStatement)con.prepareStatement("insert into question_table values ( '"+qp.question+"','"+qp.correctAnswerOption+"',"+"CURDATE()"+",'"+subject+"',"+level+","+standard+")");
                                                    ps.executeUpdate();  
                                                    System.out.println("Question stored succesfully in DB");
                                                } catch (SQLException ex) {
                                                    Logger.getLogger(Quiz.class.getName()).log(Level.SEVERE, null, ex);
                                                    System.out.println("Error in storing the questions");
                                                }
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
						return questionFormed;
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
		for(Student stud : team)
		{
			SelectedGroupPacket sgp = new SelectedGroupPacket(gname , leader, team);
			System.out.println("inside packet size is : "+Utilities.serialize(sgp).length);
			Packet p = new Packet(Utilities.seqNo, false, false, false, Utilities.serialize(sgp),
					false, false, true, false);
			p.type = PacketTypes.GROUP_DETAILS_MESSAGE;
			p.ack = false;
			
			sendToClient_Reliable(sendSocket, recvSocket, stud.IP, p);
		}
	}

	public void cleanBuffer()
	{
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