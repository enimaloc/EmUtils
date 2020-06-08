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

import java.sql.SQLException;

public class PlayerListener implements Listener {

    private final EmUtils main;

    public PlayerListener(EmUtils main) {
        this.main = main;
    }

    /**
     * Trigger when player login to the server
     *
     * @param event {@link PlayerLoginEvent} Event object of the event
     */
    @EventHandler
    public void onPlayerConnect(PlayerLoginEvent event) {
        Player player = event.getPlayer();
        EmPlayer emPlayer = EmPlayer.get(player);
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
