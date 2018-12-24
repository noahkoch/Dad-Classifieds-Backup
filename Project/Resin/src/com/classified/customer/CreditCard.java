package com.classified.customer;

import java.util.Vector;
import java.util.HashMap;
import java.util.Iterator;
///import org.apache.turbine.services.db.TurbineDB;
///import org.apache.turbine.util.db.pool.DBConnection;
///import org.apache.turbine.util.TurbineConfig;
import com.classified.exceptions.CustomerActivityException;
import java.sql.*;
import javax.sql.*;
import java.util.ResourceBundle;
///import org.apache.log4j.Category;
import javax.naming.NamingException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingEnumeration;
import javax.naming.directory.InitialDirContext;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Date;

/**
 * Holds the credit card information used in the wallet and in recorded 
 * orders.<P>
 *
 * Copyright 2002, James M. Turner
 * No warranty is made as to the fitness of this code for any
 * particular purpose.  It is included for demonstration purposes
 * only.
 *
 * @author       James M. Turner
 */

public class CreditCard implements Cloneable{
    ///static Category cat = Category.getInstance(CreditCard.class);

  private static ResourceBundle sql_bundle =
    ResourceBundle.getBundle("com.classified.customer.SQLQueries");

  protected int cardID;
  protected Address address;
  protected Customer customer;
  protected String cardOwner;
  protected String cardType;
  protected String cardNumber;
  protected int expMonth;
  protected int expYear;

  /**
   * Returns the unique Card ID, which is an autoincrementing
   * database column used to uniquely identify each credit card record.
   *
   * @return the card ID
   **/

  public int getCardID(){
    return cardID;
  }

  /**
   * Sets the unique card ID, which is an autoincrementing
   * database column used to uniquely identify each card record.
   * Note that in normal usage, this should not be used because IDs
   * are assigned by the database.
   *
   * @param id the card ID
   **/

  public void setCardID(int id){
    cardID = id;
  }

  /**
   * Returns the billing address of this card.
   *
   * @return The address
   **/

  public Address getAddress(){
    return address;
  }

  /**
   * Sets the billing address of this card.
   *
   * @param addr The address
   **/

  public void setAddress(Address addr){
    address = addr;
  }

  /**
   * Returns the customer associated with this card.
   *
   * @return The customer
   **/

  public Customer getCustomer(){
    return customer;
  }

  /**
   * Sets the customer associated with this card.
   *
   * @param cust The customer
   **/

  public void setCustomer(Customer cust){
    customer = cust;
  }

  /**
   * Returns the card owner name of this card.
   *
   * @return The card owner as a string
   **/

  public String getCardOwner(){
    return cardOwner;
  }

  /**
   * Sets the card owner name of this card.
   *
   * @param name The card owner as a string
   **/

  public void setCardOwner(String name) {
    cardOwner = name;
  }

  /**
   * Returns the credit card type of this card.<BR>
   * Valid values are:
   * <LI>VISA
   * <LI>MC
   * <LI>DISC
   * <LI>AMEX
   *
   * @return The card type as a string
   **/

  public String getCardType(){
    return cardType;
  }

  /**
   * Sets the credit card type of this card.<BR>
   * Valid values are:
   * <LI>VISA
   * <LI>MC
   * <LI>DISC
   * <LI>AMEX
   *
   * @param name The card type as a string
   **/

  public void setCardType(String name) {
    cardType = name;
  }

  /**
   * Returns the credit card number of this card.
   *
   * @return The card number as a string
   **/

  public String getCardNumber(){
    return cardNumber;
  }

  /**
   * Returns the credit card number of this card with all but the
   * last four digits obscured with stars.
   *
   * @return The obscured card number as a string
   **/

  public String getObscuredNumber(){
    if((cardNumber != null) && (cardNumber.length() > 6)){
      String digitsOnly = getDigitsOnly(cardNumber);
      String allStars = "****************************************";
      return digitsOnly.substring(0,2) +  
      allStars.substring(0, digitsOnly.length() - 6) +
      digitsOnly.substring(digitsOnly.length() - 4, digitsOnly.length());
    }else{
      return "";
    }
  }

  /**
   * Sets the credit card number of this card.
   *
   * @param name The card number
   **/

  public void setCardNumber(String name){
    cardNumber = name;
  }

  /**
   * Returns the credit card expiration month of this card.
   *
   * @return The card expiration month as an int
   **/

  public int getExpMonth(){
    return expMonth;
  }

  /**
   * Sets the credit card expiration month of this card.
   *
   * @param month The card expiration month as an int
   **/

  public void setExpMonth(int month){
    expMonth = month;
  }

  /**
   * Returns the credit card expiration year of this card.
   *
   * @return The card expiration year as an int (4 digit)
   **/

  public int getExpYear(){
    return expYear;
  }

  /**
   * Sets the credit card expiration year of this card.
   *
   * @param year The card expiration year as an int (4 digit)
   **/

  public void setExpYear(int year){
    expYear = year;
  }
  private HashMap validationErrors = new HashMap();

  /**
   * Returns a validation error against a specific field.  If a field
   * was found to have an error during 
   * {@link #validateCreditCard validateCreditCard},  the error message
   * can be accessed via this method.
   *
   * @param fieldname The bean property name of the field
   * @return The error message for the field or null if none is
   * present.
   **/

  public String getFieldError(String fieldname){
    return((String)validationErrors.get(fieldname));
  }
    
  /**
   * Sets the validation error against a specific field.  Used by
   * {@link #validateCreditCard validateCreditCard}.
   *
   * @param fieldname The bean property name of the field
   * @param error The error message for the field or null if none is
   * present.
   **/

  public void addFieldError(String fieldname, String error){
    validationErrors.put(fieldname, error);
  }

  /**
   * Validates that the expiration date is after today.
   *
   * @param expMonth The expiration month
   * @param expYear The expiration year
   * @return <code>true</code> if the date is valid, otherwise 
   * <code>false</code>
   **/

  public boolean validateCCDate(int expMonth, int expYear){
    SimpleDateFormat formatter = new SimpleDateFormat ("MM/yy");
    try{
      GregorianCalendar c =  new GregorianCalendar();
	    c.setTime(formatter.parse(expMonth + "/" + expYear));
	    c.roll(Calendar.MONTH, 1);
	    c.set(Calendar.DATE, 1);
	    c.set(Calendar.HOUR, 0);
	    c.set(Calendar.MINUTE, 0);
	    c.set(Calendar.SECOND, 0);
	    Date now = new Date();
	    return (now.compareTo(c.getTime()) < 0);
    }catch (ParseException ex){
      System.out.println("CrediCard bean parse exception " + ex);
    };
    return false;
  }

  /**
   * Returns only the numeric characters from a string.
   *
   * @param s The string to be stripped of non-numerics
   * @return A string composed only of numbers.
   **/

  private String getDigitsOnly (String s){
    StringBuffer digitsOnly = new StringBuffer();
    char c;
    for (int i = 0; i < s.length (); i++){
      c = s.charAt (i);
      if (Character.isDigit (c)){
        digitsOnly.append(c);
      }
    }
    return digitsOnly.toString ();
  }

  /**
   * Validates the credit card against the standard prefixes and
   * checksums for credit card numbers
   *
   * @param cardNumber The credit card number
   * @return <code>true</code> if the number is valid, otherwise 
   * <code>false</code>
   **/

  public boolean validateCreditCardNumber (String cardNumber){
    String digitsOnly = getDigitsOnly (cardNumber);
    int sum = 0;
    int digit = 0;
    int addend = 0;
    boolean timesTwo = false;
    int digitLength = digitsOnly.length();
    boolean foundcard = false;

    // MC
    if(digitsOnly.startsWith("51") || digitsOnly.startsWith("52") 
	    || digitsOnly.startsWith("53") || digitsOnly.startsWith("54")){
	    if (digitLength != 16) return false;
	    foundcard = true;
    }
    // VISA
    if (digitsOnly.startsWith("4")){
	    if ((digitLength != 16) && (digitLength != 13)) return false;
	    foundcard = true;
    }
    // AMEX
    if(digitsOnly.startsWith("34") || digitsOnly.startsWith("37")){
	    if (digitLength != 15) return false;
	    foundcard = true;
    }
    // DISC
    if (digitsOnly.startsWith("6011")){
	    if (digitLength != 16) return false;
	    foundcard = true;
    }
    if (!foundcard) return false;
    for (int i = digitsOnly.length () - 1; i >= 0; i--){
	    digit = Integer.parseInt (digitsOnly.substring (i, i + 1));
	    if (timesTwo){
        addend = digit * 2;
        if (addend > 9){
          addend -= 9;
        }
	    }else{
        addend = digit;
	    }
	    sum += addend;
	    timesTwo = !timesTwo;
    }

    int modulus = sum % 10;
    return modulus == 0;
  }

  /**
   * Validates the credit card against a number of checks including
   * missing fields, invalid expiration date and bad checksum on the
   * card number.
   *
   * @return <code>true</code> if the card passes the validation
   * checks,  otherwise  <code>false</code>
   **/

  public boolean validateCreditCard(){
    validationErrors.clear();
    boolean valid = true;
    if ((cardType == null) || (cardType.length() == 0)){
      addFieldError("cardType", "Card Type is required.");
	    valid = false;
    } 
    if ((cardOwner == null) || (cardOwner.length() == 0)){
      addFieldError("cardOwner", "Cardholder Name is required.");
	    valid = false;
    } 
    if ((cardNumber == null) || (cardNumber.length() == 0)){
	    addFieldError("cardNumber", "Card Number is required.");
	    valid = false;
    }else{
      if(!validateCreditCardNumber(cardNumber)){
        addFieldError("cardNumber", "Invalid Card Number");
        valid = false;
	    }
    }
    if(expMonth == 0){
	    addFieldError("expMonth", "Expiration Month is required.");
	    valid = false;
    } 
    if(expYear == 0){
	    addFieldError("expYear", "Expiration Year is required.");
	    valid = false;
    } 
    if(!validateCCDate(expMonth, expYear)){
	    addFieldError("expYear", "Expired Card Date");
	    valid = false;
    } 
    return valid;
  }

  /**
   * Given an card ID, returns a new card object populated
   * from the database, or null if no such card exists.
   *
   * @param ID The unique ID of the card in the database
   * @return A populated CreditCard object, or null if none found.
   * @throws CustomeryActivityException Thrown on database errors
   **/

  public static CreditCard findCreditCard(int cardID) 
                                              throws CustomerActivityException{
    CreditCard cc = null;
	  ///DBConnection dbConn = null;
    ///////////////   Start test ///////////////////////
    javax.sql.DataSource pool;
    Connection dbConnection = null; 

    try{         
        System.out.println("Customer.updateCustomer bean: start");
        Context env = (Context) new InitialContext().lookup("java:comp/env");
        pool = (DataSource) env.lookup("jdbc/Classifieds");
        dbConnection = pool.getConnection();
        
        if (pool == null){
            System.out.println("Can't get database connection");
            throw new CustomerActivityException();
        }
       
    //}catch(NamingException e){
    //    throw new CustomerActivityException(e);
    //}
/////////// END TEST    
	  ///try{
    ///    dbConn = TurbineDB.getConnection();
		///    if (dbConn == null){
    ///      cat.error("Can't get database connection");
    ///      throw new CustomerActivityException();
		///    }
		    PreparedStatement pstmt =
          dbConnection.prepareStatement(sql_bundle.getString("creditQuery"));
		    pstmt.setInt(1, cardID);
		    ResultSet rs = pstmt.executeQuery();
		    if(rs.next()){
          cc = new CreditCard();
          cc.setCardID(rs.getInt("CARD_ID"));
          cc.setCardType(rs.getString("CARD_TYPE"));
          cc.setCardNumber(rs.getString("CARD_NUMBER"));
          cc.setCardOwner(rs.getString("CARD_OWNERNAME"));
          cc.setExpMonth(rs.getInt("CARD_EXPMONTH"));
          cc.setExpYear(rs.getInt("CARD_EXPYEAR"));
          cc.setAddress(Address.findAddress(rs.getInt("ADDRESS_KEY")));
		    }else{
          ///cat.error("Couldn't find record for Credit Card");
          System.out.println("Couldn't find record for Credit Card");
		    }
		    rs.close();
		    pstmt.close();
		} catch (Exception e){
		    ///cat.error("Error during findCreditCard", e);
        System.out.println("Error during findCreditCard " + e);
		    throw new CustomerActivityException();
		}finally{
      try{
			  ///TurbineDB.releaseConnection(dbConn);
        dbConnection.close();
			}catch (Exception e){
			    ///cat.error("Error during release connection", e);
          System.out.println("Error during release connection " + e);
			}
		}
	    System.out.println("Credit card is " + cc + ", card id is " + cc.getCardID());
	    return cc;
	}

	

  /**
   * Creates a new card in the database and sets the card ID
   * of the object to the newly created record's ID
   *
   * @throws CustomerActivityException Thrown if there is an error
   * inserting the record in the database.
   **/

  public void createCreditCard() throws CustomerActivityException{
    ///DBConnection dbConn = null;
	  ///DBConnection dbConn = null;
    ///////////////   Start test ///////////////////////
    javax.sql.DataSource pool;
    Connection dbConnection = null; 

    try{         
        System.out.println("Customer.updateCustomer bean: start");
        Context env = (Context) new InitialContext().lookup("java:comp/env");
        pool = (DataSource) env.lookup("jdbc/Classifieds");
        dbConnection = pool.getConnection();
        
        if (pool == null){
            System.out.println("Can't get database connection");
            throw new CustomerActivityException();
        }
       
    //}catch(NamingException e){
    //    throw new CustomerActivityException(e);
    //}
/////////// END TEST        
    ///try{
		///    dbConn = TurbineDB.getConnection();
		///    if (dbConn == null){
    //      cat.error("Can't get database connection");
    ///      throw new CustomerActivityException();
		///    }
		    PreparedStatement pstmt =
          dbConnection.prepareStatement(sql_bundle.getString("creditInsert"));
		    pstmt.setInt(1, getCustomer().getCustomerId());
		    pstmt.setString(2, getCardType());
		    pstmt.setString(3, getCardNumber());
		    pstmt.setString(4, getCardOwner());
		    pstmt.setInt(5, getExpMonth());
		    pstmt.setInt(6, getExpYear());
		    pstmt.setInt(7, getAddress().getAddressID());
		    pstmt.executeUpdate();
		    pstmt.close();
		    pstmt =	dbConnection.prepareStatement(sql_bundle.getString("addressID"));
		    ResultSet rs = pstmt.executeQuery();
		    if(rs.next()){
          setCardID( rs.getInt(1));
		    }else{
          ///cat.error("Couldn't find record for new Credit Card");
          System.out.println("Couldn't find record for new Credit Card");
		    }
		    rs.close();
		    pstmt.close();
		}catch(Exception e){
      ///cat.error("Error during createCreditCard", e);
      System.out.println("Error during createCreditCard " + e);
		  throw new CustomerActivityException();
		}finally{
      try{
        ///TurbineDB.releaseConnection(dbConn);
        dbConnection.close();
			}catch (Exception e){
        ///cat.error("Error during release connection", e);
        System.out.println("Error during release connection " + e);
			}
		}
	}

  /**
   * Updates an card in the database.
   *
   * @throws CustomerActivityException Thrown if there is an error
   * updating the record in the database.
   **/

  public void updateCreditCard() throws CustomerActivityException{
    ///DBConnection dbConn = null;
    ///////////////   Start test ///////////////////////
    javax.sql.DataSource pool;
    Connection dbConnection = null; 

    try{         
        System.out.println("Customer.updateCustomer bean: start");
        Context env = (Context) new InitialContext().lookup("java:comp/env");
        pool = (DataSource) env.lookup("jdbc/Classifieds");
        dbConnection = pool.getConnection();
        
        if (pool == null){
            System.out.println("Can't get database connection");
            throw new CustomerActivityException();
        }
       
    //}catch(NamingException e){
    //    throw new CustomerActivityException(e);
    //}
/////////// END TEST          
	  ///try{
        ///dbConn = TurbineDB.getConnection();
		    ///if(dbConn == null){
        ///  cat.error("Can't get database connection");
        ///  throw new CustomerActivityException();
		    ///}
		    PreparedStatement pstmt =
          dbConnection.prepareStatement(sql_bundle.getString("creditUpdate"));
		    pstmt.setInt(1, getCustomer().getCustomerId());
		    pstmt.setString(2, getCardType());
		    pstmt.setString(3, getCardNumber());
		    pstmt.setString(4, getCardOwner());
		    pstmt.setInt(5, getExpMonth());
		    pstmt.setInt(6, getExpYear());
		    pstmt.setInt(7, getAddress().getAddressID());
		    pstmt.setInt(8, getCardID());
		    pstmt.executeUpdate();
		    pstmt.close();
		}catch(Exception e){
      ///cat.error("Error during updateCreditCard", e);
      System.out.println("Error during updateCreditCard" + e);
		  throw new CustomerActivityException();
		}finally{
      try{
        ///TurbineDB.releaseConnection(dbConn);
        dbConnection.close();
			}catch (Exception e){
        ///cat.error("Error during release connection", e);
        System.out.println("Error during release connection " + e);
			}
		}
	}

  /**
   * Deletes an card from the database.
   *
   * @throws CustomerActivityException Thrown if there is an error
   * deleting the record in the database.
   **/

  public void deleteCreditCard() throws CustomerActivityException{
    ///DBConnection dbConn = null;
    ///////////////   Start test ///////////////////////
    javax.sql.DataSource pool;
    Connection dbConnection = null; 

    try{         
        System.out.println("Customer.updateCustomer bean: start");
        Context env = (Context) new InitialContext().lookup("java:comp/env");
        pool = (DataSource) env.lookup("jdbc/Classifieds");
        dbConnection = pool.getConnection();
        
        if (pool == null){
            System.out.println("Can't get database connection");
            throw new CustomerActivityException();
        }
       
    //}catch(NamingException e){
    //    throw new CustomerActivityException(e);
    //}
/////////// END TEST              
	  ///try{
		///    dbConn = TurbineDB.getConnection();
		///    if(dbConn == null){
    ///      cat.error("Can't get database connection");
    ///      throw new CustomerActivityException();
		///    }
		    getAddress().deleteAddress();
		    PreparedStatement pstmt =
          dbConnection.prepareStatement(sql_bundle.getString("creditDelete"));
		    pstmt.setInt(1, getCardID());
		    pstmt.executeUpdate();
		    pstmt.close();
		}catch(Exception e){
      ///cat.error("Error during deleteCreditCard", e);
      System.out.println("Error during deleteCreditCard" + e);
		    throw new CustomerActivityException();
		}finally{
      try{
			    ///TurbineDB.releaseConnection(dbConn);
          dbConnection.close();
			}catch (Exception e){
        ///cat.error("Error during release connection", e);
        System.out.println("Error during release connection" + e);
			}
		}
  }

  /**
   * Returns a copy of the CreditCard object
   *
   * @returns A duplicate of the object
   **/

  public Object clone() throws java.lang.CloneNotSupportedException{
    return super.clone();
  }
}

