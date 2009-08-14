package Sirius.server.localserver.attribute;


import Sirius.server.newuser.permission.*;
import Sirius.server.middleware.types.*;
import Sirius.util.*;
import de.cismet.cids.tools.fromstring.DateFromString;
import de.cismet.cids.tools.fromstring.FromStringCreator;
import de.cismet.cids.tools.fromstring.StringCreateable;
import de.cismet.cids.tools.tostring.StringConvertable;
import de.cismet.cids.tools.tostring.ToStringConverter;


public class ObjectAttribute extends Attribute implements Mapable,java.io.Serializable,Renderable,Editable,StringCreateable,StringConvertable
{
    private transient org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(this.getClass());
    

   // objekt zu dem das Attribut gehoert
    protected int objectID;
    
    //klasse des Objektes zu dem das Attribut gehoert????
    protected int classID;
    
    // Metainformation for this attribute (nachtraeglich dazugekommen)
    protected MemberAttributeInfo mai;

    protected Object deletedValue=null;
    
    
    // xxx not initialized yet
    public FromStringCreator objectCreator;
    
    protected String editor;
    
    protected String complexEditor;
    
    protected String toStringString;

    protected Sirius.server.localserver.object.Object parentObject;
    
    ///////////////constructor///////////////////////////////////////
    public ObjectAttribute(String id,MemberAttributeInfo mai,int objectID,java.lang.Object value,Policy policy)
    {
        // Info wird zum Teil doppelt gehalten im mai Objekt und in der Superklasse
        //verursacht durch :  mai nachtraeglich eingefuegt
        super(id,mai.getName(),null,new PermissionHolder(policy),mai.isVisible());
        this.setMai(mai);
        this.objectID=objectID;
        this.classID=mai.getClassId();
        this.isArray=mai.isArray();
        super.typeId=mai.getTypeId();
        super.referencesObject=mai.foreignKey;
        super.optional=mai.isOptional();
        
        if(value instanceof java.lang.String)
            this.value=((String)value).trim();
        else
            this.value=value;
        
        
        
        
        this.editor= mai.getEditor();
        this.complexEditor=mai.getComplexEditor();
        
        
        this.toStringString=mai.getToString();
        
        //toString
        //  initRenderer(mai);
        
        
        //fromstring /////
        initFromString(mai);
        
        
        
        
    }
    
    
    
    public ObjectAttribute(MemberAttributeInfo mai,int objectID,java.lang.Object value,Policy policy)
    {
        //id????
        this(mai.getId()+"",mai,objectID,value,policy);
        
        
        
        
    }
    
    
//    public ObjectAttribute(String id, java.lang.Object value,int objectID, int classID,String name, String description, boolean visible)
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
     * Ein teil des Visitor-Konzeptes. Diese Fkt ruft die visitMA Fkt aus dem
     * interface TypeVisitor auf.
     *
     * @param mov Implementation des TypeVisitor-Interfaces das beschreibt
     * was in diesem Objekt gemacht werden soll wenn diese Funktion aufgerufen
     * wird.
     * @param o ein Objekt f\u00FCr evt Parameter. H\u00E4ngt von Implementation des
     * TypeVisitor-Interfaces ab, ob und wie es benutzt wird.
     *
     * @return das Ergebnis der Verarbeitung bei aufruf dieser Funktion. Es wird
     * der Returnwert der visitMA(...) Fkt aus dem Inteface TypeVisitor
     * geliefert.
     */
    public Object accept(TypeVisitor mov, Object o)
    {
        return mov.visitMA(this, o);
    }
    
    /** Getter for property classID.
     * @return Value of property classID.
     *
     */
    public int getClassID()
    {
        return classID;
    }
    
    /** Setter for property classID.
     * @param classID New value of property classID.
     *
     */
    public void setClassID(int classID)
    {
        this.classID = classID;
    }
    
    /** Setter for property objectID.
     * @param objectID New value of property objectID.
     *
     */
    public void setObjectID(int objectID)
    {
        this.objectID = objectID;
    }
    
    final int getObjectID()
    {
        return objectID;}
    
    
    //mapable
    public java.lang.Object getKey()
    {
        return id+"@"+classID;
        
    }
    
    public Object constructKey(Mapable m)
    {
        return super.constructKey(m);
    }
    
    
    
    public String getToStringString()
    {
        return toStringString;
    }
    
    // ggf zu \u00E4ndern
    public String getRenderer()
    {return toStringString;}
    
    public Object fromString(String objectRepresentation,java.lang.Object mo) throws Exception
    {
        
        return objectCreator.create(objectRepresentation, mo);
        
    }
    
    
    public boolean isStringCreateable()
    {
        
        return (objectCreator != null);
        
    }
    //Hell
    public String getComplexEditor()
    {
        if (this.complexEditor==null)
        {
            complexEditor="Sirius.navigator.ui.attributes.editor.metaobject.DefaultComplexMetaAttributeEditor";
        }
        if (this.editor==null)
        {
            editor="Sirius.navigator.ui.attributes.editor.metaobject.DefaultSimpleComplexMetaAttributeEditor";
        }
        if(this.referencesObject())
        {
            return complexEditor;
        }
        else
        {
            return editor;
        }
        
    }
    
    public String getSimpleEditor()
    {
        return editor;
    }
    
    /**
     * Setter for property complexEditor.
     * @param complexEditor New value of property complexEditor.
     */
    public void setComplexEditor(java.lang.String complexEditor)
    {
        this.complexEditor = complexEditor;
    }
    

    public String toString()
    {
        setLogger();
        
        logger.debug("entered toString for ObjectAttribute value="+value);
        
        if( value!=null )
        {
            
            if (toStringConverter==null )
            {
                
                if(toStringString!=null)
                {
                    // try to load converter
                    java.lang.Class c0 = de.cismet.cids.tools.tostring.ToStringConverter.class;
                    try
                    {
                        if(logger!=null)logger.debug("try to instantiate toStringConverter for "+toStringString +"");
                        
                        java.lang.Class c = java.lang.Class.forName(toStringString.trim());
                        
                        if(c!=null && c0.isAssignableFrom(c))
                        {
                            super.toStringConverter=(ToStringConverter)c.newInstance();
                        }
                        else
                        {
                            if(logger!=null)logger.warn("toStringConverter for "+toStringString + " could not be instantiated as it does not extend ToStingconverter therfore  ToStingconverter is set as a default");
                            super.toStringConverter=new ToStringConverter();
                        }
                        
                        
                        return super.toStringConverter.convert(this);
                    }
                    
                    catch (Throwable ex)
                    {
                        if(logger!=null)logger.warn("toStringConverter for "+toStringString + " could not be instantiated use toString of Value",ex);
                        return value.toString();
                    }
                }
                else // stringconverter 00 null toSTringString == null but value !=null
                {
                    return value.toString();
                }
                
                
            }
            else //stringconverter!=null also braucht man nicht mehr zu laden
            {
//                super.toStringConverter=new ToStringConverter();
//
//                if(logger!=null)logger.debug("toStringConverter was set=> call convert " +super.toStringConverter.getClass());
//
                return super.toStringConverter.convert(this);
//                return value.toString();
            }
            
            
        }
          
        // wenn nix mehr geht standardverhalten .
        if(logger!=null)logger.warn("toStringConverter for "+toStringString + " could not be instantiated and therefore toString of the superclass will be returned");
        //return "Wert nicht gesetzt";
        return "";
    }
    
    // muss total neu gemacht werden
    protected void initFromString(MemberAttributeInfo mai)
    {
        String fromString = mai.getFromString();
        if(fromString!=null)
        {
            
            try
            {
                logger.debug("<LS> info :: try to load fromString if not null : " +fromString);
                
                
                java.lang.Class c0 = java.lang.Class.forName( "Sirius.util.FromStringCreator");
                java.lang.Class c = java.lang.Class.forName( fromString.trim());
                
                if( c0.isAssignableFrom(c))
                {
                    this.objectCreator=(FromStringCreator)c.newInstance();
                    logger.debug(this.objectCreator+"vom typ"+fromString +" erfolgreich zugewiesen");
                }
                else
                    logger.warn("<LS> info ::  fromSTringObjectCreator "+ fromString+"nicht geladen: reference is :" +this.objectCreator);
                
                
                
            }
            catch (Exception e)
            {
                
                logger.error("<LS> ERROR :: "+fromString +" f\u00FCr Klasse "+name+" konnte nicht geladen werden set string converter to Default ",e);
                
            }
            
            
        }
        else// fromString==null nicht gesetz aber value evtl vorhanden
        {
            
            // default from string
            if(value instanceof java.sql.Date || value instanceof java.util.Date  || (typeId >78 && typeId <87) )
            {
                this.objectCreator=new DateFromString();
                
            }
            
            
        }
        
    }
    
    public void setLogger()
    {
        if(logger==null)
            logger = org.apache.log4j.Logger.getLogger(this.getClass());
    }

    public MemberAttributeInfo getMai() {
        return mai;
    }

    public void setMai(MemberAttributeInfo mai) {
        this.mai = mai;
    }

    public Sirius.server.localserver.object.Object getParentObject() {
        return parentObject;
    }

    public void setParentObject(Sirius.server.localserver.object.Object parentObject) {
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


   


    
    
}//end of class
