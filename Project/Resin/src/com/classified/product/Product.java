package com.classified.product;

import java.util.Vector;
import java.util.HashMap;
import java.util.Iterator;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
///import org.apache.turbine.services.db.TurbineDB;
///import org.apache.turbine.util.db.pool.DBConnection;
///import org.apache.turbine.util.TurbineConfig;
import com.classified.exceptions.ProductActivityException;
import com.classified.exceptions.CustomerActivityException; 
import java.sql.*;
import javax.sql.*;
import java.util.ResourceBundle;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.directory.InitialDirContext;
///import org.apache.log4j.Category;

public class Product{
  private static ResourceBundle sql_bundle =
    ResourceBundle.getBundle("com.classified.product.SQLQueries");

    ///static Category cat = Category.getInstance(Product.class);

  protected String pISBN;
  protected String pTitle;
  protected Vector pAuthors = new Vector();
  protected double pPrice;
  protected java.sql.Date pPubDate;
  protected String pDescription;
  protected static HashMap products = new HashMap();

  public String getISBN(){
    return pISBN;
  }

  public String getTitle(){
    return pTitle;
  }

  public Vector getAuthors(){
    return pAuthors;
	}

  private String getAuthorHREF(Author author){
    return "<A HREF=\"/classified/product/ByAuthor.jsp?author=" +
                        author.getName() + "\">" + author.getName() + "</A>";
  }


  public String getAuthorString(){
    Vector authors = getAuthors();
    if (authors.size() == 1){ 
      return (getAuthorHREF((Author)authors.elementAt(0)));
    }
    StringBuffer s = new StringBuffer();
    Iterator i = authors.iterator();

    while (i.hasNext()){
      Author author = (Author) i.next();
	    if (author == authors.firstElement()){
        s.append(getAuthorHREF(author));
	    }else{
        s.append("; " + getAuthorHREF(author));
	    }
    }
    return(s.toString());
  }

  public double getPrice() {
    return pPrice;
  }

  public String getPriceString() {
    NumberFormat nf = NumberFormat.getCurrencyInstance();
    return nf.format(pPrice);
  }

  public java.sql.Date getPubDate() {
    return pPubDate;
  }

  public String getPubDateString(){
    SimpleDateFormat df = new SimpleDateFormat("MMM dd, yyyy");
    return(df.format(getPubDate()));
  }

  public String getDescription(){
    return pDescription;
  }
  
  public void setISBN(String ISBN){
    pISBN = ISBN;
  }

  public void setTitle(String Title){
    pTitle = Title;
  }

  public void setAuthors (Vector Authors){
    pAuthors = Authors;
	}

  public void setPrice(double Price){
    pPrice = Price;
  }

  public void setPubDate(java.sql.Date PubDate){
    pPubDate = PubDate;
  }

  public void setDescription(String Description) {
    pDescription = Description;
  }

  public static Product findProduct(String ISBN) throws ProductActivityException{
    NumberFormat nf = NumberFormat.getInstance();
	  if (products.get(ISBN) != null){
      return (Product) products.get(ISBN);
	  }
	  ///DBConnection dbConn = null;
	  Product prod = null;
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
    ///      throw new ProductActivityException();
    ///    }
		    PreparedStatement pstmt =
          dbConnection.prepareStatement(sql_bundle.getString("findQuery"));
          pstmt.setString(1, ISBN);
          ResultSet rs = pstmt.executeQuery();
          if (rs.next()){
            prod = new Product();
            prod.setISBN(rs.getString("ISBN"));
            prod.setTitle(rs.getString("TITLE"));
            prod.setPrice(nf.parse(rs.getString("PRICE")).doubleValue());
            prod.setPubDate(rs.getDate("PUB_DATE"));
            prod.setDescription(rs.getString("DESCRIPTION"));
          }
          rs.close();
          pstmt.close();
          if(prod != null){
            pstmt = dbConnection.prepareStatement(sql_bundle.getString("authorQuery"));
            pstmt.setString(1, ISBN);
            rs = pstmt.executeQuery();
            while(rs.next()){
              Author author = Author.findAuthor(rs.getString("AUTHOR_NAME"));
              prod.getAuthors().add(author);
              author.getBooks().add(prod);
            }
            rs.close();
            pstmt.close();
            products.put(ISBN, prod);
          }
    }catch (Exception e){
      ///cat.error("Error during findProduct", e);
      System.out.println("Error during findProduct " + e);
		  e.printStackTrace();
		  throw new ProductActivityException();
		}finally{
		    try{
          ///TurbineDB.releaseConnection(dbConn);
          dbConnection.close();
        }catch (Exception e){
          ///cat.error("Error during releaseConnection", e);
          System.out.println("Error during releaseConnection " + e);
        }
    }
	  return prod;
	}
/**
	public static void main(String[] args) {
	    TurbineConfig tc = new TurbineConfig("com/bfg/props/",
						 "TurbineResources.properties");
	    tc.init();

	    try {
		Product p = 
		    findProduct("672320959");
		if (p != null) {
		    System.out.println("Good Test: Find Real Product");
		    System.out.println("Author string is: " + 
				       p.getAuthorString());
		    System.out.println("Price is " + p.getPrice()); 
		} else {
		    System.out.println("Failed Test: Find Real Product");
		}
	    } catch (Exception e) {
		System.out.println("Failed Test: Find Real Product");
		e.printStackTrace();
	    }
	    try {
		Product p = 
		    findProduct("notanisbn");
		if (p != null) {
		    System.out.println("Bad Test: Find Fake Product");
		} else {
		    System.out.println("Good Test: Find Fake Product");
		}
	    } catch (Exception e) {
		System.out.println("Failed Test: Find Fake Product");
		e.printStackTrace();
	    }
	}
*/


}	

