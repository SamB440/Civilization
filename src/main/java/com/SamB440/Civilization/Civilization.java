package com.SamB440.Civilization;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.SamB440.Civilization.commands.SettlementCommand;

import lombok.Getter;

public class Civilization extends JavaPlugin {
	
    @Getter private Connection sql;
    private String host, database, username, password;
    private int port;
    
    private String c = "[Civilization] ";
    
    @Getter private FileConfiguration lang;
    
    Logger log = Bukkit.getLogger();
    
    @Getter List<Entity> removeOnDisable = new ArrayList<Entity>();
	
	@Override
	public void onEnable()
	{
		createConfig();
		addFiles();
		createLang();
		registerListeners();
		registerCommands();
		startTasks();
		startSQL();
	}
	
	@Override
	public void onDisable()
	{
		for(Entity entity : removeOnDisable)
		{
			entity.remove();
		}
		
		if(isSQL())
		{
			try {
				sql.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void createConfig()
	{
		getConfig().options().copyDefaults(true);
		getConfig().addDefault("Server.sql.Enabled", false);
		getConfig().addDefault("Server.sql.host", "localhost");
		getConfig().addDefault("Server.sql.port", 3306);
		getConfig().addDefault("Server.sql.database", "RPGWorld");
		getConfig().addDefault("Server.sql.username", "root");
		getConfig().addDefault("Server.sql.password", "123");
		saveConfig();
	}
	
	private void createLang()
	{
		File langfile = new File(getDataFolder() + "/lang/messages.yml");
		if(!langfile.exists())
		{
			saveResource("messages.yml", true);
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
				PreparedStatement statement = sql.prepareStatement("CREATE TABLE IF NOT EXISTS PlayerData (uuid varchar(36) NOT NULL, settlement varchar(36), PRIMARY KEY (uuid))");
				statement.executeUpdate();
				
				PreparedStatement s3 = sql.prepareStatement("CREATE TABLE IF NOT EXISTS Settlements (king varchar(36) NOT NULL, name varchar(36), level int, science int, PRIMARY KEY(king, name))");
				s3.executeUpdate();
				
				PreparedStatement s4 = sql.prepareStatement("CREATE TABLE IF NOT EXISTS Tech (settlement varchar(36) NOT NULL, tech varchar(36), PRIMARY KEY(settlement, tech))");
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
	    if(sql != null && !sql.isClosed()) return;
	 
	    synchronized (this) 
	    {
	        if(sql != null && !sql.isClosed()) return;
	        Class.forName("com.mysql.jdbc.Driver");
	        sql = DriverManager.getConnection("jdbc:mysql://" + this.host+ ":" + this.port + "/" + this.database, this.username, this.password);
	    }
	}
	
	private void registerListeners()
	{
		PluginManager pm = Bukkit.getServer().getPluginManager();
	}
	
	private void registerCommands()
	{
		getCommand("Settlement").setExecutor(new SettlementCommand(this));
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
