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
package de.cismet.cids.server.actions;

import org.apache.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.Random;

import de.cismet.cids.utils.ActionUploadConfig;
import de.cismet.cids.utils.UncaughtClientExceptionConfig;
import de.cismet.cids.utils.serverresources.GeneralServerResources;
import de.cismet.cids.utils.serverresources.ServerResourcesLoader;

import de.cismet.commons.security.WebDavClient;
import de.cismet.commons.security.WebDavHelper;

import de.cismet.netutil.ProxyHandler;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class ServerActionHelper {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(ServerActionHelper.class);
    private static final int DEFAULT_THRESHOLD = (1024 * 1024 * 10); // 10MB
    private static String path = null;
    private static String usr = null;
    private static String pwd = null;
    private static int threshold = DEFAULT_THRESHOLD;

    private static final DateFormat DF = new SimpleDateFormat("yyyyMMdd");

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     */
    private static void init() {
        if ((path == null)) {
            try {
                final ActionUploadConfig config = ServerResourcesLoader.getInstance()
                            .loadJson(GeneralServerResources.CONFIG_ACTION_UPLOAD_JSON.getValue(),
                                ActionUploadConfig.class);

                path = config.getPath();
                threshold = convertStringToByteCount(config.getThreshold());
                usr = config.getUser();
                pwd = config.getPassword();
            } catch (final Exception ex) {
                LOG.error("Error while reading the action upload.json", ex);
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   obj       DOCUMENT ME!
     * @param   filename  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static Object asyncByteArrayHelper(final Object obj, final String filename) {
        init();

        if ((obj instanceof byte[]) && (path != null)) {
            if (((byte[])obj).length >= threshold) {
                // handle it
                final Date now = new Date();
                final String dayFolder = DF.format(now);
                final String randomFolderName = generateRandomString(20);

                final String server = ((!path.endsWith("/")) ? (path + "/") : path);
                final String username = usr;
                final String password = pwd;
                final String file = ((filename != null) ? filename : generateRandomString(15));

                // the web dav client makes normal http requests, but uses basic authentication, if required and uses
                // the proxy, if configured
                final WebDavClient webDavClient = new WebDavClient(ProxyHandler.getInstance().getProxy(),
                        username,
                        password);

                try {
                    // create the folders, if they not already exists
                    int res = WebDavHelper.createFolder(server + dayFolder + "/", webDavClient);
                    res = WebDavHelper.createFolder(server + dayFolder + "/" + randomFolderName + "/",
                            webDavClient);

                    final String completeFolder = server
                                + dayFolder
                                + "/"
                                + randomFolderName
                                + "/";

                    // create the file
                    res = WebDavHelper.uploadFileToWebDAV(
                            file,
                            new ByteArrayInputStream((byte[])obj),
                            completeFolder,
                            webDavClient,
                            null);

                    return new PreparedAsyncByteAction(completeFolder + file, ((byte[])obj).length);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return obj;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   bytes  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private static int convertStringToByteCount(final String bytes) {
        if (bytes != null) {
            String numberString = bytes;
            char postfix = 0;
            double number = 0.0;

            if (numberString.toLowerCase().endsWith("k") || numberString.toLowerCase().endsWith("m")
                        || numberString.toLowerCase().endsWith("g")) {
                numberString = numberString.substring(0, numberString.length() - 1);
                postfix = bytes.toLowerCase().charAt(bytes.length() - 1);
            }

            try {
                number = Double.parseDouble(numberString);
            } catch (NumberFormatException e) {
                return DEFAULT_THRESHOLD;
            }

            if (number != 0.0) {
                switch (postfix) {
                    case 'g': {
                        return (int)(number * Math.pow(1024.0, 3));
                    }
                    case 'm': {
                        return (int)(number * Math.pow(1024.0, 2));
                    }
                    case 'k': {
                        return (int)(number * Math.pow(1024.0, 1));
                    }
                    default: {
                        return (int)number;
                    }
                }
            }
        }

        return DEFAULT_THRESHOLD;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   stringLength  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private static String generateRandomString(final int stringLength) {
        final int leftLimit = 48;   // numeral '0'
        final int rightLimit = 122; // letter 'z'
        final Random random = new Random();

        final String generatedString = random.ints(leftLimit, rightLimit + 1)
                    .filter(i ->
                                ((i <= 57) || (i >= 65))
                                && ((i <= 90) || (i >= 97)))
                    .limit(stringLength)
                    .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                    .toString();

        return generatedString;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  args  DOCUMENT ME!
     */
    public static void main(final String[] args) {
//        System.out.println("generateRandomString: " + generateRandomString(20));
        final byte[] test = { 1, 2, 3, 4 };

        System.out.println("start");
        asyncByteArrayHelper(test, "test.pdf");
        System.out.println("end");
    }
}
