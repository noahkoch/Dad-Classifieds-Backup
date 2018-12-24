package com.classified.customer;
/**
 * File:  BookMarkDBServlet.java
 * Purpose: To allow the user to bookmark an ad into the
 * SAVED_ADS table so they can later be retrieved.
 * @author Tom Kochanowicz,  tkoc@cox.net
 * @version 0.1, 26 April. 2003 
 */

import java.util.*;
import java.util.Properties;
import javax.servlet.*;
import javax.servlet.http.*;
import java.sql.*;
import javax.sql.*;
import javax.naming.*;
import java.io.*;
import java.text.*;


import com.classified.common.Classified;

public class BookMarkDBServlet extends HttpServlet{
    // data members
    private Connection dbConnection;  
    private DataSource pool;


    /**
     * For insertStatementBookMark prepared statement.
     * Insert the UniqueDateId number and the email address (that was retrieved
     * from the session once the user logs in) into the SAVED_ADS table.
     * The user click the book in the SearchDBServlet a.k.a. search page.
     */
    private PreparedStatement insertStatementBookMark; 
    static final String Insert_Statement_BookMark =
      "insert into SAVED_ADS (EMAIL_ADDRESS, UniqueDateId) " +
      "values (?, ?)";

    private final int EMAIL_ADDRESS_1 = 1;
    private final int UNIQUE_DATE_ID_2 = 2;   // a.k.a. Ad Id Number



    /**
     * Delete ads ending on a specified date from ManageClassified.jsp
     * Delete Ads From Database For Ads Ending On mm/dd/yy
     */
    private PreparedStatement deleteStatementAdInfoView;
    private PreparedStatement deleteStatementAdInfo;
    private PreparedStatement deleteStatementCustInfo;
    static final String Delete_Statement_AdInfoView =
      "DELETE FROM AdInfoView USING "
      + "AdInfoView, CustInfo WHERE "
      + "AdInfoView.UniqueDateId = CustInfo.UniqueDateId AND "
      + "CustInfo.EndDate = ?";
    static final String Delete_Statement_AdInfo =
      "DELETE FROM AdInfo USING "
      + "AdInfo, CustInfo WHERE "
      + "AdInfo.UniqueDateId = CustInfo.UniqueDateId AND "
      + "CustInfo.EndDate = ?";
    static final String Delete_Statement_CustInfo =
      "delete from CustInfo USING "
      + "CustInfo WHERE CustInfo.EndDate = ?";
    // For the three prepared delete statements.
    private int AD_END_DATE_INFO_VIEW_POSITION_1 = 1; // try putting final back into these!
    private int AD_END_DATE_INFO_POSITION_1 = 1;
    private int AD_END_DATE_CUST_INFO_POSITION_1 = 1;


    /**
     * List all the bookmarks the user has based on their email
     * address and the UniqueDateId.
     */
    private PreparedStatement bookmarkStatement;
    static final String Bookmark_Statement =
      "select * from CustInfo, AdInfo where " +
			"CustInfo.StartDate = ?" +
			" AND CustInfo.UniqueDateId = AdInfo.UniqueDateId order by " +
			"CustInfo.UniqueDateId desc";
    // For bookmarkStatement prepared statements.
    private int CHECK_START_DATE_POSITION_1 = 1;

    /**
     * Delete Bad Ads from AdInfo, AdInfoView & CustInfo db via ManageClassified.jsp
     * Delete Bad Ads from AdInfo, AdInfoView & CustInfo db <12 digit code>
     */
    private PreparedStatement deleteBadAdInfo;
    private PreparedStatement deleteBadAdInfoView;
    private PreparedStatement deleteBadCustInfo;
    static final String Delete_Bad_AdInfo =
      "delete from AdInfo where UniqueDateId = ?";      
    static final String Delete_Bad_AdInfoView =
      "delete from AdInfoView where UniqueDateId = ?";
    static final String Delete_Bad_CustInfo =
      "delete from CustInfo where UniqueDateId = ?";      
    // For deleteBad... statements
    private int DELETE_BAD_ADINFO_1 = 1;
    private int DELETE_BAD_ADINFO_VIEW_1 = 1;
    private int DELETE_BAD_CUSTINFO_1 = 1;

    private final String CR = "\n";
    private Properties p = new Properties();
    private int isAdigit = 0;

 
  public void init(ServletConfig config) throws ServletException{
    super.init(config);
      try{
          Context env = (Context) new InitialContext().lookup("java:comp/env");
          pool = (DataSource) env.lookup("jdbc/Classifieds");

          if (pool == null)
            throw new ServletException("`jdbc/Classifieds' is an unknown DataSource");
      }catch (NamingException e){
        System.out.println("ManageDBServlet NamingException " + e);
        throw new ServletException(e);
      }   
  }
    /**
     * synchronize the whole doGet method. Performance should not be an issue   
     * since only one administrator would be using it a a single time.
     */
    public synchronized void doGet(HttpServletRequest request, HttpServletResponse response)
           throws ServletException, IOException{

	 		PrintWriter outputToBrowser =  new PrintWriter(response.getOutputStream());
	 		HttpSession session = request.getSession(true);


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


session.setAttribute("loginUser.target", "/bk_mrk.htm"); // THIS ONE WORKED ok,
// make it be a blank window informing the user that they can now bookmark their
// ads after they close the window.

//// START TEST
      //session.setAttribute("loginUser.target", request.getRequestURL().toString());
      /// Note: the line below and above will direct you to the page you want initially.
      ///System.out.println("This is the target I think I want " + request.getRequestURL().toString());
      ///response.sendRedirect(request.getScheme() + "://" +
      ///request.getServerName() + ":" + request.getServerPort() +
      ///	"/loginUser.jsp");
/// END TEST       
      /** Redirect to login page */
      response.sendRedirect("/classified/loginUser.jsp");


      return;
    }
    /////////////////////  END TEST  ////////////////////////////
           
				
				// We should always get a session back & check for database connection.
				if(session == null){
					outputToBrowser.println("ERROR: Bookmark Database session is null.");
 					System.out.println("ERROR: Bookmark Database session is null.");
					outputToBrowser.flush();
					outputToBrowser.close();
					return;
				}		


//// TEST
String userEmail = (String)session.getAttribute("loginUser.isDone"); // TEST 
System.out.println("The user who logged into BookMarkDBServlet session is none other then " +
  userEmail);

////// END TEST
        

		/** 
     * Get the choice from the search page. 
		 */ 

		String choice = request.getParameter("Choice");

// TEST    
        /// YOU NEED THE LINE BELOW
    		String savedAdId = request.getParameter("UID"); // a.k.a.uniqueDateId
System.out.println("The value of savedAdId is " + savedAdId);

        // This one below works, IF you are logged in.
        //String may look like http://192.168.1.102/classified/bookmark?UID=12345678901
// END TEST        
		String adStartDate = request.getParameter("AdStartDate");
    
		String adEndDate = request.getParameter("AdEndDate");
		String creditDate = request.getParameter("CreditDate");
		String badAd = request.getParameter("BadAd");

		/**
     * Get ClassifiedAd data from HTML form and put in Select statement for display
     */

///  if(choice.equals("MoveAds")){
  if((savedAdId != null) && (userEmail != null)){


    try {  
		  ///synchronized(dbConnection){				
      /**
       * For insertStatementBookMark prepared statement.
       * Insert the UniqueDateId number and the email address (that was retrieved
       * from the session once the user logs in) into the SAVED_ADS table.
       * The user click the book in the SearchDBServlet a.k.a. search page.
       */
      dbConnection = null;
      dbConnection = pool.getConnection();


      /**
       * Turn on transactions
       */
       dbConnection.setAutoCommit(false);
       
      insertStatementBookMark =	
        dbConnection.prepareStatement(Insert_Statement_BookMark);

			// clear parameters
			insertStatementBookMark.clearParameters();

      /**
       * userEmail is retrieved when the user logs into the session, the
       * savedAdId (a.k.a. uniqueDateId) is passed as a parameter from the SearchDBServlet 
       * (a.k.a. search) generated html page.
       */
      // set position  parameters.
      insertStatementBookMark.setString(EMAIL_ADDRESS_1, userEmail);  
      insertStatementBookMark.setString(UNIQUE_DATE_ID_2, savedAdId);   
       			
				try{
					// Turn on transactions
					/// dbConnection.setAutoCommit(false); // code moved above
					// Call the executeUpdate statement.
					insertBookMark(request, response);
					// Commit statement if no errors.
					dbConnection.commit();
				}
				catch(Exception e){	
					try{			
						// Any error is grounds for rollback.
						dbConnection.rollback();
					}	catch(Exception exp){      
              System.out.println("Have an exception, rolling back transaction " + exp);
					}
					outputToBrowser.println("Insert failed. Please contact technical support.");
				}
		  ///}
		}catch (Exception e){
			System.out.println("A problem occured while inserting your bookmarked ad. "
				+ " Please try again." + e);
		}finally{
      try{
        // close the statments & DB connection
        insertStatementBookMark.close();
        }catch(Exception e){
          System.out.println("Problem with the insertStatementBookMark.close() statement"); 
        }
        try{
          if(!isConnectionClosed())
              dbConnection.close();
        }catch(Exception e){
            System.out.println("Problem with insertStatements dbConnection.close() " + e);
        }
    }
	}else if(choice.equals("DeleteAds")){ 
		/**
     * Delete ads that end on the specified date. 
     */
		if ((adEndDate.length() != 8) || (adEndDate.charAt(2) != '/') 
		 || (adEndDate.charAt(5) != '/') || (!Character.isDigit(adEndDate.charAt(0)))
		 || (!Character.isDigit(adEndDate.charAt(1))) || (!Character.isDigit(adEndDate.charAt(3)))
		 || (!Character.isDigit(adEndDate.charAt(4))) || (!Character.isDigit(adEndDate.charAt(6)))
		 || (!Character.isDigit(adEndDate.charAt(7))) ){
			try{			

				// build Classified Ad error page
				StringBuffer htmlPage = new StringBuffer();
				htmlPage.append("<html><head><title> Manage Classified Delete Ad Error Page</title></head>");
				htmlPage.append("<body background=../classified/servletimages/newsbg.jpg bgcolor=ff0000>");
				htmlPage.append("<center><h1>Manage Field Error Page</h1></center><hr>");
				htmlPage.append("<b>Sorry...</b>The information for the <b>End Date</b> field does not ");
				htmlPage.append("appear to be valid <b>Press the BACK button on your browser if</b>");
				htmlPage.append("<b> you wish to correct the information you provided.</b>");
				htmlPage.append(" Make sure you use a<b> The date format mm/dd/yy</b>");
				htmlPage.append("<hr>");
				htmlPage.append("<center><a href=../classified/index.jsp>Return to Home Page</a>");
				htmlPage.append("<p><i>" + this.getServletInfo() + "</i>");
				htmlPage.append("</center></body></html>");	
			
	   			// now let's send this dynamic data
	   			// back to the browser
		
	   			response.setContentType("text/html");
	   			outputToBrowser.println(htmlPage);
	   			outputToBrowser.close();
				return;
			}catch (Exception e){
	      //cleanUp();
	      e.printStackTrace();
	   	}
		}

		else try {
			///synchronized(dbConnection){				

      /**
       * The three delete statements are for MySQL and the ManageClassified.jsp 
       * page choice of "Delete Ads From Database For Ads Ending On dd/mm/yy. 
       */ 
      dbConnection = null;
      dbConnection = pool.getConnection();

      // Turn on transactions
			dbConnection.setAutoCommit(false);
      
      deleteStatementAdInfoView =         
        dbConnection.prepareStatement(Delete_Statement_AdInfoView);          

      deleteStatementAdInfo =
        dbConnection.prepareStatement(Delete_Statement_AdInfo);          

      deleteStatementCustInfo =
        dbConnection.prepareStatement(Delete_Statement_CustInfo);

      // clear parameters
			deleteStatementAdInfoView.clearParameters();
			deleteStatementAdInfo.clearParameters();
			deleteStatementCustInfo.clearParameters();
			
			// set statements      
			deleteStatementAdInfoView.setString(AD_END_DATE_INFO_VIEW_POSITION_1 = 1, adEndDate);
    	deleteStatementAdInfo.setString(AD_END_DATE_INFO_POSITION_1 = 1, adEndDate);
    	deleteStatementCustInfo.setString(AD_END_DATE_CUST_INFO_POSITION_1 = 1, adEndDate);
	
			///}
    }catch (Exception exp){
			System.out.println(exp + "A problem occured while deleting your classifiedAd. "
				+ " Please try again." + exp);
   		exp.printStackTrace();
		}

		try{				
			// Turn on transactions
			//dbConnection.setAutoCommit(false); // moved code above
      // execute the statemensts
			deleteClassifieds(request, response);
      // Commit statement if no errors.
			dbConnection.commit();
		}
		catch(Exception e){
      System.out.println("An exception occured when calling " +
      "the deleteClassifieds(request, response); statement " + e);
      // Any error is grounds for rollback.
      try{			
				// Any error is grounds for rollback.
				dbConnection.rollback();
			}catch(SQLException exp){
        System.out.println("deleteClassifieds exception " + exp);
        outputToBrowser.println("Delete failed. Please contact technical support.");
			}
    }
    // put a finally clause here to release connection pool p269 Jason H.
    finally{
      try{
        // Close all statements after execution.
        deleteStatementAdInfoView.close();
      }catch(Exception e){
        System.out.println("Problem with the deleteStatementAdInfoView prepared statement " + e);
      }
      try{
        deleteStatementAdInfo.close();
      }catch(Exception e){
        System.out.println("Problem with the deleteStatementAdInfo prepared statement " + e);
      }
      try{
        deleteStatementCustInfo.close();
      }catch(Exception e){
        System.out.println("Problem with the deleteStatementCustInfo prepared statement " + e);
      }
      try{
        if(!isConnectionClosed())
            dbConnection.close();
      }catch(Exception e){
          System.out.println("Problem with the delete statements dbConnection.close() statement " + e);
      }
    }
	}else if(choice.equals("CheckAds")){
		/**
     * Check credit start date field for valid entry which appears as
     * "Check Credit Card Number and Ad Starting On mm/dd/yy" from the 
     * ManageClassified.jsp page.
     */

		if ((creditDate.length() != 8) || (creditDate.charAt(2) != '/') 
		  || (creditDate.charAt(5) != '/') || (!Character.isDigit(creditDate.charAt(0)))
		  || (!Character.isDigit(creditDate.charAt(1))) || (!Character.isDigit(creditDate.charAt(3)))
		  || (!Character.isDigit(creditDate.charAt(4))) || (!Character.isDigit(creditDate.charAt(6)))
		  || (!Character.isDigit(creditDate.charAt(7))) ){
			try{			

				// build Classified Ad error page
				StringBuffer htmlPage = new StringBuffer();
				htmlPage.append("<html><head><title> Manage Classified Ad Error Page</title></head>");
				htmlPage.append("<body background=../classified/servletimages/newsbg.jpg bgcolor=ff0000>");
				htmlPage.append("<center><h1>Manage Field Error Page</h1></center><hr>");
				htmlPage.append("<b>Sorry...</b>The information for the <b>Start Date</b> field does not ");
				htmlPage.append("appear to be valid <b>Press the BACK button on your browser if</b>");
				htmlPage.append("<b> you wish to correct the information you provided.</b>");
				htmlPage.append(" Make sure you use a<b> The date format mm/dd/yy</b>");
				htmlPage.append("<hr>");
				htmlPage.append("<center><a href=../classified/index.jsp>Return to Home Page</a>");
				htmlPage.append("<p><i>" + this.getServletInfo() + "</i>");
				htmlPage.append("</center></body></html>");	
			
	   		// now let's send this dynamic data
	   		// back to the browser
		
	   		response.setContentType("text/html");
	   		outputToBrowser.println(htmlPage);
	   		outputToBrowser.close();
				return;
			}catch (Exception e){
	        ///cleanUp();
	        e.printStackTrace();
	   	}
		 }
    else try{		

      /**
       * Check all ads that fall on a specified date from ManageClassified.jsp.
       * Check Credit Card Number and Ad Starting On mm/dd/yy
       */

      dbConnection = null;
      dbConnection = pool.getConnection();
        
      bookmarkStatement =
				dbConnection.prepareStatement(Bookmark_Statement);
          /**
            * synchronize your shared objects. See example p.282, Jason Hunters
            * book.
            */        
          synchronized(bookmarkStatement){
     
            // clear parameters
            bookmarkStatement.clearParameters();
				
            // set the string
            bookmarkStatement.setString(CHECK_START_DATE_POSITION_1, creditDate);

            // execute query
            listAllBookmarks(request, response);
          }     
    }catch (Exception e){
			System.out.println("A problem occured while finding your bookmarked ads. "
				+ "Please try again: " + e);
   			e.printStackTrace();
    }
    finally{
      try{
        // Close prepared statement after execution.
        bookmarkStatement.close();
      }catch(Exception e){
        System.out.println("Problem with the bookmarkStatement prepared statement " + e);
      }
      try{ // close the database connection if not closed.
        if(!isConnectionClosed())
            dbConnection.close();
      }catch(Exception e){
          System.out.println("Problem with the check all dbConnection.close() statement " + e);
      }
    }

	}else if(choice.equals("DeleteBadAd")){
    /**
     * Delete "Bad Ads using the UniqueDateID
     */
		if((badAd.length() != 12) || (badAd.equals(null))){
			try{			
				// build Classified Ad error page
				StringBuffer htmlPage = new StringBuffer();
				htmlPage.append("<html><head><title> Manage Classified Ad Error Page</title></head>");
				htmlPage.append("<body background=../classified/servletimages/newsbg.jpg bgcolor=ff0000>");
				htmlPage.append("<center><h1>Manage Field Error Page</h1></center><hr>");
				htmlPage.append("<b>Sorry...</b>The information for the <b>UniqueDateId</b> field does not ");
				htmlPage.append("appear to be valid <b>Press the BACK button on your browser if</b>");
				htmlPage.append("<b> you wish to correct the information you provided.</b>");
				htmlPage.append(" Make sure you use a<b> 12 digit number</b>");
				htmlPage.append("<hr>");
				htmlPage.append("<center><a href=../classified/index.jsp>Return to Home Page</a>");
				htmlPage.append("<p><i>" + this.getServletInfo() + "</i>");
				htmlPage.append("</center></body></html>");	
			
        // now let's send this dynamic data
	   		// back to the browser
		
	   		response.setContentType("text/html");
	   		outputToBrowser.println(htmlPage);
	   		outputToBrowser.close();
				return;
			}catch (Exception e){
        //cleanUp();
	      e.printStackTrace();
	   	}
		}

		else try {
      /**
       * SQL to delete bad ads from AdInfo, AdInfoView & CustInfo tables 
       * before it can go into production. Specified 12 digit UniqueDateId
       * from ManageClassified.jsp page
       */
       
      dbConnection = null;
      dbConnection = pool.getConnection();

      // Turn on transactions
			dbConnection.setAutoCommit(false);

      // SQL to delete bad ads from AdInfo, AdInfoView & CustInfo tables 
      // before it can go into production.
      deleteBadAdInfo =
        dbConnection.prepareStatement(Delete_Bad_AdInfo);
      deleteBadAdInfoView =
        dbConnection.prepareStatement(Delete_Bad_AdInfoView);
      deleteBadCustInfo =
        dbConnection.prepareStatement(Delete_Bad_CustInfo);
    
			// clear parameters
			deleteBadAdInfo.clearParameters();
			deleteBadAdInfoView.clearParameters();
			deleteBadCustInfo.clearParameters();
			
			// set statements
			deleteBadAdInfo.setString(DELETE_BAD_ADINFO_1 = 1, badAd);
			deleteBadAdInfoView.setString(DELETE_BAD_ADINFO_VIEW_1 = 1, badAd);
    	deleteBadCustInfo.setString(DELETE_BAD_CUSTINFO_1 = 1, badAd);

			// Turn on transactions
			///dbConnection.setAutoCommit(false); // code moved to above.

      // execute the statement
      deleteBadAd(request, response);

      // Commit statement if no errors.
			dbConnection.commit();

		}
		catch (Exception e)
        	{
			System.out.println("A problem occured while deleting your bad classifiedAd. "
				+ " Please try again.");
   		e.printStackTrace();
        try{			
          // Any error is grounds for rollback.
          dbConnection.rollback();
        }catch(SQLException sqe){ 
            System.out.println("Need to rollback delete BAD ads transaction " + sqe);
        }
      outputToBrowser.println("Delete failed. Please contact technical support.");
		}
    finally{
      try{
          deleteBadAdInfo.close();          
      }catch(Exception e){
        System.out.println("Problem closing deleteBadAdInfo statement " + e);
      }
      try{
          deleteBadAdInfoView.close();
      }catch(Exception e){
        System.out.println("Problem closing deleteBadAdInfoView statement " + e);
      }
      try{
          deleteBadCustInfo.close();
      }catch(Exception e){
        System.out.println("Problem closing deleteBadCustInfo statement " + e);
      }
      try{ // close the database connection if not closed.      
        if(!isConnectionClosed())
            dbConnection.close();
      }catch(Exception e){
          System.out.println("Problem with the deleteBadAd dbConnection.close() statement " + e);
      }
    }
	}

	  /**
     * End the session and send user to login screen.
     */
    else if(choice.equals("EndSession")){
			try{
          session.invalidate();
          response.sendRedirect(request.getScheme() + "://" +
					request.getServerName() + ":" + request.getServerPort() +	"/classified/login.jsp");
          return;
			}catch(IllegalStateException e){
				System.out.println("Exception " + e);
			}
    }		
  }

	public void displayErrorPage(HttpServletRequest request, HttpServletResponse response){
		try{			
			// build Classified Ad error page
			StringBuffer htmlPage = new StringBuffer();
			htmlPage.append("<html><head><title> Bookmark Classified Ad Error Page</title></head>");
			htmlPage.append("<body background=../classified/servletimages/newsbg.jpg bgcolor=ff0000>");
			htmlPage.append("<center><h1>Bookmark Field Error Page</h1></center><hr>");
			htmlPage.append("<b>Sorry...</b>The information for does not ");
			htmlPage.append("appear to be valid <b>Press the BACK button on your browser if</b>");
			htmlPage.append("<b> you wish to correct the information you provided.</b>");
			htmlPage.append(" Make sure you use a<b> The date format mm/dd/yy</b>");
			htmlPage.append("<hr>");
			htmlPage.append("<center><a href=../classified/index.jsp>Return to Home Page</a>");
			htmlPage.append("<p><i>" + this.getServletInfo() + "</i>");
			htmlPage.append("</center></body></html>");	
			
   		// now let's send this dynamic data
   		// back to the browser
		
			PrintWriter outputToBrowser =  new PrintWriter(response.getOutputStream());
   		response.setContentType("text/html");
   		outputToBrowser.println(htmlPage);
   		outputToBrowser.close();
		}catch (Exception e){
      e.printStackTrace();
   	}
	}


  public void insertBookMark(HttpServletRequest request, HttpServletResponse response){      
		try {

			// Insert the users email address and ad the user wanted bookmarked
      // into the SAVED_ADS table.
				
			// execute the insert statement which returns a count of records moved.
			int countInsert = insertStatementBookMark.executeUpdate();

			if (countInsert >= 1){      
	      // Data moved from AdInfo table to AdInfoView table sucessfully.
				StringBuffer htmlPage = new StringBuffer();
				htmlPage.append("<html><head><title>Bookmark Classified Ad</title></head>").append(CR);
				///htmlPage.append("<body background=../classified/servletimages/eagle.jpg bgcolor=#C9E8F8>");
        htmlPage.append("<center><IMG src='servletimages/eagle.gif' width='32' height='32' align='BOTTOM' border='0'></center><br>");

        htmlPage.append("<body bgcolor=#C9E8F8><center><FONT face='Arial, Helvetica, sans-serif' size='2' color='#0000a0'>");
				htmlPage.append("<hr><b> Your Ad is bookmarked! </b><br><br><br>").append(CR);
        htmlPage.append("Click on the Bookmark tab when you<br> want to view your bookmarked ad(s)");
				///htmlPage.append("</table></center>");
        htmlPage.append("</FONT></center>");
				htmlPage.append("<p><hr>");

        /**
         * javascript to close bookmark pop up window
         */
        htmlPage.append("<br><center><b><a href=\"javascript: self.close()\">");
        htmlPage.append("<font face=Verdana,Arial,Helv size=1>Close Window</font></a></b>");
        htmlPage.append("</center>");      
				htmlPage.append("</body></html>");

				// now let's send this dynamic data
	   		// back to the browser
		
				PrintWriter outputToBrowser =  new PrintWriter(response.getOutputStream());
	   		response.setContentType("text/html");
	   		outputToBrowser.println(htmlPage);
	   		outputToBrowser.close();
			}else{
				// ad is already bookmarked or nothing existed.
				StringBuffer htmlPage = new StringBuffer();
				htmlPage.append("<html><head><title>Bookmark Classified Ad</title></head>").append(CR);
				htmlPage.append("<body background=../classified/servletimages/newsbg.jpg bgcolor=ff0000>");
				htmlPage.append("<body><center>").append(CR);
				htmlPage.append("<h4>Thank You For Using Omaha's finest Classifed ads.</h4>").append(CR);
				htmlPage.append("<hr></center><p>").append(CR);
				htmlPage.append("<center><h4><b> No ads where found to bookmark. </b></h4></center>").append(CR);
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
			}
      
		}catch (Exception e){
            System.out.println("Exception moving ads " + e);
            try{
              ///PrintWriter out =  new PrintWriter(response.getOutputStream());
              ///out.println("<center><h1>Exception moving ads </h1></center)<br>" + e);
              ///out.close();
              /**
               * When user tried to bookmard a duplicate ad, redirect them
               * to the bk_error.htm page.
               */
              response.sendRedirect("/classified/bk_error.htm");
            }catch(IOException exp){
              System.out.println("Have a problem print to html " + exp);
            }
    }
  }


  public void deleteClassifieds(HttpServletRequest request, HttpServletResponse response){      
		try {
      /**    /// SHOULD DELETE BOOKMARK BASED ON EMAIL ADDRESS?
       * Delete a new bookmarked ad based on the UniqueDateId field	
       * execute the delete statement which returns a count of records deleted.
       */
			int countAdView = deleteStatementAdInfoView.executeUpdate();
			int countAd = deleteStatementAdInfo.executeUpdate();
			int countCust = deleteStatementCustInfo.executeUpdate();

      // check if UniqueDateId was found in any three tables.	
			if ((countCust >= 1)||(countAd >= 1)||(countAdView >= 1)){
				StringBuffer htmlPage = new StringBuffer();
				htmlPage.append("<html><head><title>Delete Classified Ad</title></head>").append(CR);
				htmlPage.append("<body background=../classified/servletimages/newsbg.jpg bgcolor=ff0000>");
				htmlPage.append("<body><center>").append(CR);
				htmlPage.append("<h1>Thank You For Using blue<i>ads</i>.com</h1>").append(CR);
				htmlPage.append("<hr></center><p>").append(CR);
				htmlPage.append("<center><h3><b> THE ADS HAVE BEEN DELETED FROM THE FOLLOWING TABLES, THANK YOU! </b></h3></center>").append(CR);
				htmlPage.append("<center><h3><b>").append(countAdView).append(" From the AdInfoView Table").append("</b></h3></center>").append(CR);
				htmlPage.append("<center><h3><b>").append(countAd).append(" From the AdInfo Table").append("</b></h3></center>").append(CR);
				htmlPage.append("<center><h3><b>").append(countCust).append(" From the CustInfo Table").append("</b></h3></center>").append(CR);
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
			}
			else{
				// ad is already deleted or has never existed.

				StringBuffer htmlPage = new StringBuffer();
				htmlPage.append("<html><head><title>Check Classified Ad</title></head>").append(CR);
				htmlPage.append("<body background=../classified/servletimages/newsbg.jpg bgcolor=ff0000>");
				htmlPage.append("<body><center>").append(CR);
				htmlPage.append("<h1>Thank You For Using blue<i>ads</i>.com</h1>").append(CR);
				htmlPage.append("<hr></center><p>").append(CR);
				htmlPage.append("<center><h3><b> NO ADS WHERE FOUND TO DELETE, PRESS BACK TO TRY AGAIN! </b></h3></center>").append(CR);
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
			}      					  			
		}catch (Exception e){
      System.out.println("problem with managing Delete Classified " + e);
      //cleanUp();
      e.printStackTrace();
    }
  }

 
// ADDED THIS
//  public synchronized void listAllBookmarks(HttpServletRequest request, HttpServletResponse response){
  public void listAllBookmarks(HttpServletRequest request, HttpServletResponse response){
    try{
      // Search a new classified ad based on the form data
      Classified aClassified = new Classified(request);
             
      // execute the query to get a list of the ads
      ResultSet dataResultSet = bookmarkStatement.executeQuery();

      // Initialize htmlPage variable.
      String htmlPage = "";
	
    	int rowNumber = 0;
	    while (dataResultSet.next()){
        aClassified = new Classified(dataResultSet, 0);
        htmlPage += aClassified.toListString();       	             	        
        rowNumber++;
      }

/// May cause error	    dataResultSet.close();
   
      // Build a html header for the output.
      StringBuffer htmlHead = new StringBuffer();
			htmlHead.append("<html><head>");
      htmlHead.append("<title>List Classified Ads</title></head>");   
      htmlHead.append("<body background=../classified/servletimages/newsbg.jpg bgcolor=FFFFFF>");
      htmlHead.append("<center><h3><b>").append(rowNumber).append(" CLASSIFIED ADS FOUND </b></h3></center>").append(CR);
      htmlHead.append("</table></center>");
      htmlHead.append("<p><hr>");
      htmlHead.append("</body></html>");

      // Append htmlHead and send this 
      // dynamic data back to the browser.
      PrintWriter outputToBrowser =  new PrintWriter(response.getOutputStream());
      response.setContentType("text/html");
      htmlHead.append(htmlPage);
      outputToBrowser.println(htmlHead);
      outputToBrowser.close();        					  			
    }catch (Exception e){
      System.out.println("Check / List Ads Starting on Error " + e);
      e.printStackTrace();
    }
  }

  public void deleteBadAd(HttpServletRequest request, HttpServletResponse response){       
		try{
			// Delete a new classified ad based on the UniqueDateId field
		
			// execute the delete statement which returns a count of records deleted.
			int countBadAd = deleteBadAdInfo.executeUpdate();
			int countBadAdView = deleteBadAdInfoView.executeUpdate();
			int countBadCust = deleteBadCustInfo.executeUpdate();
      
			if ((countBadCust >= 1)||(countBadAd >= 1)||(countBadAdView >= 1)){
	      // UniqueDateId was found in one or both tables.

				StringBuffer htmlPage = new StringBuffer();
				htmlPage.append("<html><head><title>Delete Bad Classified Ad</title></head>").append(CR);
				htmlPage.append("<body background=../classified/servletimages/newsbg.jpg bgcolor=ff0000>");
				htmlPage.append("<body><center>").append(CR);
				htmlPage.append("<h1>Thank You For Using blue<i>ads</i>.com</h1>").append(CR);
				htmlPage.append("<hr></center><p>").append(CR);
				htmlPage.append("<center><h3><b> THE BAD AD HAS BEEN DELETED FROM THE FOLLOWING TABLES, THANK YOU! </b></h3></center>").append(CR);
				htmlPage.append("<center><h3><b>").append(countBadAd).append(" From the AdInfo Table").append("</b></h3></center>").append(CR);
				htmlPage.append("<center><h3><b>").append(countBadAdView).append(" From the AdInfoView Table").append("</b></h3></center>").append(CR);
				htmlPage.append("<center><h3><b>").append(countBadCust).append(" From the CustInfo Table").append("</b></h3></center>").append(CR);
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
			}else{
      
				// ad is already deleted or has never existed.
				StringBuffer htmlPage = new StringBuffer();
				htmlPage.append("<html><head><title>Check Bad Classified Ad</title></head>").append(CR);
				htmlPage.append("<body background=../classified/servletimages/newsbg.jpg bgcolor=ff0000>");
				htmlPage.append("<body><center>").append(CR);
				htmlPage.append("<h1>Thank You For Using blue<i>ads</i>.com</h1>").append(CR);
				htmlPage.append("<hr></center><p>").append(CR);
				htmlPage.append("<center><h3><b> NO AD WAS FOUND TO DELETE, PRESS BACK TO TRY AGAIN! </b></h3></center>").append(CR);
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
			}      					  			
		}catch (Exception e){
        //cleanUp();
        e.printStackTrace();
    }
  }

  public String getServletInfo(){
    return "<i>Classified Ad Servlet, v.02</i>";
  }

	// Test to see if you lost a database connection.
	public boolean isConnectionClosed(){
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
