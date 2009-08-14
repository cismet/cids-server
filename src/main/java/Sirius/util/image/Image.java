package Sirius.util.image;


import java.io.*;


public class Image implements java.io.Serializable
{
    private final transient org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(this.getClass());
    /////////////////members//////////////////////////////////
    
    private byte[] imageData;
    private String name;
    private String description;
    
    
    
    ///////////////////constructor///////////////////////////////
    
    public Image(String filepath)
    {
        this(new File(filepath));
    }
    
    
    
    public Image(File inFile)
    {
        try
        {
            
            InputStream stream;
            name = inFile.getName();
            description = new String("nn");
            imageData = new byte[(int) inFile.length()];
            stream = new FileInputStream(inFile);
            
            //read the file into imageData
            int bytesRead = stream.read(imageData,0,(int) inFile.length());
            
            if (bytesRead == -1) // error occured during readingprocess
                throw new Exception("read fehlgeschlagen");
            else if (bytesRead != (int) inFile.length())
                throw new Exception("Bildinformation wahrscheinlich Fehlerhaft");
            
            stream.close();
        }
        catch(java.io.FileNotFoundException fnfe)
        {
            logger.warn(" File not found: ",fnfe);
            System.err.println("INFO :: File not found: "+fnfe.getMessage());
        }
        
        catch(Exception e)
        {  logger.error(e);  }
        
        
    }
    
    
    public Image()
    {
        imageData = new byte[0];
        name = "noname";
        description = "nodescription";
    }
    
    
    
    
    //////////////////methods////////////////////////////////////////////////
    
    final public byte[] getImageData()
    {return imageData;}
    
    
    final public String getName()
    {return name;}
    
    final public String getDescription()
    {return description;}
    
}
