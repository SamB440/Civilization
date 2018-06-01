package com.SamB440.Civilization.API.data;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.entity.Player;

import com.SamB440.Civilization.Civilization;
import com.SamB440.Civilization.utils.TechTree;

public class Technology {
	
	Civilization plugin;
	Settlement settlement;
	Advancement advancement;
	Player researcher;
	int turns = 0;
	int completion, time;
	List<Integer> cancel = new ArrayList<Integer>();
	
	public Technology(Civilization plugin, Settlement settlement, Advancement advancement, Player researcher, int completion, int time)
	{
		this.plugin = plugin;
		this.settlement = settlement;
		this.advancement = advancement;
		this.researcher = researcher;
		this.completion = completion;
		this.time = time;
	}
	
	public void startResearch()
	{
		AdvancementProgress progress = researcher.getAdvancementProgress(advancement);
		if(progress.isDone())
		{
			researcher.sendMessage(ChatColor.RED + "This technology has already been researched!");
			return;
		}
		
		if(progress.getAwardedCriteria().size() >= 1) 
		{
			if(turns <= turns + 2) turns = turns + 2;
			researcher.sendMessage("true");
		}
		researcher.sendMessage("1");
		for(OfflinePlayer op : settlement.getMembers())
		{
			if(op.isOnline())
			{
				researcher.sendMessage("2");
				Player player = Bukkit.getPlayer(op.getUniqueId());
				TechTree tech = new TechTree(plugin);
				tech.showInfo("research", player);
			}
		}
		
		final int original = progress.getAwardedCriteria().size();
		final int completion = this.completion;
		
		cancel.add(Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
			
			if(original != researcher.getAdvancementProgress(advancement).getAwardedCriteria().size())
			{
				cancel();
				redo();
			} else {
				if(turns == completion)
				{
					researcher.sendMessage("completed");
					TechTree tech = new TechTree(plugin);
					tech.grantAdvancement(advancement, researcher);
					cancel();
				} else turns++;
				researcher.sendMessage(turns + " turns");
			}
		}, time, time));
	}
	
	public void cancel()
	{
		Bukkit.getScheduler().cancelTask(cancel.get(0));
		researcher.sendMessage("cancelled task");
	}
	
	private void redo()
	{
		researcher.sendMessage("redo");
		cancel.remove(cancel.get(0));
		startResearch();
	}
}
