/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.server.actions;

import Sirius.server.middleware.impls.proxy.ProxyImpl;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
@org.openide.util.lookup.ServiceProvider(service = ServerAction.class)
public class DownloadFileAction implements ServerAction {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(ProxyImpl.class);

    //~ Methods ----------------------------------------------------------------

    @Override
    public String getTaskName() {
        return "downloadFile";
    }

    @Override
    public Object execute(final String json) {
        try {
            final File file = new File(json);
            final FileInputStream fin = new FileInputStream(file);
            final byte[] fileContent = new byte[(int)file.length()];
            fin.read(fileContent);
            return new String(fileContent);
        } catch (IOException ex) {
            LOG.error(ex, ex);
            return null;
        }
    }
}
