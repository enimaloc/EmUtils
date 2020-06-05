package ga.enimaloc.emutils.spigot.entity;

import ga.enimaloc.emutils.spigot.Constant;
import ga.enimaloc.emutils.spigot.utils.WebUtils;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;

import java.io.IOException;
import java.util.*;

public class EmPlayer {

    private UUID uuid;
    private boolean premium;

    EmPlayer(OfflinePlayer player) {
        this.uuid = player.getUniqueId();
        this.minedBlocks = new HashMap<>();
        this.commandsList = new ArrayList<>();
        try {
            this.premium = WebUtils.isUsernamePremium(this.uuid);
        } catch (IOException e) {
            this.premium = false;
            e.printStackTrace();
        }
    }

    /**
     * @param player get {@link EmPlayer} of this {@link OfflinePlayer}
     * @return get or create {@link EmPlayer} object
     */
    public static EmPlayer get(OfflinePlayer player) {
        if (!Constant.emPlayers.containsKey(player.getUniqueId()))
            Constant.emPlayers.put(player.getUniqueId(), new EmPlayer(player));
        return Constant.emPlayers.get(player.getUniqueId());
    }

    /**
     * @return {@link UUID} from {@link OfflinePlayer#getUniqueId()}
     */
    public UUID getUuid() {
        return uuid;
    }

    /**
     * @return if the {@link OfflinePlayer} is premium account
     */
    public boolean isPremium() {
        return premium;
    }

    // Mined blocks section
    private Map<Material, Integer> minedBlocks;

    /**
     * @return a {@link Map} of player mined block
     */
    public Map<Material, Integer> getMinedBlocks() {
        return minedBlocks;
    }

    /**
     * @param material {@link Material} to increment
     */
    public void incrementMinedBlock(Material material) {
        minedBlocks.merge(material, 1, Integer::sum);
    }

    /**
     * @param material {@link Material} to get count
     * @return get number of mined block corresponding to {@link Material} parameter
     */
    public int getMinedBlockCount(Material material) {
        return minedBlocks.getOrDefault(material, 0);
    }

    /**
     * Source: <a href="https://mkyong.com/java/how-to-sort-a-map-in-java/">mkyong.com</a>
     * @return a sorted {@link Map} of {@link Material} of mined block
     */
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

    // Command list section
    private List<Map.Entry<Date, String>> commandsList;

    /**
     * @return all commands execute of {@link OfflinePlayer}
     */
    public List<Map.Entry<Date, String>> getCommandsList() {
        return commandsList;
    }

    /**
     * @param commands Commands string to add
     */
    public void addCommand(String commands) {
        commandsList.add(new AbstractMap.SimpleEntry<>(new Date(), commands));
    }

    // MySQL section

    /**
     * Used to load data from database
     */
    public void load() {}

    /**
     * Used to save data from database
     */
    public void save() {}
}
