/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cismet.cids.server.connectioncontext;

import Sirius.server.middleware.types.MetaClass;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import de.cismet.connectioncontext.ConnectionContext;

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
public abstract class AbstractMetaClassConnectionContext extends ConnectionContext {

    //~ Static fields/initializers ---------------------------------------------

    public static final String FIELD__CLASS_NAME = "className";
    public static final String FIELD__CLASS_NAMES = "classNames";

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new AbstractObjectConnectionContext object.
     *
     * @param  category  DOCUMENT ME!
     * @param  context   DOCUMENT ME!
     * @param  mc        DOCUMENT ME!
     */
    public AbstractMetaClassConnectionContext(final Category category, final String context, final MetaClass mc) {
        super(category, context);
        getInfoFields().put(FIELD__CLASS_NAME, (mc == null) ? null : mc.getTableName());
    }

    /**
     * Creates a new AbstractMetaClassConnectionContext object.
     *
     * @param  category  DOCUMENT ME!
     * @param  context   DOCUMENT ME!
     * @param  mcs       DOCUMENT ME!
     */
    public AbstractMetaClassConnectionContext(final Category category,
            final String context,
            final Collection<MetaClass> mcs) {
        super(category, context);

        final Set<String> classes;
        if (mcs != null) {
            classes = new HashSet<>();
            for (final MetaClass mc : mcs) {
                classes.add((mc != null) ? mc.getTableName() : null);
            }
        } else {
            classes = null;
        }
        getInfoFields().put(FIELD__CLASS_NAMES, classes);
    }
}
