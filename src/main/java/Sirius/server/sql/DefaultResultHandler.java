/*
 * DefaultResultHandler.java
 *
 * Created on 26. September 2003, 15:04
 */

package Sirius.server.sql;
import java.sql.*;
import java.util.*;
import Sirius.server.search.*;

/**
 *
 * @author  schlob
 */
public class DefaultResultHandler implements ResultHandler
{
    private final transient org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(this.getClass());
    
    
    /** Creates a new instance of DefaultResultHandler */
    
    
    public Object handle(ResultSet rs, Query q) throws SQLException, Exception
    {
        //        java.lang.Class resultType =q.getResultType();
        
        
        
        Vector handledResult = new Vector(100,100);
        
        // konstruktorparameter
        
        int length =rs.getMetaData().getColumnCount();
        
        // rs.beforeFirst();
        while(rs.next())
        {
            Object[] values = new Object[length];
            
            for(int i =0; i <values.length;i++)
                values[i]=rs.getObject(i+1);
            
            handledResult.add(values);
            
        }
        
        
        handledResult.trimToSize();
        
       logger.debug("suchergebnis"+handledResult);
        
        //       if(!(resultType.newInstance() instanceof Createable))
        //        {
        //             return handledResult;
        //
        //        }
        //        else //result is Createable
        //        {
        //                Createable c = (Createable )resultType.newInstance();
        //
        //                Vector tmp = new Vector(handledResult.size());
        //
        //                for(int i =0;i < handledResult.size();i++)
        //                    tmp.add(c.newInstance((Object[])handledResult.get(i)));
        //                //xxx vielleicht in die Schleife oben mit reinziehen
        //
        //                return tmp;
        //
        //
        //        }
        
        // auskommentieren wenn obiges wieder aktuell
        return handledResult;
        
    }
    
    
    
    private String toString(Vector v)
    {
        
        StringBuffer buf = new StringBuffer();
        buf.append("(");
        
        Iterator i = v.iterator();
        boolean hasNext = i.hasNext();
        while (hasNext)
        {
            Object o = i.next();
            buf.append( String.valueOf(o));
            hasNext = i.hasNext();
            if (hasNext)
                buf.append(",");
        }
        
        buf.append(")");
        return buf.toString();
        
        
        
    }
    
    
}
