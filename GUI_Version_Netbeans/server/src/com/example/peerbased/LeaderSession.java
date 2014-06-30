package com.example.peerbased;

import GUI.DisplayGroups;
import GUI.DisplayLeaders;
import GUI.LeaderSessionWait;
import GUI.waitPageGUI;
import com.mysql.jdbc.Util;
import java.awt.Dialog;
import java.io.IOException;
import java.io.Serializable;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;


class Interupter extends Thread
{
	private long time;
	
	public Interupter(long time)
	{
		this.time = time;
	}
	
	public void setTime(long time)
	{
		this.time = time;
	}
	
	public void run()
	{
		try {
			Thread.sleep(time);
			LeaderSession.running = false;
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}

class Leader implements Serializable
{
	/* This class is used for storing the leaders and publishing them to the non-leader students */
	public static final long serialVersionUID = 191249L;
	String name;
	String id;
	public Leader(String name, String id)
	{
		this.name = name;
		this.id = id;
	}
}

public class LeaderSession extends Thread{
	
	public static boolean running = true;
	ArrayList<Student> studentsList;
        ArrayList<Leader> leaderRequests;
        ArrayList<Group> groups;
        ArrayList<String> rcvdReqs;
	private int noOfGroups;
	private byte groupSize;
	private DatagramSocket sendSock;
	private DatagramSocket recvSock;
	private long time_limit;
	private long grp_sel_time_limit;
	
	public LeaderSession(
			ArrayList<Student> students, 
			DatagramSocket ssocket, 
			DatagramSocket rsocket,
			int nogrps,
			long leaderTime,
			long grpTime,
			byte grpSize)
	{
		studentsList = students;
		sendSock = ssocket;
		recvSock = rsocket;
		noOfGroups = nogrps;
		groupSize = grpSize;
		leaderRequests = new ArrayList<Leader>();
		time_limit = leaderTime;
		rcvdReqs = new ArrayList<>();
		grp_sel_time_limit = grpTime;
	}
	
	public void startLeaderSession()
	{
                
		/* Get leader requests */
                waitPageGUI wp = new waitPageGUI();
                wp.setText("Please wait untill leaders give their requests");
                
                LeaderSessionWait lsw = new LeaderSessionWait();
                lsw.setVisible(false);
                
                DisplayLeaders dl = null;
                
                wp.setVisible(true);
                
		while(true)
		{
                        
			runSession();
                        wp.setVisible(false);
                        lsw.setVisible(true);
			System.out.println("The Leader Request Session time has been completed Press 1 to Repeat 2 to continue");
			
                        while( lsw.getWaitStatus() == true )
                        {
                            try {
                                Thread.sleep(200);
                            } catch (InterruptedException ex) {
                                Logger.getLogger(LeaderSession.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                        int choice = lsw.getChoice();
			if( choice == 1 )
			{
                                lsw.resetWait();
                                lsw.setVisible(false);
                                wp.setVisible(true);
				continue;
			}
			else
			{
                                /*
                                    Teacher pressed continue. Check if all the leader requests are recieved.
                                */
                                if( leaderRequests.size() < noOfGroups )
                                {
                                    /*
                                        Have to continue again
                                    */
                                    lsw.showDialog();
                                    lsw.resetWait();
                                    lsw.setVisible(false);
                                    wp.setVisible(true);
                                    continue;
                                    
                                }
                                
                                lsw.setVisible(false);
				String leaders = printLeaders();
                                dl = new DisplayLeaders(leaders);
                                dl.setVisible(true);
				break;
			}
		}
                /*
                    Create a structure for groups
                */
		groups = new ArrayList<Group>(noOfGroups);
		broadCastLeaders();
		// Now receive the group name requests from leaders and the leader selection requests from the other students
		serveGroupnameAndGroupSelectionRequests(dl,lsw);
	}
	
	private void serveGroupnameAndGroupSelectionRequests(DisplayLeaders dl, LeaderSessionWait lsw)
	{
		int count = 0;
		while( true )
		{
			serveGroupsRequestsFunction();
                        dl.setVisible(false);
                        lsw.resetWait();
                        lsw.setLabel("Group selection session is done!");
                        lsw.setVisible(true);
			System.out.println("The Group serve time has been completed Press 1 to Repeat 2 to continue");
			
                        while( lsw.getWaitStatus() == true )
                        {
                            try {
                                Thread.sleep(200);
                            } catch (InterruptedException ex) {
                                Logger.getLogger(LeaderSession.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                        int choice = lsw.getChoice();
			if( choice == 1 )
			{
                                lsw.resetWait();
                                lsw.setVisible(false);
                                dl.setVisible(true);
				continue;
			}
			else
			{
                                lsw.setVisible(false);
                                String groupText = printGroups();
                                DisplayGroups dg = new DisplayGroups();
                                dg.setText(groupText);
                                dg.setVisible(true);
                                while( dg.getWaitStatus() == true )
                                {
                                    try {
                                        Thread.sleep(200);
                                    } catch (InterruptedException ex) {
                                        Logger.getLogger(LeaderSession.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                }
                                dg.setVisible(false);
				break;
			}
		}
	}
	
        private void serveGroupsRequestsFunction()
        {
            int count = 0;
           
            while( true )
            {
                    byte[] b = new byte[Utilities.MAX_BUFFER_SIZE];
                    DatagramPacket pack  =  new DatagramPacket(b, b.length);
                    try
                    {
                            recvSock.receive(pack);
                    }
                    catch (IOException e)
                    {
                            // TODO Auto-generated catch block
                            System.out.println("Timeout!");
                            count++;
                            if( count == grp_sel_time_limit )
                            {
                                    break;
                            }
                            continue;
                    }

                    InetAddress clientIP = pack.getAddress();

                    System.out.println("Got a packet outside!!!!");
                    Packet p = (Packet)Utilities.deserialize(b);
                    
                    int rcvdSeqNo = p.seq_no;
                    
                    if( p.type == PacketTypes.GROUP_NAME_SELECTION && p.ack == false )
                    {
                            System.out.println("Got a packet!!!!");
                            
                            GroupNameSelectionPacket gnsp = (GroupNameSelectionPacket)Utilities.deserialize(p.data);

                            if( gnsp.accepted == false )
                            {
                                    System.out.println("Its group name request!!");
                                    /*
                                     *  Check if the request has been already received
                                     */
                                    if( rcvdReqs.contains(gnsp.studentID) )
                                    {
                                            System.out.println("Redundant request!!");
                                            /*
                                             * Send reply
                                             */
                                            sendGroupSelectAck(true, clientIP, rcvdSeqNo);
                                            continue;
                                    }
                                    // Add the group name
                                    for(Group g : groups)
                                    {
                                            if( g.leaderID.equals(gnsp.studentID) )
                                            {
                                                    g.groupName = gnsp.groupName;
                                            }
                                    }
                                    rcvdReqs.add(new String(gnsp.studentID));
                                    /*
                                     * Send reply
                                     */
                                    sendGroupSelectAck(true, clientIP, rcvdSeqNo);
                            }
                    }
                    else if( p.type == PacketTypes.TEAM_SELECTION && p.ack == false )
                    {
                            TeamSelectPacket tsp = (TeamSelectPacket)Utilities.deserialize(p.data);
                            
                            //String clientName = tsp.name;
                            String clientID = tsp.ID;
                            String leaderID = tsp.leaderID;

                            if( tsp.accepted == false )
                            {
                                    /*
                                     * The tsp packet received from the student should always have accepted=false
                                     */
                                    if( rcvdReqs.contains(new String(clientID)) )
                                    {
                                            /*
                                             * Simply reject multiple requests from the same client.
                                             * This will happen only when the student's previous request was valid.
                                             */
                                            System.out.println("Redundant request!!");
                                            sendTeamSelectAck(true,clientIP, rcvdSeqNo);
                                            continue;
                                    }
                                    for(Group g : groups)
                                    {
                                            if( g.leaderID.equals(leaderID) )
                                            {
                                                    if( g.teamMembers.size() >= groupSize )
                                                    {
                                                            /*
                                                             * If the request is not valid, Dont add the student to the rcvdRequest list
                                                             * This way he/she will be allowed to select other leader
                                                             */
                                                            sendTeamSelectAck(false,clientIP, rcvdSeqNo);
                                                            break;
                                                    }
                                                    /*
                                                     * The below loop can be optimized!
                                                     * We can avoid iterating through the studentslist, byt creating the record on the go!
                                                     */
                                                    for( Student s: studentsList)
                                                    {
                                                            if( s.uID.equals(clientID) )
                                                            {
                                                                    g.teamMembers.add(s);
                                                                    /*
                                                                     * Add the student to the rcvdRequest list only if the student request is valid
                                                                     */
                                                                    rcvdReqs.add(new String(clientID));
                                                                    sendTeamSelectAck(true,clientIP, rcvdSeqNo);
                                                                    break;
                                                            }
                                                    }
                                            }
                                    }
                            }
                    }
                    else
                    {
                            continue;
                    }
	    }
        }
        
	private void sendGroupSelectAck(boolean flag, InetAddress ip, int rcvdSeqNo)
	{
                /*
                    Send an Ack
                */
		GroupNameSelectionPacket gsp = new GroupNameSelectionPacket(flag);

		Packet p = new Packet(rcvdSeqNo, PacketTypes.GROUP_NAME_SELECTION,true , Utilities.serialize(gsp));
                
		byte[] bytes = Utilities.serialize(p);
		
		System.out.println("\nSENTTT\n");
		
		DatagramPacket pack = new DatagramPacket(bytes, bytes.length, ip, Utilities.clientPort);
		try {
			sendSock.send(pack);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void sendTeamSelectAck(boolean flag, InetAddress ip, int rcvdSeqNo)
	{
		TeamSelectPacket tsp = new TeamSelectPacket(flag);
                
		Packet p = new Packet(rcvdSeqNo, PacketTypes.TEAM_SELECTION, true, Utilities.serialize(tsp));
                
		byte[] bytes = Utilities.serialize(p);
		
		DatagramPacket pack = new DatagramPacket(bytes, bytes.length, ip, Utilities.clientPort);
		try {
			sendSock.send(pack);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void broadCastLeaders()
	{
		/* 
		 * This function broadcast's the leaders to the students ( who are not leaders ) and sends a groupname request to leaders
		 */
            
                Iterator<Leader> leaderIter = leaderRequests.iterator();
            
		while( leaderIter.hasNext() )
		{
                        Iterator<Student> studIter = studentsList.iterator();
			
                        Leader l = leaderIter.next();
                        
                        boolean removeFlag = false;
                        
                        while( studIter.hasNext() )
			{
                                Student s = studIter.next();
                                
				if(s.uID.equals(l.id) ) 
				{
					// Add the leader name 
					l.name = s.name;
					// Make a new group entry
					groups.add(new Group("",s.name, s.uID, s));
					System.out.println("Added a group!");

					if( sendLeaderMessage(s.IP,s.name) == false )
                                        {
                                            /*
                                                Leader can't be reached.
                                                Remove his record in the leader list and also in the student list
                                            */
                                            studIter.remove();
                                            continue;
                                        }
                                        
					System.out.println("Sent leader group request to "+s.name+"!!!");
				}
                                
                                /*
                                    Remove from leader list
                                */
                                leaderIter.remove();
			}
		}
		for(Student s: studentsList)
                {
			boolean flag = true;
			for(Leader l : leaderRequests)
                        {
				System.out.println("I am inside...........ids are "+s.uID+" "+l.id);
				if(l.id.equals(s.uID))
				{
					flag = false;
				}
			} 
			if( flag == true )
			{
				sendElectedLeaders(leaderRequests, s.IP,s.name);
				System.out.println("Sent online Leaders to "+s.uID+"!!!");
			}
		}
	}
	

	private void sendElectedLeaders(ArrayList<Leader> leaders, InetAddress IP, String name) {
		/*
		 * Send the leaders to everyone except the leader students
		 */  
		// TODO can add strict check's at the client by sending the userName and ID. That will be validated at the client side
		LeaderPacket lp = new LeaderPacket();
		lp.LeadersListBroadcast = true; // This is flag to differentiate the list packet for non-leaders and group name selection packet for leaders
		lp.leaders = leaders;			// Add the leaders
		
		Packet sendPacky = new Packet(Utilities.seqNo,PacketTypes.LEADER_SCREEN_CHANGE,false ,Utilities.serialize(lp));
		
		if( UDPReliableHelperClass.sendToClientReliableWithGUI(sendSock, recvSock, IP, sendPacky, name);
	}
	
	
	/*
	 * Uses reliable UDP
	 */
	private boolean sendLeaderMessage(InetAddress IP,String name)
	{
		
		// TODO can add strict check's at the client by sending the userName and ID. That will be validated at the client side
		/*
		 * Create a leader packet, packet and send it to the client. Wait for the acknowldgement and try for Utilities.noOfAttempts times
		 * Sequence number is used to track the same packets and reply from the client to the resp packet which is sent by the server
		 * Sequence number is global and incremented for every client
		 */
		LeaderPacket lp = new LeaderPacket();
		/*
		 * Switch on the groupName request flag
		 */
		lp.grpNameRequest = true;
		
		
		Packet sendPacky = new Packet(Utilities.seqNo, PacketTypes.LEADER_SCREEN_CHANGE, false, Utilities.serialize(lp));
		/*
		 * For receiving the packet
		 */
		return UDPReliableHelperClass.sendToClientReliableWithGUI(sendSock, recvSock, IP, sendPacky, name);
	}
	
	
	public void runSession()
	{
		byte[] b;
                int count = 0;
		while( true )
		{
			b = new byte[Utilities.MAX_BUFFER_SIZE];
			DatagramPacket pack  =  new DatagramPacket(b, b.length);
			try {
				recvSock.receive(pack);
			} catch (IOException e) {
				
				System.out.println("Timeout!");
                                count++;
                                if( count >= time_limit )
                                {
                                    break;
                                }
				continue;
			}
			Packet p = (Packet)Utilities.deserialize(b);
			if( p.type == PacketTypes.LEADER_REQUEST && p.ack == false )
			{
				System.out.println("Received a request!");
				addRequestAndSendReply(p, pack.getAddress());
			}   
		}
	}
	public void addRequestAndSendReply(Packet p, InetAddress IPadd)
	{
                        int currentRcvdSeq = p.seq_no;
                        
			LeaderPacket lp = (LeaderPacket)Utilities.deserialize(p.data);	
			
			if( isPresentInLeaderReqs(lp.uID) )
			{
                                System.out.println("Already present");
				// If the student is present in the list, send him postive reply
				grantRequest(true, IPadd, currentRcvdSeq);
				return;
			}
			else
			{
                                System.out.println("Not present");
				if( leaderRequests.size() >= noOfGroups )
				{
                                        System.out.println("More than what is required");
					grantRequest(false, IPadd, currentRcvdSeq);
					return;
				}
				if( lp.granted == false )
				{
                                        System.out.println("True one adding");
					System.out.println("luid = "+lp.uID+" name : "+lp.uName);
					addRequest(new Leader(lp.uName, lp.uID));
					grantRequest(true, IPadd, currentRcvdSeq);
				}
				else
				{
                                        System.out.println("Erroneous packet");
					grantRequest(false, IPadd, currentRcvdSeq);
				}
			}
	}
	
	public void grantRequest(boolean flag, InetAddress IP, int rcvdSeqNo)
	{
		LeaderPacket lp = new LeaderPacket();
		lp.granted = flag;
                
		Packet p = new Packet(rcvdSeqNo,PacketTypes.LEADER_REQUEST,true, Utilities.serialize(lp));
		
		byte[] ba = Utilities.serialize(p);
		
		
		DatagramPacket pack = new DatagramPacket(ba, ba.length, IP, Utilities.clientPort);
		try
		{
			sendSock.send(pack);
		} 
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public boolean addRequest(Leader s)
	{
                if(leaderRequests.contains(s))
                {
                        return true;
                }
		System.out.println("User added!");
		leaderRequests.add(s);
		return false;
	}
	public String printLeaders()
	{
                String leaderString = "";
		for(Leader l : leaderRequests)
                {
			leaderString = leaderString + "Leader "+" : "+l.name+"\n";
		}
                return leaderString;
	}
        
        private String printGroups()
	{
                String grpString = "";
		System.out.println("The groups are : ");
		
                for( Group g : groups)
                {
			grpString = grpString + "GroupName: "+g.groupName+"\n"+"Leader: "+g.leaderName+"\n";
                        System.out.println("GroupName: "+g.groupName+"\n"+"Leader: "+g.leaderName+"\n");
			for(int j=0;j<g.teamMembers.size();j++)
			{
                                grpString = grpString + "Student : "+g.teamMembers.get(j).name+"\n";
				System.out.println("Student : "+g.teamMembers.get(j).name);
			}
                        grpString = grpString + "\n";
		}
                return grpString;
	}
	
	private boolean isPresentInLeaderReqs(String id)
	{
		for(Leader l : leaderRequests)
                {
                        if( l.id.equals(id) )
			{
				return true;
			}
		}
		return false;
	}
	
	public ArrayList<Leader> getLeaders()
	{
		return leaderRequests;
	}
	public ArrayList<Group> getGroups()
	{
		return groups;
	}
}
