package ga.enimaloc.emutils.spigot.entity;

import ga.enimaloc.emutils.spigot.Constant;
import ga.enimaloc.emutils.spigot.utils.WebUtils;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

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

    // https://mkyong.com/java/how-to-sort-a-map-in-java/
    public Map<Material, Integer> getSortedMinedBlocks() {
        // 1. Convert Map to List of Map
        List<Map.Entry<Material, Integer>> list =
                new LinkedList<Map.Entry<Material, Integer>>(getMinedBlocks().entrySet());

        // 2. Sort list with Collections.sort(), provide a custom Comparator
        //    Try switch the o1 o2 position for a different order
        Collections.sort(list, new Comparator<Map.Entry<Material, Integer>>() {
            public int compare(Map.Entry<Material, Integer> o1,
                               Map.Entry<Material, Integer> o2) {
                return (o2.getValue()).compareTo(o1.getValue());
            }
        });

        // 3. Loop the sorted list and put it into a new insertion order Map LinkedHashMap
        Map<Material, Integer> sortedMap = new LinkedHashMap<Material, Integer>();
        for (Map.Entry<Material, Integer> entry : list) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }

        /*
        //classic iterator example
        for (Iterator<Map.Entry<String, Integer>> it = list.iterator(); it.hasNext(); ) {
            Map.Entry<String, Integer> entry = it.next();
            sortedMap.put(entry.getKey(), entry.getValue());
        }*/

        return sortedMap;
    }
}
