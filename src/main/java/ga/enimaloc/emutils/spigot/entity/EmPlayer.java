package ga.enimaloc.emutils.spigot.entity;

import ga.enimaloc.emutils.spigot.Constant;
import ga.enimaloc.emutils.spigot.utils.WebUtils;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EmPlayer {

    private UUID uuid;
    private boolean premium;
    private Map<Material, Integer> minedBlocks;

    EmPlayer(OfflinePlayer player) {
        this.uuid = player.getUniqueId();
        this.minedBlocks = new HashMap<>();
        try {
            this.premium = WebUtils.isUsernamePremium(this.uuid);
        } catch (IOException e) {
            this.premium = false;
            e.printStackTrace();
        }
    }

    public static EmPlayer get(OfflinePlayer player) {
        if (!Constant.emPlayers.containsKey(player.getUniqueId()))
            Constant.emPlayers.put(player.getUniqueId(), new EmPlayer(player));
        return Constant.emPlayers.get(player.getUniqueId());
    }

    public UUID getUuid() {
        return uuid;
    }

    public boolean isPremium() {
        return premium;
    }

    // Mined blocks section

    public Map<Material, Integer> getMinedBlocks() {
        return minedBlocks;
    }

    public void incrementMinedBlock(Material material) {
        minedBlocks.merge(material, 1, Integer::sum);
    }

    public int getMinedBlockCount(Material material) {
        return minedBlocks.getOrDefault(material, 0);
    }
}
