/*
 * TransactionHelper.java
 *
 * Created on 3. Juni 2006, 12:59
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package Sirius.server.localserver.object;

import Sirius.server.property.ServerProperties;
import Sirius.server.sql.DBConnection;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 *
 * @author schlob
 */
public class TransactionHelper
{
    
       
    protected Connection con;
    protected boolean workBegun;
    
    /** prohibit usage of standard constructor*/
    private TransactionHelper(){}
    
        /**
     * Creates a new instance of TransactionHelper
     */
    TransactionHelper(DBConnection dbcon,ServerProperties properties) throws Exception
    {
        this.con=createMetaJDBCConnection(dbcon,properties);

       
        workBegun=false;
    
    }
    
    
    public void setWorkBegun(boolean workBegun)
    {
        this.workBegun=workBegun;
    }
    
    public boolean getWorkBegun()
    {
        return workBegun;
    
    }
    
    public Connection getConnection()
    {
            return con;
    
    
    }
    
    
    
    void rollback(/*Connection con , Savepoint s*/) throws SQLException
    {
        
        
        
        if(workBegun)
        {con.rollback();
         // con.releaseSavepoint(s);
         con.setAutoCommit(true);
         
        }
        workBegun=false;
        
        
        
    }
    
   void beginWork(/*Connection con */) throws SQLException
    {
        if(!workBegun)
        {
            con.setAutoCommit(false);
            
            con.createStatement().execute("begin");
            workBegun=true;
        }
        
        
    }
    
    void commit(/*Connection con*/) throws SQLException
    {
        if(workBegun)
        {
            con.commit();
            con.setAutoCommit(true);
        }
        
        
    }
    
      /**
     * Erzeugt eine Verbindung zur DB \u00FCber MetaJDBCTreiber.
     */
    private Connection createMetaJDBCConnection(Sirius.server.sql.DBConnection dbCon,ServerProperties properties) throws ClassNotFoundException, SQLException
    {
        
//        String metaJDBCURL = "jdbc:cidsDB:system://";
//        String metaJDBCDriver = "Sirius.metajdbc.driver.CidsDriver";
//
//        Properties info = new Properties();
//
//        // Sirius.server.sql.DBConnection dbCon = dbServer.getActiveDBConnection();
//
//        info.put("jdbc_driver", dbCon.getDriver());
//        info.put("db_url", dbCon.getURL());
//        info.put("user", dbCon.getUser());
//        info.put("password", dbCon.getPassword());
//
//        info.put("auto_id", "true");
//
//        // ServerProperties properties = dbServer.getProperties();
//
//        //
//        String prop;
//
//        if ((prop = properties.getLog4jPropertyFile()) != null) {
//            info.put("log4j_prop_file", prop);
//        }
//
//        //if((prop = properties.getMetaJDBC_CacheType()) != null) xxxxxxxxxxxxxxx
//        info.put("cache_type", "none");
//
//        if ((prop = properties.getMetaJDBC_schema()) != null) {
//            info.put("schema", prop);
//        }
//
//        Class.forName(metaJDBCDriver);
//        return DriverManager.getConnection(metaJDBCURL, info);
        return dbCon.getConnection();
        //ACHTUNG
        //HELL
        //METAJDBC abgeschaltet

    }
}
