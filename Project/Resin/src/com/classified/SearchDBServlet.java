/**
 * File:  SearchDBServlet.java
 * @author Tom Kochanowicz
 * @version 0.1, 13 Jun 1998
 * @version .02 TJK 02/05/03
 * This servlet works with the index.jsp page to display either a search-by-word
 * or a hyper link choice which then queries the AdInfoView database table.
 */

package com.classified;

import java.awt.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.sql.*;
import javax.sql.*;
import javax.naming.*;
import java.io.*;

import com.classified.common.Classified;

public class SearchDBServlet extends HttpServlet{
	// data members
  private Connection dbConnection;
  private DataSource pool;
  
  private String CR = "\n";
	private int isAletter = 0;

  /**
   * For displayStatementWordAdInfoView statement.
   */
	protected PreparedStatement displayStatementWordAdInfoView;
	static final String Display_Statement_Word_AdInfoView =
    "select * from AdInfoView where(WhatAdSays01 like"
		+ " ?) and UniqueDateId < ? order by UniqueDateId desc";	
  
	// For displayStatementWordAdInfoView statement & AdInfoView table.
	protected final int CLASSIFIED_SEARCH_WORD_POSITION_1 = 1;
	protected final int CLASSIFIED_WORD_AD_DATE_ID_POSITION = 2; // NOT IN TABLE

  /**
   * For displayStatementAdInfoView statement.  
   */
  protected PreparedStatement displayStatementAdInfoView; 
	static final String Display_Statement_AdInfoView =
    "select * from AdInfoView where ClassifiedAd = ? " 
     + "and UniqueDateId < ? order by UniqueDateId desc";
	protected final int CLASSIFIED_AD_POSITION = 1;
	protected final int CLASSIFIED_AD_DATE_ID_POSITION = 2; // NOT IN TABLE

  public void init(ServletConfig config) throws ServletException{
    super.init(config);

      try{
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

	  HttpSession session = request.getSession(true);

		// Get URI
		String uri = request.getRequestURI();

    /**
     * Get the last 12-digit UniqueDateId so you can
     * save this value when you page through the query.
     * DateId is the value from html page.
     */

		String DateId = "999999999999";
		//String values[] = request.getParameterValues("DateId");
		String values = request.getParameter("DateId");

		if(values != null){
			//DateId = values[0];
			DateId = values;
		}

		PrintWriter outputToBrowser =  new PrintWriter(response.getOutputStream());
			
		// We should always get a session back & check for database connection.
		if(session == null){
			outputToBrowser.println("ERROR: Search Database session is null.");
			outputToBrowser.flush();
			outputToBrowser.close();
			return;
		}
    /**
     * Get ClassifiedAd data from HTML form and put in Select statement for display.
     * Preparing display statement
     */ 

			isAletter = 0; // reset isAletter
			String classifiedAd = request.getParameter("ClassifiedAd");
			String searchWord = request.getParameter("SearchWord");
			
			int length = searchWord.length();
			char arrayOfLetters[] = searchWord.toCharArray();

      /**
       * Check for non-letters in search string of searchWord but accept white spaces.
       */
			for (int i = 0; i < length; i++){
					if (Character.isLetter(arrayOfLetters[i]) ||
						Character.isWhitespace(arrayOfLetters[i])){
						isAletter++;
					}
			}

//// TEST
String user = (String)session.getAttribute("loginUser.isDone"); // TEST 
System.out.println("The user who logged into this session is none other then " +
  user);

////// END TEST

		if (((classifiedAd.equals("SearchByWord")) && (length <= 1)) 
                              || (length != isAletter) || (searchWord == null)){			
			displayErrorPage(request, response);
    }else if(classifiedAd.equals("SearchByWord")){
			/**
       * Check if the radio button is clicked on html page to search by word.
       * Searches all of WhatAdSays01.
       */
      try{
          dbConnection = null;
          dbConnection = pool.getConnection();
          
					// set AdInfoView sql parameters for displayStatement "Word" AdInfoView
          displayStatementWordAdInfoView =
            dbConnection.prepareStatement(Display_Statement_Word_AdInfoView);
            /**
             * synchronize your shared objects. See example p.282, Jason Hunters
             * book.
             */
            synchronized(displayStatementWordAdInfoView){            
              // clear all parameters         
              displayStatementWordAdInfoView.clearParameters();
          
              // set the strings
              displayStatementWordAdInfoView.setString(CLASSIFIED_SEARCH_WORD_POSITION_1, "%" + searchWord + "%");
              displayStatementWordAdInfoView.setString(CLASSIFIED_WORD_AD_DATE_ID_POSITION, DateId);

              // Keep the number of rows in the ResultSet
              int rowCount = 1;
              // execute query
              rowCount = displayClassifieds(request, response, uri, classifiedAd, searchWord);
            }          
        }catch (Exception e){
          System.out.println("A problem occured while retrieving your classifiedAd." + e);
	   			e.printStackTrace();
        }finally{
          try{
            // Close prepared statement after execution.
            displayStatementWordAdInfoView.close();
          }catch(Exception e){
            System.out.println("Problem with the checkAllStatement prepared statement " + e);
          }
          try{ // close the database connection if not closed.
            if(!isConnectionClosed())
            dbConnection.close();
          }catch(Exception e){
              System.out.println("Problem with the check all dbConnection.close() statement " + e);
          }
        }
          
		}else{
      try{
          dbConnection = null;
          dbConnection = pool.getConnection();
      
          // set AdInfoView sql parameters for displayStatement "when clicking http link               
          displayStatementAdInfoView =
            dbConnection.prepareStatement(Display_Statement_AdInfoView);

            /**
             * synchronize your shared objects. See example p.282, Jason Hunters
             * book.
             */
            synchronized(displayStatementAdInfoView){
          
              // set AdInfoView sql parameters
              displayStatementAdInfoView.clearParameters();

              // set the strings
              displayStatementAdInfoView.setString(CLASSIFIED_AD_POSITION, classifiedAd);
              displayStatementAdInfoView.setString(CLASSIFIED_AD_DATE_ID_POSITION, DateId);

              // Keep the number of rows in the ResultSet
              int rowCount = 1;
              // execute query
              rowCount = displayClassifieds(request, response, uri, classifiedAd, searchWord);
            }          
      }catch (Exception e){
				System.out.println("A problem occured while retrieving your classifiedAd." + e);
	   			e.printStackTrace();
			}finally{
          try{
            // Close prepared statement after execution.
            displayStatementAdInfoView.close();
          }catch(Exception e){
            System.out.println("Problem with the checkAllStatement prepared statement " + e);
          }try{ // close the database connection if not closed.
            if(!isConnectionClosed())
            dbConnection.close();
          }catch(Exception e){
              System.out.println("Problem with SearchDBServlet " + 
                "displayAdInfoView dbConnection.close() statement " + e);
          }
      }
		}
  } 

 	public void displayErrorPage(HttpServletRequest request, HttpServletResponse response)
    {
		try{

			// build Classified Ad error page

			StringBuffer htmlPage = new StringBuffer();
			htmlPage.append("<html><head><title> Search Classified Ad Error Page</title></head>");
			htmlPage.append("<body background=../classified/servletimages/newsbg.jpg bgcolor=ff0000>");
			htmlPage.append("<center><h1>Search Field Error Page</h1></center><hr>");
			htmlPage.append("<b>Sorry...</b>The information for the <b>Search</b> field does not ");
			htmlPage.append("appear to be valid <b>Press the BACK button on your browser if</b>");
			htmlPage.append("<b> you wish to correct the information you provided.</b>");
			htmlPage.append(" Make sure you use a<b> word(s).</b>");
			htmlPage.append("<hr>");
			htmlPage.append("<center><a href=../classified/index.jsp>Return to Home/Search Page</a>");
			htmlPage.append("<p><i>" + this.getServletInfo() + "</i>");
			htmlPage.append("</center></body></html>");

   		// now let's send this dynamic data
   		// back to the browser
	
   		PrintWriter outputToBrowser =  new PrintWriter(response.getOutputStream());
   		response.setContentType("text/html");
   		outputToBrowser.println(htmlPage);
   		outputToBrowser.close();
		}catch(Exception e){
        System.out.println("Problem In SearchDBServlet " + e);
        e.printStackTrace();
		}
	}

  public int displayClassifieds(HttpServletRequest request, HttpServletResponse response,
                                      String uri, String classifiedAd, String searchWord){
		// Keep stats for how long it takes to execute the query.
		boolean rc = true;
		long startMS = System.currentTimeMillis();

		//
		// Choose a banner to put in depending on the section of classified ad.
		//
		String adBanner = "";
		if (!(classifiedAd.equals("SearchByWord"))){

      //// MERCHANDISE ////
      if(classifiedAd.equals("Appliances"))
        adBanner = "<P ALIGN=\"CENTER\"><A href=../classified/Advertise.html target=\"blueads\"><IMG SRC=\"../classified/servletimages/banners/blueads.gif\" WIDTH=\"468\" HEIGHT=\"60\" ALIGN=\"BOTTOM\" BORDER=\"0\"></A>";
      else if(classifiedAd.equals("Auctions, Estate Sales"))
        adBanner = "<P ALIGN=\"CENTER\"><A href=../classified/Advertise.html target=\"blueads\"><IMG SRC=\"../classified/servletimages/banners/blueads.gif\" WIDTH=\"468\" HEIGHT=\"60\" ALIGN=\"BOTTOM\" BORDER=\"0\"></A>";
      else if(classifiedAd.equals("Business Equip."))
        adBanner = "<P ALIGN=\"CENTER\"><A href=../classified/Advertise.html target=\"blueads\"><IMG SRC=\"../classified/servletimages/banners/blueads.gif\" WIDTH=\"468\" HEIGHT=\"60\" ALIGN=\"BOTTOM\" BORDER=\"0\"></A>";
      else if(classifiedAd.equals("Collectibles"))
        adBanner = "<P ALIGN=\"CENTER\"><A href=../classified/Advertise.html target=\"blueads\"><IMG SRC=\"../classified/servletimages/banners/blueads.gif\" WIDTH=\"468\" HEIGHT=\"60\" ALIGN=\"BOTTOM\" BORDER=\"0\"></A>";
      else if(classifiedAd.equals("Computers/Software"))
        adBanner = "<P ALIGN=\"CENTER\"><A href=../classified/Advertise.html target=\"blueads\"><IMG SRC=\"../classified/servletimages/banners/blueads.gif\" WIDTH=\"468\" HEIGHT=\"60\" ALIGN=\"BOTTOM\" BORDER=\"0\"></A>";
      else if(classifiedAd.equals("Electronics"))
        adBanner = "<P ALIGN=\"CENTER\"><A href=../classified/Advertise.html target=\"blueads\"><IMG SRC=\"../classified/servletimages/banners/blueads.gif\" WIDTH=\"468\" HEIGHT=\"60\" ALIGN=\"BOTTOM\" BORDER=\"0\"></A>";
      else if(classifiedAd.equals("Garage Sales."))
        adBanner = "<P ALIGN=\"CENTER\"><A href=../classified/Advertise.html target=\"blueads\"><IMG SRC=\"../classified/servletimages/banners/blueads.gif\" WIDTH=\"468\" HEIGHT=\"60\" ALIGN=\"BOTTOM\" BORDER=\"0\"></A>";
      else if(classifiedAd.equals("Home Furnishings"))
        adBanner = "<P ALIGN=\"CENTER\"><A href=../classified/Advertise.html target=\"blueads\"><IMG SRC=\"../classified/servletimages/banners/blueads.gif\" WIDTH=\"468\" HEIGHT=\"60\" ALIGN=\"BOTTOM\" BORDER=\"0\"></A>";
      else if(classifiedAd.equals("Jewelry/Crafts"))
        adBanner = "<P ALIGN=\"CENTER\"><A href=../classified/Advertise.html target=\"blueads\"><IMG SRC=\"../classified/servletimages/banners/blueads.gif\" WIDTH=\"468\" HEIGHT=\"60\" ALIGN=\"BOTTOM\" BORDER=\"0\"></A>";
      else if(classifiedAd.equals("Misc."))
        adBanner = "<P ALIGN=\"CENTER\"><A href=../classified/Advertise.html target=\"blueads\"><IMG SRC=\"../classified/servletimages/banners/blueads.gif\" WIDTH=\"468\" HEIGHT=\"60\" ALIGN=\"BOTTOM\" BORDER=\"0\"></A>";
      else if(classifiedAd.equals("Musical"))
        adBanner = "<P ALIGN=\"CENTER\"><A href=../classified/Advertise.html target=\"blueads\"><IMG SRC=\"../classified/servletimages/banners/blueads.gif\" WIDTH=\"468\" HEIGHT=\"60\" ALIGN=\"BOTTOM\" BORDER=\"0\"></A>";
      else if(classifiedAd.equals("Pets"))
        adBanner = "<P ALIGN=\"CENTER\"><A href=../classified/Advertise.html target=\"blueads\"><IMG SRC=\"../classified/servletimages/banners/blueads.gif\" WIDTH=\"468\" HEIGHT=\"60\" ALIGN=\"BOTTOM\" BORDER=\"0\"></A>";
      else if(classifiedAd.equals("Tools/Mech. Equip."))
        adBanner = "<P ALIGN=\"CENTER\"><A href=../classified/Advertise.html target=\"blueads\"><IMG SRC=\"../classified/servletimages/banners/blueads.gif\" WIDTH=\"468\" HEIGHT=\"60\" ALIGN=\"BOTTOM\" BORDER=\"0\"></A>";

      //// SERVICES ////
      else if(classifiedAd.equals("Building/Service/Repairs"))
        adBanner = "<P ALIGN=\"CENTER\"><A href=../classified/Advertise.html target=\"blueads\"><IMG SRC=\"../classified/servletimages/banners/blueads.gif\" WIDTH=\"468\" HEIGHT=\"60\" ALIGN=\"BOTTOM\" BORDER=\"0\"></A>";
      else if(classifiedAd.equals("Catering"))
        adBanner = "<P ALIGN=\"CENTER\"><A href=../classified/Advertise.html target=\"blueads\"><IMG SRC=\"../classified/servletimages/banners/blueads.gif\" WIDTH=\"468\" HEIGHT=\"60\" ALIGN=\"BOTTOM\" BORDER=\"0\"></A>";
      else if(classifiedAd.equals("Child Care"))
        adBanner = "<P ALIGN=\"CENTER\"><A href=../classified/Advertise.html target=\"blueads\"><IMG SRC=\"../classified/servletimages/banners/blueads.gif\" WIDTH=\"468\" HEIGHT=\"60\" ALIGN=\"BOTTOM\" BORDER=\"0\"></A>";
      else if(classifiedAd.equals("Educational"))
        adBanner = "<P ALIGN=\"CENTER\"><A href=../classified/Advertise.html target=\"blueads\"><IMG SRC=\"../classified/servletimages/banners/blueads.gif\" WIDTH=\"468\" HEIGHT=\"60\" ALIGN=\"BOTTOM\" BORDER=\"0\"></A>";
      else if(classifiedAd.equals("Finance"))
        adBanner = "<P ALIGN=\"CENTER\"><A href=../classified/Advertise.html target=\"blueads\"><IMG SRC=\"../classified/servletimages/banners/blueads.gif\" WIDTH=\"468\" HEIGHT=\"60\" ALIGN=\"BOTTOM\" BORDER=\"0\"></A>";
      else if(classifiedAd.equals("Health"))
        adBanner = "<P ALIGN=\"CENTER\"><A href=../classified/Advertise.html target=\"blueads\"><IMG SRC=\"../classified/servletimages/banners/blueads.gif\" WIDTH=\"468\" HEIGHT=\"60\" ALIGN=\"BOTTOM\" BORDER=\"0\"></A>";
      else if(classifiedAd.equals("Heating & Air, Plumbing"))
        adBanner = "<P ALIGN=\"CENTER\"><A href=../classified/Advertise.html target=\"blueads\"><IMG SRC=\"../classified/servletimages/banners/leo.gif\" WIDTH=\"468\" HEIGHT=\"60\" ALIGN=\"BOTTOM\" BORDER=\"0\"></A>";
      else if(classifiedAd.equals("Insurance"))
        adBanner = "<P ALIGN=\"CENTER\"><A href=../classified/Advertise.html target=\"blueads\"><IMG SRC=\"../classified/servletimages/banners/blueads.gif\" WIDTH=\"468\" HEIGHT=\"60\" ALIGN=\"BOTTOM\" BORDER=\"0\"></A>";
      else if(classifiedAd.equals("Legal"))
        adBanner = "<P ALIGN=\"CENTER\"><A href=../classified/Advertise.html target=\"blueads\"><IMG SRC=\"../classified/servletimages/banners/blueads.gif\" WIDTH=\"468\" HEIGHT=\"60\" ALIGN=\"BOTTOM\" BORDER=\"0\"></A>";
      else if(classifiedAd.equals("Painting, Papering etc."))
        adBanner = "<P ALIGN=\"CENTER\"><A href=../classified/Advertise.html target=\"blueads\"><IMG SRC=\"../classified/servletimages/banners/blueads.gif\" WIDTH=\"468\" HEIGHT=\"60\" ALIGN=\"BOTTOM\" BORDER=\"0\"></A>";
      else if(classifiedAd.equals("Personal Services"))
        adBanner = "<P ALIGN=\"CENTER\"><A href=../classified/Advertise.html target=\"blueads\"><IMG SRC=\"../classified/servletimages/banners/blueads.gif\" WIDTH=\"468\" HEIGHT=\"60\" ALIGN=\"BOTTOM\" BORDER=\"0\"></A>";
      else if(classifiedAd.equals("Trucking, Hauling"))
        adBanner = "<P ALIGN=\"CENTER\"><A href=../classified/Advertise.html target=\"blueads\"><IMG SRC=\"../classified/servletimages/banners/blueads.gif\" WIDTH=\"468\" HEIGHT=\"60\" ALIGN=\"BOTTOM\" BORDER=\"0\"></A>";
      else if(classifiedAd.equals("Yard Care"))
        adBanner = "<P ALIGN=\"CENTER\"><A href=../classified/Advertise.html target=\"blueads\"><IMG SRC=\"../classified/servletimages/banners/blueads.gif\" WIDTH=\"468\" HEIGHT=\"60\" ALIGN=\"BOTTOM\" BORDER=\"0\"></A>";

      //// EMPLOYMENT ////
      else if(classifiedAd.equals("Admin. Professional"))
        adBanner = "<P ALIGN=\"CENTER\"><A href=../classified/Advertise.html target=\"blueads\"><IMG SRC=\"../classified/servletimages/banners/blueads.gif\" WIDTH=\"468\" HEIGHT=\"60\" ALIGN=\"BOTTOM\" BORDER=\"0\"></A>";
      else if(classifiedAd.equals("Agricultural"))
        adBanner = "<P ALIGN=\"CENTER\"><A href=../classified/Advertise.html target=\"blueads\"><IMG SRC=\"../classified/servletimages/banners/blueads.gif\" WIDTH=\"468\" HEIGHT=\"60\" ALIGN=\"BOTTOM\" BORDER=\"0\"></A>";
      else if(classifiedAd.equals("Clerical & Office"))
        adBanner = "<P ALIGN=\"CENTER\"><A href=../classified/Advertise.html target=\"blueads\"><IMG SRC=\"../classified/servletimages/banners/blueads.gif\" WIDTH=\"468\" HEIGHT=\"60\" ALIGN=\"BOTTOM\" BORDER=\"0\"></A>";
      else if(classifiedAd.equals("Computer Professional"))
        adBanner = "<P ALIGN=\"CENTER\"><A href=../classified/Advertise.html target=\"blueads\"><IMG SRC=\"../classified/servletimages/banners/blueads.gif\" WIDTH=\"468\" HEIGHT=\"60\" ALIGN=\"BOTTOM\" BORDER=\"0\"></A>";
      else if(classifiedAd.equals("General Help"))
        adBanner = "<P ALIGN=\"CENTER\"><A href=../classified/Advertise.html target=\"blueads\"><IMG SRC=\"../classified/servletimages/banners/blueads.gif\" WIDTH=\"468\" HEIGHT=\"60\" ALIGN=\"BOTTOM\" BORDER=\"0\"></A>";
      else if(classifiedAd.equals("Health Care"))
        adBanner = "<P ALIGN=\"CENTER\"><A href=../classified/Advertise.html target=\"blueads\"><IMG SRC=\"../classified/servletimages/banners/blueads.gif\" WIDTH=\"468\" HEIGHT=\"60\" ALIGN=\"BOTTOM\" BORDER=\"0\"></A>";
      else if(classifiedAd.equals("Instruction"))
        adBanner = "<P ALIGN=\"CENTER\"><A href=../classified/Advertise.html target=\"blueads\"><IMG SRC=\"../classified/servletimages/banners/blueads.gif\" WIDTH=\"468\" HEIGHT=\"60\" ALIGN=\"BOTTOM\" BORDER=\"0\"></A>";
      else if(classifiedAd.equals("Part Time"))
        adBanner = "<P ALIGN=\"CENTER\"><A href=../classified/Advertise.html target=\"blueads\"><IMG SRC=\"../classified/servletimages/banners/blueads.gif\" WIDTH=\"468\" HEIGHT=\"60\" ALIGN=\"BOTTOM\" BORDER=\"0\"></A>";
      else if(classifiedAd.equals("Production"))
        adBanner = "<P ALIGN=\"CENTER\"><A href=../classified/Advertise.html target=\"blueads\"><IMG SRC=\"../classified/servletimages/banners/blueads.gif\" WIDTH=\"468\" HEIGHT=\"60\" ALIGN=\"BOTTOM\" BORDER=\"0\"></A>";
      else if(classifiedAd.equals("Restaurants & Clubs"))
        adBanner = "<P ALIGN=\"CENTER\"><A href=../classified/Advertise.html target=\"blueads\"><IMG SRC=\"../classified/servletimages/banners/blueads.gif\" WIDTH=\"468\" HEIGHT=\"60\" ALIGN=\"BOTTOM\" BORDER=\"0\"></A>";
      else if(classifiedAd.equals("Retail"))
        adBanner = "<P ALIGN=\"CENTER\"><A href=../classified/Advertise.html target=\"blueads\"><IMG SRC=\"../classified/servletimages/banners/blueads.gif\" WIDTH=\"468\" HEIGHT=\"60\" ALIGN=\"BOTTOM\" BORDER=\"0\"></A>";
      else if(classifiedAd.equals("Sales"))
        adBanner = "<P ALIGN=\"CENTER\"><A href=../classified/Advertise.html target=\"blueads\"><IMG SRC=\"../classified/servletimages/banners/blueads.gif\" WIDTH=\"468\" HEIGHT=\"60\" ALIGN=\"BOTTOM\" BORDER=\"0\"></A>";
      else if(classifiedAd.equals("Tech-Trades"))
        adBanner = "<P ALIGN=\"CENTER\"><A href=../classified/Advertise.html target=\"blueads\"><IMG SRC=\"../classified/servletimages/banners/blueads.gif\" WIDTH=\"468\" HEIGHT=\"60\" ALIGN=\"BOTTOM\" BORDER=\"0\"></A>";
      else if(classifiedAd.equals("Truck Driver"))
        adBanner = "<P ALIGN=\"CENTER\"><A href=../classified/Advertise.html target=\"blueads\"><IMG SRC=\"../classified/servletimages/banners/blueads.gif\" WIDTH=\"468\" HEIGHT=\"60\" ALIGN=\"BOTTOM\" BORDER=\"0\"></A>";

      //// VEHICLES ////
      else if(classifiedAd.equals("Airplanes"))
        adBanner = "<P ALIGN=\"CENTER\"><A href=../classified/Advertise.html target=\"blueads\"><IMG SRC=\"../classified/servletimages/banners/blueads.gif\" WIDTH=\"468\" HEIGHT=\"60\" ALIGN=\"BOTTOM\" BORDER=\"0\"></A>";
      else if(classifiedAd.equals("Auto Repair/Parts"))
        adBanner = "<P ALIGN=\"CENTER\"><A href=../classified/Advertise.html target=\"blueads\"><IMG SRC=\"../classified/servletimages/banners/blueads.gif\" WIDTH=\"468\" HEIGHT=\"60\" ALIGN=\"BOTTOM\" BORDER=\"0\"></A>";
      else if(classifiedAd.equals("Boats"))
        adBanner = "<P ALIGN=\"CENTER\"><A href=../classified/Advertise.html target=\"blueads\"><IMG SRC=\"../classified/servletimages/banners/blueads.gif\" WIDTH=\"468\" HEIGHT=\"60\" ALIGN=\"BOTTOM\" BORDER=\"0\"></A>";
      else if(classifiedAd.equals("Cars"))
        adBanner = "<P ALIGN=\"CENTER\"><A href=../classified/Advertise.html target=\"blueads\"><IMG SRC=\"../classified/servletimages/banners/blueads.gif\" WIDTH=\"468\" HEIGHT=\"60\" ALIGN=\"BOTTOM\" BORDER=\"0\"></A>";
      else if(classifiedAd.equals("Cars, Classic"))
        adBanner = "<P ALIGN=\"CENTER\"><A href=../classified/Advertise.html target=\"blueads\"><IMG SRC=\"../classified/servletimages/banners/blueads.gif\" WIDTH=\"468\" HEIGHT=\"60\" ALIGN=\"BOTTOM\" BORDER=\"0\"></A>";
      else if(classifiedAd.equals("Industrial. Const. Equip."))
        adBanner = "<P ALIGN=\"CENTER\"><A href=../classified/Advertise.html target=\"blueads\"><IMG SRC=\"../classified/servletimages/banners/blueads.gif\" WIDTH=\"468\" HEIGHT=\"60\" ALIGN=\"BOTTOM\" BORDER=\"0\"></A>";
      else if(classifiedAd.equals("Motorcycles"))
        adBanner = "<P ALIGN=\"CENTER\"><A href=../classified/Advertise.html target=\"blueads\"><IMG SRC=\"../classified/servletimages/banners/blueads.gif\" WIDTH=\"468\" HEIGHT=\"60\" ALIGN=\"BOTTOM\" BORDER=\"0\"></A>";
      else if(classifiedAd.equals("RV's"))
        adBanner = "<P ALIGN=\"CENTER\"><A href=../classified/Advertise.html target=\"blueads\"><IMG SRC=\"../classified/servletimages/banners/blueads.gif\" WIDTH=\"468\" HEIGHT=\"60\" ALIGN=\"BOTTOM\" BORDER=\"0\"></A>";
      else if(classifiedAd.equals("Trucks & Vans"))
        adBanner = "<P ALIGN=\"CENTER\"><A href=../classified/Advertise.html target=\"blueads\"><IMG SRC=\"../classified/servletimages/banners/blueads.gif\" WIDTH=\"468\" HEIGHT=\"60\" ALIGN=\"BOTTOM\" BORDER=\"0\"></A>";
      else if(classifiedAd.equals("Truck Repair/Parts"))
        adBanner = "<P ALIGN=\"CENTER\"><A href=../classified/Advertise.html target=\"blueads\"><IMG SRC=\"../classified/servletimages/banners/blueads.gif\" WIDTH=\"468\" HEIGHT=\"60\" ALIGN=\"BOTTOM\" BORDER=\"0\"></A>";

      //// REAL ESTATE FOR SALE ////
      else if(classifiedAd.equals("Bellevue/Sarpy County"))
        adBanner = "<P ALIGN=\"CENTER\"><A href=../classified/Advertise.html target=\"blueads\"><IMG SRC=\"../classified/servletimages/banners/blueads.gif\" WIDTH=\"468\" HEIGHT=\"60\" ALIGN=\"BOTTOM\" BORDER=\"0\"></A>";
      else if(classifiedAd.equals("Carter Lake, C.B. Iowa"))
        adBanner = "<P ALIGN=\"CENTER\"><A href=../classified/Advertise.html target=\"blueads\"><IMG SRC=\"../classified/servletimages/banners/blueads.gif\" WIDTH=\"468\" HEIGHT=\"60\" ALIGN=\"BOTTOM\" BORDER=\"0\"></A>";
      else if(classifiedAd.equals("Elkhorn"))
        adBanner = "<P ALIGN=\"CENTER\"><A href=../classified/Advertise.html target=\"blueads\"><IMG SRC=\"../classified/servletimages/banners/blueads.gif\" WIDTH=\"468\" HEIGHT=\"60\" ALIGN=\"BOTTOM\" BORDER=\"0\"></A>";
      else if(classifiedAd.equals("Gretna"))
        adBanner = "<P ALIGN=\"CENTER\"><A href=../classified/Advertise.html target=\"blueads\"><IMG SRC=\"../classified/servletimages/banners/blueads.gif\" WIDTH=\"468\" HEIGHT=\"60\" ALIGN=\"BOTTOM\" BORDER=\"0\"></A>";
      else if(classifiedAd.equals("LaVista"))
        adBanner = "<P ALIGN=\"CENTER\"><A href=../classified/Advertise.html target=\"blueads\"><IMG SRC=\"../classified/servletimages/banners/blueads.gif\" WIDTH=\"468\" HEIGHT=\"60\" ALIGN=\"BOTTOM\" BORDER=\"0\"></A>";
      else if(classifiedAd.equals("Millard"))
        adBanner = "<P ALIGN=\"CENTER\"><A href=../classified/Advertise.html target=\"blueads\"><IMG SRC=\"../classified/servletimages/banners/blueads.gif\" WIDTH=\"468\" HEIGHT=\"60\" ALIGN=\"BOTTOM\" BORDER=\"0\"></A>";
      else if(classifiedAd.equals("Omaha E. of 108th St."))
        adBanner = "<P ALIGN=\"CENTER\"><A href=../classified/Advertise.html target=\"blueads\"><IMG SRC=\"../classified/servletimages/banners/blueads.gif\" WIDTH=\"468\" HEIGHT=\"60\" ALIGN=\"BOTTOM\" BORDER=\"0\"></A>";
      else if(classifiedAd.equals("Omaha W. of 108th St."))
        adBanner = "<P ALIGN=\"CENTER\"><A href=../classified/Advertise.html target=\"blueads\"><IMG SRC=\"../classified/servletimages/banners/blueads.gif\" WIDTH=\"468\" HEIGHT=\"60\" ALIGN=\"BOTTOM\" BORDER=\"0\"></A>";
      else if(classifiedAd.equals("Papillion"))
        adBanner = "<P ALIGN=\"CENTER\"><A href=../classified/Advertise.html target=\"blueads\"><IMG SRC=\"../classified/servletimages/banners/blueads.gif\" WIDTH=\"468\" HEIGHT=\"60\" ALIGN=\"BOTTOM\" BORDER=\"0\"></A>";
      else if(classifiedAd.equals("Ralston"))
        adBanner = "<P ALIGN=\"CENTER\"><A href=../classified/Advertise.html target=\"blueads\"><IMG SRC=\"../classified/servletimages/banners/blueads.gif\" WIDTH=\"468\" HEIGHT=\"60\" ALIGN=\"BOTTOM\" BORDER=\"0\"></A>";

      //// REAL ESTATE SALE/LEASE/RENT ////
      else if(classifiedAd.equals("Apts. For Rent"))
        adBanner = "<P ALIGN=\"CENTER\"><A href=../classified/Advertise.html target=\"blueads\"><IMG SRC=\"../classified/servletimages/banners/blueads.gif\" WIDTH=\"468\" HEIGHT=\"60\" ALIGN=\"BOTTOM\" BORDER=\"0\"></A>";
      else if(classifiedAd.equals("Condo/Townhouse Rent"))
        adBanner = "<P ALIGN=\"CENTER\"><A href=../classified/Advertise.html target=\"blueads\"><IMG SRC=\"../classified/servletimages/banners/blueads.gif\" WIDTH=\"468\" HEIGHT=\"60\" ALIGN=\"BOTTOM\" BORDER=\"0\"></A>";
      else if(classifiedAd.equals("Condo/Townhouse Sale"))
        adBanner = "<P ALIGN=\"CENTER\"><A href=../classified/Advertise.html target=\"blueads\"><IMG SRC=\"../classified/servletimages/banners/blueads.gif\" WIDTH=\"468\" HEIGHT=\"60\" ALIGN=\"BOTTOM\" BORDER=\"0\"></A>";
      else if(classifiedAd.equals("Houses For Rent"))
        adBanner = "<P ALIGN=\"CENTER\"><A href=../classified/Advertise.html target=\"blueads\"><IMG SRC=\"../classified/servletimages/banners/blueads.gif\" WIDTH=\"468\" HEIGHT=\"60\" ALIGN=\"BOTTOM\" BORDER=\"0\"></A>";
      else if(classifiedAd.equals("Ind. Space Sale, Lease"))
        adBanner = "<P ALIGN=\"CENTER\"><A href=../classified/Advertise.html target=\"blueads\"><IMG SRC=\"../classified/servletimages/banners/blueads.gif\" WIDTH=\"468\" HEIGHT=\"60\" ALIGN=\"BOTTOM\" BORDER=\"0\"></A>";
      else if(classifiedAd.equals("Land For Sale, Lease"))
        adBanner = "<P ALIGN=\"CENTER\"><A href=../classified/Advertise.html target=\"blueads\"><IMG SRC=\"../classified/servletimages/banners/blueads.gif\" WIDTH=\"468\" HEIGHT=\"60\" ALIGN=\"BOTTOM\" BORDER=\"0\"></A>";
      else if(classifiedAd.equals("Mobile Home Sale, Lease"))
        adBanner = "<P ALIGN=\"CENTER\"><A href=../classified/Advertise.html target=\"blueads\"><IMG SRC=\"../classified/servletimages/banners/blueads.gif\" WIDTH=\"468\" HEIGHT=\"60\" ALIGN=\"BOTTOM\" BORDER=\"0\"></A>";
      else if(classifiedAd.equals("Multi-Unit Sale"))
        adBanner = "<P ALIGN=\"CENTER\"><A href=../classified/Advertise.html target=\"blueads\"><IMG SRC=\"../classified/servletimages/banners/blueads.gif\" WIDTH=\"468\" HEIGHT=\"60\" ALIGN=\"BOTTOM\" BORDER=\"0\"></A>";
      else if(classifiedAd.equals("Office Space Sale, Lease"))
        adBanner = "<P ALIGN=\"CENTER\"><A href=../classified/Advertise.html target=\"blueads\"><IMG SRC=\"../classified/servletimages/banners/blueads.gif\" WIDTH=\"468\" HEIGHT=\"60\" ALIGN=\"BOTTOM\" BORDER=\"0\"></A>";
      else if(classifiedAd.equals("Vac. Resort Sale, Lease"))
        adBanner = "<P ALIGN=\"CENTER\"><A href=../classified/Advertise.html target=\"blueads\"><IMG SRC=\"../classified/servletimages/banners/blueads.gif\" WIDTH=\"468\" HEIGHT=\"60\" ALIGN=\"BOTTOM\" BORDER=\"0\"></A>";

      //// GENERAL ////
      else if(classifiedAd.equals("Announcements"))
        adBanner = "<P ALIGN=\"CENTER\"><A href=../classified/Advertise.html target=\"blueads\"><IMG SRC=\"../classified/servletimages/banners/blueads.gif\" WIDTH=\"468\" HEIGHT=\"60\" ALIGN=\"BOTTOM\" BORDER=\"0\"></A>";
      else if(classifiedAd.equals("Lost & Found"))
        adBanner = "<P ALIGN=\"CENTER\"><A href=../classified/Advertise.html target=\"blueads\"><IMG SRC=\"../classified/servletimages/banners/blueads.gif\" WIDTH=\"468\" HEIGHT=\"60\" ALIGN=\"BOTTOM\" BORDER=\"0\"></A>";
      else if(classifiedAd.equals("Personal"))
        adBanner = "<P ALIGN=\"CENTER\"><A href=../classified/Advertise.html target=\"blueads\"><IMG SRC=\"../classified/servletimages/banners/blueads.gif\" WIDTH=\"468\" HEIGHT=\"60\" ALIGN=\"BOTTOM\" BORDER=\"0\"></A>";

      //// DATE NET ////
      else if(classifiedAd.equals("Men Seeking Date"))
        adBanner = "<P ALIGN=\"CENTER\"><A href=../classified/Advertise.html target=\"blueads\"><IMG SRC=\"../classified/servletimages/banners/blueads.gif\" WIDTH=\"468\" HEIGHT=\"60\" ALIGN=\"BOTTOM\" BORDER=\"0\"></A>";
      else if(classifiedAd.equals("Women Seeking Date"))
        adBanner = "<P ALIGN=\"CENTER\"><A href=../classified/Advertise.html target=\"blueads\"><IMG SRC=\"../classified/servletimages/banners/blueads.gif\" WIDTH=\"468\" HEIGHT=\"60\" ALIGN=\"BOTTOM\" BORDER=\"0\"></A>";

      //// WHERE TO GO ? ////
      else if(classifiedAd.equals("Dining"))
        adBanner = "<P ALIGN=\"CENTER\"><A href=../classified/Advertise.html target=\"blueads\"><IMG SRC=\"../classified/servletimages/banners/blueads.gif\" WIDTH=\"468\" HEIGHT=\"60\" ALIGN=\"BOTTOM\" BORDER=\"0\"></A>";
      else if(classifiedAd.equals("Entertainment"))
        adBanner = "<P ALIGN=\"CENTER\"><A href=../classified/Advertise.html target=\"blueads\"><IMG SRC=\"../classified/servletimages/banners/blueads.gif\" WIDTH=\"468\" HEIGHT=\"60\" ALIGN=\"BOTTOM\" BORDER=\"0\"></A>";
        //adBanner = "<P ALIGN=\"CENTER\"><A HREF=\"http://www.thereader.com/\" target=\"blueads\"><IMG SRC=\"../servletimages/banners/thereader.gif\" WIDTH=\"468\" HEIGHT=\"60\" ALIGN=\"BOTTOM\" BORDER=\"0\"></A>";
      else if(classifiedAd.equals("Recreation"))
        adBanner = "<P ALIGN=\"CENTER\"><A href=../classified/Advertise.html target=\"blueads\"><IMG SRC=\"../classified/servletimages/banners/blueads.gif\" WIDTH=\"468\" HEIGHT=\"60\" ALIGN=\"BOTTOM\" BORDER=\"0\"></A>";		
		}


		// Reset the row counter.
		int rowCount = 1;

		try {
			// Search a new classified ad based on the form data
			Classified aClassified = new Classified(request);
		 
			int rowsPerPage = 11; // Shows on Submit button as 10

			// Keep track of the last DateId found
			String DateId = "";

      /**
       * This will be true if there is still more data
       * in the table.
       */
			boolean more = false;

    // build the html page heading
		StringBuffer htmlHead = new StringBuffer();
		///htmlHead.append("<html><head><title>List of Classifieds</title></head>").append(CR);
 		htmlHead.append("<html><head>").append(CR);

    /**
     *  javascript function called "NewWindow()" that is called from the toTableString()
     *  method of the Classifed.java class.
     *  JavaScript To Pop Up userEmail servlet.
     */
    htmlHead.append("<SCRIPT LANGUAGE=\"JavaScript\"> <!-- Hide script from old browsers\n ");
    htmlHead.append(" function NewWindow(winWidth,winHeight,url)");
    htmlHead.append(" {");
    htmlHead.append(" window.open(url,\"mailWindow\",\"menubars=0,scrollbars=0,resizable=1,height=\"+winHeight+\",width=\"+winWidth);");
    htmlHead.append(" }");
    htmlHead.append("// End hiding script from old browsers-->\n</SCRIPT>");
    htmlHead.append(CR);
    htmlHead.append("</head>").append(CR);

		// build the html body
		StringBuffer htmlBody = new StringBuffer();
		htmlBody.append("<body bgcolor=000000><font color=\"#E1F2FD\">"); //bg black, font medium-blue
		// Put in the banner and the classified ad section.

    htmlBody.append(adBanner).append("<center><FONT face='Arial, Helvetica, sans-serif' size='3' color='#ffffff'>");
    htmlBody.append("... ").append(classifiedAd).append("</FONT></center>");
    
		//htmlBody.append("<font color=000000></font><br>").append(CR); // black font in boxes
		htmlBody.append("<font color=000000></font><br>"); // black font in boxes

		// build the table heading
		StringBuffer tableHead = new StringBuffer();
		tableHead.append("<center><table border=0 width=90% cellspacing=0 bgcolor=\"#E1F2FD\">");

		// execute the query to get a list of the ads by Search By Word
		ResultSet dataResultSet = null;
		if (classifiedAd.equals("SearchByWord")){
      try{
        dataResultSet = displayStatementWordAdInfoView.executeQuery();
      }catch(Exception e){
          System.out.println("Problem with displayStatementWordAdInfoView " + e);
      }         
		}
		else{
      // execute the query to get a list of the ads by clicking HTTP Link
			dataResultSet = displayStatementAdInfoView.executeQuery();	
		}

		// Process the results. First dump out the column
		// headers as found in the ResultSetMetaData 
	  ResultSetMetaData rsmd = dataResultSet.getMetaData(); 
	  int columnCount = rsmd.getColumnCount();

    // build the table body
		StringBuffer tableBody = new StringBuffer();
		tableBody.append("");

		while (dataResultSet.next()){
    	aClassified = new Classified(dataResultSet);
      // get the acutual DateId from database & pass it toTableString
      String UID = dataResultSet.getString("UniqueDateId");
      //tableBody.append(aClassified.toTableString(rowCount));
      tableBody.append(aClassified.toTableString(rowCount, UID));
   
      rowCount++;

      // Dump out the values of each row
      for (int i = 0; i < columnCount; i++){
      
        // Note that the column index is 1-based.
        String data = dataResultSet.getString(i + 1);

        /**
         * If this is the UniqueDateId column, cache it.
         * Note: first column is counted as 0.
         */
        if (i == 0){
          DateId = data;
        }
      }
      /**
       * If we are keeping track of the maximum number of
       * rows per page and we have exceeded that count
       * break out of the loop
       */
			if((rowsPerPage > 0) && (rowCount >= rowsPerPage)){
				// Find out if their are any more rows after this one.
				more = dataResultSet.next();
				break;
			}
    }
      // THE BELOW MAY CAUSE ERROR IN HEAVY LOADS
			try{         
				if (dataResultSet != null)
        	dataResultSet.close();
			}
			catch (Exception e1) {
				System.out.println("Closing Resultset: " + e1 + "");
				rc = false;
			}

      // build the table bottom
			StringBuffer tableBottom = new StringBuffer();
			tableBottom.append("</table></center>");

      // build complete html page
			htmlBody.append(tableHead).append(tableBody).append(tableBottom);
      
      // put a link back to the home page.
      htmlBody.append("<br><center><a href=../classified/index.jsp><font color=\"#ffffff\">");
      htmlBody.append("<font SIZE='2' FACE='Arial, Helvetica, sans-serif' color='#ffffff'>Return to Home/Search Page</font></a></center>"); 

      /**
       * Create a "More Ads" button & put in hidden values. The DateId is
       * used to reference the UniqueDateId for the next set of rows.
       */
			if(more){
				// JavaScript To prevent user hitting Submit button too many times.
				htmlBody.append("<SCRIPT LANGUAGE=\"JavaScript\"> <!-- Hide script from old browsers\n ");
				htmlBody.append(" var submitted = false;");
				htmlBody.append(" function doSubmit(form)");
				htmlBody.append(" {");
				htmlBody.append(" if (!submitted) {");
				htmlBody.append(" submitted = true;");
				htmlBody.append(" form.submit();");
				htmlBody.append(" }");
				htmlBody.append(" }");
				htmlBody.append("// End hiding script from old browsers-->\n</SCRIPT>");

				htmlBody.append("<form method=GET action=\"" + uri + "\">");
				// Note line below, you need value=\"" + classifiedAd + "\" for a string with spaces.
				htmlBody.append("<input type=hidden name=ClassifiedAd value=\"").append(classifiedAd).append("\">");
				htmlBody.append("<input type=hidden name=SearchWord value=\"").append(searchWord).append("\">");
				htmlBody.append("<input type=hidden name=DateId value=").append(DateId).append(">");
				//htmlBody.append("<div align=\"right\"><INPUT TYPE=\"button\" VALUE=\"Back\" ONCLICK=\"history.go(-1)\">");
				htmlBody.append("<div align=\"right\"><INPUT TYPE=\"button\" VALUE=\"Back\" ONCLICK=\"history.go(-1)\">");         
				htmlBody.append("<INPUT TYPE=\"button\" VALUE=\"Next ").append((rowsPerPage - 1)).append(" Ads\" ONCLICK=\"doSubmit(this.form)\"></div>");

				// If we queried the table successfully, output some statistics.
				if (rc){
					long elapsed = System.currentTimeMillis() - startMS;
          htmlBody.append("<font color=silver face=Verdana,Arial,Helv size=1><br>" + 
          (rowCount - 1) + " ad(s) in " + elapsed + " ms</font>");
				}
			}
			// Put in Back button on last page. Done this way so it works with IE Explorer & Netscape.
			else{
				// If we queried the table successfully, output some statistics.
				if (rc){
					long elapsed = System.currentTimeMillis() - startMS;
          htmlBody.append("<font color=silver face=Verdana,Arial,Helv size=1><br>" + 
          (rowCount - 1) + " ad(s) in " + elapsed + " ms</font>");               
				}				
				htmlBody.append("<div align=\"right\"><P><TABLE BORDER=\"1\" WIDTH=\"7%\"><TR><TD WIDTH=\"100%\" BGCOLOR=\"silver\">");
        //htmlBody.append("<P ALIGN=\"CENTER\"><A HREF=# ONCLICK=\"history.go(-1)\"><FONT COLOR=\"black\">Back</FONT></A>");
				htmlBody.append("<P ALIGN=\"CENTER\"><A HREF=\"javascript:history.go(-1)\"><FONT COLOR=\"black\">Back</FONT></A>");
				htmlBody.append("</TD></TR></TABLE></DIV>");
			}

		  // build html page bottom
			StringBuffer htmlBottom = new StringBuffer();								
			htmlBottom.append("</body></html>");

			// Put it all together
			StringBuffer htmlPage = new StringBuffer();
			htmlPage.append(htmlHead).append(htmlBody).append(htmlBottom);			

	    // now let's send this dynamic data back to the browser     
      PrintWriter outputToBrowser =  new PrintWriter(response.getOutputStream());

      // MAY CAUSE ERRORS IN HEAVY LOADS
			try{
				if (dataResultSet != null)
       		dataResultSet.close();
			}
			catch (SQLException e1){
        System.out.println("Problem closing Result set " + e1);
				outputToBrowser.println("&lti>Error code: " + e1 + "");
        rc = false;
			}

	    response.setContentType("text/html");
      outputToBrowser.println(htmlPage);
      outputToBrowser.close();  					  			
		}catch (Exception e) {
      System.out.println("We have an error in SearchDBServlet " + e);
	    e.printStackTrace();
		}
		return rowCount; 
  }

  public String getServletInfo(){
    return "<center><i><font color=63c6de>Classified Ads, v.02</font></i></center>";
  }

	// Test to see if you lost a database connection 
	public boolean isConnectionClosed(){
    try{
      if (dbConnection != null){
        dbConnection.close();
        return dbConnection.isClosed();
		}else
			return true;
		}
		catch (SQLException e){
			return true;
		}
	}
}
