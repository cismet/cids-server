/*
 * ServerExit.java
 *
 * Created on 1. M\u00E4rz 2004, 15:07
 */

package Sirius.server;

/**
 *
 * @author  schlob
 */
public class ServerExit extends java.lang.Throwable implements java.io.Serializable {
    
    /** Creates a new instance of ServerExitError */
    public ServerExit() {
        super();
    }
    
    
    public ServerExit(String message) {
        super(message);
    }
    
    
    public ServerExit(String message,Throwable cause)
    {
        super(message,cause);
    }
    
    public ServerExit(Throwable cause)
    {
        super(cause);
    }
    
}
