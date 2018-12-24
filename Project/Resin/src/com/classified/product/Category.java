package com.classified.product;

import java.util.Vector;
import java.util.Set;
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
import java.util.Properties;
import java.text.*;
import java.util.ResourceBundle;
///import org.apache.log4j.Category;
import javax.naming.NamingException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingEnumeration;
import javax.naming.directory.InitialDirContext;
import java.util.ResourceBundle;

/**
 * Implements categories, which are collections of products.
 *
 * Copyright 2002, James M. Turner
 * No warranty is made as to the fitness of this code for any
 * particular purpose.  It is included for demonstration purposes
 * only.
 *
 * @author       James M. Turner
 */

public class Category{
  private static ResourceBundle sql_bundle =
    ResourceBundle.getBundle("com.classified.product.SQLQueries");

  ///static org.apache.log4j.Category lcat = org.apache.log4j.Category.getInstance(Category.class);

  protected int pID;
  protected Product pFeaturedProduct;
  protected Vector pProducts = new Vector();
  protected String pName;
  protected static HashMap categories = new HashMap();

  /**
   * Returns the unique category ID
   *
   * @return the category ID
   **/

  public int getID(){
    return pID;
  }

  private void setID(int ID){
    pID = ID;
  }

  /**
   * Returns the category name
   *
   * @return the category name
   **/

  public String getName(){
    return pName;
  }

  private void setName(String Name){
    pName = Name;
  }

  /**
   * Returns the product which should be featured in a display page for this 
   * category.
   *
   * @return The featured product
   **/

  public Product getFeaturedProduct(){
    return pFeaturedProduct;
  }

  private void setFeaturedProduct(Product Product){
    pFeaturedProduct = Product;
  }

  /**
   * Returns a vector containing all the products in this category.
   *
   * @return The products in a Vector
   **/

  public Vector getProducts(){
    return pProducts;
  }

  private void setProducts(Vector Products){
    pProducts = Products;
  }

  /**
   * Returns the categories as a Set
   *
   * @return The categories
   **/

  public static Set getCategories(){
    return categories.keySet();
  }

  /**
   * Returns a category, keyed by the category ID, or null if not found
   *
   * @param Id The ID of the category as an int
   * @return The category
   * @throws ProductActivityException Thrown on database errors
   **/

  public static Category findCategoryById(int Id) throws ProductActivityException{
    loadAllCategories();
    Iterator it = getCategories().iterator();

    while (it.hasNext()){
	    Category c = findCategory((String)it.next());
	    if (c.getID() == Id) return c;
    }
    return null;
  }

  /**
   * Returns a category, keyed by the category name, or null if not found
   *
   * @param Name The name of the category as a string
   * @return The category
   * @throws ProductActivityException Thrown on database errors
   **/

  public static Category findCategory(String Name) throws ProductActivityException{
    if (categories.get(Name) != null){
      return (Category) categories.get(Name);
	  }
	  ///DBConnection dbConn = null;
	  Category cat = null;
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
		///    dbConn = TurbineDB.getConnection();
		///    if (dbConn == null){
    ///      lcat.error("Can't get database connection");
    ///      throw new ProductActivityException();
		 ///   }
		    PreparedStatement pstmt =
          dbConnection.prepareStatement(sql_bundle.getString("findCatQuery"));
		    pstmt.setString(1, Name);
		    ResultSet rs = pstmt.executeQuery();
		    if (rs.next()){
          cat = new Category();
          cat.setName(rs.getString("CATEGORY_NAME"));
          cat.setID(rs.getInt("CATEGORY_ID"));
          String feat = rs.getString("FEATURED_PRODUCT");
          if(!rs.wasNull()){
            cat.setFeaturedProduct(Product.findProduct(feat));
          }
		    }
		    rs.close();
		    pstmt.close();
		    if(cat != null){
          pstmt = dbConnection.prepareStatement(sql_bundle.getString("catProdQuery"));
          pstmt.setInt(1, cat.getID());
          rs = pstmt.executeQuery();
          while (rs.next()){
            cat.getProducts().add(Product.findProduct(rs.getString("PRODUCT_ISBN")));
          }
          rs.close();
          pstmt.close();
          categories.put(Name, cat);
		    }   
		}catch (Exception e){
		    ///lcat.error("Error during findCategory", e);
        System.out.println("Error during findCategory " + e);
		    e.printStackTrace();
		    throw new ProductActivityException();
		}finally{
      try{
        dbConnection.close();
			  ///TurbineDB.releaseConnection(dbConn);
			}catch(Exception e){
			    ///lcat.error("Error during releaseConnection", e);
          System.out.println("Error during releaseConnection " + e);
			}
		}
	  return cat;
	}

  static boolean allLoaded = false;

  public static void loadAllCategories()throws ProductActivityException{
    if (allLoaded) return;
    allLoaded = true;
    ///DBConnection dbConn = null;
    
    Category cat = null;
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
   /// try{
   ///   dbConn = TurbineDB.getConnection();
   ///   if (dbConn == null){
		///    lcat.error("Can't get database connection");
		///    throw new ProductActivityException();
    ///  }
      PreparedStatement pstmt =
		    dbConnection.prepareStatement(sql_bundle.getString("findAllCats"));
      ResultSet rs = pstmt.executeQuery();
      while (rs.next()){
        findCategory(rs.getString("CATEGORY_NAME"));
      }
      rs.close();
      pstmt.close();
	  }catch (Exception e){
      ///lcat.error("Error during loadAllCategories", e);
      System.out.println("Error during loadAllCategories " + e);
		  e.printStackTrace();
		  throw new ProductActivityException();
		}finally{
      try{
        ///TurbineDB.releaseConnection(dbConn);
        dbConnection.close();
			}catch (Exception e){
        ///lcat.error("Error during releaseConnection", e);
        System.out.println("Error during releaseConnection " + e);
			}
		}
	}

  /**
  public static void main(String[] args){
    TurbineConfig tc = new TurbineConfig("com/bfg/props/",
      "TurbineResources.properties");
	    tc.init();

	    try{
        Category.loadAllCategories();
        Category c = findCategory("JDBC");
        if (c != null) {
          System.out.println("Good Test: Find Real Category");
        }else{
		    System.out.println("Failed Test: Find Real Category");
      }
	  }catch(Exception e){
      System.out.println("Failed Test: Find Real Category");
      e.printStackTrace();
	  }
	  try{
      Category c =  findCategory("notancat");
      if (c != null){
		    System.out.println("Bad Test: Find Fake Category");
      }else{
		    System.out.println("Good Test: Find Fake Category");
      }
	  }catch(Exception e){
      System.out.println("Failed Test: Find Fake Category");
      e.printStackTrace();
	  }
	}
  */
}	

