/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package Sirius.server.middleware.types;

import Sirius.server.localserver.attribute.Attribute;
import Sirius.server.localserver.attribute.AttributeVector;
import Sirius.server.localserver.attribute.MemberAttributeInfo;
import Sirius.server.localserver.attribute.ObjectAttribute;

import Sirius.util.Editable;
import Sirius.util.Groupable;
import Sirius.util.Renderable;

import de.cismet.cids.tools.tostring.ToStringConverter;

import de.cismet.tools.BlacklistClassloading;

import java.util.Iterator;
import java.util.LinkedHashMap;

/**
 * Return Type of a RMI method.
 *
 * @version  $Revision$, $Date$
 */
public class MetaClass extends Sirius.server.localserver._class.Class implements java.io.Serializable,
    Groupable,
    Renderable,
    Editable {

    //~ Static fields/initializers ---------------------------------------------

    private static String toStringConverterPrefix = "de.cismet.cids.custom.tostringconverter.";   // NOI18N
    private static String toStringConverterPostfix = "ToStringConverter";   // NOI18N

    //~ Instance fields --------------------------------------------------------

    /** domain. */
    protected String domain;

    private transient org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(this.getClass());
    /** erzeugt eine String repr\u00E4entation eines Objketes der Klasse, kann in toString benutzt werden. */
    private transient ToStringConverter toStringConverter;
    private transient Class javaClass = null;
    private Boolean hasExtensionAttributes = null;
    // -------------------------------------------------------------------

    //~ Constructors -----------------------------------------------------------

    /**
     * constuructor adding the domain.
     *
     * @param  c       "server" class
     * @param  domain  domain
     */
    public MetaClass(Sirius.server.localserver._class.Class c, String domain) {
        /*SystemProperties*/

        super(
            c.getID(),
            c.getName(),
            c.getDescription(),
            c.getIcon(),
            c.getObjectIcon(),
            c.getTableName(),
            c.getPrimaryKey(),
            c.getToString(),
            c.getPermissions(),
            c.getAttributePolicy());
        super.attribs = new AttributeVector(c.getAttributes());
        super.memberAttributeInfos = new LinkedHashMap(c.getMemberAttributeInfos());
        // Hell
        super.setEditor(c.getEditor());

        super.setRenderer(c.getRenderer());

        this.domain = domain;
        this.arrayElementLink = c.isArrayElementLink();
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * grouping criterion.
     *
     * @return  grouping criterion
     */
    public String getGroup() {
        return domain;
    }

    /**
     * getter for icon.
     *
     * @return  class icon (image)
     */
    public byte[] getIconData() {
        return icon.getImageData();
    }

    /**
     * getter for object icon.
     *
     * @return  object icon (image)
     */
    public byte[] getObjectIconData() {
        return objectIcon.getImageData();
    }

    /**
     * methods.
     *
     * @return  methods
     */
    public int[] getMethodArray() {
        return methodIDs.convertToArray();
    }

    /**
     * getter for domain.
     *
     * @return  domain
     */
    public String getDomain() {
        return domain;
    }

    /**
     * getter for key.
     *
     * @return  key
     */
    public Object getKey() {
        return id + "@" + domain;   // NOI18N
    }

    /**
     * getter for simple editor.
     *
     * @return  simple editor
     */
    public String getSimpleEditor() {
        return editor;
    }

    /**
     * getter for renderer.
     *
     * @return  renderer
     */
    public String getRenderer() {
        return renderer;
    }

    /**
     * getter for complex editor.
     *
     * @return  complex editor
     */
    public String getComplexEditor() {
        return editor;
    }

//    public String toString()
//    { return super.toString()+"@"+domain;}
    /**
     * getter for toStringConverter.
     *
     * @return  toStringConverter
     */
    public ToStringConverter getToStringConverter() {
        if (toStringConverter == null) {
            loadToStringConverter();
        }

        return toStringConverter;
    }
    /**
     * public void setToStringConverter(ToStringConverter toStringConverter) { this.toStringConverter =
     * toStringConverter; }.
     */
    private void loadToStringConverter() {
        setLogger();
        try {
            if (logger != null) {
                if (logger.isDebugEnabled()) {
                    logger.debug("try to load stringconverter if not null : " + toString);   // NOI18N
                }
            }

            Class converterClass = null;
            String lazyClassName = null;

            try {
                String tableNamePreparedForClassName = getTableName().substring(0, 1).toUpperCase()
                    + getTableName().substring(1).toLowerCase();
                lazyClassName = toStringConverterPrefix + (domain + ".").toLowerCase() + tableNamePreparedForClassName   // NOI18N
                    + toStringConverterPostfix;
                converterClass = BlacklistClassloading.forName(lazyClassName);
            } catch (Exception e) {
                if (logger.isDebugEnabled()) {
                    logger.debug("no lazy toStringConverter found (" + lazyClassName + ")");   // NOI18N
                }
            }

            if ((converterClass == null) && (toString != null)) {
                converterClass = BlacklistClassloading.forName(toString.trim());
            }
            if (
                (converterClass != null)
                        && de.cismet.cids.tools.tostring.ToStringConverter.class.isAssignableFrom(converterClass)) {
                this.toStringConverter = (ToStringConverter)converterClass.newInstance();
            } else if (logger != null) {
                if (logger.isDebugEnabled()) {
                    logger.debug(
                        " customized stringconverter could not be loaded as ClassQualifer ist not a valid ToSTringconverter "   // NOI18N
                        + toString);
                }
            }

            if (converterClass == null) {
                this.toStringConverter = new ToStringConverter();
                if (logger.isDebugEnabled()) {
                    logger.debug(" default stringconverter loaded: reference is :" + this.toStringConverter);   // NOI18N
                }
            }
        } catch (Exception e) {
            if (logger != null) {
                logger.error(
                    toString + " for Klasse " + name   // NOI18N
                    + " could not be loaded. Set string converter to Default ",   // NOI18N
                    e);
            }
            this.toStringConverter = new ToStringConverter();
        }
    }

    /**
     * DOCUMENT ME!
     */
    public void setLogger() {
        if (logger == null) {
            logger = org.apache.log4j.Logger.getLogger(this.getClass());
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public org.apache.log4j.Logger getLogger() {
        if (logger == null) {
            setLogger();
        }
        return logger;
    }

    public int getId() {
        return super.getID();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Class getJavaClass() {
        if (javaClass == null) {
            try {
                javaClass = BeanFactory.getInstance().getJavaClass(this);
            } catch (Exception e) {
                getLogger().error("Javaclass for " + this.getName() + " could not be created.", e);   // NOI18N
            }
        }
        return javaClass;
    }

    /**
     * returns an empty instance of the metaClass. only first level initialization. no arrays. no subobjects.
     *
     * @return  MetaObject
     */
    public MetaObject getEmptyInstance() {
        try {
            Sirius.server.localserver.object.Object o = new Sirius.server.localserver.object.DefaultObject(-1, getId());
            o.setStatus(Sirius.server.localserver.object.Object.NEW);

            Iterator iter = getMemberAttributeInfos().values().iterator();

            while (iter.hasNext()) {
                MemberAttributeInfo mai = (MemberAttributeInfo)iter.next();

                ObjectAttribute oAttr;
                oAttr = new ObjectAttribute(mai, -1, null, getAttributePolicy());

                oAttr.setVisible(mai.isVisible());
                oAttr.setSubstitute(mai.isSubstitute());
                oAttr.setReferencesObject(mai.isForeignKey());

                oAttr.setIsPrimaryKey(mai.getFieldName().equalsIgnoreCase(getPrimaryKey()));
                if (oAttr.isPrimaryKey()) {
                    oAttr.setValue(-1);
                }
                oAttr.setOptional(mai.isOptional());

                oAttr.setClassKey(mai.getForeignKeyClassId() + "@" + domain);   // NOI18N
                o.addAttribute(oAttr);
            }

            return new DefaultMetaObject(o, getDomain());
        } catch (Exception e) {
            getLogger().error("Error in getEmptyInstance", e);   // NOI18N
            return null;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean hasExtensionAttributes() {
        if (hasExtensionAttributes == null) {
            Iterator iter = getMemberAttributeInfos().values().iterator();
            while (iter.hasNext()) {
                MemberAttributeInfo mai = (MemberAttributeInfo)iter.next();
                if (mai.isExtensionAttribute()) {
                    hasExtensionAttributes = true;
                    break;
                }
            }
            if (hasExtensionAttributes == null) {
                hasExtensionAttributes = false;
            }
        }
        return hasExtensionAttributes;
    }
}
