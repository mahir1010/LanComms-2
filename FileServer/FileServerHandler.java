import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.File;
import java.net.Socket;


public class FileServerHandler extends Thread {


	public FileServerHandler(Socket s) {
		this.s = s;
		try {
			this.dis = new DataInputStream(s.getInputStream());
			this.dos = new DataOutputStream(s.getOutputStream());
			this.name = dis.readUTF();

		} catch (Exception e) {

		}
		this.start();
	}

	public void run() {
		try {
			byte checker = dis.readByte();
			if (checker == 0) {
				long length = dis.readLong();

				if (length > 500000000) {
					System.out.println("error");
					dos.writeUTF("e");  // Handle this writeUTF
					return;
				}
				dos.writeUTF("s");

				String ext = dis.readUTF();
				System.out.println(ext);
				String fileName = name + sdf.format(new Date()) + "." + ext;
				FileOutputStream fos = new FileOutputStream("./Files/" + fileName);

				if (fos == null) {
					dos.writeUTF("e");
					return;
				}
				dos.writeUTF("s");

				long totalBytesRead = 0;
				int bytesRead = 0;
				while (totalBytesRead != length) {
					bytesRead = dis.read(buffer);
					fos.write(buffer, 0, bytesRead);
					totalBytesRead += bytesRead;


				}
				fos.flush();
				fos.close();
				dos.writeUTF(fileName);   /// Handle this writeUTF
			} else {
				String fileName = dis.readUTF();
				System.out.println(fileName);
				File f = new File("../" + fileName);
				FileInputStream fis = new FileInputStream("./Files/" + fileName);
				if (fis == null) {
					dos.writeUTF("e");
					return;
				}
				dos.writeUTF("s");
				int bytesRead = fis.read(buffer);
				while (bytesRead != -1) {
					dos.write(buffer, 0, bytesRead);
					bytesRead = fis.read(buffer);
				}
				fis.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				dos.close();
				dis.close();
				s.close();
			} catch (Exception e) {

			}
		}
	}
	private Socket s;
	private String name;
	private DataOutputStream dos;
	private DataInputStream dis;
	private static SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyhhmmss");
	private byte []buffer = new byte[16384];
}