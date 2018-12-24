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
import javax.sql.*;
import javax.naming.*;

/** -- For mail -- */
import javax.mail.*;
import javax.mail.internet.*;
import javax.activation.*;

public class UserEmailServlet extends HttpServlet{
  /** --  data members -- */
  private Connection dbConnection;
  private javax.sql.DataSource pool;

  /** -- For mail -- */
  public static String MAIL_FROM = "emailAddr";
  public static String MAIL_SUBJECT = "subject";
  public static String MAIL_BODY = "body";
	public static String uniqueDateId = "UID";   
  private final static String MAIL_HOST = "smtp.central.cox.net";
  /** email host is the same for all servlets, so set it 
   * in emailProperties.properties file
   */
  private static ResourceBundle email_bundle =
    ResourceBundle.getBundle("com.classified.common.emailProperties");

  /**
   * Performs the HTTP GET operation
   * @param req The request from the client
   * @param resp The response from the servlet
   */
  /** --------------------------  Build A FORM  ------------------------------*/   
  public void doGet(HttpServletRequest req, HttpServletResponse resp)
                                throws ServletException, java.io.IOException{
	
	  uniqueDateId = req.getParameter("UID");

		/** -- Set the content type of the response so can't use back button. -- */
		resp.setContentType("text/html");
		resp.setHeader("pragma", "no-cache");
		resp.setDateHeader("Expires", 0);
		resp.setHeader("Cache-Control", "no-cache");

	  // Get the PrintWriter to write the response
	  java.io.PrintWriter out = resp.getWriter();

	  /** ---- Create the HTML form ---- */
	  out.println("<html>");
	  out.println("<head>");
	  out.println("<title>Classified Ads User Email</title>");
    ///out.println("<body background=../classified/servletimages/newsbg.jpg bgcolor=white>");
    out.println("<body bgcolor=#C9E8F8>");

	  out.println("<br><form method=POST action=\"" +
	                  req.getRequestURI() + "\">");
  /**
   * These are the colors and fonts used on this page.
   * cell color (dark blue) #0000a0   font color (white)= #ffffff  face = Arial, Helvetica, sans-serif 
   * Light blue = #C9E8F8
   */
		out.println("<CENTER><P><TABLE BORDER=0 CELLPADDING=0 CELLSPACING=0 WIDTH=66% HEIGHT=367>");
		out.println("<TR><TD  NOWRAP COLSPAN=2 BGCOLOR=\"#0000a0\">");
    out.println("<P ALIGN=CENTER><B><FONT SIZE=3 COLOR=#ffffff FACE=Arial, Helvetica, sans-serif >" + 
      "Email classified ad listing.</FONT></B>");   
		out.println("</TD></TR>");
                                                // dark blue char
    out.println("<TR><TD WIDTH=11%><B><FONT SIZE=2 COLOR=#0000a0 FACE=Arial, Helvetica, sans-serif >" +
      "Your Email:</FONT></B></TD>");    
		out.println("<TD WIDTH=89%><INPUT TYPE=TEXT NAME=" + MAIL_FROM + " SIZE=25><B><FONT COLOR=\"#CC0000\" FACE=Wingdings> +</FONT></B></TD>");
		out.println("</TR>");
		out.println("<TR><TD WIDTH=11%><B><FONT SIZE=2 COLOR=#0000a0 FACE=Arial, Helvetica, sans-serif >" + 
      "Subject:</FONT></B></TD>");
		out.println("<TD WIDTH=89%><INPUT TYPE=TEXT NAME=" + MAIL_SUBJECT + 
      " SIZE=25><B><FONT COLOR=#CC0000 FACE=Wingdings> T</FONT></B></TD>");
		out.println("</TR>");
		out.println("<TR>");
		out.println("<TD COLSPAN=2 BGCOLOR=\"#0000a0\">");
		out.println("<P ALIGN=CENTER><B><FONT SIZE=4 COLOR=#ffffff FACE=Wingdings>@</FONT><FONT SIZE=2 COLOR=#ffffff FACE=Arial, Helvetica, sans-serif >" +
      "Type your message or resume below. <I>Tip: Save");
		out.println("time and use cut &amp; paste.<I></FONT></B><FONT SIZE=4  COLOR=#ffffff FACE=Wingdings>#</FONT>");
		out.println("</TD></TR>");
		out.println("<TR><TD HEIGHT=244 COLSPAN=2><CENTER>");
		out.println("<P><TEXTAREA NAME=" + MAIL_BODY + " ROWS=13 COLS=70></TEXTAREA>");
		out.println("</CENTER></TD></TR></TABLE>");
    out.println("<input type=reset value=\"Reset\">");
    out.println("<INPUT TYPE=\"button\" VALUE=\"Submit\" ONCLICK=\"return submitIt(this.form)\"></div>");
    /**
     * The code below will close the email window "before" the UserEmailServlet
     * is submitted.
     */
    out.println("<br><center><b><a href=\"javascript: self.close()\"><font face=Verdana,Arial,Helv size=1>Close Window</font></a></b>");
    out.println("</center>");



	  /** ---------------------- START JAVA SCRIPT --------------------------- */

    /**
     * JavaScript To Check for Valid Email Entry, Subject and Body.
     */
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

	  /** - Check to make sure something was entered into the Subject field. - */
	  out.println(" function checkSubject(subject) {");
	  out.println(" 	if (subject == \"\") {");
	  out.println(" 		return false");
    out.println("	} ");
    out.println("	return true ");
    out.println(" }");

    /** -- Check to make sure something was entered into the Body field. -- */
	  out.println(" function checkBody(body) {");
	  out.println(" 	if (body == \"\") {");
	  out.println(" 		return false");
	  out.println("	} ");
	  out.println("	return true ");
	  out.println(" }");

	  /** 
     * Submit button can-be-hit-once from the doSubmit() inside 
     * the submitIt() function.
     */
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

	  /** --  Test if anything was entered into the Subject field. -- */
	  out.println("  if (!checkSubject(form.subject.value)) {");
	  out.println("	alert(\"Please enter a subject\")");
	  out.println("	form.subject.focus() ");
	  out.println("	form.subject.select() ");
	  out.println("	return false");
	  out.println("  }");

	  /** - Test if anything was entered into the Body (Message/Text) field. - */
	  out.println("  if (!checkBody(form.body.value)) {");
	  out.println("	alert(\"Please make an entry in the Message/Resume box.\")");
	  out.println("	form.body.focus() ");
	  out.println("	form.body.select() ");
	  out.println("	return false");
	  out.println("  }");

	  /** -- Make sure Submit button is hit only once. -- */
	  out.println("  doSubmit(form)"); // THIS MUST GO LAST
    // It made it here so everything else is valid, so return true.
	  out.println("  return true");
	  out.println(" }"); 
	  out.println("// End hiding script from old browsers-->\n</SCRIPT>");
    /**
     * ------------------------------ END JAVA SCRIPT --------------------------
     */
	  
	  // Wrap up
	  out.println("</form></body></html>");
	  out.flush();
    }
    /** -------------------------  END OF doGet FORM  ------------------------*/

    
  /**
   * From here on, a database connection is made to the database 
   * to find the ads email address & send a message.
   */
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

	/**
	* Performs the HTTP POST operation
	* @param req The request from the client
	* @param resp The response from the servlet
	*/
	public void doPost(HttpServletRequest req, HttpServletResponse resp)
                                throws ServletException, java.io.IOException{
		/** -- Set the content type of the response so can't use back button. -- */
		resp.setContentType("text/html");
		resp.setHeader("pragma", "no-cache");
		resp.setDateHeader("Expires", 0);
		resp.setHeader("Cache-Control", "no-cache");

		/** -- Create a PrintWriter to write the response -- */
		java.io.PrintWriter out = new java.io.PrintWriter(resp.getOutputStream());

    /** We should always get a session back & check for database connection. */
    HttpSession session = req.getSession(true);	
    if(session == null){
      out.println("ERROR: UserEmailServlet Database session is null.");
      out.flush();
      out.close();
      return;
    } 

   /**
    * synchronize, so emails don't overlap each other.
    */
   synchronized(session){
		// Get the data from the form
		String from = req.getParameter(MAIL_FROM);
		String subject = req.getParameter(MAIL_SUBJECT);
		String body = req.getParameter(MAIL_BODY);

	try{

		System.out.println("UserEmailServlet init: Start");
            	System.out.println("UserEmailServlet init: Loading Database Driver");
    dbConnection = null;
    /** get a connection to the database pool */
    dbConnection = pool.getConnection();

    /**
     * Find the email address that matches the UID, also known as the UniqueDateId.
     */
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
          out.println("<title>Classified Ad User Email</title>");
	    		out.println("<body background=../classified/servletimages/newsbg.jpg bgcolor=white><center>");
          out.println("<h2>Your email has been sent!</h2><br><br><br><br><br>");
          /**
           * The line of javascript below shows a link that will close the window
           * "after" the email had been submitted.'
           */
          out.println("<br><center><b><a href=\"javascript: self.close()\"><font face=Verdana,Arial,Helv size=1>Close Window</font></a></b>");          
          out.println("</center></body></html>");
	      	/** --  Wrap up  -- */
					out.flush();
					break; /** -- get out of while loop. -- */
				}
				else{
					out.println("<head>");
					out.println("<title>Classified Ads Email Error</title>");
			    out.println("<body background=../classified/servletimages/newsbg.jpg bgcolor=white><center>");
				  out.println("<h2>There was an error sending your email, click ");
					out.println("<ALIGN=\"CENTER\"><A HREF=# ONCLICK=\"history.go(-1)\"><FONT COLOR=\"blue\">Here</FONT></A>");
					out.println(" & try again!</h2>");
				        //out.println("<br>Message=" + mex.getMessage());
					///out.println("<br><center><a href=/classified/index.jsp><font color=\"BLUE\">Return to Home/Search Page</font></a>");
          out.println("<br><center><b><a href=\"javascript: self.close()\"><font face=Verdana,Arial,Helv size=1>Close Window</font></a></b>");          
          out.println("</center></body></html>");
				  out.println("</center>");
				  out.println("</body></html>");
					/** --  Wrap up  -- */
					out.flush();
					break; /** -- get out of while loop. -- */
				}
			}
		}

		try{
			if(dataResultSet1 != null)dataResultSet1.close();
		}
		catch (SQLException ignored){
		}finally{
			if(dataResultSet1 != null)dataResultSet1.close();
		}		
	}catch (SQLException e) {
		System.out.println("SQLException caught: " + e.getMessage());
	}finally {
		try{
			if(dbConnection !=null) dbConnection.close();
		}
      catch (SQLException ignored){}
    }
   } /** -- end synchronized -- */
  }


  public boolean sendMail(String email, String from, String subject, String msgText){
  /**
   * --------------------- Start Mail configuration ---------------------------
   */
	//String host = "mailhost.worldnet.att.net"; 
	//String host = "mail.nfinity.com"; 

	boolean debug = Boolean.valueOf("true").booleanValue(); // Change to false to turn off debug.

	/** -- create some properties and get the default Session -- */
	Properties props = new Properties();
	///props.put("mail.smtp.host", host);
  props.put("mail.smtp.host", MAIL_HOST);
  ///props.put("mail.smtp.host", email_bundle.getString("MAIL_HOST"));
	if (debug) props.put("mail.debug", "true"); // Change to false to turn off debug.

	javax.mail.Session emailSession = javax.mail.Session.getDefaultInstance(props, null);
	emailSession.setDebug(debug);

	try {
	    /** create a message -- */
	    Message msg = new MimeMessage(emailSession);
	    msg.setFrom(new InternetAddress(from));
	    //InternetAddress[] emailAddress = {new InternetAddress(to)};
	    InternetAddress[] emailAddress = {new InternetAddress(email)};
	    msg.setRecipients(Message.RecipientType.TO, emailAddress);
	    msg.setSubject(subject);
	    msg.setSentDate(new java.util.Date());
	    msg.setText(msgText);	    
	    Transport.send(msg);
	}catch (MessagingException mex){
		System.out.println("\n--Exception Submit's Email");
		System.out.println("Email is = to " + email);
		return false;
	}								
	/** ------------------- End Mail configuration ------------------ */
    return true;
  }

  /**
    * Destroy the servlet. This is called once when the servlet
    * is unloaded. Usually automatically.
    */

  public void destroy(){
      super.destroy();
  }
}


