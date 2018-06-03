package com.SamB440.Civilization.API.data;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
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
	Connection sql;
	
	/**
	 * @param plugin - The {@link Civilization} plugin.
	 * @param player - The Bukkit player.
	 */
	public CivPlayer(Civilization plugin, Player player)
	{
		this.plugin = plugin;
		this.player = player;
		this.sql = plugin.getSql();
		
		File pf = new File(plugin.getDataFolder() + "/storage/" + player.getUniqueId() + ".yml");
		
		if(!plugin.isSQL())
		{
			if(!pf.exists()) {
				try {
					pf.createNewFile();
					
					configuration = YamlConfiguration.loadConfiguration(pf);
					
					configuration.addDefault("settlement", "null");
					configuration.options().copyDefaults(true);
					reloadConfiguration();
				} catch(IOException e) {
					e.printStackTrace();
				}
			}
		} else {
			try {
				PreparedStatement statement = sql.prepareStatement("SELECT uuid FROM PlayerData WHERE uuid = ?");
				statement.setString(1, player.getUniqueId().toString().replace("-", ""));
				ResultSet rs = statement.executeQuery();
				if(!rs.next())
				{
					PreparedStatement statement2 = sql.prepareStatement("INSERT INTO PlayerData (uuid, settlement) VALUES (?, ?)");
					statement2.setString(1, player.getUniqueId().toString().replace("-", ""));
					statement2.setNull(2, Types.OTHER);
					statement2.executeUpdate();
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
	
	/**
	 * @param plugin - The {@link Civilization} plugin.
	 * @param player - The Offline Bukkit player.
	 * <br></br>
	 * Methods not available:
	 * 	{@link #getOrigin()},
	 * 	{@link #setViewing(boolean, Location)}
	 */
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
	 * @return the settlement the player is in. May be null.
	 */
	public Settlement getSettlement()
	{
		if(plugin.isSQL()) 
		{
			SQLQuery sq = new SQLQuery(plugin);
			String s = sq.getString("settlement", "PlayerData", player.getUniqueId());
			if(s != null) return new Settlement(plugin, s);
			else return null;
		} else {
			if(!configuration.getString("settlement").equals("null")) return new Settlement(plugin, configuration.getString("settlement"));
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
		if(plugin.isSQL()) 
		{
			SQLQuery sq = new SQLQuery(plugin);
			if(settlement != null) sq.set("settlement", "PlayerData", settlement.getName(), uuid);
			else sq.setNull("settlement", "PlayerData", uuid);
		} else {
			if(settlement != null) configuration.set("settlement", settlement.getName());
			else configuration.set("settlement", "null");
			reloadConfiguration();
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
					reloadConfiguration();
				} catch(IOException e) {
					e.printStackTrace();
				}
			}
		} else {
			try {
				PreparedStatement statement = sql.prepareStatement("SELECT uuid FROM PlayerData WHERE uuid = ?");
				statement.setString(1, player.getUniqueId().toString().replace("-", ""));
				ResultSet rs = statement.executeQuery();
				if(!rs.next())
				{
					PreparedStatement statement2 = sql.prepareStatement("INSERT INTO PlayerData (uuid, settlement) VALUES (?, ?)");
					statement2.setString(1, player.getUniqueId().toString().replace("-", ""));
					statement.executeUpdate();
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
