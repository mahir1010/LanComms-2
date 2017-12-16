package lanComms.server.utils;
import java.net.Socket;
import java.io.DataInputStream;
import java.io.DataOutputStream;

public class ConnectedUser{

	public ConnectedUser(String name,Socket s,DataInputStream dis,DataOutputStream dos)
	{
		this.name=name;
		UserSocket=s;
		this.dis=dis;
		this.dos=dos;

	}
	public void closeConnection()
	{
		UserSocket=null;
		dis=null;
		dos=null;
	}

public String name;
public Socket UserSocket;
public DataInputStream dis;
public DataOutputStream dos;

}