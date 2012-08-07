/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
//------------------------------------------------------------------------------
//
// Project   : NamingServerMonitor
// File name : JamingServerMonitor.java
// Author    : Rene Wendling
// Date      : 24.10.2000
//
//------------------------------------------------------------------------------
//
package Sirius.util.NamingServerMonitor;

import java.awt.*;
import java.awt.event.*;

import java.rmi.*;
import java.rmi.registry.*;
import java.rmi.server.*;

import java.util.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

//import NamingServerMonitor.*;

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
public class NamingServerMonitor extends JPanel implements Runnable, ActionListener {

    //~ Static fields/initializers ---------------------------------------------

    public static String host;

    //~ Instance fields --------------------------------------------------------

    public String[] bounds;

    public NamingServerMonitor monitor;
    /** Referenz auf den PortScan.* */
    private PortScan portscan;

    private java.lang.Object[] columnnamesForBounds = {
            org.openide.util.NbBundle.getMessage(NamingServerMonitor.class, "NamingServerMonitor.columnnamesForBounds")
        }; // NOI18N
    /** Tabelle.* */
    private JTable boundsTable;

    /** zeigt eventuell auftretende Fehlermeldungen an.* */
    private JLabel messageLabel;

    /** Wert fuer das UpdateIntervall.* */
    private int updateIntervall = 60;

    /** Ueberschrift fuer den CentralServerMonitor.* */
    private String panelHeader;

    private JTextField enterHost;

    private java.rmi.registry.Registry rmiRegistry;

    //~ Constructors -----------------------------------------------------------

    // ----------------------------------------------------------------------------------------
    /**
     * Konstruktor.
     */
    public NamingServerMonitor() {
        if (host != null) {
            try {
                portscan = new PortScan(enterHost.getText());

                // java.rmi.registry.Registry rmiRegistry;

                System.out.println("Received Port: " + portscan.getPort()); // NOI18N

                rmiRegistry = LocateRegistry.getRegistry(enterHost.getText(), portscan.getPort());
                bounds = new String[rmiRegistry.list().length];
                bounds = rmiRegistry.list();

                // update();
            } catch (Exception e) {
                messageLabel.setForeground(Color.red);
                message(org.openide.util.NbBundle.getMessage(
                        NamingServerMonitor.class,
                        "NamingServerMonitor.NamingServerMonitor.message",
                        new Object[] { portscan.getPort() })); // NOI18N
            }

            System.out.println("Length: " + bounds.length); // NOI18N

            for (int i = 0; i < bounds.length; i++) {
                System.out.println("Content: " + bounds[i]); // NOI18N
            }

            initMainPanel();
        } else {
            bounds = new String[0];
            initMainPanel();
        }
    }
//----------------------------------------------------------------------------------------

    //~ Methods ----------------------------------------------------------------

// ----------------------------------------------------------------------------------------

    /**
     * Funktion zum initialisieren des Layouts.*
     */
    private void initMainPanel() {
        // JTable boundsTable = new JTable(bounds[], columnnamesForBounds);
        boundsTable = new JTable(new TableModel(TableModel.convertToMatrix(bounds), columnnamesForBounds));

        // JTabbedPane erzeugen und Tabellen hinzufuegen
        final JTabbedPane boundsPanel = new JTabbedPane();
        boundsPanel.add("bounds", new JScrollPane(boundsTable)); // NOI18N

        messageLabel = new JLabel();

        // UpdateButton fuer manuelles Update
        final JButton updateButton = new JButton(org.openide.util.NbBundle.getMessage(
                    NamingServerMonitor.class,
                    "NamingServerMonitor.updateButton.text")); // NOI18N
        updateButton.setActionCommand("update");               // NOI18N
        updateButton.addActionListener(new UpdateListener(this));
        // updateButton.addActionListener(new MonitorUpdateListener());

        // Panel f\u00FCr timePanel und hostPanel
        final JPanel containerPanel = new JPanel();
        containerPanel.setLayout(new BorderLayout());

        // Panel fuer updateIntervall Einstellungen
        final JPanel timePanel = new JPanel();
        timePanel.setBorder(BorderFactory.createTitledBorder(
                org.openide.util.NbBundle.getMessage(
                    NamingServerMonitor.class,
                    "NamingServerMonitor.timePanel.border.title"))); // NOI18N

        final ButtonGroup buttonGroup = new ButtonGroup();
        final JRadioButton oneMin = (new JRadioButton(
                    org.openide.util.NbBundle.getMessage(NamingServerMonitor.class, "NamingServerMonitor.oneMin.text"))); // NOI18N
        oneMin.setActionCommand("all 1 Minute");                                                                          // NOI18N
        oneMin.addActionListener(this);
        final JRadioButton fiveMin = new JRadioButton(org.openide.util.NbBundle.getMessage(
                    NamingServerMonitor.class,
                    "NamingServerMonitor.fiveMin.text"));                                                                 // NOI18N
        fiveMin.setActionCommand("all 5 Minutes");                                                                        // NOI18N
        oneMin.addActionListener(this);
        final JRadioButton tenMin = new JRadioButton(org.openide.util.NbBundle.getMessage(
                    NamingServerMonitor.class,
                    "NamingServerMonitor.tenMin.text"));                                                                  // NOI18N
        tenMin.setActionCommand("all 10 Minutes");                                                                        // NOI18N
        oneMin.addActionListener(this);

        timePanel.add(oneMin);
        timePanel.add(fiveMin);
        timePanel.add(tenMin);
        oneMin.setSelected(true);
        setUpdateIntervall(60);

        buttonGroup.add(oneMin);
        buttonGroup.add(fiveMin);
        buttonGroup.add(tenMin);

        final JPanel hostPanel = new JPanel();
        hostPanel.setLayout(new BorderLayout());
        enterHost = new JTextField();
        hostPanel.setBorder(BorderFactory.createTitledBorder(
                org.openide.util.NbBundle.getMessage(
                    NamingServerMonitor.class,
                    "NamingServerMonitor.hostPanel.border.title"))); // NOI18N

        hostPanel.add(enterHost, BorderLayout.CENTER);

        containerPanel.add(hostPanel, BorderLayout.NORTH);
        containerPanel.add(timePanel, BorderLayout.SOUTH);

        // MessageLabel und MessagePanel
        final JPanel messagePanel = new JPanel();
        messagePanel.setBorder(BorderFactory.createTitledBorder(
                org.openide.util.NbBundle.getMessage(
                    NamingServerMonitor.class,
                    "NamingServerMonitor.messagePanel.border.title"))); // NOI18N
        messagePanel.add(messageLabel);

        final JPanel buttonAndMessagePanel = new JPanel();
        buttonAndMessagePanel.setLayout(new BorderLayout());
        buttonAndMessagePanel.add(updateButton, BorderLayout.NORTH);
        buttonAndMessagePanel.add(messagePanel, BorderLayout.CENTER);

        // HauptPaneleistellungen
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder(panelHeader));
        add(containerPanel, BorderLayout.NORTH);
        add(boundsPanel, BorderLayout.CENTER);
        add(buttonAndMessagePanel, BorderLayout.SOUTH);
        // add(hostPanel, BorderLayout.WEST);
    }
    /**
     * ----------------------------------------------------------------------------------------
     *
     * @param  intervall  DOCUMENT ME!
     */
    public void setUpdateIntervall(final int intervall) {
        this.updateIntervall = intervall;
    }
    /**
     * ----------------------------------------------------------------------------------------
     * ----------------------------------------------------------------------------------------
     *
     * @param  message  DOCUMENT ME!
     */
    public void message(final String message) {
        messageLabel.setText(message);
    }
    /**
     * ----------------------------------------------------------------------------------------
     * ----------------------------------------------------------------------------------------
     *
     * @param  host  DOCUMENT ME!
     */
    public void setHost(String host) {
        host = host;
    }
    /**
     * ----------------------------------------------------------------------------------------
     * ----------------------------------------------------------------------------------------
     *
     * @return  DOCUMENT ME!
     */
    public String getHost() {
        return host;
    }
//----------------------------------------------------------------------------------------

    /**
     * .* * DOCUMENT ME!
     */
    private void update() {
        try {
            portscan = new PortScan(enterHost.getText());

            rmiRegistry = LocateRegistry.getRegistry(enterHost.getText(), portscan.getPort());
            bounds = new String[rmiRegistry.list().length];
            bounds = rmiRegistry.list();
        } catch (Exception e) {
            messageLabel.setForeground(Color.red);
            message(org.openide.util.NbBundle.getMessage(
                    NamingServerMonitor.class,
                    "NamingServerMonitor.update().message",
                    new Object[] { portscan.getPort() }) + portscan.getPort()); // NOI18N
        }
    }
//----------------------------------------------------------------------------------------

    /**
     * DOCUMENT ME!
     */
    public void updateTables() {
        update();
        try {
            final TableModel tmboundsTable = (TableModel)boundsTable.getModel();

            tmboundsTable.setDataVector(TableModel.convertToMatrix(bounds), columnnamesForBounds);

            message(org.openide.util.NbBundle.getMessage(
                    NamingServerMonitor.class,
                    "NamingServerMonitor.updateTables().message",
                    new Object[] { new Date(System.currentTimeMillis()) })); // NOI18N
        } catch (IllegalArgumentException e) {
            boundsTable.setModel(new TableModel());
        }
    }

//----------------------------------------------------------------------------------------
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
                message(org.openide.util.NbBundle.getMessage(
                        NamingServerMonitor.class,
                        "NamingServerMonitor.run().message",
                        new Object[] { new Date(System.currentTimeMillis()) })); // NOI18N
                t.sleep(updateIntervall * 1000);
                updateTables();
            } catch (InterruptedException e) {
            }
        }
    }
//----------------------------------------------------------------------------------------

    /**
     * MainFunktion zum testen des CentralServerMonitor.*
     *
     * @param  args  DOCUMENT ME!
     */
    public static void main(final String[] args) {
        // setHost("134.96.158.158");
        final NamingServerMonitor monitor = new NamingServerMonitor();

        final JFrame frame = new JFrame(org.openide.util.NbBundle.getMessage(
                    NamingServerMonitor.class,
                    "NamingServerMonitor.main(String[]).frame.title")); // NOI18N

        frame.getContentPane().add(monitor);
        frame.setSize(400, 400);
        frame.setVisible(true);
    }

//----------------------------------------------------------------------------------------
    @Override
    public void actionPerformed(final ActionEvent event) {
        final String command = event.getActionCommand();
        if (command.equals("all 1 Minute")) {          // NOI18N
            setUpdateIntervall(60);
        } else if (command.equals("all 5 Minutes")) {  // NOI18N
            setUpdateIntervall(300);
        } else if (command.equals("all 10 Minutes")) { // NOI18N
            setUpdateIntervall(600);
        }
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * ----------------------------------------------------------------------------------------.
     *
     * @version  $Revision$, $Date$
     */
    public class UpdateListener implements ActionListener {

        //~ Instance fields ----------------------------------------------------

        protected NamingServerMonitor monitor;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new UpdateListener object.
         *
         * @param  monitor  DOCUMENT ME!
         */
        public UpdateListener(final NamingServerMonitor monitor) {
            this.monitor = monitor;
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public void actionPerformed(final ActionEvent event) {
            monitor.updateTables();
        }
    }
//----------------------------------------------------------------------------------------
}
