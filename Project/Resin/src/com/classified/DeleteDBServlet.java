/**
 *  This servlet deletes the data from
 *  from the AdInfoView table, using the email & UniqueDateId
 *  @author Tom Kochanowicz,  blue-j@worldnet.att.net
 *  @version 0.1, 3 June, 1999
 *  @version 0.2 Feb, 2003
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
import javax.naming.*;
import java.text.*;

import com.classified.common.Classified;

public class DeleteDBServlet extends HttpServlet{
  // data members
  private String CR = "\n";
  private int isAdigit = 0;  
  private Connection dbConnection;  
  private javax.sql.DataSource pool;

  /**
   * Prepared statement to delete only from the AdInfoView table
   * using the email address and UniqueDateId
   */
  private PreparedStatement deleteStatementAdInfoView;

  static final String Delete_Statement_AdInfoView =
    "delete from AdInfoView where UniqueDateId = ? AND Email = ?";
///    "delete from AdInfoView where UniqueDateId = ?";

  // AdInfoView table
  private final int AD_UNIQUE_DATE_ID_POSITION = 1;
  private final int EMAIL_ADDRESS_POSITION = 2; //??
  
  public void init(ServletConfig config) throws ServletException{
    super.init(config);
      try{
          System.out.println("init() DeleteDBServlet: Start Loading Database Driver");
          Context env = (Context) new InitialContext().lookup("java:comp/env");
          pool = (DataSource) env.lookup("jdbc/Classifieds");

          if (pool == null)
            throw new ServletException("`jdbc/Classifieds' is an unknown DataSource");
      }catch (NamingException e) {
        throw new ServletException(e);
      }
  }

  public void doGet(HttpServletRequest request, HttpServletResponse response)
           throws ServletException, IOException{
    /**
     * Get the session object or create one if not exist. A session will
     * persist as long as the client browser maintains a connection with
     * the server. Also check if their is a connection with the database.
     */
    PrintWriter outputToBrowser =  new PrintWriter(response.getOutputStream());
	 	HttpSession session = request.getSession(true);
    /**
     * We should always get a session back & check for database connection.
     */
		if(session == null){
      outputToBrowser.println("ERROR: Delete Database session is null.");
			outputToBrowser.flush();
			outputToBrowser.close();
			return;
		}

    ///////////////////////// LOGIN TEST  //////////////////////////////
    // THE CODE BELOW MUST BE IN THE SERVLET THAT YOU NEED TO LOG INTO BEFORE
    // YOU CAN USE IT. 
    /**
     * Purpose of code block below checks to see if the user is logged in.
     * If the user is not logged in, they are re-directed to a loginUser.jsp
     * page where they login. Then they are redirected back to the page 
     * they originally tried to log into.
     */
 
    // Does the session indicate this user already logged in?
    Object done = session.getAttribute("loginUser.isDone");		// marker object
    if(done == null){
      // No loginUser.isDone means he hasn't logged in.
      // Save the request URL as the true target and redirect to the login page.

      session.setAttribute("loginUser.target", "/DeleteClassified.jsp");
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
	 synchronized(session){
      /**
       * Put a responce back to the browser to prevent the user from hitting the
       * "Delete" button multiple times and locking up the database. Note that
       * this will not work correctly with out the <br> at the end of the html code.
       */
			try{
        /**
         * build Classified Ad Processing Information page
         */
				StringBuffer htmlPage = new StringBuffer();
				htmlPage.append("<html><head><title>");
				htmlPage.append("Processing Information Page</title></head>");
				htmlPage.append("<center><h4><font color=000000>"); // black font
				htmlPage.append("Please Wait...Processing Information</font></h4></center><br> "); // black letters
				htmlPage.append("</center></body></html>");

      /**
       * now let's send this dynamic data
       * back to the browser
       */
      response.setContentType("text/html");
      outputToBrowser.print(htmlPage);	// Use print instead of println here.
      outputToBrowser.flush(); 
      outputToBrowser.close();

			}catch (Exception e){
        System.out.println("Problem with the DeleteDBServlet class " + e);
        e.printStackTrace();
    	}	 		

		String delete = "Delete";
    /**
     * Get ClassifiedAd data from HTML form and put in Select statement for display
     */
		isAdigit = 0; // reset isAdigit
		String deleteAdIdNum = request.getParameter("DeleteAdIdNum").trim();
		String email = request.getParameter("Email").trim();
		int length = deleteAdIdNum.length();
		char arrayOfNums[] = deleteAdIdNum.toCharArray();


		try {
        dbConnection = null;
        dbConnection = pool.getConnection();

        /**
         * Turn on transactions
         */
        dbConnection.setAutoCommit(false);

        /**
         * set AdInfoView sql parameters for delete statement.
         */
        deleteStatementAdInfoView =
          dbConnection.prepareStatement(Delete_Statement_AdInfoView);

        /**
         * set AdInfoView sql parameters
         */
        deleteStatementAdInfoView.clearParameters();
    
        /**
         * set AdInfoView sql parameters
         */ 
        deleteStatementAdInfoView.setString(AD_UNIQUE_DATE_ID_POSITION, deleteAdIdNum);
        deleteStatementAdInfoView.setString(EMAIL_ADDRESS_POSITION, email);
        System.out.println("DeleteDBServlet init: End");
		}catch (Exception e){
			System.out.println("A problem occured while deleting your classifiedAd. "
				+ " Please try again." + e);
   			e.printStackTrace();
		}

      /**
       *  Check if not numbers and make sure 12 units in delete field.
       */
			for (int i = 0; i < length; i++){
				if (Character.isDigit(arrayOfNums[i])){
					isAdigit++;
				}
			}

		if (isAdigit != 12){
			displayErrorPage(request, response);
    }
    /**
     * Check the email address to see if it is a valid pattern.
     */
		else if(!checkEmail(email)){
			displayEmailErrorPage(request, response);
		}
		else {

			try{
					/**
           * Turn on transactions  /// moved code above
           */
          ///dbConnection.setAutoCommit(false);

          deleteClassifieds(request, response);

          /**
           * Commit statement if no errors.
           */
          dbConnection.commit();
			}catch(Exception e){	
				try{			
					/** Any error is grounds for rollback. */
          outputToBrowser.println("Delete failed. Please contact technical support.");
					dbConnection.rollback();
          }catch(SQLException sqe){
          System.out.println("SQL exception in DeleteDBServlet " + sqe);
          }
			}finally{
        try{
          // close the statments & DB connection
          deleteStatementAdInfoView.close();
        }catch(Exception exp){
          System.out.println("Problem with the deleteStatementAdInfoView.close() statement"); 
        }
        try{
          if(!isConnectionClosed())
            dbConnection.close();
        }catch(SQLException exp){
          System.out.println("Problem with dbConnection.close() " + exp);
        }
      }     
		}
	 } /** end synchronized */
  }

	public void displayErrorPage(HttpServletRequest request, HttpServletResponse response){
		try{			
			Classified aClassified = new Classified(request);

			// build Classified Ad error page
			StringBuffer htmlPage = new StringBuffer();
			htmlPage.append("<html><head><title> Delete Classified Ad Error Page</title></head>");
			htmlPage.append("<body background=../classified/servletimages/newsbg.jpg bgcolor=ff0000>");
			htmlPage.append("<center><h1>Delete Field Error Page</h1></center><hr>");
			htmlPage.append("<b>Sorry...</b>The information for the <b>Delete</b> field does not ");
			htmlPage.append("appear to be valid <b>Press the BACK button on your browser if</b>");
			htmlPage.append("<b> you wish to correct the information you provided.</b>");
			htmlPage.append(" Make sure you use a<b> 12-digit number.</b>");
			htmlPage.append("<hr>");
			htmlPage.append("<center><a href=/classified/index.jsp>Return to Home Page</a>");
			htmlPage.append("<p><i>" + this.getServletInfo() + "</i>");
			htmlPage.append("</center></body></html>");	
			
      /**
       * now let's send this dynamic data
       * back to the browser
       */
			PrintWriter outputToBrowser =  new PrintWriter(response.getOutputStream());
   		response.setContentType("text/html");
   		outputToBrowser.println(htmlPage);
   		outputToBrowser.close();
		}catch (Exception e){
      System.out.println("Problem with Delete Field in DeleteDBServlet " + e);
      e.printStackTrace();
   	}
	}


	public void displayEmailErrorPage(HttpServletRequest request, HttpServletResponse response){
		try{			
			//Classified aClassified = new Classified(request);
      /**
       * build Classified Ad error page
       */
			StringBuffer htmlPage = new StringBuffer();
			htmlPage.append("<html><head><title> Delete Classified Ad Email Error Page</title></head>");
			htmlPage.append("<body background=../classified/servletimages/newsbg.jpg bgcolor=ff0000>");
			htmlPage.append("<center><h1>Email Field Error Page</h1></center><hr>");
			htmlPage.append("<b>Sorry...</b>The information for the <b>Email</b> field does not ");
			htmlPage.append("appear to be valid <b>Press the BACK button on your browser if</b>");
			htmlPage.append("<b> you wish to correct the information you provided.</b>");
			htmlPage.append(" Make sure you use the format<b> your_email@somewhere.com.</b>");
			htmlPage.append("<hr>");
			htmlPage.append("<center><a href=/classified/index.jsp>Return to Home Page</a>");
			htmlPage.append("<p><i>" + this.getServletInfo() + "</i>");
			htmlPage.append("</center></body></html>");	

			/**
       * now let's send this dynamic data
       * back to the browser
       */
			PrintWriter outputToBrowser =  new PrintWriter(response.getOutputStream());
   		response.setContentType("text/html");
   		outputToBrowser.println(htmlPage);
   		outputToBrowser.close();
		}catch (Exception e){
        System.out.println("Problem with Email in DeleteDBServlet class " + e);
        e.printStackTrace();
   	}
	}

  public void deleteClassifieds(HttpServletRequest request, HttpServletResponse response){        
		try {
			Classified aClassified = new Classified(request);
			/**
       * execute the delete statement which returns a count of records deleted.
       * int countCust = deleteStatementCustInfo.executeUpdate();
       */
			int countAdView = deleteStatementAdInfoView.executeUpdate();
			
			if (countAdView >= 1){
        /**
         * UniqueDateId was found in table to delete
         */
				StringBuffer htmlPage = new StringBuffer();
				htmlPage.append("<html><head><title>Delete Classified Ad</title></head>").append(CR);
				htmlPage.append("<body background=../classified/servletimages/newsbg.jpg bgcolor=ff0000>");
				htmlPage.append("<body><center>").append(CR);
				htmlPage.append("<h1>Thank You For Using out Classified Ads</h1>").append(CR);
				htmlPage.append("<hr></center><p>").append(CR);
				htmlPage.append("<center><h3><b> YOUR AD HAS BEEN DELETED, THANK YOU! </b></h3></center>").append(CR);
				htmlPage.append("</table></center>");
				htmlPage.append("<p><hr>");
				htmlPage.append("<center><a href=/classified/index.jsp>Return to Classified Home Page</a>");
				htmlPage.append("<p><i>").append(this.getServletInfo()).append("</i></center>");
				htmlPage.append("</body></html>");

        /**
         * now let's send this dynamic data
         * back to the browser
         */
				PrintWriter outputToBrowser =  new PrintWriter(response.getOutputStream());
	   		response.setContentType("text/html");
	   		outputToBrowser.println(htmlPage);
	   		outputToBrowser.close();
			}else{
        /**
         * ad is already deleted or has never existed.
         */
				StringBuffer htmlPage = new StringBuffer();
				htmlPage.append("<html><head><title>Delete Classified Ad</title></head>").append(CR);
				htmlPage.append("<body background=../classified/servletimages/newsbg.jpg bgcolor=ff0000>");
				htmlPage.append("<body><center>").append(CR);
				htmlPage.append("<h1>Thank You For Using our Classified Ads</h1>").append(CR);
				htmlPage.append("<hr></center><p>").append(CR);
				htmlPage.append("<center><h3><b> NO AD WAS FOUND TO DELETE, PRESS BACK TO TRY AGAIN! </b></h3></center>").append(CR);
				htmlPage.append("</table></center>");
				htmlPage.append("<p><hr>");
				htmlPage.append("<center><a href=/classified/index.jsp>Return to Classified Home Page</a>");
				htmlPage.append("<p><i>").append(this.getServletInfo()).append("</i></center>");
				htmlPage.append("</body></html>");

        /**
         *  now let's send this dynamic data
         *  back to the browser
         */
				PrintWriter outputToBrowser =  new PrintWriter(response.getOutputStream());
	   			response.setContentType("text/html");
	   			outputToBrowser.println(htmlPage);
	   			outputToBrowser.close();
			}      					  			
		}catch (Exception e){
        System.out.println("Problem deleting classified ad " + e);
        e.printStackTrace();
    }
    // put a finally clause here to release connection pool p269 Jason H.
    finally{
      if (dbConnection != null)
        try { 
          //dbConnection.close(); // didn't like this
          deleteStatementAdInfoView.close(); 	
        }catch (SQLException sqe){
          System.out.println("SQLException in finally statement of DeleteDBServlet " +
            "class " + sqe);
        }			
    }
  }

	public boolean checkEmail (String email){
    /**
     * If showing both email & phone or just email, make sure email is not null
     */
		if(email == null)
			return false;

		int length=email.length();
    /**
     * Check format of email to see if no spaces & contains @ and .
     */
		if((length > 3 && length < 31) && (email.indexOf('@') > -1) && (email.indexOf('.') > -1) && 
			(!(email.indexOf(' ') > -1)))
			return true;
		else
			return false;	
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
    System.out.println("DeleteDBServlet: destroy");
    cleanUp();
  }

  public String getServletInfo(){
    ///return "<i>Classified Ads, v.02</i>";
    return "<P><i><FONT size='-1'>Classified Ads, v.02</FONT></i></P>";

  }

	// Test to see if you lost a database
	// connection.

	public boolean isConnectionClosed(){
	   try {
		if (dbConnection != null)
			return dbConnection.isClosed();
		else
			return true;
	   }
	   catch (SQLException e){
			return true;
	   }
	}

}
