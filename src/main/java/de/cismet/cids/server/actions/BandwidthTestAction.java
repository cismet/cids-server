/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.server.actions;

import Sirius.server.middleware.impls.domainserver.DomainServerImpl;
import Sirius.server.middleware.interfaces.domainserver.MetaService;
import Sirius.server.property.ServerProperties;

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
@org.openide.util.lookup.ServiceProvider(service = ServerAction.class)
public class BandwidthTestAction extends DownloadFileAction {

    //~ Static fields/initializers ---------------------------------------------

    public static final String TASK_NAME = "bandwidthTest";

    //~ Methods ----------------------------------------------------------------

    @Override
    public String getTaskName() {
        return TASK_NAME;
    }

    @Override
    public Object execute(final Object body, final ServerActionParameter... params) {
        if (body == null) {
            throw new RuntimeException("The body is missing.");
        } else if (!(body instanceof Integer)) {
            throw new RuntimeException("Wrong type for body, have to be an Integer.");
        } else {
            final Integer fileSizeInMb = (Integer)body;

            final ServerProperties serverProps = DomainServerImpl.getServerProperties();
            final String serverRespath = serverProps.getServerResourcesBasePath();
            final String s = serverProps.getFileSeparator();
            final String filePath = "/de/cismet/cids/tools/bandwidthTest/" + fileSizeInMb.toString()
                        + "MB.zip";
            final Object ret;
            if ("/".equals(s)) {
                ret = super.execute(serverRespath + filePath);
            } else {
                ret = super.execute(serverRespath + filePath.replace("/", s));
            }
            if (ret == null) {
                throw new RuntimeException("Testfile not found.");
            }
            return ret;
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
