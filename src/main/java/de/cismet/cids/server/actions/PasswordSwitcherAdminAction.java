/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cismet.cids.server.actions;

import Sirius.server.middleware.impls.domainserver.DomainServerImpl;
import Sirius.server.newuser.User;
import Sirius.server.sql.DialectProvider;
import Sirius.server.sql.SQLTools;
import Sirius.server.sql.ServerSQLStatements;

import org.apache.log4j.Logger;

import org.openide.util.Exceptions;
import org.openide.util.Lookup;

import java.rmi.RemoteException;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import de.cismet.connectioncontext.ConnectionContext;
import de.cismet.connectioncontext.ConnectionContextStore;

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
@org.openide.util.lookup.ServiceProvider(service = ServerAction.class)
public class PasswordSwitcherAdminAction implements UserAwareServerAction, ConnectionContextStore {

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

    private ConnectionContext connectionContext = ConnectionContext.createDummy();

    private User user;

    //~ Methods ----------------------------------------------------------------

    @Override
    public ConnectionContext getConnectionContext() {
        return connectionContext;
    }

    @Override
    public void initWithConnectionContext(final ConnectionContext connectionContext) {
        this.connectionContext = connectionContext;
    }

    @Override
    public Object execute(final Object body, final ServerActionParameter... params) {
        String loginNameToSwitch = null;
        Integer recoveryTimer = null;

        for (final ServerActionParameter param : params) {
            if (ParameterType.LOGIN_NAME.toString().equals(param.getKey().toString())) {
                loginNameToSwitch = (String)param.getValue();
            } else if (ParameterType.RECOVERY_TIMER.toString().equals(param.getKey().toString())) {
                recoveryTimer = (Integer)param.getValue();
            }
        }

        if (loginNameToSwitch == null) {
            return new Exception("Parameter '" + ParameterType.LOGIN_NAME.toString() + "' not set");
        }
        if (recoveryTimer == null) {
            return new Exception("Parameter '" + ParameterType.RECOVERY_TIMER.toString() + "' not set");
        }

        try {
            if (
                !DomainServerImpl.getServerInstance().hasConfigAttr(
                            getUser(),
                            "csa://passwordSwitcher",
                            getConnectionContext())) {
                return new Exception("Missing permission. Not allowed to switch passwords !");
            }
        } catch (final Exception ex) {
            return new Exception("Error while checking permissings !", ex);
        }
        try {
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
                return new Exception("User '" + loginNameToSwitch + "' not found!");
            }
            rs = s.executeQuery(stmts.getPasswordSwitcherAdminActionSelectUserStmt(adminLogin));
            if (!rs.next()) {
                return new Exception("User '" + adminLogin + "' not found!");
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
            return new Exception(ex.getMessage(), ex);
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
}
