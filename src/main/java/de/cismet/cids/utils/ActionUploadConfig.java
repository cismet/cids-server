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
public class ActionUploadConfig {

    //~ Instance fields --------------------------------------------------------

    private String path;
    private String user;
    private String password;
    private String threshold;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new UncaughtClientExceptionConfig object.
     */
    private ActionUploadConfig() {
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static ActionUploadConfig getInstance() {
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

        private static final ActionUploadConfig INSTANCE;

        static {
            try {
                INSTANCE = ServerResourcesLoader.getInstance()
                            .loadJson(GeneralServerResources.CONFIG_ACTION_UPLOAD_JSON.getValue(),
                                    ActionUploadConfig.class);
            } catch (final Exception ex) {
                throw new RuntimeException("error while initializing ActionUploadConfig", ex);
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
