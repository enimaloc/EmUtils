package ga.enimaloc.emutils.spigot.entity;

import ga.enimaloc.emutils.spigot.EmUtils;
import ga.enimaloc.emutils.spigot.utils.WebUtils;
import org.bukkit.*;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.PistonMoveReaction;
import org.bukkit.block.data.BlockData;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationAbandonedEvent;
import org.bukkit.entity.*;
import org.bukkit.entity.memory.MemoryKey;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.*;
import org.bukkit.map.MapView;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class EmPlayer /*implements Player*/ {

    // Stock emPlayers instance for all players
    public static Map<UUID, EmPlayer> emPlayers = new HashMap<>();

    private final UUID uuid;
    private final OfflinePlayer offlinePlayer;
    private boolean premium;
    private final BukkitRunnable runnable;
    // Mined blocks section
    private final Map<Material, Integer> minedBlocks;
    // Command list section
    private final List<Map.Entry<Date, String>> commandsList;

    EmPlayer(OfflinePlayer offlinePlayer) {
        this.offlinePlayer = offlinePlayer;
        this.uuid = offlinePlayer.getUniqueId();
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
        long period = EmUtils.getPlugin(EmUtils.class).getConfig().getLong("caches.save-period") * 20L;
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
        runnable.runTaskTimer(EmUtils.getPlugin(EmUtils.class), period, period);
    }

    /**
     * @param offlinePlayer get {@link EmPlayer} of this {@link OfflinePlayer}
     * @return get or create {@link EmPlayer} object
     */
    public static EmPlayer get(OfflinePlayer offlinePlayer) {
        if (!emPlayers.containsKey(offlinePlayer.getUniqueId()))
            emPlayers.put(offlinePlayer.getUniqueId(), new EmPlayer(offlinePlayer));
        return emPlayers.get(offlinePlayer.getUniqueId());
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
        PreparedStatement stmt = EmUtils.getPlugin(EmUtils.class).getConnection().prepareStatement("SELECT * FROM emPlayers WHERE uuid = ?;");
        stmt.setString(1, getUuid().toString());
        ResultSet result = stmt.executeQuery();
        if (result.next()) { // Load row
            // Loading mined blocks
            String minedBlock = result.getString("mined_block");
            if (!minedBlock.isEmpty()) {
                for (String mb : minedBlock.split(";")) {
                    String[] split = mb.split(":");
                    getMinedBlocks().put(Material.valueOf(split[0]), Integer.valueOf(split[1]));
                }
            }

            // Loading commands
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss");
            String commands = result.getString("commands");
            if (!commands.isEmpty()) {
                for (String c : commands.split(";")) {
                    String[] split = c.split("\\|");
                    try {
                        getCommandsList().add(new AbstractMap.SimpleEntry<>(simpleDateFormat.parse(split[0]), split[1]));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            }
        } else { // Create row
            PreparedStatement statement = EmUtils.getPlugin(EmUtils.class).getConnection().prepareStatement(
                    "INSERT INTO emPlayers (uuid, mined_block, commands) VALUES (?, '', '');"
            );
            statement.setString(1, getUuid().toString());
            statement.execute();
        }
    }

    /**
     * Used to save data from database
     */
    public void save() throws SQLException {
        Connection connection = EmUtils.getPlugin(EmUtils.class).getConnection();
        PreparedStatement stmt = connection.prepareStatement("UPDATE emPlayers SET mined_block = ?, commands = ? WHERE uuid = ?;");
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

    // Maybe add
//    //Implement Player
//    @Override
//    public String getDisplayName() {
//        return getOr(offlinePlayer.getPlayer().getDisplayName(), offlinePlayer.getName());
//    }
//
//    @Override
//    public void setDisplayName(String s) {
//        if (isOnline()) offlinePlayer.getPlayer().setDisplayName(s);
//    }
//
//    @Override
//    public String getPlayerListName() {
//        return getOr(offlinePlayer.getPlayer().getPlayerListName(), offlinePlayer.getName());
//    }
//
//    @Override
//    public void setPlayerListName(String s) {
//        if (isOnline()) offlinePlayer.getPlayer().setPlayerListName(s);
//    }
//
//    @Override
//    public String getPlayerListHeader() {
//        return getOr(offlinePlayer.getPlayer().getPlayerListHeader(), "");
//    }
//
//    @Override
//    public String getPlayerListFooter() {
//        return getOr(offlinePlayer.getPlayer().getPlayerListFooter(), "");
//    }
//
//    @Override
//    public void setPlayerListHeader(String s) {
//        if (isOnline()) offlinePlayer.getPlayer().setPlayerListHeader(s);
//    }
//
//    @Override
//    public void setPlayerListFooter(String s) {
//        if (isOnline()) offlinePlayer.getPlayer().setPlayerListFooter(s);
//    }
//
//    @Override
//    public void setPlayerListHeaderFooter(String s, String s1) {
//        if (isOnline()) offlinePlayer.getPlayer().setPlayerListHeaderFooter(s, s1);
//    }
//
//    @Override
//    public void setCompassTarget(Location location) {
//        if (isOnline()) offlinePlayer.getPlayer().setCompassTarget(location);
//    }
//
//    @Override
//    public Location getCompassTarget() {
//        return getOr(offlinePlayer.getPlayer().getCompassTarget(), new Location(null, 0, 0, 0));
//    }
//
//    @Override
//    public InetSocketAddress getAddress() {
//        return getOr(offlinePlayer.getPlayer().getAddress(), new InetSocketAddress(0));
//    }
//
//    @Override
//    public boolean isConversing() {
//        return false;
//    }
//
//    @Override
//    public void acceptConversationInput(String s) {
//
//    }
//
//    @Override
//    public boolean beginConversation(Conversation conversation) {
//        return false;
//    }
//
//    @Override
//    public void abandonConversation(Conversation conversation) {
//
//    }
//
//    @Override
//    public void abandonConversation(Conversation conversation, ConversationAbandonedEvent conversationAbandonedEvent) {
//
//    }
//
//    @Override
//    public void sendRawMessage(String s) {
//
//    }
//
//    @Override
//    public void kickPlayer(String s) {
//
//    }
//
//    @Override
//    public void chat(String s) {
//
//    }
//
//    @Override
//    public boolean performCommand(String s) {
//        return false;
//    }
//
//    @Override
//    public boolean isSneaking() {
//        return false;
//    }
//
//    @Override
//    public void setSneaking(boolean b) {
//
//    }
//
//    @Override
//    public boolean isSprinting() {
//        return false;
//    }
//
//    @Override
//    public void setSprinting(boolean b) {
//
//    }
//
//    @Override
//    public void saveData() {
//
//    }
//
//    @Override
//    public void loadData() {
//
//    }
//
//    @Override
//    public void setSleepingIgnored(boolean b) {
//
//    }
//
//    @Override
//    public boolean isSleepingIgnored() {
//        return false;
//    }
//
//    /**
//     * @param location
//     * @param b
//     * @param b1
//     * @deprecated
//     */
//    @Override
//    public void playNote(Location location, byte b, byte b1) {
//
//    }
//
//    @Override
//    public void playNote(Location location, Instrument instrument, Note note) {
//
//    }
//
//    @Override
//    public void playSound(Location location, Sound sound, float v, float v1) {
//
//    }
//
//    @Override
//    public void playSound(Location location, String s, float v, float v1) {
//
//    }
//
//    @Override
//    public void playSound(Location location, Sound sound, SoundCategory soundCategory, float v, float v1) {
//
//    }
//
//    @Override
//    public void playSound(Location location, String s, SoundCategory soundCategory, float v, float v1) {
//
//    }
//
//    @Override
//    public void stopSound(Sound sound) {
//
//    }
//
//    @Override
//    public void stopSound(String s) {
//
//    }
//
//    @Override
//    public void stopSound(Sound sound, SoundCategory soundCategory) {
//
//    }
//
//    @Override
//    public void stopSound(String s, SoundCategory soundCategory) {
//
//    }
//
//    /**
//     * @param location
//     * @param effect
//     * @param i
//     * @deprecated
//     */
//    @Override
//    public void playEffect(Location location, Effect effect, int i) {
//
//    }
//
//    @Override
//    public <T> void playEffect(Location location, Effect effect, T t) {
//
//    }
//
//    /**
//     * @param location
//     * @param material
//     * @param b
//     * @deprecated
//     */
//    @Override
//    public void sendBlockChange(Location location, Material material, byte b) {
//
//    }
//
//    @Override
//    public void sendBlockChange(Location location, BlockData blockData) {
//
//    }
//
//    /**
//     * @param location
//     * @param i
//     * @param i1
//     * @param i2
//     * @param bytes
//     * @deprecated
//     */
//    @Override
//    public boolean sendChunkChange(Location location, int i, int i1, int i2, byte[] bytes) {
//        return false;
//    }
//
//    @Override
//    public void sendSignChange(Location location, String[] strings) throws IllegalArgumentException {
//
//    }
//
//    @Override
//    public void sendSignChange(Location location, String[] strings, DyeColor dyeColor) throws IllegalArgumentException {
//
//    }
//
//    @Override
//    public void sendMap(MapView mapView) {
//
//    }
//
//    @Override
//    public void updateInventory() {
//
//    }
//
//    @Override
//    public void incrementStatistic(Statistic statistic) throws IllegalArgumentException {
//
//    }
//
//    @Override
//    public void decrementStatistic(Statistic statistic) throws IllegalArgumentException {
//
//    }
//
//    @Override
//    public void incrementStatistic(Statistic statistic, int i) throws IllegalArgumentException {
//
//    }
//
//    @Override
//    public void decrementStatistic(Statistic statistic, int i) throws IllegalArgumentException {
//
//    }
//
//    @Override
//    public void setStatistic(Statistic statistic, int i) throws IllegalArgumentException {
//
//    }
//
//    @Override
//    public int getStatistic(Statistic statistic) throws IllegalArgumentException {
//        return 0;
//    }
//
//    @Override
//    public void incrementStatistic(Statistic statistic, Material material) throws IllegalArgumentException {
//
//    }
//
//    @Override
//    public void decrementStatistic(Statistic statistic, Material material) throws IllegalArgumentException {
//
//    }
//
//    @Override
//    public int getStatistic(Statistic statistic, Material material) throws IllegalArgumentException {
//        return 0;
//    }
//
//    @Override
//    public void incrementStatistic(Statistic statistic, Material material, int i) throws IllegalArgumentException {
//
//    }
//
//    @Override
//    public void decrementStatistic(Statistic statistic, Material material, int i) throws IllegalArgumentException {
//
//    }
//
//    @Override
//    public void setStatistic(Statistic statistic, Material material, int i) throws IllegalArgumentException {
//
//    }
//
//    @Override
//    public void incrementStatistic(Statistic statistic, EntityType entityType) throws IllegalArgumentException {
//
//    }
//
//    @Override
//    public void decrementStatistic(Statistic statistic, EntityType entityType) throws IllegalArgumentException {
//
//    }
//
//    @Override
//    public int getStatistic(Statistic statistic, EntityType entityType) throws IllegalArgumentException {
//        return 0;
//    }
//
//    @Override
//    public void incrementStatistic(Statistic statistic, EntityType entityType, int i) throws IllegalArgumentException {
//
//    }
//
//    @Override
//    public void decrementStatistic(Statistic statistic, EntityType entityType, int i) {
//
//    }
//
//    @Override
//    public void setStatistic(Statistic statistic, EntityType entityType, int i) {
//
//    }
//
//    @Override
//    public void setPlayerTime(long l, boolean b) {
//
//    }
//
//    @Override
//    public long getPlayerTime() {
//        return 0;
//    }
//
//    @Override
//    public long getPlayerTimeOffset() {
//        return 0;
//    }
//
//    @Override
//    public boolean isPlayerTimeRelative() {
//        return false;
//    }
//
//    @Override
//    public void resetPlayerTime() {
//
//    }
//
//    @Override
//    public void setPlayerWeather(WeatherType weatherType) {
//
//    }
//
//    @Override
//    public WeatherType getPlayerWeather() {
//        return null;
//    }
//
//    @Override
//    public void resetPlayerWeather() {
//
//    }
//
//    @Override
//    public void giveExp(int i) {
//
//    }
//
//    @Override
//    public void giveExpLevels(int i) {
//
//    }
//
//    @Override
//    public float getExp() {
//        return 0;
//    }
//
//    @Override
//    public void setExp(float v) {
//
//    }
//
//    @Override
//    public int getLevel() {
//        return 0;
//    }
//
//    @Override
//    public void setLevel(int i) {
//
//    }
//
//    @Override
//    public int getTotalExperience() {
//        return 0;
//    }
//
//    @Override
//    public void setTotalExperience(int i) {
//
//    }
//
//    @Override
//    public void sendExperienceChange(float v) {
//
//    }
//
//    @Override
//    public void sendExperienceChange(float v, int i) {
//
//    }
//
//    @Override
//    public float getExhaustion() {
//        return 0;
//    }
//
//    @Override
//    public void setExhaustion(float v) {
//
//    }
//
//    @Override
//    public float getSaturation() {
//        return 0;
//    }
//
//    @Override
//    public void setSaturation(float v) {
//
//    }
//
//    @Override
//    public int getFoodLevel() {
//        return 0;
//    }
//
//    @Override
//    public void setFoodLevel(int i) {
//
//    }
//
//    @Override
//    public boolean getAllowFlight() {
//        return false;
//    }
//
//    @Override
//    public void setAllowFlight(boolean b) {
//
//    }
//
//    /**
//     * @param player
//     * @deprecated
//     */
//    @Override
//    public void hidePlayer(Player player) {
//
//    }
//
//    @Override
//    public void hidePlayer(Plugin plugin, Player player) {
//
//    }
//
//    /**
//     * @param player
//     * @deprecated
//     */
//    @Override
//    public void showPlayer(Player player) {
//
//    }
//
//    @Override
//    public void showPlayer(Plugin plugin, Player player) {
//
//    }
//
//    @Override
//    public boolean canSee(Player player) {
//        return false;
//    }
//
//    @Override
//    public boolean isFlying() {
//        return false;
//    }
//
//    @Override
//    public void setFlying(boolean b) {
//
//    }
//
//    @Override
//    public void setFlySpeed(float v) throws IllegalArgumentException {
//
//    }
//
//    @Override
//    public void setWalkSpeed(float v) throws IllegalArgumentException {
//
//    }
//
//    @Override
//    public float getFlySpeed() {
//        return 0;
//    }
//
//    @Override
//    public float getWalkSpeed() {
//        return 0;
//    }
//
//    /**
//     * @param s
//     * @deprecated
//     */
//    @Override
//    public void setTexturePack(String s) {
//
//    }
//
//    @Override
//    public void setResourcePack(String s) {
//
//    }
//
//    @Override
//    public void setResourcePack(String s, byte[] bytes) {
//
//    }
//
//    @Override
//    public Scoreboard getScoreboard() {
//        return null;
//    }
//
//    @Override
//    public void setScoreboard(Scoreboard scoreboard) throws IllegalArgumentException, IllegalStateException {
//
//    }
//
//    @Override
//    public boolean isHealthScaled() {
//        return false;
//    }
//
//    @Override
//    public void setHealthScaled(boolean b) {
//
//    }
//
//    @Override
//    public void setHealthScale(double v) throws IllegalArgumentException {
//
//    }
//
//    @Override
//    public double getHealthScale() {
//        return 0;
//    }
//
//    @Override
//    public Entity getSpectatorTarget() {
//        return null;
//    }
//
//    @Override
//    public void setSpectatorTarget(Entity entity) {
//
//    }
//
//    /**
//     * @param s
//     * @param s1
//     * @deprecated
//     */
//    @Override
//    public void sendTitle(String s, String s1) {
//
//    }
//
//    @Override
//    public void sendTitle(String s, String s1, int i, int i1, int i2) {
//
//    }
//
//    @Override
//    public void resetTitle() {
//
//    }
//
//    @Override
//    public void spawnParticle(Particle particle, Location location, int i) {
//
//    }
//
//    @Override
//    public void spawnParticle(Particle particle, double v, double v1, double v2, int i) {
//
//    }
//
//    @Override
//    public <T> void spawnParticle(Particle particle, Location location, int i, T t) {
//
//    }
//
//    @Override
//    public <T> void spawnParticle(Particle particle, double v, double v1, double v2, int i, T t) {
//
//    }
//
//    @Override
//    public void spawnParticle(Particle particle, Location location, int i, double v, double v1, double v2) {
//
//    }
//
//    @Override
//    public void spawnParticle(Particle particle, double v, double v1, double v2, int i, double v3, double v4, double v5) {
//
//    }
//
//    @Override
//    public <T> void spawnParticle(Particle particle, Location location, int i, double v, double v1, double v2, T t) {
//
//    }
//
//    @Override
//    public <T> void spawnParticle(Particle particle, double v, double v1, double v2, int i, double v3, double v4, double v5, T t) {
//
//    }
//
//    @Override
//    public void spawnParticle(Particle particle, Location location, int i, double v, double v1, double v2, double v3) {
//
//    }
//
//    @Override
//    public void spawnParticle(Particle particle, double v, double v1, double v2, int i, double v3, double v4, double v5, double v6) {
//
//    }
//
//    @Override
//    public <T> void spawnParticle(Particle particle, Location location, int i, double v, double v1, double v2, double v3, T t) {
//
//    }
//
//    @Override
//    public <T> void spawnParticle(Particle particle, double v, double v1, double v2, int i, double v3, double v4, double v5, double v6, T t) {
//
//    }
//
//    @Override
//    public AdvancementProgress getAdvancementProgress(Advancement advancement) {
//        return null;
//    }
//
//    @Override
//    public int getClientViewDistance() {
//        return 0;
//    }
//
//    @Override
//    public String getLocale() {
//        return null;
//    }
//
//    @Override
//    public void updateCommands() {
//
//    }
//
//    @Override
//    public void openBook(ItemStack itemStack) {
//
//    }
//
//    @Override
//    public Location getLocation() {
//        return null;
//    }
//
//    @Override
//    public Location getLocation(Location location) {
//        return null;
//    }
//
//    @Override
//    public void setVelocity(Vector vector) {
//
//    }
//
//    @Override
//    public Vector getVelocity() {
//        return null;
//    }
//
//    @Override
//    public double getHeight() {
//        return 0;
//    }
//
//    @Override
//    public double getWidth() {
//        return 0;
//    }
//
//    @Override
//    public BoundingBox getBoundingBox() {
//        return null;
//    }
//
//    @Override
//    public boolean isOnGround() {
//        return false;
//    }
//
//    @Override
//    public World getWorld() {
//        return null;
//    }
//
//    @Override
//    public void setRotation(float v, float v1) {
//
//    }
//
//    @Override
//    public boolean teleport(Location location) {
//        return false;
//    }
//
//    @Override
//    public boolean teleport(Location location, PlayerTeleportEvent.TeleportCause teleportCause) {
//        return false;
//    }
//
//    @Override
//    public boolean teleport(Entity entity) {
//        return false;
//    }
//
//    @Override
//    public boolean teleport(Entity entity, PlayerTeleportEvent.TeleportCause teleportCause) {
//        return false;
//    }
//
//    @Override
//    public List<Entity> getNearbyEntities(double v, double v1, double v2) {
//        return null;
//    }
//
//    @Override
//    public int getEntityId() {
//        return 0;
//    }
//
//    @Override
//    public int getFireTicks() {
//        return 0;
//    }
//
//    @Override
//    public int getMaxFireTicks() {
//        return 0;
//    }
//
//    @Override
//    public void setFireTicks(int i) {
//
//    }
//
//    @Override
//    public void remove() {
//
//    }
//
//    @Override
//    public boolean isDead() {
//        return false;
//    }
//
//    @Override
//    public boolean isValid() {
//        return false;
//    }
//
//    @Override
//    public void sendMessage(String s) {
//
//    }
//
//    @Override
//    public void sendMessage(String[] strings) {
//
//    }
//
//    @Override
//    public Server getServer() {
//        return null;
//    }
//
//    /**
//     * @deprecated
//     */
//    @Override
//    public boolean isPersistent() {
//        return false;
//    }
//
//    /**
//     * @param b
//     * @deprecated
//     */
//    @Override
//    public void setPersistent(boolean b) {
//
//    }
//
//    /**
//     * @deprecated
//     */
//    @Override
//    public Entity getPassenger() {
//        return null;
//    }
//
//    /**
//     * @param entity
//     * @deprecated
//     */
//    @Override
//    public boolean setPassenger(Entity entity) {
//        return false;
//    }
//
//    @Override
//    public List<Entity> getPassengers() {
//        return null;
//    }
//
//    @Override
//    public boolean addPassenger(Entity entity) {
//        return false;
//    }
//
//    @Override
//    public boolean removePassenger(Entity entity) {
//        return false;
//    }
//
//    @Override
//    public boolean isEmpty() {
//        return false;
//    }
//
//    @Override
//    public boolean eject() {
//        return false;
//    }
//
//    @Override
//    public float getFallDistance() {
//        return 0;
//    }
//
//    @Override
//    public void setFallDistance(float v) {
//
//    }
//
//    @Override
//    public void setLastDamageCause(EntityDamageEvent entityDamageEvent) {
//
//    }
//
//    @Override
//    public EntityDamageEvent getLastDamageCause() {
//        return null;
//    }
//
//    @Override
//    public UUID getUniqueId() {
//        return null;
//    }
//
//    @Override
//    public int getTicksLived() {
//        return 0;
//    }
//
//    @Override
//    public void setTicksLived(int i) {
//
//    }
//
//    @Override
//    public void playEffect(EntityEffect entityEffect) {
//
//    }
//
//    @Override
//    public EntityType getType() {
//        return null;
//    }
//
//    @Override
//    public boolean isInsideVehicle() {
//        return false;
//    }
//
//    @Override
//    public boolean leaveVehicle() {
//        return false;
//    }
//
//    @Override
//    public Entity getVehicle() {
//        return null;
//    }
//
//    @Override
//    public void setCustomNameVisible(boolean b) {
//
//    }
//
//    @Override
//    public boolean isCustomNameVisible() {
//        return false;
//    }
//
//    @Override
//    public void setGlowing(boolean b) {
//
//    }
//
//    @Override
//    public boolean isGlowing() {
//        return false;
//    }
//
//    @Override
//    public void setInvulnerable(boolean b) {
//
//    }
//
//    @Override
//    public boolean isInvulnerable() {
//        return false;
//    }
//
//    @Override
//    public boolean isSilent() {
//        return false;
//    }
//
//    @Override
//    public void setSilent(boolean b) {
//
//    }
//
//    @Override
//    public boolean hasGravity() {
//        return false;
//    }
//
//    @Override
//    public void setGravity(boolean b) {
//
//    }
//
//    @Override
//    public int getPortalCooldown() {
//        return 0;
//    }
//
//    @Override
//    public void setPortalCooldown(int i) {
//
//    }
//
//    @Override
//    public Set<String> getScoreboardTags() {
//        return null;
//    }
//
//    @Override
//    public boolean addScoreboardTag(String s) {
//        return false;
//    }
//
//    @Override
//    public boolean removeScoreboardTag(String s) {
//        return false;
//    }
//
//    @Override
//    public PistonMoveReaction getPistonMoveReaction() {
//        return null;
//    }
//
//    @Override
//    public BlockFace getFacing() {
//        return null;
//    }
//
//    @Override
//    public Pose getPose() {
//        return null;
//    }
//
//    @Override
//    public Spigot spigot() {
//        return null;
//    }
//
//    @Override
//    public boolean isOnline() {
//        return false;
//    }
//
//    @Override
//    public boolean isBanned() {
//        return false;
//    }
//
//    @Override
//    public boolean isWhitelisted() {
//        return false;
//    }
//
//    @Override
//    public void setWhitelisted(boolean b) {
//
//    }
//
//    @Override
//    public Player getPlayer() {
//        return null;
//    }
//
//    @Override
//    public long getFirstPlayed() {
//        return 0;
//    }
//
//    @Override
//    public long getLastPlayed() {
//        return 0;
//    }
//
//    @Override
//    public boolean hasPlayedBefore() {
//        return false;
//    }
//
//    @Override
//    public Map<String, Object> serialize() {
//        return null;
//    }
//
//    @Override
//    public String getName() {
//        return null;
//    }
//
//    @Override
//    public PlayerInventory getInventory() {
//        return null;
//    }
//
//    @Override
//    public Inventory getEnderChest() {
//        return null;
//    }
//
//    @Override
//    public MainHand getMainHand() {
//        return null;
//    }
//
//    @Override
//    public boolean setWindowProperty(InventoryView.Property property, int i) {
//        return false;
//    }
//
//    @Override
//    public InventoryView getOpenInventory() {
//        return null;
//    }
//
//    @Override
//    public InventoryView openInventory(Inventory inventory) {
//        return null;
//    }
//
//    @Override
//    public InventoryView openWorkbench(Location location, boolean b) {
//        return null;
//    }
//
//    @Override
//    public InventoryView openEnchanting(Location location, boolean b) {
//        return null;
//    }
//
//    @Override
//    public void openInventory(InventoryView inventoryView) {
//
//    }
//
//    @Override
//    public InventoryView openMerchant(Villager villager, boolean b) {
//        return null;
//    }
//
//    @Override
//    public InventoryView openMerchant(Merchant merchant, boolean b) {
//        return null;
//    }
//
//    @Override
//    public void closeInventory() {
//
//    }
//
//    /**
//     * @deprecated
//     */
//    @Override
//    public ItemStack getItemInHand() {
//        return null;
//    }
//
//    /**
//     * @param itemStack
//     * @deprecated
//     */
//    @Override
//    public void setItemInHand(ItemStack itemStack) {
//
//    }
//
//    @Override
//    public ItemStack getItemOnCursor() {
//        return null;
//    }
//
//    @Override
//    public void setItemOnCursor(ItemStack itemStack) {
//
//    }
//
//    @Override
//    public boolean hasCooldown(Material material) {
//        return false;
//    }
//
//    @Override
//    public int getCooldown(Material material) {
//        return 0;
//    }
//
//    @Override
//    public void setCooldown(Material material, int i) {
//
//    }
//
//    @Override
//    public int getSleepTicks() {
//        return 0;
//    }
//
//    @Override
//    public Location getBedSpawnLocation() {
//        return null;
//    }
//
//    @Override
//    public void setBedSpawnLocation(Location location) {
//
//    }
//
//    @Override
//    public void setBedSpawnLocation(Location location, boolean b) {
//
//    }
//
//    @Override
//    public boolean sleep(Location location, boolean b) {
//        return false;
//    }
//
//    @Override
//    public void wakeup(boolean b) {
//
//    }
//
//    @Override
//    public Location getBedLocation() {
//        return null;
//    }
//
//    @Override
//    public GameMode getGameMode() {
//        return null;
//    }
//
//    @Override
//    public void setGameMode(GameMode gameMode) {
//
//    }
//
//    @Override
//    public boolean isBlocking() {
//        return false;
//    }
//
//    @Override
//    public boolean isHandRaised() {
//        return false;
//    }
//
//    @Override
//    public int getExpToLevel() {
//        return 0;
//    }
//
//    @Override
//    public boolean discoverRecipe(NamespacedKey namespacedKey) {
//        return false;
//    }
//
//    @Override
//    public int discoverRecipes(Collection<NamespacedKey> collection) {
//        return 0;
//    }
//
//    @Override
//    public boolean undiscoverRecipe(NamespacedKey namespacedKey) {
//        return false;
//    }
//
//    @Override
//    public int undiscoverRecipes(Collection<NamespacedKey> collection) {
//        return 0;
//    }
//
//    /**
//     * @deprecated
//     */
//    @Override
//    public Entity getShoulderEntityLeft() {
//        return null;
//    }
//
//    /**
//     * @param entity
//     * @deprecated
//     */
//    @Override
//    public void setShoulderEntityLeft(Entity entity) {
//
//    }
//
//    /**
//     * @deprecated
//     */
//    @Override
//    public Entity getShoulderEntityRight() {
//        return null;
//    }
//
//    /**
//     * @param entity
//     * @deprecated
//     */
//    @Override
//    public void setShoulderEntityRight(Entity entity) {
//
//    }
//
//    @Override
//    public double getEyeHeight() {
//        return 0;
//    }
//
//    @Override
//    public double getEyeHeight(boolean b) {
//        return 0;
//    }
//
//    @Override
//    public Location getEyeLocation() {
//        return null;
//    }
//
//    @Override
//    public List<Block> getLineOfSight(Set<Material> set, int i) {
//        return null;
//    }
//
//    @Override
//    public Block getTargetBlock(Set<Material> set, int i) {
//        return null;
//    }
//
//    @Override
//    public List<Block> getLastTwoTargetBlocks(Set<Material> set, int i) {
//        return null;
//    }
//
//    @Override
//    public Block getTargetBlockExact(int i) {
//        return null;
//    }
//
//    @Override
//    public Block getTargetBlockExact(int i, FluidCollisionMode fluidCollisionMode) {
//        return null;
//    }
//
//    @Override
//    public RayTraceResult rayTraceBlocks(double v) {
//        return null;
//    }
//
//    @Override
//    public RayTraceResult rayTraceBlocks(double v, FluidCollisionMode fluidCollisionMode) {
//        return null;
//    }
//
//    @Override
//    public int getRemainingAir() {
//        return 0;
//    }
//
//    @Override
//    public void setRemainingAir(int i) {
//
//    }
//
//    @Override
//    public int getMaximumAir() {
//        return 0;
//    }
//
//    @Override
//    public void setMaximumAir(int i) {
//
//    }
//
//    @Override
//    public int getMaximumNoDamageTicks() {
//        return 0;
//    }
//
//    @Override
//    public void setMaximumNoDamageTicks(int i) {
//
//    }
//
//    @Override
//    public double getLastDamage() {
//        return 0;
//    }
//
//    @Override
//    public void setLastDamage(double v) {
//
//    }
//
//    @Override
//    public int getNoDamageTicks() {
//        return 0;
//    }
//
//    @Override
//    public void setNoDamageTicks(int i) {
//
//    }
//
//    @Override
//    public Player getKiller() {
//        return null;
//    }
//
//    @Override
//    public boolean addPotionEffect(PotionEffect potionEffect) {
//        return false;
//    }
//
//    @Override
//    public boolean addPotionEffect(PotionEffect potionEffect, boolean b) {
//        return false;
//    }
//
//    @Override
//    public boolean addPotionEffects(Collection<PotionEffect> collection) {
//        return false;
//    }
//
//    @Override
//    public boolean hasPotionEffect(PotionEffectType potionEffectType) {
//        return false;
//    }
//
//    @Override
//    public PotionEffect getPotionEffect(PotionEffectType potionEffectType) {
//        return null;
//    }
//
//    @Override
//    public void removePotionEffect(PotionEffectType potionEffectType) {
//
//    }
//
//    @Override
//    public Collection<PotionEffect> getActivePotionEffects() {
//        return null;
//    }
//
//    @Override
//    public boolean hasLineOfSight(Entity entity) {
//        return false;
//    }
//
//    @Override
//    public boolean getRemoveWhenFarAway() {
//        return false;
//    }
//
//    @Override
//    public void setRemoveWhenFarAway(boolean b) {
//
//    }
//
//    @Override
//    public EntityEquipment getEquipment() {
//        return null;
//    }
//
//    @Override
//    public void setCanPickupItems(boolean b) {
//
//    }
//
//    @Override
//    public boolean getCanPickupItems() {
//        return false;
//    }
//
//    @Override
//    public boolean isLeashed() {
//        return false;
//    }
//
//    @Override
//    public Entity getLeashHolder() throws IllegalStateException {
//        return null;
//    }
//
//    @Override
//    public boolean setLeashHolder(Entity entity) {
//        return false;
//    }
//
//    @Override
//    public boolean isGliding() {
//        return false;
//    }
//
//    @Override
//    public void setGliding(boolean b) {
//
//    }
//
//    @Override
//    public boolean isSwimming() {
//        return false;
//    }
//
//    @Override
//    public void setSwimming(boolean b) {
//
//    }
//
//    @Override
//    public boolean isRiptiding() {
//        return false;
//    }
//
//    @Override
//    public boolean isSleeping() {
//        return false;
//    }
//
//    @Override
//    public void setAI(boolean b) {
//
//    }
//
//    @Override
//    public boolean hasAI() {
//        return false;
//    }
//
//    @Override
//    public void setCollidable(boolean b) {
//
//    }
//
//    @Override
//    public boolean isCollidable() {
//        return false;
//    }
//
//    @Override
//    public <T> T getMemory(MemoryKey<T> memoryKey) {
//        return null;
//    }
//
//    @Override
//    public <T> void setMemory(MemoryKey<T> memoryKey, T t) {
//
//    }
//
//    @Override
//    public AttributeInstance getAttribute(Attribute attribute) {
//        return null;
//    }
//
//    @Override
//    public void damage(double v) {
//
//    }
//
//    @Override
//    public void damage(double v, Entity entity) {
//
//    }
//
//    @Override
//    public double getHealth() {
//        return 0;
//    }
//
//    @Override
//    public void setHealth(double v) {
//
//    }
//
//    @Override
//    public double getAbsorptionAmount() {
//        return 0;
//    }
//
//    @Override
//    public void setAbsorptionAmount(double v) {
//
//    }
//
//    /**
//     * @deprecated
//     */
//    @Override
//    public double getMaxHealth() {
//        return 0;
//    }
//
//    /**
//     * @param v
//     * @deprecated
//     */
//    @Override
//    public void setMaxHealth(double v) {
//
//    }
//
//    /**
//     * @deprecated
//     */
//    @Override
//    public void resetMaxHealth() {
//
//    }
//
//    @Override
//    public String getCustomName() {
//        return null;
//    }
//
//    @Override
//    public void setCustomName(String s) {
//
//    }
//
//    @Override
//    public void setMetadata(String s, MetadataValue metadataValue) {
//
//    }
//
//    @Override
//    public List<MetadataValue> getMetadata(String s) {
//        return null;
//    }
//
//    @Override
//    public boolean hasMetadata(String s) {
//        return false;
//    }
//
//    @Override
//    public void removeMetadata(String s, Plugin plugin) {
//
//    }
//
//    @Override
//    public boolean isPermissionSet(String s) {
//        return false;
//    }
//
//    @Override
//    public boolean isPermissionSet(Permission permission) {
//        return false;
//    }
//
//    @Override
//    public boolean hasPermission(String s) {
//        return false;
//    }
//
//    @Override
//    public boolean hasPermission(Permission permission) {
//        return false;
//    }
//
//    @Override
//    public PermissionAttachment addAttachment(Plugin plugin, String s, boolean b) {
//        return null;
//    }
//
//    @Override
//    public PermissionAttachment addAttachment(Plugin plugin) {
//        return null;
//    }
//
//    @Override
//    public PermissionAttachment addAttachment(Plugin plugin, String s, boolean b, int i) {
//        return null;
//    }
//
//    @Override
//    public PermissionAttachment addAttachment(Plugin plugin, int i) {
//        return null;
//    }
//
//    @Override
//    public void removeAttachment(PermissionAttachment permissionAttachment) {
//
//    }
//
//    @Override
//    public void recalculatePermissions() {
//
//    }
//
//    @Override
//    public Set<PermissionAttachmentInfo> getEffectivePermissions() {
//        return null;
//    }
//
//    @Override
//    public boolean isOp() {
//        return false;
//    }
//
//    @Override
//    public void setOp(boolean b) {
//
//    }
//
//    @Override
//    public PersistentDataContainer getPersistentDataContainer() {
//        return null;
//    }
//
//    @Override
//    public void sendPluginMessage(Plugin plugin, String s, byte[] bytes) {
//
//    }
//
//    @Override
//    public Set<String> getListeningPluginChannels() {
//        return null;
//    }
//
//    @Override
//    public <T extends Projectile> T launchProjectile(Class<? extends T> aClass) {
//        return null;
//    }
//
//    @Override
//    public <T extends Projectile> T launchProjectile(Class<? extends T> aClass, Vector vector) {
//        return null;
//    }
//
//    private <T> T getOr(T get, T or) {
//        return isOnline() ? get : or;
//    }
}
