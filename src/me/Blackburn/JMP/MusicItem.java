package me.Blackburn.JMP;

public class MusicItem {
	
	private String name;
	private String filepath;
	
	
	public MusicItem(String name, String fp)
	{
		this.name = name;
		this.filepath = fp;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getFilepath() {
		return filepath;
	}
	public void setFilepath(String filepath) {
		this.filepath = filepath;
	}

}
