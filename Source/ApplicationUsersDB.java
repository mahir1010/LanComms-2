package lanComms.server.database;
import lanComms.server.utils.Message;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.io.File;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.ArrayList;


public class ApplicationUsersDB {

	public ApplicationUsersDB() {
		try {
			new File("../Users").mkdir();
			Class.forName("org.sqlite.JDBC");
			Con = DriverManager.getConnection("jdbc:sqlite:../Users/UserDB.db");
			stmnt = Con.createStatement();
			stmnt.executeUpdate("create table if not exists Users (email TEXT primary key,userName TEXT,passwd TEXT,secAns1 TEXT,secAns2 TEXT)");
			stmnt.executeUpdate("Create Table if not exists undeliveredMsg (email TEXT,Msg TEXT,ext TEXT,size TEXT,sourceEmail Text)");
			addUser = Con.prepareStatement("insert into Users (email,userName,passwd,secAns1,secAns2) values(?,?,?,?,?)");
			addMsg = Con.prepareStatement("insert into undeliveredMsg(email,Msg,ext,size,sourceEmail) values(?,?,?,?,?)");
			changePass = Con.prepareStatement("update Users set passwd=? where email=?");
			verify = Con.prepareStatement("select email from Users where email=? and secAns1=? and secAns2=?");
			checkUsr = Con.prepareStatement("select passwd from Users where(email=?)");
			DelMsg = Con.prepareStatement("delete from undeliveredMsg where email=?");
		}

		catch (Exception err) {
			System.err.println(err.getMessage());
		}
	}

	public synchronized int addUser(String email, String userName, String passwd, String secAns1, String secAns2) {
		try {
			addUser.setString(1, email);
			addUser.setString(2, userName);
			addUser.setString(3, passwd);
			addUser.setString(4, secAns1);
			addUser.setString(5, secAns2);
			addUser.executeUpdate();
			
		}

		catch (Exception err) {
			System.out.println(err);
			return 1;

		}
		return 0;
	}

	public int forgotPasswd(String email, String secAns1, String secAns2, String newPass) {
		try {
			verify.setString(1, email);
			verify.setString(2, secAns1);
			verify.setString(3, secAns2);
			if (verify.executeQuery().next()) {
				changePass.setString(2, email);
				changePass.setString(1, newPass);
				changePass.executeUpdate();
				return 0;
			}

		}

		catch (Exception err) {
			System.err.println(err.getMessage());
		}
		return 1;
	}

	public synchronized  void addTextMsg(String source, String email, String msg) {
		try {
			addMsg.setString(1, email);
			addMsg.setString(2, msg);
			addMsg.setString(3, "");
			addMsg.setString(4, "");
			addMsg.setString(5, source);
			addMsg.executeUpdate();
		}

		catch (Exception err) {
			System.err.println(err.getMessage());
		}
	}

	public synchronized  void addFileMsg(String source, String email, String path, String ext, String size) {
		try {
			addMsg.setString(1, email);
			addMsg.setString(2, path);
			addMsg.setString(3, ext);
			addMsg.setString(4, size);
			addMsg.setString(5, source);
			addMsg.executeUpdate();
		}

		catch (Exception err) {
			System.err.println(err.getMessage());
		}
	}

	public synchronized  int verifyUser(String email, String passwd) {
		try {
			checkUsr.setString(1, email);
			ResultSet s = checkUsr.executeQuery();
			if (s.next()) {
				if (passwd.equals(s.getString(1))) {
					return 0;
				}
			}

		} catch (Exception err) {
			System.err.println(err.getMessage());
		}
		return 1;
	}

	public synchronized ArrayList<String[]> getUsers() {
		ArrayList<String[]> userNames = new ArrayList<String[]>();
		try {
			ResultSet users = stmnt.executeQuery("select * from Users");

			while (users.next()) {
				String us[] = new String[2];
				us[0] = users.getString(1);
				us[1] = users.getString(2);
				userNames.add(us);
			}
		}

		catch (Exception err) {
			System.err.println(err.getMessage());
		}
		return userNames;
	}

	public synchronized ArrayList<Message> getMsgs(String email) {
		ArrayList<Message>msgs = new ArrayList<Message>();
		try {
			int count = 0;
			ResultSet msg = stmnt.executeQuery("select * from undeliveredMsg where(email='" + email + "')");
			while (msg.next()) {
				msgs.add(new Message(msg.getString(2), msg.getString(3), msg.getString(4), msg.getString(5),email));
				count++;
			}
			if (count == 0) {
				return null;
			}
			DelMsg.setString(1, email);
			DelMsg.executeUpdate();
		}

		catch (Exception err) {
			System.err.println(err.getMessage());
		}

		return msgs;
	}

	private Connection Con;
	private Statement stmnt;
	private PreparedStatement addUser, addMsg, checkUsr, DelMsg, verify, changePass;
}