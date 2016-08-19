/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package Sirius.server.middleware.types;

import Sirius.server.localserver.attribute.ObjectAttribute;

import org.apache.log4j.Logger;

import java.io.Serializable;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * A MetaObjectReference enables reusing the same instance of one meta object within a hierarchy of meta objects. To
 * preserve parent-child relationships among identical meta objects within the hierarchy, each MetaObjectReference
 * instance contains a separate referencingAttribute property.<br>
 * See also <a href="https://javaclippings.wordpress.com/2009/03/18/dynamic-proxies-equals-hashcode-tostring/">Java
 * Dynamic Proxies – equals(), hashCode(), toString()</a>
 *
 * @author   Pascal Dihé <pascal.dihe@web.de>
 * @version  $Revision$, $Date$
 * @since    2016-07-01
 */
public class MetaObjectReference implements InvocationHandler, Serializable {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOGGER = Logger.getLogger(MetaObjectReference.class);

    private static final String EQUALS = "equals";

    private static final String HASHCODE = "hashCode";

    private static final String GET_REFERENCING_OBJECT_ATTRIBUTE = "getReferencingObjectAttribute";

    private static final String SET_REFERENCING_OBJECT_ATTRIBUTE = "setReferencingObjectAttribute";

    //~ Instance fields --------------------------------------------------------

    private final MetaObject metaObject;
    private ObjectAttribute referencingObjectAttribute;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new MetaObjectReference object.
     *
     * @param  metaObject                  DOCUMENT ME!
     * @param  referencingObjectAttribute  DOCUMENT ME!
     */
    private MetaObjectReference(final MetaObject metaObject,
            final ObjectAttribute referencingObjectAttribute) {
        this.metaObject = metaObject;
        this.referencingObjectAttribute = referencingObjectAttribute;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   metaObject                  DOCUMENT ME!
     * @param   referencingObjectAttribute  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static final MetaObject getInstance(final MetaObject metaObject,
            final ObjectAttribute referencingObjectAttribute) {
        metaObject.setReferencingObjectAttribute(null);
        final MetaObject metaObjectProxy = (MetaObject)Proxy.newProxyInstance(
                metaObject.getClass().getClassLoader(),
                metaObject.getClass().getInterfaces(),
                new MetaObjectReference(metaObject, referencingObjectAttribute));

        // change references to proxy object
        for (final ObjectAttribute objectAttribute : metaObject.getAttributes().values()) {
            objectAttribute.setParentObject(metaObjectProxy);
        }

        return metaObjectProxy;
    }

    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        if (GET_REFERENCING_OBJECT_ATTRIBUTE.equals(method.getName())) {
            return this.referencingObjectAttribute;
        }

        if (SET_REFERENCING_OBJECT_ATTRIBUTE.equals(method.getName())) {
            this.referencingObjectAttribute = (ObjectAttribute)args[0];

            if (LOGGER.isDebugEnabled()) {
                LOGGER.warn("unexpected call to setReferencingObjectAttribute('" + args[0]
                            + "') on MetaObjectReference '"
                            + this.metaObject.getName() + "' (" + this.metaObject.getKey()
                            + "), original ReferencingObjectAttribute '"
                            + this.referencingObjectAttribute + "'");
            }
        }

        if (EQUALS.equals(method.getName())) {
            return equalsInternal(proxy, args[0]);
        }

        if (HASHCODE.equals(method.getName())) {
            return this.metaObject.hashCode();
        }

        try {
            return method.invoke(metaObject, args);
        } catch (InvocationTargetException itx) {
            throw itx.getTargetException();
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   me     DOCUMENT ME!
     * @param   other  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private boolean equalsInternal(final Object me, final Object other) {
        if (other == null) {
            return false;
        }

        if (other instanceof java.lang.reflect.Proxy) {
            final InvocationHandler handler = Proxy.getInvocationHandler(other);

            if (!(handler instanceof MetaObjectReference)) {
                // the proxies behave differently.
                return false;
            }

            return ((MetaObjectReference)handler).metaObject.equals(this.metaObject);
        }

        if (other instanceof MetaObject) {
            return this.metaObject.equals(other);
        }

        return false;
    }
}
