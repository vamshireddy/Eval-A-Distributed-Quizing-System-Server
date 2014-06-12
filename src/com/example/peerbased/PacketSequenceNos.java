package com.example.peerbased;

public class PacketSequenceNos {

	public static final int AUTHENTICATION_SEND_SERVER = 123;
	public static final int AUTHENTICATION_SEND_CLIENT = 124;
	
	public static final int GROUP_REQ_SERVER_SEND = 234;
	public static final int GROUP_REQ_CLIENT_SEND = 235;
	public static final int GROUP_REQ_SERVER_ACK = 236;
	
	public static final int LEADER_REQ_SERVER_SEND = 1021;
	public static final int LEADER_REQ_CLIENT_SEND = 1022;
	
	public static final int TEAM_REQ_SERVER_SEND = 412;
	public static final int TEAM_REQ_CLIENT_SEND = 413;
	public static final int TEAM_REQ_SERVER_ACK = 414;
	
	public static final int SELECTED_LEADERS_SERVER_SEND = 512;
	public static final int GROUP_SERVER_SEND = 516;
	
	public static final int QUIZ_START_BCAST_SERVER_SEND = 1023;
	
	public static final int FORMED_GROUP_SERVER_SEND = 12319;
	
	
	// quiz seq no's
	
	public static final int QUIZ_INTERFACE_PACKET_SERVER_SEND = 213414;
	public static final int QUIZ_QUESTION_PACKET_CLIENT_SEND = 213454;
	public static final int QUIZ_QUESTION_PACKET_SERVER_ACK = 2134354;
	
	
	public static final int QUIZ_QUESTION_BROADCAST_SERVER_SEND = 452514;
	
	public static final int QUIZ_RESPONSE_CLIENT_SEND = 12314123;
	public static final int QUIZ_RESPONSE_SERVER_ACK = 21342154;
	
}
