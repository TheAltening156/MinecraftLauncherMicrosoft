package fr.altening.launcher;

public class AccountData {
	public String username;
	public String uuid;
	public String refreshToken;
	public String clientId;
    public String xuid;
	
	public AccountData(String username, String refreshToken, String uuid, String clientId, String xuid) {
		this.username = username;
		this.uuid = uuid;
		this.refreshToken = refreshToken;
		this.clientId = clientId;
		this.xuid = xuid;
	}
	
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getRefreshToken() {
		return refreshToken;
	}
	public void setRefreshToken(String refreshToken) {
		this.refreshToken = refreshToken;
	}
	
	public String getUuid() {
		return uuid;
	}
	
	public String getClientId() {
		return clientId;
	}
    
    public String getXuid() {
		return xuid;
	}
	
}
