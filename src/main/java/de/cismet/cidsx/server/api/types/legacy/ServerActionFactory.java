/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cidsx.server.api.types.legacy;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ClassUtils;
import org.apache.log4j.Logger;

import org.openide.util.Lookup;

import java.io.InputStream;
import java.io.ObjectInputStream;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import javax.ws.rs.core.MediaType;

import de.cismet.cids.server.actions.ServerAction;
import de.cismet.cids.server.actions.ServerActionParameter;

import de.cismet.cidsx.base.types.MediaTypes;
import de.cismet.cidsx.base.types.Type;

import de.cismet.cidsx.server.actions.RestApiCidsServerAction;
import de.cismet.cidsx.server.api.types.ActionInfo;
import de.cismet.cidsx.server.api.types.ActionTask;
import de.cismet.cidsx.server.api.types.ParameterInfo;

/**
 * Helper Methods for dealing with ServerAction and and ActionInfo.
 *
 * @author   Pascal Dihé
 * @version  $Revision$, $Date$
 */
public class ServerActionFactory {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(ServerActionFactory.class);

    private static ServerActionFactory factory = null;

    private static final ObjectMapper MAPPER = new ObjectMapper(new JsonFactory());

    //~ Instance fields --------------------------------------------------------

    private final HashMap<String, Class<? extends ServerAction>> serverActionClassMap =
        new HashMap<String, Class<? extends ServerAction>>();
    private final HashMap<String, ServerAction> serverActionMap = new HashMap<String, ServerAction>();
    private final HashMap<String, ActionInfo> serverActionInfoMap = new HashMap<String, ActionInfo>();

    private boolean cacheFilled = false;

    private final ParameterInfo defaultBodyDescription;
    private final ParameterInfo defaultReturnDescription;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new ServerActionFactory object.
     */
    private ServerActionFactory() {
        defaultBodyDescription = new ParameterInfo();
        defaultBodyDescription.setKey("body");
        defaultBodyDescription.setDescription("Body Part Parameter (usually a file) of the Server Action.");
        defaultBodyDescription.setType(Type.JAVA_SERIALIZABLE);
        defaultBodyDescription.setMediaType(MediaTypes.APPLICATION_X_JAVA_SERIALIZED_OBJECT);
        defaultBodyDescription.setArray(false);

        defaultReturnDescription = new ParameterInfo();
        defaultBodyDescription.setDescription("Return value of the Server Action.");
        defaultReturnDescription.setKey("return");
        defaultReturnDescription.setType(Type.JAVA_SERIALIZABLE);
        defaultReturnDescription.setMediaType(MediaTypes.APPLICATION_X_JAVA_SERIALIZED_OBJECT);
        defaultReturnDescription.setArray(false);

        this.fillCache();
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static final ServerActionFactory getFactory() {
        if (factory == null) {
            factory = new ServerActionFactory();
        }

        return factory;
    }

    /**
     * Inspects a ServerAction Instance and tries to automatically derive a proper ActionInfo object.
     *
     * @param   cidsServerAction  server action to be inspected
     *
     * @return  action info or null if the inspection fails
     */
    public ActionInfo actionInfoFromServerAction(final ServerAction cidsServerAction) {
        try {
            final Class<? extends ServerAction> serverActionClass = cidsServerAction.getClass();
            LOG.info("processing ServerAction '" + serverActionClass.getName() + "'");

            final ActionInfo actionInfo = new ActionInfo();
            // FIXME: actionKey should  be the class name!
            // final String actionKey = serverActionClass.getName();
            final String actionKey = cidsServerAction.getTaskName();
            final String name = cidsServerAction.getTaskName();
            final String description = "Cids Server Action '" + name
                        + "'. Attention: ActionInfo generated automatically, no "
                        + "information about parameter and return types available!";

            // that's all. No information about parameters at classs level available!
            final LinkedList<ParameterInfo> actionParameterInfos = new LinkedList<ParameterInfo>();

            final ParameterInfo bodyParameterInfo = this.getDefaultBodyDescription();
            final ParameterInfo returnParameterInfo = this.getDefaultReturnDescription();

            actionInfo.setActionKey(actionKey);
            actionInfo.setName(serverActionClass.getSimpleName());
            actionInfo.setDescription(description);
            actionInfo.setParameterDescription(actionParameterInfos);
            actionInfo.setBodyDescription(bodyParameterInfo);
            actionInfo.setResultDescription(returnParameterInfo);

            return actionInfo;
        } catch (Throwable t) {
            LOG.error("could not inspect ServerAction '"
                        + cidsServerAction.getClass().getName() + "': " + t.getMessage(),
                t);
        }

        return null;
    }

    /**
     * Lookups all available Server Actions, collects the respective Action Infos and adds it to the cache.
     */
    private void fillCache() {
        if (cacheFilled) {
            LOG.warn("ServerActionCache already filled");
        }

        final Collection<? extends RestApiCidsServerAction> lookupableServerActions = Lookup.getDefault()
                    .lookupAll(RestApiCidsServerAction.class);
        final Collection<? extends ServerAction> cidsServerActions = Lookup.getDefault().lookupAll(ServerAction.class);

        LOG.info("loading " + lookupableServerActions.size() + " Lookupable Server Action and trying to inspect "
                    + cidsServerActions.size() + " cids Server Actions");

        // process the actions that provide action info
        for (final RestApiCidsServerAction lookupableServerAction : lookupableServerActions) {
            final ActionInfo actionInfo = lookupableServerAction.getActionInfo();
            final Class serverActionClass = lookupableServerAction.getClass();
            final String actionKey = actionInfo.getActionKey();
            if (LOG.isDebugEnabled()) {
                LOG.debug("adding Lookupable Server Action '" + actionKey + "'");
            }
            this.serverActionMap.put(actionKey, lookupableServerAction);
            this.serverActionClassMap.put(actionKey, serverActionClass);
            this.serverActionInfoMap.put(actionKey, new ActionTask(actionInfo));
        }

        // procress the legacy actions
        for (final ServerAction cidsServerAction : cidsServerActions) {
            // FIXME: should use the class name as action key!
            // final String actionKey = cidsServerAction.getClass().getName();
            final String actionKey = cidsServerAction.getTaskName();
            if (!this.serverActionInfoMap.containsKey(actionKey)) {
                final ActionInfo actionInfo = this.actionInfoFromServerAction(cidsServerAction);
                if (actionInfo != null) {
                    final Class serverActionClass = cidsServerAction.getClass();
                    if (!actionKey.equals(actionInfo.getActionKey())) {
                        LOG.warn("action key missmatch: '" + actionKey
                                    + "' != '" + actionInfo.getActionKey() + "'!");
                    }

                    // FIXME: should check against the class name
                    if (!this.serverActionInfoMap.containsKey(cidsServerAction.getTaskName())) {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("adding Cids Server Action '" + actionKey + "'");
                        }
                        this.serverActionMap.put(actionKey, cidsServerAction);
                        this.serverActionClassMap.put(actionKey, serverActionClass);
                        this.serverActionInfoMap.put(actionKey, new ActionTask(actionInfo));
                    } else {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("Cids Server Action '" + actionKey
                                        + "' already registered by Lookupable Server Action.");
                        }
                    }
                }
            } else {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Cids Server Action '" + cidsServerAction.getClass().getName()
                                + "' already registered by Lookupable Server Action.");
                }
            }
        }

        cacheFilled = true;
    }

    /**
     * Tries to find a cached ActionInfo object of the specified cids server action.
     *
     * @param   actionKey  the action key (e.g.java class name) of the action
     *
     * @return  ActionInfo instance or null
     */
    public ActionInfo getServerActionInfo(final String actionKey) {
        if (!this.serverActionInfoMap.containsKey(actionKey)) {
            LOG.warn("could not find ActionInfo for action key '" + actionKey + "'");
        }

        return this.serverActionInfoMap.get(actionKey);
    }

    /**
     * Tries to find a cached ActionInfo object of the specified cids server action.
     *
     * @param   actionKey  the action key (e.g.java class name) of the action
     *
     * @return  ActionInfo instance or null
     */
    public ServerAction getServerAction(final String actionKey) {
        if (!this.serverActionMap.containsKey(actionKey)) {
            LOG.warn("could not find ServerAction for action key '" + actionKey + "'");
        }

        return this.serverActionMap.get(actionKey);
    }

    /**
     * Returns all cached ServerActionInfos.
     *
     * @return  ServerActionInfo Collection
     */
    public List<ActionInfo> getServerActionInfos() {
        return new LinkedList<ActionInfo>(this.serverActionInfoMap.values());
    }

    /**
     * Returns all cached ServerActionInfos.
     *
     * @return  ServerActionInfo Collection
     */
    public List<ServerAction> getServerActions() {
        return new LinkedList<ServerAction>(this.serverActionMap.values());
    }

    /**
     * Tries to find a cached ServerAction class for the specified cids server action key.
     *
     * @param   actionKey  key (e.g. class name) of the server action
     *
     * @return  ServerAction Class or null
     */
    public Class<? extends ServerAction> getServerActionClass(final String actionKey) {
        if (!this.serverActionClassMap.containsKey(actionKey)) {
            LOG.warn("could not find legacy action java class for action key '" + actionKey + "'");
        }

        return this.serverActionClassMap.get(actionKey);
    }

    /**
     * Populates an instance of a ServerAction with parameters from the actionParameters object.
     *
     * @param   actionTask  DOCUMENT ME!
     *
     * @return  ServerAction with parameters
     */
    public ServerActionParameter[] ServerActionParametersFromActionTask(
            final ActionTask actionTask) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getting parameters from cids server action '" + actionTask.getActionKey() + "'");
        }

        if ((actionTask.getParameters() == null) || actionTask.getParameters().isEmpty()) {
            LOG.warn("no action parameters available for action '" + actionTask.getActionKey() + "'");
            return new ServerActionParameter[0];
        }

        final ServerActionParameter[] actionParameters = ServerActionParameter.fromMap(actionTask.getParameters());
        if (LOG.isDebugEnabled()) {
            LOG.debug(actionParameters.length + " action parameters found for cids server action '"
                        + actionTask.getActionKey() + "'");
        }
        if ((actionTask.getParameterDescription() != null) && !actionTask.getParameterDescription().isEmpty()) {
            for (int i = 0; i < actionParameters.length; i++) {
                final ServerActionParameter actionParameter = actionParameters[i];
                final ParameterInfo parameterInfo = actionTask.getActionParameterInfo(actionParameter.getKey());
                if ((parameterInfo != null) && (parameterInfo.getType() == Type.JAVA_SERIALIZABLE)) {
                    if (parameterInfo.getType() == Type.JAVA_SERIALIZABLE) {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("deserializing binary java object parameter '" + parameterInfo.getKey() + "'");
                        }
                        try {
                            final Object paramValue = ServerSearchFactory.fromBase64String(actionParameter.getValue()
                                            .toString());
                            actionParameters[i] = new ServerActionParameter(actionParameter.getKey(), paramValue);
                        } catch (Throwable t) {
                            LOG.warn("could not deserialize binary java object parameter '" + parameterInfo.getKey()
                                        + "': " + t.getMessage(),
                                t);
                        }
                    }
                }
            }
        }
        return actionParameters;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   fileAttachement  DOCUMENT ME!
     * @param   bodyDescription  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public Object bodyObjectFromFileAttachment(final InputStream fileAttachement, final ParameterInfo bodyDescription)
            throws Exception {
        final Object body;

        if (bodyDescription.getMediaType().toLowerCase().equalsIgnoreCase(
                        MediaTypes.APPLICATION_X_JAVA_SERIALIZED_OBJECT)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("deserializing binary java object attachment");
            }
            final ObjectInputStream ois = new ObjectInputStream(fileAttachement);
            body = ois.readObject();
            if (LOG.isDebugEnabled()) {
                LOG.debug("successfully restored java object '" + body.getClass() + "' from binary input file");
            }
        } else if (bodyDescription.getMediaType().equalsIgnoreCase(MediaType.APPLICATION_JSON)) {
            Class javaClass = Object.class;
            try {
                if ((bodyDescription.getType() != null) && (bodyDescription.getAdditionalTypeInfo() != null)) {
                    javaClass = ClassUtils.getClass(bodyDescription.getAdditionalTypeInfo());
                }
            } catch (ClassNotFoundException cne) {
                LOG.warn("could not find java class for type '" + bodyDescription.getAdditionalTypeInfo() + "'", cne);
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug("deserializing JSON attachment to Java Object '" + javaClass.getSimpleName() + "'");
            }
            body = MAPPER.readValue(fileAttachement, javaClass);
        } else if (bodyDescription.getMediaType().toLowerCase().contains("text")) {
            body = IOUtils.toString(fileAttachement, "UTF-8");
            if (LOG.isDebugEnabled()) {
                LOG.debug("deserializing plain text attachment to String: '" + body + "'");
            }
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug("deserializing byte array attachment from '" + bodyDescription.getMediaType() + "' resource");
            }
            body = IOUtils.toByteArray(fileAttachement);
            if (body != null) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("successfully read byte array of length " + ((byte[])body).length);
                }
            } else {
                if (LOG.isDebugEnabled()) {
                    LOG.warn("no binary body (e.g. file) parameter provided!");
                }
            }
        }

        return body;
    }

    /**
     * Generates a default body description, if neither the action implementation nor the client provide information
     * about the content type of the body part.<br>
     * The Default content type if none is specified is application/x-java-object (binary serialized java object).
     *
     * @return  DefaultBodyDescriptio
     */
    public ParameterInfo getDefaultBodyDescription() {
        return defaultBodyDescription;
    }

    /**
     * Generates a default return description, if the action implementation does not provide information about the
     * content type of it's return value.<br>
     * The Default content type if none is specified is application/x-java-object (binary serialized java object).
     *
     * @return  DefaultReturnDescription
     */
    public ParameterInfo getDefaultReturnDescription() {
        return defaultReturnDescription;
    }
}
