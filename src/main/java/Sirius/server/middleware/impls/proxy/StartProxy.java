/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package Sirius.server.middleware.impls.proxy;

import java.rmi.*;
import java.rmi.server.*;
import java.rmi.registry.*;

import javax.swing.*;

import java.awt.event.*;
import java.awt.*;

import java.net.*;

import java.util.*;

import Sirius.server.registry.*;
import Sirius.server.property.*;
import Sirius.server.middleware.interfaces.proxy.*;
import Sirius.server.middleware.impls.proxy.*;
import Sirius.server.observ.*;
import Sirius.server.*;

import org.apache.log4j.*;

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
public class StartProxy {

    //~ Static fields/initializers ---------------------------------------------

    protected static StartProxy THIS;

    //~ Instance fields --------------------------------------------------------

    private final transient org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(this.getClass());

    private SystemService systemServer;
    private UserService userServer;
    private MetaService metaServer;
    private ProxyImpl callServer;

    private String siriusRegistryIP;

    // private JFrame frame;
    // private JTextArea textArea;
    private ServerProperties properties;
    private Server serverInfo;
    private ServerStatus status;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new StartProxy object.
     *
     * @param   configFile  DOCUMENT ME!
     *
     * @throws  Throwable        DOCUMENT ME!
     * @throws  ServerExitError  DOCUMENT ME!
     */
    public StartProxy(String configFile) throws Throwable {
        String[] ips = { "localhost" };   // NOI18N
        String rmiPort = "1099";   // NOI18N

        properties = new ServerProperties(configFile);

        String fileName;
        if (((fileName = properties.getLog4jPropertyFile()) != null) && !fileName.equals("")) {   // NOI18N
            PropertyConfigurator.configure(fileName);
        }

        try {
            ips = properties.getRegistryIps();
        } catch (MissingResourceException mre) {
            System.err.println(
                "<CS> Value for Key " + mre.getMessage() + " in ConfigFile " + configFile + " is missing");   // NOI18N
            logger.error(
                "<CS> Value for Key " + mre.getMessage() + " in ConfigFile " + configFile + " is missing",   // NOI18N
                mre);

            throw new ServerExitError(mre);
        }

        this.siriusRegistryIP = ips[0];

        System.out.println("<CS> INFO siriusRegistryIP:: " + siriusRegistryIP);   // NOI18N
        logger.info("<CS> INFO siriusRegistryIP:: " + siriusRegistryIP);   // NOI18N
        System.out.println("<CS> INFO configFile:: " + configFile);   // NOI18N
        logger.info("<CS> INFO configFile:: " + configFile);   // NOI18N

        try {
            rmiPort = properties.getRMIRegistryPort();
            serverInfo = new Server(
                    ServerType.CALLSERVER,
                    properties.getServerName(),
                    InetAddress.getLocalHost().getHostAddress(),
                    rmiPort);
        } catch (MissingResourceException mre) {
            System.err.println(
                "Info <CS> Value for Key " + mre.getMessage() + " in ConfigFile " + configFile + " is missing");   // NOI18N
            logger.error(
                "Info <CS> Value for Key " + mre.getMessage() + " in ConfigFile " + configFile + " is missing",   // NOI18N
                mre);

            System.out.println("Info <CS> Set Default RMIPort to 1099");   // NOI18N
            if (logger.isDebugEnabled()) {
                logger.debug("Info <CS> Set Default RMIPort to 1099");   // NOI18N
            }

            rmiPort = "1099";   // NOI18N
            serverInfo = new Server(
                    ServerType.CALLSERVER,
                    properties.getServerName(),
                    InetAddress.getLocalHost().getHostAddress(),
                    rmiPort);
        }

        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new RMISecurityManager());
        }

        // abfragen, ob schon eine  RMI Registry exitiert.
        java.rmi.registry.Registry rmiRegistry;

        try {
            if (logger.isDebugEnabled()) {
                logger.debug("Info <CS> getRMIRegistry on Port " + rmiPort);   // NOI18N
            }
            rmiRegistry = LocateRegistry.getRegistry(new Integer(rmiPort).intValue());
            // wenn keine Registry vorhanden, wird an dieser Stelle Exception ausgeloest
        } catch (RemoteException e) {
            // wenn nicht, neue Registry starten und auf portnummer setzen
            System.err.println(e.getMessage() + " \n" + "Info <CS> no RMIRegistry on Port " + rmiPort + " available");   // NOI18N
            logger.error("Info <CS> no RMIRegistry on Port " + rmiPort + " available", e);   // NOI18N
            System.out.println("Info <CS> create RMIRegistry on Port " + rmiPort);   // NOI18N
            logger.info("Info <CS> create RMIRegistry on Port " + rmiPort);   // NOI18N

            rmiRegistry = LocateRegistry.createRegistry(new Integer(rmiPort).intValue());
        }

        callServer = new ProxyImpl(properties);

        Naming.bind("callServer", callServer);   // NOI18N

        String[] list = rmiRegistry.list();
        int number = rmiRegistry.list().length;
        if (logger.isDebugEnabled()) {
            logger.debug("<CS> RMIRegistry does exist...");   // NOI18N
            logger.debug(" Info <CS> Already registered with RMIRegistry:");   // NOI18N
        }

        if (logger.isDebugEnabled()) {
            String t = "";   // NOI18N
            for (int i = 0; i < number; i++) {
                t += ("\t" + list[i]);   // NOI18N
            }

            logger.debug(t);
        }

        // ------------------------------------------

        // initFrame();
        status = new ServerStatus();
        THIS = this;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static final StartProxy getServerInstance() {
        return THIS;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public ServerStatus getStatus() {
        return status;
    }

    /**
     * public void initFrame() throws java.net.UnknownHostException { textArea = new JTextArea("siriusRegistryIP:
     * "+siriusRegistryIP+"\nobserves SiriusRegistry for new LocalServers..."); JButton shutdownButton = new
     * JButton("shutdown"); shutdownButton.addActionListener(new ShutdownListener()); frame = new JFrame("CallServer:
     * "+InetAddress.getLocalHost().getHostAddress()+":"+properties.getServerPort()); frame.getContentPane().add(new
     * JScrollPane(textArea), BorderLayout.CENTER); frame.getContentPane().add(shutdownButton, BorderLayout.SOUTH);
     * frame.setSize(300,300); frame.setVisible(true); }
     *
     * @param   args  DOCUMENT ME!
     *
     * @throws  Throwable        DOCUMENT ME!
     * @throws  ServerExitError  DOCUMENT ME!
     */
    public static void main(String[] args) throws Throwable {
        if (args == null) {
            throw new ServerExitError("args == null no commandline parameters given(Configfile / port)");   // NOI18N
        } else if (args.length < 1) {
            throw new ServerExitError("insuffitient arguments");   // NOI18N
        }

        try {
            new StartProxy(args[0]);
        } catch (ServerExitError e) {
            e.printStackTrace();
            throw e;
        } catch (AlreadyBoundException e) {
            Naming.unbind("callServer");   // NOI18N
            throw new ServerExitError(e);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e);

            throw new ServerExitError(e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @throws  Throwable        DOCUMENT ME!
     * @throws  ServerExitError  DOCUMENT ME!
     */
    public void shutdown() throws Throwable {
        try {
            callServer.unregisterAsObserver(siriusRegistryIP);
            callServer.nameServer.unregisterServer(
                ServerType.CALLSERVER,
                serverInfo.getName(),
                serverInfo.getIP(),
                serverInfo.getRMIPort());

            Naming.unbind("callServer");   // NOI18N

            throw new ServerExit("Server exited regularly");   // NOI18N
        } catch (Exception e) {
            throw new ServerExitError(e);
        }
    }
}
