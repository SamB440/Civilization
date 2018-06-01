package com.SamB440.Civilization.API.data;

import java.io.File;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
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

import net.md_5.bungee.api.ChatColor;

public class Settlement {
	
	Civilization plugin;
	String name;
	Player king;
	File file;
	FileConfiguration yaml;
	
	public Settlement(Civilization plugin, String name, Player king)
	{
		this.plugin = plugin;
		this.name = name;
		this.king = king;
		this.file = new File(plugin.getDataFolder() + "/settlements/" + name + ".settlement");
		this.yaml = YamlConfiguration.loadConfiguration(file);
	}
	
	public Settlement(Civilization plugin, String name)
	{
		this.plugin = plugin;
		this.name = name;
		this.file = new File(plugin.getDataFolder() + "/settlements/" + name + ".settlement");
		this.yaml = YamlConfiguration.loadConfiguration(file);
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
				PreparedStatement statement = plugin.getSQL().prepareStatement("INSERT INTO Settlements (king, name, level) VALUES (?, ?, ?)");
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
				yaml.addDefault("king", king.getUniqueId());
				yaml.addDefault("level", 1);
				yaml.addDefault("science", 100);
				yaml.save(file);
				yaml.load(file);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InvalidConfigurationException e) {
				e.printStackTrace();
			}
		}
		
		TechTree tech = new TechTree(plugin);
		tech.grantAdvancement(Bukkit.getAdvancement(new NamespacedKey(plugin, "tech/root")), king);
		tech.showInfo("root", king);
	}
	
	public void delete()
	{
		for(OfflinePlayer players : getMembers())
		{
			if(players.isOnline())
			{
				Player player = Bukkit.getPlayer(players.getUniqueId());
				CivPlayer rp = new CivPlayer(plugin, player);
				rp.setSettlement(null);
				player.sendMessage(ChatColor.RED + "Your settlement has been disbanded!");
				player.playSound(player.getLocation(), Sound.ENTITY_ENDERDRAGON_DEATH, 1, 1);
			} else {
				CivPlayer rp = new CivPlayer(plugin, players);
				rp.setSettlement(null);
			}
		}
		
		if(plugin.isSQL())
		{
			try {
				PreparedStatement statement = plugin.getSQL().prepareStatement("DELETE FROM Settlements WHERE king = ?");
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
		} else file.delete();
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
				PreparedStatement statement = plugin.getSQL().prepareStatement("SELECT science FROM Settlements WHERE name = ?");
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
				PreparedStatement statement = plugin.getSQL().prepareStatement("SELECT king FROM Settlements WHERE name = ?");
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
		try {
			PreparedStatement statement = plugin.getSQL().prepareStatement("SELECT king FROM Settlements WHERE name = ?");
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
		} return false;
	}
	
	public List<OfflinePlayer> getMembers()
	{
		SQLQuery sq = new SQLQuery(plugin);
		return sq.getMembers(this);
	}
	
	public List<String> getTech()
	{
		SQLQuery sq = new SQLQuery(plugin);
		return sq.getTech(this);
	}
	
	public boolean hasTech(String tech)
	{
		return getTech().contains(tech);
	}
	
	public int getLevel()
	{
		try {
			PreparedStatement statement = plugin.getSQL().prepareStatement("SELECT level FROM Settlements WHERE name = ?");
			statement.setString(1, name);
			
			ResultSet rs = statement.executeQuery();
			if(rs.next()) return rs.getInt("level");
		} catch (SQLException e) {
			e.printStackTrace();
		} return 0;
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
		try {
			PreparedStatement statement = plugin.getSQL().prepareStatement("UPDATE Settlements SET level = ?");
			statement.setInt(1, level);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
