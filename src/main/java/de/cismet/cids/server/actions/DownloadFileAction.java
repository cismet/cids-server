/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.server.actions;

import Sirius.server.middleware.interfaces.domainserver.MetaService;
import Sirius.server.middleware.interfaces.domainserver.MetaServiceStore;

import org.apache.log4j.Logger;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.LinkedList;
import java.util.List;

import javax.ws.rs.core.MediaType;

import de.cismet.cidsx.base.types.Type;

import de.cismet.cidsx.server.actions.RestApiCidsServerAction;
import de.cismet.cidsx.server.api.types.ActionInfo;
import de.cismet.cidsx.server.api.types.ActionParameterInfo;
import de.cismet.cidsx.server.api.types.GenericResourceWithContentType;
/**
 * DOCUMENT ME!
 *
 * @author   jruiz, Pascal Dihé
 * @version  $Revision$, $Date$
 */
@org.openide.util.lookup.ServiceProvider(service = RestApiCidsServerAction.class)
public class DownloadFileAction implements RestApiCidsServerAction, MetaServiceStore {

    //~ Static fields/initializers ---------------------------------------------

    protected static final Logger LOG = Logger.getLogger(DownloadFileAction.class);

    public static final String TASK_NAME = "downloadFile";

    //~ Enums ------------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    public enum PARAMETER_TYPE {

        //~ Enum constants -----------------------------------------------------

        FILEPATH
    }

    //~ Instance fields --------------------------------------------------------

    protected final ActionInfo actionInfo;

    MetaService ms;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new DownloadFileAction object.
     */
    public DownloadFileAction() {
        actionInfo = new ActionInfo();
        actionInfo.setName("Downlad File");
        actionInfo.setActionKey(TASK_NAME);
        actionInfo.setDescription("Downloads a remote file from the server.");

        final List<ActionParameterInfo> parameterDescriptions = new LinkedList<ActionParameterInfo>();
        final ActionParameterInfo pathParameterDescription = new ActionParameterInfo();
        pathParameterDescription.setKey(PARAMETER_TYPE.FILEPATH.name());
        pathParameterDescription.setType(Type.STRING);
        pathParameterDescription.setDescription("Absolute local path of the file to be downloaded");
        parameterDescriptions.add(pathParameterDescription);
        actionInfo.setParameterDescription(parameterDescriptions);

        final ActionParameterInfo bodyDescription = new ActionParameterInfo();
        bodyDescription.setKey("body");
        bodyDescription.setType(Type.STRING);
        bodyDescription.setMediaType(MediaType.TEXT_PLAIN);
        bodyDescription.setDescription("Deprecated body parameter, use server action parameter 'filename' instead!");
        actionInfo.setBodyDescription(bodyDescription);

        final ActionParameterInfo returnDescription = new ActionParameterInfo();
        returnDescription.setKey("return");
        returnDescription.setType(Type.BYTE);
        returnDescription.setMediaType(MediaType.WILDCARD);
        returnDescription.setDescription("Return value is the file byte stream");
        actionInfo.setResultDescription(returnDescription);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public String getTaskName() {
        return TASK_NAME;
    }

    @Override
    public GenericResourceWithContentType<byte[]> execute(
            final Object body,
            final ServerActionParameter... params) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("executing '" + this.getTaskName() + "' with "
                        + params.length + " server action parameters and body object: " + (body != null));
        }
        try {
            Path path = null;
            if (params.length > 0) {
                for (final ServerActionParameter sap : params) {
                    if (sap.getKey().equalsIgnoreCase(PARAMETER_TYPE.FILEPATH.name())) {
                        path = Paths.get(sap.getValue().toString());
                        break;
                    } else {
                        LOG.warn("unsupported server action parameter:" + sap.toString());
                    }
                }
            }

            if (path == null) {
                LOG.warn("client did not provide ServerActionParameter '" + PARAMETER_TYPE.FILEPATH.name() + "'");
                if (body != null) {
                    path = Paths.get(body.toString());
                    LOG.warn("client provided '" + PARAMETER_TYPE.FILEPATH.name() + "' as body parameter!");
                }
            }

            if (path == null) {
                final String message =
                    "Could  not download file, client did not provide a valid path (parameter FILEPATH)";
                LOG.error(message);
                throw new RuntimeException(message);
            } else if (!path.toFile().canRead()) {
                final String message = "Cannot read file at specified path '" + path + "'";
                LOG.error(message);
                throw new RuntimeException(message);
            }

            String contentType = Files.probeContentType(path);
            if (contentType == null) {
                LOG.warn("Content type of file at specified path '" + path + "' could not be determined!");
            }
            contentType = (contentType != null) ? contentType : MediaType.APPLICATION_OCTET_STREAM;

            // TODO: check requested content type in ActionInfo
            LOG.info("reading file at specified path '" + path + "' with content type '" + contentType + "'");

            // FIXME: not very efficient for large files -> out of memory!
            // return output stream instead?!
            final byte[] fileContent = Files.readAllBytes(path);

            LOG.info("File '" + path + "' of type '" + contentType + "' with "
                        + fileContent.length + " bytes loaded");

            final GenericResourceWithContentType ressource = new GenericResourceWithContentType(
                    contentType,
                    fileContent);

            return ressource;
        } catch (Throwable ex) {
            final String message = "Could  not download file: " + ex.getMessage();
            LOG.error(message, ex);
            throw new RuntimeException(message, ex);
            // return null;
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

    @Override
    public ActionInfo getActionInfo() {
        return this.actionInfo;
    }
}
