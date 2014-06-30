/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package DatabaseGUI;

import com.mysql.jdbc.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author sukalyan14
 */
public class Essentials 
{
    public static String language;
    public static String country;
    public static String fontURL;
    
    public static ResourceBundle messages;
    public static Locale currentLocale;
            
    public static Connection databaseConnection;
    
    
    // Object for Home Page
    public static  HomePage objHomePage;
//    
//    // Object for Teacher Registration
//    public static TeacherRegistration objTeacherRegistration;
//    
//    // Object for Teacher Details
//    public static TeacherDetails objTeacherDetails;
//    
//    // Object for Teacher Login
//    public static TeacherLogin objTeacherLogin;
//    
//    
//    // Object for Teacher Options
//    public static TeacherOptions objTeacherOption;
//    
//    // Object for Configuring student details
//    public static  ConfigureStudents objConfigureStudent;
//    
//    
//    // Object for Viewing Performance of students
//    public static  ViewPerformance objViewPerformance;
//    
//    
//    // Object for Viewing Questions 
//    public static  ViewQuestions objViewQuestions;
//    
//    
//    // Object for Adding a new student
//    public static AddStudent objAddStudent;
//    
//    // Objectt for taking the new password and modifying the student record
//    public static ModifyStudent objModifyStudent;
//    
//    
//    // Object for viewing student details
//    public static ViewStudentDetails objViewStudentDetails;
//            
    
    static
    {
        try 
        {
            Class.forName("com.mysql.jdbc.Driver");
            databaseConnection = (Connection)DriverManager.getConnection("jdbc:mysql://localhost:3306/quizAppNew","root","reddy123");

        } 
        catch (ClassNotFoundException ex) 
        {
            Logger.getLogger(Essentials.class.getName()).log(Level.SEVERE, null, ex);
        } 
        catch (SQLException ex) 
        {
            Logger.getLogger(Essentials.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        objHomePage=null;
//        objTeacherLogin=null;
//        objTeacherOption=null;
//        objViewQuestions=null;
    }    
    
    
  
    public static void imp(String l,String c)
    {
        language=new String(l);
        country=new String(c);
        language="en";
        country="US";
        System.out.println(language+country);
         
        currentLocale=new Locale(language,country);
        messages=ResourceBundle.getBundle("GUI.MessagesBundle",currentLocale);
        if(messages==null)
        {
            System.out.println("Not good");
            //System.out.println(messages.getString("enterTeacherDetails"));
            
        }
        else
        {
            System.out.println("It is good");
        }         
        fontURL="file:///users/vamshi/Desktop/NetBeansProjects/K010.ttf";  
    }
}
