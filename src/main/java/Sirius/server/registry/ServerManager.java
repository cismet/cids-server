
package Sirius.server.registry;


import Sirius.server.*;
import java.util.HashMap;
import java.util.Vector;

public class ServerManager
{



	//protected ServerProperties props;
	protected ServerListHash servers;
	protected IpListHash ips;



	ServerManager(/*ServerProperties props*/)
	{
		//this.props=props;

		servers = new ServerListHash();

		ips = new IpListHash();


	}

	public boolean registerServer(int serverTyp, String name, String ip) throws Exception {return registerServer(serverTyp,name,"");}

	public boolean registerServer(int serverTyp, String name, String ip, String port) throws Exception
	{

		boolean ipsDone = ips.addServerIP(serverTyp, name, ip, port);

		boolean serversDone = servers.addServer(serverTyp,name,ip,port);

		return serversDone && ipsDone;


	}



  	public boolean unregisterServer( int serverTyp, String name, String ip) throws Exception {return unregisterServer(serverTyp,name,"");}



	public boolean unregisterServer( int serverTyp, String name, String ip, String port) throws Exception
	{

		boolean ipsDone = ips.removeServerIP(serverTyp,name,ip,port);
		boolean serversDone = servers.removeServer(serverTyp,name/*,ip,port*/);

		return ipsDone && serversDone;

	}

//-------------------------------------------------------------------------------------------------------------


	public HashMap<String,String>  getServerIPs(int serverTyp) throws Exception {return ips.getServerIPs(serverTyp);}


//-------------------------------------------------------------------------------------------------------------

	String getServerIP(int serverTyp, String name)throws Exception {return ips.getServerIP(serverTyp,name);}


//-------------------------------------------------------------------------------------------------------------

 	public Server[] getServers(int serverTyp)
 	{
			Vector s = servers.getServerList(serverTyp);

			return (Server[]) s.toArray (new  Server[s.size()]);

	}

//-------------------------------------------------------------------------------------------------------------

	public Server getServer(int serverTyp, String serverName){return servers.getServer(serverTyp,serverName);}

        
        public int getServerCount(int serverTyp){return servers.getServerList(serverTyp).size();}

}