/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.server.actions;

import Sirius.server.middleware.impls.domainserver.DomainServerImpl;
import Sirius.server.property.ServerProperties;

import de.cismet.cidsx.base.types.Type;

import de.cismet.cidsx.server.api.types.GenericResourceWithContentType;

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
@org.openide.util.lookup.ServiceProvider(service = ServerAction.class)
public class BandwidthTestAction extends DownloadFileAction {

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new BandwidthTestAction object.
     */
    public BandwidthTestAction() {
        super();

        this.actionInfo.setActionKey("bandwidthTest");
        this.actionInfo.setName("Bandwidth Test");
        this.actionInfo.setDescription("Bandwidth Test");

        this.actionInfo.getParameterDescription().get(0).setType(Type.INTEGER);
        this.actionInfo.getParameterDescription().get(0).setKey("fileSizeInMb");
        this.actionInfo.getParameterDescription().get(0).setDescription("Size of the test file in MB");

        this.actionInfo.getBodyDescription().setType(Type.INTEGER);
        this.actionInfo.getBodyDescription().setKey("fileSizeInMb");
        this.actionInfo.getBodyDescription()
                .setDescription("Deprecated, use 'fileSizeInMb' server action parameter instead");
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public final String getTaskName() {
        return "bandwidthTest";
    }

    @Override
    public GenericResourceWithContentType execute(final Object body, final ServerActionParameter... params) {
        int fileSizeInMb = -1;
        if ((params != null) && (params.length > 0)) {
            for (final ServerActionParameter sap : params) {
                if (sap.getKey().equalsIgnoreCase("fileSizeInMb")) {
                    fileSizeInMb = Integer.valueOf(sap.getValue().toString());
                    break;
                } else {
                    LOG.warn("unsupported server action parameter:" + sap.toString());
                }
            }
        }

        if (fileSizeInMb == -1) {
            LOG.warn("client did not provide ServerActionParameter 'fileSizeInMb'");
            if (body != null) {
                fileSizeInMb = Integer.valueOf(body.toString());
                final String message = "client provided 'fileSizeInMb' as server action "
                            + "nor as body parameter!";
                throw new RuntimeException(message);
            }
        }

        final ServerProperties serverProps = DomainServerImpl.getServerProperties();
        final String serverRespath = serverProps.getServerResourcesBasePath();
        final String s = serverProps.getFileSeparator();
        final String filePath = "/bandwidthTest/" + fileSizeInMb + "MB.zip";
        final GenericResourceWithContentType ret;
        if ("/".equals(s)) {
            final ServerActionParameter pathParameter = new ServerActionParameter(
                    PARAMETER_TYPE.FILEPATH.name(),
                    serverRespath
                            + filePath);
            ret = super.execute(null, pathParameter);
        } else {
            final ServerActionParameter pathParameter = new ServerActionParameter(
                    PARAMETER_TYPE.FILEPATH.name(),
                    serverRespath
                            + filePath.replace("/", s));
            ret = super.execute(null, pathParameter);
        }
        if (ret == null) {
            final String message = "Testfile '" + serverRespath + "' not found.";
            LOG.error(message);
            throw new RuntimeException(message);
        }

        return ret;
    }
}
