/*
 * AdressStringConverter.java
 *
 * Created on 11. Mai 2004, 13:31
 *test
 */

package de.cismet.cids.tools.tostring;
import Sirius.server.localserver.attribute.*;
import Sirius.server.middleware.types.MetaObject;
/**
 *
 * @author  schlob
 */
public class AdressStringConverter extends ToStringConverter implements java.io.Serializable
{
     private transient final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(this.getClass());
  
    /** Creates a new instance of AdressStringConverter */
    public AdressStringConverter()
    {
        super();
    }
    

    public  String convert(de.cismet.cids.tools.tostring.StringConvertable o)
    {
        MetaObject mo = ( MetaObject)o;
     
        String stringRepresentation="";
        
        ObjectAttribute[] attrs = mo.getAttribs();
        
        for(int i = 0; i< attrs.length;i++)
        {
            // besser getAttributeByname
            if(attrs[i].getName().equalsIgnoreCase("strasse") || attrs[i].getName().equalsIgnoreCase("hausnummer") )
                stringRepresentation+=( attrs[i].toString() + " ");
            
//            else //surpress
//            {
//                stringRepresentation+=( attrs[i].toString() + "?");
//              logger.error("unerwartetes Attribut");
//            }
            
            
        }
        
        return stringRepresentation;
    }
    
}
