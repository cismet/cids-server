/*
 * UnableToDeregister.java
 *
 * Created on 24. November 2006, 15:58
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package Sirius.server.registry.rmplugin.exception;

/**
 *
 * @author Sebastian
 */
public class UnableToDeregisterException extends Exception {
    
    /** Creates a new instance of UnableToDeregister */
    public UnableToDeregisterException() {
    super();
    }
    
    public UnableToDeregisterException(String message) {
    super(message);
    }
    
}
