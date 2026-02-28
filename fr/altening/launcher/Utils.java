package fr.altening.launcher;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

import org.json.JSONArray;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class Utils {
	public static File workdir = new File(getAppData(), ".minecraft");
	public static String versionurl = "https://launchermeta.mojang.com/mc/game/version_manifest.json";
	public static File versionManifest = new File(System.getProperty("java.io.tmpdir"), "version_manifest.json");
	public static File accountJson = new File(workdir, "mc_account.json");
	public static File assetsFolder = new File(workdir, "assets");
	public static File log_configsFolder = new File(assetsFolder, "log_configs");
	public static File libFolder = new File(workdir, "libraries");
	public static File versionsFolder = new File(workdir, "versions");
    public static List<VersionData> versionList = new ArrayList<VersionData>();

    public static void info(String message) {
		System.out.println("[Launcher] " + message);
	}
	
	public static void error(String message, Exception e) {
		System.err.println("[Launcher] " + message);
		System.err.println(e.getLocalizedMessage());
	}
    
	public static void getVersionsFromFolder() {
		if (versionsFolder.exists() && versionsFolder.isDirectory()) {
    		for (File subFolder : versionsFolder.listFiles(File::isDirectory)) {
    			String folderName = subFolder.getName();
    			File jsonFile = new File(subFolder, folderName + ".json");
    			if (jsonFile.exists() && jsonFile.isFile()) {
    				try (FileReader reader = new FileReader(jsonFile)) {
    					JsonObject obj = JsonParser.parseReader(reader).getAsJsonObject();
    					
    					if (obj.has("releaseTime") && obj.has("id") && folderName.equals(obj.get("id").getAsString())) {
    						boolean alreadyExists = versionList.stream()
                                    .anyMatch(v -> v.getId().equals(folderName));
    						if (!alreadyExists) {
    	        				versionList.add(new VersionData(folderName, "release", "no", obj.get("releaseTime").getAsString()));
    	        				info("Found " + folderName);
    	        			}
    					}
    				} catch (IOException e) {
        				e.printStackTrace();
        			}
    			}
    		}
    	}
	}
	
	public static void saveAccount(String username, String refreshToken) throws IOException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (Writer writer = new FileWriter(accountJson)) {
        	gson.toJson(new AccountData(username, refreshToken), writer);
        }
    }
	
	public static AccountData loadAccount() throws IOException {
	    if (!accountJson.exists()) return null;
	    
	    Gson gson = new Gson();
	    
	    try (Reader reader = new FileReader(accountJson)) {
	    	return gson.fromJson(reader, AccountData.class);
	    }
	}
	
	public static String visitSite(String urly) {
		ArrayList<String> lines = new ArrayList<>();
		String stuff = "";
		try {
			URL url = new URL(urly);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.addRequestProperty("User-Agent",
					"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/72.0.3626.121 Safari/537.36");
			BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
			String line;
			while ((line = in.readLine()) != null)
				lines.add(line);
		} catch (Exception line) {
		}
		for (String s : lines)
			stuff = String.valueOf(stuff) + s;
		return stuff;

	}
	
	public static File getAppData() {
		EnumOS os = getOSType();
		if (os == EnumOS.WINDOWS)
			return new File(System.getenv("APPDATA"));
		if (os == EnumOS.MACOS)
			return new File(System.getProperty("user.home") + "/Library/Application Support");
		return new File(System.getProperty("user.home"));
	}
		
	public static void getVersionData(boolean snapshot, boolean alpha, boolean beta) {
		try {	
			JSONObject root = new JSONObject(new String(Files.readAllBytes(versionManifest.toPath()), StandardCharsets.UTF_8));
			JSONArray versions = root.getJSONArray("versions");
			for (int i = 0; i < versions.length(); i++) {
				JSONObject ver = versions.getJSONObject(i);
				String type = ver.getString("type");
				String name = ver.getString("id");
				String url = ver.getString("url");
				String releaseTime = ver.getString("releaseTime");
				
				if (!type.equals("release") && (alpha ? !type.equals("old_alpha") : true) && (beta ? !type.equals("old_beta") : true) && (snapshot ? !type.equals("snapshot") : true)) continue;
					versionList.add(new VersionData(name, type, url, releaseTime));
			}
		} catch (IOException e) {
			error("Unable to download version_manifest from : " + versionurl, e);
			versionList.add(new VersionData("1.8.9", "release", "https://piston-meta.mojang.com/v1/packages/d546f1707a3f2b7d034eece5ea2e311eda875787/1.8.9.json", "2015-12-03T09:24:39+00:00"));
			e.printStackTrace();
		}
	}
	
	public enum EnumOS {
		WINDOWS("windows", "windows"),
		MACOS("macos", "osx"),
		LINUX("linux", "linux"),
		SOLARIS("solaris", "solaris"),
		UNKNOWN("", "");
		
		public String osName;
		public String alt;
		EnumOS(String osName, String alt) {
			this.osName = osName;
			this.alt = alt;
		}
	}

	public static EnumOS getOSType() {
	    String s = System.getProperty("os.name").toLowerCase();

	    if (s.contains("win")) return EnumOS.WINDOWS;
	    if (s.contains("mac")) return EnumOS.MACOS;
	    if (s.contains("solaris") || s.contains("sunos")) return EnumOS.SOLARIS;
	    if (s.contains("linux") || s.contains("unix")) return EnumOS.LINUX;

	    return EnumOS.UNKNOWN;
	}
	
	public static void download(String fileUrl, File dest, BootFrame bootFrame) throws IOException {
		JProgressBar bar = null;
		JLabel label = null;
		if (bootFrame != null) {
			bootFrame.progressBar.setIndeterminate(false);
			bar = bootFrame.progressBar;
			label = bootFrame.label2;
		}
		setProgress(0, " % | (0B/s)", bar, label);
		URL url = new URL(fileUrl);
		long fileSize = url.openConnection().getContentLengthLong();

		try (InputStream in = url.openStream(); FileOutputStream fos = new FileOutputStream(dest)) {

			byte[] buffer = new byte[1024];
			int count;
			long startTime = System.currentTimeMillis();
			long totalBytesRead = 0;
			long lastTime = startTime;
			long lastBytes = 0;
			String lastBytesPerSec = "(0B/s)";
			while ((count = in.read(buffer, 0, 1024)) != -1) {
				fos.write(buffer, 0, count);
				totalBytesRead += count;

				long currentTime = System.currentTimeMillis();
				if (currentTime - lastTime >= 250) {
					long bytesInLastSec = totalBytesRead - lastBytes;
					setProgress((totalBytesRead * 100) / fileSize, " % | " + (lastBytesPerSec = getBytesPerSecString(bytesInLastSec * 4)), bar, label);
					lastTime = currentTime;
					lastBytes = totalBytesRead;
				}
			}
			setProgress(100L, " % | " + lastBytesPerSec, bar, label);
		}
	}
	
	public static void setProgress(long progress, String text, JProgressBar progressBar, JLabel label) {
		String txt = text.contains("Extracting") ? text : (progress + text);
		if (progressBar != null && label != null) {
	    	if (progress <= progressBar.getMaximum()) 
	    		SwingUtilities.invokeLater(() -> { 
	    			progressBar.setValue((int) progress);
	    			label.setText(txt);
	    			progressBar.setString(txt);
	    		});
		}
		info(txt);
	}
	
	public static String getBytesPerSecString(long bytesPerSecond) {
		double speed = bytesPerSecond;
		String unit = "B/s";

		if (speed >= 1024) {
			speed /= 1024;
			unit = "KB/s";
		}
		if (speed >= 1024) {
			speed /= 1024;
			unit = "MB/s";
		}
		if (speed >= 1024) {
			speed /= 1024;
			unit = "GB/s";
		}
		return String.format("(%.2f %s)", speed, unit);
	}
	
	public static String getMinecraftArch() {
	    return normalizeArch(System.getProperty("os.arch"));
	}
	
	public static String normalizeArch(String arch) {
	    arch = arch.toLowerCase();

	    if (arch.equals("amd64") || arch.equals("x86_64"))
	        return "x86_64";

	    if (arch.equals("x86") || arch.equals("i386") || arch.equals("i686"))
	        return "x86";

	    if (arch.contains("aarch64") || arch.contains("arm64"))
	        return "arm64";

	    return arch;
	}
	
	public static void extractNative(File jarFile, File outputDir) throws IOException {
		if (!outputDir.exists()) outputDir.mkdirs();
		
		try(ZipInputStream zip = new ZipInputStream(new FileInputStream(jarFile))) {
			ZipEntry entry;
			while ((entry = zip.getNextEntry()) != null) {
				if (entry.isDirectory() || entry.getName().startsWith("META-INF/")) continue;
				File outFile = new File(outputDir, new File(entry.getName()).getName());
				outFile.getParentFile().mkdirs();
				if (outFile.getName().endsWith(".class")) continue;
				try (FileOutputStream out = new FileOutputStream(outFile)) {
					byte[] buffer = new byte[4096];
					int len;
					while ((len = zip.read(buffer)) > 0) {
						out.write(buffer, 0, len);
					}
				}
			}
		}
	}
	
	public static void launch(VersionData version, BootFrame boot) throws Exception {
		classpathElements = new ArrayList<String>();
		String selectedVersion = version.getId();
		File versionFolder = new File(versionsFolder, selectedVersion);
		versionFolder.mkdirs();
		File versionJson = new File(versionFolder, selectedVersion + ".json");
		if (!versionJson.exists() && !version.getUrl().equals("no")) {
			info("Téléchargement de " + versionJson.getName());
			boot.label.setText("Téléchargement de " + versionJson.getName());
			download(version.getUrl(), versionJson, boot);
		}
		JSONObject json = new JSONObject(new String(Files.readAllBytes(versionJson.toPath()), StandardCharsets.UTF_8));
		File versionJar = new File(versionFolder, selectedVersion + ".jar");
		if (!versionJar.exists()) {
			JSONObject dls = json.getJSONObject("downloads");
			JSONObject client = dls.getJSONObject("client");
			String url = client.getString("url");
			info("Téléchargement de " + versionJar.getName());
			boot.label.setText("Téléchargement de " + versionJar.getName());
			download(url, versionJar, boot);
		}	
		classpathElements.add(versionJar.getAbsolutePath());
		JSONObject assetIndex = json.getJSONObject("assetIndex");
		File indexesDir = new File(assetsFolder, "indexes");
		File objectsDir = new File(assetsFolder, "objects");
		objectsDir.mkdirs();
		indexesDir.mkdirs();
		String assetsIndexId = assetIndex.getString("id");
		File assetsIndex = new File(indexesDir, assetsIndexId + ".json");
		if (!assetsIndex.exists()) {
			info("Téléchargement de " + assetsIndex.getName());
			boot.label.setText("Téléchargement de " + assetsIndex.getName());
			download(assetIndex.getString("url"), assetsIndex, boot);
		}
		File nativeFolder = new File(versionFolder, "natives");
		nativeFolder.mkdirs();
		JSONObject index = new JSONObject(new String(Files.readAllBytes(assetsIndex.toPath()), StandardCharsets.UTF_8)).getJSONObject("objects");
		for (String key : index.keySet()) {
			JSONObject entry = index.getJSONObject(key);
			String hash = entry.getString("hash");
			String subDir = hash.substring(0, 2);
			String url = "https://resources.download.minecraft.net/" + subDir + "/" + hash;
			
			File asset = new File(objectsDir, subDir + "/" + hash);
			if (!asset.exists()) {
				asset.getParentFile().mkdirs();
				info("Téléchargement de " + asset.getName());
				boot.label.setText("Téléchargement de " + asset.getName());
				download(url, asset, boot);
			}
		}
		libFolder.mkdirs();
		JSONArray libs = json.getJSONArray("libraries");
		EnumOS os = getOSType();
		String arch = getMinecraftArch();
		
		for (int i = 0; i < libs.length(); i++) {
		    JSONObject lib = libs.getJSONObject(i);

		    boolean allowed = true;
		    if (lib.has("rules")) {
		        allowed = false;
		        JSONArray rules = lib.getJSONArray("rules");
		        for (int r = 0; r < rules.length(); r++) {
		            JSONObject rule = rules.getJSONObject(r);
		            String action = rule.getString("action");
		            boolean matches = true;
		            if (rule.has("os")) {
		                JSONObject ruleOS = rule.getJSONObject("os");
		                if (ruleOS.has("name")) {
				            String ruleOSName = ruleOS.getString("name");
			                if (!ruleOSName.equals(os.osName) && !ruleOSName.equals(os.alt)) {
			                	matches = false;
			                }
			                
		                }
		                if (ruleOS.has("arch")) {
		                	String ruleArch = normalizeArch(ruleOS.getString("arch"));
		                	if (!ruleArch.equals(arch)) {
		                		matches = false;
		                	}
		                }
		            } 
		            if (matches) 
		                allowed = action.equals("allow");
		            
		        }
		    }
		    if (!allowed) continue;

		    if (lib.has("name") ) {
		    	String name = lib.getString("name");
		    	String[] parts = name.split(":");
		    	
		    	if (parts.length >= 4) {
		    		String classifier = parts[3];
		    		if (classifier.startsWith("natives-" + os.osName)) {
		    			if (classifier.endsWith("-x86") && !arch.equals("x86")) continue;
		    			if (classifier.endsWith("-arm64") && !arch.equals("arm64")) continue;
		    			if (classifier.equals("natives-" + os.osName) && !arch.equals("x86_64")) continue;
		    		}
		    	}
		    }
		    
		    if (!lib.has("downloads")) continue;
		    JSONObject downloads = lib.getJSONObject("downloads");

		    if (downloads.has("artifact")) {
		        JSONObject artifact = downloads.getJSONObject("artifact");
		        File libFile = new File(libFolder, getLib(artifact, lib));
		        if (!libFile.exists()) {
		            libFile.getParentFile().mkdirs();
		            info("Téléchargement de " + libFile.getName());
		            boot.label.setText("Téléchargement de " + libFile.getName());
		            download(artifact.getString("url"), libFile, boot);
		        }
		        if (libFile.exists()) {
		        	if (libFile.getName().contains("native")) {
		        		info("Extraction des natives depuis le fichier : " + libFile.getName());
	            		extractNative(libFile, nativeFolder);
		        	} else {
		        		info("Ajout de " + libFile.getName() + " dans le classpath");
		        		classpathElements.add(libFile.getAbsolutePath());
		        	}
		        }
		    }

		    if (downloads.has("classifiers") && lib.has("natives")) {
		        JSONObject natives = lib.getJSONObject("natives");
		        String nativeKey = null;
		        if (natives.has(os.osName))
		        	nativeKey = natives.getString(os.osName);
		        else if (natives.has(os.alt))
		        	nativeKey = natives.getString(os.alt);

		        if (nativeKey != null) {
		            
		            JSONObject classifiers = downloads.getJSONObject("classifiers");
		            if (nativeKey.contains("${arch}")) {
		            	String modern = nativeKey.replace("${arch}", arch);
		            	String legacy = nativeKey.replace("${arch}", 
		            			arch.equals("x86") ? "32" :
		            			arch.equals("x86_64") ? "64" : arch);
		            	
		            	if (classifiers.has(modern))
		            		nativeKey = modern;
		            	else if (classifiers.has(legacy))
		            		nativeKey = legacy;
		            	else 
		            		nativeKey = null;
		            }
		            
		            if (nativeKey != null && classifiers.has(nativeKey)) {
		                JSONObject nativeArtifact = classifiers.getJSONObject(nativeKey);
		                File nativeFile = new File(libFolder, getNative(nativeArtifact, lib));
		                if (!nativeFile.exists()) {
		                    nativeFile.getParentFile().mkdirs();
		                    info("(Legacy Native) Téléchargement de " + nativeFile.getName());
		                    boot.label.setText("Téléchargement de " + nativeFile.getName());
		                    download(nativeArtifact.getString("url"), nativeFile, boot);
		                }
		                if (nativeFile.exists() && nativeFile.getName().contains("native")) {
	                		info("Extraction des natives depuis le fichier : " + nativeFile.getName());
		            		extractNative(nativeFile, nativeFolder);	
			            }
		            }
		        }
		    }
		}
		File logConfig = new File("");
		if (json.has("logging")) {
			JSONObject logging = json.getJSONObject("logging").getJSONObject("client");
			JSONObject loggingFile = logging.getJSONObject("file");
			logConfig = new File(log_configsFolder, loggingFile.getString("id"));
			if (!logConfig.exists()) {
				log_configsFolder.mkdirs();
				boot.label.setText("Téléchargement de " + logConfig.getName());
	            download(loggingFile.getString("url"), logConfig, boot);
			}
		}
		String classpath = buildClasspath();
		info("Classpath : " + classpath);
		ProcessBuilder game = new ProcessBuilder(
				"java",
				"-Djava.library.path=" + nativeFolder.getAbsolutePath(),
				"-Djava.net.preferIPv4Stack=true",
				"-Dlog4j.configurationFile=" + logConfig.getAbsolutePath(),
				"-Dlog4j2.formatMsgNoLookups=true",
	            "-Dlog4j2.stdout.layoutPattern=%d{HH:mm:ss} [%t/%level]: %msg%n",
	            "-cp", classpath, json.getString("mainClass"),
	            "--version", selectedVersion,
	            "--gameDir", workdir.getAbsolutePath(),
	            "--assetsDir", "assets",
	            "--assetIndex", assetsIndexId,
	            "--accessToken", "0");
		game.directory(workdir);
		game.redirectErrorStream(true);
		
		Main.main.setVisible(false);
		boot.dispose();
		Process process = game.start();
		try (BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
			String line;
			String currentLevel = "";
			String currentThread = "";
			long currentTimestamp = 0;

			while((line = br.readLine()) != null) {
				line = line.trim();
			    if (line.startsWith("<log4j:Event")) {
			        Matcher mLevel = Pattern.compile("level=\"(.*?)\"").matcher(line);
			        if (mLevel.find()) currentLevel = mLevel.group(1);

			        Matcher mThread = Pattern.compile("thread=\"(.*?)\"").matcher(line);
			        if (mThread.find()) currentThread = mThread.group(1);

			        Matcher mTime = Pattern.compile("timestamp=\"(\\d+)\"").matcher(line);
			        if (mTime.find()) currentTimestamp = Long.parseLong(mTime.group(1));
			    }

			    else if (line.contains("<![CDATA[")) {
			        line = line.replaceAll(".*<!\\[CDATA\\[", "").replaceAll("]]>.*", "");

			        String timeStr = new SimpleDateFormat("HH:mm:ss").format(new Date(currentTimestamp));

			        System.out.println("[" + timeStr + "]" + " [" + currentThread + "/" + currentLevel + "]: " + line.trim());
			    }
			}
		}
		Main.main.setVisible(true);
	}
	public static List<String> classpathElements;
	public static String buildClasspath() {
		return String.join(File.pathSeparator, classpathElements);
	}
	
	public static String getLib(JSONObject artifact, JSONObject lib) {
		String path;
        if (!artifact.has("path")) {
        	String[] parts = lib.getString("name").split(":");
            if (parts.length < 3) throw new RuntimeException("Invalid library name: " + lib.getString("name"));
            String groupId = parts[0].replace('.', '/');
            String artifactId = parts[1];
            String ver = parts[2];
            String classifier = parts.length >= 4 ? "-" + parts[3] : "";
            String fileName = artifactId + "-" + ver + classifier + ".jar";
            path = groupId + "/" + artifactId + "/" + ver + "/" + fileName;
        } else {
        	path = artifact.getString("path");
        }
        return path;
	}
	public static String getNative(JSONObject artifact, JSONObject lib) {
		String fileName;
		if (artifact.has("path")) {
			fileName = artifact.getString("path");
		} else {
		    String url = artifact.getString("url");
		    String[] urlParts = url.split("/");
		    int len = urlParts.length;
		    fileName = urlParts[len - 4] + "/" + urlParts[len - 3] + "/" + urlParts[len - 2] + "/" + urlParts[len - 1];
		}
		return fileName;
	}
	
	public static int compareVersion(String v1, String v2) {
	    String[] parts1 = v1.split("\\.");
	    String[] parts2 = v2.split("\\.");

	    int len = Math.max(parts1.length, parts2.length);
	    for (int i = 0; i < len; i++) {
	        int p1 = i < parts1.length ? Integer.parseInt(parts1[i]) : 0;
	        int p2 = i < parts2.length ? Integer.parseInt(parts2[i]) : 0;
	        if (p1 < p2) return -1;
	        if (p1 > p2) return 1;
	    }
	    return 0;
	}
}
