package com.SamB440.Civilization.utils;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import com.SamB440.Civilization.Civilization;
import com.SamB440.Civilization.API.data.Settlement;
import com.SamB440.Civilization.API.data.TechnologyType;

public class SQLQuery {
	
	Civilization plugin;
	
	public SQLQuery(Civilization plugin)
	{
		this.plugin = plugin;
		try {
			if(plugin.getSQL().isClosed()) plugin.openConnection();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public int getInteger(String column, String database, UUID uuid)
	{
		try {
			PreparedStatement statement = plugin.getSQL().prepareStatement("SELECT " + column + " FROM " + database + " WHERE uuid = ?");
			statement.setString(1, uuid.toString().replaceAll("-", ""));
			ResultSet rs = statement.executeQuery();
			if(rs.next())
			{
				return rs.getInt(column);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}
	
	public String getString(String column, String database, UUID uuid)
	{
		try {
			PreparedStatement statement = plugin.getSQL().prepareStatement("SELECT " + column + " FROM " + database + " WHERE uuid = ?");
			statement.setString(1, uuid.toString().replaceAll("-", ""));
			ResultSet rs = statement.executeQuery();
			if(rs.next())
			{
				return rs.getString(column);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public Integer getInteger(String column, String database, String rclass, UUID uuid)
	{
		try {
			PreparedStatement statement = plugin.getSQL().prepareStatement("SELECT " + column + " FROM " + database + " WHERE uuid = ? AND class = ?");
			statement.setString(1, uuid.toString().replaceAll("-", ""));
			statement.setString(2, rclass);
			ResultSet rs = statement.executeQuery();
			if(rs.next())
			{
				return rs.getInt(column);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public List<TechnologyType> getTech(Settlement settlement)
	{
		List<TechnologyType> tech = new ArrayList<TechnologyType>();
		try {
			PreparedStatement statement = plugin.getSQL().prepareStatement("SELECT tech FROM Tech WHERE settlement = ?");
			statement.setString(1, settlement.getName());
			
			ResultSet rs = statement.executeQuery();
			while(rs.next()) tech.add(TechnologyType.valueOf(rs.getString("tech")));
			
		} catch (SQLException e) {
			e.printStackTrace();
		} return tech;
	}
	
	public List<OfflinePlayer> getMembers(Settlement settlement)
	{
		List<OfflinePlayer> op = new ArrayList<OfflinePlayer>();
		try {
			PreparedStatement statement = plugin.getSQL().prepareStatement("SELECT uuid FROM PlayerData WHERE settlement = ?");
			statement.setString(1, settlement.getName());
			ResultSet rs = statement.executeQuery();
			while(rs.next())
			{
				StringBuilder builder = new StringBuilder(rs.getString("uuid"));
			    try {
			        builder.insert(20, "-");
			        builder.insert(16, "-");
			        builder.insert(12, "-");
			        builder.insert(8, "-");
			    } catch (StringIndexOutOfBoundsException e) {
			        e.printStackTrace();
			    } op.add(Bukkit.getOfflinePlayer(UUID.fromString(builder.toString())));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} return op;
	}
	
	public boolean settlementExists(Settlement settlement)
	{
		try {
			PreparedStatement statement = plugin.getSQL().prepareStatement("SELECT name FROM Settlements WHERE name = ?");
			statement.setString(1, settlement.getName());
			ResultSet rs = statement.executeQuery();
			while(rs.next())
			{
				if(rs.getString("name").equals(settlement.getName())) return true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} return false;
	}
	
	public double getDouble(String column, String database, UUID uuid)
	{
		try {
			PreparedStatement statement = plugin.getSQL().prepareStatement("SELECT " + column + " FROM " + database + " WHERE uuid = ?");
			statement.setString(1, uuid.toString().replaceAll("-", ""));
			
			ResultSet rs = statement.executeQuery();
			if(rs.next()) return rs.getDouble(column);
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0.0;
	}
	
	public void set(String column, String database, Object amount, UUID uuid)
	{
		try {
			PreparedStatement statement = plugin.getSQL().prepareStatement("UPDATE " + database + " SET " + column + " = ? WHERE uuid = ?");
			Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
				try {
					statement.setObject(1, amount);
					statement.setString(2, uuid.toString().replaceAll("-", ""));
					statement.executeUpdate();
				} catch (SQLException e) {
					e.printStackTrace();
				}	
			});
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void set(String column, String database, String value, UUID uuid)
	{
		try {
			PreparedStatement statement = plugin.getSQL().prepareStatement("UPDATE " + database + " SET " + column + " = ? WHERE uuid = ?");
			Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
				try {
					statement.setString(1, value);
					statement.setString(2, uuid.toString().replaceAll("-", ""));
					statement.executeUpdate();
				} catch (SQLException e) {
					e.printStackTrace();
				}	
			});
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void set(String column, String database, Integer value, String rclass, UUID uuid)
	{
		try {
			PreparedStatement statement = plugin.getSQL().prepareStatement("UPDATE " + database + " SET " + column + " = ? WHERE uuid = ? AND class = ?");
			Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
				try {
					statement.setInt(1, value);
					statement.setString(2, uuid.toString().replaceAll("-", ""));
					statement.setString(3, rclass);
					statement.executeUpdate();
				} catch (SQLException e) {
					e.printStackTrace();
				}	
			});
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void setNull(String column, String database, UUID uuid)
	{
		try {
			PreparedStatement statement = plugin.getSQL().prepareStatement("UPDATE " + database + " SET " + column + " = ? WHERE uuid = ?");
			Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
				try {
					statement.setNull(1, Types.OTHER);
					statement.setString(2, uuid.toString().replaceAll("-", ""));
					statement.executeUpdate();
				} catch (SQLException e) {
					e.printStackTrace();
				}	
			});
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
