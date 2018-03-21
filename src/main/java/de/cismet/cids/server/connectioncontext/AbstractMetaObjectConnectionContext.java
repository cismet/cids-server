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
import Sirius.server.middleware.types.MetaObject;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
public abstract class AbstractMetaObjectConnectionContext extends AbstractMetaClassConnectionContext {

    //~ Static fields/initializers ---------------------------------------------

    public static final String FIELD__OBJECT_ID = "objectId";
    public static final String FIELD__OBJECT_IDS = "objectIds";

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new AbstractObjectConnectionContext object.
     *
     * @param  category  DOCUMENT ME!
     * @param  context   DOCUMENT ME!
     * @param  mo        DOCUMENT ME!
     */
    public AbstractMetaObjectConnectionContext(final Category category, final String context, final MetaObject mo) {
        super(category, context, (mo != null) ? mo.getMetaClass() : null);
        getInfoFields().put(FIELD__OBJECT_ID, (mo == null) ? null : mo.getId());
    }

    /**
     * Creates a new AbstractMetaObjectConnectionContext object.
     *
     * @param  category  DOCUMENT ME!
     * @param  context   DOCUMENT ME!
     * @param  mc        DOCUMENT ME!
     * @param  mos       DOCUMENT ME!
     */
    public AbstractMetaObjectConnectionContext(final Category category,
            final String context,
            final MetaClass mc,
            final Collection<MetaObject> mos) {
        super(category, context, mc);

        final Set<Integer> objectIds;
        if (mos != null) {
            objectIds = new HashSet<>();
            for (final MetaObject mo : mos) {
                objectIds.add((mo != null) ? mo.getId() : null);
            }
        } else {
            objectIds = null;
        }
        getInfoFields().put(FIELD__OBJECT_IDS, objectIds);
    }
}
