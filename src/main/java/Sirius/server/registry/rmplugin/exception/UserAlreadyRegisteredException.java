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
public class UserAlreadyRegisteredException extends Exception {
    
    /** Creates a new instance of UnableToDeregister */
    public UserAlreadyRegisteredException() {
    super();
    }
    
    public UserAlreadyRegisteredException(String message) {
    super(message);
    }
    
}
