/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * ResultSetToLargeException.java
 *
 * Created on 18. September 2003, 09:13
 */
package Sirius.server.dataretrieval;

import org.apache.log4j.*;

import java.sql.SQLException;

/**
 * Wenn CachedRS zu gross zum cachen der Daten wird diese Exception ausgeworfen.
 *
 * @author   awindholz
 * @version  $Revision$, $Date$
 */
public class ResultSetTooLargeException extends DataRetrievalException {

    //~ Static fields/initializers ---------------------------------------------

    /** Use serialVersionUID for interoperability. */
    private static final long serialVersionUID = -7887240275287448087L;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new instance of ResultSetToLargeException.
     */
    public ResultSetTooLargeException() {
    }
    /**
     * ResultSetTooLargeException die lediglich eine Meldung enth\u00E4lt. Ohne logging.
     *
     * @param  meldung  DOCUMENT ME!
     */
    public ResultSetTooLargeException(final String meldung) {
        super(meldung);
    }

    /**
     * ResultSetTooLargeException die eine Exception enth\u00E4lt. StackTrace wird in dise Exception mit
     * \u00FCbernommen. Ohne logging.
     *
     * @param  meldung  DOCUMENT ME!
     * @param  logger   DOCUMENT ME!
     */
/*    public ResultSetTooLargeException(Throwable e) {
        super(e);
    }*/

    /**
     * ResultSetTooLargeException die die \u00FCbergebene Exception mit der \u00FCbergebenen Message (statt der
     * Originalmessage) enth\u00E4lt. StackTrace wird in dise Exception mit \u00FCbernommen. Ohne logging.
     *
     * @param  meldung  DOCUMENT ME!
     * @param  logger   DOCUMENT ME!
     */
/*    public ResultSetTooLargeException(String meldung, Throwable e) {
        super(meldung, e);
    }*/

    // Konstruktoren mit Logging mechanisnus.

    /**
     * ResultSetTooLargeException die lediglich eine Meldung enth\u00E4lt. Geloggt wird die Meldung auf dem ERROR-Level.
     *
     * @param  meldung  DOCUMENT ME!
     * @param  logger   DOCUMENT ME!
     */
    public ResultSetTooLargeException(final String meldung, final Logger logger) {
        super(meldung, logger);
    }

    /**
     * ResultSetTooLargeException die eine Exception enth\u00E4lt. StackTrace wird in dise Exception mit
     * \u00FCbernommen. Die Message die in der Exception enthalten ist wird auf dem ERROR-Level gelogt, StackTrace auf
     * DEBUG-Level.
     *
     * @param  e       DOCUMENT ME!
     * @param  logger  DOCUMENT ME!
     */
    public ResultSetTooLargeException(final Throwable e, final Logger logger) {
        super(e, logger);
    }

    /**
     * ResultSetTooLargeException die die \u00FCbergebene Exception mit der \u00FCbergebenen
     * Message (statt der Originalmessage) enth\u00E4lt. StackTrace wird in
     * dise Exception mit \u00FCbernommen. Die \u00FCbergebene Message wird auf dem
     * ERROR-Level gelogt, die original-Message und StackTrace auf DEBUG-Level.
     */
/*    public ResultSetTooLargeException(String meldung, Throwable e, Logger logger) {
        super(meldung, e, logger);
    }*/

    /**
     * Da SQLException tiefe Struktur haben kann ist dieses Konstruktor auch
     * entsprechend angepasst. ResultSetTooLargeException die die \u00FCbergebene
     * SQLException enth\u00E4lt. StackTrace wird in  diese Exception mit \u00FCbernommen.
     *  Die Message die in der obersten SQLException enthalten ist wird auf dem
     * ERROR-Level gelogt. Auf dem DEBUG-Level wird die tiefere Struktur
     * mitber\u00FCcksichtigt, auch Stacktrace wird geloggt.
     */
/*    public ResultSetTooLargeException(SQLException e, Logger logger) {
        super(e, logger);
    }*/

    /**
     * Da SQLException tiefe Struktur haben kann ist dieses Konstruktor auch
     * entsprechend angepasst. ResultSetTooLargeException die die \u00FCbergebene
     * SQLException enth\u00E4lt. StackTrace wird in  diese Exception mit \u00FCbernommen.
     * Message der \u00FCbergebenen SQLException wird durch die \u00FCbergebene ersetzt.
     * Die \u00FCbergebene Message wird auf dem ERROR-Level gelogt, die original-
     * Message und StackTrace unter beachtung der tieferen struktur auf DEBUG-Level.
     */
/*    public ResultSetTooLargeException(String meldung, SQLException e, Logger logger) {
        super(meldung, e, logger);
    }*/
}
