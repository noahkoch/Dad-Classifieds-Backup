package com.java_internals.resourcefactory;

import java.util.Hashtable; 
import javax.naming.Context; 
import javax.naming.Name; 
import javax.naming.Reference; 
import javax.naming.spi.ObjectFactory;
import com.mysql.jdbc.jdbc2.optional.MysqlConnectionPoolDataSource; 

public class MyResourceFactory implements ObjectFactory { 
    protected final String poolDataSourceName = "com.mysql.jdbc.jdbc2.optional.MysqlConnectionPoolDataSource";
    public Object getObjectInstance ( Object refObj, Name nm, Context ctx, Hashtable env ) throws Exception { 
      Reference ref = (Reference) refObj; 
      MysqlConnectionPoolDataSource datasource = null; 
        try { 
              datasource = (MysqlConnectionPoolDataSource)Class.forName(poolDataSourceName).newInstance(); 
              int portNumber = 3306; 
                if ( ref.get("port") != null ) { 
                  try { portNumber = Integer.parseInt ( 
                    (String)ref.get ("port").getContent() ); 
                  } catch ( NumberFormatException oops ) { 
                      oops.printStackTrace(); 
                    } 
                  } 
                  datasource.setPort ( portNumber ); 
                if ( ref.get( "user") != null ) {
                  datasource.setUser ( (String)ref.get ( "user" ).getContent() );
                  } 
                if ( ref.get ( "password" ) != null ){ 
                  datasource.setPassword ( (String)ref.get ( "password" ).getContent() ); 
                } 
                if ( ref.get ( "serverName" ) != null ) { 
                  datasource.setServerName ( (String)ref.get ( "serverName" ).getContent() );
                } 
                if ( ref.get ( "databaseName" ) != null ) {
                  datasource.setDatabaseName ( (String)ref.get ( "databaseName" ).getContent() ); 
                } 
        } catch (Exception ex) { 
            throw new RuntimeException("Unable to create DataSource of class '" + 
            poolDataSourceName + "', reason: " + ex.toString()); 
        } 
        return datasource; 
    } 
} 