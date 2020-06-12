package ga.enimaloc.emutils.spigot.listener;

import ga.enimaloc.emutils.spigot.EmUtils;
import ga.enimaloc.emutils.spigot.entity.EmPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerListener implements Listener {

    private final EmUtils emUtils;
    private Map<UUID, BukkitRunnable> clear = new HashMap<>();

    public PlayerListener(EmUtils emUtils) {
        this.emUtils = emUtils;
    }

    /**
     * Trigger when player login to the server
     *
     * @param event {@link PlayerLoginEvent} Event object of the event
     */
    @EventHandler
    public void onPlayerConnect(PlayerLoginEvent event) {
        Player player = event.getPlayer();
        if (!emUtils.getUuidCache().contains(player.getUniqueId())) emUtils.getUuidCache().add(player.getUniqueId());
        if (clear.containsKey(player.getUniqueId())) clear.get(player.getUniqueId()).cancel();
    }

    /**
     * Trigger when player quit to the server
     *
     * @param event {@link PlayerQuitEvent} Event object of the event
     */
    @EventHandler
    public void onPlayerDisconnect(PlayerQuitEvent event) {
        try {
            EmPlayer.get(event.getPlayer()).destroy();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        if (emUtils.getConfig().getInt("caches.clear-uuid") != -1)
            new BukkitRunnable() {
                @Override
                public void run() {
                    emUtils.getUuidCache().remove(event.getPlayer().getUniqueId());
                }
            }.runTaskLater(emUtils, emUtils.getConfig().getInt("caches.clear-uuid") * 20L);

    }

    /**
     * Trigger when player break a block
     *
     * @param event {@link BlockBreakEvent} Event object of the event
     */
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        EmPlayer.get(event.getPlayer()).incrementMinedBlock(event.getBlock().getType());
    }

    /**
     * Trigger before player process a command
     *
     * @param event {@link PlayerCommandPreprocessEvent} Event object of the event
     */
    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        EmPlayer.get(event.getPlayer()).addCommand(event.getMessage());
    }

}
