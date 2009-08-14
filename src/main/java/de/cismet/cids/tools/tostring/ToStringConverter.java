/*
 * ToString.java
 *
 * Created on 10. Mai 2004, 16:02
 */

package de.cismet.cids.tools.tostring;

/**
 *
 * @author  schlob
 */


import Sirius.server.localserver.attribute.*;
import java.util.*;
import Sirius.server.middleware.types.*;

public class ToStringConverter implements java.io.Serializable
{
       private transient org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(this.getClass());
    
    
    
    
    public  String convert(Sirius.server.localserver.object.Object o,HashMap classes)
    {
        String stringRepresentation="";
        
        //ObjectAttribute[] attrs = o.getAttribs();
        Collection names = o.getAttributeByName("name",1);
        Iterator iter = names.iterator();
        
        if(iter.hasNext())
            stringRepresentation+=((ObjectAttribute)iter.next()).getValue();
        else
            stringRepresentation+=o.getKey().toString();
        
//
//        for(int i = 0; i< attrs.length;i++)
//        {
//            if(!attrs[i].referencesObject())
//                stringRepresentation+=(attrs[i].toString()+ " ");
//            else
//                stringRepresentation+= ( ( (MetaObject)attrs[i].getValue()).toString(classes) + " " );
//
//        }
//
        return stringRepresentation;
    }
    
    
    
    
    public  String convert(de.cismet.cids.tools.tostring.StringConvertable o)
    {
        setLogger();
        
        if (logger!=null)logger.debug("convert von ToStringconverter gerufen");
        
        String stringRepresentation="";
        
        if(o instanceof Sirius.server.localserver.object.Object)
        {
            Collection names = ((Sirius.server.localserver.object.Object)o).getAttributeByName("name",1);
            Iterator iter = names.iterator();
            
            if(iter.hasNext())
                stringRepresentation+=((ObjectAttribute)iter.next()).getValue();
            else 
                stringRepresentation += "";
//            ObjectAttribute[] attrs = ((Sirius.server.localserver.object.Object)o).getAttribs();
//
//            for(int i = 0; i< attrs.length;i++)
//            {
//
//                stringRepresentation+=( attrs[i].toString() + " ");
//
//
//            }
            
            
        }
        else if(o instanceof Sirius.server.localserver.attribute.ObjectAttribute)
        {
          if(logger!=null)logger.debug("call convert for ObjectAttribute");  
          stringRepresentation+= ((ObjectAttribute)o).getValue();
        }
        else
        {
          if(logger!=null)logger.warn("Unknown Type for StringConversion ::"+o.getClass());
        }
      
        
        
        return stringRepresentation;
    }
    
     public void setLogger()
    {
        if(logger==null)
            logger = org.apache.log4j.Logger.getLogger(this.getClass());
    }
}
