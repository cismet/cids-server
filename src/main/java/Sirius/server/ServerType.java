/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package Sirius.server;

import Sirius.server.property.Createable;

import org.apache.log4j.Logger;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
public class ServerType implements Comparable, Createable {

    //~ Static fields/initializers ---------------------------------------------

    // Konstantendeklaration
    /**
     * Konstante, die den ServerTyp NOT_PREDEFINED f\u00FCr Servertypen welche nicht in den folgenden Konstanten
     * vorgesehen sind.*
     */
    public static final int NOT_PREDEFINED = 0;

    /**
     * Konstante, die den ServerTyp LocalServer repraesentiert. Wird benoetigt bei
     * {@link Sirius.server.naming.NameServer#registerServer(int, java.lang.String, java.lang.String)},
     * {@link Sirius.server.naming.NameServer#unregisterServer(int, java.lang.String, java.lang.String)},
     * {@link Sirius.server.naming.NameServer#getServerIP(int, java.lang.String)} 
     * {@link Sirius.server.naming.NameServer#getServerIPs(int) }
     */
    public static final int LOCALSERVER = 1;

    /**
     * Konstante, die den ServerTyp CallServer repraesentiert. Wird benoetigt bei
     * {@link Sirius.server.naming.NameServer#registerServer(int, java.lang.String, java.lang.String)},
     * {@link Sirius.server.naming.NameServer#unregisterServer(int, java.lang.String, java.lang.String)},
     * {@link Sirius.server.naming.NameServer#getServerIP(int, java.lang.String)} 
     * {@link Sirius.server.naming.NameServer#getServerIPs(int) }
     */
    public static final int CALLSERVER = 2;

    /**
     * Konstante, die den ServerTyp ProtocolServer repraesentiert. Wird benoetigt bei
     * {@link Sirius.server.naming.NameServer#registerServer(int, java.lang.String, java.lang.String)},
     * {@link Sirius.server.naming.NameServer#unregisterServer(int, java.lang.String, java.lang.String)},
     * {@link Sirius.server.naming.NameServer#getServerIP(int, java.lang.String)} 
     * {@link Sirius.server.naming.NameServer#getServerIPs(int) }
     */
    public static final int PROTOCOLSERVER = 3;

    /**
     * Konstante, die den ServerTyp ProtocolServer repraesentiert. Wird benoetigt bei
     * {@link Sirius.server.naming.NameServer#registerServer(int, java.lang.String, java.lang.String)},
     * {@link Sirius.server.naming.NameServer#unregisterServer(int, java.lang.String, java.lang.String)},
     * {@link Sirius.server.naming.NameServer#getServerIP(int, java.lang.String)} 
     * {@link Sirius.server.naming.NameServer#getServerIPs(int) }
     */
    public static final int USERSERVER = 4;

    /**
     * Konstante, die den ServerTyp ModelServer repraesentiert. Wird benoetigt bei
     * {@link Sirius.server.naming.NameServer#registerServer(int, java.lang.String, java.lang.String)},
     * {@link Sirius.server.naming.NameServer#unregisterServer(int, java.lang.String, java.lang.String)},
     * {@link Sirius.server.naming.NameServer#getServerIP(int, java.lang.String)} 
     * {@link Sirius.server.naming.NameServer#getServerIPs(int) }
     */
    public static final int MODELSERVER = 6;

    /**
     * Konstante, die den ServerTyp ModelServer repraesentiert. Wird benoetigt bei
     * {@link Sirius.server.naming.NameServer#registerServer(int, java.lang.String, java.lang.String)},
     * {@link Sirius.server.naming.NameServer#unregisterServer(int, java.lang.String, java.lang.String)},
     * {@link Sirius.server.naming.NameServer#getServerIP(int, java.lang.String)} 
     * {@link Sirius.server.naming.NameServer#getServerIPs(int) }
     */
    public static final int IRSEARCHSERVER = 7;

    protected static final Hashtable typeStrings = new Hashtable(10);

    private static final transient Logger LOG = Logger.getLogger(ServerType.class);

    //~ Instance fields --------------------------------------------------------

    protected int id;
    protected String name;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new ServerType object.
     *
     * @param  name  DOCUMENT ME!
     * @param  id    DOCUMENT ME!
     */
    public ServerType(final String name, final int id) {
        typeStrings.put(new Integer(NOT_PREDEFINED), "unknown");  // NOI18N
        typeStrings.put(new Integer(LOCALSERVER), "localServer"); // NOI18N
        typeStrings.put(new Integer(CALLSERVER), "callServer");   // NOI18N
        typeStrings.put(new Integer(USERSERVER), "userServer");   // NOI18N
        this.name = name;
        this.id = id;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   type  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static String getBindString(final int type) {
        // FIXME: nasty
        // debug to make shure Class is loaded xxx
        new ServerType("", 1); // NOI18N

        final Object o = typeStrings.get(new Integer(type));
        // logger.debug("type :"+type +"  "+ o);

        return o.toString();
    }

    /**
     * DOCUMENT ME!
     *
     * @param   o  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public int compareTo(final Object o) {
        return ((ServerType)o).id - id;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static int[] getAllServerTypes() {
        final Enumeration enu = typeStrings.keys();
        final int[] result = new int[typeStrings.size()];
        int i = 0;

        while (enu.hasMoreElements()) {
            result[i] = ((Integer)enu.nextElement()).intValue();
            i++;

            // assert i<size()

        }

        return result;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   id    DOCUMENT ME!
     * @param   name  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static boolean addType(final int id, final String name) {
        final Integer ID = new Integer(id);

        if (!typeStrings.contains(ID)) {
            typeStrings.put(ID, name);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public java.lang.Object createObject(final String constructorArgs, final String delimiter) {
        final String[] args = tokenizeString(constructorArgs, delimiter);

        if (args.length == 2) {
            return new ServerType(args[0], new Integer(args[1]).intValue());
        } else {
            LOG.error("<LS> ERROR Warning :: creatObject falsche Anzahl ConstructorParameter " + args.length); // NOI18N
            return null;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   s          DOCUMENT ME!
     * @param   delimiter  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private static String[] tokenizeString(final String s, final String delimiter) {
        final StringTokenizer tokenizer = new StringTokenizer(s, delimiter);
        final String[] stringArray = new String[tokenizer.countTokens()];
        int i = 0;

        while (tokenizer.hasMoreTokens()) {
            stringArray[i++] = tokenizer.nextToken();
        }

        return stringArray;
    }
}
