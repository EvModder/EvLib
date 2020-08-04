package net.evmodder.EvLib;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
/**
 * Check for updates on BukkitDev for a given plugin, and download the updates if needed.
 * <p>
 * <b>VERY, VERY IMPORTANT</b>: Because there are no standards for adding auto-update toggles in your plugin's config, this system provides NO CHECK WITH YOUR CONFIG to make sure the user has allowed auto-updating.
 * <br>
 * It is a <b>BUKKIT POLICY</b> that you include a boolean value in your config that prevents the auto-updater from running <b>AT ALL</b>.
 * <br>
 * If you fail to include this option in your config, your plugin will be <b>REJECTED</b> when you attempt to submit it to dev.bukkit.org.
 * </p>
 * An example of a good configuration option would be something similar to 'auto-update: true' - if this value is set to false you may NOT run the auto-updater.
 * <br>
 * If you are unsure about these rules, please read the plugin submission guidelines: http://goo.gl/8iU5l
 *
 * @author Gravity
 * @version 2.3
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
	private static final String API_KEY_CONFIG_KEY = "api-key";// Config key for api key
	private static final String DISABLE_CONFIG_KEY = "disable";// Config key for disabling Updater
	private static final String API_KEY_DEFAULT = "PUT_API_KEY_HERE";// Default api key value in config
	private static final boolean DISABLE_DEFAULT = false;// Default disable value in config

	/* User-provided variables */
	private final Plugin plugin;// Plugin running Updater
	private final UpdateType type;// Type of update check to run
	private final boolean announce;// Whether to announce file downloads
	private final File file;// The plugin file (jar)
	private final File updateFolder;// The folder that downloads will be placed in
	private final UpdateCallback callback;// The provided callback (if any)
	private int id = -1;// Project's Curse ID
	private String apiKey = null;// BukkitDev ServerMods API key

	/* Collected from Curse API */
	private String versionName;
	private String versionLink;
	private String versionType;
	private String versionGameVersion;

	/* Update process variables */
	private URL url;// Connection to RSS
	private Thread thread;// Updater thread
	private Updater.UpdateResult result = Updater.UpdateResult.SUCCESS;// Used for determining the outcome of the update process

	/**
	 * Gives the developer the result of the update process. Can be obtained by called {@link #getResult()}
	 */
	public enum UpdateResult{
		SUCCESS,/** The updater found an update, and has readied it to be loaded the next time the server restarts/reloads. */
		NO_UPDATE,/** The updater did not find an update, and nothing was downloaded. */
		DISABLED,/** The server administrator has disabled the updating system. */
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
		DEFAULT,/** Run a version check, and then if the file is out of date, download the newest version. */
		NO_VERSION_CHECK,/** Don't run a version check, just find the latest update and download it. */
		NO_DOWNLOAD/** Get information about the version and the download size, but don't actually download anything. */
	}
	/**
	 * Represents the various release types of a file on BukkitDev.
	 */
	public enum ReleaseType{
		ALPHA,/** An "alpha" file. */
		BETA,/** A "beta" file. */
		RELEASE/** A "release" file. */
	}
	/**
	 * Initialize the updater.
	 *
	 * @param plugin The plugin that is checking for an update.
	 * @param id The dev.bukkit.org id of the project.
	 * @param file The file that the plugin is running from, get this by doing this.getFile() from within your main class.
	 * @param type Specify the type of update this will be. See {@link UpdateType}
	 * @param announce True if the program should announce the progress of new updates in console.
	 */
	public Updater(Plugin plugin, int id, File file, UpdateType type, boolean announce){
		this(plugin, id, file, type, null, announce);
	}
	/**
	 * Initialize the updater with the provided callback.
	 *
	 * @param plugin The plugin that is checking for an update.
	 * @param id The dev.bukkit.org id of the project.
	 * @param file The file that the plugin is running from, get this by doing this.getFile() from within your main class.
	 * @param type Specify the type of update this will be. See {@link UpdateType}
	 * @param callback The callback instance to notify when the Updater has finished
	 */
	public Updater(Plugin plugin, int id, File file, UpdateType type, UpdateCallback callback){
		this(plugin, id, file, type, callback, false);
	}
	/**
	 * Initialize the updater with the provided callback.
	 *
	 * @param plugin The plugin that is checking for an update.
	 * @param id The dev.bukkit.org id of the project.
	 * @param file The file that the plugin is running from, get this by doing this.getFile() from within your main class.
	 * @param type Specify the type of update this will be. See {@link UpdateType}
	 * @param callback The callback instance to notify when the Updater has finished
	 * @param announce True if the program should announce the progress of new updates in console.
	 */
	public Updater(Plugin plugin, int id, File file, UpdateType type, UpdateCallback callback, boolean announce){
		this.plugin = plugin;
		this.type = type;
		this.announce = announce;
		this.file = file;
		this.id = id;
		this.updateFolder = plugin.getServer().getUpdateFolderFile();
		this.callback = callback;
		final File pluginFile = plugin.getDataFolder().getParentFile();
		final File updaterFile = new File(pluginFile, "Updater");
		final File updaterConfigFile = new File(updaterFile, "config.yml");
		YamlConfiguration config = new YamlConfiguration();
		config.options().header("This configuration file affects all plugins using the Updater system "
				+ "(version 2+ - http://forums.bukkit.org/threads/96681/ )" + '\n'
				+ "If you wish to use your API key, read http://wiki.bukkit.org/ServerMods_API and place it below." + '\n'
				+ "Some updating systems will not adhere to the disabled value, but these may be turned off in their plugin's configuration.");
		config.addDefault(API_KEY_CONFIG_KEY, API_KEY_DEFAULT);
		config.addDefault(DISABLE_CONFIG_KEY, DISABLE_DEFAULT);
		if(!updaterFile.exists()) fileIOOrError(updaterFile, updaterFile.mkdir(), true);
		boolean createFile = !updaterConfigFile.exists();
		try{
			if(createFile){
				fileIOOrError(updaterConfigFile, updaterConfigFile.createNewFile(), true);
				config.options().copyDefaults(true);
				config.save(updaterConfigFile);
			}
			else {
				config.load(updaterConfigFile);
			}
		}
		catch(Exception e){
			String message;
			if(createFile) message = "The updater could not create configuration at " + updaterFile.getAbsolutePath();
			else message = "The updater could not load configuration at " + updaterFile.getAbsolutePath();
			plugin.getLogger().log(Level.SEVERE, message, e);
		}
		if(config.getBoolean(DISABLE_CONFIG_KEY)){
			result = UpdateResult.DISABLED;
			return;
		}
		String key = config.getString(API_KEY_CONFIG_KEY);
		if(API_KEY_DEFAULT.equalsIgnoreCase(key) || "".equals(key)) key = null;
		apiKey = key;
		try{url = new URL(Updater.HOST + Updater.QUERY + id);}
		catch(MalformedURLException e){
			plugin.getLogger().log(Level.SEVERE, "The project ID provided for updating, " + id + " is invalid.", e);
			result = UpdateResult.FAIL_BADID;
		}
		if(result != UpdateResult.FAIL_BADID){
			thread = new Thread(new UpdateRunnable());
			thread.start();
		}
		else runUpdater();
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
	/**
	 * Get the latest version's release type.
	 *
	 * @return latest version's release type.
	 * @see ReleaseType
	 */
	public ReleaseType getLatestType(){
		waitForThread();
		if(versionType != null){
			for(ReleaseType type : ReleaseType.values()){
				if(versionType.equalsIgnoreCase(type.name())) return type;
			}
		}
		return null;
	}
	/**
	 * Get the latest version's game version (such as "CB 1.2.5-R1.0").
	 *
	 * @return latest version's game version.
	 */
	public String getLatestGameVersion(){
		waitForThread();
		return versionGameVersion;
	}
	/**
	 * Get the latest version's name (such as "Project v1.0").
	 *
	 * @return latest version's name.
	 */
	public String getLatestName(){
		waitForThread();
		return versionName;
	}
	/**
	 * Get the latest version's direct file link.
	 *
	 * @return latest version's file link.
	 */
	public String getLatestFileLink(){
		waitForThread();
		return versionLink;
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
	/**
	 * Download a file and save it to the specified folder.
	 */
	private void downloadFile(){
		BufferedInputStream in = null;
		FileOutputStream fout = null;
		try{
			URL fileUrl = new URL(versionLink);
			final int fileLength = fileUrl.openConnection().getContentLength();
			in = new BufferedInputStream(fileUrl.openStream());
			fout = new FileOutputStream(new File(updateFolder, file.getName()));
			final byte[] data = new byte[Updater.BYTE_SIZE];
			int count;
			if(announce) plugin.getLogger().info("About to download a new update: " + versionName);
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
	 * Remove possibly leftover files from the update folder.
	 */
	private void deleteOldFiles(){
		//Just a quick check to make sure we didn't leave any files from last time...
		File[] list = listFilesOrError(updateFolder);
		for(final File xFile : list){
			if(xFile.getName().endsWith(".zip")) fileIOOrError(xFile, xFile.mkdir(), true);
		}
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
				if(!entry.isDirectory()){
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
	 * Find any new files extracted from an update into the plugin's data directory.
	 * @param zipPath path of extracted files.
	 */
	private void moveNewZipFiles(String zipPath){
		File[] list = listFilesOrError(new File(zipPath));
		for(final File dFile : list){
			if(dFile.isDirectory() && pluginExists(dFile.getName())) {
				// Current dir
				final File oFile = new File(plugin.getDataFolder().getParent(), dFile.getName());
				// List of existing files in the new dir
				final File[] dList = listFilesOrError(dFile);
				// List of existing files in the current dir
				final File[] oList = listFilesOrError(oFile);
				for(File cFile : dList){
					// Loop through all the files in the new dir
					boolean found = false;
					for(final File xFile : oList){
						// Loop through all the contents in the current dir to see if it exists
						if(xFile.getName().equals(cFile.getName())){
							found = true;
							break;
						}
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
			}
			fileIOOrError(dFile, dFile.delete(), false);
		}
		File zip = new File(zipPath);
		fileIOOrError(zip, zip.delete(), false);
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
	 * Check to see if the program should continue by evaluating whether the plugin is already updated, or shouldn't be updated.
	 *
	 * @return true if the version was located and is not the same as the remote's newest.
	 */
	private boolean versionCheck(){
		final String title = versionName;
		if(type != UpdateType.NO_VERSION_CHECK){
			final String localVersion = plugin.getDescription().getVersion();
			if(title.split(DELIMETER).length == 2) {
				// Get the newest file's version number
				final String remoteVersion = title.split(DELIMETER)[1].split(" ")[0];
				if(hasTag(localVersion) || !shouldUpdate(localVersion, remoteVersion)){
					// We already have the latest version, or this build is tagged for no-update
					result = Updater.UpdateResult.NO_UPDATE;
					return false;
				}
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
		}
		return true;
	}
	/**
	 * <b>If you wish to run mathematical versioning checks, edit this method.</b>
	 * <p>
	 * With default behavior, Updater will NOT verify that a remote version available on BukkitDev
	 * which is not this version is indeed an "update".
	 * If a version is present on BukkitDev that is not the version that is currently running,
	 * Updater will assume that it is a newer version.
	 * This is because there is no standard versioning scheme, and creating a calculation that can
	 * determine whether a new update is actually an update is sometimes extremely complicated.
	 * </p>
	 * <p>
	 * Updater will call this method from {@link #versionCheck()} before deciding whether
	 * the remote version is actually an update.
	 * If you have a specific versioning scheme with which a mathematical determination can
	 * be reliably made to decide whether one version is higher than another, you may
	 * revise this method, using the local and remote version parameters, to execute the
	 * appropriate check.
	 * </p>
	 * <p>
	 * Returning a value of <b>false</b> will tell the update process that this is NOT a new version.
	 * Without revision, this method will always consider a remote version at all different from
	 * that of the local version a new update.
	 * </p>
	 * @param localVersion the current version
	 * @param remoteVersion the remote version
	 * @return true if Updater should consider the remote version an update, false if not.
	 */
	public boolean shouldUpdate(String localVersion, String remoteVersion){
		return localVersion.compareTo(remoteVersion) < 0; // TODO: <eg: 1.11 vs 1.9> returns incorrect result
	}
	/**
	 * Evaluate whether the version number is marked showing that it should not be updated by this program.
	 *
	 * @param version a version number to check for tags in.
	 * @return true if updating should be disabled.
	 */
	private boolean hasTag(String version){
		for(final String string : Updater.NO_UPDATE_TAG){
			if(version.contains(string)) return true;
		}
		return false;
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
			final JSONArray array = (JSONArray) JSONValue.parse(response);
			if(array.isEmpty()){
				plugin.getLogger().warning("The updater could not find any files for the project id " + id);
				result = UpdateResult.FAIL_BADID;
				return false;
			}
			JSONObject latestUpdate = (JSONObject) array.get(array.size() - 1);
			versionName = (String) latestUpdate.get(Updater.TITLE_VALUE);
			versionLink = (String) latestUpdate.get(Updater.LINK_VALUE);
			versionType = (String) latestUpdate.get(Updater.TYPE_VALUE);
			versionGameVersion = (String) latestUpdate.get(Updater.VERSION_VALUE);
			return true;
		}
		catch(final IOException e){
			if(e.getMessage().contains("HTTP response code: 403")){
				plugin.getLogger().severe("dev.bukkit.org rejected the API key provided in plugins/Updater/config.yml");
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
	private class UpdateRunnable implements Runnable{
		@Override public void run(){runUpdater();}
	}
	private void runUpdater(){
		if(url != null && (read() && versionCheck())){
			// Obtain the results of the project's file feed
			if((versionLink != null) && (type != UpdateType.NO_DOWNLOAD)){
				String name = file.getName();
				// If it's a zip file, it shouldn't be downloaded as the plugin's name
				if(versionLink.endsWith(".zip")) name = versionLink.substring(versionLink.lastIndexOf("/") + 1);
				saveFile(name);
			}
			else result = UpdateResult.UPDATE_AVAILABLE;
		}
		if(callback != null) new BukkitRunnable(){@Override public void run(){runCallback();}}.runTask(plugin);
	}
	private void runCallback(){callback.onFinish(this);}
}