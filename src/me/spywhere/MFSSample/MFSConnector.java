package me.spywhere.MFSSample;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Logger;

import lib.spywhere.MFS.MFS;
import lib.spywhere.MFS.StorageType;

import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.UnknownDependencyException;

// TODO: Auto-generated Javadoc
/**
 * The Class MFSConnector.
 */
public class MFSConnector {
	
	/** The log. */
	private static Logger log=Logger.getLogger("Minecraft");

	/** The mfs. */
	private static MFS mfs=null;
	
	/** The pm. */
	private static PluginManager pm=null;

	/**
	 * Checks if is connected.
	 *
	 * @return true, if is connected
	 */
	public static boolean isConnected(){
		return (mfs!=null);
	}

	/**
	 * Gets the MFS.
	 *
	 * @param ipm Input PluginManager
	 * @param url Database host url
	 * @param user Database user
	 * @param password Database password
	 * @param type Storage type
	 * @return MFS
	 */
	static public MFS getMFS(PluginManager ipm,String url, String user, String password, StorageType type){
		loadPlugin(ipm);
		pm=ipm;
		if(pm.getPlugin("MFS")!=null){
			if(!new File("lib",type.getFileName()).exists()){
				log.info("[MFSConnector] Downloading "+type.getName()+" library from server...");
				if(!downloadLib(type)){
					log.severe("[MFSConnector] Error downloading "+type.getName()+" library.");
					return null;
				}
				log.info("[MFSConnector] Download successful.");
			}
			mfs=(MFS)pm.getPlugin("MFS");
			mfs.setMFS(url,user,password,type);
			return mfs;
		}
		return null;
	}

	/**
	 * Gets the mFS.
	 *
	 * @param ipm the ipm
	 * @param type the type
	 * @return the mFS
	 */
	static public MFS getMFS(PluginManager ipm, StorageType type){
		return getMFS(ipm,"","","",type);
	}

	/**
	 * Download lib.
	 *
	 * @param type the type
	 * @return true, if successful
	 */
	static private boolean downloadLib(StorageType type){
		try {
			if(!new File("lib",type.getFileName()).exists()){
				if(!type.equals(StorageType.FLATFILE)&&!type.equals(StorageType.YML)){
					download("lib",type.getLibraryURL(), type.getFileName());
				}
			}
		} catch (IOException e) {
			return false;
		}
		return true;
	}

	/**
	 * Download.
	 *
	 * @param time the time
	 * @return true, if successful
	 */
	static private boolean download(int time){
		try {
			if(time>=2){return true;}
			if(time==0){
				download("plugins",Mirror.Server1.getUrl(), "MFS.jar");
			}
			if(time==1){
				download("plugins",Mirror.Server2.getUrl(), "MFS.jar");	
			}
		} catch (IOException e) {
			return false;
		}
		return true;
	}

	/**
	 * Load plugin.
	 *
	 * @param pm the pm
	 */
	static private void loadPlugin(PluginManager pm){
		if(pm.getPlugin("MFS")==null){
			try {
				pm.loadPlugin(new File("plugins","MFS.jar"));
			} catch (UnknownDependencyException e) {
				log.severe("[MFSConnector] Error UnknownDependency: "+e.getMessage());
			} catch (InvalidPluginException e) {
				log.severe("[MFSConnector] Error InvalidPlugin: "+e.getMessage());
			} catch (InvalidDescriptionException e) {
				log.severe("[MFSConnector] Error InvalidDescription: "+e.getMessage());
			}
		}else{
			if(!pm.getPlugin("MFS").isEnabled()){
				pm.enablePlugin(pm.getPlugin("MFS"));
			}
		}
	}

	/**
	 * Prepare mfs.
	 *
	 * @param pdf the pdf
	 * @param pm the pm
	 * @return true, if successful
	 */
	static public boolean prepareMFS(PluginDescriptionFile pdf,PluginManager pm){
		//Download all required files
		if(!new File("plugins","MFS.jar").exists()){
			log.info("[MFSConnector] Downloading MFS.jar from server...");
			int m=0;
			while(!download(m)){
				m++;
				if(m<2){
					log.info("[MFSConnector] Downloading MFS.jar from Server "+(m+1)+"...");
				}
			}
			if(m>=2){
				log.severe("[MFSConnector] Error downloading MFS.jar.");
				return false;
			}else{
				log.info("[MFSConnector] Download successful.");
			}
		}
		log.info("[MFSConnector] Starting MFS...");	
		loadPlugin(pm);
		return true;
	}

	/**
	 * The Enum Mirror.
	 */
	private static enum Mirror{
		
		/** The Server1. */
		Server1("http://dl.dropbox.com/u/65468988/Plugins/MFS/Stable%20Build/v0.2/MFS.jar"),
		
		/** The Server2. */
		Server2("http://dev.bukkit.org/media/files/588/699/MFS.jar");

		/**
		 * Instantiates a new mirror.
		 *
		 * @param url the url
		 */
		private Mirror(String url){
			this.url=url;
		}

		/** The url. */
		String url;
		
		/**
		 * Gets the url.
		 *
		 * @return the url
		 */
		public String getUrl() {
			return this.url;
		}
	}

	/** The cancelled. */
	protected static boolean cancelled;

	/**
	 * Cancel.
	 */
	public synchronized void cancel()
	{
		cancelled = true;
	}

	/**
	 * Download.
	 *
	 * @param fdr the fdr
	 * @param location the location
	 * @param filename the filename
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	protected static synchronized void download(String fdr,String location, String filename) throws IOException {
		URLConnection connection = new URL(location).openConnection();
		connection.setUseCaches(false);
		String destination = fdr + File.separator + filename;
		File parentDirectory = new File(destination).getParentFile();
		if (parentDirectory != null) {
			parentDirectory.mkdirs();
		}
		InputStream in = connection.getInputStream();
		OutputStream out = new FileOutputStream(destination);
		byte[] buffer = new byte[65536];

		while (!cancelled)
		{
			int count = in.read(buffer);
			if (count < 0) {
				break;
			}
			out.write(buffer, 0, count);
		}

		in.close();
		out.close();
	}
}
