/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.server.actions;

import Sirius.server.middleware.impls.proxy.ProxyImpl;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;

import java.text.SimpleDateFormat;

import java.util.Date;

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
@org.openide.util.lookup.ServiceProvider(service = ServerAction.class)
public class AppendToTestFileServerAction implements ServerAction {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(ProxyImpl.class);
    private static final String TASKNAME = "AppendToTestFile";
    private static final String FILE_NAME = "AppendToTestFile.txt";
    private static final String CHARSET = "UTF-8";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

    //~ Methods ----------------------------------------------------------------

    @Override
    public String getTaskName() {
        return TASKNAME;
    }

    @Override
    public Object execute(final Object body, final ServerActionParameter... params) {
        try {
            final Date now = new Date();
            final String text = DATE_FORMAT.format(now) + " | " + (body instanceof byte[] ? new String((byte[])body) : body) + "\n";
            final File appendToTestFile = new File(FILE_NAME);
            FileUtils.writeStringToFile(appendToTestFile, text, CHARSET, true);
            return text;
        } catch (final IOException ex) {
            LOG.error(ex, ex);
            return ex;
        }
    }
}
