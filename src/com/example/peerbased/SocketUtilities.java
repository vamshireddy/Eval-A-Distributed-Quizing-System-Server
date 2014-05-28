package com.example.peerbased;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;


public class SocketUtilities {

	public static DatagramPacket createDatagram(String data, String IP, int port)
	{
		byte[] bytes = data.getBytes();
		InetAddress address;
		DatagramPacket packet;
		try {
			address = InetAddress.getByName(IP);
			packet = new DatagramPacket(bytes, bytes.length,address,port);
			return packet;
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	public static DatagramPacket createDatagram(String data)
	{
		byte[] bytes = data.getBytes();
		DatagramPacket packet = new DatagramPacket(bytes, bytes.length);
		return packet;
	}
}
