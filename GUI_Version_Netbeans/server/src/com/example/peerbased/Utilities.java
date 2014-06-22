package com.example.peerbased;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Scanner;


public class Utilities {
	
	public static final int FAIL = 1;
	public static final int SUCCESS = 1;
	
	public static final int noOfAttempts = 20;
	public static int seqNo = 150;
	
	// Constants
	public static final int MAX_BUFFER_SIZE = 1024;
	public static final int MAX_LENGTH_OF_DATA = 100;
	private static ByteArrayInputStream bais;
	private static ObjectInputStream ois;
	private static ByteArrayOutputStream baos;
	private static ObjectOutputStream oos;

	// Port numbers
	public static int servPort = 4444;
	public static int clientPort = 5555;
	public static int servProbePort = 9989;
	public static int clientProbePort = 9998;
	public static int authServerPort = 9876;
	public static int authClientPort = 6789;
	public static int clientErrorPort = 9999;
	// Addresses
	public static InetAddress broadcastIP;
	// Error codes
	public static final byte NO_ERROR = 1;
	public static final byte INVALID_USER_PASS = 2;
	public static final byte ALREADY_LOGGED = 3;
	public static final byte INVALID_FIELDS = 4;
	public static final byte INVALID_REQUEST = 5;
	// Utility objects
	public static Scanner scan = new Scanner(System.in);
	
	static
	{
		try {
			broadcastIP = InetAddress.getByName("192.168.1.255");
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static Object deserialize(byte[] buffer)
	{
		Object result = null;
	    try {
	    	bais = new ByteArrayInputStream(buffer);
			ois = new ObjectInputStream(bais);
			result = ois.readObject();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("safsa");
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("safsa");
		}
	    return result;
	}
	
	public static byte[] serialize(Object obj)
	{
		byte[] Buf = null;
	    try {
	    	baos = new ByteArrayOutputStream();
		    oos = new ObjectOutputStream(baos);
			oos.writeObject(obj);
			oos.flush();
		    Buf = baos.toByteArray();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return Buf;
	}
	public static void cleanServerBuffer(DatagramSocket s)
	{
		try {
			// 1 second
			s.setSoTimeout(1000);
		} catch (SocketException e1) {
			e1.printStackTrace();
		}
		while(true)
		{
			byte[] b  = new byte[Utilities.MAX_BUFFER_SIZE];
			DatagramPacket p = new DatagramPacket(b, b.length);
			try {
				s.receive(p);
			}
			catch( SocketTimeoutException e1)
			{
				// This exception occurs when there are no packets for the specified timeout period.
				// Buffer is clean!!
				try {
					s.setSoTimeout(0); // infinete timeout (blocking)
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
}
