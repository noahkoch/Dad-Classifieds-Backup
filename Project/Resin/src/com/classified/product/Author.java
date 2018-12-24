package com.classified.product;

import java.util.Vector;
import java.util.HashMap;
///import org.apache.log4j.Category;

/**
 * Stores information about an author.<P>
 *
 * Copyright 2002, James M. Turner
 * No warranty is made as to the fitness of this code for any
 * particular purpose.  It is included for demonstration purposes
 * only.
 *
 * @author       James M. Turner
 */

public class Author{
  protected static HashMap authors = new HashMap();
  ///static Category cat = Category.getInstance(Author.class);

  protected Vector pBooks = new Vector();
  protected String pName;

  /**
   * Returns a vector of Product objects which contains the books
   * written by this author.
   *
   * @return The books this author has written.
   **/

  public Vector getBooks(){
    return pBooks;
  }

  /**
   * Returns the author's name as a String.
   *
   * @return The author's name
   **/

  public String getName(){
    return pName;
  }

  private void setName(String Name){
    pName = Name;
  }

  /**
   * Returns an (possibly cached) author using the name as a key.
   *
   * @return An author object
   **/

  public static Author findAuthor(String name){
    if (authors.get(name) != null){
      return ((Author) authors.get(name));
    }
    Author author = new Author();
    author.setName(name);
    authors.put(name, author);
    return author;
  }

}
