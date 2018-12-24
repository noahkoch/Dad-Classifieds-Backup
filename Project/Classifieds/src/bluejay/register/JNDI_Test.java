package bluejay.register;

import javax.naming.*;
import javax.sql.*;
import java.sql.*;

public class JNDI_Test {

  String classifiedAd = "Not Connected";
  String color = "What Color?";
     
  public void init() {
    try{
      Context ctx = new InitialContext();
      if(ctx == null ) 
          throw new Exception("Boom - No Context");

      DataSource ds = 
            (DataSource)ctx.lookup(
               "java:comp/env/jdbc/ClassifiedsDB");

      if (ds != null) {
        Connection conn = ds.getConnection();
              
        if(conn != null)  {
            classifiedAd = "Got Connection "+conn.toString();
            Statement stmt = conn.createStatement();
            ResultSet rst = 
                stmt.executeQuery(
                      "select UniqueDateId, ClassifiedAd, Color, Banner," +
                      "WhatAdSays01, Phone, Email, WebSite from AdInfo");
            if(rst.next()) {
               classifiedAd=rst.getString(5);
               color=rst.getString(3);
            }
            conn.close();
        }
      }
    }catch(Exception e) {
      e.printStackTrace();
    }
 }

 public String getClassifiedAd() { return classifiedAd; }
 public String getColor() { return color;}
}

