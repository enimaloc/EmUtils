package ga.enimaloc.emutils.spigot;

import ga.enimaloc.emutils.spigot.entity.EmPlayer;
import org.bukkit.ChatColor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Constant {

    // Stock emPlayers instance for all players
    public static Map<UUID, EmPlayer> emPlayers = new HashMap<>();

    // Prefix of plugin in command return
    // (Not colored: "[EmUtils] >> ")
    public static final String prefix =
            ChatColor.GREEN+"["+ChatColor.DARK_BLUE+"EmUtils"+ChatColor.GREEN+"] "+ChatColor.GOLD+">> "+ChatColor.GRAY;

}
