
/**
 *  UserEmailServlet.java
 *  This servlet searches the AdInfoView database and finds the matching email address 
 *  using the UniqueDateId as a search field. It then brings up a form where the user
 *  can enter in a message to the user, so that the recepient of the email know who
 *  sent the email, but the person sending the email does not know who they sent it to.
 *  After the servlet is invoked it does a doGet() first to get the users info and
 *  then does the doPost() to do the look the email up in the database, then send it.
 *  The reason for this is to prevent the email collectors from using the email address
 *  and for security.
 *  @author Tom Kochanowicz,  blue-j@worldnet.att.net
 *  @version 0.1, April 6, 2000
 *  @author Tom Kochanowicz tkoc@cox.net
 *  @version 0.2 Feb. 2003
 */

package com.classified.common;

import java.awt.*;
import java.util.*;
import java.util.Properties;
import javax.servlet.*;
import javax.servlet.http.*;
import java.sql.*;
import java.io.*;
import java.text.*;


// For mail
import javax.mail.*;
import javax.mail.internet.*;
import javax.activation.*;

public class UserEmailServlet extends HttpServlet{
	// data members
    	protected Connection dbConnection;
	protected Properties p = new Properties();
	protected String dbURL = "jdbc:weblogic:pool:blueadsPool";

  public static String MAIL_FROM = "emailAddr";
  public static String MAIL_SUBJECT = "subject";
  public static String MAIL_BODY = "body";
	public static String uniqueDateId = "UID"; 

	// FOR TESTING // Compiler.disable();  //turn off JIT so we can see line numbers when debugging	 
	// Get the session object or create one if not exist. A session will
	// persist as long as the client browser maintains a connection with
	// the server. Also check if their is a connection with the database.
	 		
///	HttpSession session = request.getSession(true);


  /**
   * Performs the HTTP GET operation
   * @param req The request from the client
   * @param resp The response from the servlet
   */
  public void doGet(HttpServletRequest req, HttpServletResponse resp)
                                throws ServletException, java.io.IOException{
	
	  uniqueDateId = req.getParameter("UID");

		// Set the content type of the response so can't use back button.
		resp.setContentType("text/html");
		resp.setHeader("pragma", "no-cache");
		resp.setDateHeader("Expires", 0);
		resp.setHeader("Cache-Control", "no-cache");

	  // Get the PrintWriter to write the response
	  java.io.PrintWriter out = resp.getWriter();


	  // Create the HTML form
	  out.println("<html>");
	  out.println("<head>");
	  out.println("<title>blueads.com User Email</title>");
    out.println("<body background=../classified/servletimages/newsbg.jpg bgcolor=white>");

	  out.println("<br><form method=POST action=\"" +
	                  req.getRequestURI() + "\">");

		out.println("<CENTER><P><TABLE BORDER=0 CELLPADDING=0 CELLSPACING=0 WIDTH=66% HEIGHT=367>");
		out.println("<TR><TD  NOWRAP COLSPAN=2 BGCOLOR=\"#C9E8F8\">");
		out.println("<P ALIGN=CENTER><B><FONT SIZE=4>Email </FONT><FONT SIZE=4 COLOR=\"#0066FF\">blue</FONT></B><I><B><FONT SIZE=4");
		out.println("COLOR=\"#0066FF\">ads</FONT></B></I><B><FONT SIZE=4 COLOR=\"#0066FF\">.com</FONT><FONT SIZE=4> classified ad listing</FONT>.</B>");
		out.println("</TD>");
		out.println("</TR>");
		out.println("<TR><TD WIDTH=11%><B>Your Email:</B></TD>");
		out.println("<TD WIDTH=89%><INPUT TYPE=TEXT NAME=" + MAIL_FROM + " SIZE=25><FONT COLOR=\"#CC0000\" FACE=Wingdings> +</FONT></TD>");
		out.println("</TR>");
		out.println("<TR><TD WIDTH=11%><B>Subject:</B></TD>");
		out.println("<TD WIDTH=89%><INPUT TYPE=TEXT NAME=" + MAIL_SUBJECT + " SIZE=25><FONT FACE=Wingdings> </FONT><FONT COLOR=fuchsia FACE=Wingdings>R</FONT></TD>");
		out.println("</TR>");
		out.println("<TR>");
		out.println("<TD COLSPAN=2 BGCOLOR=\"#C9E8F8\">");
		out.println("<P ALIGN=CENTER><B><FONT SIZE=4 FACE=Wingdings>@</FONT>Type your message or resume below. <FONT COLOR=blue>Tip:Save");
		out.println("time and use cut &amp; paste</FONT></B><FONT COLOR=blue>.</FONT><FONT SIZE=4 FACE=Wingdings>#</FONT>");
		out.println("</TD>");
		out.println("</TR>");
		out.println("<TR><TD HEIGHT=244 COLSPAN=2><CENTER>");
		out.println("<P><TEXTAREA NAME=" + MAIL_BODY + " ROWS=13 COLS=70></TEXTAREA>");
		out.println("</CENTER></TD></TR></TABLE>");

    out.println("<input type=reset value=\"Reset\">");
	      
    out.println("<INPUT TYPE=\"button\" VALUE=\"Submit\" ONCLICK=\"return submitIt(this.form)\"></div>");
    out.println("<br><center><a href=/classified/index.jsp><font color=\"BLUE\">Return to Home/Search Page</font></a>");
    out.println("</center>");


	  //////////////////// START JAVA SCRIPT/////////////////
	  // JavaScript To Check for Valid Email Entry, Subject and Body.
	  out.println("<SCRIPT LANGUAGE=\"JavaScript\"> <!-- Hide script from old browsers\n ");

	  out.println(" function validEmail(email) {");
	  out.println("  invalidChars = \" /:,;\"");
	  out.println("  if (email == \" \") {");
	  out.println("    return false");
	  out.println("  }");

	  out.println("  for (i=0; i<invalidChars.length; i++) {");
	  out.println("    badChar = invalidChars.charAt(i)");
	  out.println("    if (email.indexOf(badChar,0) != -1) {");
	  out.println("	   return false");
	  out.println("    }");
	  out.println("  }");
	  out.println("  atPos = email.indexOf(\"@\",1)");
	  out.println("  if (atPos == -1) {");
	  out.println(" 	return false");
	  out.println("  }");

	  out.println("  if (email.indexOf(\"@\",atPos+1) != -1) {");
	  out.println("	return false");
	  out.println("  }");

	  out.println("  periodPos = email.indexOf(\".\",atPos)");
	  out.println("  if (periodPos == -1) {");
	  out.println("	return false");
	  out.println("  }");

	  out.println("  if (periodPos+3 > email.length) { ");
	  out.println("  	return false");
	  out.println("  }");
	  out.println("  return true");
	  out.println(" }");

	  // Check to make sure something was entered into the Subject field.
	  out.println(" function checkSubject(subject) {");
	  out.println(" 	if (subject == \"\") {");
	  out.println(" 		return false");
    out.println("	} ");
    out.println("	return true ");
    out.println(" }");

    // Check to make sure something was entered into the Body field.
	  out.println(" function checkBody(body) {");
	  out.println(" 	if (body == \"\") {");
	  out.println(" 		return false");
	  out.println("	} ");
	  out.println("	return true ");
	  out.println(" }");

	  // Submit button can-be-hit-once from the doSubmit() inside the submitIt() function.
	  out.println(" var submitted = false;");
	  out.println(" function doSubmit(form)");
	  out.println(" {");
	  out.println("   if (!submitted) {");
	  out.println(" 	submitted = true;");
	  out.println(" 	form.submit();");
	  out.println("   }");
	  out.println(" }");

    /**
     * ALL JAVA SCRIPT FUNCTIONS ABOVE ARE CALLED FROM submitIt() BELOW.
     */

    /**
     * Check the email address.
     */
	  out.println(" function submitIt(form) {");
	  out.println("  if (!validEmail(form.emailAddr.value)) {");
	  out.println("	alert(\"Please enter a valid email address\")");
	  out.println("	form.emailAddr.focus() ");
	  out.println("	form.emailAddr.select() ");
	  out.println("	return false");
	  out.println("  }");

	  // Test if anything was entered into the Subject field.
	  out.println("  if (!checkSubject(form.subject.value)) {");
	  out.println("	alert(\"Please enter a subject\")");
	  out.println("	form.subject.focus() ");
	  out.println("	form.subject.select() ");
	  out.println("	return false");
	  out.println("  }");

	  // Test if anything was entered into the Body (Message/Text) field.
	  out.println("  if (!checkBody(form.body.value)) {");
	  out.println("	alert(\"Please make an entry in the Message/Resume box.\")");
	  out.println("	form.body.focus() ");
	  out.println("	form.body.select() ");
	  out.println("	return false");
	  out.println("  }");

	  // Make sure Submit button is hit only once.
	  out.println("  doSubmit(form)"); // THIS MUST GO LAST
    // It made it here so everything else is valid, so return true.
	  out.println("  return true");
	  out.println(" }"); 
	  out.println("// End hiding script from old browsers-->\n</SCRIPT>");
    /**
     * //////////////////// END JAVA SCRIPT/////////////////
     */
	  
	  // Wrap up
	  out.println("</form></body></html>");
	  out.flush();
    }
  

	/**
	* <p>Performs the HTTP POST operation
	*
	* @param req The request from the client
	* @param resp The response from the servlet
	*/
	public void doPost(HttpServletRequest req, HttpServletResponse resp)
                                throws ServletException, java.io.IOException{

		// Set the content type of the response so can't use back button.
		resp.setContentType("text/html");
		resp.setHeader("pragma", "no-cache");
		resp.setDateHeader("Expires", 0);
		resp.setHeader("Cache-Control", "no-cache");

		// Create a PrintWriter to write the response
		java.io.PrintWriter out = new java.io.PrintWriter(resp.getOutputStream());

		// Get the data from the form
		String from = req.getParameter(MAIL_FROM);
		String subject = req.getParameter(MAIL_SUBJECT);
		String body = req.getParameter(MAIL_BODY);

	try{

		System.out.println("UserEmailServlet init: Start");
            	System.out.println("UserEmailServlet init: Loading Database Driver");
		p.put("user", "system");
		p.put("password", "cluster4");

		dbConnection = null;

		Class.forName("weblogic.jdbc.pool.Driver").newInstance();
		Connection dbConnection = DriverManager.getConnection("jdbc:weblogic:pool:blueadsPool", p);
					
		System.out.println("UserEmailServlet init: Getting a connection to - " + dbURL);

		//
		// Find the email address that matches the UID, also known as the UniqueDateId.
		//
		Statement stmt1 = null;
		ResultSet dataResultSet1 = null;

		stmt1 = dbConnection.createStatement();
		dataResultSet1 = stmt1.executeQuery("select Email from AdInfoView where UniqueDateId = '" + uniqueDateId + "'");

		String email = null;
		while(dataResultSet1.next()){						
			email = dataResultSet1.getString("Email").trim();

			if(email != null){
				if(sendMail(email, from, subject, body)){
					// Let the user know that the mail was sent
	    				out.println("<html>");
            				out.println("<head>");
            				out.println("<title>blueads.com User Email</title>");
	    				out.println("<body background=../classified/servletimages/newsbg.jpg bgcolor=white><center>");
            				out.println("<h2>Your email has been sent!</h2>");
	    				out.println("<br><center><a href=/classified/index.jsp><font color=\"BLUE\">Click Here To Continue</font></a>");
            				out.println("</center></body></html>");
	      				// Wrap up
					out.flush();
					break; // get out of while loop.
				}
				else{
					out.println("<head>");
					out.println("<title>blueads.com Email Error</title>");
			       		out.println("<body background=../classified/servletimages/newsbg.jpg bgcolor=white><center>");
				        out.println("<h2>There was an error sending your email, click ");
					out.println("<ALIGN=\"CENTER\"><A HREF=# ONCLICK=\"history.go(-1)\"><FONT COLOR=\"blue\">Here</FONT></A>");
					out.println(" & try again!</h2>");
				        //out.println("<br>Message=" + mex.getMessage());
					out.println("<br><center><a href=/classified/index.jsp><font color=\"BLUE\">Return to Home/Search Page</font></a>");
				        out.println("</center>");
				        out.println("</body></html>");
					// Wrap up
					out.flush();
					break; // get out of while loop.
				}
			}
		}

		try{
			if(dataResultSet1 != null)dataResultSet1.close();
		}
		catch (SQLException ignored){
		}
		finally{
			if(dataResultSet1 != null)dataResultSet1.close();
		}
		
	}
	catch (ClassNotFoundException e){
		System.out.println("Couldn't load database drivers: " + e.getMessage());
	}
	catch (SQLException e) {
		System.out.println("SQLException caught: " + e.getMessage());
	}
	catch (IllegalAccessException e) {
		System.out.println("IllegalAccessException caught: " + e.getMessage());
	}
	catch (InstantiationException e) {
		System.out.println("InstantiationException caught: " + e.getMessage());
	}
	finally {
		try{
			if(dbConnection !=null) dbConnection.close();
		}
      catch (SQLException ignored){}
	}
  }


  public boolean sendMail(String email, String from, String subject, String msgText){

	////////////////////////// Start Mail configuration //////////////////////////

	String host = "mailhost.worldnet.att.net"; 
	//String host = "mail.nfinity.com"; 

	boolean debug = Boolean.valueOf("true").booleanValue(); // Change to false to turn off debug.

	// create some properties and get the default Session
	Properties props = new Properties();
	props.put("mail.smtp.host", host);

	if (debug) props.put("mail.debug", "true"); // Change to false to turn off debug.

	javax.mail.Session emailSession = javax.mail.Session.getDefaultInstance(props, null);
	emailSession.setDebug(debug);

	try {
	    // create a message
	    Message msg = new MimeMessage(emailSession);
	    msg.setFrom(new InternetAddress(from));
	    //InternetAddress[] emailAddress = {new InternetAddress(to)};
	    InternetAddress[] emailAddress = {new InternetAddress(email)};
	    msg.setRecipients(Message.RecipientType.TO, emailAddress);
	    msg.setSubject(subject);
	    msg.setSentDate(new java.util.Date());
	    msg.setText(msgText);	    
	    Transport.send(msg);
	} catch (MessagingException mex) {
		System.out.println("\n--Exception Submit's Email");
		System.out.println("Email is = to " + email);
		return false;
	}								
	////////////////////////// End Mail configuration //////////////////////////
	return true;
    }


    /**
    * <p>Initialize the servlet. This is called once when the
    * servlet is loaded. It is guaranteed to complete before any
    * requests are made to the servlet
    *
    * @param cfg Servlet configuration information
    */

  public void init(ServletConfig cfg)
    throws ServletException
    {
      super.init(cfg);
    }

  /**
    * <p>Destroy the servlet. This is called once when the servlet
    * is unloaded.
    */

  public void destroy()
    {
      super.destroy();
    }
	
}


