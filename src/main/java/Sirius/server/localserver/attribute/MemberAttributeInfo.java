/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package Sirius.server.localserver.attribute;

import Sirius.util.Mapable;

/**
 * DOCUMENT ME!
 *
 * @author   schlob
 * @version  $Revision$, $Date$
 */
public class MemberAttributeInfo implements Mapable, java.io.Serializable {

    //~ Static fields/initializers ---------------------------------------------

    /** Use serialVersionUID for interoperability. */
    private static final long serialVersionUID = 1172278959184473585L;

    //~ Instance fields --------------------------------------------------------

    // attribut id aus cs_attr
    protected int id;
    // Klasse zu der das Attribut gehoert
    protected int classId;
    // attribute type
    protected int typeId;
    protected String name;
    // field name
    protected String fieldName;
    //
    protected boolean foreignKey;
    protected boolean substitute;
    protected int foreignKeyClassId;
    protected String arrayKeyFieldName;
    protected boolean visible;
    protected boolean indexed;
    protected boolean isArray = false;
    protected String editor;
    protected String toString;
    protected String defaultValue;
    protected boolean optional = false;
    protected String fromString;
    protected int position;
    private String complexEditor;
    private String renderer;
    private String javaclassname = null;
    private boolean extensionAttribute = false;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new MemberAttributeInfo object.
     */
    public MemberAttributeInfo() {
        this(-1, -1, -1, null, null, false, false, -1, false, false, false,
            null, null, null, -1);
    }

    /**
     * Creates a new instance of MemberAttributeInfo.
     *
     * @param  id                 DOCUMENT ME!
     * @param  classId            DOCUMENT ME!
     * @param  typeId             DOCUMENT ME!
     * @param  name               DOCUMENT ME!
     * @param  fieldName          DOCUMENT ME!
     * @param  foreignKey         DOCUMENT ME!
     * @param  substitute         DOCUMENT ME!
     * @param  foreignKeyClassId  DOCUMENT ME!
     * @param  visible            DOCUMENT ME!
     * @param  indexed            DOCUMENT ME!
     * @param  isArray            DOCUMENT ME!
     * @param  arrayKeyFieldName  DOCUMENT ME!
     * @param  fromString         DOCUMENT ME!
     * @param  toString           DOCUMENT ME!
     * @param  position           DOCUMENT ME!
     */
    public MemberAttributeInfo(
            final int id,
            final int classId,
            final int typeId,
            final String name,
            final String fieldName,
            final boolean foreignKey,
            final boolean substitute,
            final int foreignKeyClassId,
            final boolean visible,
            final boolean indexed,
            final boolean isArray,
            final String arrayKeyFieldName,
            final String fromString,
            final String toString,
            final int position) {
        this.id = id;
        this.classId = classId;
        this.typeId = typeId;
        this.name = name;
        this.fieldName = fieldName;
        this.foreignKey = foreignKey;
        this.substitute = substitute;
        this.foreignKeyClassId = foreignKeyClassId;
        this.visible = visible;
        this.indexed = indexed;
        this.isArray = isArray;
        this.arrayKeyFieldName = arrayKeyFieldName;
        this.fromString = fromString;
        this.toString = toString;
        this.position = position;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * Getter for property fieldName.
     *
     * @return  Value of property fieldName.
     */
    public java.lang.String getFieldName() {
        return fieldName;
    }

    /**
     * Setter for property fieldName.
     *
     * @param  fieldName  New value of property fieldName.
     */
    public void setFieldName(final String fieldName) {
        this.fieldName = fieldName;
    }

    /**
     * Getter for property foreignKey.
     *
     * @return  Value of property foreignKey.
     */
    public boolean isForeignKey() {
        return foreignKey;
    }

    /**
     * Setter for property foreignKey.
     *
     * @param  foreignKey  New value of property foreignKey.
     */
    public void setForeignKey(final boolean foreignKey) {
        this.foreignKey = foreignKey;
    }

    /**
     * Getter for property foreignKeyClassId.
     *
     * @return  Value of property foreignKeyClassId.
     */
    public int getForeignKeyClassId() {
        return foreignKeyClassId;
    }

    /**
     * Setter for property foreignKeyClassId.
     *
     * @param  foreignKeyClassId  New value of property foreignKeyClassId.
     */
    public void setForeignKeyClassId(final int foreignKeyClassId) {
        this.foreignKeyClassId = foreignKeyClassId;
    }

    /**
     * Getter for property indexed.
     *
     * @return  Value of property indexed.
     */
    public boolean isIndexed() {
        return indexed;
    }

    /**
     * Setter for property indexed.
     *
     * @param  indexed  New value of property indexed.
     */
    public void setIndexed(final boolean indexed) {
        this.indexed = indexed;
    }

    /**
     * Getter for property substitute.
     *
     * @return  Value of property substitute.
     */
    public boolean isSubstitute() {
        return substitute;
    }

    /**
     * Setter for property substitute.
     *
     * @param  substitute  New value of property substitute.
     */
    public void setSubstitute(final boolean substitute) {
        this.substitute = substitute;
    }

    /**
     * Getter for property typeId.
     *
     * @return  Value of property typeId.
     */
    public int getTypeId() {
        return typeId;
    }

    /**
     * Setter for property typeId.
     *
     * @param  typeId  New value of property typeId.
     */
    public void setTypeId(final int typeId) {
        this.typeId = typeId;
    }

    /**
     * Getter for property visible.
     *
     * @return  Value of property visible.
     */
    public boolean isVisible() {
        return visible;
    }

    /**
     * Setter for property visible.
     *
     * @param  visible  New value of property visible.
     */
    public void setVisible(final boolean visible) {
        this.visible = visible;
    }

    @Override
    public Object getKey() {
        return id + "@" + classId; // NOI18N
    }

    @Override
    public Object constructKey(final Mapable m) {
        if (m instanceof MemberAttributeInfo) {
            return m.getKey();
        } else {
            return null;
        }
    }

    /**
     * Getter for property name.
     *
     * @return  Value of property name.
     */
    public String getName() {
        return name;
    }

    /**
     * Setter for property name.
     *
     * @param  name  New value of property name.
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * Getter for property id.
     *
     * @return  Value of property id.
     */
    public int getId() {
        return id;
    }

    /**
     * Setter for property id.
     *
     * @param  id  New value of property id.
     */
    public void setId(final int id) {
        this.id = id;
    }

    /**
     * Getter for property classId.
     *
     * @return  Value of property classId.
     */
    public int getClassId() {
        return classId;
    }

    /**
     * Setter for property classId.
     *
     * @param  classId  New value of property classId.
     */
    public void setClassId(final int classId) {
        this.classId = classId;
    }

    /**
     * Getter for property isArray.
     *
     * @return  Value of property isArray.
     */
    public boolean isArray() {
        return isArray;
    }

    /**
     * Setter for property isArray.
     *
     * @param  isArray  New value of property isArray.
     */
    public void setIsArray(final boolean isArray) {
        this.isArray = isArray;
    }

    /**
     * Getter for property arrayKeyFieldName.
     *
     * @return  Value of property arrayKeyFieldName.
     */
    public java.lang.String getArrayKeyFieldName() {
        return arrayKeyFieldName;
    }

    /**
     * Setter for property arrayKeyFieldName.
     *
     * @param  arrayKeyFieldName  New value of property arrayKeyFieldName.
     */
    public void setArrayKeyFieldName(final String arrayKeyFieldName) {
        this.arrayKeyFieldName = arrayKeyFieldName;
    }

    /**
     * Getter for property editor.
     *
     * @return  Value of property editor.
     */
    public String getEditor() {
        return editor;
    }

    /**
     * Setter for property editor.
     *
     * @param  editor  New value of property editor.
     */
    public void setEditor(final String editor) {
        this.editor = editor;
    }

    /**
     * Getter for property renderer.
     *
     * @return  Value of property renderer.
     */
    public String getToString() {
        return toString;
    }

    /**
     * Setter for property renderer.
     *
     * @param  toString  renderer New value of property renderer.
     */
    public void setToString(final String toString) {
        this.toString = toString;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean isOptional() {
        return optional;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  optional  DOCUMENT ME!
     */
    public void setOptional(final boolean optional) {
        this.optional = optional;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getDefaultValue() {
        return defaultValue;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  defaultValue  DOCUMENT ME!
     */
    public void setDefaultValue(final String defaultValue) {
        this.defaultValue = defaultValue;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getFromString() {
        return fromString;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  fromString  DOCUMENT ME!
     */
    public void setFromString(final String fromString) {
        this.fromString = fromString;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public int getPosition() {
        return position;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  position  DOCUMENT ME!
     */
    public void setPosition(final int position) {
        this.position = position;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getComplexEditor() {
        return complexEditor;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  complexEditor  DOCUMENT ME!
     */
    public void setComplexEditor(final String complexEditor) {
        this.complexEditor = complexEditor;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getRenderer() {
        return renderer;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  renderer  DOCUMENT ME!
     */
    public void setRenderer(final String renderer) {
        this.renderer = renderer;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getJavaclassname() {
        return javaclassname;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  javaclassname  DOCUMENT ME!
     */
    public void setJavaclassname(final String javaclassname) {
        this.javaclassname = javaclassname;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean isExtensionAttribute() {
        return extensionAttribute;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  extensionAttribute  DOCUMENT ME!
     */
    public void setExtensionAttribute(final boolean extensionAttribute) {
        this.extensionAttribute = extensionAttribute;
    }
}
