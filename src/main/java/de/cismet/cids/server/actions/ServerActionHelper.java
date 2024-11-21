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

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
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
    private static String tmpFilePath = null;
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
                tmpFilePath = config.getTmpFilePath();
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

        if (((obj instanceof byte[]) || (obj instanceof UploadableInputStream)) && (path != null)) {
            if ((obj instanceof UploadableInputStream) || (((byte[])obj).length >= threshold)) {
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
                    int filesize = -1;
                    int res = WebDavHelper.createFolder(server + dayFolder + "/", webDavClient);
                    res = WebDavHelper.createFolder(server + dayFolder + "/" + randomFolderName + "/",
                            webDavClient);

                    final String completeFolder = server
                                + dayFolder
                                + "/"
                                + randomFolderName
                                + "/";

                    // create the file
                    if (obj instanceof byte[]) {
                        res = WebDavHelper.uploadFileToWebDAV(
                                file,
                                new ByteArrayInputStream((byte[])obj),
                                completeFolder,
                                webDavClient,
                                null);
                        filesize = ((byte[])obj).length;
                    } else if (obj instanceof UploadableInputStream) {
                        FileOutputStream fos = null;
                        File tmpFile = null;

                        if (tmpFilePath != null) {
                            final File directory = new File(tmpFilePath);

                            if (directory.exists()) {
                                tmpFile = File.createTempFile("actionResult", ".out", directory);
                                fos = new FileOutputStream(tmpFile);
                            } else {
                                LOG.error("Directory for temporary files does not exists: " + tmpFilePath);
                            }
                        }

                        if (fos == null) {
                            tmpFile = File.createTempFile("actionResult", "out");
                            fos = new FileOutputStream(tmpFile);
                        }

                        InputStream is = ((UploadableInputStream)obj).getInputStream();
                        final byte[] tmp = new byte[2048];
                        int count;
                        filesize = 0;

                        while ((count = is.read(tmp)) != -1) {
                            fos.write(tmp, 0, count);
                            filesize += count;
                        }

                        is.close();
                        fos.close();
                        fos = null;
                        is = null;

                        res = WebDavHelper.uploadFileToWebDAVWithPreemptiveAuth(
                                file,
                                tmpFile,
                                completeFolder,
                                webDavClient,
                                null);

                        if (tmpFile != null) {
                            try {
                                tmpFile.delete();
                            } catch (Exception e) {
                                LOG.error("Cannot delete the temporary file: " + tmpFile.getAbsolutePath());
                            }
                        }
                    }

                    if (res == 201) {
                        return new PreparedAsyncByteAction(completeFolder + file, filesize);
                    } else {
                        LOG.error("Cannot copy the action result to the http server. HTTP status code = " + res);
                    }
                } catch (Exception e) {
                    LOG.error("Error while trying to copxy the action result to the http server", e);
                }
            }
        }

        if (obj instanceof UploadableInputStream) {
            try {
                return IOUtils.toByteArray(((UploadableInputStream)obj).getInputStream());
            } catch (Exception e) {
                LOG.error("Cannot convert inputStream to byte array", e);
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
