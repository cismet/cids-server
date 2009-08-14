/*
 * DataRetrievalException.java
 *
 * Created on 16. September 2003, 10:54
 */

package Sirius.server.dataretrieval;
import org.apache.log4j.*;
import java.sql.SQLException;

/**
 *  Bietet Konstruktoren f\u00FCr die \u00FCbliche verwendung, sowie solche die zus\u00E4tzlich
 * logging Funktionalit\u00E4t besitzen.
 *
 * @author  awindholz
 */
public class DataRetrievalException extends Exception {
    
    // Standardkonstruktoren
    public DataRetrievalException() {
        super();
    }
    
    /** 
     * DataRetrievalException die lediglich eine Meldung enth\u00E4lt. Ohne logging.
     */
    public DataRetrievalException(String meldung) {
        super(meldung);
    }
    
    /** 
     * DataRetrievalException die eine Exception enth\u00E4lt. StackTrace wird in 
     * dise Exception mit \u00FCbernommen. Ohne logging.
     */
    public DataRetrievalException(Throwable e) {
        super(e);
        super.setStackTrace(e.getStackTrace());
    }

    /** 
     * DataRetrievalException die die \u00FCbergebene Exception mit der \u00FCbergebenen
     * Message (statt der Originalmessage) enth\u00E4lt. StackTrace wird in 
     * dise Exception mit \u00FCbernommen. Ohne logging.
     */
    public DataRetrievalException(String meldung, Throwable e) {
        super(meldung, e);
        super.setStackTrace(e.getStackTrace());
    }
    
    // Konstruktoren mit Logging mechanisnus.
    
    /** 
     * DataRetrievalException die lediglich eine Meldung enth\u00E4lt. 
     * Geloggt wird die Meldung auf dem ERROR-Level.
     */
    public DataRetrievalException(String meldung, Logger logger) {
        super(meldung);
        
        logger.error(meldung);
    }
    
    /** 
     * DataRetrievalException die eine Exception enth\u00E4lt. StackTrace wird in 
     * dise Exception mit \u00FCbernommen. Die Message die in der Exception enthalten
     * ist wird auf dem ERROR-Level gelogt, StackTrace auf DEBUG-Level.
     */
    public DataRetrievalException(Throwable e, Logger logger) {
        super(e);
        super.setStackTrace(e.getStackTrace());
        
       // logger.error(e.getMessage());
        
        logger.debug(e, e);
    }
    
    /** 
     * DataRetrievalException die die \u00FCbergebene Exception mit der \u00FCbergebenen
     * Message (statt der Originalmessage) enth\u00E4lt. StackTrace wird in 
     * dise Exception mit \u00FCbernommen. Die \u00FCbergebene Message wird auf dem 
     * ERROR-Level gelogt, die original-Message und StackTrace auf DEBUG-Level.
     */
    public DataRetrievalException(String meldung, Throwable e, Logger logger) {
        super(meldung, e);
        super.setStackTrace(e.getStackTrace());
        
       // logger.error(meldung);
        
        logger.debug(e, e);
    }
    
    /** 
     * Da SQLException tiefe Struktur haben kann ist dieses Konstruktor auch 
     * entsprechend angepasst. DataRetrievalException die die \u00FCbergebene 
     * SQLException enth\u00E4lt. StackTrace wird in  diese Exception mit \u00FCbernommen. 
     *  Die Message die in der obersten SQLException enthalten ist wird auf dem 
     * ERROR-Level gelogt. Auf dem DEBUG-Level wird die tiefere Struktur 
     * mitber\u00FCcksichtigt, auch Stacktrace wird geloggt.
     */
    public DataRetrievalException(SQLException e, Logger logger) {
        super(e);
        super.setStackTrace(e.getStackTrace());
        
        //logger.error(e.getMessage());
        
        debug(e, logger);
    }
    
    /** 
     * Da SQLException tiefe Struktur haben kann ist dieses Konstruktor auch 
     * entsprechend angepasst. DataRetrievalException die die \u00FCbergebene 
     * SQLException enth\u00E4lt. StackTrace wird in  diese Exception mit \u00FCbernommen. 
     * Message der \u00FCbergebenen SQLException wird durch die \u00FCbergebene ersetzt.
     * Die \u00FCbergebene Message wird auf dem ERROR-Level gelogt, die original-
     * Message und StackTrace unter beachtung der tieferen struktur auf DEBUG-Level.
     */
    public DataRetrievalException(String meldung, SQLException e, Logger logger) {
        super(meldung, e);
        super.setStackTrace(e.getStackTrace());
        
        //logger.error(meldung);
        
        debug(e, logger);
    }
    
    private void debug(SQLException e, Logger logger) {
        if(logger.isDebugEnabled()) {
            SQLException tmp;
            logger.debug(e, e);
            while((tmp = e.getNextException()) != null) {
                logger.debug(tmp, tmp);
            }
        }
    }

}
