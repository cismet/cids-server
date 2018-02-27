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
public class ServerConnectionContext extends AbstractConnectionContext {

    //~ Instance fields --------------------------------------------------------

    private final String content;
    private final Origin origin;
    private final String clientAddress;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new ServerConnectionContext object.
     *
     * @param  origin      DOCUMENT ME!
     * @param  category    DOCUMENT ME!
     * @param  content     DOCUMENT ME!
     * @param  clientHost  DOCUMENT ME!
     */
    private ServerConnectionContext(final Origin origin,
            final Category category,
            final String content,
            final String clientHost) {
        super(category);
        this.content = content;
        this.origin = origin;
        this.clientAddress = clientHost;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static ServerConnectionContext createDeprecated() {
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
    public static ServerConnectionContext create() {
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
    public static ServerConnectionContext create(final String context) {
        return new ServerConnectionContext(Origin.SERVER, Category.UNKNOWN, context, null);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   clientConnectionContext  DOCUMENT ME!
     * @param   localAddress             DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static ServerConnectionContext createFromClientContext(final ClientConnectionContext clientConnectionContext,
            final String localAddress) {
        final ServerConnectionContext serverConnectionContext = new ServerConnectionContext(
                Origin.CLIENT,
                clientConnectionContext.getCategory(),
                clientConnectionContext.getContent(),
                localAddress);
        serverConnectionContext.getAdditionalFields().putAll(clientConnectionContext.getAdditionalFields());
        return serverConnectionContext;
    }
}
