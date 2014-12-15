/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package Sirius.server.sql;

import org.openide.util.NbBundle;

import java.util.ResourceBundle;

/**
 * DOCUMENT ME!
 *
 * @author   martin.scholl@cismet.de
 * @version  $Revision$, $Date$
 */
public final class SQLTools {

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new SQLTools object.
     */
    private SQLTools() {
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   c           DOCUMENT ME!
     * @param   dialect     DOCUMENT ME!
     * @param   descriptor  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static String getStatement(final Class c, final String dialect, final String descriptor) {
        final String statement;
        if ((dialect == null) || dialect.isEmpty()) {
            statement = NbBundle.getMessage(c, descriptor);
        } else {
            final String bundleName;
            if ("postgres_9".equals(dialect)) {
                // default dialect
                bundleName = "Bundle";
            } else {
                bundleName = "Bundle_" + dialect;
            }
            final String bundle = c.getPackage().getName().replaceAll("\\.", "/") + "/" + bundleName;
            statement = ResourceBundle.getBundle(bundle).getString(descriptor);
        }

        return statement;
    }
}
