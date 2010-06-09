/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package Sirius.server.registry.monitor;

import Sirius.server.*;
import Sirius.server.naming.*;
import Sirius.server.newuser.*;
import Sirius.server.registry.*;
import Sirius.server.registry.events.*;

import Sirius.util.*;

import java.awt.*;
import java.awt.event.*;

import java.rmi.*;
import java.rmi.server.*;

import java.util.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

/**
 * Der RegistryMonitor ist ein Frontend fuer die Registry (Sirius.Registry) Der Monitor stellt die Server, die beim der
 * Registry registriert sind, in einer Tabelle dar. Dabei fragt der Monitor die Registry zyklisch ab, um die
 * Anzeigetabellen aktuell zu halten. Das UpdateIntervall ist einstellbar. Ausserdem ist ein manuelles Update jederzeit
 * m\u00F6glich.
 *
 * @author   Bernd Kiefer, Rene Wendling
 * @version  1.0
 *
 *           <p>letzte Aenderung: 24.10.2000</p>
 */
public class RegistryMonitor extends JPanel implements Runnable {

    //~ Instance fields --------------------------------------------------------

    /** Referenz auf den NameServer der Registry.* */
    private NameServer nameServer;

    /** Referenz auf den UserServer der Registry.* */
    private UserServer userServer;

    /** die IP-Adresse der Registry.* */
    private String registryIP;

    /** Die Tabellenueberschriften fuer die Anzeigetabellen Server.* */
    private java.lang.Object[] columnnamesForServer = { "Name", "IP", "Port" };

    /** Die Tabellenueberschriften fuer die Anzeigetabelle User.* */
    private java.lang.Object[] columnnamesForUser = { "ID", "Name", "local Servername", "Usergroup", "Admin" };

    /** umgewandelter Typ: Sirius.Server.Server[] nach java.lang.Object[][] * */
    private Server[] callServer;

    /** umgewandelter Typ: Sirius.Server.Server[] nach java.lang.Object[][] * */
    private Server[] localServer;

    /** umgewandelter Typ: Sirius.Server.Server[] nach java.lang.Object[][] * */
    private Server[] protocolServer;

    /** umgewandelter Typ: Sirius.Server.Server[] nach java.lang.Object[][] * */
    private Server[] translServer;

    /** Tabelle die alle aktiven CallServer anzeigt.* */
    private JTable callServerTable;

    /** Tabelle die alle aktiven LocalServer anzeigt.* */
    private JTable localServerTable;

    /** Tabelle die alle aktiven ProtocolServer anzeigt.* */
    private JTable protocolServerTable;

    /** Tabelle, die alle registrierten TranslationServer anzeigt.* */
    // private JTable translServerTable;

    /** Tabelle, die alle registrierten User anzeigt.* */
    private JTable userTable;

    /** Vector der die User enth\u00E4lt.* */
    private Vector users;

    /** zeigt eventuell auftretende Fehlermeldungen an.* */
    private JLabel messageLabel;

    /** Wert fuer das UpdateIntervall.* */
    private int updateIntervall = 60;

    /** Ueberschrift fuer den CentralServerMonitor.* */
    private String panelHeader;
    // ----------------------------------------------------------------------------------------

    //~ Constructors -----------------------------------------------------------

    /**
     * Konstruktor.
     *
     * @param  registryIP  args[0] die IP-Adresse des CentralServers *
     */
    public RegistryMonitor(final String registryIP) {
        this.registryIP = registryIP;

        // MessageLabel setzen
        messageLabel = new JLabel();

        // ueberschrift fuer den Monitor
        panelHeader = "RegistryMonitor  " + registryIP;

        try {
            // Referenz auf NameServer und UserServer der Registry erzeugen
            nameServer = (NameServer)Naming.lookup("rmi://" + registryIP + "/nameServer");
            userServer = (UserServer)nameServer; // (UserServer) Naming.lookup("rmi://"+registryIP+"/userServer");

            // abfragen der aktiven Server
            callServer = nameServer.getServers(ServerType.CALLSERVER);
            localServer = nameServer.getServers(ServerType.LOCALSERVER);
            protocolServer = nameServer.getServers(ServerType.PROTOCOLSERVER);
            // translServer   = nameServer.getServers(ServerType.TRANSLATIONSERVER);

            // abfragen der aktiven User
            users = new Vector(userServer.getUsers());

            // Layout initialisieren
            initMainPanel();

            // update-Schleife starten
            new Thread(this, "updateThread").start();
        } catch (Exception e) {
            // wenn CentralServer auf IP-Adresse nicht vorhanden, Panel wird trotzdem initialisiert
            messageLabel.setText("There is no SiriusRegistry on " + registryIP);

            initMainPanel();

            // start der update-Schleife
            new Thread(this, "updateThread").start();
        }
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * ----------------------------------------------------------------------------------------
     *
     * @param  intervall  DOCUMENT ME!
     */
    public void setUpdateIntervall(final int intervall) {
        this.updateIntervall = intervall;
    }

    // ----------------------------------------------------------------------------------------

    /**
     * fragt die SiriusRegisty/NameServer nach aktuellen Servern ab. *
     */
    private void update() {
        try {
            // Referenz wird neu angelegt, um zu pr\u00FCfen, ob der SiriusRegistry noch laeuft
            nameServer = (NameServer)Naming.lookup("rmi://" + registryIP + "/nameServer");

            // get all Servers
            callServer = nameServer.getServers(ServerType.CALLSERVER);
            localServer = nameServer.getServers(ServerType.LOCALSERVER);
            protocolServer = nameServer.getServers(ServerType.PROTOCOLSERVER);
            // translServer   = nameServer.getServers(ServerType.TRANSLATIONSERVER);

            users = new Vector(userServer.getUsers());
        } catch (Exception e) {
            messageLabel.setForeground(Color.red);
            message("SiriusRegistry on " + registryIP + " is down or not available!!");
        }
    }

    // ----------------------------------------------------------------------------------------

    /**
     * fragt SiriusRegistry/NameServer nach aktuellen Servern ab und aktualisiert die Tabellen.*
     */
    public void updateTables() {
        update();
        try {
            final MonitorTableModel tmLocalServers = (MonitorTableModel)localServerTable.getModel();
            final MonitorTableModel tmCallServers = (MonitorTableModel)callServerTable.getModel();
            final MonitorTableModel tmProtocolServers = (MonitorTableModel)protocolServerTable.getModel();
            // MonitorTableModel tmTranslServers       = (MonitorTableModel)translServerTable.getModel();
            final MonitorTableModel tmUserServers = (MonitorTableModel)userTable.getModel();

            tmLocalServers.setDataVector(MonitorTableModel.convertToMatrix(localServer), columnnamesForServer);
            tmCallServers.setDataVector(MonitorTableModel.convertToMatrix(callServer), columnnamesForServer);
            tmProtocolServers.setDataVector(MonitorTableModel.convertToMatrix(protocolServer), columnnamesForServer);
            // tmTranslServers.setDataVector(MonitorTableModel.convertToMatrix(translServer),columnnamesForServer);
            tmUserServers.setDataVector(MonitorTableModel.convertToMatrix(users), columnnamesForUser);

            message("last update: " + (new Date(System.currentTimeMillis())));
        } catch (IllegalArgumentException e) {
            localServerTable.setModel(new MonitorTableModel());
            callServerTable.setModel(new MonitorTableModel());
            protocolServerTable.setModel(new MonitorTableModel());
            // translServerTable.setModel(new MonitorTableModel());
            userTable.setModel(new MonitorTableModel());
        }
    }

    // ----------------------------------------------------------------------------------------

    /**
     * Funktion zum initialisieren des Layouts.*
     */
    private void initMainPanel() {
        // es wird versucht die Tabellen zu initialisiern. Ist kein CentralServer vorhanden,
        // k\u00F6nnen die Variablen call-, local- und protocolServer nicht initialisiert werden,
        // Exception wird ausgeworfen. Dann wird ein TableModel ohne Parameter zugewiesen
        try {
            callServerTable = new JTable(
                    new MonitorTableModel(MonitorTableModel.convertToMatrix(callServer), columnnamesForServer));
            localServerTable = new JTable(
                    new MonitorTableModel(MonitorTableModel.convertToMatrix(localServer), columnnamesForServer));
            protocolServerTable = new JTable(
                    new MonitorTableModel(MonitorTableModel.convertToMatrix(protocolServer), columnnamesForServer));
            // translServerTable         = new JTable(new
            // MonitorTableModel(MonitorTableModel.convertToMatrix(translServer),columnnamesForServer));
            userTable = new JTable(new MonitorTableModel(MonitorTableModel.convertToMatrix(users), columnnamesForUser));
        } catch (IllegalArgumentException e) {
            callServerTable = new JTable(new MonitorTableModel());
            localServerTable = new JTable(new MonitorTableModel());
            protocolServerTable = new JTable(new MonitorTableModel());
            // translServerTable   = new JTable (new MonitorTableModel());
            userTable = new JTable(new MonitorTableModel());
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("initmainPanel");
        }

        // JTabbedPane erzeugen und Tabellen hinzufuegen
        final JTabbedPane allServerPane = new JTabbedPane();
        allServerPane.add("LocalServers", new JScrollPane(localServerTable));
        allServerPane.add("CallServers", new JScrollPane(callServerTable));
        // allServerPane.add("TranslationServers", new JScrollPane(translServerTable));
        allServerPane.add("ProtocolServers", new JScrollPane(protocolServerTable));
        allServerPane.add("Users", new JScrollPane(userTable));

        // UpdateButton fuer manuelles Update
        final JButton updateButton = new JButton("update");
        updateButton.addActionListener(new MonitorUpdateListener(this));

        // Panel fuer updateIntervall Einstellungen
        final JPanel timePanel = new JPanel();
        final ButtonGroup buttonGroup = new ButtonGroup();
        final JRadioButton oneMin = (new JRadioButton("all 1 Minute"));
        final JRadioButton fiveMin = new JRadioButton("all 5 Minutes");
        final JRadioButton tenMin = new JRadioButton("all 10 Minutes");
        final MonitorIntervallListener il = new MonitorIntervallListener(this);
        oneMin.addActionListener(il);
        fiveMin.addActionListener(il);
        tenMin.addActionListener(il);
        oneMin.setSelected(true);

        buttonGroup.add(oneMin);
        buttonGroup.add(fiveMin);
        buttonGroup.add(tenMin);

        timePanel.add(oneMin);
        timePanel.add(fiveMin);
        timePanel.add(tenMin);
        timePanel.setBorder(BorderFactory.createTitledBorder("Intervall for automatical update"));

        // MessageLabel und MessagePanel
        final JPanel messagePanel = new JPanel();
        messagePanel.setBorder(BorderFactory.createTitledBorder("Messages"));
        messagePanel.add(messageLabel);

        final JPanel buttonAndMessagePanel = new JPanel();
        buttonAndMessagePanel.setLayout(new BorderLayout());
        buttonAndMessagePanel.add(updateButton, BorderLayout.NORTH);
        buttonAndMessagePanel.add(messagePanel, BorderLayout.CENTER);

        // HauptPaneleistellungen
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder(panelHeader));
        add(timePanel, BorderLayout.NORTH);
        add(allServerPane, BorderLayout.CENTER);
        add(buttonAndMessagePanel, BorderLayout.SOUTH);
    }

    // ----------------------------------------------------------------------------------------
    /**
     * Schleife zum automatischen aktualisieren der Tabellen, Intervall kann ueber Variable updateIntervall gesetzt
     * werden. *
     */
    @Override
    public void run() {
        while (true) {
            try {
                final Thread t = Thread.currentThread();
                messageLabel.setForeground(new Color(102, 102, 153));
                message("last update: " + (new Date(System.currentTimeMillis())));
                t.sleep(updateIntervall * 1000);
                updateTables();
            } catch (InterruptedException e) {
            }
        }
    }
    /**
     * ----------------------------------------------------------------------------------------
     *
     * @param  message  DOCUMENT ME!
     */
    public void message(final String message) {
        messageLabel.setText(message);
    }

    // ----------------------------------------------------------------------------------------

    /**
     * MainFunktion zum testen des CentralServerMonitor.*
     *
     * @param  args  DOCUMENT ME!
     */
    public static void main(final String[] args) {
        // RegistryMonitor monitor = new RegistryMonitor(args[0]);
        final RegistryMonitor monitor = new RegistryMonitor("192.168.0.1");
        final JFrame frame = new JFrame("RegistryMonitor");

        frame.getContentPane().add(monitor);
        frame.setSize(400, 400);
        frame.setVisible(true);
    }
}
