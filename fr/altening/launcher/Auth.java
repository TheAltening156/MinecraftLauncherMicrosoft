package fr.altening.launcher;

public class Auth {
	private final String username;
    private final String uuid;
    private final String accessToken;
    private final String clientId;
    private final String xuid;
    
    public Auth(String username, String uuid, String accessToken, String clientId, String xuid) {
        this.username = username;
        this.uuid = uuid;
        this.accessToken = accessToken;
		this.clientId = clientId;
		this.xuid = xuid;
    }

    public String getUsername() {
        return username;
    }

    public String getUuid() {
        return uuid;
    }

    public String getAccessToken() {
        return accessToken;
    }
    
    public String getClientId() {
		return clientId;
	}
    
    public String getXuid() {
		return xuid;
	}
}
