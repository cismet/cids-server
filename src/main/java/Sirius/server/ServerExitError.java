/*
 * ServerExitError.java
 *
 * Created on 1. M\u00E4rz 2004, 14:46
 */

package Sirius.server;

/**
 *Will be thrown when a server process (registry,domainserver,proxy terminates
 * @author  schlob
 */
public class ServerExitError extends java.lang.Throwable implements java.io.Serializable {
    
    /** Creates a new instance of ServerExitError */
    public ServerExitError() {
        super();
    }
    
    
    public ServerExitError(String message) {
        super(message);
    }
    
    
    public ServerExitError(String message,Throwable cause)
    {
        super(message,cause);
    }
    
    public ServerExitError(Throwable cause)
    {
        super(cause);
    }
    
    
    
}
