package messagefiles.plugins;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.util.Properties;
import java.util.Map.Entry;

import play.Logger;
import play.Play;
import play.PlayPlugin;
import play.i18n.Messages;
import play.libs.IO;
import play.vfs.VirtualFile;

public class MessageFilesPlugin extends PlayPlugin {
	private static final String CONF_LANGS_PATH_KEY = "messagefiles.path";
	private static final String CONF_DEFAULT_LOCALE_KEY = "messagefiles.defaultLocale";
	private static final String PROPERTIES_EXTENSION = ".properties";
	private static long lastLoading = 0L;
	private FilenameFilter propertiesFileFilter;
	
	public MessageFilesPlugin() {
		propertiesFileFilter = new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.contains(PROPERTIES_EXTENSION);
			}
		};
	}
	
	@Override
	public void onApplicationStart() {
		loadMessages();
	}
	
	@Override
	public void detectChange() {
		VirtualFile langsRoot = getLangsRoot();
		
		if (langsRoot != null) {
			for (String locale : Play.langs) {
				VirtualFile localeRoot = langsRoot.child(locale);
				if (localeRoot != null && localeRoot.exists()) {
					for (File file : localeRoot.getRealFile().listFiles(propertiesFileFilter)) {
						if (file.lastModified() > lastLoading) {
							loadMessages();
							return;
						}
					}
				}
			}
		}
	}
	
	private void loadMessages() {
		String langsPath = getLangsPath();
		VirtualFile langsRoot = getLangsRoot();
		String defaultLocale = Play.configuration.getProperty(CONF_DEFAULT_LOCALE_KEY);
		
		if (defaultLocale == null) {
			Logger.warn("%s is not defined in application.conf", CONF_DEFAULT_LOCALE_KEY);
		}
		
		if (langsPath == null) {
			Logger.warn("%s is not defined in application.conf", CONF_LANGS_PATH_KEY);
		} else if (langsRoot == null || !langsRoot.exists()) {
			Logger.error("%s is defined in application.conf but respective folder could not be found", CONF_LANGS_PATH_KEY);
		} else {
			for (String locale : Play.langs) {
				VirtualFile localeRoot = langsRoot.child(locale);
				if (localeRoot == null || !localeRoot.exists()) {
					Logger.warn("Could not find %s locale in %s", locale, langsPath);
				} else {
					for (File file : localeRoot.getRealFile().listFiles(propertiesFileFilter)) {
						try {
							String prefix = file.getName().replace(PROPERTIES_EXTENSION, "");
							Properties props = readLocalizationFile(file);
							Properties propsWithPrefix = new Properties();
							
							// Prepend prefix.
							for (Entry<Object, Object> prop : props.entrySet()) {
								propsWithPrefix.put(prefix + "." + (String)prop.getKey(), prop.getValue());
							}
							
							Messages.locales.put(locale, propsWithPrefix);
							
							// Put messages from this locale in Play default messages 
							// if this locale is defined as default in configuration.
							if (defaultLocale != null && defaultLocale.equals(locale)) {
								if (Messages.defaults == null) {
									Messages.defaults = new Properties();
								}
								Messages.defaults.putAll(propsWithPrefix);
							}
						} catch (FileNotFoundException e) {
							Logger.warn(e, "Could not read %s", file.getAbsolutePath());
						}
					}
				}
			}
		}
	}
	
	private String getLangsPath() {
		return Play.configuration.getProperty(CONF_LANGS_PATH_KEY);
	}
	
	private VirtualFile getLangsRoot() {
		return getLangsPath() != null ? Play.getVirtualFile(getLangsPath()) : null;
	}
	
	private Properties readLocalizationFile(File f) throws FileNotFoundException {
		if (f != null) {
			return IO.readUtf8Properties(new FileInputStream(f));
		}
		
		return null;
	}
}
