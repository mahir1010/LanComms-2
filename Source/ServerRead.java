package lanComms.server.utils;

import java.util.ArrayList;
import java.io.DataInputStream;



public class ServerRead extends Thread {

	public ServerRead(ArrayList<Message> msgs, DataInputStream dis, String email) {
		msgbuffer = msgs;
		istream = dis;
		this.email = email;
	}

	public void run() {

		try {
			while (true) {

				msg = istream.readUTF();
				
				if (msg.equals("$exit")) {
					synchronized (msgbuffer) {
						msgbuffer.add(new Message(msg, "cmd", null, email, null));
					}
					break;
				} else if (msg.equals("$file")) {
					msg = istream.readUTF();
					String ext = istream.readUTF();
					String size = istream.readUTF();
					String dest = istream.readUTF();
					synchronized(msgbuffer){msgbuffer.add(new Message(msg, ext, size, email, dest));}

				} else if (msg.equals("$getNames")) {
					
					synchronized(msgbuffer){msgbuffer.add(new Message(msg, "cmd", null, email, null));}

				} else {
					String temp[] = null;
					if (String.valueOf(msg.charAt(0)).equals("@")) {
						temp = msg.split("~", 2);
						temp[0] = temp[0].substring(1);
						
						synchronized(msgbuffer){msgbuffer.add(new Message(temp[1], null, null, email, temp[0]));}
					}
				}

				Thread.sleep(10);
			}
		} catch (Exception err) {
			synchronized(msgbuffer){msgbuffer.add(new Message("$exit", "cmd", null, email, null));}
		}

	}
	private ArrayList<Message> msgbuffer;
	private DataInputStream istream;
	private String msg = "";
	final private String email;

}