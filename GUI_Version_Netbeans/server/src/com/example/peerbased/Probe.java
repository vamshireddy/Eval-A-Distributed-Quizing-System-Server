package com.example.peerbased;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.ArrayList;

public class Probe extends Thread{
	
	private static int seq_no = 1;
	private DatagramSocket sock;
	private int serverProbePort;
	private int clientProbePort;
	private ArrayList<Student> studentsList;
	private int attempts = 0;
	
	public Probe(ArrayList<Student> students)
	{
		studentsList = StudentListHandler.getList();
		serverProbePort = Utilities.servProbePort;
		clientProbePort = Utilities.clientProbePort;

		try {
			sock = new DatagramSocket(null);
			// Set the socket to reuse the address
			sock.setReuseAddress(true);
			sock.bind(new InetSocketAddress(serverProbePort));
			sock.setSoTimeout(3000);
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}
	public void run()
	{	
		/* Continue the probing untill there are no students, or end of an application */
		while( studentsList.size() > 0 )
		{
			/* In each iteration, go through the entire students list and delete their entries, if the students are not answering */
			java.util.Iterator<Student> iter = studentsList.iterator();
			
			while( iter.hasNext() )
			{	
				Student s = iter.next();
				//System.out.println("The student index is "+studentIndex+" and attempts = "+attempts.get(studentIndex).intValue());
				Packet probe_pack = new Packet(seq_no++,false,false,true,null);
				byte[] byte_buff = Utilities.serialize(probe_pack);
				DatagramPacket probe = new DatagramPacket(byte_buff, byte_buff.length, s.IP, clientProbePort);
				
				/* Clear the attempts to zero */
				attempts = 0;
				
				while( attempts <= 3 )
				{
					System.out.println("Started probing!");
					try
					{
						sock.send(probe);
						System.out.println("Probe sent!");
						byte[] buf = new byte[Utilities.MAX_BUFFER_SIZE];
						DatagramPacket recv_probe = new DatagramPacket(buf, buf.length);
						attempts++;
						
						
						sock.receive(recv_probe);
						Packet p = (Packet)Utilities.deserialize(buf);
						
						if( p.probe_packet == true )
						{
							break;
						}
						else
						{
							if( attempts >= 3 )
							{
								iter.remove();
								System.out.println("Unable to reach student! "+s.IP);
								break;
							}
						}
					} 
					catch (IOException e) 
					{
						if( attempts >= 3 )
						{
							iter.remove();
							System.out.println("Unable to reach student! "+s.IP);
							break;
						}
					}
				}
			}
		}
	}
}
