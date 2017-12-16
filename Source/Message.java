package lanComms.server.utils;

public class Message {
	public Message(String data,String ext,String size,String source,String dest){
		this.data=data;
		this.ext=ext;
		this.size=size;
		this.source=source;
		this.dest=dest;
	}
	public String getDest(){
		return dest;
	}
	public String getData(){
		return data;
	}

	public String getExt(){
		return ext;
	}

	public String getSize(){
		return size;
	}

	public String getSource(){
		return source;
	}

	private String data,ext,size,source,dest;
}