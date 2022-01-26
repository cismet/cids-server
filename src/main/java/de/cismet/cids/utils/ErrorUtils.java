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

import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import java.util.ArrayList;
import java.util.List;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class ErrorUtils {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(ErrorUtils.class);
    private static final List<Exception> thrownExceptions = new ArrayList<>();

    //~ Methods ----------------------------------------------------------------

    /**
     * Exits the server with the given eror code and prints the error message from the given Throwable object to the
     * file rapidUnscheduledDisassembly.log. This method should be invoked, when an error occured that makes it
     * impossible for the server to work properly.
     *
     * @param  errorCode  the error code to return
     * @param  message    DOCUMENT ME!
     * @param  e          the exception with the error message and stacktrace
     */
    public static void createRUDFileAndExit(final int errorCode, final String message, final Exception e) {
        createRUDFile(message, e);
        System.exit(errorCode);
    }

    /**
     * Exits the server with the given eror code and prints the error message from the given Throwable object to the
     * file rapidUnscheduledDisassembly.log. This method should be invoked, when an error occured that makes it
     * impossible for the server to work properly.
     *
     * @param  message  DOCUMENT ME!
     * @param  e        t the throwable with the error message and stacktrace
     */
    public static void createRUDFile(final String message, final Exception e) {
        if (!exceptionAlreadyShown(e)) {
            try(final PrintWriter fw = new PrintWriter(new FileWriter(new File("rapidUnscheduledDisassembly.log")))) {
                if (message != null) {
                    fw.println(message);
                }
                e.printStackTrace(fw);
                addThrownException(e);
            } catch (IOException ex) {
                LOG.error("Cannot write rapidUnscheduledDisassembly file", ex);
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   e  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static boolean exceptionAlreadyShown(final Exception e) {
        for (final Exception thrownException : thrownExceptions) {
            if (thrownException.equals(e)) {
                return true;
            }
        }

        return false;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  e  DOCUMENT ME!
     */
    public static void addThrownException(final Exception e) {
        if (!exceptionAlreadyShown(e)) {
            thrownExceptions.add(e);
        }
    }
}
