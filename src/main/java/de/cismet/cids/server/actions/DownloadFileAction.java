/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.server.actions;

import Sirius.server.middleware.impls.proxy.ProxyImpl;
import Sirius.server.middleware.interfaces.domainserver.MetaService;
import Sirius.server.middleware.interfaces.domainserver.MetaServiceStore;

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
public class DownloadFileAction implements ServerAction, MetaServiceStore {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(ProxyImpl.class);

    //~ Instance fields --------------------------------------------------------

    MetaService ms;

    //~ Methods ----------------------------------------------------------------

    @Override
    public String getTaskName() {
        return "downloadFile";
    }

    @Override
    public Object execute(final Object body, final ServerActionParameter... params) {
        System.out.println("Ausführen von DownloadFileAction");
        for (final ServerActionParameter sap : params) {
            System.out.println(sap);
        }
        try {
            final File file = new File((String)body);
            final FileInputStream fin = new FileInputStream(file);
            final byte[] fileContent = new byte[(int)file.length()];
            fin.read(fileContent);
            return fileContent;
        } catch (IOException ex) {
            LOG.error(ex, ex);
            return null;
        }
    }

    @Override
    public MetaService getMetaService() {
        return ms;
    }

    @Override
    public void setMetaService(final MetaService service) {
        ms = service;
    }
}
