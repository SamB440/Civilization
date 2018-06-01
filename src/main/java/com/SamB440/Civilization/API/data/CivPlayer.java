package com.SamB440.Civilization.API.data;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import com.SamB440.Civilization.Civilization;
import com.SamB440.Civilization.utils.SQLQuery;

public class CivPlayer {
	
	Civilization plugin;
	Player player;
	OfflinePlayer oplayer;
	FileConfiguration configuration;
	
	/**
	 * @param plugin - The {@link RPGWorld} plugin.
	 * @param player - The Bukkit player.
	 */
	public CivPlayer(Civilization plugin, Player player)
	{
		this.plugin = plugin;
		this.player = player;
		
		File pf = new File(plugin.getDataFolder() + "/storage/" + player.getUniqueId() + ".yml");
		
		if(!plugin.isSQL())
		{
			if(!pf.exists()) {
				try {
					pf.createNewFile();
					
					configuration = YamlConfiguration.loadConfiguration(pf);
					
					configuration.addDefault("settlement", "null");
					configuration.options().copyDefaults(true);
					configuration.save(pf);
					configuration.load(pf);
				} catch(IOException e) {
					e.printStackTrace();
				} catch (InvalidConfigurationException e) {
					e.printStackTrace();
				}
			}
		} else {
			try {
				PreparedStatement statement = plugin.getSQL().prepareStatement("SELECT uuid FROM PlayerData WHERE uuid = '" + player.getUniqueId().toString().replaceAll("-", "") + "'");
				ResultSet rs = statement.executeQuery();
				if(!rs.next())
				{
					statement.executeUpdate("INSERT INTO PlayerData (uuid, level, multiplier, class, settlement) VALUES ('" + player.getUniqueId().toString().replaceAll("-", "") + "', 1, 1, " + null + ", " + null + ")");
					player.setLevel(1);
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		configuration = YamlConfiguration.loadConfiguration(pf);
		
		configuration.addDefault("origin.x", player.getLocation().getX());
		configuration.addDefault("origin.y", player.getLocation().getY());
		configuration.addDefault("origin.z", player.getLocation().getZ());
		configuration.addDefault("viewing", false);
		
		for(File f : new File(plugin.getDataFolder() + "/classes/").listFiles())
		{
			String s = f.getName().replace(".yml", "").trim();
			configuration.addDefault(s + "-level", player.getLevel());
			configuration.addDefault(s + ".log-out.exp", player.getExp());
			configuration.addDefault(s + ".log-out.world", player.getLocation().getWorld());
			configuration.addDefault(s + ".log-out.x", player.getLocation().getX());
			configuration.addDefault(s + ".log-out.y", player.getLocation().getY());
			configuration.addDefault(s + ".log-out.z", player.getLocation().getZ());
			configuration.addDefault(s + ".log-out.pitch", player.getEyeLocation().getPitch());
			configuration.addDefault(s + ".log-out.yaw", player.getEyeLocation().getYaw());
			List<String> con = new ArrayList<String>();
			configuration.addDefault(s + "-inventory", con);
			try {
				configuration.save(new File(plugin.getDataFolder() + "/storage/" + player.getUniqueId() + ".yml"));
				configuration.load(new File(plugin.getDataFolder() + "/storage/" + player.getUniqueId() + ".yml"));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InvalidConfigurationException e) {
				e.printStackTrace();
			}
		}
	}
	
	public CivPlayer(Civilization plugin, OfflinePlayer player)
	{
		this.plugin = plugin;
		this.oplayer = player;
		
		File pf = new File(plugin.getDataFolder() + "/storage/" + player.getUniqueId() + ".yml");
		
		configuration = YamlConfiguration.loadConfiguration(pf);
	}
	
	/**
	 * @return The Bukkit player.
	 */
	public Player getBukkitPlayer()
	{
		return player;
	}
	
	/**
	 * This method is not executed asynchronously.
	 * @return the settlement the player is in. May be null.
	 */
	public Settlement getSettlement()
	{
		if(!plugin.isSQL()) 
		{
			if(!configuration.getString("settlement").equals("null")) return new Settlement(plugin, configuration.getString("settlement"));
			else return null;
		} else {
			SQLQuery sq = new SQLQuery(plugin);
			String s = sq.getString("settlement", "PlayerData", player.getUniqueId());
			if(s != null) return new Settlement(plugin, s);
			else return null;
		}
	}
	
	public boolean isViewing()
	{
		return configuration.getBoolean("viewing");
	}
	
	public void setViewing(boolean val, Location origin)
	{
		configuration.set("viewing", val);
		configuration.set("origin.x", origin.getX());
		configuration.set("origin.y", origin.getY());
		configuration.set("origin.z", origin.getZ());
		reloadConfiguration();
	}
	
	public Location getOrigin()
	{
		return new Location(getBukkitPlayer().getWorld(), configuration.getDouble("origin.x"), configuration.getDouble("origin.y"), configuration.getDouble("origin.z"));
	}
	
	public void setSettlement(Settlement settlement)
	{
		UUID uuid;
		if(player != null) uuid = player.getUniqueId();
		else uuid = oplayer.getUniqueId();
		if(!plugin.isSQL()) 
		{
			if(settlement != null) configuration.set("settlement", settlement.getName());
			else configuration.set("settlement", "null");
			reloadConfiguration();
		} else {
			SQLQuery sq = new SQLQuery(plugin);
			if(settlement != null) sq.set("settlement", "PlayerData", settlement.getName(), uuid);
			else sq.setNull("settlement", "PlayerData", uuid);
		}
	}
	
	/**
	 * Force update a player's stats.
	 */
	public void update()
	{

		File pf = new File(plugin.getDataFolder() + "/storage/" + player.getUniqueId() + ".yml");
		
		if(!plugin.isSQL())
		{
			if(!pf.exists()) {
				try {
					pf.createNewFile();
					
					configuration = YamlConfiguration.loadConfiguration(pf);
					
					configuration.addDefault("settlement", "null");
					configuration.options().copyDefaults(true);
					configuration.save(pf);
					configuration.load(pf);
				} catch(IOException e) {
					e.printStackTrace();
				} catch (InvalidConfigurationException e) {
					e.printStackTrace();
				}
			}
		} else {
			try {
				PreparedStatement statement = plugin.getSQL().prepareStatement("SELECT uuid FROM PlayerData WHERE uuid = '" + player.getUniqueId().toString().replaceAll("-", "") + "'");
				ResultSet rs = statement.executeQuery();
				if(!rs.next())
				{
					statement.executeUpdate("INSERT INTO PlayerData (uuid, level, multiplier, class, settlement) VALUES ('" + player.getUniqueId().toString().replaceAll("-", "") + "', 1, 1, " + null + ", " + null + ")");
					player.setLevel(1);
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		configuration = YamlConfiguration.loadConfiguration(pf);
		
		configuration.addDefault("origin.x", player.getLocation().getX());
		configuration.addDefault("origin.y", player.getLocation().getY());
		configuration.addDefault("origin.z", player.getLocation().getZ());
		configuration.addDefault("viewing", false);
	}
	
	private void reloadConfiguration()
	{
		File pf = new File(plugin.getDataFolder() + "/storage/" + player.getUniqueId() + ".yml");
		try {
			configuration.save(pf);
			configuration.load(pf);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InvalidConfigurationException e) {
			e.printStackTrace();
		}
	}
}
