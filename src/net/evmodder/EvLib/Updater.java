package net.evmodder.EvLib;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.bukkit.plugin.Plugin;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

/**
 * Check for updates on BukkitDev for a given plugin, and download the updates if needed.
 * It is a <b>BUKKIT POLICY</b> that you include a boolean value in your config that prevents the auto-updater from running <b>AT ALL</b>.
 * If you fail to include this option in your config, your plugin will be <b>REJECTED</b> when you attempt to submit it to dev.bukkit.org.
 * If you are unsure about these rules, please read the plugin submission guidelines: http://goo.gl/8iU5l
 *
 * @authors Gravity, EvModder
 * @version 2.4
 */
public class Updater {
	/* Constants */
	private static final String TITLE_VALUE = "name";// Remote file's title
	private static final String LINK_VALUE = "downloadUrl";// Remote file's download link
	private static final String TYPE_VALUE = "releaseType";// Remote file's release type
	private static final String VERSION_VALUE = "gameVersion";// Remote file's build version
	private static final String QUERY = "/servermods/files?projectIds=";// Path to GET
	private static final String HOST = "https://api.curseforge.com";// Slugs will be appended to this to get to the project's RSS feed
	private static final String USER_AGENT = "Updater (by Gravity)";// User-agent when querying Curse
	private static final String DELIMETER = "^v|[\\s_-]v";// Used for locating version numbers in file names
	private static final String[] NO_UPDATE_TAG = { "-DEV", "-PRE", "-SNAPSHOT" };// If the version number contains one of these, don't update.
	private static final int BYTE_SIZE = 1024;// Used for downloading files

//	private static final String API_KEY_CONFIG_KEY = "api-key";// Config key for api key
//	private static final String DISABLE_CONFIG_KEY = "disable";// Config key for disabling Updater
//	private static final String API_KEY_DEFAULT = "PUT_API_KEY_HERE";// Default api key value in config
//	private static final boolean DISABLE_DEFAULT = false;// Default disable value in config

	/* User-provided variables */
	private final Plugin plugin;// Plugin running Updater
	private final int id;// Project's Curse ID
	private final File file;// The plugin file (jar)
	private final UpdateType updateType;// Type of update check to run
	private final String apiKey;// BukkitDev ServerMods API key
//	private final UpdateCallback callback;// The provided callback (if any)
	private final boolean announce;// Whether to announce file downloads

	/* Collected from Curse API */
	private String updateTitle;
	private String updateLink;
	private ReleaseType updateReleaseType;
	private String updateGameVersion;

	/* Update process variables */
	private final File updateFolder;// The folder that downloads will be placed in
	private URL url;// Connection to RSS
	private Thread thread;// Updater thread
	private Updater.UpdateResult result = Updater.UpdateResult.SUCCESS;// Used for determining the outcome of the update process

	/**
	 * Gives the developer the result of the update process. Can be obtained by called {@link #getResult()}
	 */
	public enum UpdateResult{
		SUCCESS,/** The updater found an update, and has readied it to be loaded the next time the server restarts/reloads. */
		NO_UPDATE,/** The updater did not find an update, and nothing was downloaded. */
//		DISABLED,/** The server administrator has disabled the updating system. */
		FAIL_GAME_VERSION,/** The update found an update, but it is for a different game version than what the server is currently running. */
		FAIL_DOWNLOAD,/** The updater found an update, but was unable to download it. */
		FAIL_DBO,/** For some reason, the updater was unable to contact dev.bukkit.org to download the file. */
		FAIL_NOVERSION,/** When running the version check, the file on DBO did not contain a recognizable version. */
		FAIL_BADID,/** The id provided by the plugin running the updater was invalid and doesn't exist on DBO. */
		FAIL_APIKEY,/** The server administrator has improperly configured their API key in the configuration. */
		UPDATE_AVAILABLE/** The updater found an update, but because of the UpdateType being set to NO_DOWNLOAD, it wasn't downloaded. */
	}
	/**
	 * Allows the developer to specify the type of update that will be run.
	 */
	public enum UpdateType{
		/** Run a version check, and then if the file is out of date, download the newest version. */
		DEFAULT,
		/** Same as DEFAULT, but allows installing beta builds */
		DEFAULT_ALLOW_BETA,
		/** Same as DEFAULT, but allows installing alpha and beta builds */
		DEFAULT_ALLOW_ALPHA_AND_BETA,
		/** Don't run a version check, just find the latest update and download it. */
		NO_VERSION_CHECK,
		/** Get information about the version and the download size, but don't actually download anything. */
		NO_DOWNLOAD,
//		/** Do nothing, updater disabled*/
//		DISABLED
	}
	/**
	 * Represents the various release types of a file on BukkitDev.
	 */
	public enum ReleaseType{
		ALPHA,/** An "alpha" file. */
		BETA,/** A "beta" file. */
		RELEASE,/** A "release" file. */
		UNKNOWN/** Missing or unrecognized release type. */
	}
	/**
	 * Called on main thread when the Updater has finished working, regardless
	 * of result.
	 */
	public interface UpdateCallback{
		/**
		 * Called when the updater has finished working.
		 * @param updater The updater instance
		 */
		void onFinish(Updater updater);
	}
	/**
	 * Initialize the updater with the provided callback.
	 *
	 * @param plugin The plugin that is checking for an update.
	 * @param id The dev.bukkit.org id of the project.
	 * @param file The file that the plugin is running from, get this by doing this.getFile() from within your main class.
	 * @param updateType Specify the type of update this will be. See {@link UpdateType}
	 * @param apiKey Optional Bukkit API key to be used in web requests.
	 * @param callback The callback instance to notify when the Updater has finished
	 * @param announce True if the program should announce the progress of new updates in console.
	 */
	@SuppressWarnings("deprecation")
	public Updater(Plugin plugin, int id, File file, UpdateType updateType, String apiKey, UpdateCallback callback, boolean announce){
		this.plugin = plugin;
		this.id = id;
		this.file = file;
		this.updateType = updateType != null ? updateType : UpdateType.DEFAULT;
		// For apiKey: http://wiki.bukkit.org/ServerMods_API (or https://bukkit.fandom.com/wiki/ServerMods_API)
		this.apiKey = apiKey;
//		this.callback = callback;
		this.announce = announce;
		this.updateFolder = plugin.getServer().getUpdateFolderFile();

		try{
			url = new URL(Updater.HOST + Updater.QUERY + id);
			thread = new Thread(plugin.getName()+"-updater"){@Override public void run(){
				if(read() && pluginVersionCheck() && gameVersionCheck()){
					// Obtain the results of the project's file feed
					if(updateLink != null && canDownloadUpdate()){
						String name = file.getName();
						// If it's a zip file, it shouldn't be downloaded as the plugin's name
						if(updateLink.endsWith(".zip")) name = updateLink.substring(updateLink.lastIndexOf("/") + 1);
						saveFile(name);
					}
					else result = UpdateResult.UPDATE_AVAILABLE;
				}
				if(callback != null) plugin.getServer().getScheduler().runTask(plugin, ()->callback.onFinish(Updater.this));
			}};
			thread.start();
		}
		catch(MalformedURLException e){
			plugin.getLogger().log(Level.SEVERE, "The project ID provided for updating, " + id + " is invalid.", e);
			result = UpdateResult.FAIL_BADID;
			if(callback != null) callback.onFinish(Updater.this);
		}
	}
	/**
	 * As the result of Updater output depends on the thread's completion, it is necessary to wait for the thread to finish
	 * before allowing anyone to check the result.
	 */
	private void waitForThread(){
		if((thread != null) && thread.isAlive()){
			try{thread.join();}
			catch(final InterruptedException e){plugin.getLogger().log(Level.SEVERE, null, e);}
		}
	}
	/**
	 * Get the result of the update process.
	 *
	 * @return result of the update process.
	 * @see UpdateResult
	 */
	public Updater.UpdateResult getResult(){
		waitForThread();
		return result;
	}
//	/**
//	 * Get the latest version's release type.
//	 *
//	 * @return latest version's release type.
//	 * @see ReleaseType
//	 */
//	public ReleaseType getLatestType(){
//		waitForThread();
//		return updateReleaseType;
//	}
//	/**
//	 * Get the latest version's game version (such as "CB 1.2.5-R1.0").
//	 *
//	 * @return latest version's game version.
//	 */
//	public String getLatestGameVersion(){
//		waitForThread();
//		return updateGameVersion;
//	}
//	/**
//	 * Get the latest version's name (such as "Project v1.0").
//	 *
//	 * @return latest version's name.
//	 */
//	public String getLatestName(){
//		waitForThread();
//		return updateTitle;
//	}
//	/**
//	 * Get the latest version's direct file link.
//	 *
//	 * @return latest version's file link.
//	 */
//	public String getLatestFileLink(){
//		waitForThread();
//		return updateLink;
//	}
	/**
	 * Download a file and save it to the specified folder.
	 */
	private void downloadFile(){
		BufferedInputStream in = null;
		FileOutputStream fout = null;
		try{
			final URL fileUrl = URI.create(updateLink).toURL();
			final int fileLength = fileUrl.openConnection().getContentLength();
			in = new BufferedInputStream(fileUrl.openStream());
			fout = new FileOutputStream(new File(updateFolder, file.getName()));
			final byte[] data = new byte[Updater.BYTE_SIZE];
			int count;
			if(announce) plugin.getLogger().info("About to download a new update: " + updateTitle);
			long downloaded = 0;
			while((count = in.read(data, 0, Updater.BYTE_SIZE)) != -1){
				downloaded += count;
				fout.write(data, 0, count);
				final int percent = (int) ((downloaded * 100) / fileLength);
				if(announce && ((percent % 10) == 0)) plugin.getLogger().info("Downloading update: " + percent + "% of " + fileLength + " bytes.");
			}
		}
		catch(Exception ex){
			plugin.getLogger().log(Level.WARNING, "The auto-updater tried to download a new update, but was unsuccessful.", ex);
			result = Updater.UpdateResult.FAIL_DOWNLOAD;
		}
		finally{
			try{if(in != null) in.close();}
			catch(final IOException ex){plugin.getLogger().log(Level.SEVERE, null, ex);}
			try{if(fout != null) fout.close();}
			catch(final IOException ex){plugin.getLogger().log(Level.SEVERE, null, ex);}
		}
	}
	/**
	 * Perform a file operation and log any errors if it fails.
	 * @param file file operation is performed on.
	 * @param result result of file operation.
	 * @param create true if a file is being created, false if deleted.
	 */
	private void fileIOOrError(File file, boolean result, boolean create){
		if(!result) plugin.getLogger().severe("The updater could not " + (create ? "create" : "delete") + " file at: " + file.getAbsolutePath());
	}
	private File[] listFilesOrError(File folder){
		File[] contents = folder.listFiles();
		if(contents == null){
			plugin.getLogger().severe("The updater could not access files at: " + updateFolder.getAbsolutePath());
			return new File[0];
		}
		else return contents;
	}
	/**
	 * Remove possibly leftover files from the update folder.
	 */
	private void deleteOldFiles(){
		//Just a quick check to make sure we didn't leave any files from last time...
		final File[] list = listFilesOrError(updateFolder);
		for(final File xFile : list) if(xFile.getName().endsWith(".zip")) fileIOOrError(xFile, xFile.mkdir(), true);
	}
	/**
	 * Check if the name of a jar is one of the plugins currently installed, used for extracting the correct files out of a zip.
	 *
	 * @param name a name to check for inside the plugins folder.
	 * @return true if a file inside the plugins folder is named this.
	 */
	private boolean pluginExists(String name){
		File[] plugins = listFilesOrError(new File("plugins"));
		for(final File file : plugins) if(file.getName().equals(name)) return true;
		return false;
	}
	/**
	 * Find any new files extracted from an update into the plugin's data directory.
	 * @param zipPath path of extracted files.
	 */
	private void moveNewZipFiles(String zipPath){
		File[] list = listFilesOrError(new File(zipPath));
		for(final File dFile : list){
			if(!dFile.isDirectory() || !pluginExists(dFile.getName())) continue;
			// Current dir
			final File oFile = new File(plugin.getDataFolder().getParent(), dFile.getName());
			// List of existing files in the new dir
			final File[] dList = listFilesOrError(dFile);
			// List of existing files in the current dir
			final File[] oList = listFilesOrError(oFile);
			for(File cFile : dList){
				// Loop through all the files in the new dir
//				final boolean found = Arrays.stream(oList).anyMatch(xFile -> xFile.getName().equals(cFile.getName()));
				boolean found = false;
				for(final File xFile : oList){
					// Loop through all the contents in the current dir to see if it exists
					if(xFile.getName().equals(cFile.getName())){found = true; break;}
				}
				if(!found){
					// Move the new file into the current dir
					File output = new File(oFile, cFile.getName());
					fileIOOrError(output, cFile.renameTo(output), true);
				}
				else{
					// This file already exists, so we don't need it anymore.
					fileIOOrError(cFile, cFile.delete(), false);
				}
			}
			fileIOOrError(dFile, dFile.delete(), false);
		}
		File zip = new File(zipPath);
		fileIOOrError(zip, zip.delete(), false);
	}
	/**
	 * Part of Zip-File-Extractor, modified by Gravity for use with Updater.
	 *
	 * @param file the location of the file to extract.
	 */
	private void unzip(String file){
		final File fSourceZip = new File(file);
		try{
			final String zipPath = file.substring(0, file.length() - 4);
			ZipFile zipFile = new ZipFile(fSourceZip);
			Enumeration<? extends ZipEntry> e = zipFile.entries();
			while(e.hasMoreElements()){
				ZipEntry entry = e.nextElement();
				File destinationFilePath = new File(zipPath, entry.getName());
				fileIOOrError(destinationFilePath.getParentFile(), destinationFilePath.getParentFile().mkdirs(), true);
				if(entry.isDirectory()) continue;
				final BufferedInputStream bis = new BufferedInputStream(zipFile.getInputStream(entry));
				int b;
				final byte[] buffer = new byte[Updater.BYTE_SIZE];
				final FileOutputStream fos = new FileOutputStream(destinationFilePath);
				final BufferedOutputStream bos = new BufferedOutputStream(fos, Updater.BYTE_SIZE);
				while((b = bis.read(buffer, 0, Updater.BYTE_SIZE)) != -1) bos.write(buffer, 0, b);
				bos.flush();
				bos.close();
				bis.close();
				final String name = destinationFilePath.getName();
				if(name.endsWith(".jar") && pluginExists(name)){
					File output = new File(updateFolder, name);
					fileIOOrError(output, destinationFilePath.renameTo(output), true);
				}
			}
			zipFile.close();
			// Move any plugin data folders that were included to the right place, Bukkit won't do this for us.
			moveNewZipFiles(zipPath);
		}
		catch(final IOException e){
			plugin.getLogger().log(Level.SEVERE, "The auto-updater tried to unzip a new update file, but was unsuccessful.", e);
			result = Updater.UpdateResult.FAIL_DOWNLOAD;
		}
		finally{fileIOOrError(fSourceZip, fSourceZip.delete(), false);}
	}
	/**
	 * Save an update from dev.bukkit.org into the server's update folder.
	 *
	 * @param file the name of the file to save it as.
	 */
	private void saveFile(String file){
		final File folder = updateFolder;
		deleteOldFiles();
		if(!folder.exists()) fileIOOrError(folder, folder.mkdir(), true);

		downloadFile();
		// Check to see if it's a zip file, if it is, unzip it.
		final File dFile = new File(folder.getAbsolutePath(), file);
		if(dFile.getName().endsWith(".zip")) unzip(dFile.getAbsolutePath());
		if(announce) plugin.getLogger().info("Finished updating.");
	}
	/** Mathematically check if remote version is GREATER than local version.
	 * 
	 * @param localVersion the current version
	 * @param remoteVersion the remote version
	 * @return true if Updater should consider the remote version an update, false if not.
	 */
	private boolean shouldUpdate(String localVersion, String remoteVersion){
		final String[] thisParts = localVersion.replaceAll("[^\\d]+", " ").trim().split(" ");
		final String[] thatParts = remoteVersion.replaceAll("[^\\d]+", " ").trim().split(" ");
		final int minLen = Math.min(thisParts.length, thatParts.length);
		for(int i=0; i<minLen; ++i){
			final int thisV = Integer.parseInt(thisParts[i]), thatV = Integer.parseInt(thatParts[i]);
			if(thisV != thatV) return thatV > thisV;
		}
		return thatParts.length > thisParts.length;
	}
	/**
	 * Evaluate whether the version number is marked showing that it should not be updated by this program.
	 *
	 * @param version a version number to check for tags in.
	 * @return true if updating should be disabled.
	 */
	private boolean hasTag(String version){
//		return Arrays.stream(Updater.NO_UPDATE_TAG).anyMatch(version::contains);
		for(final String string : Updater.NO_UPDATE_TAG) if(version.contains(string)) return true;
		return false;
	}
	/**
	 * Check to see if the program should continue by evaluating whether the plugin is already updated, or shouldn't be updated.
	 *
	 * @return true if the version was located and is not the same as the remote's newest.
	 */
	private boolean pluginVersionCheck(){
		if(updateType == UpdateType.NO_VERSION_CHECK) return true;
		final String localVersion = plugin.getDescription().getVersion();
		if(updateTitle.split(DELIMETER).length == 2){
			// Get the newest file's version number
			final String remoteVersion = updateTitle.split(DELIMETER)[1].split(" ")[0];
			if(hasTag(localVersion) || !shouldUpdate(localVersion, remoteVersion)){
				// We already have the latest version, or this build is tagged for no-update (-PRE, -DEV, -SNAPSHOT)
				result = Updater.UpdateResult.NO_UPDATE;
				return false;
			}
			plugin.getLogger().info("Version update: "+localVersion+" -> "+remoteVersion);
		}
		else{
			// The file's name did not contain the string 'vVersion'
			final String authorInfo = plugin.getDescription().getAuthors().isEmpty() ? "" : " (" + plugin.getDescription().getAuthors().get(0) + ")";
			plugin.getLogger().warning("The author of this plugin" + authorInfo + " has misconfigured their Auto Update system");
			plugin.getLogger().warning("File versions should follow the format 'PluginName vVERSION'");
			plugin.getLogger().warning("Please notify the author of this error.");
			result = Updater.UpdateResult.FAIL_NOVERSION;
			return false;
		}
		return true;
	}
	/**
	 * Check to see if the program should continue by evaluating whether the latest version of the plugin matches the server version
	 *
	 * @return true if the version was located and is compatible with the current server version
	 */
	private boolean gameVersionCheck(){
		if(updateType == UpdateType.NO_VERSION_CHECK) return true;

		//TODO: Implement
		// The update found an update, but it is for a different game version than what the server is currently running.
		//result = UpdateResult.FAIL_GAME_VERSION;

		plugin.getLogger().info("Updater Debug: Plugin update game version(s): "+updateGameVersion);//TODO: remove
		plugin.getLogger().info("Updater Debug: Server game version: "+plugin.getServer().getVersion());//TODO: remove

		return true;
	}
	/**
	 * Check to see if we are permitted to download the available update
	 *
	 * @return true if the available update can be installed
	 */
	private boolean canDownloadUpdate(){
		switch(updateReleaseType){
			case RELEASE:
				if(updateType == UpdateType.DEFAULT) return true;
			case BETA:
				if(updateType == UpdateType.DEFAULT_ALLOW_BETA) return true;
			case ALPHA:
				if(updateType == UpdateType.DEFAULT_ALLOW_ALPHA_AND_BETA) return true;
			case UNKNOWN:
			default:
				// updateType != UpdateType.NO_DOWNLOAD
				return updateType == UpdateType.NO_VERSION_CHECK;
		}
	}
	/**
	 * Make a connection to the BukkitDev API and request the newest file's details.
	 *
	 * @return true if successful.
	 */
	private boolean read(){
		try{
			final URLConnection conn = url.openConnection();
			conn.setConnectTimeout(5000);
			if(apiKey != null) conn.addRequestProperty("X-API-Key", apiKey);
			conn.addRequestProperty("User-Agent", Updater.USER_AGENT);
			conn.setDoOutput(true);
			final BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			final String response = reader.readLine();
			//TODO: not require JSON
			final JSONArray array = (JSONArray) JSONValue.parse(response);
			if(array.isEmpty()){
				plugin.getLogger().warning("The updater could not find any files for the project id " + id);
				result = UpdateResult.FAIL_BADID;
				return false;
			}
			final JSONObject latestUpdate = (JSONObject) array.get(array.size() - 1);
			updateTitle = (String) latestUpdate.get(Updater.TITLE_VALUE);
			updateLink = (String) latestUpdate.get(Updater.LINK_VALUE);
			final String versionTypeStr = (String) latestUpdate.get(Updater.TYPE_VALUE);
			try{updateReleaseType = ReleaseType.valueOf(versionTypeStr.toUpperCase());}
			catch(IllegalArgumentException e){
				plugin.getLogger().warning("The updater does not recognize version type '" + versionTypeStr+"'");
				updateReleaseType = ReleaseType.UNKNOWN;
			}
			updateGameVersion = (String) latestUpdate.get(Updater.VERSION_VALUE);
			return true;
		}
		catch(final IOException e){
			if(e.getMessage().contains("HTTP response code: 403")){
				plugin.getLogger().severe("dev.bukkit.org rejected the provided API key");
				plugin.getLogger().severe("Please double-check your configuration to ensure it is correct.");
				result = UpdateResult.FAIL_APIKEY;
			}
			else{
				plugin.getLogger().severe("The updater could not contact dev.bukkit.org for updating.");
				plugin.getLogger().severe("If you have not recently modified your configuration and this is the "
						+ "first time you areseeing this message, the site may be experiencing temporary downtime.");
				result = UpdateResult.FAIL_DBO;
			}
			plugin.getLogger().log(Level.SEVERE, null, e);
			return false;
		}
	}
}