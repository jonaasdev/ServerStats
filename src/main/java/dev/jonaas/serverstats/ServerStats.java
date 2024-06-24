package dev.jonaas.serverstats;

import org.bukkit.Bukkit;
import org.bukkit.boss.BossBar;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;

public class ServerStats extends JavaPlugin implements CommandExecutor {
    private Map<Player, BossBar> playerBossBars = new HashMap<>();
    private Map<Player, BarColor> playerColors = new HashMap<>();
    private Map<Player, BarStyle> playerStyles = new HashMap<>();

    @Override
    public void onEnable() {
        this.getCommand("togglestats").setExecutor(this);

        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (playerBossBars.containsKey(player)) {
                        updateBossBar(player);
                    }
                }
            }
        }.runTaskTimer(this, 0L, 20L);
    }

    @Override
    public void onDisable() {
        for (BossBar bossBar : playerBossBars.values()) {
            bossBar.removeAll();
        }
        playerBossBars.clear();
    }

    private void updateBossBar(Player player) {
        BossBar bossBar = playerBossBars.get(player);
        int onlinePlayers = Bukkit.getOnlinePlayers().size();
        long freeMemory = Runtime.getRuntime().freeMemory() / (1024 * 1024);
        long maxMemory = Runtime.getRuntime().maxMemory() / (1024 * 1024);
        double tps = getTPS();

        String title = String.format("Memory used: %d MB of %d MB - TPS: %.2f", freeMemory, maxMemory, tps);
        bossBar.setTitle(title);

        double progress = (double) freeMemory / maxMemory;
        bossBar.setProgress(Math.min(1.0, Math.max(0.0, progress)));
    }

    private double getTPS() {
        try {
            Object minecraftServer = Bukkit.getServer().getClass().getMethod("getServer").invoke(Bukkit.getServer());
            double[] recentTps = (double[]) minecraftServer.getClass().getField("recentTps").get(minecraftServer);
            return recentTps[0];
        } catch (Exception e) {
            e.printStackTrace();
            return 0.0;
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("togglestats")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;

                if (args.length == 0) {
                    toggleStats(player);
                } else if (args.length == 2 && args[0].equalsIgnoreCase("color")) {
                    changeColor(player, args[1]);
                } else if (args.length == 2 && args[0].equalsIgnoreCase("style")) {
                    changeStyle(player, args[1]);
                } else {
                    sender.sendMessage("Usage: /togglestats [string] [value]");
                }
                return true;
            }
            sender.sendMessage("This command can only be executed by a Player.");
            return true;
        }
        return false;
    }

    private void toggleStats(Player player ) {
        if (playerBossBars.containsKey(player)) {
            BossBar bossBar = playerBossBars.get(player);
            bossBar.removeAll();
            playerBossBars.remove(player);
            player.sendMessage("Server statistics are now hidden.");
        } else {
            BarColor color = playerColors.getOrDefault(player, BarColor.PURPLE);
            BarStyle style = playerStyles.getOrDefault(player, BarStyle.SOLID);
            BossBar bossBar = Bukkit.createBossBar("Server stats", color, style);
            bossBar.addPlayer(player);
            bossBar.setVisible(true);
            playerBossBars.put(player, bossBar);
            player.sendMessage("Server statistics are now shown.");
        }
    }

    private void changeColor(Player player, String colorStr) {
        try {
            BarColor color = BarColor.valueOf(colorStr.toUpperCase());
            playerColors.put(player, color);
            if (playerBossBars.containsKey(player)) {
                BossBar bossBar = playerBossBars.get(player);
                bossBar.setColor(color);
            }
            player.sendMessage("Bossbar color was changed to " + colorStr.toUpperCase() + ".");
        } catch (IllegalArgumentException e) {
            player.sendMessage("Invalid color.");
        }
    }

    private void changeStyle(Player player, String styleStr) {
        try {
            BarStyle style = BarStyle.valueOf(styleStr.toUpperCase());
            playerStyles.put(player, style);
            if (playerBossBars.containsKey(player)) {
                BossBar bossBar = playerBossBars.get(player);
                bossBar.setStyle(style);
            }
            player.sendMessage("Bossbar style was changed to " + styleStr.toUpperCase() + ".");
        } catch (IllegalArgumentException e) {
            player.sendMessage("Invalid style.");
        }
    }
}