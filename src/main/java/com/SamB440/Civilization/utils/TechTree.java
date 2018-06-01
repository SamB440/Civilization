package com.SamB440.Civilization.utils;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.entity.Player;

import com.SamB440.Civilization.Civilization;

public class TechTree {
	
	Civilization plugin;
	
	public TechTree(Civilization plugin)
	{
		this.plugin = plugin;
	}
	
	public void grantAdvancement(Advancement advancement, Player player) 
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
	
	public void showInfo(String name, Player player)
	{
		AdvancementProgress progress = player.getAdvancementProgress(Bukkit.getAdvancement(new NamespacedKey(plugin, "info/" + name)));
		progress.awardCriteria("impossible");
		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> progress.revokeCriteria("impossible"), 60);
	}
	
	public boolean hasTech(String name, Player player)
	{
		AdvancementProgress progress = player.getAdvancementProgress(Bukkit.getAdvancement(new NamespacedKey(plugin, "tech/" + name)));
		return progress.isDone();
	}
}
