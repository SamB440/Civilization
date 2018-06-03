package com.SamB440.Civilization.API.data;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import com.SamB440.Civilization.Civilization;
import com.SamB440.Civilization.utils.SQLQuery;
import com.SamB440.Civilization.utils.TechTree;

public class Settlement {
	
	Civilization plugin;
	String name;
	Player king;
	File file;
	FileConfiguration yaml;
	Connection sql;
	
	public Settlement(Civilization plugin, String name, Player king)
	{
		this.plugin = plugin;
		this.name = name;
		this.king = king;
		this.file = new File(plugin.getDataFolder() + "/settlements/" + name + ".settlement");
		this.yaml = YamlConfiguration.loadConfiguration(file);
		this.sql = plugin.getSql();
	}
	
	public Settlement(Civilization plugin, String name)
	{
		this.plugin = plugin;
		this.name = name;
		this.file = new File(plugin.getDataFolder() + "/settlements/" + name + ".settlement");
		this.yaml = YamlConfiguration.loadConfiguration(file);
		this.sql = plugin.getSql();
	}

	public String getName()
	{
		return name;
	}
	
	public boolean exists()
	{
		if(plugin.isSQL())
		{
			SQLQuery sq = new SQLQuery(plugin);
			return sq.settlementExists(this);
		} else return file.exists();
	}
	
	public void create()
	{
		if(plugin.isSQL())
		{
			try {
				PreparedStatement statement = sql.prepareStatement("INSERT INTO Settlements (king, name, level) VALUES (?, ?, ?)");
				statement.setString(1, king.getUniqueId().toString().replace("-", ""));
				statement.setString(2, name);
				statement.setInt(3, 1);
				statement.executeUpdate();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} else {
			try {
				file.createNewFile();
				yaml.options().copyDefaults(true);
				yaml.addDefault("king", king.getUniqueId().toString());
				yaml.addDefault("level", 1);
				yaml.addDefault("science", 100);
				yaml.addDefault("tech", Arrays.asList(""));
				yaml.addDefault("members", Arrays.asList(king.getUniqueId().toString()));
				yaml.save(file);
				yaml.load(file);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InvalidConfigurationException e) {
				e.printStackTrace();
			}
		}
		
		CivPlayer civ = new CivPlayer(plugin, king);
		civ.setSettlement(this);
		
		TechTree tech = new TechTree(plugin);
		tech.grantTech(Bukkit.getAdvancement(new NamespacedKey(plugin, "tech/root")), king);
		tech.showInfo("root", king);
	}
	
	public void delete()
	{
		for(OfflinePlayer players : getMembers())
		{
			if(players.isOnline())
			{
				Player player = Bukkit.getPlayer(players.getUniqueId());
				CivPlayer civ = new CivPlayer(plugin, player);
				civ.setSettlement(null);
				player.sendMessage(ChatColor.RED + "Your settlement has been disbanded!");
				player.playSound(player.getLocation(), Sound.ENTITY_ENDERDRAGON_DEATH, 1, 1);
			} else {
				CivPlayer civ = new CivPlayer(plugin, players);
				civ.setSettlement(null);
			}
		}
		
		if(plugin.isSQL())
		{
			try {
				PreparedStatement statement = sql.prepareStatement("DELETE FROM Settlements WHERE king = ?");
				statement.setString(1, king.getUniqueId().toString().replace("-", ""));
				statement.executeUpdate();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} else file.delete();
	}
	
	public void exile(OfflinePlayer player)
	{
		if(plugin.isSQL())
		{
			SQLQuery sq = new SQLQuery(plugin);
			sq.setNull("settlement", "PlayerData", player.getUniqueId());
		} else {
			List<OfflinePlayer> members = getMembers();
			members.remove(player);
			yaml.set("members", members);
			reloadConfiguration();
		}
	}
	
	public void exile(Player player)
	{
		if(plugin.isSQL())
		{
			SQLQuery sq = new SQLQuery(plugin);
			sq.setNull("settlement", "PlayerData", player.getUniqueId());
		} else file.delete();
	}
	
	public int getScience()
	{
		if(plugin.isSQL())
		{
			try {
				PreparedStatement statement = sql.prepareStatement("SELECT science FROM Settlements WHERE name = ?");
				statement.setString(1, name);
				
				ResultSet rs = statement.executeQuery();
				if(rs.next()) return rs.getInt("science");
			} catch (SQLException e) {
				e.printStackTrace();
			} return 0;
		} else return yaml.getInt("science");
	}
	
	public OfflinePlayer getOwner()
	{
		if(plugin.isSQL())
		{
			try {
				PreparedStatement statement = sql.prepareStatement("SELECT king FROM Settlements WHERE name = ?");
				statement.setString(1, name);
				
				ResultSet rs = statement.executeQuery();
				if(rs.next())
				{
					StringBuilder builder = new StringBuilder(rs.getString("king"));
				    try {
				        builder.insert(20, "-");
				        builder.insert(16, "-");
				        builder.insert(12, "-");
				        builder.insert(8, "-");
				    } catch (StringIndexOutOfBoundsException e) {
				        e.printStackTrace();
				    } return Bukkit.getOfflinePlayer(UUID.fromString(builder.toString()));
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} else return Bukkit.getOfflinePlayer(UUID.fromString(yaml.getString("king")));
		
		return null;
	}
	
	public boolean isKing(Player player)
	{
		if(plugin.isSQL())
		{
			try {
				PreparedStatement statement = sql.prepareStatement("SELECT king FROM Settlements WHERE name = ?");
				statement.setString(1, name);
				
				ResultSet rs = statement.executeQuery();
				if(rs.next())
				{
					StringBuilder builder = new StringBuilder(rs.getString("king"));
				    try {
				        builder.insert(20, "-");
				        builder.insert(16, "-");
				        builder.insert(12, "-");
				        builder.insert(8, "-");
				    } catch (StringIndexOutOfBoundsException e) {
				        e.printStackTrace();
				    } if(builder.toString().equals(player.getUniqueId().toString())) return true;
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} else return yaml.getString("king").equals(player.getUniqueId().toString());
		
		return false;
	}
	
	
	/**
	 * @return players in settlement - includes king.
	 */
	public List<OfflinePlayer> getMembers()
	{
		if(plugin.isSQL())
		{
			SQLQuery sq = new SQLQuery(plugin);
			return sq.getMembers(this);
		} else {
			List<OfflinePlayer> members = new ArrayList<OfflinePlayer>();
			for(String player : yaml.getStringList("members"))
			{
				members.add(Bukkit.getOfflinePlayer(UUID.fromString(player)));
			}
			
			return members;
		}
	}
	
	public List<TechnologyType> getTech()
	{
		if(plugin.isSQL())
		{
			SQLQuery sq = new SQLQuery(plugin);
			return sq.getTech(this);
		} else {
			List<TechnologyType> types = new ArrayList<TechnologyType>();
			for(String type : yaml.getStringList("tech"))
			{
				types.add(TechnologyType.valueOf(type));
			}
			
			return types;
		}
	}
	
	/**
	 * @deprecated this is outdated and does not show granted technology to online players.
	 * @see {@link TechTree}
	 * @param tech - technology type
	 */
	@Deprecated
	public void addTech(TechnologyType tech)
	{
		if(plugin.isSQL())
		{
			try {
				PreparedStatement statement = sql.prepareStatement("INSERT INTO Tech (settlement, tech) VALUES (?, ?)");
				statement.setString(1, name);
				statement.setString(2, tech.toString());
				statement.executeUpdate();
				
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} else {
			List<TechnologyType> types = getTech();
			types.add(tech);
			
			yaml.set("tech", types);
			reloadConfiguration();
		}
	}
	
	public boolean hasTech(TechnologyType tech)
	{
		return getTech().contains(tech);
	}
	
	public int getLevel()
	{
		if(plugin.isSQL())
		{
			try {
				PreparedStatement statement = sql.prepareStatement("SELECT level FROM Settlements WHERE name = ?");
				statement.setString(1, name);
				
				ResultSet rs = statement.executeQuery();
				if(rs.next()) return rs.getInt("level");
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} else return yaml.getInt("level");
		
		return 0;
	}
	
	public List<SettlementClaim> getClaims(World world)
	{
		List<SettlementClaim> claims = new ArrayList<SettlementClaim>();
		for(File file : new File(plugin.getDataFolder() + "/claims/").listFiles())
		{
			FileConfiguration owner = YamlConfiguration.loadConfiguration(file);
			String[] split = file.getName().split(",");
			int x = Integer.valueOf(split[0]);
			int z = Integer.valueOf(split[1]);
			if(owner.getString("Owner").equals(getName())) claims.add(new SettlementClaim(plugin, world.getChunkAt(x, z), this));
		} return claims;
	}
	
	public void setLevel(int level)
	{
		if(plugin.isSQL())
		{
			try {
				PreparedStatement statement = sql.prepareStatement("UPDATE Settlements SET level = ?");
				statement.setInt(1, level);
				statement.executeUpdate();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} else yaml.set("level", level);
	}
	
	private void reloadConfiguration()
	{
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
