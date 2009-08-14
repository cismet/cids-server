/*
 * Creatable.java
 *
 * Created on 21. November 2003, 14:44
 */

package Sirius.server.sql;

/**
 *
 * @author  schlob
 */
public interface Createable {
    
   public Object newInstance(Object[] params);
    
}
