/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * UrlConverter.java
 *
 * Created on 12. Dezember 2005, 13:23
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */
package de.cismet.cids.tools.tostring;

import de.cismet.cids.annotations.CidsAttribute;

import de.cismet.cids.tools.CustomToStringConverter;

/**
 * DOCUMENT ME!
 *
 * @author   hell
 * @version  $Revision$, $Date$
 */
public class UrlConverter extends CustomToStringConverter implements java.io.Serializable {

    //~ Instance fields --------------------------------------------------------

    @CidsAttribute("URL_BASE_ID.PROT_PREFIX")//NOI18N
    public String prot;

    @CidsAttribute("URL_BASE_ID.SERVER")//NOI18N
    public String server;

    @CidsAttribute("URL_BASE_ID.PATH")//NOI18N
    public String path;

    @CidsAttribute("OBJECT_NAME")//NOI18N
    public String name;
    private final transient org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(this.getClass());

    //~ Methods ----------------------------------------------------------------

    public String createString() {
        return prot + server + path + name;
    }
}
