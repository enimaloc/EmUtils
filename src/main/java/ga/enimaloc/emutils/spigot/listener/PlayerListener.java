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
    }

    @EventHandler
    public void onPlayerDisconnect(PlayerQuitEvent event) {}

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        EmPlayer.get(event.getPlayer()).incrementMinedBlock(event.getBlock().getType());
    }

}
