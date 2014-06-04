package com.example.peerbased;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter.DEFAULT;

import com.mysql.jdbc.Connection;
import com.mysql.jdbc.PreparedStatement;

class StartSession {
	//private final short port = 4444; // Constant port created for the Teacher to send/receive packets
	private String teacherName;		 // Name which was entered in the login prompt by the teacher
	private String teacherID;
	private String teacherPassword;  // Password which was entered in the login prompt by the teacher
	private Quiz quiz;				 // Quiz reference variable to which an object would be created when the "Start Quiz" button is creating
	private Date date;				 // Login Date
	private String subject;			 // Subject of the teacher
	private Connection databaseConnection;	 // Database connection to the underlying mySQL database which contains the information about the teachers and students
	private ArrayList<Student> studentsList;
	
	public boolean verifyDetails(String id, String password)
	{
		try {
			// Prepare the statement to be executed on the database with the necessary query string
			PreparedStatement p = (PreparedStatement)databaseConnection.prepareStatement("select * from teacher where teacher_id='"+id+
																"' and password='"+password+"'");
			ResultSet result = p.executeQuery();
			// result will initially point to the record before the 1st record. To access the 1st record, use result.next().
			// If it returns null, then the teacher won't be authenticated with the given details
			if( result.next() )
			{
				// After getting the matched record from the database, we extract the Teacher name and subject name
				teacherName = result.getString("teacher_name");
				subject = result.getString("subject");
				System.out.println("Subject is "+subject);
				return true;
			}
			else
			{
				// UserID and Password doesn't exist in the database
				return false;
			}
		} 
		catch (SQLException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Error in the database query ");
		System.exit(0);// included for completeness
		return false;
	}
	
	// we pass the database name after creating a connection in the MainClass
	public StartSession(Connection db)
	{
		date = new Date(0);
		databaseConnection = db;
		studentsList = StudentListHandler.getList();
	}
	
	public void printWelcomeMessage()
	{
		System.out.println("Hello "+teacherName+",Have a nice time teaching "+subject);
	}
	
	public void start()
	{
		StudentLogin sl = new StudentLogin(databaseConnection);
		sl.start();
		
		while(true)
		{
			System.out.println("Enter your userID and password: ");
			teacherID = Utilities.scan.nextLine();
			teacherPassword = Utilities.scan.nextLine();
			
			if( verifyDetails(teacherID,teacherPassword) )
			{
				printWelcomeMessage();
				int retVal;
				do
				{
					retVal = showOptions();
				}
				while( retVal!=-1 );
				break;
			}
			else
			{
				System.out.println("Invalid userID or Password, Try again.");
			}
		}
	}
	
	public int showOptions()
	{
		System.out.println("1.Configure Student Details\n2.Start a Quiz\n3.View Performance of student\n"+
									"4.View Questions in Database\n5.Upload any Documents\n6.View Online Students\n7.Exit");
		int choice = Utilities.scan.nextInt();
		switch(choice)
		{
			case 1: // This case needs to access student database
					break;
			case 2:	// Initiates the Quiz
					byte noOfStudents;
					byte noOfGroups;
					byte noOfStudentsInGroup;
					byte noOfRounds;
					System.out.println("No of students present in the class : ( 0-128 )");
					noOfStudents = Utilities.scan.nextByte();
					System.out.println("No of Groups : (0-128) ");
					noOfGroups = Utilities.scan.nextByte();
					System.out.println("No of students present in each group : (0-128) ");
					noOfStudentsInGroup = Utilities.scan.nextByte();
					System.out.println("No of rounds : ");
					noOfRounds = Utilities.scan.nextByte();
					quiz = new Quiz(noOfStudents,noOfGroups,noOfStudentsInGroup,subject,teacherName,date, databaseConnection, noOfRounds);
					// This initiates the quiz with the parameters specified above
					quiz.startQuizSession();
					break;
			case 3:	queryPerformance();
					break;
			case 4: // This case needs to access student database ( Questions in database )
					queryQuestions();
					break;
			case 5: // Upload Documents 
					break;
			case 6: // Exit
					displayStudents();
					break;
			case 7: return -1;
			
		    default: return -1;
		}
		return 1;
	}
	
	public void displayStudents()
	{
		System.out.println("These are the students who are logged in right now!");
		for(int i=0;i<studentsList.size();i++)
		{
			Student s = studentsList.get(i);
			System.out.println("uID : "+s.uID+" "+"IP : "+s.IP+" "+" Marks : "+s.marks);
		}
	}
	
	public void queryPerformance()
	{
		
	}
	
	public void queryQuestions()
	{
		
	}
}
