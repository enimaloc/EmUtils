package ga.enimaloc.emutils.spigot.entity;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public enum Lang {
    // English language
    EN(
            new String[]{"true", "Yes"},
            new String[]{"false", "No"},


            new String[]{"error.need_player", ChatColor.DARK_RED + "You need to be a player to do that!"},


            new String[]{"inventory.main", "EmUtils"},

            new String[]{"inventory.main.player_info", "Information of players"},


            new String[]{"inventory.player_info", "Information of %s"},

            new String[]{"inventory.player_info.general_info", "General Info:"},
            new String[]{"inventory.player_info.general_info.hostname", "IP: %s"},
            new String[]{"inventory.player_info.general_info.locale", "Lang: %s"},
            new String[]{"inventory.player_info.general_info.first_played", "First played: %s"},
            new String[]{"inventory.player_info.general_info.premium", "Premium? %s"},
            new String[]{"inventory.player_info.general_info.online", "Online? %s"},
            new String[]{"inventory.player_info.general_info.op", "Op? %s"},
            new String[]{"inventory.player_info.general_info.banned", "Banned? %s"},

            new String[]{"inventory.player_info.xp_stats", "XP Stats:"},
            new String[]{"inventory.player_info.xp_stats.level", "Level: %s"},

            new String[]{"inventory.player_info.life_stats", "Life Stats:"},
            new String[]{"inventory.player_info.life_stats.health", "Health: %s"},
            new String[]{"inventory.player_info.life_stats.food", "Food: %s"},
            new String[]{"inventory.player_info.life_stats.saturation", "Saturation: %s"},

            new String[]{"inventory.player_info.position", "Position:"},
            new String[]{"inventory.player_info.position.x", "X: %s"},
            new String[]{"inventory.player_info.position.y", "Y: %s"},
            new String[]{"inventory.player_info.position.z", "Z: %s"},
            new String[]{"inventory.player_info.position.yaw", "Yaw: %s"},
            new String[]{"inventory.player_info.position.pitch", "Pitch: %s"},
            new String[]{"inventory.player_info.position.target.distance", "Target distance: %s"},
            new String[]{"inventory.player_info.position.target.x", "Target X: %s"},
            new String[]{"inventory.player_info.position.target.y", "Target Y: %s"},
            new String[]{"inventory.player_info.position.target.z", "Target Z: %s"},

            new String[]{"inventory.player_info.mined_block", "Mined block"},
            new String[]{"inventory.player_info.mined_block.total", "Total: %s"},


            new String[]{"inventory.mined_block", "List of mined block of %s"},
            new String[]{"inventory.mined_block.count", "Count: %s"}
    ),
    // French language
    FR(
            new String[]{"true", "Oui"},
            new String[]{"false", "Non"},

            new String[]{"error.need_player", ChatColor.DARK_RED + "Vous devez être un joueur pour éxécuter cette commande !"},


            new String[]{"inventory.main", "EmUtils"},

            new String[]{"inventory.main.player_info", "Information des joueurs"},


            new String[]{"inventory.player_info", "Information de %s"},

            new String[]{"inventory.player_info.general_info", "Information général:"},
            new String[]{"inventory.player_info.general_info.hostname", "IP: %s"},
            new String[]{"inventory.player_info.general_info.locale", "Langue: %s"},
            new String[]{"inventory.player_info.general_info.first_played", "Premiére connection: %s"},
            new String[]{"inventory.player_info.general_info.premium", "Premium? %s"},
            new String[]{"inventory.player_info.general_info.online", "En ligne? %s"},
            new String[]{"inventory.player_info.general_info.op", "Op? %s"},
            new String[]{"inventory.player_info.general_info.banned", "Banni? %s"},

            new String[]{"inventory.player_info.xp_stats", "Statistique XP:"},
            new String[]{"inventory.player_info.xp_stats.level", "Niveau: %s"},

            new String[]{"inventory.player_info.life_stats", "Statistique de vie:"},
            new String[]{"inventory.player_info.life_stats.health", "Vie: %s"},
            new String[]{"inventory.player_info.life_stats.food", "Nourriture: %s"},
            new String[]{"inventory.player_info.life_stats.saturation", "Saturation: %s"},

            new String[]{"inventory.player_info.position", "Position:"},
            new String[]{"inventory.player_info.position.x", "X: %s"},
            new String[]{"inventory.player_info.position.y", "Y: %s"},
            new String[]{"inventory.player_info.position.z", "Z: %s"},
            new String[]{"inventory.player_info.position.yaw", "Yaw: %s"},
            new String[]{"inventory.player_info.position.pitch", "Pitch: %s"},
            new String[]{"inventory.player_info.position.target.distance", "Distance de la cible: %s"},
            new String[]{"inventory.player_info.position.target.x", "Cible X: %s"},
            new String[]{"inventory.player_info.position.target.y", "Cible Y: %s"},
            new String[]{"inventory.player_info.position.target.z", "Cible Z: %s"},

            new String[]{"inventory.player_info.mined_block", "Blocs minés"},
            new String[]{"inventory.player_info.mined_block.total", "Total: %s"},


            new String[]{"inventory.mined_block", "Liste de blocs minés de %s"},
            new String[]{"inventory.mined_block.count", "%s blocs minés"}
    ),

    ;

    Map<String, String> m;

    Lang(String[]... a) {
        m = new HashMap<>();
        for (String[] c : a) {
            m.put(c[0], c[1]);
        }
    }

    /**
     * @param lang Two first chars of the lang
     * @return Lang object
     */
    public static Lang getLang(String lang) {
        for (Lang value : values()) {
            if (lang.equalsIgnoreCase(value.name())) return value;
        }
        throw new IllegalArgumentException("Lang '" + lang + "' doesn't exist");
    }

    /**
     * @param player Player to get the locale
     * @return Lang object via {@link #getLang(String)}
     */
    public static Lang getLang(Player player) {
        return getLang(player.getLocale().split("_")[0]);
    }

    /**
     * Method without exception to try catch
     *
     * @param key      Key of the String to get
     * @param elements (Optional) Elements when formatting is needed
     * @return The value corresponding to key or key if the sentence was not found
     */
    public String get(String key, Object... elements) {
        try {
            return getE(key, elements);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        return key;
    }

    /**
     * Method with exception to try catch
     *
     * @param key      Key of the String to get
     * @param elements (Optional) Elements when formatting is needed
     * @return The value corresponding to the key
     * @throws IllegalArgumentException thrown when key is not found
     */
    public String getE(String key, Object... elements) throws IllegalArgumentException {
        if (!m.containsKey(key))
            throw new IllegalArgumentException("Key '" + key + "' doesn't exist in lang '" + this.name() + "'");
        return String.format(m.get(key), elements);
    }
}
