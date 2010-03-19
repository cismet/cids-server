/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * ResultHandler.java
 *
 * Created on 26. September 2003, 14:57
 */
package Sirius.server.sql;

import java.sql.*;

import java.util.*;

import Sirius.server.search.*;
/**
 * DOCUMENT ME!
 *
 * @author   schlob
 * @version  $Revision$, $Date$
 */
public interface ResultHandler {

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   rs  DOCUMENT ME!
     * @param   q   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  SQLException  DOCUMENT ME!
     * @throws  Exception     DOCUMENT ME!
     */
    Object handle(ResultSet rs, Query q) throws SQLException, Exception;
}
