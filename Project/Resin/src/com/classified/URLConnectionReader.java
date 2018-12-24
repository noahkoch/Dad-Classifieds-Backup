/**
 * File:  URLConnectionReader.java
 * Original Source Code located at:
 * http://java.sun.com/docs/books/tutorial/networking/urls/readingWriting.html
 * Purpose: This code is used to execute the Look4MatchServlet.java 
 * It is called (in windows) from the Look4U.cmd file which gets called
 * using the AT command or from the Task Scheduler.
 * 
 * Note: Here is how you set it up in Scheduled Tasks (under Accessories ->
 * System Tools menu item). Look4U.cmd file and the URLConnectionReader.class
 * are located under WEB-INF\classified\classes directory.
 * For example you may need the full path to Look4U.cmd
 * e.g. C:\resin-ee-2.1.6\webapps\classified\WEB-INF\classes>Look4U
 * 
 * In the Task Schedular, Run: has the path
 * C:\resin-ee-2.1.6\webapps\classified\WEB-INF\classes\Look4U.cmd
 * Start in: has the path:
 * C:\resin-ee-2.1.6\webapps\classified\WEB-INF\classes
 * 
 * @author blueads.com, Tom Kochanowicz 03/31/00
 * @version 0.2 03/04/03
 */

// NOTE: NO PACKAGE

import java.net.*;
import java.io.*;

public class URLConnectionReader{

  /**
   * This is the absolute path to the look4Match servlet.
   * Just change the http path to get it to work with a
   * different configuration.
   */
  static final String look4matchServlet = 
            "http://192.168.1.102/classified/look4Match";


    public static void main(String[] args) throws Exception {

        URL servletURLpath = new URL(look4matchServlet);
        
        URLConnection ba = servletURLpath.openConnection();
        BufferedReader in = new BufferedReader(
                                new InputStreamReader(
                                ba.getInputStream()));
        String inputLine;

        while ((inputLine = in.readLine()) != null) 
            System.out.println(inputLine);
        in.close();
    }
}
