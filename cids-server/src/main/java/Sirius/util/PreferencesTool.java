/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * PrefsMgr.java
 *
 * Created on 23. September 2003, 12:41
 */
package Sirius.util;

import java.net.Socket;

import java.util.prefs.*;
//import org.apache.log4j.*;
//import org.apache.log4j.net.SocketAppender;

/**
 * Setzt preferences in die BackingStore.
 *
 * @author   awindholz
 * @version  $Revision$, $Date$
 */
public class PreferencesTool {

    //~ Static fields/initializers ---------------------------------------------

    /** PathSepatrator. */
    private static final String PS = "/"; // NOI18N

    //~ Instance fields --------------------------------------------------------

// private static Logger logger = Logger.getLogger(PreferencesTool.class);

    // NOI18N

// private static Logger logger = Logger.getLogger(PreferencesTool.class);
    private String pathName;
    private boolean isSysNode = true;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new instance of PreferencesTool.
     */
    public PreferencesTool() {
//        BasicConfigurator.configure(new SocketAppender("localhost", 4445));
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   key           Schl\u00FCsselname dessen Wert abgefragt werden soll. Name ohne Pfad.
     * @param   defaultValue  wert der Geliefert werden soll wenn der Schl\u00FCssel nicht gefunden wird.
     *
     * @return  wert des Schl\u00FCssels oder defaultValue wenn keiner gefunden.
     *
     * @throws  BackingStoreException  DOCUMENT ME!
     */
    public String getPreference(final String key, final String defaultValue) throws BackingStoreException {
        return getPreference(isSysNode, pathName + PS + key, defaultValue);
    }

    /**
     * Setzt den Knoten der zu verarbeiten ist. Die werte werden gesetzt ungeachtetdessen ob dieser knoter existiert
     * oder nicht.
     *
     * @param     isSysNode  true: Knoten aus Systembaum, false: Knoten aus Userbaum.
     * @param     pathName   DOCUMENT ME!
     *
     * @return    false wenn \u00FCbergebener Knoten nicht gefunden wurde.
     *
     * @throws    BackingStoreException  DOCUMENT ME!
     *
     * @pathName  Path zu dem Knoten.
     */
    public boolean setData(final boolean isSysNode, final String pathName) throws BackingStoreException {
        this.isSysNode = isSysNode;
        this.pathName = pathName;

        return getRootNode(isSysNode).nodeExists(pathName);
    }

    /**
     * Setzt den Schl\u00FCssel und deren wert in die BackingStore.
     *
     * @param   key    DOCUMENT ME!
     * @param   value  DOCUMENT ME!
     *
     * @throws  BackingStoreException  DOCUMENT ME!
     */
    public void setPreference(final String key, final String value) throws BackingStoreException {
        setPreference(isSysNode, pathName + PS + key, value);
    }

    /**
     * Setzt belibig viele beliebige Schl\u00FCssel und deren Werte.
     *
     * @param   keys    schl\u00FCsselnamen
     * @param   values  Werte der Schl\u00FCssel.
     *
     * @throws  BackingStoreException  wenn lengen der beiden Arrays ungleich
     */
    public void setPreferences(final String[] keys, final String[] values) throws BackingStoreException {
        setPreferences(isSysNode, pathName, keys, values);
    }

    ////////////////////////////////////////////////////////////////////////////////
    // Statischen Funktionen
    /**
     * Setzt belibig viele beliebige Schl\u00FCssel und deren Werte.
     *
     * @param   isSysNode  ob es sich bei diesem Knoten um einen System oder User Knoten handelt.
     * @param   pathName   pfad zu dem Knoten in den die Schl\u00FCsel gesetzt werden sollen.
     * @param   keys       schl\u00FCsselnamen ohne pfadangaben.
     * @param   values     Werte der Schl\u00FCssel.
     *
     * @throws  BackingStoreException  wenn lengen der beiden Arrays ungleich
     */
    public static void setPreferences(final boolean isSysNode,
            final String pathName,
            final String[] keys,
            final String[] values) throws BackingStoreException {
        if (keys.length != values.length) {
            final String meldung = "Unequal number of keys and values"               // NOI18N
                        + " was given to set the preferences in the backing store."; // NOI18N

            throw new BackingStoreException(meldung);
        }

        for (int i = 0; i < keys.length; i++) {
            setPreference(isSysNode, pathName + PS + keys[i], values[i]);
        }
    }

    /**
     * Laedt ein RootKnoten aus dem Backing Store.
     *
     * @param   isSysNode  true(System) oder false(User).
     *
     * @return  System-RootKnoten wenn true \u00FCbergeben wurde, User-RootKnoten wenn fasle \u00FCbergeben wurde.
     */
    private static Preferences getRootNode(final boolean isSysNode) {
        if (isSysNode) {
            return Preferences.systemRoot();
        } else {
            return Preferences.userRoot();
        }
    }

    /**
     * Laedt eine Einstellung in das Backing Store.
     *
     * @param   isSysNode     key name der Eigenschaft.
     * @param   qualifiedKey  DOCUMENT ME!
     * @param   value         wert der Eigenschaft.
     *
     * @throws  BackingStoreException  DOCUMENT ME!
     */
    public static void setPreference(final boolean isSysNode, final String qualifiedKey, final String value)
            throws BackingStoreException {
        final int index = qualifiedKey.lastIndexOf(PS);
        Preferences prefs = getRootNode(isSysNode);
        String key = qualifiedKey;
        if (index > 0) {
            final String node = qualifiedKey.substring(0, index);
            prefs = getRootNode(isSysNode).node(node);
            key = qualifiedKey.substring(index + 1, qualifiedKey.length());
        }
        // logger.info("Daten in BackingStore geschrieben: (" + key + ", " + value + ")");
        prefs.put(key, value);

        // Update des Backing Stores erzwingen
        prefs.flush();
    }

    /**
     * Laedt eine Einstellung aus dem Backing Store.
     *
     * @param   isSysNode     DOCUMENT ME!
     * @param   qualifiedKey  vollqualifizierter name des Schl\u00FCssels.
     * @param   defaultValue  wert der Geliefert werden soll wenn der Schl\u00FCssel nicht gefunden wird.
     *
     * @return  wert des Schl\u00FCssels oder defaultValue wenn keiner gefunden.
     *
     * @throws  BackingStoreException  DOCUMENT ME!
     */
    public static String getPreference(final boolean isSysNode, final String qualifiedKey, final String defaultValue)
            throws BackingStoreException {
        final int index = qualifiedKey.lastIndexOf(PS);
        Preferences prefs = getRootNode(isSysNode);
        String key = qualifiedKey;
        if (index > 0) {
            final String node = qualifiedKey.substring(0, index);
            prefs = getRootNode(isSysNode).node(node);
            key = qualifiedKey.substring(index + 1, qualifiedKey.length());
        }

//        logger.debug("Daten aus BackingStore auslesen: qulifiedKey: " +
//        qualifiedKey + " node: " + prefs.name() + " key: " + key);
        final String ret = prefs.get(key, defaultValue);
//        logger.debug("Auslesen erfolgreich. Wert: " + ret);

        return ret;
    }

    /**
     * Anzahl der Parameter muss ungerade sein. Erster parameter ist der Pfad zu dem Knoten in der BackingStore, dieser
     * darf am Ende keinen Separator haben. Weiter folgen paarweise Name des Schl\u00FCssels und dessen Wert. Z.B.
     * PreferencesTool {/RootNode/node, key1, wert1, key2, wert2}
     *
     * @param  args  DOCUMENT ME!
     */
    public static void main(final String[] args) {
        try {
            if ((args.length % 2) == 0) {
                usage();
                System.exit(-1);
            }

            final PreferencesTool pt = new PreferencesTool();
            if (!pt.setData(true, args[0])) {
                System.out.println("Info: Node " + args[0] + " does not exist, yet."); // NOI18N
            }

            for (int i = 1; i < args.length; i = i + 2) {
                pt.setPreference(args[i], args[i + 1]);
            }

            System.out.println("Values were successfully written."); // NOI18N
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    /**
     * DOCUMENT ME!
     */
    private static void usage() {
        System.out.println("Usage: ");                                                                          // NOI18N
        System.out.println("   java " + PreferencesTool.class.getName() + " <NODEPATH> " + "(<KEY> <VALUE>)+"); // NOI18N
    }
}
