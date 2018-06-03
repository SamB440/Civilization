package com.SamB440.Civilization.API.data;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

import com.SamB440.Civilization.Civilization;
import com.SamB440.Civilization.utils.TechTree;

public class Technology {
	
	Civilization plugin;
	Settlement settlement;
	String name;
	Player researcher;
	int turns = 0;
	int completion, time;
	TechnologyType type;
	
	List<Integer> cancel = new ArrayList<Integer>();
	
	public Technology(Civilization plugin, Settlement settlement, Player researcher, int completion, int time, TechnologyType type)
	{
		this.plugin = plugin;
		this.settlement = settlement;
		this.name = type.getName();
		this.researcher = researcher;
		this.completion = completion;
		this.time = time;
		this.type = type;
	}
	
	public void startResearch()
	{
		Advancement advancement = Bukkit.getAdvancement(new NamespacedKey(plugin, "tech/" + name));
		AdvancementProgress progress = researcher.getAdvancementProgress(advancement);
		
		if(progress.isDone())
		{
			researcher.sendMessage(ChatColor.RED + "This technology has already been researched!");
			return;
		}
		
		BossBar bossbar = Bukkit.createBossBar(ChatColor.AQUA + "Currently Researching: " + name, BarColor.BLUE, BarStyle.SEGMENTED_20);
		bossbar.setProgress(0);
		
		for(OfflinePlayer op : settlement.getMembers())
		{
			if(op.isOnline())
			{
				Player player = Bukkit.getPlayer(op.getUniqueId());
				bossbar.addPlayer(player);
				bossbar.setVisible(true);
			}
		}
		
		if(progress.getAwardedCriteria().size() >= 1) 
		{
			double calculate = (double) completion / 2.0;
			researcher.sendMessage(calculate + "");
			completion = (int) Math.rint(calculate);
			researcher.sendMessage(completion + "");
			researcher.sendMessage("true");
		}
		
		TechTree tech = new TechTree(plugin);
		tech.showInfo("research", settlement);
		
		final int original = progress.getAwardedCriteria().size();
		final int completion = this.completion;
		final double ui = (double) (100 / completion) / 100;
		
		researcher.sendMessage(ui + "");
		researcher.sendMessage(time + "");
		
		cancel.add(Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
			researcher.sendMessage("1");
			if(original != researcher.getAdvancementProgress(advancement).getAwardedCriteria().size())
			{
				cancel();
				redo();
			} else {
				turns++;
				if(turns == completion)
				{
					researcher.sendMessage("completed");
					tech.grantTech(settlement, type);
					bossbar.removeAll();
					bossbar.setVisible(false);
					cancel();
				}
				
				bossbar.setProgress(bossbar.getProgress() + ui);
				researcher.sendMessage(bossbar.getProgress() + "");
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
