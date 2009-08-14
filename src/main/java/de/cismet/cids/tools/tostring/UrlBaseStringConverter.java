/*
 * UrlBaseStringConverter.java
 *
 * Created on 11. Mai 2004, 13:45
 */

package de.cismet.cids.tools.tostring;
import Sirius.server.localserver.attribute.*;
/**
 *
 * @author  schlob
 */
public class UrlBaseStringConverter extends ToStringConverter implements java.io.Serializable
{
    
    /** Creates a new instance of UrlBaseStringConverter */
    public UrlBaseStringConverter()
    {
        super();
    }
    
    public  String convert(Sirius.server.localserver.object.Object o)
    {
        String stringRepresentation="";
        
        ObjectAttribute[] attrs = o.getAttribs();
        
        for(int i = 0; i< attrs.length;i++)
        {
            
            if(attrs[i].getName().equalsIgnoreCase("prot_prefix") || attrs[i].getName().equalsIgnoreCase("server")|| attrs[i].getName().equalsIgnoreCase("path") )
                stringRepresentation+=(attrs[i].toString());
            
            else //surpress
            {
                //stringRepresentation+=( attrs[i].toString() + "?");
                //System.err.println("unerwartetes Attribut implements StringConverter");
            }
            
            
        }
        
        return stringRepresentation;
    }
    
}
