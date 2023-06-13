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
import Sirius.server.middleware.interfaces.domainserver.MetaServiceStore;
import Sirius.server.property.ServerProperties;

import org.apache.commons.io.IOUtils;

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
public class BandwidthTestAction implements ServerAction, MetaServiceStore {

    //~ Static fields/initializers ---------------------------------------------

    public static final String TASK_NAME = "bandwidthTest";

    //~ Instance fields --------------------------------------------------------

    private MetaService ms;

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
            final String separator = serverProps.getFileSeparator();

            final String filePath = "/bandwidthTest/" + fileSizeInMb.toString() + "MB.zip";
            final File file = new File(serverRespath
                            + ("/".equals(separator) ? filePath : filePath.replace("/", separator)));
            try {
                return IOUtils.toByteArray(new FileInputStream(file));
            } catch (final IOException ex) {
                throw new RuntimeException("Testfile not found.", ex);
            }
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
