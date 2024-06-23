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

public class ServerStats extends JavaPlugin implements CommandExecutor {
    private BossBar bossBar;
    private boolean isBossBarVisible;

    @Override
    public void onEnable() {
        bossBar = Bukkit.createBossBar("Server Stats", BarColor.PINK, BarStyle.SOLID);
        bossBar.setVisible(true);
        isBossBarVisible = false;

        this.getCommand("togglestats").setExecutor(this);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (isBossBarVisible) {
                    updateBossBar();
                }
            }
        }.runTaskTimer(this, 0L, 20L);
    }

    @Override
    public void onDisable() {
        bossBar.removeAll();
    }

    private void updateBossBar() {
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

                if (isBossBarVisible) {
                    bossBar.removeAll();
                    bossBar.setVisible(false);
                    player.sendMessage("Server statistics hidden.");
                } else {
                    for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                        bossBar.addPlayer(onlinePlayer);
                    }
                    bossBar.setVisible(true);
                    player.sendMessage("Server statistics shown.");
                }

                isBossBarVisible = !isBossBarVisible;
                return true;
            }
            sender.sendMessage("This command can only be executed by a Player.");
            return true;
        }
        return false;
    }
}