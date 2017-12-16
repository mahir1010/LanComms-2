import java.net.ServerSocket;
import java.net.Socket;
import java.io.DataInputStream;
import java.io.DataOutputStream;


class Server{
	Server() throws Exception{
		ss=new ServerSocket(9999);
		Thread accept=new Thread(new Runnable(){
			public void run(){
				while(true){
					try{
 						Socket s=ss.accept();
 						new FileServerHandler(s);

					}catch (Exception e) {
						
					}
				}
			}	
		});
		accept.start();
		accept.join();
	}

	private ServerSocket ss;
}


public class FileServer {
	
	public static void main(String[] args){
		try{
			new Server();
		}catch (Exception e) {
			
		}
	}	
}
