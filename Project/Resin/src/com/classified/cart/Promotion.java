package com.classified.cart;

import java.util.Vector;
import java.util.HashMap;
import java.util.Iterator;
import java.text.NumberFormat;
import com.classified.product.Product;
import com.classified.product.Category;
///import org.apache.turbine.services.db.TurbineDB;
///import org.apache.turbine.util.db.pool.DBConnection;
///import org.apache.turbine.util.TurbineConfig;
import com.classified.exceptions.ProductActivityException;
import com.classified.exceptions.CustomerActivityException;
import java.sql.*;
import javax.sql.*;
import javax.naming.NamingException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingEnumeration;
import javax.naming.directory.InitialDirContext;
import java.util.ResourceBundle;

/**
 * Promotion is used to implement the Gift With Purchase promotion
 * used by the Books for Geeks Web Site.  It looks for items in the
 * cart which match the target condition, either by specific product
 * or by category.  If the target condition is met, a gift is placed
 * in the cart with the promotionItem flag set, which causes it not to
 * be charged for.<P>
 *
 * Copyright 2002, James M. Turner
 * No warranty is made as to the fitness of this code for any
 * particular purpose.  It is included for demonstration purposes
 * only.
 *
 * @author       James M. Turner
 */

public class Promotion{
  private static ResourceBundle sql_bundle =
      ResourceBundle.getBundle("com.bfg.cart.SQLQueries");

  ///static org.apache.log4j.Category cat = org.apache.log4j.Category.getInstance(Promotion.class);
  static Vector promotions = new Vector();
  static boolean loaded = false;

  protected String promoName;
  protected Product targetItem;
  protected Category targetCategory;
  protected Product giftItem;
  protected int quantityRequired;
  protected boolean categoryPromotion = false;

  /** 
   * Returns the human-readable name of the promotion
   *
   * @return           The name of the promotion.
   */

  public String getPromoName(){
    return promoName;
  }

  /**
   * Returns true if this promotion applies to a category rather
   * than an individual product.
   *
   * @return           <code>true</code> if the promotion is applied
   *                   to a category, <code>false</code> if it is
   *                   applied to a single product.
   */

  public boolean isCategoryPromotion(){
    return categoryPromotion;
  }

  /**
   * Returns the product which must be purchased in order to receive
   * the free gift. Only used by promotions which do not have
   * <code>isCategoryPromotion</code> set to true.
   *
   * @return           The product which satisfies the requirement
   *                   for this promotion.
   */

  public Product getTargetItem(){
    return targetItem;
  }

  /**
   * Returns the category which must be purchased in order to receive
   * the free gift. Only used by promotions which have
   * <code>isCategoryPromotion</code> set to true.
   *
   * @return           The category which satisfies the requirement
   *                   for this promotion.
   */

  public Category getTargetCategory(){
    return targetCategory;
  }

  /**
   * Returns the product which is received if the conditions for the
   * promotion are met.
   *
   * @return           The product which will be received
   *                   as a gift for this promotion.
   */

  public Product getGiftItem(){
    return giftItem;
  }

  /**
   * Returns the number of the target item that must be purchased in
   * order to receive the gift.  Note that for category promotions,
   * this is the number in aggregate from all items that are in the category.
   *
   * @return           The number of target items required to
   *                   receive the gift.
   */

  public int getQuantityRequired(){
    return quantityRequired;
  }


  /**
   * Returns the list of promotions currently available.  If the
   * promotion list has previously been loaded from the database, it
   * returns a cached copy, otherwise it calls
   * <code>loadPromotions</code> to get the promotions from the
   * database and stores them to the cache.
   *
   * @return           The promotions to be used on the site.
   */

  public static Vector getPromotions(){
    if(loaded){
      return promotions;
    }else{
	    loadPromotions();
	    loaded = true;
	    return promotions;
    }
  }

  /**
   * Connects to the database and retreives the list of
   * promotions. If a promotion is no longer valid because the
   * product or category it references no longer exists, the
   * promotion is ignored.
   *
   */

  public static void loadPromotions(){
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
    ///  if (dbConn == null){
		///    cat.error("Can't get database connection");
		///    return;
    ///  }
      PreparedStatement pstmt =
		    dbConnection.prepareStatement(sql_bundle.getString("loadPromotions"));
      ResultSet rs = pstmt.executeQuery();
      while(rs.next()){
        Promotion promo = new Promotion();
		    promo.giftItem = Product.findProduct(rs.getString("GIFT_ID"));
		    promo.promoName = rs.getString("PROMO_NAME");
		    promo.categoryPromotion =	(rs.getString("PROD_OR_CAT").equals("C"));
		    if (promo.categoryPromotion){
          promo.targetCategory = 
            Category.findCategoryById(rs.getInt("CATEGORY_ID"));
        if(promo.targetCategory == null) 
          continue;
		    }else{
              promo.targetItem = 
                  Product.findProduct(rs.getString("PRODUCT_ISBN"));
              if(promo.targetItem == null) continue;
		    }
		    promo.quantityRequired = rs.getInt("DISCOUNT_QUANT");
		    promotions.add(promo);
      }
      rs.close();
      pstmt.close();
	  }catch (Exception e){
      ///cat.error("Error during loadPromotions", e);
      System.out.println("Error during loadPromotions " + e);
	  }finally{
      try{
        ///TurbineDB.releaseConnection(dbConn);
        dbConnection.close();
		  }catch (Exception e){
        ///cat.error("Error during releaseConnection", e);
        System.out.println("Error during releaseConnection " + e);
		  }
	  }
  }

  /**
   * Run all of the current promotions over the shopping cart.
   * Before running the promotions, it removes any existing
   * promotional items from the cart so that each run is a fresh one.
   *
   * @param cart     The shopping cart to calculate promotions for.
   */

  public static void runPromotions(Cart cart){
    cart.removePromotionItems();
    Iterator promos = getPromotions().iterator();

    while (promos.hasNext()){
      Promotion promo = (Promotion) promos.next();
	    promo.runPromotion(cart);
    }
  }

  /**
   * Run this promotion over the shopping cart.  If the condition is
   * met, add a promotional item to the cart.
   *
   * @param cart     The shopping cart to calculate promotions for.
   */

  public void runPromotion(Cart cart){
    Iterator items = cart.getItems();
    int quant = 0;
    while (items.hasNext()){
	    CartItem item = (CartItem) items.next();
	    if (isCategoryPromotion()){
        if (targetCategory.getProducts().contains(item.getProduct())){
          quant += item.getQuantity();
        }
	    }else{
        if(item.getProduct().equals(targetItem)){
          quant += item.getQuantity();
        }
	    }
    }
    if (quant >= quantityRequired){
      cart.addPromotionItem(giftItem, 1);
    }
  }
}
	    

