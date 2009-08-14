/*
 * DBVersionException.java
 *
 * Created on 29. April 2003, 10:21
 */

package Sirius.util.DBVersionChecker;

/**
 * F\u00FCr Exceptions im VersionChecker.
 *
 * @author  awindholz
 */
public class DBVersionException extends Throwable {
    
    /** Creates a new instance of DBVersionException */
    public DBVersionException() {
    }
    
    /** Creates a new instance of DBVersionException */
    public DBVersionException(String message) {
        super(message);
    }
    
    /** Creates a new instance of DBVersionException */
    public DBVersionException(String message, Throwable cause) {
        super(message, cause);
    }
    
    /** Creates a new instance of DBVersionException */
    public DBVersionException(Throwable cause) {
        super(cause);
    }
}
