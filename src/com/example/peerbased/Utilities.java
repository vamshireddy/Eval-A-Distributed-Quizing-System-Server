package com.example.peerbased;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Scanner;


public class Utilities {
	
	// Constants
	public static final int MAX_BUFFER_SIZE = 1024;
	public static final int MAX_LENGTH_OF_DATA = 100;
	private static ByteArrayInputStream bais;
	private static ObjectInputStream ois;
	private static ByteArrayOutputStream baos;
	private static ObjectOutputStream oos;

	// Port numbers
	public static int recvPort = 4444;
	public static int clientPort = 5555;
	public static int recvProbePort = 9989;
	public static int clientProbePort = 9998;
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
	
}
