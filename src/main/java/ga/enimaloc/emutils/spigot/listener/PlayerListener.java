package ga.enimaloc.emutils.spigot.listener;

import ga.enimaloc.emutils.spigot.EmUtils;
import ga.enimaloc.emutils.spigot.entity.EmPlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class PlayerListener implements Listener {

    private EmUtils main;

    public PlayerListener(EmUtils main) {
        this.main = main;
    }

    @EventHandler
    public void onPlayerConnect(PlayerLoginEvent event) {
        Player player = event.getPlayer();
        EmPlayer emPlayer = EmPlayer.get(player);
        System.out.println("emPlayer = " + emPlayer.toString());
        System.out.println("player = " + player);
        System.out.println("player.getAddress() = " + player.getAddress());
        System.out.println("player.getDisplayName() = " + player.getDisplayName());
        System.out.println("player.getAllowFlight() = " + player.getAllowFlight());
        System.out.println("player.getClientViewDistance() = " + player.getClientViewDistance());
        System.out.println("player.getCompassTarget() = " + player.getCompassTarget());
        System.out.println("player.getExhaustion() = " + player.getExhaustion());
        System.out.println("player.getExp() = " + player.getExp());
        System.out.println("player.getFlySpeed() = " + player.getFlySpeed());
        System.out.println("player.getFoodLevel() = " + player.getFoodLevel());
        System.out.println("player.getHealthScale() = " + player.getHealthScale());
        System.out.println("player.getLevel() = " + player.getLevel());
        System.out.println("player.getLocale() = " + player.getLocale());
        System.out.println("player.getPlayerListFooter() = " + player.getPlayerListFooter());
        System.out.println("player.getPlayerListHeader() = " + player.getPlayerListHeader());
        System.out.println("player.getPlayerListName() = " + player.getPlayerListName());
        System.out.println("player.getPlayerTime() = " + player.getPlayerTime());
        System.out.println("player.getPlayerTimeOffset() = " + player.getPlayerTimeOffset());
        System.out.println("player.getPlayerWeather() = " + player.getPlayerWeather());
        System.out.println("player.getSaturation() = " + player.getSaturation());
        System.out.println("player.getTotalExperience() = " + player.getTotalExperience());
        System.out.println("player.getWalkSpeed() = " + player.getWalkSpeed());
        System.out.println("player.isFlying() = " + player.isFlying());
        System.out.println("player.isHealthScaled() = " + player.isHealthScaled());
        System.out.println("player.isPlayerTimeRelative() = " + player.isPlayerTimeRelative());
        System.out.println("player.isSleepingIgnored() = " + player.isSleepingIgnored());
        System.out.println("player.isSneaking() = " + player.isSneaking());
        System.out.println("player.isSprinting() = " + player.isSprinting());
    }

    @EventHandler
    public void onPlayerDisconnect(PlayerQuitEvent event) {}

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        EmPlayer.get(event.getPlayer()).incrementMinedBlock(event.getBlock().getType());
    }

}
