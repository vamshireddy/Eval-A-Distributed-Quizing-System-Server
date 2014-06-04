package com.example.peerbased;

import java.io.IOException;
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


public class LeaderSession extends Thread{
	//private ArrayList<Student>[] groups;
	public static boolean running = true;
	private ArrayList<Student> studentsList;
	private ArrayList<String> leaderRequests;
	private int noOfGroups;
	private DatagramSocket sendSock;
	private DatagramSocket recvSock;
	private int leaderReqCount = 0;
	private long time_limit;
	
	public LeaderSession(ArrayList<Student> sl, DatagramSocket ssocket, DatagramSocket rsocket,int seq, int nogrps, long time)
	{
		sendSock = ssocket;
		recvSock = rsocket;
		studentsList = sl;
		noOfGroups = nogrps;
		//groups =  (ArrayList<Student>[])new ArrayList[noOfGroups];
		leaderRequests = new ArrayList<String>();
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
			System.out.println("Received a request!");
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
			if( leaderReqCount >= noOfGroups )
			{
				grantRequest(p,lp,false, IPadd);
				return;
			}
			
			if( lp.granted == false )
			{
				boolean flag = addRequest(new String(lp.uID));
				if( flag == false)
				{
					leaderReqCount++;
				}
				grantRequest(p,lp,true, IPadd);
			}
			else
			{
				grantRequest(p,lp,false, IPadd);
			}
	}
	
	public void grantRequest(Packet p, LeaderPacket lp, boolean flag, InetAddress IP)
	{
		lp.granted = flag;
		p.data = Utilities.serialize(lp);
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
	public boolean addRequest(String s)
	{
		for(int i=0;i<noOfGroups;i++)
		{
			if(leaderRequests.equals(s))
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
}
