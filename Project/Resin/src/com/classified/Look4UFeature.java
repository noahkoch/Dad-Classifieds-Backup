/**
 * File:  Look4UFeature.java
 * The Look4UFeature class has data members to describe
 * a Look4U database table.  String methods are available to
 * display the data members to the console or web page.
 *
 * @version Tom Kochanowicz, blue-j@worldnet.att.net
 * @version 0.1, 2 Feb 2000
 * @version 0.3 March 03
 *
 */

package com.classified;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.text.*;
import javax.servlet.*;
import java.lang.*;
import java.util.Vector;
import javax.servlet.http.*;

public class Look4UFeature extends HttpServlet{
  // data members
  protected String name;
  protected String email;
  protected String startDate;
  protected String phrase;
  protected String endDate;
  protected String duration;
  Calendar calendar;
  protected final String CR = "\n";     // carriage return

  // constructors
  public Look4UFeature(){
  }

  public Look4UFeature(HttpServletRequest request){
    email = request.getParameter("Email");
    phrase = request.getParameter("Phrase");
    duration = request.getParameter("Duration");
  }

//public Look4UFeature(ResultSet dataResultSet){
//	// assign data members
//  try {
//		email = dataResultSet.getString("Email");
//  }catch (SQLException e){
//    e.printStackTrace();
//  }
//}

  //  accessors
  public String getEmail(){
    return email;
  }

  public String getPhrase(){
    return phrase;
  }

  public String getStartDate(){
    return startDate;
  }

	public String getEndDate(){
    return endDate;
  }

  //  returns data as HTML formatted un-ordered list for Look4UDBServlet for Start Look4U
  public String toStartLookWebString(){
    StringBuffer buf = new StringBuffer();
    buf.append("<ul>");
    buf.append("<li><B>E-mail:</B> ").append(email).append(CR);
    buf.append("<li><B>Phrase:</B> ").append(phrase).append(CR);
    buf.append("<li><B>Duration of Look4U:</B> ").append(duration).append(" days").append(CR);
    buf.append("</ul>").append(CR);
    String replyString = buf.toString();
    return replyString;
  }

  //  returns data as HTML formatted un-ordered list for Look4UDBServlet for Stop Look4U
  public String toStoptLookWebString(){
    StringBuffer buf = new StringBuffer();
    buf.append("<ul>");
    buf.append("<li><B>E-mail:</B> ").append(email).append(CR);
    //	buf.append("<li><B>Phrase:</B> ").append(phrase).append(CR);
    //	buf.append("<li><B>Duration of Look4U:</B> ").append(duration).append(" days").append(CR);
    buf.append("</ul>").append(CR);
    String replyString = buf.toString();
    return replyString;
  }

////////////////////////////////////////////////////////////////////////
// 		PUT VALIDATION FUNCTIONS BELOW THIS LINE
////////////////////////////////////////////////////////////////////////

	// Check the email field to make sure something was written into it
	// with at least the @ characters, the . character and three characters. 
	
	public boolean checkEmail(){
    // Make sure email it is not null
		if(email == null)
      return false;
      
		int length=email.length();
		if((length > 3 && length < 51) && (email.indexOf('@') > -1) && (email.indexOf('.') > -1))
			return true;
		else
			return false;	
	}

  // Check the phrase field to make sure something was written into it
  // with at least three characters and no more than thirty 

  public boolean checkPhrase(String look){
    if(phrase == null)
      return false;

    int length=phrase.length();
		// Don't check phrase field if Stopping Look4U
    if((length > 1 && length < 31) || (look.equals("StopLook4U")))
			return true;
    else
			return false;
  }

	//
	// calculateEndDate() determines the ending date of search. 
	//

	// private void calculateEndDate(java.util.Date todaysDate, int daysfromtodaysdate){ //was here
	public String calculateEndDate(java.util.Date todaysDate, int daysfromtodaysdate){
		SimpleDateFormat formatterEndDate;
		calendar = Calendar.getInstance();
		formatterEndDate = new SimpleDateFormat("MM/dd/yy");
		formatterEndDate.setTimeZone(java.util.TimeZone.getDefault());  // bug fix so not PST & is CST
		calendar.add(Calendar.DAY_OF_YEAR, + Integer.valueOf(duration).intValue()); 
		todaysDate = calendar.getTime();					
		return endDate = formatterEndDate.format(todaysDate);				
	}
}

