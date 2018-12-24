package com.classified.product;

import java.util.Vector;
import java.util.HashMap;
import java.util.Iterator;
import java.text.NumberFormat;
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
///import org.apache.log4j.Category;
///import org.apache.turbine.services.db.TurbineDB;
///import org.apache.turbine.util.db.pool.DBConnection;
import com.classified.product.Product;
import java.util.ResourceBundle;
import com.classified.customer.*;
import com.classified.product.*;
import com.classified.exceptions.*;
import com.classified.cart.*;
import javax.naming.NamingException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingEnumeration;
import javax.naming.directory.InitialDirContext;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.AddressException;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.MessagingException;
import javax.mail.Transport;
import javax.servlet.http.*;
import com.braju.format.*;

/**
 * Orders store purchases after they are confirmed, they record all
 * the information about a purchase as it was at the time the purchase
 * was made.<P> 
 *
 * Copyright 2002, James M. Turner
 * No warranty is made as to the fitness of this code for any
 * particular purpose.  It is included for demonstration purposes
 * only.
 *
 * @author       James M. Turner
 */

public class Order{
  private static ResourceBundle sql_bundle =
    ResourceBundle.getBundle("com.bfg.product.SQLQueries");

  private static ResourceBundle email_bundle =
    ResourceBundle.getBundle("com.bfg.emailProperties");

  ///static Category cat = Category.getInstance(Order.class);

  protected Vector items = new Vector();

  /**
   * Returns a vector containing the items as they were when the
   * order was placed.  In particular, any price changes to products
   * will not be reflected in the copies in the order items.
   *
   * @return A vector containing CartItems
   **/

  public Vector getItems(){
    return items;
  }

  private void setItems(Vector it){
    items = it;
  }

  protected Customer customer;

  /**
   * Returns the customer who placed the order.
   *
   * @return The Customer who placed the order
   **/

  public Customer getCustomer(){
    return customer;
  }

  /**
   * Sets the customer who placed the order.
   *
   * @param c The Customer who placed the order
   **/

  public void setCustomer(Customer c){
    customer = c;
  }

  protected Address orderaddr;

  /**
   * Returns the shipping address for the order
   *
   * @return The Address that the order was shipped to
   **/

  public Address getAddress(){
    return orderaddr;
  }

  /**
   * Sets the shipping address for the order
   *
   * @param addr The Address that the order was shipped to
   **/

  public void setAddress(Address addr){
    orderaddr = addr;
  }

  protected CreditCard ordercard;

  /**
   * Returns the credit card information used for the order
   *
   * @return The Credit Card that the order was charge with
   **/

  public CreditCard getCreditCard(){
    return ordercard;
  }

  /**
   * Sets the credit card information used for the order
   *
   * @param cc The Credit Card that the order was charge with
   **/

  public void setCreditCard(CreditCard cc){
    ordercard = cc;
  }

    protected int orderNumber;

  /**
   * Returns the unique order number, which is an autoincrementing
   * database column used to uniquely identify each order record.
   *
   * @return the order number
   **/

  public int getOrderNumber(){
    return orderNumber;
  }

  private void setOrderNumber(int n){
    orderNumber = n;
  }

  protected Date orderDate;

  /**
   * Returns the date that the order was placed
   *
   * @return The date of the order
   **/

  public Date getOrderDate(){
    return orderDate;
  }

  private void setOrderDate(Date d){
    orderDate = d;
  }

  protected double orderSubtotal;

  /**
   * Returns the order total before tax and shipping
   *
   * @return The total order price before tax and shipping
   **/

  public double getOrderSubtotal(){
    return orderSubtotal;
  }

  /**
   * Returns the order total before tax and shipping, formatted as currency
   *
   * @return The total order price before tax and shipping as a
   * formatted string.
   **/

  public String getOrderSubtotalString(){
    NumberFormat nf = NumberFormat.getCurrencyInstance();
    return nf.format(getOrderSubtotal());
  }
	    
  private void setOrderSubtotal(double t){
    orderSubtotal = t;
  }

  protected double orderTax;

  /**
   * Returns the order tax
   *
   * @return The order tax
   **/

  public double getOrderTax(){
    return orderTax;
  }

  /**
   * Returns the order tax as a formatted currency
   *
   * @return The order tax as a formatted string
   **/

  public String getOrderTaxString(){
    NumberFormat nf = NumberFormat.getCurrencyInstance();
    return nf.format(getOrderTax());
  }
	    
  private void setOrderTax(double v){
    orderTax = v;
  }

  protected double orderShipping;

  /**
   * Returns the order shipping
   *
   * @return The order shipping
   **/

  public double getOrderShipping (){
    return orderShipping;
  }

  /**
   * Returns the order shipping as a formatted currency
   *
   * @return The order shipping as a formatted string
   **/

  public String getOrderShippingString(){
    NumberFormat nf = NumberFormat.getCurrencyInstance();
    return nf.format(getOrderShipping());
  }
	    
  private void setOrderShipping (double v){
    orderShipping = v;
  }

  protected double orderTotal;

  /**
   * Returns the order total after tax and shipping
   *
   * @return The total order price after tax and shipping
   **/

  public double getOrderTotal(){
    return orderTotal;
  }

  /**
   * Returns the order total after tax and shipping, formatted as currency
   *
   * @return The total order price after tax and shipping as a
   * formatted string.
   **/

  public String getOrderTotalString(){
    NumberFormat nf = NumberFormat.getCurrencyInstance();
    return nf.format(getOrderTotal());
  }
	    
  private void setOrderTotal(double v){
    orderTotal = v;
  }


  public Order(){
    super();
  }

  /**
   * Constructs an order object, taking it's data from a Customer,
   * Address, CreditCard and Cart object handed in as arguments.
   *
   * @param cust The customer placing the order
   * @param ad The shipping address for the order
   * @param cc The credit card used to place the order
   * @param cart The cart holding the items in the order
   * @return The newly created order
   **/

  public Order(Customer cust, Address ad, CreditCard cc, Cart cart){
    super();
    setAddress(ad);
    setCreditCard(cc);
    setCustomer(cust);
    Iterator item_it = cart.getItems();
    while (item_it.hasNext()){
      CartItem item = (CartItem) item_it.next();
	    items.add(item);
    }
    setOrderSubtotal(cart.getTotal());
    setOrderTotal(cart.getGrandTotal(ad));
    setOrderShipping(cart.getShipping(ad));
    setOrderTax(cart.getTax(ad));
  }

  /**
   * Records the order in the database, generating a new order number.
   *
   * @throws Exception Thrown if there is an error
   * inserting the record in the database.
   **/

  public void recordOrder()	throws Exception{
    ///DBConnection dbConn = null;
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
		///    throw new Exception();
    ///  }
      Address ad = (Address) getAddress().clone();
      CreditCard cc = (CreditCard) getCreditCard().clone();
      cc.setAddress((Address) cc.getAddress().clone());

      ad.createAddress();
      cc.getAddress().createAddress();
      cc.setCustomer(null);
      cc.createCreditCard();

      PreparedStatement pstmt =
        dbConnection.prepareStatement(sql_bundle.getString("createOrder"));
      pstmt.setString(1, getCustomer().getEmail());
      pstmt.setInt(2, ad.getAddressID());
      pstmt.setInt(3, cc.getCardID());
      pstmt.setDouble(4, getOrderSubtotal());
      pstmt.setDouble(5, getOrderTax());
      pstmt.setDouble(6, getOrderShipping());
      pstmt.setDouble(7, getOrderTotal());
      pstmt.executeUpdate();
      pstmt.close();
      pstmt =
        dbConnection.prepareStatement(sql_bundle.getString("getOrderID"));
      ResultSet rs = pstmt.executeQuery();
      if (rs.next()){
        setOrderNumber(rs.getInt(1));
      }else{
        ///cat.error("Couldn't find record for new Order");
        System.out.println("Couldn't find record for new Order");
		    throw new Exception();
      }
      rs.close();
      pstmt.close();
      Iterator items = getItems().iterator();
      pstmt = 
        dbConnection.prepareStatement(sql_bundle.getString("addOrderItem"));
      while (items.hasNext()){
        CartItem item = (CartItem) items.next();
		    pstmt.setInt(1, getOrderNumber());
		    pstmt.setString(2, item.getProduct().getISBN());
		    pstmt.setString(3, item.getProduct().getTitle());
		    pstmt.setInt(4, item.getQuantity());
		    pstmt.setDouble(5, item.getProduct().getPrice());
		    pstmt.setDouble(6, item.getLineItemPrice());
		    pstmt.executeUpdate();
      }
      pstmt.close();
      setOrderDate(new java.sql.Date(new java.util.Date().getTime()));
	  }catch (Exception e){
      ///cat.error("Error during createOrder", e);
      System.out.println("Error during createOrder " + e);
		  throw new CustomerActivityException();
		}finally{
      try{
        ///TurbineDB.releaseConnection(dbConn);
        dbConnection.close();
		  }catch(Exception e){
        ///cat.error("Error during release connection", e);
        System.out.println("Error during release connection " + e);
		  }
	  }
  }

  /**
   * Given an order number, loads that order from the database and
   * returns it.
   *
   * @param orderNumber The order number to retrieve
   * @return The order or null if not found.
   * @throws Exception Thrown if there is an error
   * loading the record from the database.
   **/

  public static Order findOrder(int orderNumber) throws Exception{
    Order order = null;
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
		///    throw new Exception();
    ///  }
      PreparedStatement pstmt =
		    dbConnection.prepareStatement(sql_bundle.getString("findOrder"));
      pstmt.setInt(1, orderNumber);
      ResultSet rs = pstmt.executeQuery();

      // If no such order found, return null

      if(!rs.next()){
		    rs.close();
		    pstmt.close();
		    return null;
      }

      // Create a new order to hold the one we found

      order = new Order();
      order.setOrderNumber(rs.getInt("ORDER_ID"));
      order.setAddress(Address.findAddress(rs.getInt("ADDRESS_KEY")));
      order.setCreditCard(CreditCard.findCreditCard(rs.getInt("CARD_KEY")));
      order.setOrderDate(rs.getDate("ORDER_DATE"));
      order.setOrderSubtotal(rs.getDouble("ORDER_SUBTOTAL"));
      order.setOrderTax(rs.getDouble("ORDER_TAX"));
      order.setOrderShipping(rs.getDouble("ORDER_SHIPPING"));
      order.setOrderTotal(rs.getDouble("ORDER_TOTAL"));
      order.setCustomer(Customer.findCustomer(rs.getString("EMAIL_ADDRESS")));
      rs.close();
      pstmt.close();

      // Now read in the items associated with the order

      pstmt = dbConnection.prepareStatement(sql_bundle.getString("getOrderItems"));
      pstmt.setInt(1, orderNumber);
      rs = pstmt.executeQuery();
      while (rs.next()){
        CartItem item = new CartItem();
		    Product product = new Product();
		    item.setProduct(product);
		    item.setQuantity(rs.getInt("QUANTITY"));
		    product.setISBN(rs.getString("PRODUCT_ISBN"));
		    product.setTitle(rs.getString("PRODUCT_TITLE"));
		    product.setPrice(rs.getDouble("UNIT_PRICE"));
		    double totalPrice = rs.getDouble("TOTAL_PRICE");
		    // If price is zero, must be a promotion item
		    if (totalPrice == 0F){
          item.setPromotionItem(true);
		    }
		    order.getItems().add(item);
      }
      rs.close();
      pstmt.close();
	  }catch(Exception e){
      ///cat.error("Error during createOrder", e);
      System.out.println("Error during createOrder " + e);
		  throw new CustomerActivityException();
		}finally{
      try{
        ///TurbineDB.releaseConnection(dbConn);
        dbConnection.close();
		  }catch(Exception e){
        ///cat.error("Error during release connection", e);
        System.out.println("Error during release connection " + e);
		  }
	  }
    return order;
  }
    
  /**
   * Mails a receipt for the order to the customer.
   *
   * @param email The address to mail the receipt to.
   **/

  public void emailReceipt(String email) 
    throws NamingException, AddressException, MessagingException{
    ///Context initCtx = new InitialContext();
    ///Context envCtx = (Context) initCtx.lookup("java:comp/env");
    ///Session session = (Session) envCtx.lookup("mail/session");
    
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
    message.setSubject("Receipt for order " + getOrderNumber());
    StringBuffer contents = new StringBuffer();
    contents.append("Thank you for our Classified Ads.\n\n");
    contents.append("Here is the receipt for your order " + getOrderNumber() +
			" placed on " + getOrderDate() + "\n\n");
    contents.append("CODE        TITLE                           QUANT  PRICE  TOTAL\n");
    contents.append("================================================================\n");
    Iterator items = getItems().iterator();
    while (items.hasNext()){
	    CartItem it = (CartItem) items.next();
	    Parameters p = new Parameters();
	    p.add(it.getProduct().getISBN());
	    p.add(it.getProduct().getTitle());
	    p.add(it.getQuantity());
	    p.add(it.getProduct().getPrice());
	    p.add(it.getLineItemPrice());
                      // line below could be com.java.text.*
	    contents.append(com.braju.format.Format.sprintf("%-10.10s  %-30.30s  %2d     $%5.2f $%5.2f\n", p));
    }
    message.setContent(contents.toString(), "text/plain");
    Transport.send(message);
  }
}
	    
