package lanComms.server.main;
import lanComms.server.database.ApplicationUsersDB;
import lanComms.server.utils.Message;
import lanComms.server.utils.ConnectedUser;
import lanComms.server.utils.ServerRead;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.net.ServerSocket;
import java.net.InetAddress;


public class Server {
	public static void main(String[] args) {

		try {
			String ip="";
			if (args.length!=1 ){
				System.out.println("Enter Ip address");
				return;
			}else{
				ip=args[0];
			}
			ServerClass obj = new ServerClass(ip);
		}

		catch (Exception err) {
			System.err.println(err.getMessage());
		}

	}
	
}

class ServerClass {
	public ServerClass(String ip) throws Exception {
		this.ip=ip;
		ss = new ServerSocket(2145);
		msgs = new ArrayList<Message>();
		users = new LinkedHashMap<String, ConnectedUser>();
		Database = new ApplicationUsersDB();
		emailUserMap=new StringBuilder();
		updateEmailUserMap();
		HandleClient = new Thread(new Runnable() {
			public void run() {
				while (true) {
					try {
						Socket socket = ss.accept();
						DataInputStream dis = new DataInputStream(socket.getInputStream());
						DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
						String checker = "", email = "", passwd, sec1, sec2;
						int c;
						handleLoop: for (byte i = 0; i <= 5; i++) {
							if (i == 5) {
								throw new Exception();
							}
							checker = dis.readUTF();
							switch (checker) {
							case "login":
								email = dis.readUTF();
								passwd = dis.readUTF();
								if(users.get(email)!=null){
									dos.writeUTF("1");
									continue;
								}
								c = Database.verifyUser(email, scramble(passwd));
								if (c == 1) {
									dos.writeUTF("1");
									continue;
								}
								break handleLoop;
							case "register":
								email = dis.readUTF();
								String username = dis.readUTF();
								passwd = dis.readUTF();
								sec1 = dis.readUTF();
								sec2 = dis.readUTF();
								c = Database.addUser(email, username, scramble(passwd), scramble(sec1), scramble(sec2));
								emailUserMap.append(email + "~" + username + "=");
								if (c == 1) {
									dos.writeUTF("1");
									continue;
								}
								break handleLoop;
							case "forgot":
								email = dis.readUTF();
								if(users.get(email)!=null){
									dos.writeUTF("1");
									continue;
								}
								sec1 = dis.readUTF();
								sec2 = dis.readUTF();
								passwd = dis.readUTF();
								c = Database.forgotPasswd(email, scramble(sec1), scramble(sec2), scramble(passwd));
								if (c == 1) {
									dos.writeUTF("1");
									continue;
								}
								break handleLoop;
							}
						}
						dos.writeUTF("0");
						dos.writeUTF(ip);
						sendUserName(dos);
						users.put(email, new ConnectedUser(email, socket, dis, dos));
						if (!checker.equals("register")) {
							ArrayList<Message> m = Database.getMsgs(email);
							if (m != null) {
								synchronized (msgs) {
									for (Message ms : m) {
										msgs.add(ms);
									}
								}
							}
						}
						ServerRead r = new ServerRead(msgs, dis, email);
						r.start();
					} catch (Exception E) {
					}
				}
			}
		});

		HandleMsgs = new Thread(new Runnable() {
			public void run() {
				while (true) {
					String sourceEmail, data, size, ext, destEmail;
					synchronized (msgs) {
						try {
							for (Message m : msgs) {
								data = m.getData();
								ext = m.getExt();
								size = m.getSize();
								sourceEmail = m.getSource();
								destEmail = m.getDest();
								if (ext == null || ext.equals("")) {
									if (destEmail.equals("Broadcast")) {
										
										ArrayList<String[]> u = Database.getUsers();
										for (String[] t : u) {
											

											if(t[0].equals(sourceEmail)){

												continue;
											}
											if (!users.containsKey(t[0])) {
												Database.addTextMsg(sourceEmail, t[0], data);
											} else {
												users.get(t[0]).dos.writeUTF(sourceEmail + "~" + data);
											}
										}
										continue;
									} else {
										if (!users.containsKey(destEmail)) {
											Database.addTextMsg(sourceEmail, destEmail, data);

										} else {
											System.out.println("destination " + destEmail);
											System.out.println(sourceEmail + "~" + data);
											users.get(destEmail).dos.writeUTF(sourceEmail + "~" + data);

										}
									}
								} else if (ext.equals("cmd")) {
									if (data.equals("$exit")) {
										if (users.get(sourceEmail) != null) {
											users.get(sourceEmail).closeConnection();
											users.remove(sourceEmail);
											System.out.println(sourceEmail + " Exited");
										}
									} else {
										sendUserName(users.get(sourceEmail).dos);
									}
								} else {
									if (destEmail.equals("Broadcast")) {
										ArrayList<String[]> u = Database.getUsers();
										for (String[] t : u) {
											if(t[0].equals(sourceEmail)){
												continue;
											}
											if (!users.containsKey(t[0])) {
												Database.addFileMsg(sourceEmail, t[0], data, ext, size);
											} else {
												dest = users.get(t[0]).dos;
												dest.writeUTF("$file");
												dest.writeUTF(sourceEmail);
												dest.writeUTF(ext);
												dest.writeUTF(size);
												dest.writeUTF(data);

											}
										}
									} else {
										if (!users.containsKey(destEmail)) {
											Database.addFileMsg(sourceEmail, destEmail, data, ext, size);
										} else {
											dest = users.get(destEmail).dos;
											dest.writeUTF("$file");
											dest.writeUTF(sourceEmail);
											dest.writeUTF(ext);
											dest.writeUTF(size);
											dest.writeUTF(data);

										}
									}
								}
							}
							msgs.clear();
						} catch (Exception E) {
						}
					}
				}
			}
		});
		HandleClient.start();
		HandleMsgs.start();
		HandleClient.join();
	}
	private void updateEmailUserMap(){
		emailUserMap.delete(0,emailUserMap.length());
		ArrayList<String[]> nameslist = Database.getUsers();
		if (nameslist.size() != 1){
			for (String[] Names : nameslist) {
				emailUserMap.append(Names[0] + "~" + Names[1] + "=");
			}
		}
	}
	private void sendUserName(DataOutputStream dos) throws Exception{
		if(emailUserMap.length()==0){
			dos.writeUTF("NOUSERFOUND");
		}else{
			dos.writeUTF(emailUserMap.toString());
		}
	}
	private String scramble(String s) {
		byte b[] = s.getBytes();
		for (int i = 0; i < b.length; i++) {
			b[i] = (byte)(b[i] * (5 + i / (b[i] + 1)) % 250);
		}
		return new String(b);
	}

	Thread HandleClient, HandleMsgs;
	private ServerSocket ss;
	private DataOutputStream dest;
	private LinkedHashMap<String, ConnectedUser> users;
	private ArrayList<Message> msgs;
	private ArrayList<String> emails;
	private ApplicationUsersDB Database;
	private String filseServerIP;
	private StringBuilder emailUserMap;
	private static String ip;
}