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

import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
@Getter
@Setter
public class ClientConnectionContext extends AbstractConnectionContext {

    //~ Instance fields --------------------------------------------------------

    private final String content;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new ClientConnectionContext object.
     *
     * @param  category  DOCUMENT ME!
     * @param  content   DOCUMENT ME!
     */
    private ClientConnectionContext(final Category category, final String content) {
        super(category);
        this.content = content;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static ClientConnectionContext createDeprecated() {
        final StackTraceElement[] elements = new Exception().getStackTrace();
        final String context = DEPRECATED_CONTENT
                    + (SHOW_FULL_DEPRECATED_STACKTRACE ? Arrays.toString(elements) : elements[1].toString());
        return create(context);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static ClientConnectionContext create() {
        final StackTraceElement element = Thread.currentThread().getStackTrace()[2];
        final String context = element.getClassName() + ":" + element.getMethodName();
        return create(context);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   context  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static ClientConnectionContext create(final String context) {
        return new ClientConnectionContext(Category.UNKNOWN, context);
    }

    @Override
    public Origin getOrigin() {
        return Origin.CLIENT;
    }
}
