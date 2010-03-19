/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * DateFromString.java
 *
 * Created on 6. September 2004, 14:50
 */
package de.cismet.cids.tools.fromstring;
import Sirius.server.localserver.attribute.*;

import Sirius.util.*;

import java.sql.*;

/**
 * DOCUMENT ME!
 *
 * @author   schlob
 * @version  $Revision$, $Date$
 */
public class DateFromString extends FromStringCreator implements java.io.Serializable {

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new instance of DateFromString.
     */
    public DateFromString() {
        super();
    }

    //~ Methods ----------------------------------------------------------------

    public Object create(String dateString, Object hull) throws Exception {
        ObjectAttribute oa = (ObjectAttribute)hull;

        if (dateString.equalsIgnoreCase("now") || dateString.equalsIgnoreCase(" ")) {
            oa.setValue(new Date(System.currentTimeMillis()));
            return oa;
        } else if (dateString.equalsIgnoreCase("")) {
            oa.setValue(null);
            return oa;
        } else {
            long millis = System.currentTimeMillis();

//            try
//            {
            java.text.SimpleDateFormat formater = new java.text.SimpleDateFormat();

            formater.applyPattern("dd.mm.yy");

            millis = formater.parse(dateString).getTime(); // Date.parse(dateString); }catch(Exception
                                                           // e){e.printStackTrace();System.err.println("Date format
                                                           // falsch setzte date auf NOW");}

            oa.setValue(new Date(millis));
            return oa;
        }
    }
}
