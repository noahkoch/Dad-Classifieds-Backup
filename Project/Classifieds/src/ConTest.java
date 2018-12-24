/**
 * ConTest.java
 *
 * This is a simple program to test if you have a JDBC connection to your.
 * mySQL database. Do this test only after you set your classpath variable
 * for the JDBC connection. In windows 9x use the "sysedit" command to change 
 * the autoexecute.bat file. I set mine as follows:
 * SET CLASSPATH=.;C:\mySQlDriver\mm.mysql-2.0.4;
 *
 * If nothing comes back after typing "java ConTest" it works!
 *
 * If you get the following message, you Do Not have it set up right:
 *
 * SQLException: Cannot connect to MySQL server on localhos:3306. Is there a MySQL
 * server running on the machine/port you are trying to connect to? (java.net.Unkn
 * wnHostException)
 * SQLState:     08S01
 * VendorError:  0
 */


import java.sql.*;

public class ConTest {
    

  public static void main(String[] args) 
                    throws SQLException, ClassNotFoundException {

    // String dbUrl = "jdbc:mysql://localhost:3306/test";  // 1.1.1.33
    // String dbUrl = "jdbc:mysql://localhost:3306/TestDB?user=tkoc&password=trustno1";
    ///String dbUrl = "jdbc:mysql://localhost:3306/TestDB";
    String dbUrl = "jdbc:mysql://localhost:3306/javatest";

    String user = "tkoc";
    String password = "trustno1";
    Connection connect = null;
    Statement st = null;
    ResultSet rs = null;
    
    try{
        // Load the driver (registers itself)
        // The newInstance() call is a work around for some
        // broken Java implementations
        // Class.forName("org.gjt.mm.mysql.Driver").newInstance();
        Class.forName("com.mysql.jdbc.Driver").newInstance();
    }
    catch(Exception E) {
        System.err.println("Unable to load driver.");
        E.printStackTrace();
    }
 

    try {
        connect = DriverManager.getConnection(dbUrl, user, password);
        st = connect.createStatement();
        rs = st.executeQuery("select * from testdata");

        while(rs.next())
        {
          System.out.println(rs.getString("id"));
          System.out.println(rs.getString("foo"));
          System.out.println(rs.getString("bar"));                    
        }
        
    }catch (SQLException E) {
        System.out.println("SQLException: " + E.getMessage());
        System.out.println("SQLState:     " + E.getSQLState());
        System.out.println("VendorError:  " + E.getErrorCode());
    }


  }
}
