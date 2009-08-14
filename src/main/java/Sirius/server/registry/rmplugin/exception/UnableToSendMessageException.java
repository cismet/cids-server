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
public class UnableToSendMessageException extends Exception {
    
    private int totalSended=0;
    
    
    /** Creates a new instance of UnableToDeregister */
    public UnableToSendMessageException() {
    super();
    }
    
    public UnableToSendMessageException(String message) {
    super(message);
    }
    
    public UnableToSendMessageException(String message,int totalSended) {
    super(message);
    this.totalSended = totalSended;
    }

    public int getTotalSended() {
        return totalSended;
    }

    public void setTotalSended(int totalSended) {
        this.totalSended = totalSended;
    }
    
    
}
