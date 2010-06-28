/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package Sirius.server.localserver.attribute;

import Sirius.server.middleware.types.*;
import Sirius.server.newuser.permission.*;

import Sirius.util.*;

import de.cismet.cids.tools.fromstring.DateFromString;
import de.cismet.cids.tools.fromstring.FromStringCreator;
import de.cismet.cids.tools.fromstring.StringCreateable;
import de.cismet.cids.tools.tostring.StringConvertable;
import de.cismet.cids.tools.tostring.ToStringConverter;

import de.cismet.tools.BlacklistClassloading;

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
public class ObjectAttribute extends Attribute implements Mapable,
    java.io.Serializable,
    Renderable,
    Editable,
    StringCreateable,
    StringConvertable {

    //~ Static fields/initializers ---------------------------------------------

    /** Use serialVersionUID for interoperability. */
    private static final long serialVersionUID = 2266358985361133488L;

    private static String toStringConverterPrefix = "de.cismet.cids.custom.tostringconverter.";
    private static String toStringConverterPostfix = "ToStringConverter";

    //~ Instance fields --------------------------------------------------------

    // xxx not initialized yet
    public FromStringCreator objectCreator;
    // objekt zu dem das Attribut gehoert
    protected int objectID;
    // klasse des Objektes zu dem das Attribut gehoert????
    protected int classID;
    // Metainformation for this attribute (nachtraeglich dazugekommen)
    protected MemberAttributeInfo mai;
    protected Object deletedValue = null;
    protected String editor;
    protected String complexEditor;
    protected String toStringString;
    protected Sirius.server.localserver.object.Object parentObject;

    private transient org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(this.getClass());

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new ObjectAttribute object.
     *
     * @param  mai       DOCUMENT ME!
     * @param  objectID  DOCUMENT ME!
     * @param  value     DOCUMENT ME!
     * @param  policy    DOCUMENT ME!
     */
    public ObjectAttribute(final MemberAttributeInfo mai,
            final int objectID,
            final java.lang.Object value,
            final Policy policy) {
        // id????
        this(mai.getId() + "", mai, objectID, value, policy);
    }
    /**
     * /////////////constructor///////////////////////////////////////
     *
     * @param  id        DOCUMENT ME!
     * @param  mai       DOCUMENT ME!
     * @param  objectID  DOCUMENT ME!
     * @param  value     DOCUMENT ME!
     * @param  policy    DOCUMENT ME!
     */
    public ObjectAttribute(final String id,
            final MemberAttributeInfo mai,
            final int objectID,
            final java.lang.Object value,
            final Policy policy) {
        // Info wird zum Teil doppelt gehalten im mai Objekt und in der Superklasse
        // verursacht durch :  mai nachtraeglich eingefuegt
        super(id, mai.getName(), null, new PermissionHolder(policy), mai.isVisible());
        this.setMai(mai);
        this.objectID = objectID;
        this.classID = mai.getClassId();
        this.isArray = mai.isArray();
        super.typeId = mai.getTypeId();
        super.referencesObject = mai.foreignKey;
        super.optional = mai.isOptional();
        if (value instanceof java.lang.String) {
            this.value = ((String)value).trim();
        } else {
            this.value = value;
        }
        this.editor = mai.getEditor();
        this.complexEditor = mai.getComplexEditor();

        this.toStringString = mai.getToString();

        // toString
        // initRenderer(mai);

        // fromstring /////
        initFromString(mai);
    }

    //~ Methods ----------------------------------------------------------------

// public ObjectAttribute(String id, java.lang.Object value,int objectID, int classID,String name, String description, boolean visible)
//    {
//        super(id,name,description);
//        this.objectID=objectID;
//        this.classID=classID;
//
//        if(value instanceof java.lang.String)
//            this.value=((String)value).trim();
//        else
//            this.value=value;
//
//        this.visible=visible;
//    }
    /////////////////////////methods///////////////////////////////////
    /**
     * Ein teil des Visitor-Konzeptes. Diese Fkt ruft die visitMA Fkt aus dem interface TypeVisitor auf.
     *
     * @param   mov  Implementation des TypeVisitor-Interfaces das beschreibt was in diesem Objekt gemacht werden soll
     *               wenn diese Funktion aufgerufen wird.
     * @param   o    ein Objekt f\u00FCr evt Parameter. H\u00E4ngt von Implementation des TypeVisitor-Interfaces ab, ob
     *               und wie es benutzt wird.
     *
     * @return  das Ergebnis der Verarbeitung bei aufruf dieser Funktion. Es wird der Returnwert der visitMA(...) Fkt
     *          aus dem Inteface TypeVisitor geliefert.
     */
    public Object accept(final TypeVisitor mov, final Object o) {
        return mov.visitMA(this, o);
    }

    /**
     * Getter for property classID.
     *
     * @return  Value of property classID.
     */
    public int getClassID() {
        return classID;
    }

    /**
     * Setter for property classID.
     *
     * @param  classID  New value of property classID.
     */
    public void setClassID(final int classID) {
        this.classID = classID;
    }

    /**
     * Setter for property objectID.
     *
     * @param  objectID  New value of property objectID.
     */
    public void setObjectID(final int objectID) {
        this.objectID = objectID;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    final int getObjectID() {
        return objectID;
    }

    // mapable
    @Override
    public java.lang.Object getKey() {
        return id + "@" + classID;
    }

    @Override
    public Object constructKey(final Mapable m) {
        return super.constructKey(m);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getToStringString() {
        return toStringString;
    }

    // ggf zu \u00E4ndern
    @Override
    public String getRenderer() {
        return toStringString;
    }

    @Override
    public Object fromString(final String objectRepresentation, final java.lang.Object mo) throws Exception {
        return objectCreator.create(objectRepresentation, mo);
    }

    @Override
    public boolean isStringCreateable() {
        return (objectCreator != null);
    }
    // Hell

    @Override
    public String getComplexEditor() {
        if (this.complexEditor == null) {
            complexEditor = "Sirius.navigator.ui.attributes.editor.metaobject.DefaultComplexMetaAttributeEditor";
        }
        if (this.editor == null) {
            editor = "Sirius.navigator.ui.attributes.editor.metaobject.DefaultSimpleComplexMetaAttributeEditor";
        }
        if (this.referencesObject()) {
            return complexEditor;
        } else {
            return editor;
        }
    }

    @Override
    public String getSimpleEditor() {
        return editor;
    }

    /**
     * Setter for property complexEditor.
     *
     * @param  complexEditor  New value of property complexEditor.
     */
    public void setComplexEditor(final java.lang.String complexEditor) {
        this.complexEditor = complexEditor;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public ToStringConverter getToStringConverter() {
        if (toStringConverter == null) {
            String classNameToLoad = getToStringConverterClassNameByConvention();
            toStringConverter = loadToStringConverterByClassName(classNameToLoad);
            if (toStringConverter == null) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Could not load ToStringConverter for Attribute " + mai.name + " by convention.");
                }
                classNameToLoad = getToStringConverterClassNameByConfiguration();
                toStringConverter = loadToStringConverterByClassName(classNameToLoad);
                if (toStringConverter == null) {
                    if (logger.isDebugEnabled()) {
                        logger.debug(
                            "Could not load ToStringConverter for Attribute "
                                    + mai.name
                                    + " by configuration. Using default");
                    }
                    toStringConverter = new ToStringConverter();
                }
            }
        }
        return toStringConverter;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private String getToStringConverterClassNameByConfiguration() {
        if (toStringString != null) {
            return toStringString.trim();
        }
        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private String getToStringConverterClassNameByConvention() {
        final Sirius.server.localserver.object.Object parObj = parentObject;
        if (parObj instanceof MetaObject) {
            final MetaObject mo = (MetaObject)parObj;
            final String tabletoLower = mo.getMetaClass().getTableName().toLowerCase();
            final String domainToLower = mo.getDomain().toLowerCase();
            final String fieldnameToLower = mai.getFieldName().toLowerCase();
            final String fieldNamePreparedForClassName = fieldnameToLower.substring(0, 1).toUpperCase()
                        + fieldnameToLower.substring(1);
            final StringBuffer lazyClassName = new StringBuffer(toStringConverterPrefix).append(domainToLower)
                        .append(".")
                        .append(tabletoLower)
                        .append(".")
                        .append(fieldNamePreparedForClassName)
                        .append(toStringConverterPostfix);
            return lazyClassName.toString();
        } else {
            logger.warn("Attribute parent object is not a MetaObject on " + mai.getName() + "!");
        }
        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   className  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private ToStringConverter loadToStringConverterByClassName(final String className) {
        if (className != null) {
            try {
                final Class<?> converterClass = BlacklistClassloading.forName(className.toString());
                if (converterClass != null) {
                    if (ToStringConverter.class.isAssignableFrom(converterClass)) {
                        return (ToStringConverter)converterClass.newInstance();
                    } else {
                        logger.warn("Class " + className + " is not subtype of ToStringConverter!");
                    }
                }
            } catch (Throwable t) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Error while trying to load ToStringConverter " + className.toString() + " !", t);
                }
            }
        }
        return null;
    }

    @Override
    public String toString() {
        setLogger();
        if (logger.isDebugEnabled()) {
            logger.debug("entered toString for ObjectAttribute value=" + value);
        }

        if (value != null) {
            final ToStringConverter toStringConv = getToStringConverter();
            if (toStringConv != null) {
                return toStringConv.convert(this);
            } else {
                return value.toString();
            }
        }
        logger.warn("Value is null!");
        return "";
    }
    /**
     * muss total neu gemacht werden.
     *
     * @param  mai  DOCUMENT ME!
     */
    protected void initFromString(final MemberAttributeInfo mai) {
        final String fromString = mai.getFromString();
        if (fromString != null) {
            try {
                if (logger.isDebugEnabled()) {
                    logger.debug("<LS> info :: try to load fromString if not null : " + fromString);
                }

                final java.lang.Class c0 = java.lang.Class.forName("Sirius.util.FromStringCreator");
                final java.lang.Class c = java.lang.Class.forName(fromString.trim());

                if (c0.isAssignableFrom(c)) {
                    this.objectCreator = (FromStringCreator)c.newInstance();
                    if (logger.isDebugEnabled()) {
                        logger.debug(this.objectCreator + "vom typ" + fromString + " erfolgreich zugewiesen");
                    }
                } else {
                    logger.warn(
                        "<LS> info ::  fromSTringObjectCreator "
                                + fromString
                                + "nicht geladen: reference is :"
                                + this.objectCreator);
                }
            } catch (Exception e) {
                logger.error(
                    "<LS> ERROR :: "
                            + fromString
                            + " f\u00FCr Klasse "
                            + name
                            + " konnte nicht geladen werden set string converter to Default ",
                    e);
            }
        } else // fromString==null nicht gesetz aber value evtl vorhanden
        {
            // default from string
            if ((value instanceof java.sql.Date)
                        || (value instanceof java.util.Date)
                        || ((typeId > 78) && (typeId < 87))) {
                this.objectCreator = new DateFromString();
            }
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
    public MemberAttributeInfo getMai() {
        return mai;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  mai  DOCUMENT ME!
     */
    public void setMai(final MemberAttributeInfo mai) {
        this.mai = mai;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Sirius.server.localserver.object.Object getParentObject() {
        return parentObject;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  parentObject  DOCUMENT ME!
     */
    public void setParentObject(final Sirius.server.localserver.object.Object parentObject) {
        this.parentObject = parentObject;
    }
//
//    public Object getDeletedValue() {
//        return deletedValue;
//    }
//
//    public void setDeletedValue(Object deletedValue) {
//        this.deletedValue = deletedValue;
//    }
} // end of class
