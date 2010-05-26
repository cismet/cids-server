/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * AdressStringConverter.java
 *
 * Created on 11. Mai 2004, 13:31
 */
package de.cismet.cids.tools.tostring;
import Sirius.server.localserver.attribute.*;

import com.vividsolutions.jts.geom.*;


//import de.cismet.tools.postgis.*;
import java.util.*;
/**
 * DOCUMENT ME!
 *
 * @author   schlob
 * @version  $Revision$, $Date$
 */
public class GeometryStringConverter extends ToStringConverter implements java.io.Serializable {

    //~ Static fields/initializers ---------------------------------------------

    /** Use serialVersionUID for interoperability. */
    private static final long serialVersionUID = -6328761746418929471L;

    protected static String GEOM_CLASS = "com.vividsolutions.jts.geom.Geometry";
    // SRID=-1;POLYGON((191232 243117,191232 243119,191234 243117,191232 243117))
    private static transient org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(
            GeometryStringConverter.class);

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new GeometryStringConverter object.
     */
    public GeometryStringConverter() {
        super();
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public String convert(final de.cismet.cids.tools.tostring.StringConvertable o) {
        if (logger == null) {
            logger = org.apache.log4j.Logger.getLogger(GeometryStringConverter.class);
        }
        // !!!! attention o.toString will cause stack overflow :-)

        if (logger != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("convert of GeometryStringconverter called");
            }
        }

//       return convert( ((ObjectAttribute)o).getValue().toString());   if (o instanceof Sirius.server.localserver.attribute.ObjectAttribute)
        if (o instanceof Sirius.server.localserver.attribute.ObjectAttribute) {
            if (logger != null) {
                if (logger.isDebugEnabled()) {
                    logger.debug(" o instanceof ObjectAttribute");
                }
            }

            final java.lang.Object attrObj = ((ObjectAttribute)o).getValue();

            if (attrObj instanceof Geometry) {
                if (logger != null) {
                    if (logger.isDebugEnabled()) {
                        logger.debug(" o instanceof Geometry");
                    }
                }

                return ((Geometry)attrObj).toText();
            } else if (attrObj instanceof Sirius.server.localserver.object.Object) {
                if (logger != null) {
                    if (logger.isDebugEnabled()) {
                        logger.debug(" o instanceof ServerObject");
                    }
                }
                try {
                    // problem welches attribut solls sein

                    final Collection c = ((Sirius.server.localserver.object.Object)attrObj).getAttributesByType(
                            Class.forName(GEOM_CLASS));
                    // nimm das erste
                    final Iterator<Sirius.server.localserver.attribute.ObjectAttribute> iter = c.iterator();

                    if (iter.hasNext()) {
                        return convert((StringConvertable)iter.next());
                    }
                } catch (ClassNotFoundException ex) {
                    return "Wrong Type not convertable Class Not Found " + ex.getMessage();
                }
            } else // irgendwas
            {
                if (logger != null) {
                    logger.error("Fehler im erstellen der Stringrepr\u00E4sentation");
                }
                return "Fehler im erstellen der Stringrep\u00E4sentation";
            }
        } else if (o instanceof Sirius.server.localserver.object.Object) {
            try {
                // problem welches attribut solls sein

                final Collection c = ((Sirius.server.localserver.object.Object)o).getAttributesByType(
                        Class.forName(GEOM_CLASS));
                // nimm das erste
                final Iterator<Sirius.server.localserver.attribute.ObjectAttribute> iter = c.iterator();

                if (iter.hasNext()) {
                    return convert((StringConvertable)iter.next());
                } else {
                    return "NO GeoAttribute found in Object";
                }
            } catch (ClassNotFoundException ex) {
                return "Wrong Type not convertable Class Not Found " + ex.getMessage();
            }
        }

        return "Wrong Type not convertable ::" + o.getClass();
    }

//     public  String convert(String o)
//    {
//        String stringRepresentation="";
//
//        stringRepresentation = o;
//
//        boolean isBox = false;
//
//        int begin =0;
//        int end =0;
//
//        if(stringRepresentation.startsWith("SRID=-1;POLYGON"))
//        {
//            begin = stringRepresentation.indexOf("N")+2;
//            end = stringRepresentation.length()-1;
//        }
//
//        else if (stringRepresentation.startsWith("SRID=-1;POINT"))
//        {
//         begin = stringRepresentation.indexOf("T")+1;
//         end = stringRepresentation.length();
//        }
//        else if (stringRepresentation.startsWith("SRID=-1;BOX3D"))
//        {
//         begin = stringRepresentation.indexOf("3D")+2;
//         end = stringRepresentation.length();
//         isBox=true;
//
//        }
//        else if (stringRepresentation.startsWith("SRID=-1;LINESTRING"))
//        {
//         begin = stringRepresentation.indexOf("G")+1;
//         end = stringRepresentation.length();
//
//        }
//
//        else
//            ; //nop
//
//
//        stringRepresentation= stringRepresentation.substring(begin,end);
//
//        stringRepresentation = stringRepresentation.replaceAll(",", ")(");
//        stringRepresentation = stringRepresentation.replaceAll(" ",",");
//
//        if(isBox) //3d :-(
//        {
//            stringRepresentation = stringRepresentation.replaceAll(",0[)]",")");
//            //stringRepresentation +=  stringRepresentation;
//        }
//
//
//
//        return stringRepresentation;
//    }

//    public static void main(String[] args)
//    {
//
//        GeometryStringConverter t = new GeometryStringConverter();
//
////        System.out.println(t.convert("SRID=-1;POLYGON((191232 243117,191232 243119,191234 243117,191232 243117))"));
////        System.out.println(t.convert("SRID=-1;POINT(191232 243117)"));
////        System.out.println(t.convert("SRID=-1;LINESTRING(191232 243117, 191232 243117)"));
////        System.out.println(t.convert("SRID=-1;BOX3D(191232 243117 0,191232 243117 0)"));
//
//
//
//    }
//
}
