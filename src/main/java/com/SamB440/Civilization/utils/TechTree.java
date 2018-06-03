package com.SamB440.Civilization.utils;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import com.SamB440.Civilization.Civilization;
import com.SamB440.Civilization.API.data.Settlement;
import com.SamB440.Civilization.API.data.TechnologyType;

/**
 * Utils class to send toast information and handle technology.
 * @author SamB440
 */
public class TechTree {
	
	private Civilization plugin;
	private Connection sql;
	
	public TechTree(Civilization plugin)
	{
		this.plugin = plugin;
		this.sql = plugin.getSql();
	}
	
	/**
	 * Grant technology to a player.
	 * @param name - advancement name
	 * @param player - Bukkit player
	 */
	public void grantTech(String name, Player player) 
	{
		AdvancementProgress progress = player.getAdvancementProgress(Bukkit.getAdvancement(new NamespacedKey(plugin, "tech/" + name)));
		if(!progress.isDone())
		{
			for(String criteria : progress.getRemainingCriteria())
			{
				progress.awardCriteria(criteria);
			}
		}
	}
	
	/**
	 * Grant technology to a player.
	 * @param advancement - Bukkit advancement
	 * @param player - Bukkit player
	 */
	public void grantTech(Advancement advancement, Player player) 
	{
		AdvancementProgress progress = player.getAdvancementProgress(advancement);
		if(!progress.isDone())
		{
			for(String criteria : progress.getRemainingCriteria())
			{
				progress.awardCriteria(criteria);
			}
		}
	}
	
	public void grantTech(Settlement settlement, TechnologyType tech)
	{
		if(plugin.isSQL())
		{
			try {
				PreparedStatement statement = sql.prepareStatement("INSERT INTO Tech (settlement, tech) VALUES (?, ?)");
				statement.setString(1, settlement.getName());
				statement.setString(2, tech.toString());
				statement.executeUpdate();
				
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} else {
			List<TechnologyType> types = settlement.getTech();
			types.add(tech);
			
			File save = new File(plugin.getDataFolder() + "/settlements/" + settlement.getName() + ".settlement");
			FileConfiguration yaml = YamlConfiguration.loadConfiguration(save);
			yaml.set("tech", types);
			reloadConfiguration(save);
		}
		
		for(OfflinePlayer op : settlement.getMembers())
		{
			if(op.isOnline())
			{
				Player player = Bukkit.getPlayer(op.getUniqueId());
				grantTech(tech.getName(), player);
			}
		}
	}
	
	/**
	 * Show toast information to a player.
	 * @param name - advancement name
	 * @param player - Bukkit player
	 */
	public void showInfo(String name, Player player)
	{
		AdvancementProgress progress = player.getAdvancementProgress(Bukkit.getAdvancement(new NamespacedKey(plugin, "info/" + name)));
		progress.awardCriteria("impossible");
		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> progress.revokeCriteria("impossible"), 60);
	}
	
	public void showInfo(String name, Settlement settlement)
	{
		for(OfflinePlayer op : settlement.getMembers())
		{
			if(op.isOnline())
			{
				Player player = Bukkit.getPlayer(op.getUniqueId());
				AdvancementProgress progress = player.getAdvancementProgress(Bukkit.getAdvancement(new NamespacedKey(plugin, "info/" + name)));
				progress.awardCriteria("impossible");
				Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> progress.revokeCriteria("impossible"), 60);
			}
		}
	}
	
	/**
	 * Check if a player has technology.
	 * @param name - advancement name
	 * @param player - Bukkit player
	 * @return true if player has researched technology
	 */
	public boolean hasTech(String name, Player player)
	{
		AdvancementProgress progress = player.getAdvancementProgress(Bukkit.getAdvancement(new NamespacedKey(plugin, "tech/" + name)));
		return progress.isDone();
	}
	
	private void reloadConfiguration(File file)
	{
		FileConfiguration yaml = YamlConfiguration.loadConfiguration(file);
		try {
			yaml.save(file);
			yaml.load(file);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InvalidConfigurationException e) {
			e.printStackTrace();
		}
	}
}
