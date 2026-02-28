package fr.altening.launcher;

public class VersionData {
    private String id;
    private String type;
    private String url;
    private String releaseTime;
    
    public VersionData(String id, String type, String url, String releaseTime) {
		this.id = id;
		this.type = type;
		this.url = url;
		this.releaseTime = releaseTime;
	}
    
	public String getId() { return id; }
    public String getType() { return type; }
    public String getUrl() { return url; }
    public String getReleaseTime() { return releaseTime; }
}