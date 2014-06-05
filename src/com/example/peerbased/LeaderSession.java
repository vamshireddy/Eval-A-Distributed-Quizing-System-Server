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
	//private ArrayList<Student>[] groups;
	public static boolean running = true;
	ArrayList<Student> studentsList;
    ArrayList<Leader> leaderRequests;
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
		broadCastLeaders();
		Utilities.cleanServerBuffer(recvSock);
	}
	private void broadCastLeaders() {
		
		for( int i=0;i<leaderRequests.size();i++ )
		{
			Leader l = leaderRequests.get(i);
			for(int j=0;j<studentsList.size();j++)
			{
				if( studentsList.get(j).uID.equals(l.id) ) 
				{
					// Add the leader name 
					l.name = studentsList.get(j).name;
		
					Student s = studentsList.get(j);
					sendLeaderMessage(s.IP);
					System.out.println("Sent leader group request to "+s.name+"!!!");
				}
			}
		}
		for( int i=0;i<studentsList.size();i++ )
		{
			Student s = studentsList.get(i);
			if( !leaderRequests.contains(new Leader(s.name, s.uID)) )
			{
				sendElectedLeaders(leaderRequests, s.IP);
				System.out.println("Sent online Leaders to "+s.uID+"!!!");
			}
		}
	}
	

	private void sendElectedLeaders(ArrayList<Leader> leaders, InetAddress IP) {
		LeaderPacket lp = new LeaderPacket();
		lp.selectedLeadersList = true;
		lp.leaders = leaders;
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
