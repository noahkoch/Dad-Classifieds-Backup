/**
 *  File:  UpdateDBServlet.java
 *  Purpose: This servlet finds a match based on email address and uniqueDateId
 *  taken from the information entered on the UpdateClassified.jsp page,
 *  then displays the ad and provides a user input box for the user to
 *  update the ad and finalizes it on the UpDate2DBServlet.java program.
 *  The user then updates the ad and it replaces (updates) their old ad.
 *  Notes: Alias for UpdateDBServlet is update, alias for Update2DBServlet
 *  is update2
 *
 *  @author Tom Kochanowicz,  blue-j@worldnet.att.net
 *  @version 0.1, 18 April, 2000
 *  @author Tom Kochanowicz,  tkoc@.cox.net
 *  @version 0.2 March 5, 03
 */

package com.classified;

import java.util.regex.*;
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

import com.classified.common.Classified;

public class UpdateDBServlet extends HttpServlet{
  // data members
  private Connection dbConnection;
  private javax.sql.DataSource pool;    

  private PreparedStatement findOldStatementAdInfoView;
        
  // SQL to select the old Classified ad so it can be updated.        
  static final String Find_Old_Statement_AdInfoView =	
    "select WhatAdSays01, Phone, WebSite " +
				"from AdInfoView where UniqueDateId = ? AND Email = ?";        

  // For findOldStatementAdInfoView prepared statement.
  private final int FIND_UNIQUE_DATE_ID_POSITION = 1;
  private final int FIND_EMAIL_POSITION = 2;

  private static final String CR = "\n";

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

  public void doGet(HttpServletRequest request, HttpServletResponse response)
           throws ServletException, IOException{

		PrintWriter outputToBrowser =  new PrintWriter(response.getOutputStream());
		HttpSession session = request.getSession(true);


		// We should always get a session back & check for database connection.
		if(session == null){
			outputToBrowser.println("ERROR: Manage Database session is null.");
			outputToBrowser.flush();
			outputToBrowser.close();
			return;
		}

    ///////////////////////// LOGIN TEST  //////////////////////////////
    // THE CODE BELOW MUST BE IN THE SERVLET THAT YOU NEED TO LOG INTO BEFORE
    // YOU CAN USE IT. 
    /**
     * Purpose of code below checks to see if the user is logged in.
     * If the user is not logged in, they are re-directed to a loginUser.jsp
     * page where they login. Then they are redirected back to the page 
     * they originally tried to log into.
     */
 
		// Does the session indicate this user already logged in?
		Object done = session.getAttribute("loginUser.isDone");		// marker object
		if(done == null){
			// No loginUser.isDone means he hasn't logged in.
			// Save the request URL as the true target and redirect to the login page.

			session.setAttribute("loginUser.target", "/UpdateClassified.jsp");
			// session.putValue("loginUser.target",
			// HttpUtils.getRequestURL(request).toString());
			///response.sendRedirect(request.getScheme() + "://" +
			///request.getServerName() + ":" + request.getServerPort() +
			///	"/loginUser.jsp");
      /** Redirect to login page */
      response.sendRedirect("/classified/loginUser.jsp");
			return;
		}
    /////////////////////  END TEST  ////////////////////////////

		// Get the action from the UpdateClassified.jsp page. For step1 or step2.
		String action = request.getParameter("action");
		String updateAdIdNum = request.getParameter("UpdateAdIdNum").trim(); // Same as UniqueDateId
		String email = request.getParameter("emailAddr").trim();

		//
		// Get ClassifiedAd data from HTML form and put in Select statement for display
		//

		System.out.println("UpdateDBServlet init: Preparing display statement");

	 if(action.equals("step1")){
	
		try{
		  ///synchronized(dbConnection){				

      /** Get a connection to the pool */
      dbConnection = null;
      dbConnection = pool.getConnection();

      // Turn on transactions
      dbConnection.setAutoCommit(false); 
      
      /**
       * SQL to select the old Classified ad so it can be updated.
       */
      findOldStatementAdInfoView =
        dbConnection.prepareStatement(Find_Old_Statement_AdInfoView);
			
			// clear parameters
			findOldStatementAdInfoView.clearParameters();
						
			// set position 1 parameters.
			findOldStatementAdInfoView.setString(FIND_UNIQUE_DATE_ID_POSITION, updateAdIdNum);
			findOldStatementAdInfoView.setString(FIND_EMAIL_POSITION, email);


      /**
       * Call the executeUpdate statement & pass email &
       * uniqueDateId <alias updateAdIdNum> to a hidden field .
       * synchronized the instance.
       */
      synchronized(this){        
        findOldClassifieds(request, response, email, updateAdIdNum);
      }
        
			// close statement
			findOldStatementAdInfoView.close();

			// Commit statement if no errors.
			dbConnection.commit();
		}catch(Exception e){	
				try{
          System.out.println("Error UpdateDBServlet rolling back " + e);
					// Any error is grounds for rollback.
					dbConnection.rollback();
				}
				catch(SQLException sqe){
          System.out.println("You have an SQL exception in UpdateDBServlet: " + sqe);
				}
				outputToBrowser.println("Select failed. Please contact technical support.");
		}finally{
      try{
        // Close prepared statement after execution.
        findOldStatementAdInfoView.close();        
      }catch (SQLException sqe){
        System.out.println("Exception in finally or UpdateDBServlet " + sqe);
      }			
      try{ // close the database connection if not closed.
          if(!isConnectionClosed())
          dbConnection.close();
      }catch(Exception e){
         System.out.println("Problem with UpDateDBServet dbConnection.close() statement " + e);
      }
    } 
	 }
  }

  public void findOldClassifieds(HttpServletRequest request, 
          HttpServletResponse response, String email, String uniqueDateId){
        
	try{
		// Find the Classified Ad from the AdInfoView Table that needs to 
		// be updated and diplay it.

		ResultSet rs = null;
		rs = findOldStatementAdInfoView.executeQuery();

		// Get the original data from the AdInfoView table & display WhatAdSays01, Phone & WebSite
		StringBuffer htmlPage = new StringBuffer();
		htmlPage.append("<html><head><title>Find Classified Ad To Update</title></head>");
///		htmlPage.append("<BODY BACKGROUND=\"../classified/servletimages/newsbg.jpg\" >");
    htmlPage.append("<BODY BGCOLOR='#C9E8F8'>");

		// JavaScript To prevent user hitting Submit button too many times.
		htmlPage.append("<SCRIPT LANGUAGE=\"JavaScript\"> <!-- Hide script from old browsers\n ");
		htmlPage.append(" var submitted = false;");
		htmlPage.append(" function doSubmit(form)");
		htmlPage.append(" {");
		htmlPage.append(" if (!submitted) {");
		htmlPage.append(" submitted = true;");
		htmlPage.append(" form.submit();");
		htmlPage.append(" }");
		htmlPage.append(" }");
		htmlPage.append("// End hiding script from old browsers-->\n</SCRIPT>");

		// JavaScript to limit WhatAdSays to 1000 chars in IE only.
		/** Check Value of TextArea field for IE browsers so text area does not exceed 1000 chars.
		 * Limit the length of textarea.
		 * There are two parameters, 
		 * the first, item, is the TEXTAREA itself.  
		 * The second, maxlen, is the maximum number 
		 * of characters you want to permit.
		 * To use the function, you arrange to call 
		 * OnKeyDownInTextArea in response to an 
		 * onkeydown event within the the TEXTAREA, e.g.:
		 */
		htmlPage.append("<SCRIPT LANGUAGE=\"JavaScript\"><!-- Hide script from old browsers\n ");
		htmlPage.append(" function OnKeyDownInTextArea( item, maxlen ) { ");
		htmlPage.append(" if ( item.value.length > maxlen - 1 ) { ");
		htmlPage.append("	if ( event.keyCode != 8 && event.keyCode <= 32 || event.keyCode > 46 ) { "); 		
		htmlPage.append("         alert( \"You are at the limit of 1000 spaces.\" ); "); 
		htmlPage.append("	  event.returnValue = false; ");
		htmlPage.append("	} ");
		htmlPage.append(" } ");
		htmlPage.append("} ");
		htmlPage.append(" //--> ");
		htmlPage.append(" </SCRIPT> ");

		// JavaScript to limit WhatAdSays to 1000 chars in NetScape only.
		htmlPage.append("<SCRIPT LANGUAGE=\"JavaScript\"><!-- Hide script from old browsers\n ");
		htmlPage.append(" function validate(ad) { ");
		htmlPage.append("    if (ad.length > 1000) { ");
		htmlPage.append("        alert('Your ad must be 1000 characters or less, including spaces, please shorten your ad.'); ");
		htmlPage.append("        return false; ");
		htmlPage.append("    } ");
		htmlPage.append("    return true; ");
		htmlPage.append(" } ");
		htmlPage.append(" //--></SCRIPT> ");

		/** doPost Here */
		htmlPage.append("<FORM ACTION=\"update2\" METHOD=\"POST\" ENCTYPE=\"application/x-www-form-urlencoded\" NAME=\"submit\">");

    /** Thank You For Using */
		htmlPage.append("<H3 ALIGN=\"CENTER\"><B><FONT color=\"#0000a0\" face=\"Arial, Helvetica, sans-serif\">Thank You For Using Omaha's");
		htmlPage.append(" Premier Classifed Ads</FONT></H3>");

    /** eagle.gif */
    htmlPage.append("</CENTER>");
    htmlPage.append("<P ALIGN=\"CENTER\"><IMG src=\"servletimages/eagle.gif\" width=\"32\" height=\"32\" align=\"BOTTOM\" border=\"0\"></P>");
    htmlPage.append("<CENTER>");

		// Works only if there is an ad to display.
		String whatAdSays01 = null;
		String webSite = null;
    String phone = null;  
		
		while(rs.next()){
		   whatAdSays01 = rs.getString("WhatAdSays01").trim();
		   webSite = rs.getString("WebSite");	       
		   phone = rs.getString("Phone");
		}

		// If there is nothing to update, display the error page and skip everything else.
		if(whatAdSays01 == null){
			errorMessage(response);
			return;
		}


    /**
     * The code below finds all matches to a regular expression pattern. It reads
     * in WhatAdSays01 and seperates the title and whatAdSay01, getting rid of the
     * enclosing html tags and replaces them with a blank ("").
     * For example see: http://javaalmanac.com/egs/java.util.regex/BasicReplace.html
     */

    /** ----------------- Start parsing WhatAdSays01 ------------------ */    
    CharSequence originalAd = whatAdSays01;
    /**
     * Parse the html tags around the title so only
     * the actual text of the title is saved.
     * note the | is a logical or. . and * parses out the rest of ad.
     */
    String titlePattern = "<b><center>|. . .</center></b>.*";

    /**
     * Parse out the title of the ad and the tags around the title
     * leaving just the ad.
     */
    String whatAdSays01Pattern = "<b><center>.*</center></b>"; /** This works for the ad! */
    String replacementStr = "";

    // Compile with dotall enabled to parse accross new lines.
    Pattern title = Pattern.compile(titlePattern, Pattern.DOTALL);
    
    // Compile regular expression
    Pattern whatAdSays = Pattern.compile(whatAdSays01Pattern);
    
    // Replace all occurrences of pattern in input
    Matcher matcherTitle = title.matcher(originalAd);
    Matcher matcherWhatAdSays = whatAdSays.matcher(originalAd);
    String newTitle = matcherTitle.replaceAll(replacementStr);
    String newWhatAdSays = matcherWhatAdSays.replaceAll(replacementStr);
    ///System.out.println("New Title: " + newTitle);
    ///System.out.println("------------------------------------------");    
    ///System.out.println("New What Ad Says: " + newWhatAdSays);
    /** ----------------- End parsing WhatAdSays01 ------------------ */


    /** Change the way you want your ad to look below */
		htmlPage.append("<P><TABLE BORDER=\"0\" CELLPADDING=\"0\" CELLSPACING=\"0\" WIDTH=\"35%\" BGCOLOR=\"#0000a0\">");
		htmlPage.append("<TR><TD WIDTH=\"100%\" NOWRAP><FONT color=\"#ffffff\" size=\"2\" face=\"Arial, Helvetica, sans-serif\">");
		htmlPage.append("<center><b>Change the way you want your ad to look below and click the Submit button.</b></center></font>");
		htmlPage.append("<FONT color=\"#ffffff\" size=\"1\" face=\"Arial, Helvetica, sans-serif\">");
    htmlPage.append("<center>");
    htmlPage.append("Note: Only title, ad, website and phone can be updated. All other selections will remain the same. ");
		htmlPage.append("</center></FONT></TD>");
		htmlPage.append("</TR></TABLE>");

    /** Choose a title for your ad: */
    htmlPage.append("<BR>");
    htmlPage.append("<CENTER>");
    htmlPage.append("<TABLE cellpadding=\"0\" cellspacing=\"0\" height=\"54\" width=\"455\">");
    htmlPage.append("<COL span=\"1\" align=\"center\">");
    htmlPage.append("<TR>");
    htmlPage.append("<TD nowrap bgcolor=\"#0000a0\" align=\"center\" width=\"455\">");
    htmlPage.append("<FONT color=\"#ffffff\" size=\"2\" face=\"Arial, Helvetica, sans-serif\">");
     htmlPage.append("<B>Choose a title for your ad:</B> This will automatically show up in <B>bold </B>print</FONT></TD>");
    htmlPage.append("</TR>");
    htmlPage.append("<TR>");   
    htmlPage.append("<TD nowrap width=\"455\" align=\"center\">");
    /** newTitle */
    htmlPage.append("<INPUT size='50' type='text' maxlength='50' name='AdTitle'  value=\""  + newTitle + "\">");
                                           
    htmlPage.append("</TD></TR>");
    htmlPage.append("</TABLE>");
    htmlPage.append("</CENTER>");

    /** WhatAdSays01 */
		htmlPage.append("<P><TEXTAREA NAME=\"WhatAdSays01\" ROWS=\"16\" COLS=\"64\" STYLE=\"overflow : hidden\" WRAP=\"PHYSICAL\" ");
		htmlPage.append(" onkeydown=\"OnKeyDownInTextArea( this,1000 )\" onChange=\"validate(this.value)\">");
    /** Ad the newWhatAdSays variable to textarea */
		htmlPage.append(newWhatAdSays + "</TEXTAREA><FONT SIZE=\"2\" FACE=\"Times New Roman\"></FONT></P>");

		htmlPage.append("<P><B><FONT SIZE=\"2\" COLOR=\"#000099\" FACE=\"Times New Roman\">Optional Website: Do not ad http:// </FONT></B>");
		htmlPage.append("<FONT SIZE=\"2\" COLOR=\"#000099\" FACE=\"Times New Roman\"><INPUT TYPE=\"TEXT\" NAME=\"WebSite\" ");
    /** Ad the webSite variable to the text field */
		htmlPage.append("SIZE=\"40\" MAXLENGTH=\"60\" VALUE=" + webSite + ">");
    htmlPage.append("</FONT><B><FONT SIZE=\"2\" COLOR=\"#000099\" FACE=\"Times New Roman\">");
		htmlPage.append("</FONT></B></P>");
    /** phone */
		htmlPage.append("<P><B><FONT SIZE=\"2\" COLOR=\"black\" FACE=\"Times New Roman\">Phone: </FONT></B>");
		htmlPage.append("<FONT SIZE=\"2\" COLOR=\"black\" FACE=\"Times New Roman\">");
    /** Ad the phone variable to the text field */
		htmlPage.append("<INPUT TYPE=\"TEXT\" NAME=\"Phone\" SIZE=\"12\" MAXLENGTH=\"12\" VALUE=" + phone + ">");
		htmlPage.append("</FONT><B><FONT SIZE=\"1\" COLOR=\"black\" FACE=\"Times New Roman\">");
		htmlPage.append("</FONT><FONT SIZE=\"2\" COLOR=\"#000099\" FACE=\"Times New Roman\">");
		htmlPage.append("(nnn-nnn-nnnn)</FONT></B></P><P><TABLE BORDER=\"0\" CELLPADDING=\"0\" CELLSPACING=\"0\" WIDTH=\"31%\" HEIGHT=\"35\">");
		htmlPage.append("<TR><TD WIDTH=\"50%\"><CENTER><P><INPUT TYPE=\"RESET\" NAME=\"B2\" VALUE=\"Clear Form\">");
		htmlPage.append("</CENTER></TD><TD WIDTH=\"50%\">");

		htmlPage.append("<P ALIGN=\"CENTER\"><INPUT TYPE=\"button\" VALUE=\"Update Ad\" onClick=\"doSubmit(this.form)\">");
		htmlPage.append("</TD></TR></TABLE><A HREF=\"../classified/index.jsp\"><B><FONT SIZE=\"2\" FACE=\"Times New Roman\">Home/View Ads</FONT>");
		htmlPage.append("</B></A><B><FONT SIZE=\"2\" FACE=\"Times New Roman\"> | </FONT></B>");
		htmlPage.append("<A HREF=\"../DeleteClassified.shtml\"><B><FONT SIZE=\"2\" FACE=\"Times New Roman\">");
		htmlPage.append("Delete Ad</FONT></B></A><B><FONT SIZE=\"2\" FACE=\"Times New Roman\"> | </FONT></B><A HREF=\"../classified/help.jsp\">");
		htmlPage.append("<B><FONT SIZE=\"2\" FACE=\"Times New Roman\">Help</FONT></B></A><BR>");

		htmlPage.append("<INPUT TYPE=\"HIDDEN\" NAME=\"action\" SIZE=\"-1\" VALUE=\"step2\">");
		htmlPage.append("<INPUT TYPE=\"HIDDEN\" NAME=\"Email\" SIZE=\"-1\" VALUE=").append(email).append(">");
		htmlPage.append("<INPUT TYPE=\"HIDDEN\" NAME=\"UniqueDateId\" SIZE=\"-1\" VALUE=").append(uniqueDateId).append(">");

		htmlPage.append("</FONT></B></CENTER></FORM>");

		htmlPage.append("</BODY>");
		htmlPage.append("</HTML>");

    /**
     * now let's send this dynamic data
     * back to the browser
     */
		PrintWriter outputToBrowser =  new PrintWriter(response.getOutputStream());
    // Set so page is not saved
    response.setHeader("Expires", "Mon, 01 Jan 1990 06:00:01 GMT");
    response.setHeader("Cache-Control", "no-cache");
    response.setHeader("Pragma", "no-cache");
    
   	response.setContentType("text/html");
   	outputToBrowser.println(htmlPage);
    outputToBrowser.flush();
   	outputToBrowser.close();
					
    }catch (Exception e){
      System.out.println("Error in UpdateServlet " + e);
      ///cleanUp();
      e.printStackTrace();
    }

  }


  public void errorMessage(HttpServletResponse response){
    try{
      // ad does not exist.
      StringBuffer htmlPage = new StringBuffer();
      htmlPage.append("<html><head><title>Update Classified Ad</title></head>").append(CR);
      htmlPage.append("<body background=../classified/servletimages/newsbg.jpg bgcolor=ff0000>");
      htmlPage.append("<body><center>").append(CR);
      htmlPage.append("<h3>Thank You For Using Classified Ads</h3>").append(CR);
      htmlPage.append("<hr></center><p>").append(CR);
      htmlPage.append("<center><h3><b> NO ADS WERE FOUND TO UPDATE, PRESS BACK TO TRY AGAIN! </b></h3></center>").append(CR);
      htmlPage.append("</table></center>");
      htmlPage.append("<p><hr>");
      htmlPage.append("<center><a href=../classified/index.jsp>Return to Classified Home Page</a>");
      htmlPage.append("<p><i>").append(this.getServletInfo()).append("</i></center>");
      htmlPage.append("</body></html>");

      // now let's send this dynamic data
      // back to the browser
      PrintWriter outputToBrowser =  new PrintWriter(response.getOutputStream());
      response.setContentType("text/html");
      outputToBrowser.println(htmlPage);
      outputToBrowser.close();
    }catch (Exception e){
      ///cleanUp();
      e.printStackTrace();
    }
  }      					  	

  public void cleanUp(){
    try {
      System.out.println("Closing database connection");
      dbConnection.close();
    }catch (SQLException e){
        e.printStackTrace();
    }
  }

  public void destroy(){
    System.out.println("UpdateDBServlet: destroy");
    ///cleanUp();
  }

  public String getServletInfo(){
    return "<i>Classified Ads v.02</i>";
  }

	// Test to see if you lost a database
	// connection with Symantec dbANYWHERE

	public boolean isConnectionClosed() {
		try{
			if (dbConnection != null)
			return dbConnection.isClosed();
			else
				return true;
		}catch (SQLException e){
				return true;
		}
	}
}
