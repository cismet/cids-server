/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cidsx.server.api.types.legacy;

import Sirius.server.middleware.types.MetaNode;
import Sirius.server.middleware.types.MetaObjectNode;
import Sirius.server.middleware.types.Node;
import Sirius.server.newuser.permission.Policy;
import com.vividsolutions.jts.geom.Geometry;
import de.cismet.cids.dynamics.CidsBeanJsonDeserializer;
import de.cismet.cids.dynamics.CidsBeanJsonSerializer;

import org.apache.log4j.Logger;

import de.cismet.cidsx.server.api.types.CidsNode;

/**
 * A factory class for converting between legacy cids types and REST/JSON types. TODO: Integrate into <strong>
 * cids-server-rest-types project</strong>!
 *
 * @author   Pascal Dihé
 * @version  $Revision$, $Date$
 */
public class CidsNodeFactory {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(CidsNodeFactory.class);
    private static final CidsNodeFactory factory = new CidsNodeFactory();

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new CidsNodeFactory object.
     */
    private CidsNodeFactory() {
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static final CidsNodeFactory getFactory() {
        return factory;
    }

    /**
     * Transforms a cids rest API node into a cids legacy node (Sirius) object.
     *
     * @param   cidsNode  the cids rest node to be converted
     *
     * @return  the converted cids legacy node
     *
     * @throws  Exception  if any error occurs during the conversion
     */
    public Sirius.server.middleware.types.Node legacyCidsNodeFromRestCidsNode(
            final de.cismet.cidsx.server.api.types.CidsNode cidsNode) throws Exception {
        final int id = (cidsNode.getId() != null) ? Integer.parseInt(cidsNode.getId()) : -1;
        final String name = cidsNode.getName();
        final String description = cidsNode.getDescription();
        final String domain = cidsNode.getDomain();
        final int classId = cidsNode.getClassId();
        final boolean isLeaf = cidsNode.isLeaf();
        final Policy policy = CidsClassFactory.getFactory().createPolicy(cidsNode.getPolicy());
        final int iconFactory = cidsNode.getIconFactory();
        final String icon = cidsNode.getIcon();
        final boolean derivePermissionsFromClass = cidsNode.isDerivePermissionsFromClass();
        final String artificialId = cidsNode.getArtificialId();
        final String dynamicChildrenStatement = cidsNode.getDynamicChildren();
        final boolean sqlSort = cidsNode.isClientSort();
        final boolean isDynamic = cidsNode.isDynamic();
        final Geometry cachedGeometry = CidsBeanJsonDeserializer.fromEwkt(cidsNode.getCachedGeometry());
        final String lightweightJson = cidsNode.getLightweightJson();

        final Node legacyNode;

        if ((cidsNode.getObjectKey() != null) && !cidsNode.getObjectKey().isEmpty()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("node '" + cidsNode.getName() + "' (" + cidsNode.getId()
                            + ") will be converted to meta object node");
            }
            final int objectId = cidsNode.getObjectId();
            legacyNode = new MetaObjectNode(
                    id,
                    name,
                    description,
                    domain,
                    objectId,
                    classId,
                    isLeaf,
                    policy,
                    iconFactory,
                    icon,
                    derivePermissionsFromClass,
                    artificialId,
                    cachedGeometry,
                    lightweightJson);
            // FIXME: creation of class tree nodes disbaled! how to determine if a cids node shall be converted to class
            // tree node or simple tree node? } else if (cidsNode.getClassKey() != null &&
            // !cidsNode.getClassKey().isEmpty()) { LOG.debug("node '" + cidsNode.getName() + "' (" + cidsNode.getId() +
            // ") will be converted to meta class node"); legacyNode = new MetaClassNode( id, domain, classId, name,
            // description, isLeaf, policy, iconFactory, icon, derivePermissionsFromClass, classId, artificialId);
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug("node '" + cidsNode.getName() + "' (" + cidsNode.getId()
                            + ") will be converted to meta node");
            }
            legacyNode = new MetaNode(
                    id,
                    domain,
                    name,
                    description,
                    isLeaf,
                    policy,
                    iconFactory,
                    icon,
                    derivePermissionsFromClass,
                    classId,
                    artificialId);
        }

        legacyNode.setDynamicChildrenStatement(dynamicChildrenStatement);
        legacyNode.setSqlSort(sqlSort);
        legacyNode.setDynamic(isDynamic);
        return legacyNode;
    }

    /**
     * Transforms a cids legacy node(Sirius) object into a cids rest API node.<br>
     * Since a meta class node (in contrast to meta object node) does not contain a meta class instance, we have to pass
     * the class name parameter in order to be able to create a cids class $self reference for the property classKey!
     *
     * @param   legacyNode  the cids legacy node to be converted
     * @param   className   name (table name) of the class associated with the node
     *
     * @return  the converted cids rest node
     */
    public de.cismet.cidsx.server.api.types.CidsNode restCidsNodeFromLegacyCidsNode(
            final Sirius.server.middleware.types.Node legacyNode,
            String className) {
        final String id = Integer.toString(legacyNode.getId());
        final String name = legacyNode.getName();
        final String domain = legacyNode.getDomain();
        final String description = legacyNode.getDescription();
        final String artificialId = legacyNode.getArtificialId();
        final String dynamicChildren = legacyNode.getDynamicChildrenStatement();
        final boolean clientSort = legacyNode.isSqlSort();
        final boolean derivePermissionsFromClass = legacyNode.isDerivePermissionsFromClass();
        final boolean leaf = legacyNode.isLeaf();
        final String icon = legacyNode.getIconString();
        final int iconFactory = legacyNode.getIconFactory();
        final String policy = legacyNode.getPermissions().getPolicy().getName();
        final int classId = legacyNode.getClassId();
        final String objectKey;
        final int objectId;
        final String classKey;
        final boolean dynamic = legacyNode.isDynamic();
        final String cachedGeometry;
        final String lightweightJson;

        if (MetaObjectNode.class.isAssignableFrom(legacyNode.getClass())) {
            final MetaObjectNode metaObjectNode = (MetaObjectNode)legacyNode;
            objectId = metaObjectNode.getObjectId();
            
            if (className == null) {
                LOG.warn("className == null, trying to derive class name from object node '" + name + "' (" + id + ")");
                if ((metaObjectNode.getObject() != null) && (metaObjectNode.getObject().getMetaClass() != null)) {
                    className = metaObjectNode.getObject().getMetaClass().getTableName();
                }
            }

            if ((className != null) && (domain != null)) {
                objectKey = "/" + domain + "." + className + "/" + metaObjectNode.getObjectId();
            } else {
                objectKey = null;
                LOG.error("could not set object key of object node '" + name + "' (" + id
                            + "), domain or classKey == null");
            }

            cachedGeometry = CidsBeanJsonSerializer.toEwkt(metaObjectNode.getCashedGeometry());
            lightweightJson = metaObjectNode.getLightweightJson();
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug("node '" + name + "' (" + id + ") is no meta object node -> class node or pure node");
            }
            objectId = -1;
            objectKey = null;
            cachedGeometry = null;
            lightweightJson = null;
        }

        // cannot derive class name from meta class node since meta class node
        // does not contain a class object!
// if(className == null && MetaClassNode.class.isAssignableFrom(legacyNode.getClass())) {
// LOG.warn("className == null, trying to derive class name from class node '"+name+"' ("+id+")");
// final MetaClassNode metaClassNode = (MetaClassNode)legacyNode;
// }
        if ((className != null) && (domain != null)) {
            classKey = "/" + domain + "." + className;
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug("could not set class key of node '" + name + "' (" + id + "), "
                            + "domain or className == null, node is no class node -> pure node)");
            }
            classKey = null;
        }

        final CidsNode cidsNode = new CidsNode(
                id,
                name,
                description,
                domain,
                classKey,
                objectKey,
                dynamicChildren,
                clientSort,
                derivePermissionsFromClass,
                leaf,
                dynamic,
                icon,
                cachedGeometry,
                lightweightJson,
                iconFactory,
                policy,
                artificialId,
                classId,
                objectId);

        return cidsNode;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   domain     DOCUMENT ME!
     * @param   nodeQuery  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Sirius.server.middleware.types.Node createLegacyQueryNode(
            final String domain,
            final String nodeQuery) {
        final Sirius.server.middleware.types.Node legacyNode = new MetaNode(
                -1,
                domain,
                "QUERY NODE",
                "NO DESCRIPTION",
                false,
                CidsClassFactory.getFactory().createPolicy("STANDARD"),
                -1,
                null,
                false,
                -1);

        legacyNode.setDynamicChildrenStatement(nodeQuery);
        return legacyNode;
    }
}
