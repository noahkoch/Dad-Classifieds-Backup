package com.classified.cart;

import java.text.NumberFormat;
///import org.apache.log4j.Category;
import com.classified.product.Product;

/**
 * CartItem is used to hold the product and quantity of a specific
 * item in the cart.<P>
 *
 * Copyright 2002, James M. Turner
 * No warranty is made as to the fitness of this code for any
 * particular purpose.  It is included for demonstration purposes
 * only.
 *
 * @author James M. Turner
 *
 **/

public class CartItem{

  private Product pProduct;
  private int pQuantity;
  private boolean isPromotionItem;

  ///static Category cat = Category.getInstance(CartItem.class);

  /**
   * Returns which Product has been placed in the cart.
   *
   * @return The product for this CartItem.
   **/

  public Product getProduct(){
    return pProduct;
  }

  /**
   * Sets the Product for a CartItem
   *
   * @param product The product for this CartItem.
   **/

  public void setProduct(Product product){
    pProduct = product;
  }

  /**
   * Returns the quantity of an item that has been placed in the cart.
   *
   * @return The quantity for this CartItem.
   **/

  public int getQuantity(){
    return pQuantity;
  }

  /**
   * Sets the quantity for a CartItem
   *
   * @param quantity The quantity for this CartItem.
   **/

  public void setQuantity(int quantity){
    pQuantity = quantity;
  }

  /**
   * Adds to the quantity for a CartItem
   *
   * @param quantity The quantity to add to this CartItem.
   **/

  public void addQuantity(int quantity){
    pQuantity += quantity;
  }

  /**
   * Determine if this is a free promotional item.
   *
   * @return <code>true</code> if this item is free
   **/

  public boolean isPromotionItem(){
    return isPromotionItem;
  }

  /**
   * Sets if this is a free promotional item.
   *
   * @param isPromo <code>true</code> if this item is free
   **/

  public void setPromotionItem(boolean isPromo){
    isPromotionItem = isPromo;
  }


  /**
   * Get the price times the quantity for this item.  If it is a
   * promotional item, return zero.
   *
   * @return the quantity times the price, or zero for promotional
   * items.
   **/

  public double getLineItemPrice(){
    if (isPromotionItem()) return 0D;
      return getQuantity() * getProduct().getPrice();
  }

  /**
   * Returns a currency formatted version of the line item price, if
   * the item is promotional, return the string "FREE!"
   *
   * @return A currency-formatted version of the line item price or "FREE!"
   **/

  public String getLineItemPriceString(){
    if(isPromotionItem()) 
      return "FREE!";
    NumberFormat nf = NumberFormat.getCurrencyInstance();
    return nf.format(getLineItemPrice());
  }
}
