package de.cismet.cids.tools.fromstring;
/*
 * StringCreateable.java
 *
 * Created on 26. August 2004, 13:38
 */

//import Sirius.server.middleware.types.*;
import Sirius.server.localserver.object.Object;

/**
 *
 * @author  schlob
 */
public interface StringCreateable
{
    
    /** Creates a new instance of FromString */
    public java.lang.Object fromString(String objectRepresentation,java.lang.Object mo) throws Exception;
    
    public boolean isStringCreateable();
    
    
    
}
