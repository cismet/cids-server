/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.server.actions;

import Sirius.server.middleware.impls.domainserver.DomainServerImpl;
import Sirius.server.newuser.User;
import Sirius.server.sql.DialectProvider;
import Sirius.server.sql.SQLTools;
import Sirius.server.sql.ServerSQLStatements;

import org.apache.log4j.Logger;

import org.openide.util.Lookup;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.LinkedList;
import java.util.List;

import de.cismet.cidsx.base.types.MediaTypes;
import de.cismet.cidsx.base.types.Type;

import de.cismet.cidsx.server.actions.RestApiCidsServerAction;
import de.cismet.cidsx.server.api.types.ActionInfo;
import de.cismet.cidsx.server.api.types.ActionParameterInfo;
import de.cismet.cidsx.server.api.types.GenericResourceWithContentType;

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
@org.openide.util.lookup.ServiceProvider(service = RestApiCidsServerAction.class)
public class PasswordSwitcherAdminAction implements UserAwareServerAction, RestApiCidsServerAction {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(PasswordSwitcherAdminAction.class);

    public static final String TASK_NAME = "passwordSwitcherAdminAction";

    //~ Enums ------------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    public enum ParameterType {

        //~ Enum constants -----------------------------------------------------

        LOGIN_NAME, RECOVERY_TIMER
    }

    //~ Instance fields --------------------------------------------------------

    protected final ActionInfo actionInfo;

    private User user;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new PasswordSwitcherAdminAction object.
     */
    public PasswordSwitcherAdminAction() {
        actionInfo = new ActionInfo();
        actionInfo.setName("Password Switcher Admin Action");
        actionInfo.setActionKey(TASK_NAME);
        actionInfo.setDescription("Password Switcher Admin Action");

        final List<ActionParameterInfo> parameterDescriptions = new LinkedList<ActionParameterInfo>();
        ActionParameterInfo parameterDescription = new ActionParameterInfo();
        parameterDescription.setKey(ParameterType.LOGIN_NAME.name());
        parameterDescription.setType(Type.STRING);
        parameterDescription.setDescription("Name of the user");
        parameterDescriptions.add(parameterDescription);

        parameterDescription = new ActionParameterInfo();
        parameterDescription.setKey(ParameterType.RECOVERY_TIMER.name());
        parameterDescription.setType(Type.INTEGER);
        parameterDescription.setDescription("Recovery Timer");
        parameterDescriptions.add(parameterDescription);

        actionInfo.setParameterDescription(parameterDescriptions);

        final ActionParameterInfo returnDescription = new ActionParameterInfo();
        returnDescription.setKey("return");
        returnDescription.setType(Type.JAVA_SERIALIZABLE);
        returnDescription.setMediaType(MediaTypes.APPLICATION_X_JAVA_SERIALIZED_OBJECT);
        returnDescription.setDescription("Returns only exceptions.");
        actionInfo.setResultDescription(returnDescription);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public GenericResourceWithContentType execute(final Object body, final ServerActionParameter... params) {
        String loginNameToSwitch = null;
        Integer recoveryTimer = null;

        for (final ServerActionParameter param : params) {
            final Object paramValue = param.getValue();

            if (ParameterType.LOGIN_NAME.name().equalsIgnoreCase(param.getKey())) {
                loginNameToSwitch = paramValue.toString();
            } else if (ParameterType.RECOVERY_TIMER.name().equalsIgnoreCase(param.getKey())) {
                if (int.class.isAssignableFrom(paramValue.getClass())
                            || Integer.class.isAssignableFrom(paramValue.getClass())) {
                    recoveryTimer = (Integer)paramValue;
                } else {
                    recoveryTimer = Integer.parseInt(paramValue.toString());
                }
            } else {
                LOG.warn("unsupported server action parameter '" + param.getKey()
                            + "' = " + paramValue);
            }
        }

        if (loginNameToSwitch == null) {
            final Exception exception = new Exception("Parameter '" + ParameterType.LOGIN_NAME.toString()
                            + "' not set");
            LOG.error(exception.getMessage());
            return new GenericResourceWithContentType(MediaTypes.APPLICATION_X_JAVA_SERIALIZED_OBJECT, exception);
        }

        if (recoveryTimer == null) {
            final Exception exception = new Exception("Parameter '" + ParameterType.RECOVERY_TIMER.toString()
                            + "' not set");
            LOG.error(exception.getMessage());
            return new GenericResourceWithContentType(MediaTypes.APPLICATION_X_JAVA_SERIALIZED_OBJECT, exception);
        }

        try {
            if (!getUser().isAdmin()) {
                final Exception exception = new Exception("Only admin Users are allowed to switch passwords !");
                LOG.error(exception.getMessage());
                return new GenericResourceWithContentType(MediaTypes.APPLICATION_X_JAVA_SERIALIZED_OBJECT, exception);
            }

            final Statement s = DomainServerImpl.getServerInstance()
                        .getConnectionPool()
                        .getDBConnection()
                        .getConnection()
                        .createStatement();

            final String adminLogin = getUser().getName();

            final ServerSQLStatements stmts = SQLTools.getStatements(Lookup.getDefault().lookup(DialectProvider.class)
                            .getDialect());

            ResultSet rs = s.executeQuery(stmts.getPasswordSwitcherAdminActionSelectUserStmt(loginNameToSwitch));
            if (!rs.next()) {
                final Exception exception = new Exception("User '" + loginNameToSwitch + "' not found!");
                LOG.error(exception.getMessage());
                return new GenericResourceWithContentType(MediaTypes.APPLICATION_X_JAVA_SERIALIZED_OBJECT, exception);
            }
            rs = s.executeQuery(stmts.getPasswordSwitcherAdminActionSelectUserStmt(adminLogin));
            if (!rs.next()) {
                final Exception exception = new Exception("User '" + adminLogin + "' not found!");
                LOG.error(exception.getMessage());
                return new GenericResourceWithContentType(MediaTypes.APPLICATION_X_JAVA_SERIALIZED_OBJECT, exception);
            }

            s.executeUpdate(stmts.getPasswordSwitcherAdminActionChangeAndBackupStmt(loginNameToSwitch, adminLogin));
            try {
                Thread.sleep(recoveryTimer);
            } catch (final InterruptedException ex) {
                LOG.error("Insomnia ! I can't sleep", ex);
            }
            s.executeUpdate(stmts.getPasswordSwitcherAdminActionRecoveryStmt(loginNameToSwitch));

            return null;
        } catch (final SQLException ex) {
            LOG.error("error while executing sql statement", ex);
            return new GenericResourceWithContentType(MediaTypes.APPLICATION_X_JAVA_SERIALIZED_OBJECT, ex);
        }
    }

    @Override
    public String getTaskName() {
        return TASK_NAME;
    }

    @Override
    public User getUser() {
        return user;
    }

    @Override
    public void setUser(final User user) {
        this.user = user;
    }

    @Override
    public ActionInfo getActionInfo() {
        return this.actionInfo;
    }
}
