/*
 * ResultHandler.java
 *
 * Created on 26. September 2003, 14:57
 */

package Sirius.server.sql;

import java.sql.*;
import java.util.*;
import Sirius.server.search.*;
/**
 *
 * @author  schlob
 */
public interface ResultHandler 
{
  
   
    
    public Object handle(ResultSet rs,Query q) throws SQLException,Exception;
    
   
  
    
}
