package com.classified.cart;

///package com.bfg.cart;

import java.util.Vector;
import java.util.HashMap;
import java.util.Iterator;
import java.text.NumberFormat;
///import org.apache.log4j.Category;
import com.classified.product.Product;
import java.util.ResourceBundle;
///import com.bfg.customer.Address;
import com.classified.customer.Address;

/**
 * Cart is used to hold the contents of a shopping cart during a customer's 
 * session.   The cart also knows how to calculate tax and shipping
 * on an order, and how to calculate totals.<P>
 *
 * Copyright 2002, James M. Turner
 * No warranty is made as to the fitness of this code for any
 * particular purpose.  It is included for demonstration purposes
 * only.
 *
 * @author James M. Turner
 *
 **/

public class Cart{

  /**
   * Holds the contents of an individual cart.
   *
   **/

  protected HashMap contents = new HashMap();

  ///static Category cat = Category.getInstance(Cart.class);

  private static ResourceBundle property_bundle =
	ResourceBundle.getBundle("com.classified.cart.Cart");

  /** 
   * Returns an <code>Iterator</code> which will generate each 
   * {@link CartItem} in the cart.
   *
   * @return An Iterator which iterates over objects of type CartItem
   *
   **/

  public Iterator getItems(){
    return contents.values().iterator();
  }

  /** 
   * Returns the CartItem for a given {@link com.bfg.product.Product Product} 
   * in the cart.
   *
   * @param item The product to check for in the cart.
   * @param return The CartItem that matches that product, or null
   * if the product is not in the cart.
   *
   **/
    
///  public CartItem getItem(Product item){
///    return (CartItem)contents.get(item);
///  }

  /**
   * Add an given quantity of a given {@link com.bfg.product.Product Product}
   * to the cart. If the product is already in the cart, increment
   * the quantity rather than create a new CartItem.
   *
   * @param item The Product to add
   * @param quantity How many of this Product to add
   *
   **/

  public void addItem(Product item, int quantity){
    if (contents.get(item) != null){
	    ((CartItem)contents.get(item)).addQuantity(quantity);
    }else{
	    CartItem citem = new CartItem();
	    citem.setQuantity(quantity);
	    citem.setProduct(item);
	    contents.put(item, citem);
    }

    // Make sure to rerun promotions to make sure that we catch
    // any new or no longer valid promotions based on the cart change.

    Promotion.runPromotions(this);
  }

  /**
   * Add an given quantity of a given {@link com.bfg.product.Product Product}
   * to the cart.  The CartItem will have the promotion item flag
   * set, and will overwrite rather than add to any item already in
   * the cart which references this product.
   *
   * @param item The Product to add
   * @param quantity How many of this Product to add
   *
   **/

  public void addPromotionItem(Product item, int quantity) {
    CartItem citem = new CartItem();
    citem.setQuantity(quantity);
    citem.setProduct(item);
    citem.setPromotionItem(true);
    contents.put(item, citem);
  }

  /** 
   * Remove a product entirely from the cart
   *
   * @param item The Product to remove
   *
   **/

///  public void removeItem(Product item){
///    contents.remove(item);
///    Promotion.runPromotions(this);
///  }

  /**
   * Remove all promotional items from the cart.
   *
   **/

  public void removePromotionItems(){
    Vector removals = new Vector();
    Iterator items = getItems();

  // We do this in two steps to avoid messing up the Iterator by
	// removing items from the Vector in the middle of the loop.

    while (items.hasNext()){
      CartItem item = (CartItem) items.next();
      if(item.isPromotionItem()){
        removals.add(item.getProduct());
      }
    }

    items = removals.iterator();
    while(items.hasNext()){
      Product item = (Product) items.next();
      contents.remove(item);
    }
  }

  /** 
   * Returns the number of distinct products in the cart.
   *
   * @return Number of different products in the cart.
   **/

  public int countItems() {
    return contents.size();
  }

  /** 
   * Returns the total cost of the cart after promotions but before
   * tax or shipping is calculated.
   *
   * @return The subtotal before shipping and tax
   **/

  public double getTotal(){
    double total = 0;
    Iterator it = contents.values().iterator();
    while (it.hasNext()){
	    total += ((CartItem)it.next()).getLineItemPrice();
    }
    return total;
  }

  /** 
   * Returns the total cost of the cart after promotions but before
   * tax or shipping is calculated, formatted as a currency string.
   *
   * @return The formatted subtotal before shipping and tax
   **/

  public String getTotalString(){
    NumberFormat nf = NumberFormat.getCurrencyInstance();
    return nf.format(getTotal());
  }

  /** 
   * Returns the calculated tax for the cart.  If the
   * <code>testTaxMode</code> property is set to "yes", the tax is
   * calculated based on a 5% rate for Massachusetts and %10 for all
   * other states. 
   *
   * If the test mode is not set, the yet-to-be-integrated tax
   * modules is called.
   *
   * @param addr The address of the customer.
   * @return The calculated tax for the cart.
   **/

  public double getTax(Address addr){
    if((property_bundle.getString("testTaxMode") != null) && 
	    (property_bundle.getString("testTaxMode").equals("yes"))) {
	    if ((addr.getState() != null) && (addr.getState().compareTo("MA") < 0)){
        return getTotal() * 0.05D;
	    }else{
        return getTotal() * 0.10D;
	    }
    }else{
	    ///cat.debug("No real tax code in place yet");
      System.out.println("No real tax code in place yet");
	    return 0D;
    }
  }
	

  /**
   * Returns a currency-formatted version of the {@link #getTax getTax}
   * method.
   *
   * @return The calculated tax for the cart as a currency string.
   **/

  public String getTaxString(Address addr){
    NumberFormat nf = NumberFormat.getCurrencyInstance();
    return nf.format(getTax(addr));
  }

  /** 
   * Returns the calculated shipping for the cart.  If the
   * <code>testShippingMode</code> property is set to "yes", the tax is
   * calculated based on a 1.25 per item rate for Massachusetts and
   * 2.75 per item rate for all other states. 
   *
   * If the test mode is not set, the yet-to-be-integrated shipping
   * modules is called.
   *
   * @param addr The address of the customer.
   * @return The calculated shipping for the cart.
   **/

  public double getShipping(Address addr){
	if ((property_bundle.getString("testShippingMode") != null) && 
	    (property_bundle.getString("testShippingMode").equals("yes"))){
	    if ((addr.getState() != null) && (addr.getState().compareTo("MA") < 0)){
        return countItems() * 1.25D;
	    }else{
        return countItems() * 2.75D;
	    }
    }else{
      ///cat.debug("No real shipping code in place yet");
      System.out.println("No real shipping code in place yet");
      return 0D;
    }
  }
	
  /**
   * Returns a currency-formatted version of the 
   * {@link #getShipping getShipping} method.
   *
   * @return The calculated shipping for the cart as a currency string.
   **/

  public String getShippingString(Address addr){
    NumberFormat nf = NumberFormat.getCurrencyInstance();
    return nf.format(getShipping(addr));
  }

  /** 
   * Returns the subtotal + tax and shipping for the cart.
   *
   * @return The grand total cost of the cart.
   **/

  public double getGrandTotal(Address addr){
    return getTotal() + getShipping(addr) + getTax(addr);
  }

  /**
   * Returns a currency-formatted version of the 
   * {@link #getGrandTotal getGrandTotal} method.
   *
   * @return The grand total cost for the cart as a currency string.
   **/

  public String getGrandTotalString(Address addr){
    NumberFormat nf = NumberFormat.getCurrencyInstance();
    return nf.format(getGrandTotal(addr));
  }

}
