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
import javax.naming.Context;
///import org.apache.log4j.Category;
import javax.naming.NamingException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingEnumeration;
import javax.naming.directory.InitialDirContext;

/**
 * Implements the address portion of a customer or credit card record.
 * Both the Customer, Order and CreditCard object use this class to
 * store address-specific information, which reduces the amount of
 * redundant code.<P>
 *
 * Copyright 2002, James M. Turner
 * No warranty is made as to the fitness of this code for any
 * particular purpose.  It is included for demonstration purposes
 * only.
 *
 * @author       James M. Turner
 */

public class Address implements Cloneable {
    ///static Category cat = Category.getInstance(Address.class);

  private static ResourceBundle sql_bundle =
    ResourceBundle.getBundle("com.classified.customer.SQLQueries");

  protected int addressID;
  protected String firstName;
  protected String lastName;
  protected String street1;
  protected String street2;
  protected String city;
  protected String state;
  protected String postalCode;

  /**
   * Returns the unique Address ID, which is an autoincrementing
   * database column used to uniquely identify each address record.
   *
   * @return the address ID
   **/

  public int getAddressID(){
    return addressID;
  }

  /**
   * Sets the unique Address ID, which is an autoincrementing
   * database column used to uniquely identify each address record.
   * Note that in normal usage, this should not be used because IDs
   * are assigned by the database.
   *
   * @param id the address ID
   **/

  public void setAddressID(int id) {
    addressID = id;
  }

  /**
   * Returns the first name of the person at this address
   *
   * @return The first name as a String
   **/

  public String getFirstName(){
    return firstName;
  }

  /**
   * Sets the first name of the person at this address
   *
   * @param name The first name as a String
   **/

  public void setFirstName(String name) {
    firstName = name;
  }

  /**
   * Returns the last name of the person at this address
   *
   * @return The last name as a String
   **/

  public String getLastName() {
    return lastName;
  }

  /**
   * Sets the last name of the person at this address
   *
   * @param name The last name as a String
   **/

  public void setLastName(String name) {
    lastName = name;
  }

  /**
   * Returns the first line of the street address
   *
   * @return The first line as a String
   **/

  public String getStreet1 () {
    return street1;
  }

  /**
   * Sets the first line of the street address
   *
   * @param street The first line as a String
   **/

  public void setStreet1(String street) {
    street1 = street;
  }

  /**
   * Returns the second line of the street address
   *
   * @return The second line as a String
   **/

  public String getStreet2 () {
    return street2;
  }

  /**
   * Sets the second line of the street address
   *
   * @param street The second line as a String
   **/

  public void setStreet2(String street) {
    street2 = street;
  }

  /**
   * Returns the city of the address
   *
   * @return The city as a String
   **/

  public String getCity () {
    return city;
  }

  /**
   * Sets the city of the address
   *
   * @param street The city as a String
   **/

  public void setCity(String c) {
    city = c;
  }

  /**
   * Returns the state (2 letter) of the address
   *
   * @return The state as a two-character String
   **/

  public String getState () {
    return state;
  }

  /**
   * Sets the state (2 letter) of the address
   *
   * @param street The state as a two-character String
   **/

  public void setState(String st) {
    state = st;
  }

  /**
   * Returns the postal code of the address
   *
   * @return The postal code as a String
   **/

  public String getPostalCode () {
    return postalCode;
  }

  /**
   * Sets the postal code of the address
   *
   * @param street The postal code as a String
   **/

  public void setPostalCode(String pc) {
    postalCode = pc;
  }

  private HashMap validationErrors = new HashMap();

  /**
   * Returns a validation error against a specific field.  If a field
   * was found to have an error during 
   * {@link #validateAddress validateAddress},  the error message
   * can be accessed via this method.
   *
   * @param fieldname The bean property name of the field
   * @return The error message for the field or null if none is
   * present.
   **/

  public String getFieldError(String fieldname) {
    return((String)validationErrors.get(fieldname));
  }
    
  /**
   * Sets the validation error against a specific field.  Used by
   * {@link #validateAddress validateAddress}.
   *
   * @param fieldname The bean property name of the field
   * @param error The error message for the field or null if none is
   * present.
   **/

  public void addFieldError(String fieldname, String error) {
    validationErrors.put(fieldname, error);
  }

  /**
   * Validates the various properties of the bean to make sure that
   * none of the required fields are blank.
   *
   * @return <code>true</code> if the values validated, otherwise
   * <code>false</code>
   **/

  public boolean validateAddress(){
    validationErrors.clear();
    boolean valid = true;
    if ((lastName == null) || (lastName.length() == 0)){
	    addFieldError("lastName", "Last Name is required.");
	    valid = false;
    } 
    if ((firstName == null) || (firstName.length() == 0)){
	    addFieldError("firstName", "First Name is required.");
	    valid = false;
    } 
    if ((street1 == null) || (street1.length() == 0)){
	    addFieldError("street1", "Street Address is required.");
      valid = false;
    } 
    if ((city == null) || (city.length() == 0)){
	    addFieldError("city", "City is required.");
	    valid = false;
    } 
    if ((state == null) || (state.length() == 0)){
	    addFieldError("state", "State is required.");
	    valid = false;
    } 
    if ((postalCode == null) || (postalCode.length() == 0)){
	    addFieldError("postalCode", "Postal Code is required.");
	    valid = false;
    } 
    return valid;
  }

  /**
   * Given an address ID, returns a new Address object populated
   * from the database, or null if no such address exists.
   *
   * @param ID The unique ID of the address in the database
   * @return A populated Address object, or null if none found.
   * @throws CustomeryActivityException Thrown on database errors
   **/

  public static Address findAddress(int ID)	throws CustomerActivityException{
    Address addr = null;
	  ///DBConnection dbConn = null;

    ///////////////   Start test ///////////////////////
    javax.sql.DataSource pool;
    Connection dbConnection = null; 

    try{         
        System.out.println("Customer.deleteAddress bean: start");
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
    ///  dbConn = TurbineDB.getConnection();
		///  if(dbConn == null){
    ///    cat.error("Can't get database connection");
    ///    throw new CustomerActivityException();
		///  }
		  PreparedStatement pstmt =
        dbConnection.prepareStatement(sql_bundle.getString("addressQuery"));
		  pstmt.setInt(1, ID);
		  ResultSet rs = pstmt.executeQuery();
		  if (rs.next()){
        addr = new Address();
        addr.setFirstName(rs.getString("FIRST_NAME"));
        addr.setLastName(rs.getString("LAST_NAME"));
        addr.setStreet1(rs.getString("STREET_1"));
        addr.setStreet2(rs.getString("STREET_2"));
        addr.setCity(rs.getString("CITY"));
        addr.setState(rs.getString("STATE"));
        addr.setPostalCode(rs.getString("POSTAL_CODE"));
        addr.setAddressID(ID);
      }else{
        ///cat.error("Couldn't find record for Address");
        System.out.println("Couldn't find record for Address");
		  }
		    rs.close();
		    pstmt.close();
		}catch (Exception e){
      ///cat.error("Error during findAddress", e);
      System.out.println("Error during findAddress " + e);
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
	  return addr;
	}

	

  /**
   * Creates a new address in the database and sets the address ID
   * of the object to the newly created record's ID
   *
   * @throws CustomerActivityException Thrown if there is an error
   * inserting the record in the database.
   **/

  public void createAddress() throws CustomerActivityException{
    ///DBConnection dbConn = null;
    ///////////////   Start test ///////////////////////
    javax.sql.DataSource pool;
    Connection dbConnection = null; 

    try{         
        System.out.println("Customer.deleteAddress bean: start");
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
    ///  dbConn = TurbineDB.getConnection();
		///  if (dbConn == null) {
    ///    ///cat.error("Can't get database connection");
    ///    throw new CustomerActivityException();
		///  }
		  PreparedStatement pstmt =
			dbConnection.prepareStatement(sql_bundle.getString("addressInsert"));
		    pstmt.setString(1, getFirstName());
		    pstmt.setString(2, getLastName());
		    pstmt.setString(3, getStreet1());
		    pstmt.setString(4, getStreet2());
		    pstmt.setString(5, getCity());
		    pstmt.setString(6, getState());
		    pstmt.setString(7, getPostalCode());
		    pstmt.executeUpdate();
		    pstmt.close();
		    pstmt =
			dbConnection.prepareStatement(sql_bundle.getString("addressID"));
		  ResultSet rs = pstmt.executeQuery();
		  if (rs.next()){
        addressID = rs.getInt(1);
		  }else{
        ///cat.error("Couldn't find record for new Address");
        System.out.println("Couldn't find record for new Address");
		  }
		  rs.close();
      pstmt.close();
    }catch (Exception e){
      ///cat.error("Error during createAddress", e);
      System.out.println("Error during createAddress" + e);
		  throw new CustomerActivityException();
		}finally{
      try{
        ///TurbineDB.releaseConnection(dbConn);
        dbConnection.close();
			}catch(Exception e){
        ///cat.error("Error during release connection", e);
        System.out.println("Error during release connection" + e);
			}
		}
	}

  /**
   * Updates an address in the database.
   *
   * @throws CustomerActivityException Thrown if there is an error
   * updating the record in the database.
   **/

  public void updateAddress() throws CustomerActivityException{
    ///DBConnection dbConn = null;
    ///////////////   Start test ///////////////////////
    javax.sql.DataSource pool;
    Connection dbConnection = null; 

    try{         
        System.out.println("Customer.updateAddress bean: start");
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
    ///  dbConn = TurbineDB.getConnection();
		///  if (dbConn == null){
    ///    cat.error("Can't get database connection");
    ///    throw new CustomerActivityException();
		///  }
		  PreparedStatement pstmt =
        dbConnection.prepareStatement(sql_bundle.getString("addressUpdate"));
		    pstmt.setString(1, getFirstName());
		    pstmt.setString(2, getLastName());
		    pstmt.setString(3, getStreet1());
		    pstmt.setString(4, getStreet2());
		    pstmt.setString(5, getCity());
		    pstmt.setString(6, getState());
		    pstmt.setString(7, getPostalCode());
		    pstmt.setInt(8, getAddressID());
		    pstmt.executeUpdate();
		    pstmt.close();
		}catch(Exception e){
      ///cat.error("Error during updateAddress", e);
      System.out.println("Error during updateAddress" + e);
		  throw new CustomerActivityException();
		}finally{
      try{
        ///TurbineDB.releaseConnection(dbConn);
        dbConnection.close();
			}catch(Exception e){
        ///cat.error("Error during release connection", e);
        System.out.println("Error during release connection" + e);
			}
		}
	}

  /**
   * Deletes an address from the database.
   *
   * @throws CustomerActivityException Thrown if there is an error
   * deleting the record in the database.
   **/

  public void deleteAddress() throws CustomerActivityException{
    ///DBConnection dbConn = null;
    ///////////////   Start test ///////////////////////
    javax.sql.DataSource pool;
    Connection dbConnection = null; 

    try{         
        System.out.println("Customer.deleteAddress bean: start");
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
    ///  dbConn = TurbineDB.getConnection();
		///  if (dbConn == null){
    ///    cat.error("Can't get database connection");
    ///    throw new CustomerActivityException();
		///  }
		  PreparedStatement pstmt =
        dbConnection.prepareStatement(sql_bundle.getString("addressDelete"));
		    pstmt.setInt(1, getAddressID());
		    pstmt.executeUpdate();
		    pstmt.close();
		}catch(Exception e){
      ///cat.error("Error during deleteAddress", e);
      System.out.println("Error during deleteAddress"+ e);
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
   * Returns a copy of the Address object
   *
   * @returns A duplicate of the object
   **/

  public Object clone() throws java.lang.CloneNotSupportedException {
    return super.clone();
  }
}


