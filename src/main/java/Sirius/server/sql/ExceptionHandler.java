/*
 * ExceptionHandler.java
 *
 * Created on 13. November 2003, 20:16
 */

package Sirius.server.sql;
import java.sql.*;

/**
 *
 * @author  schlob
 */
public abstract class ExceptionHandler
{
    private static transient final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(ExceptionHandler.class);
    
    public static Throwable handle(Throwable t)
    {
        String message="";
        
        if (t instanceof SQLException)
        {
            SQLException e = (SQLException)t;
            
            do
            {
                
                message+=(e.toString());
                message+=("\nSQL-State: " + e.getSQLState());
                message+=("\nError-Code :" + e.getErrorCode());
                
            }while(e.getNextException()!=null);
            
            
        }
        
        logger.error(message,t);
        
        t.printStackTrace();
        
        
        return t;
    }
    
    
    
}
