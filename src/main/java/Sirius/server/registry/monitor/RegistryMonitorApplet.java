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
 * @author   Bernd Kiefer
 * @version  1.0 letzte Aenderung: 01.08.2000
 */
public class RegistryMonitorApplet extends JApplet {

    //~ Instance fields --------------------------------------------------------

    protected RegistryMonitor registryMonitor;

    //~ Methods ----------------------------------------------------------------

    @Override
    public void init() {
        registryMonitor = new RegistryMonitor(getParameter("RegistryIP"));
        getContentPane().add(registryMonitor);
    }
}
