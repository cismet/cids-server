/*
 * AdressStringConverter.java
 *
 * Created on 11. Mai 2004, 13:31
 */

package de.cismet.cids.tools.tostring;
import Sirius.server.localserver.attribute.*;
/**
 *
 * @author  schlob
 */
public class BetriebStringConverter extends ToStringConverter implements java.io.Serializable
{
    
    /** Creates a new instance of AdressStringConverter */
    public BetriebStringConverter()
    {
        super();
    }
    
    
    public  String convert(Sirius.server.localserver.object.Object o)
    {
        String stringRepresentation="";
        
        ObjectAttribute[] attrs = o.getAttribs();
        
        for(int i = 0; i< attrs.length;i++)
        {
            
            if(attrs[i].getName().equalsIgnoreCase("name"))
                stringRepresentation+=( attrs[i].toString() + " ");
            
            else //surpress
            {
                //stringRepresentation+=( attrs[i].toString() + "?");
                //System.err.println("unerwartetes Attribut implements StringConverter");
            }
            
            
        }
        
        return stringRepresentation;
    }
    
}
