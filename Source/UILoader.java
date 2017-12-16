package lanComms.client.ui.loader;

import javafx.fxml.FXMLLoader;
import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.net.Socket;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Parent;
import lanComms.client.ui.controller.LoginController;
import lanComms.client.ui.controller.ClientController;
import javafx.application.Application;


public class UILoader extends Application{
	public void start(Stage myStage){
		try{
			instance=this;
			loginRoot=FXMLLoader.load(getClass().getClassLoader().getResource("./Resources/loginUI.fxml"));
			if (loginRoot == null) {
				System.out.println("UI Not Loaded");
				return;
			}
			myStage.setScene(new Scene(loginRoot));
			myStage.setTitle("LanComms");
			myStage.show();
			loginStage=myStage;

		}catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void initApp(Socket s,DataInputStream dis,DataOutputStream dos,String emailid,String ip){
		try {
			loginStage.close();
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/Resources/clientUI.fxml"));
			ClientController c=new ClientController(s,dis,dos,emailid,ip);
			loader.setController(c);
			mainRoot = loader.load();
			if (mainRoot == null) {
				System.out.println("FXML FILE NOT LOADED");
				return;
			}
			Scene mainScene = new Scene(mainRoot);
			mainStage = new Stage();
			mainStage.setScene(mainScene);
			mainStage.setTitle(emailid.substring(0,emailid.indexOf('@')));
			mainStage.show();
		}catch (Exception e) {
			e.printStackTrace();		
		}
	}

	public static UILoader getInstance(){
		if(instance==null)
		System.out.println("Instance is null");
		return instance;
	}
	private static UILoader instance;
	private Stage loginStage,mainStage;
	private Parent loginRoot,mainRoot;
}
