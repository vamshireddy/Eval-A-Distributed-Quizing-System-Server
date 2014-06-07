package com.example.peerbased;

import java.io.IOException;
import java.io.Serializable;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;

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
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		LeaderSession.running = false;
	}
}

class Leader implements Serializable
{
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
	private int noOfGroups;
	private DatagramSocket sendSock;
	private DatagramSocket recvSock;
	private long time_limit;
	
	public LeaderSession(ArrayList<Student> students, DatagramSocket ssocket, DatagramSocket rsocket,int seq, int nogrps, long time)
	{
		studentsList = students;
		sendSock = ssocket;
		recvSock = rsocket;
		noOfGroups = nogrps;
		//groups =  (ArrayList<Student>[])new ArrayList[noOfGroups];
		leaderRequests = new ArrayList<Leader>();
		time_limit = time;

	}
	public void startLeaderSession()
	{
		/* Get leader requests */
		try {
			recvSock.setSoTimeout(1000);
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		while(true)
		{
			new Interupter(time_limit).start();
			runSession();
			
			System.out.println("The Leader Request Session time has been completed \nPress 2 to go ahead");
			int choice = Utilities.scan.nextInt();
			if( choice == 1 )
			{
				continue;
			}
			else
			{
				printLeaders();
				break;
			}
		}
		groups = new ArrayList<Group>(noOfGroups);
		
		broadCastLeaders();
		
		System.out.println("I am here !!!!!!!!!!!!1");
		// Now receive the group name requests from leaders and the leader selection requests from the other students
		
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
				if( count == 50 )
				{
					break;
				}
				continue;
			}
			System.out.println("Got a packet outside!!!!");
			Packet p = (Packet)Utilities.deserialize(b);
			if( p.group_name_selection_packet == true && p.seq_no == 321321321 )
			{
				System.out.println("Got a packet!!!!");
				GroupNameSelectionPacket gnsp = (GroupNameSelectionPacket)Utilities.deserialize(p.data);
				
				if( gnsp.accepted == false  )
				{
					System.out.println("Its group name request!!");
					// Add the group name
					int gsize = groups.size();
					for( int i=0;i<gsize;i++ )
					{
						Group g = groups.get(i);
						if( g.leaderID.equals(gnsp.studentID) )
						{
							g.groupName = gnsp.groupName;
						}
					}			
				}
			}
			else if( p.team_selection_packet == true && p.seq_no == 321321123 )
			{
				TeamSelectPacket tsp = (TeamSelectPacket)Utilities.deserialize(p.data);
				int gsize = groups.size();
				for( int i=0;i<gsize;i++ )
				{
					Group g = groups.get(i);
					if( g.leaderID.equals(tsp.leaderID) )
					{
						for(int j=0;j<studentsList.size();j++)
						{
							Student s = studentsList.get(j);
							if( s.uID.equals(tsp.ID) )
							{
								g.teamMembers.add(s);
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
	private void broadCastLeaders() {
		/* 
		 * This function broadcast's the leaders to the students( who are not leaders ) and sends a groupname request to leaders
		 */
		
		int lsize = leaderRequests.size();
		int ssize = studentsList.size();
		
		for( int i=0;i<lsize;i++ )
		{
			Leader l = leaderRequests.get(i);
			for(int j=0;j<ssize;j++)
			{
				if( studentsList.get(j).uID.equals(l.id) ) 
				{
					// Add the leader name 
					l.name = studentsList.get(j).name;
		
					Student s = studentsList.get(j);
					// Make a new group entry 
					groups.add(new Group("",s.name, s.uID, s));
					System.out.println("Added a group!");
					
					
					sendLeaderMessage(s.IP);
					System.out.println("Sent leader group request to "+s.name+"!!!");
				}
			}
		}
		for( int i=0;i<ssize;i++ )
		{
			Student s = studentsList.get(i);
			boolean flag = true;
			for( int j=0;j<lsize;j++ )
			{
				Leader l = leaderRequests.get(j);
				System.out.println("I am inside...........ids are "+s.uID+" "+l.id);
				if(l.id.equals(s.uID))
				{
					flag = false;
				}
			} 
			if( flag == true )
			{
				sendElectedLeaders(leaderRequests, s.IP);
				System.out.println("Sent online Leaders to "+s.uID+"!!!");
			}
		}
	}
	

	private void sendElectedLeaders(ArrayList<Leader> leaders, InetAddress IP) {
		/*
		 * Send the leaders to everyone except the leader students
		 */
		// TODO can add strict check's at the client by sending the userName and ID. That will be validated at the client side
		LeaderPacket lp = new LeaderPacket();
		lp.LeadersListBroadcast = true; // This is flag to differentiate the list packet for non-leaders and group name selection packet for leaders
		lp.leaders = leaders;			// Add the leaders
		
		Packet p = new Packet(121441, false, false, false, Utilities.serialize(lp), false, true);
		byte[] bytes = Utilities.serialize(p);
		DatagramPacket pack = new DatagramPacket(bytes, bytes.length, IP, Utilities.clientPort);
		
		try {
			sendSock.send(pack);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private void sendLeaderMessage(InetAddress iP){
		
		// TODO can add strict check's at the client by sending the userName and ID. That will be validated at the client side
		LeaderPacket lp = new LeaderPacket();
		lp.grpNameRequest = true;
		Packet p = new Packet(121221, false, false, false, Utilities.serialize(lp), false, true);
		byte[] bytes = Utilities.serialize(p);
		DatagramPacket pack = new DatagramPacket(bytes, bytes.length, iP, Utilities.clientPort);
		
		try {
			sendSock.send(pack);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	public void runSession()
	{
		byte[] b;
		while( running == true )
		{
			b = new byte[Utilities.MAX_BUFFER_SIZE];
			DatagramPacket pack  =  new DatagramPacket(b, b.length);
			try {
				recvSock.receive(pack);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.out.println("Timeout!");
				continue;
			}
			Packet p = (Packet)Utilities.deserialize(b);
			if( p.leader_req_packet == true && p.seq_no == 111222 )
			{
				System.out.println("Received a request!");
				addRequestAndSendReply(p, pack.getAddress());
			}
		}
	}
	public void addRequestAndSendReply(Packet p, InetAddress IPadd)
	{
			LeaderPacket lp = (LeaderPacket)Utilities.deserialize(p.data);	
			
			if( leaderRequests.contains(lp.uID) )
			{
				// If the student is present in the list, send him postive reply
				grantRequest(true, IPadd);
				return;
			}
			else
			{
				if( leaderRequests.size() >= noOfGroups )
				{
					grantRequest(false, IPadd);
					return;
				}
				if( lp.granted == false )
				{
					System.out.println("luid = "+lp.uID+" name : "+lp.uName);
					addRequest(new Leader(lp.uName, lp.uID));
					grantRequest(true, IPadd);
				}
				else
				{
					grantRequest(false, IPadd);
				}
			}
	}
	
	public void grantRequest(boolean flag, InetAddress IP)
	{
		LeaderPacket lp = new LeaderPacket();
		lp.granted = flag;
		Packet p = new Packet(121441, false, false, false, Utilities.serialize(lp), false, true);
		p.leader_req_packet = true;
		
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
		for(int i=0;i<noOfGroups;i++)
		{
			if(leaderRequests.contains(s))
			{
				return true;
			}
		}
		System.out.println("User added!");
		leaderRequests.add(s);
		return false;
	}
	public void printLeaders()
	{
		for(int i=0;i<leaderRequests.size();i++)
		{
			System.out.println("Leader UserID = "+leaderRequests.get(i));
		}
	}
	public ArrayList<Leader> getLeaders()
	{
		return leaderRequests;
	}
}
