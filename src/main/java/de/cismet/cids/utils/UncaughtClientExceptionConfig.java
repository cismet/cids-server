/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cismet.cids.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import de.cismet.cids.utils.serverresources.GeneralServerResources;
import de.cismet.cids.utils.serverresources.ServerResourcesLoader;

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
@Getter
@Setter
@AllArgsConstructor
public class UncaughtClientExceptionConfig {

    //~ Instance fields --------------------------------------------------------

    private String logDirectory;
    private String logFileDateFormat;
    private String logMessage;
    private String logMessageDateFormat;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new UncaughtClientExceptionConfig object.
     */
    private UncaughtClientExceptionConfig() {
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static UncaughtClientExceptionConfig getInstance() {
        return LazyInitialiser.INSTANCE;
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private static final class LazyInitialiser {

        //~ Static fields/initializers -----------------------------------------

        private static final UncaughtClientExceptionConfig INSTANCE;

        static {
            try {
                INSTANCE = ServerResourcesLoader.getInstance()
                            .loadJson(GeneralServerResources.CONFIG_UNCAUGHT_CLIENT_EXCEPTION_JSON.getValue(),
                                    UncaughtClientExceptionConfig.class);
            } catch (final Exception ex) {
                throw new RuntimeException("error while initializing UncaughtClientExceptionConfig", ex);
            }
        }

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new LazyInitialiser object.
         */
        private LazyInitialiser() {
        }
    }
}
