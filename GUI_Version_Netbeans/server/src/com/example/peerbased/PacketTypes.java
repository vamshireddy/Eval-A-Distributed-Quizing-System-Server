package com.example.peerbased;


public class PacketTypes {
	public static final byte LEADER_SCREEN_CHANGE = 1;
	public static final byte GROUP_DETAILS_MESSAGE = 2;
	public static final byte QUIZ_TURN_SCREEN = 3;
	public static final byte QUIZ_INTERFACE_START_PACKET = 4;
	public static final byte QUESTION_VALIDITY = 5;
	public static final byte QUESTION_BROADCAST = 6;
	public static final byte QUESTION_ACK = 7;
        public static final byte QUIZ_END_PACKET = 8;
        	/*
	 * Authentication
	 */
	public static final byte AUTHENTICATION_LOGIN = 10;
        public static final byte AUTHENTICATION_CHANGE_PASS = 11;
}
