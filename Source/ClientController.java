package lanComms.client.ui.controller;


import javafx.concurrent.WorkerStateEvent;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import javafx.event.Event;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.ChangeListener;
import javafx.event.EventHandler;
import javafx.scene.layout.BorderPane;
import javafx.collections.FXCollections;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.control.ListView;
import javafx.concurrent.Task;
import javafx.concurrent.Service;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.scene.Scene;
import java.util.LinkedHashMap;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.VBox;
import javafx.event.ActionEvent;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.control.TabPane;
import javafx.scene.control.Tab;
import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.net.Socket;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import java.io.File;
import javafx.stage.FileChooser;


public class ClientController {

	public ClientController(Socket s, DataInputStream dis, DataOutputStream dos, String e, String ip) {
		this.s = s;
		this.dis = dis;
		this.dos = dos;
		emailID = e;
		FileUploaderTask.setStaticFields(ip, emailID);
		FileDownloaderTask.setStaticFields(ip, emailID);
	}
	private void updateEmailUserIDMap(String names) {
		if (names.equals("NOUSERFOUND")) {
			return;
		}
		String te[] = names.split("=");
		for (String t : te) {
			String list[] = t.split("~", 2);
			try {
				emailUserMap.put(list[0], list[1]);
			} catch (Exception e) {}
		}

	}
	Service<Void> read = new Service<Void>() {
		protected Task<Void> createTask() {
			return new Task<Void>() {
				public Void call() {
					String msg = "";


					outer:						while (true) {
						try {
							msg = dis.readUTF();
							if (flag == 1) {
								flag = 0;
								if (msg.equals("NOUSERFOUND")) {
									GenerateErrorMsg(msg);
									return null;
								}
								updateEmailUserIDMap(msg);
								Platform.runLater(new Runnable() {
									public void run() {
										ListView<String> list = new ListView<String>();
										ObservableList<String> Userlist = FXCollections.observableArrayList();
										for (String e : emailUserMap.keySet()) {
											int addflag = 1;
											for (ChatWindow c : currentTabs.values()) {
												if (c.getDest().equals(e) || e.equals(emailID)) {
													addflag = 0;
													break;
												}

											}
											if (addflag == 1) {
												Userlist.add((emailUserMap.get(e)) + "-" + e);
											}
										}

										list.setItems(Userlist);
										Tab newUserTab = initNewTab("Add New Chat");
										BorderPane pane = new BorderPane();
										pane.setCenter(list);
										Button select = new Button("Select");
										select.setOnAction(new EventHandler<ActionEvent>() {
											public void handle(ActionEvent ae) {
												String temp = list.getSelectionModel().getSelectedItem();
												if (temp == null) {
													return;
												}
												temp = temp.substring(temp.lastIndexOf("-") + 1);
												System.out.println(temp);
												newUserTab.setText(emailUserMap.get(temp));
												newUserTab.setContent(CreateChatBox(temp));
												currentTabs.put(newUserTab, newWindow);
												bottomPaneVisible(true);
											}
										});
										pane.setBottom(select);
										newUserTab.setContent(pane);
										tabPane.getTabs().add(newUserTab);
										tabPane.getSelectionModel().select(newUserTab);
										bottomPaneVisible(false);
									}
								});

							} else {
								if (msg.equals("$file")) {
									String sourceEmail = dis.readUTF();
									String ext = dis.readUTF();
									long length = Long.valueOf(dis.readUTF());
									String path = dis.readUTF();
									FileDownloaderTask download = new FileDownloaderTask(path, ext, length);
									Thread t = new Thread(download);
									t.setDaemon(true);

									for (ChatWindow c : currentTabs.values()) {
										if (c.getDest().equals(sourceEmail)) {
											Platform.runLater(new Runnable() {
												private Runnable init(ChatWindow c, String path, String ext) {
													this.c = c;
													this.path = path;
													this.ext = ext;
													return this;
												}
												ChatWindow c;
												String path, ext;
												public void run() {
													c.createFileMessage(download.getFile().getPath(), ext).progressProperty().bind(download.progressProperty());
												}
											} .init(c, path, ext));
											t.start();
											continue outer;
										}
									}
									Tab newUserTab = initNewTab(emailUserMap.get(sourceEmail));
									newUserTab.setContent(CreateChatBox(sourceEmail));
									currentTabs.put(newUserTab, newWindow);
									Platform.runLater(new Runnable() {
										public Runnable init(String path, String ext) {
											this.path = path;
											this.ext = ext;
											return this;
										}
										private String ext, path;
										public void run() {

											tabPane.getTabs().add(newUserTab);
											tabPane.getSelectionModel().select(newUserTab);
											newWindow.createFileMessage(download.getFile().getPath(), ext).progressProperty().bind(download.progressProperty());
										}
									} .init(path, ext));

									t.start();
								} else {
									String temp[] = msg.split("~", 2);
									System.out.println("msg=" + temp[1] + "\nsender=" + temp[0]);
									for (ChatWindow c : currentTabs.values()) {
										if (c.getDest().equals(temp[0])) {
											Platform.runLater(new Runnable() {
												private Runnable init(ChatWindow c, String msg, String name) {
													this.c = c;
													this.msg = msg;
													this.name = name;
													return this;
												}
												ChatWindow c;
												String msg, name;
												public void run() {
													c.createIncomingMessage(msg, name);
												}
											} .init(c, temp[1], emailUserMap.get(temp[0])));
											continue outer;
										}
									}
									Tab newUserTab = initNewTab(emailUserMap.get(temp[0]));
									newUserTab.setContent(CreateChatBox(temp[0]));
									currentTabs.put(newUserTab, newWindow);
									Platform.runLater(new Runnable() {
										public Runnable init(String msg, String name) {
											this.msg = msg;
											this.name = name;
											return this;
										}
										private String name, msg;
										public void run() {

											tabPane.getTabs().add(newUserTab);
											tabPane.getSelectionModel().select(newUserTab);
											newWindow.createIncomingMessage(msg, emailUserMap.get(name));
										}
									} .init(temp[1], temp[0]));

								}
							}
						} catch (Exception e) {

						}
					}

				}
			};
		}
	};
	@FXML public void getUserList(ActionEvent a) {
		try {
			flag = 1;
			dos.writeUTF("$getNames");
		} catch (Exception e) {
		}
	}
	private VBox CreateChatBox(String dest) {
		try {
			chatLoader = new FXMLLoader(getClass().getClassLoader().getResource("./Resources/newChat.fxml"));
			newWindow = new ChatWindow(dest);
			chatLoader.setController(newWindow);
			return (VBox)(chatLoader.load());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	private Tab initNewTab(String s) {
		Tab newUserTab = new Tab(s);
		newUserTab.setClosable(true);
		newUserTab.setOnClosed(new EventHandler<Event>() {
			public void handle(Event e) {
				Tab remove = (Tab)(e.getSource());
				currentTabs.get(remove).clearChat();
				currentTabs.remove(remove);
			}
		});
		return newUserTab;
	}
	@FXML public void initialize() {
		try {
			ChatWindow.setProperty(tabPane.heightProperty(), tabPane.widthProperty());
			msgTextField.prefHeightProperty().bind(sendButton.heightProperty());
			broadcastTab.setContent(CreateChatBox("Broadcast"));
			broadcastTab.setText("Broadcast");
			currentTabs.put(broadcastTab, newWindow);
			FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("./Resources/errorBox.fxml"));
			obj = new errorBox();
			loader.setController(obj);
			errStage = new Stage();
			errStage.setScene(new Scene(loader.load()));
			tabPane.getSelectionModel().selectedItemProperty().addListener(
			new ChangeListener<Tab>() {
				public void changed(ObservableValue<? extends Tab> ov, Tab old, Tab newTab) {
					if (newTab.getText().equals("Add New Chat")) {
						bottomPaneVisible(false);
						return;
					}
					bottomPaneVisible(true);

				}
			}
			);
			updateEmailUserIDMap(dis.readUTF());
			read.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	private void bottomPaneVisible(boolean b) {
		if (bottomPane.isVisible() != b) {
			bottomPane.setVisible(b);
			bottomPane.setManaged(b);
		}
	}
	@FXML public void sendFileMsg(ActionEvent ae) {

		Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();
		ChatWindow destChatWindow = currentTabs.get(selectedTab);
		final String dest = destChatWindow.getDest();
		if (dest.equals("Add New Chat")) {
			GenerateErrorMsg("Not A valid Tab");
			return;
		}
		FileChooser chooser = new FileChooser();
		chooser.setTitle("Open Resource File");
		File selectedFile = chooser.showOpenDialog(mainWindow.getScene().getWindow());
		if (selectedFile == null) {
			GenerateErrorMsg("No File Selected");
		}
		if (selectedFile.length() > 500000000) {
			GenerateErrorMsg("Max Size Limit Exceeded");
			return;
		}
		FileUploaderTask upload = new FileUploaderTask(selectedFile);
		upload.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
			public void handle(WorkerStateEvent WSE) {
				String path = upload.getValue();
				if (path == null) return;
				try {
					dos.writeUTF("$file");
					dos.writeUTF(path);
					dos.writeUTF(upload.getExt());
					dos.writeUTF(String.valueOf(upload.getLength()));
					dos.writeUTF(dest);
				} catch (Exception e) {

				}
			}
		});
		destChatWindow.createFileMessage(selectedFile.getPath(), upload.getExt()).progressProperty().bind(upload.progressProperty());
		Thread t = new Thread(upload);
		t.setDaemon(true);
		t.start();

	}
	@FXML public void sendTextMsgShortCut(KeyEvent ke) {
		if (ke.getCode().equals(KeyCode.ENTER)) {
			sendNewTextMessage();
		}

	}
	@FXML public void sendTextMsg(ActionEvent ae) {
		sendNewTextMessage();
	}
	private void sendNewTextMessage() {
		try {
			String msg = msgTextField.getText();
			msgTextField.setText("");
			if (msg.isEmpty()) {
				return;
			}
			Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();
			ChatWindow destChatWindow = currentTabs.get(selectedTab);
			String dest = destChatWindow.getDest();
			destChatWindow.createOutgoingMessage(msg);
			dos.writeUTF("@" + dest + "~" + msg);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void GenerateErrorMsg(String m) {
		obj.setErrorMsg(m);
		errStage.show();
	}
	private errorBox obj;
	private Stage errStage;
	private byte flag = 0;
	private Socket s;
	private DataInputStream dis;
	private DataOutputStream dos;
	@FXML TextField msgTextField;
	@FXML Button sendButton;
	@FXML TabPane tabPane;
	@FXML Tab broadcastTab;
	private FXMLLoader chatLoader;
	private ChatWindow newWindow;
	private LinkedHashMap<String, String> emailUserMap = new LinkedHashMap<String, String>();
	private LinkedHashMap<Tab, ChatWindow> currentTabs = new LinkedHashMap<Tab, ChatWindow>();
	private String emailID;
	@FXML private VBox bottomPane, mainWindow;
}

class errorBox {
	public void setErrorMsg(String err) {
		errorMsg.setText(err);
	}
	@FXML Label errorMsg;

}

class FileUploaderTask extends Task<String> {
	FileUploaderTask(File file) {
		this.file = file;
		ext = file.getName().substring(file.getName().lastIndexOf(".") + 1, file.getName().length());
		length = file.length();
	}
	public static void setStaticFields(String ip, String email) {
		FileUploaderTask.ip = ip;
		FileUploaderTask.email = email;
	}
	public String call() {
		try {
			Socket s = new Socket(ip, 9999);
			DataInputStream dis = new DataInputStream(s.getInputStream());
			DataOutputStream dos = new DataOutputStream(s.getOutputStream());
			dos.writeUTF(email);
			dos.writeByte(0);
			dos.writeLong(length);
			if (dis.readUTF().equals("e")) {
				return null;
			}
			dos.writeUTF(ext);
			if (dis.readUTF().equals("e")) {
				return null;
			}
			FileInputStream fis = new FileInputStream(file);

			int bytesRead = fis.read(buffer);
			int totalBytesRead = 0;
			while (bytesRead != -1) {
				totalBytesRead += bytesRead;
				updateProgress(totalBytesRead, length);
				dos.write(buffer, 0, bytesRead);
				bytesRead = fis.read(buffer);
			}
			fis.close();
			String path = dis.readUTF();
			dos.close();
			dis.close();
			s.close();
			return path;
		} catch (Exception e) {

		}
		return null;
	}

	public String getExt() {
		return ext;
	}
	public long getLength() {
		return length;
	}

	private File file;
	private String ext;
	private long length;
	private static String ip, email;
	private byte[] buffer = new byte[16384];
}

class FileDownloaderTask extends Task<Void> {
	FileDownloaderTask(String name, String ext, long length) {
		this.name = name;
		this.ext = ext;
		this.length = length;
		f = new File("../Downloads/" + name);
	}
	public static void setStaticFields(String ip, String email) {
		FileDownloaderTask.ip = ip;
		FileDownloaderTask.email = email;
	}
	public Void call() {
		try {
			Socket s = new Socket(ip, 9999);
			DataInputStream dis = new DataInputStream(s.getInputStream());
			DataOutputStream dos = new DataOutputStream(s.getOutputStream());
			dos.writeUTF(email);
			dos.writeByte(1);
			dos.writeUTF(name);
			if (dis.readUTF().equals("e")) {
				return null;
			}
			FileOutputStream fos = new FileOutputStream(f);
			int totalBytesRead = 0;
			int bytesRead = dis.read(buffer);
			while (bytesRead != -1) {
				totalBytesRead += bytesRead;
				updateProgress(totalBytesRead, length);
				System.out.println(totalBytesRead);
				fos.write(buffer, 0, bytesRead);
				bytesRead = dis.read(buffer);
			}
			fos.flush();
			fos.close();
			dos.close();
			dis.close();
			s.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	public File getFile() {
		return f;
	}
	private File f;
	private String name, ext;
	private long length;
	private static String ip, email;
	private byte[] buffer = new byte[16384];

}