package lanComms.client.ui.controller;

import java.awt.Desktop;
import java.io.File;

import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.fxml.FXML;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.ImageView;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import javafx.application.HostServices;
import javafx.scene.image.Image;


public class ChatWindow {


	public ChatWindow(String dest) {
		this.dest = dest;
	}

	public String getDest() {
		return dest;
	}

	@FXML public void initialize() {
		if (height != null && width != null) {
			messageContainer.prefHeightProperty().bind(height);
			messageContainer.prefWidthProperty().bind(width);
		}
	}

	public void createOutgoingMessage(String msg) {
		Label outgoingMsg = new Label(msg);
		Label source = new Label("You");
		source.getStyleClass().add("ouserName");
		outgoingMsg.getStyleClass().add("OutGoingMsg");
		messageContainer.getChildren().addAll(source, outgoingMsg);
	}

	public void createIncomingMessage(String msg, String name) {
		Label incomingMsg = new Label(msg);
		Label source = new Label(name);
		source.getStyleClass().add("iuserName");
		incomingMsg.getStyleClass().add("incomingMsg");
		messageContainer.getChildren().addAll(source, incomingMsg);
	}
	public ProgressBar createFileMessage(String path, String type) {
		VBox fileMessageContainer = new VBox();
		ProgressBar pb = new ProgressBar(0);
		String imagePath = "../Resources/unknown.png";
		if (type.equals("jpg") || type.equals("png") || type.equals("gif")) {
			imagePath = "../Resources/image.png";
		} else if (type.equals("zip") || type.equals("rar") || type.equals("7z")) {
			imagePath = "../Resources/archive.png";
		} else if (type.equals("mp4") || type.equals("webm") || type.equals("mkv") || type.equals("avi")) {
			imagePath = "../Resources/video.png";
		} else if (type.equals("pdf")) {
			imagePath = "../Resources/pdf.png";
		} else if (type.equals("docx") || type.equals("txt") || type.equals("odt")) {
			imagePath = "../Resources/document.png";
		}
		ImageView view = new ImageView(new Image("file:" + imagePath));
		view.setFitWidth(128);
		view.setFitHeight(128);
		view.setOnMouseClicked(new EventHandler<MouseEvent>() {
			public void handle(MouseEvent me) {
				try {
					Thread t = new Thread(new Runnable() {
						public void run() {
							try {
								Desktop.getDesktop().open(new File(path));
							} catch (Exception e) {

							}
						}
					});
					t.start();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		pb.prefWidthProperty().bind(view.fitWidthProperty());
		fileMessageContainer.getChildren().addAll(view, pb);
		messageContainer.getChildren().addAll(fileMessageContainer);
		return pb;
	}
	public static void setProperty(ReadOnlyDoubleProperty h, ReadOnlyDoubleProperty w) {
		height = h;
		width = w;
	}
	public void clearChat() {
		messageContainer.getChildren().clear();
	}
	private String dest;
	@FXML private VBox messageContainer;
	private static ReadOnlyDoubleProperty height, width;


}