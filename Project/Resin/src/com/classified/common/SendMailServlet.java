/**
 * File: SendMailServlet.java
 * Purpose: This servlet is used to send mail to the owner of the
 * Classifed Ads software (YOU!). They will not know your email address.
 *
 * This servlet will format an email form in HTML and, when
 * the user submits the form, will mail the message using
 * SMTP
 * 
 * Notes: to get to work, make sure you change the final variables
 * MAIL_HOST and MAIL_TO. MAIL_TO will be the owner of the Classifed
 * Ads software address and MAIL_HOST will be your service providers
 * smtp host address.
 * 
 * Added com.classified.common.emailProperties.properiies file for the MAIL_HOST
 * & MAIL_TO to make it simpler to change host addresses.
 * 
 * @author  Tom Kochanowicz
 * @version .01
 * @date    04/05/00
 * @version .02
 * @date    03/07/03
 *
 */

package com.classified.common;

// For mail
import javax.mail.*;
import javax.mail.internet.*;
import javax.activation.*;

import java.awt.*;
import java.util.*;
import java.util.Properties;
import javax.servlet.*;
import javax.servlet.http.*;
import java.sql.*;
import java.io.*;
import java.text.*;

import javax.servlet.*;
import javax.servlet.http.*;

public class SendMailServlet extends HttpServlet{
  private String MAIL_FROM = "emailAddr";
  public static String MAIL_SUBJECT = "subject";
  public static String MAIL_BODY = "body";

  // For mail
  ///private final static String MAIL_HOST = "smtp.central.cox.net";
  ///private final static String MAIL_TO = "tkoc@cox.net";
  private static ResourceBundle email_bundle =
    ResourceBundle.getBundle("com.classified.common.emailProperties");


  public void doGet(HttpServletRequest req, HttpServletResponse resp)
                                throws ServletException, java.io.IOException{
    // Set the content type of the response
    resp.setContentType("text/html");

    // Get the PrintWriter to write the response
    java.io.PrintWriter out = resp.getWriter();

    // Create the HTML form
    out.println("<html>");
    out.println("<head>");
    out.println("<title>User Email</title>");
    ///out.println("<body background=../classified/servletimages/newsbg.jpg bgcolor=white>");
    out.println("<body bgcolor=#C9E8F8>"); // light blue

      
    out.println("<center><b><i><FONT SIZE=3 COLOR=#0000a0 FACE=Arial, Helvetica, sans-serif >");
    out.println("We welcome your questions/comments!</font></i></b>");
    out.println("<br><center><IMG src=../classified/servletimages/eagle.gif width=32 height=32 border=0></center>");

///    out.println("<center><h2><font color=\"BLUE\">Email Us About Classified Ads</font></h2>");
    out.println("<br><form method=POST action=\"" + req.getRequestURI() + "\">");
    out.println("<table>");
    out.println("<tr><td><b>Your Email:</b></td>");
    out.println("<td><input type=text name=" + MAIL_FROM + " size=30></td></tr>");
    out.println("<tr><td><b>Subject:</b></td>");
    out.println("<td><input type=text name=" + MAIL_SUBJECT + " size=30></td></tr>");
    out.println("<tr><td><b>Message:</b></td>");
    out.println("<td><textarea name=" + MAIL_BODY + " cols=40 rows=6></textarea></td></tr>");
    out.println("</table><br>");
    out.println("<input type=reset value=\"Reset\">");
    //out.println("<input type=submit value=\"Send\">");
///      out.println("<INPUT TYPE=\"button\" VALUE=\"Submit\" ONCLICK=\"doSubmit(this.form)\"></div>");
    out.println("<INPUT TYPE=\"button\" VALUE=\"Submit\" ONCLICK=\"return submitIt(this.form)\"></div>");
    out.println("</form></center></body></html>");


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

    ////
    // ALL JAVA SCRIPT FUNCTIONS ABOVE ARE CALLED FROM submitIt() BELOW.
    ////

    // Check the email address.
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

    // Test if anything was entered into the Body field.
    out.println("  if (!checkBody(form.body.value)) {");
    out.println("	alert(\"Please make an entry in the text box.\")");
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
     * The code below will close the email window "before" the UserEmailServlet
     * is submitted.
     */
    out.println("<br><center><b><a href=\"javascript: self.close()\"><font face=Verdana,Arial,Helv size=1>Close Window</font></a></b>");
    out.println("</center>"); 
    // Wrap up
    out.println("</body>");
    out.println("</html>");
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
  // Set the content type of the response
  resp.setContentType("text/html");

  // Create a PrintWriter to write the response
  java.io.PrintWriter out = new java.io.PrintWriter(resp.getOutputStream());

  // Get the data from the form
  String emailAddr = req.getParameter(MAIL_FROM);
  String subject = req.getParameter(MAIL_SUBJECT);
  String body = req.getParameter(MAIL_BODY);

  /**
   * -----------------------  Mail configuration --------------------------
   */
				
		///String to = "blue-j@worldnet.att.net";
    //String from = "blueads.com";
    ///String to = "tkoc@cox.net";
    ///String to = MAIL_TO;

		///String host = "mailhost.worldnet.att.net"; 
		//String host = "mail.nfinity.com"; 
    ///String host = MAIL_HOST; 

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
		    msg.setFrom(new InternetAddress(emailAddr));
		    ///InternetAddress[] emailAddress = {new InternetAddress(to)};
        InternetAddress[] emailAddress = {new InternetAddress(email_bundle.getString("MAIL_TO"))};
		    //InternetAddress[] emailAddress = {new InternetAddress(MAIL_FROM)};
		    msg.setRecipients(Message.RecipientType.TO, emailAddress);
		    //msg.setSubject("blueads.com i.d. number");
		    msg.setSubject(subject);
		    msg.setSentDate(new java.util.Date());
		    // If the desired charset is known, you can use
		    // setText(text, charset)
		    msg.setText(body);	    
		    Transport.send(msg);

		    // Let the user know that the mail was sent
		    out.println("<html>");
	      out.println("<head>");
	      out.println("<title>Send Email</title>");
		    out.println("<body background=../classified/servletimages/newsbg.jpg bgcolor=white><center>");
	      out.println("<h2>Your email has been sent!</h2>");
        /**
         * The line of javascript below shows a link that will close the window
         * "after" the email had been submitted.'
         */
        out.println("<br><center><b><a href=\"javascript: self.close()\"><font face=Verdana,Arial,Helv size=1>Close Window</font></a></b>");          
        out.println("</center></body></html>");        
		    ///out.println("<br><center><a href=/index.html><font color=\"BLUE\">Click Here To Continue</font></a>");
	      out.println("</center></body></html>");
        out.flush();
	
		} catch (MessagingException mex){
		  System.out.println("\n--Exception Submit's Email" + mex);
			System.out.println("Email is = to " + email_bundle.getString("MAIL_HOST"));

			// Got an error sending the mail; notify the client
      out.println("<html>");
		  out.println("<head>");
		  out.println("<title>Send Email Error</title>");
      out.println("<body background=../classified/servletimages/newsbg.jpg bgcolor=white><center>");

		  out.println("<h3>There was an error sending your email click ");
			out.println("<ALIGN=\"CENTER\"><A HREF=# ONCLICK=\"history.go(-1)\"><FONT COLOR=\"black\">Here</FONT></A>");
			out.println(" & try again!</h3>");
      /**
       * Tom, the line below actually prints out the email error, when an email
       * can't be reached. Very cool but commenting out so user can see!
       */
		  // out.println("<br>Message=" + mex.getMessage());
      out.println("<br><b><i>Make sure your have a valid email address</i></b></br>");
      /**
       * The line of javascript below shows a link that will close the window
       * "after" the email had been submitted.'
       */
      out.println("<br><center><b><a href=\"javascript: self.close()\"><font face=Verdana,Arial,Helv size=1>Close Window</font></a></b>");          
      out.println("</center></body></html>");
			///out.println("<br><center><a href=../classified/index.jsp><font color=\"BLUE\">Return to Home/Search Page</font></a>");
		  ///out.println("</center>");
		  ///out.println("</body></html>");
		}								
    /** -------------------- End Mail configuration ----------------------- */
      // Wrap up
      out.flush();
  }

  /**
    * <p>Initialize the servlet. This is called once when the
    * servlet is loaded. It is guaranteed to complete before any
    * requests are made to the servlet
    *
    * @param cfg Servlet configuration information
    */

  public void init(ServletConfig cfg)throws ServletException{
      super.init(cfg);
  }

  /**
    * Destroy the servlet. This is called once when the servlet
    * is unloaded.
    */

  public void destroy(){
      super.destroy();
  }

}

