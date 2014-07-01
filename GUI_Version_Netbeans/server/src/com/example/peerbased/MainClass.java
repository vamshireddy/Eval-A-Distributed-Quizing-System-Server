package com.example.peerbased;
import GUI.ResponseStatistics;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.sql.DriverManager;
import java.sql.SQLException;

import com.mysql.jdbc.Connection;
import java.util.Locale;
import java.util.ResourceBundle;


public class MainClass {
	
	public static void main(String args[])
	{
		/* Make a connection with the local mysql database using JDBC
		 * mysql-java-connection jar file must be included in the External Jar files for this to run
		 */
		try 
		{
			Class.forName("com.mysql.jdbc.Driver");
			Connection con = (Connection)DriverManager.getConnection("jdbc:mysql://localhost:3306/QuizFinalDB","root","reddy123");
			/*
                                Session object is created to start the Application
                        */
			StartSession session = new StartSession(con);
			session.start();
                                                
                        // Util class contains the statements required for internationalization
                        // The arguments have to be decided based on the medium the user wants
//                        Util.imp("en","US");
//                        
//			StartSession session = new StartSession(con);
//                        HomeOptions homeOption=new HomeOptions(con);
//                        homeOption.setVisible(true);
		}
		catch (SQLException e){
			e.printStackTrace();
		}
		catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
}
