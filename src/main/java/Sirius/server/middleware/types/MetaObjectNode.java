/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package Sirius.server.middleware.types;

import Sirius.server.middleware.impls.domainserver.DomainServerClassCache;
import Sirius.server.newuser.permission.Policy;

import com.vividsolutions.jts.geom.Geometry;

import org.apache.commons.lang.builder.HashCodeBuilder;

import de.cismet.cids.dynamics.CidsBean;

import de.cismet.cids.nodepermissions.CustomNodePermissionProvider;

import de.cismet.cids.utils.ClassloadingHelper;

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
public class MetaObjectNode extends Node implements Comparable {

    //~ Instance fields --------------------------------------------------------

    protected int objectId;
    protected volatile MetaObject theObject;

    protected Geometry cashedGeometry;
    protected String lightweightJson;
    private final transient org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(this.getClass());

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new MetaObjectNode object.
     *
     * @param  cidsBean  DOCUMENT ME!
     */
    public MetaObjectNode(final CidsBean cidsBean) {
        this(
            -1,
            cidsBean.getMetaObject().getMetaClass().getDomain(),
            cidsBean.getMetaObject(),
            cidsBean.toString(),
            null,
            true,
            Policy.createWIKIPolicy(),
            -1,
            null,
            true);
    }

    /**
     * Creates a new MetaObjectNode object.
     *
     * @param  domain    DOCUMENT ME!
     * @param  objectId  DOCUMENT ME!
     * @param  classId   DOCUMENT ME!
     */
    public MetaObjectNode(
            final String domain,
            final int objectId,
            final int classId) {
        this(-1, null, null, domain, objectId, classId, true, Policy.createWIKIPolicy(), -1, null, true, null, null);
    }

    /**
     * Creates a new MetaObjectNode object.
     *
     * @param  domain           DOCUMENT ME!
     * @param  objectId         DOCUMENT ME!
     * @param  classId          DOCUMENT ME!
     * @param  name             DOCUMENT ME!
     * @param  cashedGeometry   DOCUMENT ME!
     * @param  lightweightJson  DOCUMENT ME!
     */
    public MetaObjectNode(
            final String domain,
            final int objectId,
            final int classId,
            final String name,
            final Geometry cashedGeometry,
            final String lightweightJson) {
        this(
            -1,
            name,
            null,
            domain,
            objectId,
            classId,
            true,
            Policy.createWIKIPolicy(),
            -1,
            null,
            true,
            cashedGeometry,
            lightweightJson);
    }

    /**
     * -----------------------------------------------
     *
     * @param  id                          DOCUMENT ME!
     * @param  localServerName             DOCUMENT ME!
     * @param  theObject                   DOCUMENT ME!
     * @param  name                        DOCUMENT ME!
     * @param  description                 DOCUMENT ME!
     * @param  isLeaf                      DOCUMENT ME!
     * @param  policy                      DOCUMENT ME!
     * @param  iconFactory                 DOCUMENT ME!
     * @param  icon                        DOCUMENT ME!
     * @param  derivePermissionsFromClass  DOCUMENT ME!
     */
    public MetaObjectNode(final int id,
            final String localServerName,
            final MetaObject theObject,
            final String name,
            final String description,
            final boolean isLeaf,
            final Policy policy,
            final int iconFactory,
            final String icon,
            final boolean derivePermissionsFromClass) {
        super(id, name, localServerName, description, isLeaf, policy, iconFactory, icon, derivePermissionsFromClass);
        this.theObject = theObject;
        if (theObject != null) {
            objectId = theObject.getID();
            classId = theObject.getClassID();
        } else {
            objectId = -1;
            classId = -1;
        }
    }

    /**
     * Creates a new MetaObjectNode object.
     *
     * @param  id                          DOCUMENT ME!
     * @param  name                        DOCUMENT ME!
     * @param  description                 DOCUMENT ME!
     * @param  domain                      DOCUMENT ME!
     * @param  objectId                    DOCUMENT ME!
     * @param  classId                     DOCUMENT ME!
     * @param  isLeaf                      DOCUMENT ME!
     * @param  policy                      DOCUMENT ME!
     * @param  iconFactory                 DOCUMENT ME!
     * @param  icon                        DOCUMENT ME!
     * @param  derivePermissionsFromClass  DOCUMENT ME!
     * @param  cashedGeometry              DOCUMENT ME!
     * @param  lightweightJson             DOCUMENT ME!
     */
    public MetaObjectNode(final int id,
            final String name,
            final String description,
            final String domain,
            final int objectId,
            final int classId,
            final boolean isLeaf,
            final Policy policy,
            final int iconFactory,
            final String icon,
            final boolean derivePermissionsFromClass,
            final Geometry cashedGeometry,
            final String lightweightJson) {
        this(
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
            null,
            cashedGeometry,
            lightweightJson);
    }

    /**
     * Creates a new MetaObjectNode object.
     *
     * @param  id                          DOCUMENT ME!
     * @param  name                        DOCUMENT ME!
     * @param  description                 DOCUMENT ME!
     * @param  domain                      DOCUMENT ME!
     * @param  objectId                    DOCUMENT ME!
     * @param  classId                     DOCUMENT ME!
     * @param  isLeaf                      DOCUMENT ME!
     * @param  policy                      DOCUMENT ME!
     * @param  iconFactory                 DOCUMENT ME!
     * @param  icon                        DOCUMENT ME!
     * @param  derivePermissionsFromClass  DOCUMENT ME!
     * @param  artificialId                DOCUMENT ME!
     * @param  cashedGeometry              DOCUMENT ME!
     * @param  lightweightJson             DOCUMENT ME!
     */
    public MetaObjectNode(final int id,
            final String name,
            final String description,
            final String domain,
            final int objectId,
            final int classId,
            final boolean isLeaf,
            final Policy policy,
            final int iconFactory,
            final String icon,
            final boolean derivePermissionsFromClass,
            final String artificialId,
            final Geometry cashedGeometry,
            final String lightweightJson) {
        super(
            id,
            name,
            domain,
            description,
            isLeaf,
            policy,
            iconFactory,
            icon,
            derivePermissionsFromClass,
            artificialId);

        this.objectId = objectId;
        this.classId = classId;
        this.cashedGeometry = cashedGeometry;
        this.lightweightJson = lightweightJson;

//        Class nodePermissonProviderClass=ClassloadingHelper.getDynamicClass(DomainServerClassCache.getInstance().getMetaClass(classId), ClassloadingHelper.CLASS_TYPE.NODE_PERMISSION_PROVIDER);
//        if (nodePermissonProviderClass!=null){
//            CustomNodePermissionProvider permissionProvider=(CustomNodePermissionProvider)nodePermissonProviderClass.newInstance();
//            permissionProvider.setObjectNode(this);
//            permissionProvider.getCustomReadPermissionDecisionforUser(u)
//        }
//        LOG.fatal(classId + "->" + DomainServerClassCache.getInstance().getMetaClass(classId).getTableName());
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public MetaObject getObject() {
        return theObject;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  theObject  DOCUMENT ME!
     */
    public void setObject(final MetaObject theObject) {
        this.theObject = theObject;
        if (theObject != null) {
            this.classId = theObject.getClassID();
            this.objectId = theObject.getID();
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean objectSet() {
        return theObject != null;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public int getObjectId() {
        return objectId;
    }

    @Override
    public int hashCode() {
        final HashCodeBuilder hb = new HashCodeBuilder();

        hb.append(id);
        hb.append(classId);
        hb.append(objectId);
        hb.append(domain);

        return hb.toHashCode();
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof MetaObjectNode)) {
            return false;
        }

        final MetaObjectNode o = (MetaObjectNode)other;

        return (id == o.id) && domain.equals(o.domain) && (objectId == o.objectId) && (classId == o.classId);
    }

    @Override
    public boolean deepEquals(final Object other) {
        if (!super.deepEquals(other)) {
            return false;
        }

        return objectId == ((MetaObjectNode)other).objectId;
    }
}
