/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * PropertyChecker.java
 *
 * Created on 4. Mai 2006, 14:42
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package Sirius.server.property;

import java.util.*;
/**
 * DOCUMENT ME!
 *
 * @author   schlob
 * @version  $Revision$, $Date$
 */
public class PropertyChecker {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(
            PropertyChecker.class);

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new instance of PropertyChecker.
     */
    private PropertyChecker() {
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   check   DOCUMENT ME!
     * @param   sample  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static boolean checkProperties(ServerProperties check, ServerProperties sample) {
        Enumeration enSample = sample.getKeys();
        Enumeration enCheck = check.getKeys();

        HashSet checkKeys = new HashSet();

        while (enCheck.hasMoreElements()) {
            checkKeys.add(enCheck.nextElement());
        }

        boolean correct = true;

        while (enSample.hasMoreElements()) {
            Object nextElement = enSample.nextElement();

            if (!checkKeys.contains(nextElement)) {
                String error = "key not found in config file " + nextElement;  // NOI18N
                logger.error(error);
                System.err.println(error);

                // nicht sofort raus damit alle fehlenden ausgegeben werden k\u00F6nnen
                correct &= false;
            }
        }

        return correct;
    }
}
