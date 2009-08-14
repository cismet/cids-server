/*
 * StrassStringConverter.java
 *
 * Created on 11. Mai 2004, 13:44
 */

package de.cismet.cids.tools.tostring;
import Sirius.server.localserver.attribute.*;
import Sirius.server.middleware.types.MetaObject;
import java.util.Collection;
/**
 *
 * @author  schlob
 */
public class StrassStringConverter extends ToStringConverter implements java.io.Serializable
{
      private static final transient org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(GeometryStringConverter.class);
    
    
    /** Creates a new instance of StrassStringConverter */
    public StrassStringConverter()
    {
        super();
    }
    
    public  String convert(de.cismet.cids.tools.tostring.StringConvertable o)
    {
        MetaObject mo = ( MetaObject)o;
        
        String stringRepresentation="";
        
        Collection<Attribute> attrs = mo.getAttributeByName("NAME",1);
        
       
      
        
       if(!attrs.isEmpty())
        {
            Attribute attr = attrs.iterator().next();
          
            
                stringRepresentation+=(attr.toString() + " ");
            
                      
            
        }
        
        return stringRepresentation;
       
    }
}   
