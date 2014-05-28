package com.example.peerbased;
import java.net.InetAddress;

/* This student class is used to create a Student object for each session */
class Student
{
	String uname;
	InetAddress IP;
	int noOfQuestions;
	int noOfAnswers;
	int marks;
	public Student( InetAddress IP, String uname )
	{
		this.IP = IP;
		this.uname = uname;
		noOfAnswers = 0;
		noOfQuestions = 0;
		marks = 0;
	}
}