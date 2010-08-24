/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package Sirius.server.sql;

import org.apache.log4j.Logger;

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
public class QueryParametrizer {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(QueryParametrizer.class);

    //~ Methods ----------------------------------------------------------------

    /**
     * Hauptfunktionalit\u00E4t der Klasse Parametrizer siehe Klassenbeschreibung.<BR>
     *
     * @param   statement   java.lang.String statement
     * @param   parameters  java.lang.Object[] parameters
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  java.lang.Exception DOCUMENT ME!
     */
    // FIXME: The statement parameter is modified!
    public static String parametrize(String statement, final Object[] parameters) throws Exception {
        if (statement != null) {
            statement = statement.trim();
        } else {
            LOG.error("Stmnt in parametrizer NUll"); // NOI18N
        }

        // nothing to parametrize
        if (parameters.length == 0) {
            return statement;
        }

        String parametrizedStmnt = ""; // NOI18N

        final char chr = '?';

        int start = 0;
        int hit = statement.indexOf(chr);
        int i = 0;

        while ((hit != -1) && (i < parameters.length)) {
            parametrizedStmnt += statement.substring(start, hit); // statement bis zum ersten parameter

            parametrizedStmnt += parameters[i++].toString();

            start = hit + 1;

            hit = statement.indexOf(chr, start);

            // logger.debug( parametrizedStmnt + " hit :"+hit+" start :"+start);
        }

        parametrizedStmnt += statement.substring(start, statement.length()); // rest after last '?'
        if (LOG.isDebugEnabled()) {
            LOG.debug("INFO Stment :  " + parametrizedStmnt);                // NOI18N
        }

        return parametrizedStmnt;
    }
}
