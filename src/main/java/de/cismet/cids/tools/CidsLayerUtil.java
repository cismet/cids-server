/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cismet.cids.tools;

import Sirius.server.localserver.attribute.ClassAttribute;
import Sirius.server.middleware.types.MetaClass;
import Sirius.server.newuser.User;

import org.apache.log4j.Logger;

import java.lang.reflect.Constructor;

import de.cismet.cids.server.cidslayer.CidsLayerInfo;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class CidsLayerUtil {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(CidsLayerUtil.class);

    //~ Methods ----------------------------------------------------------------

    /**
     * Determines the CidsLayerInfo object for the given meta class.
     *
     * @param   mc  the meta class
     *
     * @return  the CidsLayerInfo object for the given meta class
     */
    public static CidsLayerInfo getCidsLayerInfo(final MetaClass mc, User user) {
        CidsLayerInfo layerInfo = null;
        final ClassAttribute attr = mc.getClassAttribute("cidsLayer");

        if (attr != null) {
            final String className = attr.getValue().toString();

            try {
                final Class classObject = Class.forName(className);
                try {
                    Constructor c = classObject.getConstructor(MetaClass.class, User.class);
                    final Object info = c.newInstance(mc, user);

                    if (info instanceof CidsLayerInfo) {
                        layerInfo = (CidsLayerInfo)info;
                    }
                } catch (NoSuchMethodException e) {
                    Constructor c = classObject.getConstructor(MetaClass.class);

                    final Object info = c.newInstance(mc);

                    if (info instanceof CidsLayerInfo) {
                        layerInfo = (CidsLayerInfo)info;
                    }
                }
            } catch (Exception e) {
                LOG.error("Cannot instantiate CidsLayerInfo class: " + className, e);
            }
        }
        return layerInfo;
    }
}
