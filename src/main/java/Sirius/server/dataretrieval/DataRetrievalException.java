/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * DataRetrievalException.java
 *
 * Created on 16. September 2003, 10:54
 */
package Sirius.server.dataretrieval;
import org.apache.log4j.*;

import java.sql.SQLException;

/**
 * Bietet Konstruktoren f\u00FCr die \u00FCbliche verwendung, sowie solche die zus\u00E4tzlich logging
 * Funktionalit\u00E4t besitzen.
 *
 * @author   awindholz
 * @version  $Revision$, $Date$
 */
public class DataRetrievalException extends Exception {

    //~ Constructors -----------------------------------------------------------

    /**
     * Standardkonstruktoren.
     */
    public DataRetrievalException() {
        super();
    }

    /**
     * DataRetrievalException die lediglich eine Meldung enth\u00E4lt. Ohne logging.
     *
     * @param  meldung  DOCUMENT ME!
     */
    public DataRetrievalException(final String meldung) {
        super(meldung);
    }

    /**
     * DataRetrievalException die eine Exception enth\u00E4lt. StackTrace wird in dise Exception mit \u00FCbernommen.
     * Ohne logging.
     *
     * @param  e  DOCUMENT ME!
     */
    public DataRetrievalException(final Throwable e) {
        super(e);
        super.setStackTrace(e.getStackTrace());
    }

    /**
     * DataRetrievalException die die \u00FCbergebene Exception mit der \u00FCbergebenen Message (statt der
     * Originalmessage) enth\u00E4lt. StackTrace wird in dise Exception mit \u00FCbernommen. Ohne logging.
     *
     * @param  meldung  DOCUMENT ME!
     * @param  e        DOCUMENT ME!
     */
    public DataRetrievalException(final String meldung, final Throwable e) {
        super(meldung, e);
        super.setStackTrace(e.getStackTrace());
    }

    // Konstruktoren mit Logging mechanisnus.

    /**
     * DataRetrievalException die lediglich eine Meldung enth\u00E4lt. Geloggt wird die Meldung auf dem ERROR-Level.
     *
     * @param  meldung  DOCUMENT ME!
     * @param  logger   DOCUMENT ME!
     */
    public DataRetrievalException(final String meldung, final Logger logger) {
        super(meldung);

        logger.error(meldung);
    }

    /**
     * DataRetrievalException die eine Exception enth\u00E4lt. StackTrace wird in dise Exception mit \u00FCbernommen.
     * Die Message die in der Exception enthalten ist wird auf dem ERROR-Level gelogt, StackTrace auf DEBUG-Level.
     *
     * @param  e       DOCUMENT ME!
     * @param  logger  DOCUMENT ME!
     */
    public DataRetrievalException(final Throwable e, final Logger logger) {
        super(e);
        super.setStackTrace(e.getStackTrace());
        if (logger.isDebugEnabled()) {
            // logger.error(e.getMessage());
            logger.debug(e, e);
        }
    }

    /**
     * Da SQLException tiefe Struktur haben kann ist dieses Konstruktor auch entsprechend angepasst.
     * DataRetrievalException die die \u00FCbergebene SQLException enth\u00E4lt. StackTrace wird in diese Exception mit
     * \u00FCbernommen. Die Message die in der obersten SQLException enthalten ist wird auf dem ERROR-Level gelogt. Auf
     * dem DEBUG-Level wird die tiefere Struktur mitber\u00FCcksichtigt, auch Stacktrace wird geloggt.
     *
     * @param  e       DOCUMENT ME!
     * @param  logger  DOCUMENT ME!
     */
    public DataRetrievalException(final SQLException e, final Logger logger) {
        super(e);
        super.setStackTrace(e.getStackTrace());

        // logger.error(e.getMessage());

        debug(e, logger);
    }

    /**
     * DataRetrievalException die die \u00FCbergebene Exception mit der \u00FCbergebenen Message (statt der
     * Originalmessage) enth\u00E4lt. StackTrace wird in dise Exception mit \u00FCbernommen. Die \u00FCbergebene Message
     * wird auf dem ERROR-Level gelogt, die original-Message und StackTrace auf DEBUG-Level.
     *
     * @param  meldung  DOCUMENT ME!
     * @param  e        DOCUMENT ME!
     * @param  logger   DOCUMENT ME!
     */
    public DataRetrievalException(final String meldung, final Throwable e, final Logger logger) {
        super(meldung, e);
        super.setStackTrace(e.getStackTrace());
        if (logger.isDebugEnabled()) {
            // logger.error(meldung);
            logger.debug(e, e);
        }
    }

    /**
     * Da SQLException tiefe Struktur haben kann ist dieses Konstruktor auch entsprechend angepasst.
     * DataRetrievalException die die \u00FCbergebene SQLException enth\u00E4lt. StackTrace wird in diese Exception mit
     * \u00FCbernommen. Message der \u00FCbergebenen SQLException wird durch die \u00FCbergebene ersetzt. Die
     * \u00FCbergebene Message wird auf dem ERROR-Level gelogt, die original- Message und StackTrace unter beachtung der
     * tieferen struktur auf DEBUG-Level.
     *
     * @param  meldung  DOCUMENT ME!
     * @param  e        DOCUMENT ME!
     * @param  logger   DOCUMENT ME!
     */
    public DataRetrievalException(final String meldung, final SQLException e, final Logger logger) {
        super(meldung, e);
        super.setStackTrace(e.getStackTrace());

        // logger.error(meldung);

        debug(e, logger);
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param  e       DOCUMENT ME!
     * @param  logger  DOCUMENT ME!
     */
    private void debug(final SQLException e, final Logger logger) {
        if (logger.isDebugEnabled()) {
            SQLException tmp;
            logger.debug(e, e);
            while ((tmp = e.getNextException()) != null) {
                if (logger.isDebugEnabled()) {
                    logger.debug(tmp, tmp);
                }
            }
        }
    }
}
