/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package Sirius.server.localserver.attribute;

import Sirius.server.middleware.types.MetaObject;
import Sirius.server.middleware.types.TypeVisitor;
import Sirius.server.newuser.permission.PermissionHolder;
import Sirius.server.newuser.permission.Policy;

import Sirius.util.Editable;
import Sirius.util.Mapable;
import Sirius.util.Renderable;

import org.apache.log4j.Logger;

import de.cismet.cids.tools.fromstring.DateFromString;
import de.cismet.cids.tools.fromstring.FromStringCreator;
import de.cismet.cids.tools.fromstring.StringCreateable;
import de.cismet.cids.tools.tostring.StringConvertable;
import de.cismet.cids.tools.tostring.ToStringConverter;

import de.cismet.cids.utils.ClassloadingHelper;

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
    private static final transient Logger LOG = Logger.getLogger(ObjectAttribute.class);

    //~ Instance fields --------------------------------------------------------

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

    // NOI18N
    // xxx not initialized yet
    private FromStringCreator objectCreator;

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
        this(mai.getId() + "", mai, objectID, value, policy); // NOI18N
    }

    /**
     * Creates a new ObjectAttribute object.
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

        initFromString(mai);
    }

    //~ Methods ----------------------------------------------------------------

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
        return id + "@" + classID; // NOI18N
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

    @Override
    public String getRenderer() {
        return toStringString;
    }

    @Override
    public Object fromString(final String objectRepresentation, final Object mo) throws Exception {
        return objectCreator.create(objectRepresentation, mo);
    }

    @Override
    public boolean isStringCreateable() {
        return (objectCreator != null);
    }

    @Override
    public String getComplexEditor() {
        if (this.complexEditor == null) {
            complexEditor = "Sirius.navigator.ui.attributes.editor.metaobject.DefaultComplexMetaAttributeEditor"; // NOI18N
        }
        if (this.editor == null) {
            editor = "Sirius.navigator.ui.attributes.editor.metaobject.DefaultSimpleComplexMetaAttributeEditor";  // NOI18N
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
    public void setComplexEditor(final String complexEditor) {
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
            final Sirius.server.localserver.object.Object parObj = parentObject;
            if (parObj instanceof MetaObject) {
                final MetaObject mo = (MetaObject)parObj;
                try {
                    final Class<?> converterClass = ClassloadingHelper.getDynamicClass(mo.getMetaClass(),
                            mai,
                            ClassloadingHelper.CLASS_TYPE.TO_STRING_CONVERTER);
                    if (converterClass != null) {
                        if (ToStringConverter.class.isAssignableFrom(converterClass)) {
                            toStringConverter = (ToStringConverter)converterClass.newInstance();
                        } else {
                            LOG.warn("Class " + converterClass + " is not subtype of ToStringConverter!");
                        }
                    }
                } catch (final Throwable t) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Error while trying to load ToStringConverter !", t);
                    }
                }
                if (toStringConverter == null) {
                    toStringConverter = new ToStringConverter();
                }
            }
        }

        return toStringConverter;
    }

    @Override
    public String toString() {
        if (value != null) {
            final ToStringConverter toStringConv = getToStringConverter();
            if (toStringConv != null) {
                return toStringConv.convert(this);
            } else {
                return value.toString();
            }
        }

        return ""; // NOI18N
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
                if (LOG.isDebugEnabled()) {
                    LOG.debug("<LS> info :: try to load fromString if not null : " + fromString); // NOI18N
                }

                final java.lang.Class c0 = java.lang.Class.forName("Sirius.util.FromStringCreator"); // NOI18N
                final java.lang.Class c = java.lang.Class.forName(fromString.trim());

                if (c0.isAssignableFrom(c)) {
                    this.objectCreator = (FromStringCreator)c.newInstance();
                    if (LOG.isDebugEnabled()) {
                        LOG.debug(this.objectCreator + " of type " + fromString + " successfully assigned"); // NOI18N
                    }
                } else {
                    LOG.warn(
                        "<LS> info ::  fromSTringObjectCreator "                                             // NOI18N
                                + fromString
                                + "nicht geladen: reference is :"                                            // NOI18N
                                + this.objectCreator);
                }
            } catch (Exception e) {
                LOG.error(
                    "<LS> ERROR :: "                                                                         // NOI18N
                            + fromString
                            + " f\u00FCr Klasse "                                                            // NOI18N
                            + name
                            + " konnte nicht geladen werden set string converter to Default ",               // NOI18N
                    e);
            }
        } else {
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
}
