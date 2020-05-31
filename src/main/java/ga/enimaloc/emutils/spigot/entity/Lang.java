package ga.enimaloc.emutils.spigot.entity;

import org.bukkit.ChatColor;

import java.util.HashMap;
import java.util.Map;

public enum Lang {
    EN(
            new String[]{"true", "Yes"},
            new String[]{"false", "No"},


            new String[]{"error.need_player", ChatColor.DARK_RED+"You need to be a player to do that!"},


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
            new String[]{"inventory.player_info.life_stats.saturation", "Saturation: %s"}
    ),
    FR(
            new String[]{"true", "Oui"},
            new String[]{"false", "Non"},

            new String[]{"error.need_player", ChatColor.DARK_RED+"Vous devez être un joueur pour éxécuter cette commande !"},


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
            new String[]{"inventory.player_info.life_stats.saturation", "Saturation: %s"}
    ),

    ;

    Map<String, String> m;

    Lang(String[]... a) {
        m = new HashMap<>();
        for (String[] c : a) {
            m.put(c[0], c[1]);
        }
    }

    public String get(String key, Object... elements) {
        try {
            return getE(key, elements);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        return key;
    }

    public String getE(String key, Object... elements) throws IllegalArgumentException {
        if (!m.containsKey(key)) throw new IllegalArgumentException("Key '"+key+"' doesn't exist in lang '"+this.name()+"'");
        return String.format(m.get(key), elements);
    }

    public static Lang getFromString(String lang) {
        for (Lang value : values()) {
            if (lang.equalsIgnoreCase(value.name())) return value;
        }
        throw new IllegalArgumentException("Lang '"+lang+"' doesn't exist");
    }
}
