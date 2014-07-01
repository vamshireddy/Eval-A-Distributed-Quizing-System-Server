/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.example.peerbased;

import QuizPackets.QuizInterfacePacket;
import static com.example.peerbased.Quiz.rps;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author vamshi
 */
public class UDPReliableHelperClass {
    
    
        public static void sendToTeamMate_UDP_Reliable(DatagramSocket sendSocket, DatagramSocket recvSocket, Group loopGrp, Packet pack)
        {
                if( loopGrp.teamMembers.isEmpty() )
                {
                    System.out.println("Group Empty\n");
                    return;
                }
                
                Iterator<Student> innerIter = loopGrp.teamMembers.iterator();
                
                while( innerIter.hasNext() )
                {
                    Student s = innerIter.next();
                    pack.seq_no = Utilities.seqNo;

                    /*
                        Now perform checking of availabilty
                    */

                    if( UDPReliableHelperClass.sendToClientReliableWithGUI(sendSocket, recvSocket, s.IP , pack, s.name) == false )
                    {
                        System.out.println("CLient "+s.name+" is removed");
                        innerIter.remove();
                    }
                }
        }
        
        public static void sendToTeamMate_UDP_Reliable_and_IncreaseQuestionsAttempted(DatagramSocket sendSocket, DatagramSocket recvSocket, Group loopGrp, Packet pack)
        {
                if( loopGrp.teamMembers.isEmpty() )
                {
                    System.out.println("Group Empty\n");
                    return;
                }
                
                Iterator<Student> innerIter = loopGrp.teamMembers.iterator();
                
                while( innerIter.hasNext() )
                {
                    Student s = innerIter.next();
                    pack.seq_no = Utilities.seqNo;

                    /*
                        Now perform checking of availabilty
                    */

                    if( UDPReliableHelperClass.sendToClientReliableWithGUI(sendSocket, recvSocket, s.IP , pack, s.name) == false )
                    {
                        System.out.println("CLient "+s.name+" is removed");
                        innerIter.remove();
                    }
                    else
                    {
                        s.noOfQuestions++;
                    }
                }
        }
    
        public static boolean sendToLeader_UDP_Reliable(DatagramSocket sendSocket,DatagramSocket recvSocket, Group loopGrp,Packet pack)
        {
                
                while( UDPReliableHelperClass.sendToClientReliableWithGUI(sendSocket, recvSocket, loopGrp.leaderRecord.IP , pack, loopGrp.leaderRecord.name) == false )
                {
                    /*
                        If the leader is not reachable, then make next one as leader
                    */
                    System.out.println("Leader "+loopGrp.leaderName+" is removed");
                    loopGrp.leaderRecord = null;
                    loopGrp.leaderID = null;
                    loopGrp.leaderName = null;

                    /*
                        Get a new team mate and make him a leader
                    */
                    Student newLeader = null;
                    try
                    {
                         newLeader = loopGrp.teamMembers.remove(0);
                         System.out.println("Size of the team is now "+loopGrp.teamMembers.size());
                    }
                    catch( ArrayIndexOutOfBoundsException aiobe )
                    {
                        System.out.println("The group is removed "+loopGrp.groupName);
                        return false;
                    }
                    catch( IndexOutOfBoundsException ai )
                    {
                        System.out.println("The group is removed "+loopGrp.groupName);
                        return false;
                    }

                    System.out.println("Student "+newLeader.name+" is made as a new leader");


                    loopGrp.leaderRecord = newLeader;
                    loopGrp.leaderID = newLeader.uID;
                    loopGrp.leaderName = newLeader.name;
                    
                    /*
                        Update the packet with new sequence number, leader, and leader ID
                    */
                    
                    if( pack.type == PacketTypes.GROUP_DETAILS_MESSAGE )
                    {
                        /*
                            For displaying the group. In this case if the leader is not reached, then group details get changed
                        */
                        
                        System.out.println("Forming a new packet");
                        
                        SelectedGroupPacket sgp = new SelectedGroupPacket(loopGrp.groupName , loopGrp.leaderRecord, loopGrp.teamMembers);
                        
                        pack = new Packet(Utilities.seqNo, pack.type , false, Utilities.serialize(sgp));
                    }
                    else if( pack.type == PacketTypes.QUIZ_INTERFACE_START_PACKET )
                    {
                        /*
                            For changing the ID of new leader in the packet
                        */
                        
                        QuizInterfacePacket qipRcvd = (QuizInterfacePacket)Utilities.deserialize(pack.data);
                        
                        /*
                            If the current active group's leader is out, then change the packet or else if other inactive groups's leaders are gone. No need to change as
                            the active group will be the same and doesnt depend on the other people's crashing.
                        */
                        if( qipRcvd.activeGroupName.equals(loopGrp.groupName))
                        {
                                               
                            QuizInterfacePacket qip = new QuizInterfacePacket(loopGrp.groupName, loopGrp.leaderID);                        
                            pack = new Packet(Utilities.seqNo, pack.type , false, Utilities.serialize(qip));
                        }
                    }

                    pack.seq_no = Utilities.seqNo;
                    
                    System.out.println("Sending again to "+loopGrp.leaderName+" and his IP is "+loopGrp.leaderRecord.IP);
                }
                return true;
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
					System.out.println("I got a packet from client with seq no "+recvPacket.seq_no+" Expected is "+Utilities.seqNo+" and address "+recvPacky.getAddress()+" But its something else");
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
                System.out.println("\nSending to "+name+" \n");
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
