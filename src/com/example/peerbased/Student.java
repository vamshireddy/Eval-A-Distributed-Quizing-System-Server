package com.example.peerbased;
import java.net.InetAddress;

/* This student class is used to create a Student object for each session */
class Student
{
	String uID;
	InetAddress IP;
	String name;
	int noOfQuestions;
	int noOfAnswers;
	int marks;
	public Student( InetAddress IP, String uID , String name)
	{
		this.IP = IP;
		this.uID = uID;
		noOfAnswers = 0;
		noOfQuestions = 0;
		marks = 0;
		this.name = name;
	}
}