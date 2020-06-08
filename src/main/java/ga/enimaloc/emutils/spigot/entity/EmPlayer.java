package ga.enimaloc.emutils.spigot.entity;

import ga.enimaloc.emutils.spigot.EmUtils;
import ga.enimaloc.emutils.spigot.utils.WebUtils;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class EmPlayer {

    // Stock emPlayers instance for all players
    public static Map<UUID, EmPlayer> emPlayers = new HashMap<>();

    private final UUID uuid;
    private boolean premium;
    private final BukkitRunnable runnable;
    // Mined blocks section
    private final Map<Material, Integer> minedBlocks;
    // Command list section
    private final List<Map.Entry<Date, String>> commandsList;

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

        // Load from database
        try {
            load();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        // Periodic save
        long period = EmUtils.getInstance().getConfig().getLong("database.caches.save-period") * 20L;
        runnable = new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    save();
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
        };
        runnable.runTaskTimer(EmUtils.getInstance(), period, period);
    }

    /**
     * @param player get {@link EmPlayer} of this {@link OfflinePlayer}
     * @return get or create {@link EmPlayer} object
     */
    public static EmPlayer get(OfflinePlayer player) {
        if (!emPlayers.containsKey(player.getUniqueId()))
            emPlayers.put(player.getUniqueId(), new EmPlayer(player));
        return emPlayers.get(player.getUniqueId());
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
     *
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
    public void load() throws SQLException {
        PreparedStatement stmt = EmUtils.getInstance().getConnection().prepareStatement("SELECT * FROM emplayers WHERE uuid = ?;");
        stmt.setString(1, getUuid().toString());
        ResultSet result = stmt.executeQuery();
        if (result.next()) { // Load row
            // Loading mined blocks
            String minedBlock = result.getString("mined_block");
            for (String mb : minedBlock.split(";")) {
                String[] split = mb.split(":");
                getMinedBlocks().put(Material.valueOf(split[0]), Integer.valueOf(split[1]));
            }

            // Loading commands
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss");
            String commands = result.getString("commands");
            for (String c : commands.split(";")) {
                String[] split = c.split("\\|");
                try {
                    getCommandsList().add(new AbstractMap.SimpleEntry<>(simpleDateFormat.parse(split[0]), split[1]));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        } else { // Create row
            PreparedStatement statement = EmUtils.getInstance().getConnection().prepareStatement(
                    "INSERT INTO emplayers (uuid, mined_block, commands) VALUES (?, '', '');"
            );
            statement.setString(1, getUuid().toString());
            statement.execute();
        }
    }

    /**
     * Used to save data from database
     */
    public void save() throws SQLException {
        Connection connection = EmUtils.getInstance().getConnection();
        PreparedStatement stmt = connection.prepareStatement("UPDATE emplayers SET mined_block = ?, commands = ? WHERE uuid = ?;");
        stmt.setString(1, formatMinedBlockToString());
        stmt.setString(2, formatCommandListToString());
        stmt.setString(3, getUuid().toString());
        stmt.executeUpdate();
    }

    private String formatMinedBlockToString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (Material material : getMinedBlocks().keySet())
            stringBuilder.append(material.name()).append(":").append(getMinedBlockCount(material)).append(";");
        return stringBuilder.length() == 0 ? "" : stringBuilder.toString().substring(0, stringBuilder.toString().length() - 1);
    }

    /**
     * @return
     */
    private String formatCommandListToString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (Map.Entry<Date, String> entry : getCommandsList())
            stringBuilder
                    .append(new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss").format(entry.getKey()))
                    .append("|")
                    .append(entry.getValue())
                    .append(";");
        return stringBuilder.length() == 0 ? "" : stringBuilder.toString().substring(0, stringBuilder.toString().length() - 1);
    }

    /**
     * Trigger for deleted {@link EmPlayer} object
     *
     * @throws SQLException trigger when execute {@link #save()} method
     */
    public void destroy() throws SQLException {
        emPlayers.remove(getUuid());
        runnable.cancel();
        save();
    }
}
