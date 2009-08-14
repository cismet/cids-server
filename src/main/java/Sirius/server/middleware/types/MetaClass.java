package Sirius.server.middleware.types;


import Sirius.server.localserver.attribute.AttributeVector;
import Sirius.server.localserver.attribute.MemberAttributeInfo;
import Sirius.server.localserver.attribute.ObjectAttribute;
import Sirius.util.Editable;
import Sirius.util.Groupable;
import Sirius.util.Renderable;
import de.cismet.cids.tools.tostring.ToStringConverter;
import java.util.Iterator;
import java.util.LinkedHashMap;

/**
 *Return Type of a RMI method
 *
 */
public class MetaClass extends Sirius.server.localserver._class.Class
        implements java.io.Serializable, Groupable, Renderable, Editable {

    private transient org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(this.getClass());
    /**
     * erzeugt eine String repr\u00E4entation eines Objketes der Klasse, kann in toString benutzt werden
     */
    private transient ToStringConverter toStringConverter;
    /**
     * domain
     */
    protected String domain;
    private transient Class javaClass = null;

    //-------------------------------------------------------------------
    /**
     * constuructor adding the domain
     * @param c "server" class
     * @param domain domain
     */
    public MetaClass(Sirius.server.localserver._class.Class c, String domain) {
        /*SystemProperties*/

        super(c.getID(), c.getName(), c.getDescription(), c.getIcon(), c.getObjectIcon(), c.getTableName(), c.getPrimaryKey(), c.getToString(), c.getPermissions(), c.getAttributePolicy());
        super.attribs = new AttributeVector(c.getAttributes());
        super.memberAttributeInfos = new LinkedHashMap(c.getMemberAttributeInfos());
        //Hell
        super.setEditor(c.getEditor());

        super.setRenderer(c.getRenderer());

        this.domain = domain;
        this.arrayElementLink = c.isArrayElementLink();
    }

    /**
     * grouping criterion
     * @return grouping criterion
     */
    public String getGroup() {

        return domain;
    }

    /**
     * getter for icon
     * @return class icon (image)
     */
    public byte[] getIconData() {

        return icon.getImageData();
    }

    /**
     * getter for object icon
     * @return object icon (image)
     */
    public byte[] getObjectIconData() {

        return objectIcon.getImageData();
    }

    /**
     * methods
     * @return methods
     */
    public int[] getMethodArray() {

        return methodIDs.convertToArray();
    }

    /**
     * getter for domain
     * @return domain
     */
    public String getDomain() {

        return domain;
    }

    /**
     * getter for key
     * @return key
     */
    public Object getKey() {

        return id + "@" + domain;
    }

    /**
     * getter for simple editor
     * @return simple editor
     */
    public String getSimpleEditor() {
        return editor;
    }

    /**
     * getter for renderer
     * @return renderer
     */
    public String getRenderer() {
        return renderer;
    }

    /**
     * getter for complex editor
     * @return complex editor
     */
    public String getComplexEditor() {
        return editor;
    }

//    public String toString()
//    { return super.toString()+"@"+domain;}
    /**
     * getter for toStringConverter
     * @return toStringConverter
     */
    public ToStringConverter getToStringConverter() {
        if (toStringConverter == null) {
            loadToStringConverter();
        }

        return toStringConverter;

    }

//    public void setToStringConverter(ToStringConverter toStringConverter)
//    {
//        this.toStringConverter = toStringConverter;
//    }
    private void loadToStringConverter() {
        setLogger();
        try {
            if (logger != null) {
                logger.debug("try to load stringconverter if not null : " + toString);
            }

            if (toString != null) {
                java.lang.Class c = java.lang.Class.forName(toString.trim());

                if (de.cismet.cids.tools.tostring.ToStringConverter.class.isAssignableFrom(c)) {
                    this.toStringConverter = (ToStringConverter) c.newInstance();
                } else if (logger != null) {
                    logger.debug(" customized stringconverter could not be loaded as ClassQualifer ist not a valid ToSTringconverter " + toString);
                }
            } else // no toString defined set to Standard
            {
                this.toStringConverter = new ToStringConverter();
                logger.debug(" default stringconverter loaded: reference is :" + this.toStringConverter);
            }



        } catch (Exception e) {

            if (logger != null) {
                logger.error(toString + " f\u00FCr Klasse " + name + " konnte nicht geladen werden set string converter to Default ", e);
            }
            this.toStringConverter = new ToStringConverter();
        }


    }

    public void setLogger() {
        if (logger == null) {
            logger = org.apache.log4j.Logger.getLogger(this.getClass());
        }
    }

    public org.apache.log4j.Logger getLogger() {
        if (logger == null) {
            setLogger();
        }
        return logger;
    }

    public int getId() {
        return super.getID();
    }

    public Class getJavaClass() {
        if (javaClass == null) {
            try {

                javaClass = BeanFactory.getInstance().getJavaClass(this);
            } catch (Exception e) {
                getLogger().error("Javaklasse fuer " + this.getName() + " konnte nicht erzeugt werden.", e);
            }
        }
        return javaClass;
    }

    /**
     * returns an empty instance of the metaClass. only first level initialization. no arrays. no subobjects.
     * @return MetaObject
     */
    public MetaObject getEmptyInstance() {
        try {
            Sirius.server.localserver.object.Object o = new Sirius.server.localserver.object.DefaultObject(-1, getId());
            o.setStatus(Sirius.server.localserver.object.Object.NEW);


            Iterator iter = getMemberAttributeInfos().values().iterator();


            while (iter.hasNext()) {

                MemberAttributeInfo mai = (MemberAttributeInfo) iter.next();

                ObjectAttribute oAttr;
                oAttr = new ObjectAttribute(mai, -1, null, getAttributePolicy());

                oAttr.setVisible(mai.isVisible());
                oAttr.setSubstitute(mai.isSubstitute());
                oAttr.setReferencesObject(mai.isForeignKey());

                oAttr.setIsPrimaryKey(mai.getFieldName().equalsIgnoreCase(getPrimaryKey()));
                if (oAttr.isPrimaryKey()){
                    oAttr.setValue(-1);
                }
                oAttr.setOptional(mai.isOptional());

                oAttr.setClassKey(mai.getForeignKeyClassId() + "@" + domain);
                o.addAttribute(oAttr);
            }

            return new DefaultMetaObject(o, getDomain());
        } catch (Exception e) {
            getLogger().error("Fehler in getEmptyInstance", e);
            return null;
        }
    }



}