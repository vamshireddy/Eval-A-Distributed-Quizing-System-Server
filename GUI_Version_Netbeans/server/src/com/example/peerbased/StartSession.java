package com.example.peerbased;
import DatabaseGUI.Essentials;
import DatabaseGUI.HomePageDB;
import GUI.OnlineStudentsPage;
import GUI.StartPage;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import com.mysql.jdbc.Connection;
import com.mysql.jdbc.PreparedStatement;
import java.util.logging.Level;
import java.util.logging.Logger;

class StartSession {
	
	private String teacherName;		 // Name would be fetched from the database
	private String teacherID;		 // ID will take the string which was entered by teacher in the login prompt
	private String teacherPassword;  // Password which was entered in the login prompt by the teacher
	private Quiz quiz;				 // Placeholder for Quiz class object
	private String subject;			 // Subject of the teacher fetched from database
	private Connection databaseConnection;	 // Database connection to the underlying mySQL database
	private ArrayList<Student> studentsList; // List of students who are connected during the session
	private DatabaseQueries queryObject;
	private String standard;
        private GUI.HomePage hpage;
        private OnlineStudentsPage ospage;
	private StartPage spage;
        
	public StartSession() {
		teacherName = "default";
		teacherID = "default";
		teacherPassword = "default";
		quiz = null;
		subject = "default";
		databaseConnection = null;
		studentsList = null;
		queryObject = null;
		standard = "";
                hpage = new GUI.HomePage();
	}
	
	public StartSession(Connection db)
	{
		this();
                Essentials.objHomePage = new HomePageDB();
                ospage = new OnlineStudentsPage();
                ospage.reset();
                
                spage = new StartPage();
                spage.reset();
                
		databaseConnection = db;
		studentsList = StudentListHandler.getList();
		/*
		 * Create a database query class object
		 */
		queryObject = new DatabaseQueries(databaseConnection);
		/*
		 * Spawn a new thread for listening to the student authentication requests
		 */
		StudentLogin sl = new StudentLogin(databaseConnection);
		sl.start();
                /*
                    MultiThreaded TCP server for handling performance, reports , questions, etc
                */
                TCPServer tcps = new TCPServer(databaseConnection);
                tcps.start();
	}
	
	public boolean verifyDetails()
	{
		try 
		{
			/*
			 *  Prepare the statement to be executed on the database with the necessary Query string
			 */
			PreparedStatement p = (PreparedStatement)databaseConnection.prepareStatement("select * from teacher_login,teacher_detail "
                                + "where teacher_login.teacher_id='"+teacherID+"' and teacher_login.password='"+teacherPassword+"' and teacher_detail.std='"+standard+"'");
			ResultSet result = p.executeQuery();
			/*
			 *  'result' will initially point to the record before the 1st record. To access the 1st record, use result.next().
			 *   If it returns null, then the teacher won't be authenticated with the given details
			 */
			if( result.next() )
			{
				/*
				 *  After getting the matched record from the database, Extract the Teacher name and subject name
				 */
				teacherName = result.getString("teacher_login.teacher_name");
				subject = result.getString("teacher_detail.subject");
				return true;
			}
			else
			{
				/*
				 *  UserID and Password doesn't exist in the database
				 */
				return false;
			}
		} 
		catch (SQLException e) 
		{
			e.printStackTrace();
		}
		System.out.println("Error in the database query ");
		System.exit(0);
		return false;
	}
        
	// we pass the database name after creating a connection in the MainClass

	
	public void printWelcomeMessage()
	{
		System.out.println("Hello "+teacherName+", Have a Nice time teaching "+subject+" ! ");
	}
	
	public void start()
	{	
		while(true)
		{
//			System.out.println("Enter your userID and password and standard: ");
//			teacherID = Utilities.scan.nextLine();
//			teacherPassword = Utilities.scan.nextLine();
//                        standard = Utilities.scan.nextLine();
                        spage.setVisible(true);
                        
                        while( spage.getWaitStatus()== true )
                        {
                            try {
                                Thread.sleep(200);
                            } catch (InterruptedException ex) {
                                Logger.getLogger(StartSession.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                        
                        teacherID = spage.getUID();
                        teacherPassword = spage.getPass();
                        standard = spage.getStd();
                        
                        spage.reset();
                        
                        
                    
			if( verifyDetails() )
			{
                                spage.setVisible(false);
				printWelcomeMessage();
				/*
				 * For displaying the options multiple times
				 */
				int retVal;
				do
				{
					retVal = showOptions();
				}
				while( retVal!=-1 );
			}
			else
			{
				System.out.println("Invalid userID or Password, Try again.");
			}
		}
	}
	
	public int showOptions()
	{
            
                /*
                    Display Home Page
                */
                
                
                hpage.setVisible(true);
                
                while( hpage.getWaitStatus() == true )
                {
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(StartSession.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                
                int choice = hpage.getChoice();
                hpage.reset();
                
                System.out.println("Got "+choice);
                
		switch(choice)
		{
			case 1:         // This case needs to access student database
                                        hpage.setVisible(false);
                                        Essentials.objHomePage.setVisible(true);
                                        
                                        while( Essentials.objHomePage.getExitStatus() == false )
                                        {
                                            try {
                                                Thread.sleep(200);
                                            } catch (InterruptedException ex) {
                                                Logger.getLogger(StartSession.class.getName()).log(Level.SEVERE, null, ex);
                                            }
                                        }
                                        Essentials.objHomePage.reset();
					break;
                            
			case 2:         // Initiates the Quiz
					quiz = new Quiz( subject,teacherName,databaseConnection);
					// This initiates the quiz with the parameters specified above
					try
                                        {
                                            quiz.startQuizSession();
                                        }
                                        catch( Exception e )
                                        {
                                            e.printStackTrace();
                                        }
					break;
                        case 3 :        displayStudents(ospage);
                                        ospage.setVisible(true);
                                        
                                        System.out.println(ospage.getWaitStatus());
                                        
                                        while(ospage.getWaitStatus() == true )
                                        {
                                            try {
                                                Thread.sleep(200);
                                            } catch (InterruptedException ex) {
                                                Logger.getLogger(StartSession.class.getName()).log(Level.SEVERE, null, ex);
                                            }
                                        }
                                        ospage.reset();
					break;
			
                        case -1 :       hpage.setVisible(false);
                                        return -1;
                                        
		}
		return 1;
	}
	
	public void displayStudents(OnlineStudentsPage ospage)
	{
		System.out.println("These are the students who are logged in right now!");
		for(int i=0;i<studentsList.size();i++)
		{
			Student s = studentsList.get(i);
			System.out.println("Name : "+s.name+" "+" IP : "+s.IP+" "+" ID : "+s.uID);
                        ospage.addStudent(("Name : "+s.name+" "+" IP : "+s.IP+" "+" ID : "+s.uID));
		}
	}
	
	@Override
	protected void finalize() throws Throwable {
		System.exit(0);
	}
}
