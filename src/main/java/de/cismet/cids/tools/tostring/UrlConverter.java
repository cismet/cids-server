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
 *
 * @author hell
 */
public class UrlConverter extends CustomToStringConverter implements java.io.Serializable {
    private final transient org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(this.getClass());
    
    @CidsAttribute("URL_BASE_ID.PROT_PREFIX")
    public String prot;
    
    @CidsAttribute("URL_BASE_ID.SERVER")
    public String server;
    
    @CidsAttribute("URL_BASE_ID.PATH")
    public String path;
    
    @CidsAttribute("OBJECT_NAME")
    public String name;
   
    
    public String createString() {
        return prot+server+path+name;
    }
    
    
    
    
    
}
