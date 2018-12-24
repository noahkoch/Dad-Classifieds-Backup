package com.classified.customer;

///package com.bfg.customer;

import java.util.Vector;
import java.util.HashMap;
import java.util.Iterator;
import java.util.regex.*;
///import org.apache.turbine.services.db.TurbineDB;
///import org.apache.turbine.util.db.pool.DBConnection;
///import org.apache.turbine.util.TurbineConfig;
import com.classified.exceptions.DuplicateEmailAddressException;
import com.classified.cart.Cart;
import com.classified.cart.CartItem;
import com.classified.product.Product;
import com.classified.product.Category;
import com.classified.product.Order;
import com.classified.exceptions.CustomerActivityException;
import java.sql.*;
import javax.sql.*;
import java.util.Properties;
import java.text.*;
import java.util.ResourceBundle;
///import org.apache.log4j.Category;
import javax.naming.NamingException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingEnumeration;
import javax.naming.directory.InitialDirContext;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.AddressException;
import javax.servlet.http.*;

/**
 * Holds the information about the currently logged in customer.<P>
 *
 * Copyright 2002, James M. Turner
 * No warranty is made as to the fitness of this code for any
 * particular purpose.  It is included for demonstration purposes
 * only.
 *
 * @author       James M. Turner
 */

public class Customer implements HttpSessionBindingListener {

  // database connection
  //private Connection dbConnection;  
  //private javax.sql.DataSource pool;

  private String last_name;
  private String first_name;
  private String email;
  private String password;
  private String password2;  
  private HashMap addresses = new HashMap();;
  private HashMap wallet = new HashMap();
  private Cart cart;
  private int customer_id;

  ///static Category cat = Category.getInstance(Customer.class);

  private static ResourceBundle sql_bundle =
    ResourceBundle.getBundle("com.classified.customer.SQLQueries");

  private static ResourceBundle email_bundle =
    ResourceBundle.getBundle("com.classified.emailProperties");

  /**
   * Returns the email address of this customer.
   *
   * @return The email address
   **/

  public String getEmail() {
    return email;
  }

  /**
   * Sets the email address of this customer.
   *
   * @param em The email address
   **/

  public void setEmail(String em){
    email = em;
  }

  /**
   * Gets the password of this customer.
   *
   * @return The password as a String
   **/

  public String getPassword() {
    return password;
  }

  /** 
   * Get the second password and check if
   * the same as the first password
   */
   
  public String getPassword2() {
    return password2;
  }  

  /**
   * Sets the password of this customer.
   *
   * @param pw The password as a String
   **/

  public void setPassword(String pw) {
    password = pw;
  }

  /**
   * Sets the second (verify) password of this customer.
   *
   * @param pw The password as a String
   **/

  public void setPassword2(String pw2) {
    password2 = pw2;
  }  

  /**
   * Returns the unique customer ID, which is an autoincrementing
   * database column used to uniquely identify each customer record.
   *
   * @return the customer ID
   **/

  public int getCustomerId() {
    return customer_id;
  }

  /**
   * Sets the unique customer ID, which is an autoincrementing
   * database column used to uniquely identify each customer record.
   * Note that in normal usage, this should not be used because IDs
   * are assigned by the database.
   *
   * @param id the customer ID
   **/

  public void setCustomerId(int id) {
    customer_id = id;
  }

  /**
   * Returns a HashMap containing all the credit cards in the
   * customer's wallet,  with the key being the card id number.
   *
   * @return A HashMap containing CreditCard objects
   **/

  public HashMap getWallet() {
    return wallet;
  }

  /**
   * Returns a Cart containing all the current items in the
   * customer's shopping cart.
   *
   * @return The Customer's Cart
   **/

  public Cart getCart() {
    return cart;
  }

  /**
   * Sets the Cart, which contains all the current items in the
   * customer's shopping cart.
   *
   * @param c The Customer's Cart
   **/

  public void setCart(Cart c) {
    cart = c;
  }


  /**
   * Returns a HashMap containing all the addresses in the
   * customer's address book,  with the key being the address id.
   *
   * @return A HashMap containing Address objects
   **/

  public HashMap getAddressBook() {
    return addresses;
  }

  private HashMap validationErrors = new HashMap();

  /**
   * Returns a validation error against a specific field.  If a field
   * was found to have an error during 
   * {@link #validateCustomer validateCustomer},  the error message
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
   * {@link #validateCustomer validateCustomer}.
   *
   * @param fieldname The bean property name of the field
   * @param error The error message for the field or null if none is
   * present.
   **/

  public void addFieldError(String fieldname, String error) {
    validationErrors.put(fieldname, error);
  }

  /**
   * Validates the customer for missing or invalid fields.
   *
   * @return <code>true</code> if the card passes the validation
   * checks,  otherwise  <code>false</code>
   **/

  public boolean validateCustomer(){

  
    validationErrors.clear();
    boolean valid = true;
//Pattern emailPattern = Pattern.compile("^\\S+@\\S+$");
//  ^([a-zA-Z0-9_\-\.]+)@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.)|(([a-zA-Z0-9\-]+\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\]?)$
//Pattern emailPattern = Pattern.compile("^([a-zA-Z0-9_\\-\\.]+)@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.)|(([a-zA-Z0-9\\-]+\\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\\]?)$");
//Pattern emailPattern = Pattern.compile("[^A-Za-z0-9\\.\\@_\\-~#]+");

//////////////////
//Pattern TOMSemailPattern = Pattern.compile("\\@|\\.|\\s");  // change s to S when using for real
///Pattern TOMSemailPattern = Pattern.compile("{1,1}\\@+|\\.|\\s"); // change s to S when using for real
///Matcher fit = TOMSemailPattern.matcher(email);
// \w A word character: [a-zA-Z_0-9] 
// \s  A whitespace character: [ \t\n\x0B\f\r]
// \S  A non-whitespace character: [^\s]

    if((email == null) || (email.length() == 0)){
      addFieldError("email", "e-Mail Address is required.");
	    valid = false;
    }else{
      if((email.indexOf("@") == -1) || (email.indexOf(" ") > -1)) {
      addFieldError("email", "Please supply a valid address.");
      valid = false;
      }
    }
///      //if(fit.matches()){
///      if(fit.find()){
///        System.out.println ("\"" + email + "\" IS a possible email address");
///        addFieldError("email", "Please LOOKS BAD  address!.");
///        valid = false;
///      }
    

    if((password == null) || (password.trim().length() == 0)){
	    addFieldError("password", "Password is required.");
	    valid = false;
    }else{ // password must be at least 6 chars
      if((password == null) || (password.trim().length() == 0) || password.trim().length() < 4 
                  || password.trim().length() > 20  || password.indexOf(" ") > -1 ){
        addFieldError("password", "Password must be at least 4 characters long with no spaces.");
        valid = false;
      }
    }

/////////////// TEST //////////////
    
    // verify password
    if((password2 == null) || (password2.trim().length() == 0)||
                (password == null) || (password.trim().length() == 0)){
      addFieldError("password2", "Confirm Password is required.");
      valid = false;
    }else{
      if(!password.equals(password2)){
        addFieldError("password2", "Passwords do not match, try again."); 
        valid = false;
      }
    }
    //}
//////////////// END TEST //////////////    
      return valid;
  }

  /**
   * Mails a password reminder to the currently registered e-mail
   * address for a customer.
   *
   **/
/**
  public void sendPasswordReminder() 
    throws NamingException, AddressException, MessagingException {
    Context initCtx = new InitialContext();
    Context envCtx = (Context) initCtx.lookup("java:comp/env");
    Session session = (Session) envCtx.lookup("mail/session");
    // mail/session would be "smtp.central.cox.net"

    Message message = new MimeMessage(session);
    message.setFrom(new InternetAddress(email_bundle.getString("fromAddr")));
    InternetAddress to[] = new InternetAddress[1];
    to[0] = new InternetAddress(email);
    message.setRecipients(Message.RecipientType.TO, to);
    message.setSubject(email_bundle.getString("lostSubj"));
    message.setContent(email_bundle.getString("lostContent") + password, 
			   "text/plain");
    Transport.send(message);
  }
*/

//////////////  TEST EMAIL (original above) below works fine /////////////
  public void sendPasswordReminder() 
    throws NamingException, AddressException, MessagingException {

    Properties props = System.getProperties();
    ///props.put("mail.smtp.host", "smtp.central.cox.net");
    // Note: this is not the same email_bundle as used in SubmitDBSerlvet.
    // This one is found in com.classified.emailProperties
    props.put("mail.smtp.host", email_bundle.getString("MAIL_HOST"));
    Session session = Session.getDefaultInstance(props, null);
    session.setDebug(false);

    Message message = new MimeMessage(session); 
    message.setFrom(new InternetAddress(email_bundle.getString("fromAddr")));
    InternetAddress to[] = new InternetAddress[1];
    to[0] = new InternetAddress(email);
    message.setRecipients(Message.RecipientType.TO, to);
    message.setSubject(email_bundle.getString("lostSubj"));
    message.setContent(email_bundle.getString("lostContent") + password, 
			   "text/plain");
    Transport.send(message);
  }




  /**
   * Given an email address, returns a new customer object populated
   * from the database, or null if no such email exists.
   *
   * @param emailAddress The email address of the customer in the database
   * @return A populated Customer object, or null if none found.
   * @throws CustomeryActivityException Thrown on database errors
   **/

  public static Customer findCustomer(String emailAddress) 
                                        throws CustomerActivityException {
    
                                        
    ///DBConnection dbConn = null;
    javax.sql.DataSource pool;
    Connection dbConnection = null; 
	  Customer cust = null;
	  ///try{
///////////////   Start test

    try{
        System.out.println("Customer.findCustomer bean: start");
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

    
      ///dbConn = TurbineDB.getConnection();
		  ///if (dbConn == null){
        ///cat.error("Can't get database connection");
     ///   System.out.println("Can't get database connection");
     ///   throw new CustomerActivityException();
     /// }
		  PreparedStatement pstmt =
        dbConnection.prepareStatement(sql_bundle.getString("findQuery"));
		    pstmt.setString(1, emailAddress);
		    ResultSet rs = pstmt.executeQuery();
		    if (rs.next()){
          cust = new Customer();
          cust.setCustomerId(rs.getInt("CUSTOMER_ID"));
          cust.setEmail(rs.getString("EMAIL_ADDRESS"));
          cust.setPassword(rs.getString("PASSWORD"));
		    }else{
          return null;
		    }
		    rs.close();
		    pstmt.close();

        
		    pstmt = dbConnection.prepareStatement(sql_bundle.getString("getAddressBook"));
		    pstmt.setInt(1, cust.getCustomerId());
		    rs = pstmt.executeQuery();
		    while (rs.next()) {
          Address addr = Address.findAddress(rs.getInt(1));
          cust.addresses.put(new Integer(addr.getAddressID()), addr);
		    }
		    rs.close();
		    pstmt.close();

		    pstmt = dbConnection.prepareStatement(sql_bundle.getString("getWallet"));
		    pstmt.setInt(1, cust.getCustomerId());
		    rs = pstmt.executeQuery();
		    while (rs.next()) {
          CreditCard cc = CreditCard.findCreditCard(rs.getInt(1));
          cc.setCustomer(cust);
          cust.wallet.put(new Integer(cc.getCardID()), cc);
		    }
		    rs.close();
		    pstmt.close();
		}catch (Exception e){
// You need to catch the catch(NamingException e){ somewhere  
		    ///cat.error("Error during findCustomer", e);
        System.out.println("Error during findCustomer" + e);
		    throw new CustomerActivityException();
		}finally{
		    try{           
			    ///TurbineDB.releaseConnection(dbConn);
            dbConnection.close();
        }catch (SQLException e){
			    ///cat.error("Error during releaseConnection", e);
          System.out.println("Error during releaseConnection" + e);
			}
		}
	    return cust;
	}



/**
 * Given a email and password verifyUserQuery(), checks the database to make 
 * sure the user has registered with classifed ads. Once registered
 * the user can access the features, look4u, update etc. in classified
 * ads. If they are not verified, they must create a new user.
 * @param emailAddress The email address of the customer in the database
 * @param password  ........... to be done.
 */

  public boolean verifyUserQuery(String emailAddress, String password) 
                                        throws CustomerActivityException {
    javax.sql.DataSource pool;
    Connection dbConnection = null; 
	  Customer cust = null;
System.out.println("From Customer.verifyUserQuery() " + emailAddress + " " + password);    

    try{
        System.out.println("Customer.findCustomer bean: start");
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
		  PreparedStatement pstmt =
        dbConnection.prepareStatement(sql_bundle.getString("verifyUserQuery"));
		    pstmt.setString(1, emailAddress);
		    pstmt.setString(2, password);        
		    ResultSet rs = pstmt.executeQuery();
		    if (rs.next()){
          
          ///cust = new Customer();
          ///cust.setCustomerId(rs.getInt("CUSTOMER_ID"));
          ///cust.setEmail(rs.getString("EMAIL_ADDRESS"));
          ///cust.setPassword(rs.getString("PASSWORD"));
// DO YOU NEED ALL THE ABOVE STUFF??? WHY NOT SIMPLY RETURN TRUE?? BECAUSE THE
// USER HAS A MATCHING LOGIN???
          pstmt.close();
          return true;
		    }else{
          pstmt.close();
          return false;
		    }
		    ///rs.close();
		    ///pstmt.close();

		}catch (Exception e){
// You need to catch the catch(NamingException e){ somewhere  
		    ///cat.error("Error during findCustomer", e);
        System.out.println("Error during verifyUserQuery" + e);
		    throw new CustomerActivityException();
		}finally{
		    try{           
			    ///TurbineDB.releaseConnection(dbConn);           
            dbConnection.close();
        }catch (SQLException e){
			    ///cat.error("Error during releaseConnection", e);
          System.out.println("Error during releaseConnection" + e);
			}
		}
	}
  

  /**
   * Creates a new customer in the database and sets the customer ID
   * of the object to the newly created record's ID
   *
   * @throws CustomerActivityException Thrown if there is an error
   * inserting the record in the database.
   **/

  public void createCustomer() throws CustomerActivityException, DuplicateEmailAddressException{
    if (findCustomer(getEmail()) != null) {
      throw new DuplicateEmailAddressException();
    }

///////////////   Start test ///////////////////////
    javax.sql.DataSource pool;
    Connection dbConnection = null; 

    try{
           
        System.out.println("Customer.createCustomer bean: start");
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

    
    //DBConnection dbConn = null;
    //Connection dbConnection = null;
    ///try{
    ///  dbConn = TurbineDB.getConnection();
    ///  if(dbConn == null){
		///    ///cat.error("Can't get database connection");
    //    System.out.println("Can't get database connection");
		 ///   throw new CustomerActivityException();
     // }
      PreparedStatement pstmt =
		    dbConnection.prepareStatement(sql_bundle.getString("createQuery"));
      pstmt.setString(1, getEmail());
      pstmt.setString(2, getPassword());
      pstmt.executeUpdate();
      pstmt.close();
      pstmt =
		    dbConnection.prepareStatement(sql_bundle.getString("findQuery"));
      pstmt.setString(1, getEmail());
      ResultSet rs = pstmt.executeQuery();
      if (rs.next()){
		    setCustomerId(rs.getInt("CUSTOMER_ID"));
      }else{
		    ///cat.error("Couldn't find record for new Customer");
        System.out.println("Couldn't find record for new Customer");
      }
      rs.close();
      pstmt.close();
	  }catch (Exception e){
      ///cat.error("Error during createCustomer", e);
      System.out.println("Error during createCustomer" + e);
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
   * Updates an customer in the database.
   *
   * @throws CustomerActivityException Thrown if there is an error
   * updating the record in the database.
   **/

  public void updateCustomer() throws CustomerActivityException{
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
    ///  dbConn = TurbineDB.getConnection();
     /// if(dbConn == null){
     ///   ///cat.error("Can't get database connection");
      ///  System.out.println("Can't get database connection");
     ///   throw new CustomerActivityException();
     /// }
      PreparedStatement pstmt =
          dbConnection.prepareStatement(sql_bundle.getString("updateQuery"));
      pstmt.setString(1, getEmail());
      pstmt.setString(2, getPassword());
      pstmt.setInt(3, getCustomerId());
      pstmt.executeUpdate();
      pstmt.close();
    }catch (Exception e){
      ///cat.error("Error during updateCustomer", e);
      System.out.println("Error during updateCustomer" + e);
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
   * Adds an address to the customer's address book in the database.
   *
   * @param addr An address which has already been created.
   * @throws CustomerActivityException Thrown if there is an error
   * updating the record in the database.
   **/

  public void addAddress(Address addr) throws CustomerActivityException{
	    ///DBConnection dbConn = null;
    ///////////////   Start test ///////////////////////
    javax.sql.DataSource pool;
    Connection dbConnection = null; 

    try{         
        System.out.println("Customer.addAddress bean: start");
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
	   /// try{
		 ///   dbConn = TurbineDB.getConnection();
		 ///   if(dbConn == null){
      ///    ///cat.error("Can't get database connection");
      ///    System.out.println("Can't get database connection");
      ///    throw new CustomerActivityException();
		  ///  }
		    PreparedStatement pstmt =
          dbConnection.prepareStatement(sql_bundle.getString("addAddress"));
		    pstmt.setInt(1, addr.getAddressID());
		    pstmt.setInt(2, getCustomerId());
		    pstmt.executeUpdate();
		    pstmt.close();
		    getAddressBook().put(new Integer(addr.getAddressID()), addr);
      }catch (Exception e){
		    ///cat.error("Error during addAddress", e);
        System.out.println("Error during addAddress" + e);
		    throw new CustomerActivityException();
      }finally{
		    try{
          dbConnection.close();
			    ///TurbineDB.releaseConnection(dbConn);
        }catch (Exception e){
			    ///cat.error("Error during release connection", e);
          System.out.println("Error during release connection" + e);
        }
      }
	}


  /**
   * Delete an address from the customer's address book in the database.
   *
   * @param addr An address to be deleted
   * @throws CustomerActivityException Thrown if there is an error
   * updating the record in the database.
   **/

  public void deleteAddress(Address addr) throws CustomerActivityException {
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
		    ///cat.error("Can't get database connection");
    ///    System.out.println("Can't get database connection");
		 ///   throw new CustomerActivityException();
     /// }
      PreparedStatement pstmt =
		    dbConnection.prepareStatement(sql_bundle.getString("removeAddress"));
      pstmt.setInt(1, addr.getAddressID());
      pstmt.setInt(2, getCustomerId());
      pstmt.executeUpdate();
      pstmt.close();
	  }catch (Exception e){
      ///cat.error("Error during addAddress", e);
      System.out.println("Error during addAddress" + e);
      throw new CustomerActivityException();
	  }finally{
      try{
        dbConnection.close();
        ///TurbineDB.releaseConnection(dbConn);
		  }catch (Exception e){
        ///cat.error("Error during release connection", e);
        System.out.println("Error during release connection" + e);
		  }
	  }
  }


  /**
   * Delete an customer from the database.
   *
   * @throws CustomerActivityException Thrown if there is an error
   * deleting the record in the database.
   **/

  public void deleteCustomer() throws CustomerActivityException {
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
		///    ///cat.error("Can't get database connection");
    ///    System.out.println("Can't get database connection");
		 ///   throw new CustomerActivityException();
    ///  }
      PreparedStatement pstmt =
		    dbConnection.prepareStatement(sql_bundle.getString("deleteQuery"));
      pstmt.setInt(1, getCustomerId());
      pstmt.executeUpdate();
      pstmt.close();
      Iterator it = getAddressBook().keySet().iterator();
      while (it.hasNext()){
		    Address addr =  (Address) getAddressBook().get(it.next());
		    addr.deleteAddress();
		    deleteAddress(addr);
      }
	  }catch (Exception e){
      ///cat.error("Error during deleteCustomer", e);
      System.out.println("Error during deleteCustomer" + e);
      throw new CustomerActivityException();
    }finally{
      try{
        dbConnection.close();
        ///TurbineDB.releaseConnection(dbConn);
		  }catch (Exception e){
        ///cat.error("Error during release connection", e);
        System.out.println("Error during release connection" + e);
		  }
	  }
  }

  /** 
   * Required method for HttpSessionBindingListener
   *
   * @param event The event
   **/
  public void valueBound(HttpSessionBindingEvent event) {
    ///cat.debug("User bound");
    System.out.println("User bound");
  }

  /** 
   * Required method for HttpSessionBindingListener.  Causes the
   * content of the shopping cart to be saved when the customer's
   * session times out.
   *
   * @param event The event
   **/

  public void valueUnbound(HttpSessionBindingEvent event){//right braket of this
    //method originally went on the bottom of this method  .
  }


  /**
    cat.debug("In unbound");
    ///DBConnection dbConn = null;
    Connection dbConnection = null;
    Cart cart = getCart();
    try{
      dbConn = TurbineDB.getConnection();
      if (dbConn == null){
		    cat.error("Can't get database connection");
      }
      PreparedStatement pstmt =
		    dbConn.prepareStatement(sql_bundle.getString("cartClear"));
      pstmt.setInt(1, getCustomerId());
      pstmt.executeUpdate();
      pstmt.close();
      cat.debug("Deleted Cart");
      pstmt =
		    dbConn.prepareStatement(sql_bundle.getString("cartAdd"));
      if (cart != null){
		    Iterator items = cart.getItems();
		    while (items.hasNext()){
          CartItem item = (CartItem) items.next();
          pstmt.setString(1, item.getProduct().getISBN());
          pstmt.setInt(2, item.getQuantity());
          pstmt.setInt(3, getCustomerId());
          pstmt.executeUpdate();
          cat.debug("Added item " + item.getProduct().getISBN());
        }
      }else{
        cat.debug("Cart is null");
      }
      pstmt.close();
	  }catch (Exception e){
      cat.error("Error during valueUnbound", e);
	  }finally{
      try{
        TurbineDB.releaseConnection(dbConn);
		  }catch (Exception e){
        cat.error("Error during release connection", e);
		  }
	  }
  }
  */

  

  /** 
   * Used to fill a cart from the database, which has been
   * previously saved by a timeout.
   *
   **/

  public void fillCart(){
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
    
    Cart cart = getCart();
    ///try{
    ///    dbConn = TurbineDB.getConnection();
    ///  if(dbConn == null){
		///    cat.error("Can't get database connection");
    ///  }
      PreparedStatement pstmt =
		    dbConnection.prepareStatement(sql_bundle.getString("cartQuery"));
      pstmt.setInt(1, getCustomerId());
      ResultSet rs = pstmt.executeQuery();
      while (rs.next()){
		    Product p = Product.findProduct(rs.getString("ISBN"));
		    if (p != null) {
        cart.addItem(p, rs.getInt("QUANTITY"));
        }
      }
      rs.close();
      pstmt.close();
	  }catch (Exception e){
      ///cat.error("Error during fillCart", e);
      System.out.println("Error during fillCart" + e);
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
   * Returns a vector of Order objects which hold all the previous
   * orders filled for this customer
   *
   * @return A vector of Order objects.
   *
   **/

  public Vector getOrderHistory() {
    Vector orders = new Vector();
    ///DBConnection dbConn = null;
    ///////////////   Start test ///////////////////////
    javax.sql.DataSource pool;
    Connection dbConnection = null; 

    try{         
        System.out.println("Customer.addAddress bean: start");
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
    ///  dbConnection = TurbineDB.getConnection();
    ///  if (dbConnection == null) {
		////    ///cat.error("Can't get database connection");
     ///   System.out.println("Can't get database connection");
    ///  }
      PreparedStatement pstmt =
		    dbConnection.prepareStatement(sql_bundle.getString("orderHistory"));
      pstmt.setString(1, getEmail());
      ResultSet rs = pstmt.executeQuery();
      while (rs.next()){
		    orders.add(Order.findOrder(rs.getInt("ORDER_ID")));
      }
      rs.close();
      pstmt.close();
	  }catch (Exception e){
      ///cat.error("Error during getOrderHistory", e);
      System.out.println("Error during getOrderHistory " + e);
	  }finally{
      try{
        ///TurbineDB.releaseConnection(dbConn);
        dbConnection.close();
		  }catch(Exception e){
        ///cat.error("Error during release connection", e);
        System.out.println("Error during release connection " + e);
		  }
	  }
    return orders;
  }
 

	
/**
  public static void main(String[] args){
    TurbineConfig tc = new TurbineConfig("com/bfg/props/", "TurbineResources.properties");
    tc.init();

    Customer c = new Customer();
    c.setPassword("easilyguessed");
    c.setEmail("namethatshouldneverbeused@bogussite.com");
    try{
	    c.createCustomer();
	    System.out.println("Good Test: Create Customer");
    }catch (Exception e){
	    System.out.println("Failed Test: Create Customer");
	    e.printStackTrace();
    }try{
	    c.createCustomer();
	    System.out.println("Failed Test: Create Duplicate Customer");
    }catch(DuplicateEmailAddressException e){
	    System.out.println("Good Test: Create Duplicate Customer");
    }catch (Exception e){
	    System.out.println("Failed Test: Create Duplicate Customer");
	    e.printStackTrace();
    }try{
	    Customer c1 = 
      findCustomer("namethatshouldneverbeused@bogussite.com");
	    if(c1 != null){
            System.out.println("Good Test: Find Real Customer");
	    }else{
            System.out.println("Failed Test: Find Real Customer");
	    }
    }catch(Exception e){
	    System.out.println("Failed Test: Find Real Customer");
    }try{
	    Customer c1 = findCustomer("othernamethatshouldneverbeused@bogussite.com");
	    if(c1 != null){
        System.out.println("Failed Test: Find Fake Customer");
	    }else{
        System.out.println("Good Test: Find Fake Customer");
      }
    }catch(Exception e){
	    System.out.println("Failed Test: Find Fake Customer");
    }try{
	    c.deleteCustomer();
	    Customer c1 = findCustomer("namethatshouldneverbeused@bogussite.com");
	    if (c1 != null) {
        System.out.println("Failed Test: Delete Customer");
	    }else{
        System.out.println("Good Test: Delete Customer");
	    }
    }catch(Exception e){
	    System.out.println("Failed Test: Delete Customer");
    }
  }
  */
}

