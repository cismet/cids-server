/*
 * FTPProto.java
 *
 * Created on 8. September 2003, 13:29
 */

package Sirius.server.dataretrieval;

import com.enterprisedt.net.ftp.*;
import java.io.*;
import java.net.*;
import Sirius.util.Parametrizer;
import Sirius.server.middleware.types.*;
import Sirius.server.localserver.attribute.*;
import Sirius.server.middleware.types.MOTraverse.*;
import org.apache.log4j.*;

/**
 * Liest MetaDataObject, erstellt eine Verbindung zum FTP-Server und liefert
 * eine Datei als Byte-Array in einem DataObject zur\u00FCck.
 *
 * Bezeichnung dieses Protokols ist "ftp".
 *
 * @author  awindholz
 */
public class FTPProto implements MetaObjectProto {
    
    private static Logger logger = Logger.getLogger(FTPProto.class);
    
    /** Creates a new instance of JDBCProto
     */
    public FTPProto() {}
    
    /**
     * Holt eine Datei vom FTP-Server, serialisiert diese und liefert zur\u00FCck.
     */
    public DataObject getDataObject(MetaObject metaDataObject) throws DataRetrievalException {
        
        byte[] data;
        
        ProtoDelegator protoDelegator = new ProtoDelegator(logger);
        
        MetaObject param_mo = protoDelegator.getParameterMO(metaDataObject);
        
        String s_url = (String)protoDelegator.getURL(param_mo);
        
        s_url = Parametrizer.parametrize(s_url, metaDataObject);
        
        String login = (String)protoDelegator.getSingleValue(param_mo, "login");
        String password = (String)protoDelegator.getSingleValue(param_mo, "password");
        
        String fileName = metaDataObject.getName();
        
        if(fileName == null) {
            
            String message = "Name of dataobjekt was not found." +
            "The name must agree with the file name.";
            
            throw new DataRetrievalException(message, logger);
        }
        
        try {
            
            URL url = new URL(s_url.trim() + fileName.trim());
            
            String remoteHost = url.getHost();
            String path = url.getPath();
            int controllPort = url.getPort();
            if(controllPort == -1)
                controllPort = url.getDefaultPort();
            
            // userData[0]: kennung, userData[1] passwd
/*            String[] userData = getUserData(url);
            
            if(userData == null) {
                String message = "Konnte keine verbindung herstellen da keine" +
                " Benutzerdaten in URL enthalten. Benutzerdaten in URL im folgendem" +
                " Format angeben: ftp://<username>:<password>@<hosthname>[<port>]<path>";
                throw new DataRetrievalException(message, logger);
            }*/
            
            data =
            getDataObject(remoteHost, controllPort, login, password, path);
        } catch(IOException e) {
            throw new DataRetrievalException(e, logger);
        } catch(FTPException e) {
            throw new DataRetrievalException(e.getMessage(), logger);
        }
        
        return new DataObject(data, "ftp", fileName.trim());
    }
    
    /** Creates a new instance of FTPDataRetr */
    private byte[] getDataObject(
    String remoteHost,
    int controlPort,
    String kennung,
    String passwd,
    String remotePath) throws IOException, FTPException {
        byte[] bytes;
        
        FTPClient ftpClient = new FTPClient(remoteHost, controlPort);
        
        ftpClient.setLogStream(null);
        
        ftpClient.login(kennung, passwd);
        
        bytes = ftpClient.get(remotePath);
        
        // Seltsamerweise gibt die Funktion immer den Begr\u00FCsungstext beim aufruf aus.
        ftpClient.quit();
        
        return bytes;
    }
    
    public String getDataSourceClass() {
        return "ftp";
    }
} // end class
// ------------------------------ SENSE ------------------------------------ //

/**
 * Wenn es sich bei \u00FCbergebenem Parameter um instantion dieser Klasse handelt
 * wird true geliefert.
 *
 * @param o Objekt das verglichen werden soll.
 *
 * @return true wenn o eine instantion dieser Klasse ist.
 */
/*    public boolean equals(Object o) {
        return (o instanceof FTPProto);
    }*/

/**
 * Bestimt ob die heruntergeladene Dateien nach dem schliessen gel\u00F6scht
 * werden sollen. Wenn dieser flag auf true gesetzt ist, wird nach der
 * erzeugung der Datei(die in einem tempor\u00E4ren Verzeichnis abgelegt wird)
 * die Fkt deleteOnExit() auf die Datei angewendet.
 */
/*    public static boolean deleteOnExit = true;
 
    static {
        LOCAL_TMP_PATH = System.getProperty("java.io.tmpdir");
    }*/

/*    static {
        MetaObjectProtoMgr.register(new FTPProto());
    }*/

/**
 * Diese Klasse Testet ob es sich bei dem \u00FCbergebenem String um eine URL
 * f\u00FCr Protokol FTP handelt. Akzeptiert Strings die mit "ftp://" beginnen.
 * Caseunsensitiv.
 *
 * @param url der URL-String dass auf Protokol \u00FCberpr\u00FCft werden soll.
 */
/*    public boolean acceptsURL(String url) {
 
        int minLength = 6;
        String correctProtoString = "ftp://";
 
        if(url.length() < minLength) return false;
 
        String protoString = url.substring(0, minLength);
 
        return protoString.equalsIgnoreCase(correctProtoString);
    }*/


/*
    private File getFile(FTPClient ftpClient, String remotePath, String fileName)
    throws IOException, FTPException {
        String path = remotePath + "/" + fileName;
 
        //        InputStream out = new ByteArrayInputStream(ftpClient.get(path));
 
    String localPath =
            LOCAL_TMP_PATH + System.getProperty("file.separator") + fileName;
        ftpClient.get(localPath, path);
 
 
        File localFile = new File(localPath);
        if(deleteOnExit)
            localFile.deleteOnExit();
 
// Seltsamerweise gibt die Funktion immer den Begr\u00FCsungstext beim aufruf aus.
        ftpClient.quit();
 
        return localFile;
    }*/

/**
 * Versucht alle Tempor\u00E4ren Dateien zu l\u00F6schen. Damit das funktioniert
 * m\u00FCssen alle Objekte die auf auf die Dateien zugreifen die vom FTP-Server
 * heruntergeladen wurden geschlossen werden(z.B. FileReader o.\u00E4.).
 * Wenn eine Datei zur Zeit der ausf\u00FChrung nicht geschlossen ist, wird diese
 * nicht sofort gel\u00F6scht sondern erst nach dem diese geschlossen wurde. Wird
 * die Datei ohne zu schliesen verlassen, so bleibt die Datei im Tempor\u00E4ren
 * Verzeichnis erhalten.
 */
/*    public void delTempFiles() {
        File tmp;
        while(localTmpFiles.size() != 0) {
            tmp = (File)localTmpFiles.remove(0);
            if(tmp.exists()) {
                tmp.deleteOnExit();
            }
        }
    }*/
