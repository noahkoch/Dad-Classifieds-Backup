//  File:  Classified.java
//
package bluejay.register;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.text.*;
import javax.servlet.*;
import java.lang.*;
import java.util.Vector;
import javax.servlet.http.*;

/**
 *  The Classified class has data members to describe
 *  a classified ad.  String methods are available to
 *  display the data members to the console or web page.
 *
 *  @version Tom Kochanowicz, blue-j@worldnet.att.net
 *  @version 0.1, 16 Oct 1999 enhancement
 *  @version 0.2, 15 Jan 2003 start again!
 *
 */
public class Classified extends HttpServlet
{
    // data members
    protected String name;
    protected String address;
    protected String city;
    protected String state;
    protected String zip;
    protected String email;
    protected String phone;
    protected String phoneEmailChoice;
    protected String payMethod;
    protected String cardNumber;
    protected String cardMo;
    protected String cardYr;
    protected String startDate;
    //????protected java.sql.Date startDate;
    protected String endDate;
    protected String amtForDayMoYr;
    protected int bill; // bill is multiplied by amtForDayMoYr
    protected String classifiedAd;
    protected String color;
    protected String banner;
    protected String whatAdSays01;
    protected String webSite;
    protected String uniqueDateId;
    Calendar calendar;
    SimpleDateFormat formatter2;
    protected String searchWord;
    protected final String CR = "\n";     // carriage return

    // constructors
    public Classified()
    {
	
    }

    public Classified(HttpServletRequest request)
    {
      name = request.getParameter("Name");
      address = request.getParameter("Address");
      city = request.getParameter("City");
      state = request.getParameter("State");
      zip = request.getParameter("Zip");        
      email = request.getParameter("Email");
      phone = request.getParameter("Phone");
      phoneEmailChoice = request.getParameter("PhoneEmailChoice");
      payMethod = request.getParameter("PayMethod");
      cardNumber = request.getParameter("CardNumber");
      cardMo = request.getParameter("CardMo");
      cardYr = request.getParameter("CardYr");
      startDate = request.getParameter("StartDate");
      //????String dateString = request.getParameter("StartDate");
      //????startDate = java.sql.Date.valueOf(dateString);
      amtForDayMoYr = request.getParameter("AmtForDayMoYr");
      classifiedAd = request.getParameter("ClassifiedAd");
      color = request.getParameter("Color");
      banner = request.getParameter("Banner");
      whatAdSays01 = request.getParameter("WhatAdSays01");
      webSite = request.getParameter("WebSite");
      // Format Unique Date Id
      calendar = Calendar.getInstance();
      formatter2 = new SimpleDateFormat("DDDHHmmssSSS");
      formatter2.setTimeZone(java.util.TimeZone.getDefault());  // bug fix so not PST & is CST
      uniqueDateId = formatter2.format(calendar.getTime());
    }

    public Classified(ResultSet dataResultSet)
    {
		// assign data members
      try {

        // Data from AdInfo Table.
        // Format Unique Date Id
        calendar = Calendar.getInstance();
        formatter2 = new SimpleDateFormat("DDDHHmmssSSS");
        formatter2.setTimeZone(java.util.TimeZone.getDefault());  // bug fix so not PST & is CST
        uniqueDateId = formatter2.format(calendar.getTime());
        classifiedAd = dataResultSet.getString("ClassifiedAd");
        color = dataResultSet.getString("Color");
        banner = dataResultSet.getString("Banner");
        whatAdSays01 = dataResultSet.getString("WhatAdSays01");
        phone = dataResultSet.getString("Phone");
        email = dataResultSet.getString("Email");
        webSite = dataResultSet.getString("WebSite");
      }
      catch (SQLException e){
          e.printStackTrace();
      }
    }

	// Class used with ManageDBServlet.java
    public Classified(ResultSet dataResultSet, int count){
			// assign data members
      try {

        // data from CustInfo table.
        name = dataResultSet.getString("Name");
	      address = dataResultSet.getString("Address");
        city = dataResultSet.getString("City");
        state = dataResultSet.getString("State");
        zip = dataResultSet.getString("Zip");        
	      payMethod = dataResultSet.getString("PayMethod");
        cardNumber = dataResultSet.getString("CardNumber");
        cardMo = dataResultSet.getString("CardMo");
        cardYr = dataResultSet.getString("CardYr");
        startDate = dataResultSet.getString("StartDate");
        endDate = dataResultSet.getString("EndDate");
        //endDate = getEndDate();
        amtForDayMoYr = dataResultSet.getString("AmtForDayMoYr");
			
        // Data from AdInfo Table.
        // Format Unique Date Id
        //calendar = Calendar.getInstance();
        //formatter2 = new SimpleDateFormat("DDDHHmmssSSS");
        //formatter2.setTimeZone(java.util.TimeZone.getDefault());  // bug fix so not PST & is CST
        //uniqueDateId = formatter2.format(calendar.getTime());
        uniqueDateId = dataResultSet.getString("UniqueDateId");
        classifiedAd = dataResultSet.getString("ClassifiedAd");
        color = dataResultSet.getString("Color");
        banner = dataResultSet.getString("Banner");
        whatAdSays01 = dataResultSet.getString("WhatAdSays01");
        phone = dataResultSet.getString("Phone");
        email = dataResultSet.getString("Email");
        webSite = dataResultSet.getString("WebSite");
      }
      catch (SQLException e){
          e.printStackTrace();
      }
    }

    //  accessors
    public String getName(){
        return name;
    }

    public String getAddress(){
        return address;
    }

    public String getCity(){
        return city;
    }

    public String getState(){
        return state;
    }

    public String getZip(){
        return zip;
    }

    public String getEmail(){
        return email;
    }

    public String getPhone(){
        return phone;
    }

    public String getPhoneEmailChoice(){
        return phoneEmailChoice;
    }

    public String getPayMethod(){
        return payMethod;
    }

    public String getCardNumber(){
        return cardNumber;
    }

    public String getStartDate(){
        return startDate;
    }

    public String getEndDate(){
        return endDate;
    }

    public String getAmtForDayMoYr(){
        return amtForDayMoYr;
    }

    public String getClassifiedAd(){
        return classifiedAd;
    }

    public String getColor(){
        return color;
    }

    public String getBanner(){
        return banner;
    }

    public String getWhatAdSays01(){
        return whatAdSays01;
    }

    public String getWebSite(){
        return webSite;
    }

    public String getUniqueDateId(){
        return uniqueDateId;
    }


    //  methods
    //  normal text string representation
    public String toString(){
      StringBuffer buf = new StringBuffer();
      buf.append("");
      //buf.append("Amount To Send In: ").append(amtForDayMoYr).append(CR);
      buf.append("Name: ").append(name).append(CR);
      ///buf.append("Address: ").append(address).append(CR);
      ///buf.append("City: ").append(city).append(CR);
      ///buf.append("State: ").append(state).append(CR);
      ///buf.append("Zip: ").append(zip).append(CR);
      buf.append("E-mail: ").append(email).append(CR);
      buf.append("Phone: ").append(phone).append(CR);
      ///buf.append("Payment Method: ").append(payMethod).append(CR);
      ///buf.append("Credit Card Number: ").append(cardNumber).append("  Expiration Mo: ").append(cardMo).append(" Yr: ").append(cardYr).append(CR);
      buf.append("Ad Start Date: ").append(startDate).append("  Ad Ending Date: ").append(endDate).append(CR);
      buf.append("Classified Ad: ").append(classifiedAd).append(CR);
      ///buf.append("Color: ").append(color).append("COLOR</FONT>").append(CR);
      buf.append(color).append("</FONT>").append(CR);
      buf.append("Banner: ").append(banner).append(CR);
      buf.append("What Ad Says: ").append(whatAdSays01).append(CR);
      buf.append("WebSite: ").append(webSite).append(CR);
      buf.append("Ad I.D. Number").append(uniqueDateId).append(CR).append(CR);
      String replyString = buf.toString();
      
      return replyString;
    }

    //  returns data as HTML formatted un-ordered list for SubmitDBServlet & SubmitNowDBServlet
    public String toWebString(){
      StringBuffer buf = new StringBuffer();
      buf.append("<ul>");
      //buf.append("<li><B>Amount to send in:</B> $").append(bill).append(CR);
      buf.append("<li><B>Name:</B> ").append(name).append(CR);
      ///buf.append("<li><B>Address:</B> ").append(address).append(CR);
      ///buf.append("<li><B>City:</B> ").append(city).append(CR);
      ///buf.append("<li><B>State:</B> ").append(state).append(CR);
      ///buf.append("<li><B>Zip:</B> ").append(zip).append(CR);
      buf.append("<li><B>E-mail:</B> ").append(email).append(CR);
      buf.append("<li><B>Phone:</B> ").append(phone).append(CR);
      ///buf.append("<li><B>Payment Method:</B> ").append(payMethod).append(CR);
      ///buf.append("<li><B>Credit Card Number:</B> ").append(cardNumber).append("  <B>Expiration Mo:</B> ").append(cardMo).append(" <B>Yr:</B> ").append(cardYr).append(CR);
      buf.append("<li><B>Ad Start Date:</B> ").append(startDate).append("  <B>Ad Ending Date:</B> ").append(endDate).append(CR);
      buf.append("<li><B>Classified Ad:</B> ").append(classifiedAd).append(CR);
      ///buf.append("<li><B>Color:</B> ").append(color).append("COLOR</FONT>").append(CR);
      buf.append("<li><B>").append(color).append("</B></FONT>").append(CR);
      buf.append("<li><B>Banner:</B> ").append(banner).append(CR);
      buf.append("<li><B>What Ad Says:</B> ").append(CR);
      buf.append("<li></li>").append(whatAdSays01).append(CR);
      buf.append("<li><B>WebSite:</B> ").append(webSite).append(CR);
      buf.append("<li><B>Ad I.D. Number: </B>").append(uniqueDateId).append(CR);
      buf.append("</ul>").append(CR);
	
      String replyString = buf.toString();
      return replyString;
    }

    //  Used with ManageDBServlet, returns data as HTML formatted un-ordered list.
    public String toListString(){
      StringBuffer buf = new StringBuffer();
      buf.append("<HTML><HEAD><TITLE>Classified List</TITLE></HEAD><BODY><P>");
      buf.append("<FORM ACTION=\"default.cgi\" METHOD=\"POST\" ENCTYPE=\"application/x-www-form-urlencoded\">");
      ///buf.append("<FONT SIZE=2><B>Payment Method:</B> ").append(payMethod);
      ///buf.append("<BR><B>Credit Card Number:</B> ").append(cardNumber).append("  <B>Expiration Mo:</B> ").append(cardMo).append(" <B>Yr:</B> ").append(cardYr);
      //buf.append("<BR><B>Amount to send in:</B> $").append(amtForDayMoYr);
      buf.append("<BR><B>Name:</B> ").append(name);
      ///buf.append("<BR><B>Address:</B> ").append(address);
      ///buf.append("<BR><B>City:</B> ").append(city).append(" <B>State:</B> ").append(state).append(" <B>Zip:</B> ").append(zip);
      buf.append("<BR><B>E-mail:</B> ").append(email).append(" <B>Phone:</B> ").append(phone);
      buf.append("<BR><B>Ad Start Date:</B> ").append(startDate).append("  <B>Ad Ending Date:</B> ").append(endDate);
      buf.append("<BR><B>Classified Ad:</B> ").append(classifiedAd);
      buf.append("<BR><B>").append(color).append("</B>");
      buf.append("<BR><B>Banner:</B> ").append(banner);
      buf.append("<BR><B>What Ad Says:</B> ");
      buf.append("<BR>").append(whatAdSays01);
      buf.append("<BR><B>WebSite:</B> ").append(webSite);
      buf.append("<BR><B>Ad I.D. Number: </B>").append(uniqueDateId);
      buf.append("<BR><B>Ad Reviewed: </B><INPUT TYPE=CHECKBOX NAME=CheckBox VALUE=CheckBox>");
      buf.append("<BR><B>Cash:</B><INPUT TYPE=RADIO NAME=Radio VALUE=Radio>");
      buf.append("<B> Credit: </B><INPUT TYPE=RADIO NAME=Radio VALUE=Radio>"); 
      buf.append("</FONT></FORM>").append("</P>");
      buf.append("<HR ALIGN=CENTER></BODY></HTML>");
		
      String replyString = buf.toString();
        return replyString;
    }

    // returns data formatted for an HTML table row viewed from SearchDBServlet
    public String toTableString(int rowNumber, String UID){
      StringBuffer buf = new StringBuffer();
      // Note change COLSPAN to 5 if adding a botton column
      ///buf.append("").append("<tr><td COLSPAN=4 WIDTH=100% VALIGN=TOP BGCOLOR=white>");
      buf.append("<tr><td COLSPAN=4 WIDTH=100% VALIGN=TOP BGCOLOR=white>");

      // The parameter color is equal to COLOR=black  or red, blue etc.
      ///buf.append("").append("<FONT ").append(color).append(" FONT FACE=\"Times New Roman\">");
      buf.append("<FONT ").append(color).append(" FONT FACE=\"Times New Roman\">");

      // banner is a wingding e.g. <FONT SIZE="5" FACE="Wingdings">J </FONT>
      buf.append(banner);

      buf.append(whatAdSays01);
      buf.append("</FONT></TD></TR>");
      buf.append("<TR>");

      buf.append("<TD NOWRAP BGCOLOR=C9E8F8><FONT SIZE=2 FACE=\"Times New Roman\"><B>").append(rowNumber);
      buf.append(")</B></FONT>").append(" ");
      buf.append("<FONT COLOR=\"black\" FACE=\"Wingdings\">G</FONT></TD>"); // HAND POINTING UP.

      buf.append("<TD NOWRAP BGCOLOR=C9E8F8>").append("<A HREF=\"/userEmail?UID="); // MAILBOX
      buf.append(UID).append("\"><FONT SIZE=\"2\" COLOR=\"black\" FACE=\"Wingdings\">.</FONT>").append(" ");
      buf.append("<FONT SIZE=\"2\" COLOR=\"black\" FACE=\"Times New Roman\">Reply</FONT></A></TD>");

      buf.append("<TD NOWRAP BGCOLOR=C9E8F8><FONT COLOR=\"#0033CC\" FACE=\"Wingdings\">(</FONT>"); // PHONE
      buf.append("<FONT SIZE=2 FACE=\"Times New Roman\">").append(phone).append("</FONT></TD>");

      buf.append("<TD NOWRAP BGCOLOR=C9E8F8><FONT COLOR=\"#003333\" FACE=\"Wingdings\">$</FONT>"); // Glasses
      buf.append("<FONT SIZE=2 FACE=\"Times New Roman\">").append("<a href=http://").append(webSite);
      buf.append(" target=\"blueads\"> ");
      buf.append(webSite).append("</a>").append("</FONT></TD>");

      // CELL FOR FUTURE USE...MAKE SURE YOU INCREASE COLSPAN BY 1.
      //buf.append("<TD NOWRAP BGCOLOR=E1F2FD><FONT SIZE=2 FACE=\"Times New Roman\"><B>FUTURE</B></FONT></TD>");
      //buf.append("</TR>");
		
      String replyString = buf.toString();
        return replyString;
    }



////////////////////////////////////////////////////////////////////////
// 		PUT VALIDATION FUNCTIONS BELOW THIS LINE
////////////////////////////////////////////////////////////////////////

    // Check the name field to make sure something was written into it
    // with at least three characters and no more than thirty 

    public boolean checkName (){
    if(name == null) return false;
      int length=name.length();
      
    	if(length > 3 && length < 30)
        return true;
    	else
        return false;
    }


    // Check the email field to make sure something was written into it
    // with at least the @ characters, the . character and three characters. 
	
    public boolean checkEmail(){
		// If showing both email & phone or just email, make sure email is not null
      if(email == null)
        return false;

      int length=email.length();

      // Check format of email to see if no spaces & contains @ and .
      if((length > 3 && length < 51) && (email.indexOf('@') > -1) && (email.indexOf('.') > -1) && 
        (!(email.indexOf(' ') > -1)))
        return true;
      else
        return false;	
    }


    // Check the phone field to make sure something numeric was written into it
    // with at least the 12 characters. Must fit the pattern nnn-nnn-nnnn. 
    // Then checks to see if the phone area code matches the state.

    public boolean checkPhone (){


		// Check the email field to make sure something was written into it
		// with at least the @ characters, the . character and three characters. 
	

		//
		// Don't check Phone (return true) if only showing Email
		//

		if(phoneEmailChoice.equals("Email")){
      // Test to see if phone field is blank.
			if(!(phone.trim().equals("")))
				return false;
			else
				return true;
		}

		// If showing both email & phone or just phone, make sure it is not null
		if(phone == null)
			return false;

		// Make sure the numbers entered are numbers by checking
		// between the spaces and dashes.

		int 	length=phone.length();
	    	char	array [] = phone.toCharArray ();

		// Make sure 12 chars 
		if (phone.length() != 12) {
			return false;
		}
		 	
            	for (int i = length - 1; i >= 0; i--) {
                	if (Character.isWhitespace (array [i])
                        	|| array [i] == '-') {
                    	length -= 1;
                    	for (int j = i; j < length; j++) {
                        	array [j] = array [j + 1];
                    	}
                	} else if (!Character.isDigit (array [i]))
                    	return false;
            	}

		// Make sure state matches NE or IA phone area codes

		if((phone.charAt(3) == '-') && (phone.charAt(7) == '-'))
		{
			String new_string = phone.substring(0,3);
			new_string += phone.substring(4,7);
			new_string += phone.substring(8,phone.length());

			// check NE area codes 308 & 402
			if((phone.charAt(0)=='3') && (phone.charAt(1)=='0') &&
					(phone.charAt(2)=='8') && (new_string.length() == 10))
					return true;
 
			else if	((phone.charAt(0)=='4') && (phone.charAt(1)=='0') && 
					(phone.charAt(2)=='2') && (new_string.length() == 10))
					return true;

			// check IA area codes 319, 515 && 712
			if((phone.charAt(0)=='3') && (phone.charAt(1)=='1') &&
					(phone.charAt(2)=='9') && (new_string.length() == 10))
					return true;
 
			else if	((phone.charAt(0)=='5') && (phone.charAt(1)=='1') && 
					(phone.charAt(2)=='5') && (new_string.length() == 10))
					return true;
			
			else if	((phone.charAt(0)=='7') && (phone.charAt(1)=='1') && 
					(phone.charAt(2)=='2') && (new_string.length() == 10))
					return true;				
		}	
		return false;		
	}


	//																// 0123456789
	// Check the startDate field for numbers entered are in the format MM/dd/yy
	// by checking between the /'s and spaces.						
	// Also calls a method calculateEndDate() to determine the ending date.
	//

	public boolean checkStartDate(HttpServletRequest request, HttpServletResponse response) 
				throws ServletException, IOException {

		// Make sure startDate field is not null.
		if(startDate == null)
			return false;

		int length = startDate.length();
		char array[] = startDate.toCharArray();
		java.util.Date todaysDate;
		Calendar calendar;
		SimpleDateFormat formatter1;
		calendar = Calendar.getInstance();
		formatter1 = new SimpleDateFormat("MM/dd/yy");
		formatter1.setTimeZone(java.util.TimeZone.getDefault());  // bug fix so not PST & is CST
		todaysDate = calendar.getTime();


		//
		// If not between 3 and 7 days inclusively print current
		// time for a few seconds to html, then show warning page.
		//
		if (startDate.length() != 8){ // check for string length to avoid exception 

			calendar = Calendar.getInstance();
			todaysDate = calendar.getTime();

			return false;
		}

		// Check basic length and format of date field for placement of /'s and 
		// length before it checks if dates are valid for format MM/dd/yy.
	

		else if ((startDate.charAt(2) == '/') && (startDate.charAt(5) == '/') && (startDate.length() == 8)) {
			//
			// Test startDate's to make sure it's between THREE and SEVEN days.
			//
			int three = 3;
			calendar.add(Calendar.DAY_OF_YEAR, 3);
			todaysDate = calendar.getTime();

			if(formatter1.format(todaysDate).equals(startDate)){
				calculateEndDate(todaysDate, three);
				return true;
			}

			int four = 4;
			calendar.add(Calendar.DAY_OF_YEAR, 1); // add a day...day 4
			todaysDate = calendar.getTime();

			if(formatter1.format(todaysDate).equals(startDate)){
				calculateEndDate(todaysDate, four);
				return true;
			}

			int five = 5;
			calendar.add(Calendar.DAY_OF_YEAR, 1); // add a day...day 5
			todaysDate = calendar.getTime();

			if(formatter1.format(todaysDate).equals(startDate)){
				calculateEndDate(todaysDate, five);
				return true;
			}

			int six = 6;
			calendar.add(Calendar.DAY_OF_YEAR, 1); // add a day...day 6
			todaysDate = calendar.getTime();

			if(formatter1.format(todaysDate).equals(startDate)){
				calculateEndDate(todaysDate, six);
				return true;
			}

			int seven = 7;
			calendar.add(Calendar.DAY_OF_YEAR, 1); // add a day...day 7
			todaysDate = calendar.getTime();

			if(formatter1.format(todaysDate).equals(startDate)){
				calculateEndDate(todaysDate, seven);
				return true;
			}
		}
		
		return false;

	}

	//
	// calculateEndDate() method to determine the ending date of the ad based on the amount 
	// paid in. Called by checkStartDate() method.
	//

	private void calculateEndDate(java.util.Date todaysDate, int daysfromtodaysdate){

		SimpleDateFormat formatterEndDate;
		calendar = Calendar.getInstance();
		formatterEndDate = new SimpleDateFormat("MM/dd/yy");
		formatterEndDate.setTimeZone(java.util.TimeZone.getDefault());  // bug fix so not PST & is CST
		
		if(amtForDayMoYr.equals("5")){
			calendar.add(Calendar.DAY_OF_YEAR, 10 + daysfromtodaysdate);
			todaysDate = calendar.getTime();					
			endDate = formatterEndDate.format(todaysDate);
		}
		else if(amtForDayMoYr.equals("10")){
			calendar.add(Calendar.DAY_OF_YEAR, 30 + daysfromtodaysdate);
			todaysDate = calendar.getTime();					
			endDate = formatterEndDate.format(todaysDate);
		}
		else if(amtForDayMoYr.equals("20")){
			calendar.add(Calendar.DAY_OF_YEAR, 60 + daysfromtodaysdate);
			todaysDate = calendar.getTime();					
			endDate = formatterEndDate.format(todaysDate);
		}
		else if(amtForDayMoYr.equals("30")){
			calendar.add(Calendar.DAY_OF_YEAR, 90 + daysfromtodaysdate);
			todaysDate = calendar.getTime();					
			endDate = formatterEndDate.format(todaysDate);
		}
		else if(amtForDayMoYr.equals("40")){
			calendar.add(Calendar.DAY_OF_YEAR, 120 + daysfromtodaysdate);
			todaysDate = calendar.getTime();					
			endDate = formatterEndDate.format(todaysDate);
		}
		else if(amtForDayMoYr.equals("50")){
			calendar.add(Calendar.DAY_OF_YEAR, 150 + daysfromtodaysdate);
			todaysDate = calendar.getTime();					
			endDate = formatterEndDate.format(todaysDate);
		}
		else if(amtForDayMoYr.equals("60")){
			calendar.add(Calendar.DAY_OF_YEAR, 180 + daysfromtodaysdate);
			todaysDate = calendar.getTime();					
			endDate = formatterEndDate.format(todaysDate);
		}
		else if(amtForDayMoYr.equals("70")){
			calendar.add(Calendar.DAY_OF_YEAR, 210 + daysfromtodaysdate);
			todaysDate = calendar.getTime();					
			endDate = formatterEndDate.format(todaysDate);
		}
		else if(amtForDayMoYr.equals("80")){
			calendar.add(Calendar.DAY_OF_YEAR, 240 + daysfromtodaysdate);
			todaysDate = calendar.getTime();					
			endDate = formatterEndDate.format(todaysDate);
		}
		else if(amtForDayMoYr.equals("90")){
			calendar.add(Calendar.DAY_OF_YEAR, 270 + daysfromtodaysdate);
			todaysDate = calendar.getTime();					
			endDate = formatterEndDate.format(todaysDate);
		}
		else if(amtForDayMoYr.equals("100")){
			calendar.add(Calendar.DAY_OF_YEAR, 365 + daysfromtodaysdate);
			todaysDate = calendar.getTime();					
			endDate = formatterEndDate.format(todaysDate);
		}
	}

	//
	// Check Number of Days ad runs to make sure it's at least 5 days and are numbers
	// then calculate the bill.
	//
	
	public boolean checkAmtForDayMoYr() {
		int charge = 1; // multiply by $1.00

		int length = amtForDayMoYr.length();
		char array[] = amtForDayMoYr.toCharArray();

		//
		// Make sure 2 digits are where they are suppose to be.
		//
		try{
			if((amtForDayMoYr.length() < 1)) {
				return false;
			}
			else if(charge * Integer.valueOf(amtForDayMoYr).intValue() < 5){
				return false;
			}
			else bill = charge * Integer.valueOf(amtForDayMoYr).intValue();
		} 
		catch (NumberFormatException e) {
			System.out.println(e);
			System.out.println ("/nError in the checkAmtForDayMoYr() function/n");
		}
		return true;
	}
	
	// Check the whatAdSays01 field to make sure something was written into it with
	// at least 10 characters and no more than 1000 characters. Make sure its not one
	// continues line with no spaces too.

	public boolean checkWhatAdSays01() {

		int length = whatAdSays01.length();
		// Check if field is null or contains blanks
		if((length < 9) || (length > 1000) || (whatAdSays01 == null) || (whatAdSays01.trim().equals("")))
			return false;
		else
			return true;
	
	}

}


