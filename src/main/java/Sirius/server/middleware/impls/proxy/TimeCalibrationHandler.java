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
package Sirius.server.middleware.impls.proxy;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
public class TimeCalibrationHandler {

    //~ Instance fields --------------------------------------------------------

    private final Map<String, Long> calibrationOffsets = new ConcurrentHashMap<>();

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static TimeCalibrationHandler getInstance() {
        return LazyInitialiser.INSTANCE;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   domain  DOCUMENT ME!
     * @param   timeMs  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Long calibrate(final String domain, final long timeMs) {
        final long currentTimeMs = System.currentTimeMillis();
        calibrationOffsets.put(domain, currentTimeMs - timeMs);
        return currentTimeMs;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   domain  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Long getCalibrationOffset(final String domain) {
        return calibrationOffsets.containsKey(domain) ? calibrationOffsets.get(domain) : null;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   domain  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Long getCalibratedTime(final String domain) {
        final Long calibrationTime = TimeCalibrationHandler.getInstance().getCalibrationOffset(domain);
        return (calibrationTime != null) ? (System.currentTimeMillis() + calibrationTime) : null;
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private static final class LazyInitialiser {

        //~ Static fields/initializers -----------------------------------------

        private static final TimeCalibrationHandler INSTANCE = new TimeCalibrationHandler();

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new LazyInitialiser object.
         */
        private LazyInitialiser() {
        }
    }
}
