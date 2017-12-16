package lanComms.client.ui.controller;

import java.util.regex.Pattern;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.concurrent.Service;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Label;
import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.net.Socket;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.GridPane;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ComboBox;
import javafx.event.ActionEvent;
import javafx.scene.control.TextField;
import lanComms.client.ui.loader.UILoader;
public class LoginController {

	@FXML public void initialize() {
		ObservableList<String> options = FXCollections.observableArrayList("Your pet's name", "Your first school", "Any Secure phrase");
		fsecComboBox.setItems(options);
		fSecComboBox2.setItems(options);
		rComboBox1.setItems(options);
		rComboBox2.setItems(options);
		startRegistrationService.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
			public void handle(WorkerStateEvent WSE) {
				String result = startRegistrationService.getValue();
				if (result.equals("1")) {
					status.setText("Registration Failed : Email already exists");
					clearStatus();
					return;
				} else if (result.equals("0")) {
					try {
						loader.initApp(s, dis, dos, email, dis.readUTF());
					} catch (Exception e) {

					}
				}
			}

		} );
		startLoginService.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
			public void handle(WorkerStateEvent WSE) {
				String result = startLoginService.getValue();
				if (result.equals("1")) {
					status.setText("Login Failed : check Email or Password");
					clearStatus();
					return;
				} else if (result.equals("0")) {
					try {
						loader.initApp(s, dis, dos, email, dis.readUTF());
					} catch (Exception e) {

					}
				}
			}
		} );
		startRetrieveService.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
			public void handle(WorkerStateEvent WSE) {
				String result = startRetrieveService.getValue();
				if (result.equals("1")) {
					status.setText("Retrieval : Failed check Fields");
					clearStatus();
					return;
				} else if (result.equals("0")) {
					try {
						loader.initApp(s, dis, dos, email, dis.readUTF());
					} catch (Exception e) {

					}
				}
			}

		});
	}
	@FXML public void startRegistrationProcess(ActionEvent a) {
		try {
			if (!getIp()) {
				return;
			}
			if (!getRegistrationFields()) {
				return;
			}
			if (isProcessRunning()) {
				return;
			}
			startRegistrationService.reset();
			startRegistrationService.start();


		} catch (Exception e) {

		}
	}
	@FXML public void startForgotProcess(ActionEvent a) {
		try {
			if (!getIp()) {
				return;
			}
			if (!getRetrieveFields()) {
				return;
			}
			if (isProcessRunning()) {
				return;
			}
			startRetrieveService.reset();
			startRetrieveService.start();
		} catch (Exception e) {

		}
	}
	@FXML public void startLoginProcess(ActionEvent a) {
		try {
			if (!getIp()) {
				return;
			}
			if (!getLoginFields()) {
				return;
			}
			if (isProcessRunning()) {
				return;
			}
			startLoginService.reset();
			startLoginService.start();

		} catch (Exception e) {

		}
	}

	private boolean isProcessRunning() {
		return startLoginService.isRunning() | startRegistrationService.isRunning() | startRetrieveService.isRunning();
	}
	private boolean getIp() {
		ip = ipAddressField.getText();
		if (ip.isEmpty()) {
			ip = "localhost";
		}
		if (!isValidIP()) {
			status.setText("IP not valid");
			clearStatus();
			return false;
		}
		return true;
	}
	private boolean getLoginFields() {
		email = loginEmailField.getText();
		passwd = loginpasswdField.getText();
		if (!checkEmail()) {
			return false;
		}
		if (passwd.equals("")) {
			status.setText("Fields can't be Empty");
			clearStatus();
			return false;
		}
		return true;
	}
	private boolean checkEmail() {
		if (!emailRegex.matcher(email).matches() || email.isEmpty()) {
			status.setText("Invalid Email");
			clearStatus();
			return false;
		}
		return true;
	}
	private boolean getRegistrationFields() {
		email = rEmailField.getText();
		if (!checkEmail()) {
			return false;
		}
		userName = rUserNameField.getText();
		passwd = rPasswdField.getText();
		confirmPassword = rconfirmPasswdField.getText();
		if (!arePasswordMatching()) {
			return false;
		}
		secAns1 = rSecAns1.getText();
		secAns2 = rSecAns2.getText();
		if (passwd.equals("") || confirmPassword.equals("") || secAns1.equals("") || secAns2.equals("") || userName.equals("")) {
			status.setText("Fields can't be Empty");
			clearStatus();
			return false;
		}
		return true;
	}
	private boolean arePasswordMatching() {
		if (passwd.equals(confirmPassword)) {
			return true;
		}
		status.setText("Passwords do not match");
		clearStatus();
		return false;
	}
	private boolean getRetrieveFields() {
		email = fEmailField.getText();
		if (!checkEmail()) {
			return false;
		}
		passwd = fpasswdField.getText();
		confirmPassword = fconfirmpasswdField.getText();
		if (!arePasswordMatching()) {
			return false;
		}
		secAns1 = fSecAns1.getText();
		secAns2 = fSecAns2.getText();
		if (passwd.equals("") || confirmPassword.equals("") || secAns1.equals("") || secAns2.equals("")) {
			status.setText("Fields can't be Empty");
			clearStatus();
			return false;
		}
		return true;
	}
	Service<String> startLoginService = new Service<String>() {
		protected Task<String> createTask() {
			return new Task<String>() {
				public String call() {
					String result = "f";
					try {
						if (s == null || counter == 5) {
							if (counter == 5) {
								counter = 0;
							}
							s = new Socket(ip, 2145);
							dis = new DataInputStream(s.getInputStream());
							dos = new DataOutputStream(s.getOutputStream());
						} counter++;
						dos.writeUTF("login");
						dos.writeUTF(email);
						dos.writeUTF(passwd);
						result = dis.readUTF();
					} catch (Exception e) {
						e.printStackTrace();
					}
					return result;
				}
			};
		}
	};

	Service<String> startRegistrationService = new Service<String>() {
		protected Task<String> createTask() {
			return new Task<String>() {
				public String call() {
					String result = "f";
					try {
						if (s == null || counter == 5) {
							if (counter == 5) {
								counter = 0;
							}
							s = new Socket(ip, 2145);
							dis = new DataInputStream(s.getInputStream());
							dos = new DataOutputStream(s.getOutputStream());
						} counter++;
						dos.writeUTF("register");
						dos.writeUTF(email);
						dos.writeUTF(userName);
						dos.writeUTF(passwd);
						dos.writeUTF(secAns1);
						dos.writeUTF(secAns2);
						result = dis.readUTF();
					} catch (Exception e) {
						e.printStackTrace();
					}
					return result;
				}
			};
		}
	};

	Service<String> startRetrieveService = new Service<String>() {
		protected Task<String> createTask() {
			return new Task<String>() {
				public String call() {
					String result = "f";
					try {
						if (s == null || counter == 5) {
							if (counter == 5) {
								counter = 0;
							}
							s = new Socket(ip, 2145);
							dis = new DataInputStream(s.getInputStream());
							dos = new DataOutputStream(s.getOutputStream());
						} counter++;
						dos.writeUTF("forgot");
						dos.writeUTF(email);
						dos.writeUTF(secAns1);
						dos.writeUTF(secAns2);
						dos.writeUTF(passwd);
						result = dis.readUTF();
					} catch (Exception e) {
						e.printStackTrace();
					}
					return result;
				}
			};
		}
	};
	Service <Void> clearStat = new Service<Void>() {
		protected Task<Void> createTask() {
			return new Task<Void> () {
				public Void call() {
					try {
						Thread.sleep(2000);
						Platform.runLater(new Runnable() {
							public void run() {
								status.setText("");
							}
						});

					} catch (Exception e) {
						e.printStackTrace();
					}
					return null;
				}
			};
		}
	};
	private boolean isValidIP() {
		if (ip.equals("localhost")) {
			return true;
		}
		String oct[] = ip.split("\\.", 4);
		try {
			for (String o : oct) {
				if (o == null) {
					return false;
				}
				int temp = Integer.parseInt(o);
				if (temp < 0 || temp > 255) {
					return false;
				}
			}
		} catch (NumberFormatException e) {
			return false;
		}
		return true;
	}
	private void clearLoginFields() {
		loginEmailField.setText("");
		loginpasswdField.setText("");

	}
	private void clearRegisterFields() {
		rEmailField.setText("");
		rSecAns1.setText("");
		rSecAns2.setText("");
		rUserNameField.setText("");
		rPasswdField.setText("");
		rconfirmPasswdField.setText("");
	}
	private void clearForgotFields() {
		fEmailField.setText("");
		fSecAns1.setText("");
		fSecAns2.setText("");
		fpasswdField.setText("");
		fconfirmpasswdField.setText("");

	}
	private void clearStatus() {
		if (!clearStat.isRunning()) {
			clearStat.reset();
			clearStat.start();
		}
	}
	@FXML public void changeToLogin(ActionEvent e) {
		basePane.getChildren().clear();
		basePane.getChildren().add(loginPane);
	}

	@FXML public void changeToForgot(ActionEvent e) {
		basePane.getChildren().clear();
		basePane.getChildren().add(forgotPane);
	}

	@FXML public void changeToRegistration(ActionEvent e) {
		basePane.getChildren().clear();
		basePane.getChildren().add(registerPane);
	}



	@FXML TextField loginEmailField, fEmailField, fSecAns1, fSecAns2, rEmailField, rSecAns1, rSecAns2, rUserNameField, ipAddressField;
	@FXML ComboBox<String> fsecComboBox, fSecComboBox2, rComboBox1, rComboBox2;
	@FXML PasswordField loginpasswdField, fpasswdField, fconfirmpasswdField, rPasswdField, rconfirmPasswdField;
	@FXML GridPane forgotPane, loginPane, registerPane;
	@FXML StackPane basePane;
	private int counter;
	private Socket s;
	private DataInputStream dis;
	private DataOutputStream dos;
	private String email, passwd, confirmPassword, secAns1, secAns2, userName, ip;
	@FXML private Label status;
	private UILoader loader = UILoader.getInstance();
	private static Pattern emailRegex = Pattern.compile("(?:(?:\\r\\n)?[ \\t])*(?:(?:(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*))*@(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*|(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*)*\\<(?:(?:\\r\\n)?[ \\t])*(?:@(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*(?:,@(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*)*:(?:(?:\\r\\n)?[ \\t])*)?(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*))*@(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*\\>(?:(?:\\r\\n)?[ \\t])*)|(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*)*:(?:(?:\\r\\n)?[ \\t])*(?:(?:(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*))*@(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*|(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*)*\\<(?:(?:\\r\\n)?[ \\t])*(?:@(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*(?:,@(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*)*:(?:(?:\\r\\n)?[ \\t])*)?(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*))*@(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*\\>(?:(?:\\r\\n)?[ \\t])*)(?:,\\s*(?:(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*))*@(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*|(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*)*\\<(?:(?:\\r\\n)?[ \\t])*(?:@(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*(?:,@(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*)*:(?:(?:\\r\\n)?[ \\t])*)?(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*))*@(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*\\>(?:(?:\\r\\n)?[ \\t])*))*)?;\\s*)");

}