package com.SamB440.Civilization;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import lombok.Getter;

public class Civilization extends JavaPlugin {
	
    @Getter private Connection SQL;
    private String host, database, username, password;
    private int port;
    
    private String c = "[Civilization] ";
    
    @Getter private FileConfiguration lang;
    
    Logger log = Bukkit.getLogger();
	
	@Override
	public void onEnable()
	{
		createConfig();
		addFiles();
		createLang();
		registerListeners();
		startTasks();
		startSQL();
	}
	
	@Override
	public void onDisable()
	{
		
	}
	
	private void createConfig()
	{
		getConfig().options().copyDefaults(true);
		getConfig().addDefault("Server.SQL.Enabled", false);
		getConfig().addDefault("Server.SQL.host", "localhost");
		getConfig().addDefault("Server.SQL.port", 3306);
		getConfig().addDefault("Server.SQL.database", "RPGWorld");
		getConfig().addDefault("Server.SQL.username", "root");
		getConfig().addDefault("Server.SQL.password", "123");
		saveConfig();
	}
	
	private void createLang()
	{
		File langfile = new File(getDataFolder() + "/lang/messages.yml");
		if(!langfile.exists())
		{
			saveResource("lang/messages.yml", true);
			log.info(c + "Created language file.");
		}
		
		lang = YamlConfiguration.loadConfiguration(langfile);
	}
	
	private void startSQL()
	{
		if(isSQL())
		{
			log.info(c + "Starting MySQL...");
	        host = getConfig().getString("Server.SQL.host");
	        port = getConfig().getInt("Server.SQL.port");
	        database = getConfig().getString("Server.SQL.database") + "?useSSL=false" + "&autoReconnect=true";
	        username = getConfig().getString("Server.SQL.username");
	        password = getConfig().getString("Server.SQL.password");
	        try {
				openConnection();
				PreparedStatement statement = SQL.prepareStatement("CREATE TABLE IF NOT EXISTS PlayerData (uuid varchar(36) NOT NULL, settlement varchar(36), PRIMARY KEY (uuid))");
				statement.executeUpdate();
				
				PreparedStatement s3 = SQL.prepareStatement("CREATE TABLE IF NOT EXISTS Settlements (king varchar(36) NOT NULL, name varchar(36), level int, science int, PRIMARY KEY(king, name))");
				s3.executeUpdate();
				
				PreparedStatement s4 = SQL.prepareStatement("CREATE TABLE IF NOT EXISTS Tech (settlement varchar(36) NOT NULL, tech varchar(36), PRIMARY KEY(settlement, tech))");
				s4.executeUpdate();
				
				statement.close();
			} catch (ClassNotFoundException | SQLException e) {
				log.severe(c + "There was an error whilst starting MySQL as follows;");
				e.printStackTrace();
				Bukkit.getServer().getPluginManager().disablePlugin(this);
			}
		}
	}
	
	public void openConnection() throws SQLException, ClassNotFoundException 
	{
	    if(SQL != null && !SQL.isClosed()) return;
	 
	    synchronized (this) 
	    {
	        if(SQL != null && !SQL.isClosed()) return;
	        Class.forName("com.mysql.jdbc.Driver");
	        SQL = DriverManager.getConnection("jdbc:mysql://" + this.host+ ":" + this.port + "/" + this.database, this.username, this.password);
	    }
	}
	
	private void registerListeners()
	{
		PluginManager pm = Bukkit.getServer().getPluginManager();
	}
	
	private void startTasks()
	{

	}

	private void addFiles()
	{
		File folder = new File("plugins/Civilization/storage/");
		File folder2 = new File("plugins/Civilization/lang/");
		File folder3 = new File("plugins/Civilization/claims/");
		File folder4 = new File("plugins/Civilization/settlements/");
		if(!folder.exists()) folder.mkdir();
		if(!folder2.exists()) folder2.mkdir();
		if(!folder3.exists()) folder3.mkdir();
		if(!folder4.exists()) folder4.mkdir();
	}
	
	public boolean isSQL()
	{
		return getConfig().getBoolean("Server.SQL.Enabled");
	}
}
