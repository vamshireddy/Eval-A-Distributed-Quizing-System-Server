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
	private ArrayList<Integer> attempts;
	
	public Probe(ArrayList<Student> students)
	{
		studentsList = students;
		serverProbePort = Utilities.recvProbePort;
		clientProbePort = Utilities.clientProbePort;
		
		attempts = new ArrayList<>();
		
		for(int i=0;i<studentsList.size();i++)
		{
			attempts.add(0);
		}
		
		try {
			sock = new DatagramSocket(null);
			// Set the socket to reuse the address
			sock.setReuseAddress(true);
			sock.bind(new InetSocketAddress(serverProbePort));
			sock.setSoTimeout(1);
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}
	public void run()
	{	
		while( studentsList.size() > 0 )
		{
			for( Student s : studentsList )
			{
				int studentIndex = studentsList.indexOf(s);
				
				Packet probe_pack = new Packet(seq_no++,false,false,true,null);
				byte[] byte_buff = Utilities.serialize(probe_pack);
				DatagramPacket probe = new DatagramPacket(byte_buff, byte_buff.length, s.IP, clientProbePort);
				
				while( attempts.get(studentIndex).intValue() < 3 )
				{
					try
					{
						sock.send(probe);
						
						byte[] buf = new byte[Utilities.MAX_BUFFER_SIZE];
						DatagramPacket recv_probe = new DatagramPacket(buf, buf.length);
						sock.receive(recv_probe);
						
						Packet p = (Packet)Utilities.deserialize(buf);
						if( p.probe_packet == true )
						{
							attempts.set(studentIndex, 0);
							break;
						}
						else
						{
							attempts.set(studentIndex, attempts.get(studentIndex).intValue()+1);
							if( attempts.get(studentIndex).intValue() >= 3 )
							{
								studentsList.remove(s);
								attempts.remove(studentIndex);
								System.out.println("Unable to reach student!");
								break;
							}
						}
					} 
					catch (IOException e) 
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
	}
}
