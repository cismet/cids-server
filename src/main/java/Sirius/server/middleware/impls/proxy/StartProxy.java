package Sirius.server.middleware.impls.proxy;


import java.rmi.*;
import java.rmi.server.*;
import java.rmi.registry.*;
import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import java.net.*;
import java.util.*;

import Sirius.server.registry.*;
import Sirius.server.property.*;
import Sirius.server.middleware.interfaces.proxy.*;
import Sirius.server.middleware.impls.proxy.*;
import Sirius.server.observ.*;
import Sirius.server.*;

import org.apache.log4j.*;



public class StartProxy
{
    
    private transient final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(this.getClass());
    
    protected static StartProxy THIS;
    
    private SystemService  systemServer;
    private UserService userServer;
    private MetaService metaServer;
    private ProxyImpl callServer;
    
    
    private String siriusRegistryIP;
    
    //private JFrame frame;
    //private JTextArea textArea;
    private ServerProperties properties;
    private Server serverInfo;
    private ServerStatus status;
    
    
    
    
    public StartProxy(String configFile) throws Throwable
    {
        
        String [] ips =
        {"localhost"};
        String rmiPort = "1099";
        
        
        properties = new ServerProperties(configFile);
        
        String fileName;
        if((fileName = properties.getLog4jPropertyFile()) != null && !fileName.equals(""))
        {
            PropertyConfigurator.configure(fileName);
        }
        
        try
        {
            ips = properties.getRegistryIps();
        }
        catch(MissingResourceException mre)
        {
            System.err.println("<CS> Value for Key "+mre.getMessage()+" in ConfigFile "+configFile+" is missing");
            logger.error("<CS> Value for Key "+mre.getMessage()+" in ConfigFile "+configFile+" is missing",mre);
            
            throw new ServerExitError( mre);
        }
        
        this.siriusRegistryIP = ips[0];
        
        System.out.println("<CS> INFO siriusRegistryIP:: "+siriusRegistryIP);
        logger.info("<CS> INFO siriusRegistryIP:: "+siriusRegistryIP);
        System.out.println("<CS> INFO configFile:: "+configFile);
        logger.info("<CS> INFO configFile:: "+configFile);
        
        
        try
        {
            rmiPort = properties.getRMIRegistryPort();
            serverInfo=new Server(ServerType.CALLSERVER, properties.getServerName(),
                    InetAddress.getLocalHost().getHostAddress(), rmiPort);
            
        }
        catch(MissingResourceException mre)
        {
            System.err.println("Info <CS> Value for Key "+mre.getMessage()+" in ConfigFile "+configFile+" is missing");
            logger.error("Info <CS> Value for Key "+mre.getMessage()+" in ConfigFile "+configFile+" is missing",mre);
            
            System.out.println("Info <CS> Set Default RMIPort to 1099");
            logger.debug("Info <CS> Set Default RMIPort to 1099");
            
            rmiPort ="1099";
            serverInfo=new Server(ServerType.CALLSERVER,properties.getServerName(),
                    InetAddress.getLocalHost().getHostAddress(), rmiPort);
            
        }
        
        if(System.getSecurityManager()==null)
            System.setSecurityManager(new RMISecurityManager());
        
        // abfragen, ob schon eine  RMI Registry exitiert.
        java.rmi.registry.Registry rmiRegistry;
        
        try
        {
            logger.debug("Info <CS> getRMIRegistry on Port "+rmiPort);
            rmiRegistry = LocateRegistry.getRegistry(new Integer(rmiPort).intValue());
            // wenn keine Registry vorhanden, wird an dieser Stelle Exception ausgeloest
            
            
        }
        catch (RemoteException e)
        {
            
            // wenn nicht, neue Registry starten und auf portnummer setzen
            System.err.println(e.getMessage()+" \n"+"Info <CS> no RMIRegistry on Port "+rmiPort+" available");
            logger.error("Info <CS> no RMIRegistry on Port "+rmiPort+" available",e);
            System.out.println("Info <CS> create RMIRegistry on Port "+rmiPort);
            logger.info("Info <CS> create RMIRegistry on Port "+rmiPort);
            
            rmiRegistry = LocateRegistry.createRegistry(new Integer(rmiPort).intValue());
            
        }
        
        callServer = new ProxyImpl(properties);
        
        
        Naming.bind("callServer",callServer);
        
        String[] list = rmiRegistry.list();
        int number = rmiRegistry.list().length;
        logger.debug("<CS> RMIRegistry does exist...");
        logger.debug(" Info <CS> Already registered with RMIRegistry:");
        
        
        if(logger.isDebugEnabled())
        {
            String t="";
            for (int i=0; i< number; i++)
                t+=("\t"+ list[i]);
            
            logger.debug(t);
        }
        
        
        // ------------------------------------------
        
        //initFrame();
        status = new ServerStatus();
        THIS=this;
        
    }
    
    
    public static final StartProxy getServerInstance()
    {return THIS;}
    
    public ServerStatus getStatus()
    {return status;}
    
    
/*
        public void initFrame() throws java.net.UnknownHostException
        {
                textArea = new JTextArea("siriusRegistryIP: "+siriusRegistryIP+"\nobserves SiriusRegistry for new LocalServers...");
                JButton shutdownButton = new JButton("shutdown");
                shutdownButton.addActionListener(new ShutdownListener());
 
                frame = new JFrame("CallServer: "+InetAddress.getLocalHost().getHostAddress()+":"+properties.getServerPort());
                frame.getContentPane().add(new JScrollPane(textArea), BorderLayout.CENTER);
                frame.getContentPane().add(shutdownButton, BorderLayout.SOUTH);
                frame.setSize(300,300);
                frame.setVisible(true);
        }
 
 */
    
    
    public static void main(String[] args) throws Throwable
    {
        
        if(args==null)
            throw new ServerExitError("args == null keine Kommandozeilenparameter \u00FCbergeben (Configfile / port)" );
        else if (args.length <1)
            throw new ServerExitError("zu wenig Argumente" );
        
        try
        {
            new StartProxy(args[0]);
        }
        catch (ServerExitError e)
        {e.printStackTrace();throw e;}
        catch (AlreadyBoundException e)
        {
            Naming.unbind("callServer");
            throw new ServerExitError(e);
        }
        
        catch(Exception e)
        { e.printStackTrace();
          System.out.println(e);
          
          
          throw new ServerExitError(e);
        }
    }
    
    
    
    public void shutdown() throws Throwable
    {
        try
        {
            callServer.unregisterAsObserver(siriusRegistryIP);
            callServer.nameServer.unregisterServer(ServerType.CALLSERVER,serverInfo.getName(),serverInfo.getIP(),serverInfo.getRMIPort());
            
            Naming.unbind("callServer");
            
            throw new ServerExit("Server ist regul\u00E4r beendet worden");
        }
        catch(Exception e)
        {throw new ServerExitError(e);}
    }
    
    
}
