/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * DBVersionException.java
 *
 * Created on 29. April 2003, 10:21
 */
package Sirius.util.DBVersionChecker;

/**
 * F\u00FCr Exceptions im VersionChecker.
 *
 * @author   awindholz
 * @version  $Revision$, $Date$
 */
public class DBVersionException extends Throwable {

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new instance of DBVersionException.
     */
    public DBVersionException() {
    }

    /**
     * Creates a new instance of DBVersionException.
     *
     * @param  message  DOCUMENT ME!
     */
    public DBVersionException(final String message) {
        super(message);
    }

    /**
     * Creates a new instance of DBVersionException.
     *
     * @param  cause  DOCUMENT ME!
     */
    public DBVersionException(final Throwable cause) {
        super(cause);
    }

    /**
     * Creates a new instance of DBVersionException.
     *
     * @param  message  DOCUMENT ME!
     * @param  cause    DOCUMENT ME!
     */
    public DBVersionException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
