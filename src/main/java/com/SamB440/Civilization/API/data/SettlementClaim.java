package com.SamB440.Civilization.API.data;

import java.io.File;
import java.io.IOException;

import org.bukkit.Chunk;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import com.SamB440.Civilization.Civilization;

public class SettlementClaim {
	
	Civilization plugin;
	Chunk chunk;
	Settlement settlement;
	File claim;
	YamlConfiguration yaml;
	
	public SettlementClaim(Civilization plugin, Chunk chunk, Settlement settlement)
	{
		this.plugin = plugin;
		this.chunk = chunk;
		this.settlement = settlement;
		this.claim = new File(plugin.getDataFolder() + "/claims/" + chunk.getX() + "," + chunk.getZ() + ".claim");
		this.yaml = YamlConfiguration.loadConfiguration(claim);
	}
	
	public SettlementClaim(Civilization plugin, Chunk chunk)
	{
		this.plugin = plugin;
		this.chunk = chunk;
		this.claim = new File(plugin.getDataFolder() + "/claims/" + chunk.getX() + "," + chunk.getZ() + ".claim");
		this.yaml = YamlConfiguration.loadConfiguration(claim);
	}
	
	/**
	 * @return true if claim was successful, false if claim already existed
	 */
	public boolean claim()
	{
		if(!claim.exists())
		{
			try {
				claim.createNewFile();
				yaml.addDefault("Owner", settlement.getName());
				try {
					yaml.options().copyDefaults(true);
					yaml.save(claim);
					yaml.load(claim);
				} catch (InvalidConfigurationException e) {
					e.printStackTrace();
				} return true;
			} catch (IOException e) {
				e.printStackTrace();
			}
		} return false;
	}
	
	public boolean isClaimed()
	{
		return claim.exists();
	}
	
	public void remove()
	{
		claim.delete();
	}
	
	/**
	 * @return the current owner of this land. May be null if not claimed.
	 */
	public Settlement getOwner()
	{
		if(claim.exists()) return new Settlement(plugin, yaml.getString("Owner"));
		else return null;
	}
	
	public Chunk getChunk()
	{
		return chunk;
	}
	
	public Settlement getSettlement()
	{
		return settlement;
	}
}
