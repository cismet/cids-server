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

    /** Use serialVersionUID for interoperability. */
    private static final long serialVersionUID = -4406088759203948775L;

    public static String host;

    //~ Instance fields --------------------------------------------------------

    public String[] bounds;

    public NamingServerMonitor monitor;
    /** Referenz auf den PortScan.* */
    private PortScan portscan;

    private java.lang.Object[] columnnamesForBounds = { "URL" };
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

                System.out.println("\u00FCbermittelter Port: " + portscan.getPort());

                rmiRegistry = LocateRegistry.getRegistry(enterHost.getText(), portscan.getPort());
                bounds = new String[rmiRegistry.list().length];
                bounds = rmiRegistry.list();

                // update();
            } catch (Exception e) {
                messageLabel.setForeground(Color.red);
                message("Registry not on Port: " + portscan.getPort());
            }

            System.out.println("L\u00E4nge: " + bounds.length);

            for (int i = 0; i < bounds.length; i++) {
                System.out.println("Inhalt: " + bounds[i]);
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
        boundsPanel.add("bounds", new JScrollPane(boundsTable));

        messageLabel = new JLabel();

        // UpdateButton fuer manuelles Update
        final JButton updateButton = new JButton("update");
        updateButton.setActionCommand("update");
        updateButton.addActionListener(new UpdateListener(this));
        // updateButton.addActionListener(new MonitorUpdateListener());

        // Panel f\u00FCr timePanel und hostPanel
        final JPanel containerPanel = new JPanel();
        containerPanel.setLayout(new BorderLayout());

        // Panel fuer updateIntervall Einstellungen
        final JPanel timePanel = new JPanel();
        timePanel.setBorder(BorderFactory.createTitledBorder("Intervall for automatical update"));

        final ButtonGroup buttonGroup = new ButtonGroup();
        final JRadioButton oneMin = (new JRadioButton("all 1 Minute"));
        oneMin.setActionCommand("all 1 Minute");
        oneMin.addActionListener(this);
        final JRadioButton fiveMin = new JRadioButton("all 5 Minutes");
        fiveMin.setActionCommand("all 5 Minutes");
        oneMin.addActionListener(this);
        final JRadioButton tenMin = new JRadioButton("all 10 Minutes");
        tenMin.setActionCommand("all 10 Minutes");
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
        hostPanel.setBorder(BorderFactory.createTitledBorder("Enter IP - Address"));

        hostPanel.add(enterHost, BorderLayout.CENTER);

        containerPanel.add(hostPanel, BorderLayout.NORTH);
        containerPanel.add(timePanel, BorderLayout.SOUTH);

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
            message("Registry not on Port: " + portscan.getPort());
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

            message("last update: " + (new Date(System.currentTimeMillis())));
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
                message("last update: " + (new Date(System.currentTimeMillis())));
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

        final JFrame frame = new JFrame("NamingServerMonitor");

        frame.getContentPane().add(monitor);
        frame.setSize(400, 400);
        frame.setVisible(true);
    }

//----------------------------------------------------------------------------------------
    @Override
    public void actionPerformed(final ActionEvent event) {
        final String command = event.getActionCommand();
        if (command.equals("all 1 Minute")) {
            setUpdateIntervall(60);
        } else if (command.equals("all 5 Minutes")) {
            setUpdateIntervall(300);
        } else if (command.equals("all 10 Minutes")) {
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
