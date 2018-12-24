/**
 * File:  Look4MatchServlet.java
 * This servlet searches the Look4U database and counts the number of matches of Phrase from the Look4U
 * database with the WhatAdSays01 field of the AdInfoView database. If a match is found it counts it and
 * sends an email to the user telling how many matchs they have and gives them an html link where to view
 * the the data by simply clicking on it.
 * In addition to finding ads that match a search criteria, it also deletes ads
 * that are expired, only from the Look4U database.
 * This servlet is "started from the URLConnectionReader.java class which can
 * is called in an "AT" job in windows from the Look4U.cmd which contains the 
 * line: java URLConnectionReader   ... thats it!
 * @author Tom Kochanowicz,  blue-j@worldnet.att.net
 * @version 0.1, 28 March 2000
 * @author Tom Kochanowicz tkoc@cox.net
 * @version 0.2 March 03
 */

package com.classified;

import java.awt.*;
import java.util.*;
import java.util.Properties;
import javax.servlet.*;
import javax.servlet.http.*;
import java.sql.*;
import javax.sql.*;
import java.io.*;
import java.text.*;
import javax.naming.*;

// For mail
import javax.mail.*;
import javax.mail.internet.*;
import javax.activation.*;

public class Look4MatchServlet extends HttpServlet{
	// data members
  private Connection dbConnection;
  private javax.sql.DataSource pool;
  
  // For mail
  ///private final static String MAIL_HOST = "smtp.central.cox.net";
  ///private final static String MAIL_FROM = "tkoc@cox.net";
  private static ResourceBundle email_bundle =
    ResourceBundle.getBundle("com.classified.common.emailProperties");

  public void init(ServletConfig config) throws ServletException{
    super.init(config);
      try{
          Context env = (Context) new InitialContext().lookup("java:comp/env");
          pool = (javax.sql.DataSource) env.lookup("jdbc/Classifieds");

          if (pool == null)
            throw new ServletException("`jdbc/Classifieds' is an unknown DataSource");
      }catch (NamingException e) {
        throw new ServletException(e);
      }
  }

  public void service(HttpServletRequest request, HttpServletResponse response)
                            throws ServletException, IOException{
    // Have todaysDate to reference for comparison wiht BLUEADS cookie
    Calendar calendar1;
    SimpleDateFormat formatter1;
    java.util.Date todaysDate;

    // Format todaysDate
    calendar1 = Calendar.getInstance();
    formatter1 = new SimpleDateFormat("MM/dd/yy");
    formatter1.setTimeZone(java.util.TimeZone.getDefault());  // bug fix so not PST & is CST
    todaysDate = calendar1.getTime();

	 		
///	  HttpSession session = request.getSession(true);

	 try{
		System.out.println("Look4MatchServlet init: Start");

		dbConnection = null;

    /** Should only need one connection for the prepared statements */
    dbConnection = pool.getConnection();

		Statement stmt0 = null;
		ResultSet dataResultSet0 = null;

		Statement stmt1 = null;
		ResultSet dataResultSet1 = null;

		Statement stmt2 = null;
		ResultSet dataResultSet2 = null;

		PrintWriter outputToBrowser =  response.getWriter();

		// Delete all expired searches first.		
		stmt0 = dbConnection.createStatement();
		int countOfEndSearch = stmt0.executeUpdate("delete from Look4U WHERE EndDate = " + "'" + formatter1.format(todaysDate) + "'");

		stmt1 = dbConnection.createStatement();
		dataResultSet1 = stmt1.executeQuery("SELECT Email, Phrase, EndDate FROM Look4U");

		response.setContentType("text/html");

		int countForMatches = 0;

		// HTML Header
		outputToBrowser.println("<HTML><HEAD><TITLE>Look4U Results</TITLE></HEAD>");
		outputToBrowser.println("<BODY>");
		outputToBrowser.println("THE NUMBER OF ADS THAT HAVE ENDED ARE " + countOfEndSearch + " TODAYS DATE IS " + formatter1.format(todaysDate) + "<P>");
    System.out.println("THE NUMBER OF ADS THAT HAVE ENDED ARE " + countOfEndSearch + " TODAYS DATE IS " + formatter1.format(todaysDate) + "<P>");

		// Outer loop checks for email & phrase.
		while(dataResultSet1.next()){
			
			//System.out.println("THE PHRASE IS " + dataResultSet1.getString("Phrase").trim());			
			String phrase = dataResultSet1.getString("Phrase").trim();
			String email = dataResultSet1.getString("Email").trim();
			String endDate = dataResultSet1.getString("EndDate").trim();
			
			// Inner loop checks if the Phrase found above matches any of the WhatAdSays01 info.
			stmt2 = dbConnection.createStatement();
			dataResultSet2 = stmt2.executeQuery("select COUNT(WhatAdSays01) from AdInfoView WHERE WhatAdSays01 LIKE '%" + phrase + "%'");

			int numRows = 0;
		
			while(dataResultSet2.next()){
				// Count the number of matches.
				numRows = dataResultSet2.getInt(1);
				countForMatches++;					
			}
			
			if(numRows > 0){
				outputToBrowser.println("DATA RESULT SET 2 = " + numRows + " Matching the Phrase " + phrase +
					" Your email address is: " + email + "<P>");


				//Compiler.disable();  //turn off JIT so we can see line numbers when debugging		
				// Now send count of matching ads and link to search the database.

				// If spaces in URL, encode them so they will work as a hyper-link.
				String linkPhrase = java.net.URLEncoder.encode(phrase);

        /**
         * -------------------- Start Mail configuration -----------------------
         */
				String msgText = "Thank you for doing business with http://192.168.1.102/classified/\n" +
				"\tWe are commited to keeping our customers satified." +
				" Our search indicated: " + numRows + " match(s) for the phrase" +
				" you entered. The phrase you entered is: " + "\"" + phrase + "\"." + 
				" We will contine searching for you until " + endDate + " or until you" +
				" choose to stop the search by entering in your email address and choosing" +
				" the Stop Look4U option at http://192.168.1.102/classified/Look4U.jsp\n\n" + 
				"You can view your matching ad(s) now by clicking on the following link:\n" + 
				"http://192.168.1.102/classified/search?SearchWord=" + linkPhrase + "&DateId=999999999999&ClassifiedAd=SearchByWord" +
				"\n";
				
				/////String to = "blue-j@worldnet.att.net";
				///String from = MAIL_FROM;

				///String host = MAIL_HOST; 
				//String host = "mail.nfinity.com"; 

				boolean debug = Boolean.valueOf("true").booleanValue(); // Change to false to turn off debug.
	
				// create some properties and get the default Session
				Properties props = new Properties();
				///props.put("mail.smtp.host", host);
        props.put("mail.smtp.host", email_bundle.getString("MAIL_HOST"));

				if (debug) props.put("mail.debug", "true"); // Change to false to turn off debug.

				javax.mail.Session emailSession = javax.mail.Session.getDefaultInstance(props, null);
				emailSession.setDebug(debug);

				try {
				    // create a message
				    Message msg = new MimeMessage(emailSession);
				    ///msg.setFrom(new InternetAddress(from));
            msg.setFrom(new InternetAddress(email_bundle.getString("MAIL_FROM")));
				    //InternetAddress[] emailAddress = {new InternetAddress(to)};
				    InternetAddress[] emailAddress = {new InternetAddress(email)};
				    msg.setRecipients(Message.RecipientType.TO, emailAddress);
				    msg.setSubject("Classified Ads Search Results");
				    msg.setSentDate(new java.util.Date());
				    // If the desired charset is known, you can use
				    // setText(text, charset)
				    msg.setText(msgText);	    
				    Transport.send(msg);
				}catch (MessagingException mex){
				  System.out.println("\n--Exception Submit's Email");
					System.out.println("Email is = to " + email);
				}							
        /** ---------------- End Mail configuration -------------------- */
			}			
		}
		
      outputToBrowser.println(getServletInfo());

      try{
        if(dataResultSet1 != null)dataResultSet1.close();
        if(dataResultSet2 != null)dataResultSet2.close();
      }catch (SQLException ignored){}
		
	 }catch (SQLException e) {
		System.out.println("SQLException caught: " + e.getMessage());
	 }finally{
		try{
			if(dbConnection !=null) dbConnection.close();
      System.out.println("Look4Match Search End ");
		}catch (SQLException exp){
      System.out.println("Exception in Look4MatchServlet email " + exp);
    }
	 }
  }

  public String getServletInfo(){
    return "<center><i><font color=63c6de> Classified Ads v.02</font></i></center>";
  }	
}


