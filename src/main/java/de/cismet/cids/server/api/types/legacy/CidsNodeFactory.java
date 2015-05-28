/**
 * *************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 * 
* ... and it just works.
 * 
***************************************************
 */
package de.cismet.cids.server.api.types.legacy;
import Sirius.server.middleware.types.MetaClassNode;
import Sirius.server.middleware.types.MetaNode;
import Sirius.server.middleware.types.MetaObjectNode;
import Sirius.server.middleware.types.Node;
import Sirius.server.newuser.permission.Policy;
import de.cismet.cids.server.api.types.CidsNode;
import org.apache.log4j.Logger;

/**
 * A factory class for converting between legacy cids types and REST/JSON types.
 * TODO: Integrate into <strong>cids-server-rest-types project</strong>!
 *
 * @author Pascal Dihé
 */
public class CidsNodeFactory {

    private final static transient Logger LOG = Logger.getLogger(CidsNodeFactory.class);
    private final static CidsNodeFactory factory = new CidsNodeFactory();

    private CidsNodeFactory() {
    }

    public final static CidsNodeFactory getFactory() {
        return factory;
    }

    /**
     * Transforms a cids rest API node into a cids legacy node (Sirius) object.
     *
     *
     * @param cidsNode the cids rest node to be converted
     * @return the converted cids legacy node
     * @throws Exception if any error occurs during the conversion
     */
    public Sirius.server.middleware.types.Node legacyCidsNodeFromRestCidsNode(final de.cismet.cids.server.api.types.CidsNode cidsNode) throws Exception {
        
        final int id = cidsNode.getId() != null ? Integer.parseInt(cidsNode.getId()) : -1;
        final String name = cidsNode.getName();
        final String description = cidsNode.getDescription();
        final String domain = cidsNode.getDomain();
        final int classId = cidsNode.getClassId();
        final boolean isLeaf = cidsNode.isLeaf();
        final Policy policy = CidsClassFactory.getFactory().createPolicy(cidsNode.getPolicy());
        final int iconFactory = cidsNode.getIconFactory();
        final String icon  = cidsNode.getIcon();
        final boolean derivePermissionsFromClass = cidsNode.isDerivePermissionsFromClass();
        final String artificialId = cidsNode.getArtificialId();
        final String dynamicChildrenStatement = cidsNode.getDynamicChildren();
        final boolean sqlSort = cidsNode.isClientSort();
        
        final Node legacyNode;
 
        if(cidsNode.getObjectKey() != null && !cidsNode.getObjectKey().isEmpty()) {
            LOG.debug("node '"+cidsNode.getName()+"' ("+cidsNode.getId()+") will be converted to meta object node");
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
                    artificialId);
        } else if(cidsNode.getClassKey() != null && !cidsNode.getClassKey().isEmpty()) {
            LOG.debug("node '"+cidsNode.getName()+"' ("+cidsNode.getId()+") will be converted to meta class node");
            legacyNode = new MetaClassNode(
                    id, 
                    domain, 
                    classId, 
                    name, 
                    description, 
                    isLeaf, 
                    policy,
                    iconFactory, 
                    icon, 
                    derivePermissionsFromClass, 
                    classId, 
                    artificialId);
        } else {
            LOG.debug("node '"+cidsNode.getName()+"' ("+cidsNode.getId()+") will be converted to meta node");
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
        return legacyNode;        
    }

    /**
     * Transforms a cids legacy node(Sirius) object into a cids rest API node.<br>
     * Since a meta class node (in contrast to meta object node) does not contain 
     * a meta class instance, we have to pass the class name parameter in order to
     * be able to create a cids class $self reference for the property classKey!
     * 
     * @param legacyNode the cids legacy node to be converted
     * @param className name (table name) of the class associated with the node
     * @return the converted cids rest node
     */
    public de.cismet.cids.server.api.types.CidsNode restCidsNodeFromLegacyCidsNode(
            final Sirius.server.middleware.types.Node legacyNode, String className) {
        final String id = Integer.toString(legacyNode.getId());
        final String name = legacyNode.getName();
        final String domain = legacyNode.getDomain();
        final String description = legacyNode.getDescription();
        final String artificialId = legacyNode.getArtificialId();
        final String dynamicChildren = legacyNode.getDynamicChildrenStatement();
        final boolean clientSort = legacyNode.isSqlSort();
        final boolean derivePermissionsFromClass = legacyNode.isDerivePermissionsFromClass();
        final boolean isLeaf = legacyNode.isLeaf();
        final String icon = legacyNode.getIconString();
        final int iconFactory = legacyNode.getIconFactory();
        final String policy = legacyNode.getPermissions().getPolicy().getName();
        final int classId = legacyNode.getClassId();
        final String objectKey;
        final String classKey;
        
        
        if(MetaObjectNode.class.isAssignableFrom(legacyNode.getClass())) {
            final MetaObjectNode metaObjectNode = (MetaObjectNode)legacyNode;
            if(className == null) {
                LOG.warn("className == null, trying to derive class name from object node '"+name+"' ("+id+")");
                if(metaObjectNode.getObject() != null && metaObjectNode.getObject().getMetaClass() != null) {
                    className = metaObjectNode.getObject().getMetaClass().getTableName();
                }   
            }
            
            if(className != null && domain != null) {
                objectKey = "/"+domain + "." + className + "/" + ((MetaObjectNode)legacyNode).getObjectId();
            } else {
                objectKey = null;
                LOG.error("could not set object key of object node '"+name+"' ("+id+"), domain or classKey == null");
            }
        } else {
            LOG.debug("node '"+name+"' ("+id+") is no meta object node!");
            objectKey = null;
        }
        
        // cannot derive class name from meta class node since meta class node
        // does not contain a class object!
//        if(className == null && MetaClassNode.class.isAssignableFrom(legacyNode.getClass())) {
//            LOG.warn("className == null, trying to derive class name from class node '"+name+"' ("+id+")");
//            final MetaClassNode metaClassNode = (MetaClassNode)legacyNode;
//        }
        
        if(className != null && domain != null) {
            classKey = "/"+domain+"."+className;
        } else {
            LOG.warn("could not set class key of node '"+name+"' ("+id+"), domain or className == null");
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
                isLeaf, 
                icon, 
                iconFactory, 
                policy, 
                artificialId,
                classId);

        return cidsNode;
    }
}
