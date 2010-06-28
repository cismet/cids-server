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
// File name : PortScan.java
// Author    : Rene Wendling
// Date      : 24.10.2000
//
//------------------------------------------------------------------------------
//
package Sirius.util.NamingServerMonitor;

import java.io.*;

import java.net.*;

import java.rmi.*;
import java.rmi.registry.*;

import java.util.*;

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
public class PortScan {

    //~ Instance fields --------------------------------------------------------

    private String liststring;
    private String[] bounds;
    private String hostName;

    private Socket socket;

    private int port = 0;
    // private Vector ports;

    //~ Constructors -----------------------------------------------------------

// ----------------------------------------------------------------------------
    /**
     * Konstruktor.*
     *
     * @param  pcname  DOCUMENT ME!
     */
    public PortScan(final String pcname) {
        hostName = pcname;
        scanStandardPort(pcname);
    }
//----------------------------------------------------------------------------

    //~ Methods ----------------------------------------------------------------

// ----------------------------------------------------------------------------
    /**
     * \u00DCberpr\u00FCfung ob Registry auf Standardport.*
     *
     * @param  name  DOCUMENT ME!
     */
    public void scanStandardPort(final String name) {
        try {
            socket = new Socket(name, 1099);
            System.out.println("Registry auf Standartport");
            socket.close();
            setPort(1099);
        } catch (Exception e) {
            System.out.println("Registry nicht auf Standardport -> PortScan wird durchgef\u00FChrt");
            scanAll(name);
        }

        // return port;
    }
//----------------------------------------------------------------------------

//----------------------------------------------------------------------------
    /**
     * Portscan.*
     *
     * @param  name  DOCUMENT ME!
     */
    public void scanAll(final String name) {
        // ports = new Vector();

        for (int j = 1105; j < 1115; j++) {
            // System.out.println("\u00FCberpr\u00FCfe: " + j);

            try {
                // rmiRegistry = LocateRegistry.getRegistry(j);
                // bounds = new String[rmiRegistry.list().length];
                // bounds = rmiRegistry.list();
                socket = new Socket(name, j);
                System.out.println("Port in Benutzung: " + j);
                socket.close();

                if (isRegistry(j)) {
                    ;
                }
                {
                    setPort(j);
                    ;
                    break;
                }
                // ports.addElement(new Integer(j));
            } catch (IOException ex) {
                System.out.println("port frei: " + j);
            }
        }

        // return port;
    }
//----------------------------------------------------------------------------

//----------------------------------------------------------------------------
    /**
     * \u00DCberpr\u00FCfung auf benutzer Port RegistryPort ist.*
     *
     * @param   number  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean isRegistry(final int number) {
        final java.rmi.registry.Registry rmiRegistry;

        try {
            rmiRegistry = LocateRegistry.getRegistry(hostName, number);
            // port=number;
            // setPort(number);
            System.out.println("Registry gefunden auf port: " + number);
        } catch (Exception exc) {
            System.out.println("Registry nicht auf port: " + number);
        }

        return true;
    }
//----------------------------------------------------------------------------

//----------------------------------------------------------------------------
    /**
     * Aufruf von ausserhalb, R\u00FCckergabe ist der RegistryPort.*
     *
     * @return  DOCUMENT ME!
     */
    public int getPort() {
        return port;
    }
//----------------------------------------------------------------------------

//----------------------------------------------------------------------------
    /**
     * Aufruf zum Setzen des Port.*
     *
     * @param  portnr  DOCUMENT ME!
     */
    public void setPort(final int portnr) {
        port = portnr;
    }
//----------------------------------------------------------------------------

//----------------------------------------------------------------------------
    /**
     * main.*
     *
     * @param  args  DOCUMENT ME!
     */
    public static void main(final String[] args) {
        final PortScan portscan = new PortScan("134.96.158.160");
    }
//----------------------------------------------------------------------------
}
