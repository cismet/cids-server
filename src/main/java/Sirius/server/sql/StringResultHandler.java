/*
 * StringResultHandler.java
 *
 * Created on 9. Februar 2004, 13:58
 */

package Sirius.server.sql;
import java.sql.*;
import java.util.*;
import Sirius.server.search.*;


/**
 *
 * @author  schlob
 */
public class StringResultHandler extends DefaultResultHandler {
    
    /** Creates a new instance of StringResultHandler */
    public StringResultHandler() {
        super();
    }
    
    
      public Object handle(ResultSet rs, Query q) throws SQLException, Exception
        {
   

            Vector handledResult = new Vector(100,100);

             // konstruktorparameter  

            int length =rs.getMetaData().getColumnCount();
             //rs.beforeFirst();
             
            if(length == 1)
            {
             while(rs.next())
                {
                      handledResult.add(rs.getString(1));

                }
            
            }
            else
            {

           
                while(rs.next())
                {
                     String[] values = new String[length];

                for(int i =0; i <values.length;i++)
                    values[i]=rs.getString(i+1);

                handledResult.add(values);

                }
            }

           handledResult.trimToSize();




        return handledResult;

        }
    
}
