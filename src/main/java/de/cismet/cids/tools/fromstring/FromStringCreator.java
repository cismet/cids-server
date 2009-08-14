/*
 * FromStringCreator.java
 *
 * Created on 26. August 2004, 13:41
 */

package de.cismet.cids.tools.fromstring;

/**
 *
 * @author  schlob
 */
public abstract class FromStringCreator implements java.io.Serializable
{
    
    /** Creates a new instance of FromStringCreator */
    public FromStringCreator() 
    {
    }
    
    
    public abstract Object create(String objectRepresentation, Object hull) throws Exception;
    
    
}
