package ga.enimaloc.emutils.spigot;

import ga.enimaloc.emutils.spigot.entity.EmPlayer;
import org.bukkit.ChatColor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Constant {

    public static Map<UUID, EmPlayer> emPlayers = new HashMap<>();

    public static final String prefix =
            ChatColor.GREEN+"["+ChatColor.DARK_BLUE+"EmUtils"+ChatColor.GREEN+"] "+ChatColor.GOLD+">> "+ChatColor.GRAY;

}
