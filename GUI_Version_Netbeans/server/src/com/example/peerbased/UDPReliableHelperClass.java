/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.example.peerbased;

import static com.example.peerbased.Quiz.rps;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author vamshi
 */
public class UDPReliableHelperClass {
    
    
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
        
        public static void cleanBuffer(DatagramSocket socket)
	{
		while(true)
		{
			byte[] b  = new byte[Utilities.MAX_BUFFER_SIZE];
			DatagramPacket p = new DatagramPacket(b, b.length);
			try {
				socket.receive(p);
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
        
        
	public static int sendToClient_Reliable(DatagramSocket sendSock, DatagramSocket recvSock, InetAddress IP, Packet packy)
	{
		boolean ackFlag = false;
		
                System.out.println("Sending packet with sequence number : "+Utilities.seqNo);
                
		byte[] b  = new byte[Utilities.MAX_BUFFER_SIZE];
		DatagramPacket recvPacky = new DatagramPacket(b, b.length);
		
		for(int j=0;j<Utilities.noOfAttempts;j++)
		{
			System.out.println("Attempt "+(j+1));
			UDPReliableHelperClass.sendDatagramPacket(sendSock, IP, Utilities.clientPort, packy);
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
		return Utilities.FAIL;
	}
        
        public static boolean sendToClientReliableWithGUI(DatagramSocket sendSocket, DatagramSocket recvSocket, InetAddress IP, Packet packy, String name)
        {
                while( UDPReliableHelperClass.sendToClient_Reliable(sendSocket, recvSocket, IP, packy) == Utilities.FAIL )
                {
                        System.out.println("Client couldn't connect");
                        /*
                            Display an interface to teacher
                        */
                        rps.setText("Client "+name+" couldn't be reached!");
                        rps.setVisible(true);
                        while( rps.getWaitStatus() == true )
                        {
                            try {
                                Thread.sleep(200);
                            } catch (InterruptedException ex) {
                                Logger.getLogger(Quiz.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                        /*
                            Get the choice
                        */
                        int choice = rps.getChoice();
                        rps.reset();

                        if( choice == 1 )
                        {
                            rps.setVisible(false);
                            continue;
                        }
                        else
                        {
                            /*
                                If we fail to send the packet, we have to increase the seq no outside the func. If it succeeds then, it will set in the function itself.
                            */
                            Utilities.seqNo++;
                            rps.setVisible(false);
                            return false;
                        }
                }
                return true;
        }
	
}
