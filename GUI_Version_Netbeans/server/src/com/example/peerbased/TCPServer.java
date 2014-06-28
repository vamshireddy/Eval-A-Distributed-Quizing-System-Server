/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.example.peerbased;

import com.mysql.jdbc.PreparedStatement;
import static com.sun.org.apache.xalan.internal.lib.ExsltDynamic.map;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import static java.lang.System.in;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import sun.misc.IOUtils;

/**
 *
 * @author vamshi
 */

class ServeFunction extends Thread
{
    Socket sock;
    BufferedReader inFromClient;
    DataOutputStream outToClient;
    Connection con;
    
    public ServeFunction(Socket s, Connection con)
    {
        this.con = con;
        sock = s;
        try {
            /*
                Get the input stream
            */
            inFromClient = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            /*
                Get the Output stream
            */
            outToClient = new DataOutputStream(sock.getOutputStream());
         
        } catch (IOException ex) {
            Logger.getLogger(ServeFunction.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public void run()
    {
        /*
            Get the input from the client and check if he requests for questions or performance
        */
        String jsonString;
        try {
            
            jsonString = inFromClient.readLine();
            JSONObject json = (JSONObject)new JSONParser().parse(jsonString);
            String type = (String)json.get("queryType");
            
            if( type.equals("Questions"))
            {
                /*
                    Now query the database and generate a JSON string to send it back to client
                */
                String subject = (String)json.get("subject");
                String dateInMs = (String)json.get("date");
                System.out.println("DATE : "+dateInMs);
                String clientString = getJSONQuestionsString(subject,dateInMs);
                System.out.println("JSON STRING : "+clientString);
                outToClient.writeBytes(clientString+"\n");
            }
            else if( type.equals("Performance"))
            {
                /*
                    Now check whether the query is for the last test or overall
                */
                String performanceQueryType = (String)json.get("perfType");
                String studentID = (String)json.get("studentID");
                String subject = (String)json.get("subject");
                String result = null;
                if( performanceQueryType.equals("Overall") )
                {
                    /*
                        Overall performance
                    */
                    result = getJSONPerformanceString(subject,studentID,2);
                }
                else if( performanceQueryType.equals("LastTest"))
                {
                    /*
                        Individual performance
                    */
                     result = getJSONPerformanceString(subject,studentID,1);
                }
                System.out.println("JSON STRING : "+result);
                outToClient.writeBytes(result+"\n");
            }
            else if( type.equals("Files") )
            {
                /*
                    CLient is requesting the files
                */
                String std = (String)json.get("standard");
                /*
                    Client sends subject and standard and the files will be sent
                */
                String pathName = createPath(std);
                /*
                    Now traverse the folder and create a JSON File
                */
                String filesJSONString = traverseAndMakeJSON(pathName);
                System.out.println("I am sending this to client : "+filesJSONString);
                /*
                    Send it to the client back
                */
                outToClient.writeBytes(filesJSONString+"\n");
                System.out.println("Sent "+filesJSONString+" to client and its of "+filesJSONString.length()+" bytes");
                /*
                    Now wait for the file download request
                */
                String JSONfileToDownload = inFromClient.readLine();

                System.out.println("CLIENT ASKED FOR :"+JSONfileToDownload);

                /*
                    Get the file name from the JSON string
                */

                ObjectMapper mapper = new ObjectMapper();

                HashMap<String,String> fileMap = new HashMap<String, String>();

                try {

                        //convert JSON string to Map
                        fileMap = mapper.readValue(JSONfileToDownload, new TypeReference<HashMap<String,String>>(){});
                }
                catch (Exception e) {
                        e.printStackTrace();
                }

                Set<String> set = fileMap.keySet();

                String fileName="";

                /*
                    This loop iterates for one time and gets the value
                */

                for(String s : set)
                {
                    fileName = fileMap.get(s);
                }


                File transferFile = new File(fileName);

                byte [] bytearray  = new byte [(int)transferFile.length()];

                /*
                    Send the file size to the client
                */

                String senty = new String(bytearray);

                JSONObject fileSizeJSON = new JSONObject();
                fileSizeJSON.put("fileSize",bytearray.length);

                outToClient.writeBytes(fileSizeJSON+"\n");

                outToClient.flush();


                try {
                    /*
                    Now send the file to client
                    */
                    sleep(5000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(ServeFunction.class.getName()).log(Level.SEVERE, null, ex);
                }
                BufferedInputStream buff = new BufferedInputStream(new FileInputStream(transferFile));

                byte[] buffer = new byte[1024];

                OutputStream os = sock.getOutputStream();

                int count ;
                while( (count = buff.read(buffer)) >= 0 )
                {
                    os.write(buffer,0,count);
                    os.flush();
                }
                os.close();
                sock.close();
                System.out.println("File sent to client");
            }
        } catch (IOException ex) {
            Logger.getLogger(ServeFunction.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParseException ex) {
            Logger.getLogger(ServeFunction.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    public String traverseAndMakeJSON(String path)
    {
        System.out.println("DIRECTORY PATH IS :"+path);
        JSONObject obj = new JSONObject();
        
        File f = new File(path);
        String dirPath = f.getAbsolutePath();
        if( f.isDirectory() )
        {
            String[] subNote = f.list();
            for(String filename : subNote)
            {
                String completePath = dirPath+"/"+filename;
                obj.put(filename, completePath);
            }
        }
        else
        {
            System.out.println("Its not a directory!");
            obj.put("Error fetching","Error");
        }
        return obj.toJSONString();
    }
    
    public String createPath(String standard)
    {
        /*
            standard should be in the format : standard<number>
            Subject name should be started with capitalized letter
        */
        String path = "Files/"+standard+"/";
        System.out.println(path);
        return path;
    }
    
    public String getJSONPerformanceString(String sub, String id, int type)
    {
        System.out.println("I got "+sub+" "+id);
        if( type == 1)
        {
            /*
                Last test only
            */
            PreparedStatement p = null;
            try {
                p = (PreparedStatement)con.prepareStatement("select * from student_performance"+" where roll_number='"+id+"' and subject='"+sub+"' order by date desc");
                ResultSet result = p.executeQuery();
                
                HashMap<String,String> hm = new HashMap<>();
                
                if( result.next() )
                {   
                    String date = result.getString("date");
                    String marks = result.getString("marks");
                    String quesAttempted = result.getString("questions_attempted");
                    String quesCorrect = result.getString("correct_answers");
                    hm.put("date",date);
                    hm.put("marks",marks);
                    hm.put("quesAttempted",quesAttempted);
                    hm.put("correct", quesCorrect);
                }
                /*
                    Now form a JSON Object
                */
                JSONObject obj = new JSONObject(hm);
                return obj.toJSONString();
            }
            catch (SQLException ex) 
            {
                    Logger.getLogger(ServeFunction.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        else if( type == 2)
        {
            /*
                Overall
            */
            PreparedStatement p;
            try {
                ResultSet result;
                
                p = (PreparedStatement)con.prepareStatement("select * from student_performance"
                        +" where roll_number='"+id+"' "+"order by subject asc");
                result = p.executeQuery();
                
                HashMap<String,String> hm = new HashMap<>();
                
                String subject = null;
                String marksStr = "";
                
                for(int i=0; result.next(); i++ )
                {
                    if( subject == null )
                    {
                        subject = result.getString("subject");
                    }
                    else 
                    {
                        String tempSubject = result.getString("subject");
                        if( !tempSubject.equals(subject) )
                        {
                            hm.put(subject, marksStr);
                            subject = tempSubject;
                            marksStr = "";
                        }
                    }
                    marksStr = marksStr + result.getString("marks") + "$";
                }
                hm.put(subject, marksStr); 
                /*
                    Now form a JSON Object
                */
                JSONObject obj = new JSONObject(hm);
                System.out.println("STRING IS        : "+obj.toJSONString());
                return obj.toJSONString();
            }
            catch (SQLException ex) 
            {
                    Logger.getLogger(ServeFunction.class.getName()).log(Level.SEVERE, null, ex);
            } 
        }
        return null;
    }
    public String getJSONQuestionsString(String sub, String date)
    {
        PreparedStatement p;
        try {
            
            p = (PreparedStatement)con.prepareStatement("select * from question_table"
                    +" where subject='"+sub+"' and date >='"+date+"'");
            ResultSet result = p.executeQuery();
            
            /*
                Form a HashMap and add into it
            */
            HashMap<String,String> hm = new HashMap<>();
            
            while( result.next() )
            {
                String ques = result.getString("question");
                String ans = result.getString("answer");
                String level = result.getString("level");
                String datey = result.getString("date");
                ans = level+"$"+datey+"$"+ans;
                hm.put(ques, ans);
            }
            /*
                Now form a JSON Object
            */
            JSONObject jobj = new JSONObject(hm);
            System.out.println(jobj.toJSONString());
            return jobj.toJSONString();
            
        } catch (SQLException ex) {
            Logger.getLogger(ServeFunction.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
}


public class TCPServer extends Thread{
    private Connection con;
    public ServerSocket socket;
    
    public TCPServer(Connection con)
    {
        System.out.println("saf");
        try {
            socket = new ServerSocket(6711);
            socket.setReuseAddress(true);
        } catch (IOException ex) {
            Logger.getLogger(TCPServer.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.con = con;
    }
    public void run()
    {
        try {
            /*
                Listen for clients
            */
            System.out.println("TCP server started!");
            while( true )
            {
                Socket clientSock = socket.accept();
                Thread t = new ServeFunction(clientSock,con);
                t.start();
            }
            
        } catch (IOException ex) {
            Logger.getLogger(TCPServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
