/*
 * MemberAttributeInfo.java
 *
 * Created on 25. November 2003, 14:56
 */
package Sirius.server.localserver.attribute;

import Sirius.server.middleware.types.MetaClass;
import Sirius.util.*;

/**
 *
 * @author  schlob
 */
public class MemberAttributeInfo implements Mapable, java.io.Serializable {

    //attribut id aus cs_attr
    protected int id;
    //Klasse zu der das Attribut gehoert
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
    private String complexEditor;
    protected String toString;
    protected String defaultValue;
    protected boolean optional = false;
    protected String fromString;
    protected int position;
    private String renderer;
    private String javaclassname = null;

    /** Creates a new instance of MemberAttributeInfo */
    public MemberAttributeInfo(int id, int classId, int typeId, String name, String fieldName, boolean foreignKey, boolean substitute, int foreignKeyClassId, boolean visible, boolean indexed, boolean isArray, String arrayKeyFieldName, String fromString, String toString, int position) {

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

    /** Getter for property fieldName.
     * @return Value of property fieldName.
     *
     */
    public java.lang.String getFieldName() {
        return fieldName;
    }

    /** Setter for property fieldName.
     * @param fieldName New value of property fieldName.
     *
     */
    public void setFieldName(java.lang.String fieldName) {
        this.fieldName = fieldName;
    }

    /** Getter for property foreignKey.
     * @return Value of property foreignKey.
     *
     */
    public boolean isForeignKey() {
        return foreignKey;
    }

    /** Setter for property foreignKey.
     * @param foreignKey New value of property foreignKey.
     *
     */
    public void setForeignKey(boolean foreignKey) {
        this.foreignKey = foreignKey;
    }

    /** Getter for property foreignKeyClassId.
     * @return Value of property foreignKeyClassId.
     *
     */
    public int getForeignKeyClassId() {
        return foreignKeyClassId;
    }

    /** Setter for property foreignKeyClassId.
     * @param foreignKeyClassId New value of property foreignKeyClassId.
     *
     */
    public void setForeignKeyClassId(int foreignKeyClassId) {
        this.foreignKeyClassId = foreignKeyClassId;
    }

    /** Getter for property indexed.
     * @return Value of property indexed.
     *
     */
    public boolean isIndexed() {
        return indexed;
    }

    /** Setter for property indexed.
     * @param indexed New value of property indexed.
     *
     */
    public void setIndexed(boolean indexed) {
        this.indexed = indexed;
    }

    /** Getter for property substitute.
     * @return Value of property substitute.
     *
     */
    public boolean isSubstitute() {
        return substitute;
    }

    /** Setter for property substitute.
     * @param substitute New value of property substitute.
     *
     */
    public void setSubstitute(boolean substitute) {
        this.substitute = substitute;
    }

    /** Getter for property typeId.
     * @return Value of property typeId.
     *
     */
    public int getTypeId() {
        return typeId;
    }

    /** Setter for property typeId.
     * @param typeId New value of property typeId.
     *
     */
    public void setTypeId(int typeId) {
        this.typeId = typeId;
    }

    /** Getter for property visible.
     * @return Value of property visible.
     *
     */
    public boolean isVisible() {
        return visible;
    }

    /** Setter for property visible.
     * @param visible New value of property visible.
     *
     */
    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public Object getKey() {

        return id + "@" + classId;
    }

    public Object constructKey(Mapable m) {
        if (m instanceof MemberAttributeInfo) {
            return m.getKey();
        } else {
            return null;
        }
    }

    /** Getter for property name.
     * @return Value of property name.
     *
     */
    public java.lang.String getName() {
        return name;
    }

    /** Setter for property name.
     * @param name New value of property name.
     *
     */
    public void setName(java.lang.String name) {
        this.name = name;
    }

    /** Getter for property id.
     * @return Value of property id.
     *
     */
    public int getId() {
        return id;
    }

    /** Setter for property id.
     * @param id New value of property id.
     *
     */
    public void setId(int id) {
        this.id = id;
    }

    /** Getter for property classId.
     * @return Value of property classId.
     *
     */
    public int getClassId() {
        return classId;
    }

    /** Setter for property classId.
     * @param classId New value of property classId.
     *
     */
    public void setClassId(int classId) {
        this.classId = classId;
    }

    /**
     * Getter for property isArray.
     * @return Value of property isArray.
     */
    public boolean isArray() {
        return isArray;
    }

    /**
     * Setter for property isArray.
     * @param isArray New value of property isArray.
     */
    public void setIsArray(boolean isArray) {
        this.isArray = isArray;
    }

    /**
     * Getter for property arrayKeyFieldName.
     * @return Value of property arrayKeyFieldName.
     */
    public java.lang.String getArrayKeyFieldName() {
        return arrayKeyFieldName;
    }

    /**
     * Setter for property arrayKeyFieldName.
     * @param arrayKeyFieldName New value of property arrayKeyFieldName.
     */
    public void setArrayKeyFieldName(java.lang.String arrayKeyFieldName) {
        this.arrayKeyFieldName = arrayKeyFieldName;
    }

    /**
     * Getter for property editor.
     * @return Value of property editor.
     */
    public java.lang.String getEditor() {
        return editor;
    }

    /**
     * Setter for property editor.
     * @param editor New value of property editor.
     */
    public void setEditor(java.lang.String editor) {
        this.editor = editor;
    }

    /**
     * Getter for property renderer.
     * @return Value of property renderer.
     */
    public java.lang.String getToString() {
        return toString;
    }

    /**
     * Setter for property renderer.
     * @param renderer New value of property renderer.
     */
    public void setToString(java.lang.String toString) {
        this.toString = toString;
    }

    public boolean isOptional() {
        return optional;
    }

    public void setOptional(boolean optional) {
        this.optional = optional;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;

    }

    public String getFromString() {
        return fromString;
    }

    public void setFromString(String fromString) {
        this.fromString = fromString;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public String getComplexEditor() {
        return complexEditor;
    }

    public void setComplexEditor(String complexEditor) {
        this.complexEditor = complexEditor;
    }

    public String getRenderer() {
        return renderer;
    }

    public void setRenderer(String renderer) {
        this.renderer = renderer;
    }

    public String getJavaclassname() {
        return javaclassname;
    }

    public void setJavaclassname(String javaclassname) {
        this.javaclassname = javaclassname;
    }
}
