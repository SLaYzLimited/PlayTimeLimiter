package org.slayz.playtimelimiter;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.*;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class PlayTimeLimiter extends JavaPlugin implements Listener {

    private final Map<DayOfWeek, Integer> maxPlaytime = new HashMap<>();
    private final Map<UUID, Integer> todayPlayers = new HashMap<>();

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        loadConfiguration();
        maxPlaytime.put(DayOfWeek.MONDAY, this.getConfig().getInt("Limit.Monday"));
        maxPlaytime.put(DayOfWeek.TUESDAY, this.getConfig().getInt("Limit.TUESDAY"));
        maxPlaytime.put(DayOfWeek.WEDNESDAY, this.getConfig().getInt("Limit.WEDNESDAY"));
        maxPlaytime.put(DayOfWeek.THURSDAY, this.getConfig().getInt("Limit.THURSDAY"));
        maxPlaytime.put(DayOfWeek.FRIDAY, this.getConfig().getInt("Limit.FRIDAY"));
        maxPlaytime.put(DayOfWeek.SATURDAY, this.getConfig().getInt("Limit.SATURDAY"));
        maxPlaytime.put(DayOfWeek.SUNDAY, this.getConfig().getInt("Limit.SUNDAY"));
        new BukkitRunnable() {
            @Override
            public void run() {
                todayPlayers.clear(); // Clear today players list every day
            }
        }.runTaskTimer(this, 0L, 1728000L); // Run every 24 hours (20 ticks per second * 60 seconds * 60 minutes * 24 hours)
    }

    @Override
    public void onDisable() {
        for (Player player : getServer().getOnlinePlayers()) {
            player.kickPlayer("Server is restarting!");
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if(todayPlayers.containsKey(player.getUniqueId())) {
            checkPlaytime(player);
        } else {
            todayPlayers.put(player.getUniqueId(), 0); // Add player to today players list
            new BukkitRunnable() {
                @Override
                public void run() {
                    checkPlaytime(player);
                }
            }.runTaskTimer(this, 0L, 1200L);
        }
    }

    private void checkPlaytime(Player player) {
        LocalDateTime now = LocalDateTime.now();
        int maxTime = maxPlaytime.get(now.getDayOfWeek());
        int played = todayPlayers.get(player.getUniqueId());
        if (played >= maxTime) {
            player.kickPlayer(this.getConfig().getString("Message.KICK"));
        } else {
            todayPlayers.put(player.getUniqueId(), played + 1);
        }
    }

    private void loadConfiguration() {
        this.getConfig().addDefault("Limit.Monday", 30);
        this.getConfig().addDefault("Limit.TUESDAY", 30);
        this.getConfig().addDefault("Limit.WEDNESDAY", 30);
        this.getConfig().addDefault("Limit.THURSDAY", 30);
        this.getConfig().addDefault("Limit.FRIDAY", 30);
        this.getConfig().addDefault("Limit.SATURDAY", 30);
        this.getConfig().addDefault("Limit.SUNDAY", 30);
        this.getConfig().addDefault("Message.KICK", "You have reached the maximum playtime for today!");
        this.getConfig().options().copyDefaults(true);
        this.saveConfig();

    }
}



