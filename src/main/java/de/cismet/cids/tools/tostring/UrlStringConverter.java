/*
 * UrlStringConverter.java
 *
 * Created on 11. Mai 2004, 13:45
 */

package de.cismet.cids.tools.tostring;
import Sirius.server.localserver.attribute.*;
/**
 *
 * @author  schlob
 */
public class UrlStringConverter extends ToStringConverter implements java.io.Serializable
{
    private final transient org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(this.getClass());
    
    /** Creates a new instance of UrlStringConverter */
    public UrlStringConverter()
    {
        super();
    }
    
    public  String convert(Sirius.server.localserver.object.Object o)
    {
        String stringRepresentation="";
        
        ObjectAttribute[] attrs = o.getAttribs();
        
        for(int i = 0; i< attrs.length;i++)
        {
            
            if(attrs[i].getName().equalsIgnoreCase("url_base_id") || attrs[i].getName().equalsIgnoreCase("object_name") )
                stringRepresentation+=( attrs[i].toString());
            
            else //surpress
            {
                //stringRepresentation+=( attrs[i].toString() + "?");
                logger.debug("unerwartetes Attribut im StringConverter");
            }
            
            
        }
        
        return stringRepresentation;
    }
    
}
