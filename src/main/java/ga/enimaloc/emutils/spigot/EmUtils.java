package ga.enimaloc.emutils.spigot;

import ga.enimaloc.emutils.spigot.commands.EmutilsCommand;
import ga.enimaloc.emutils.spigot.commands.EmutilsCommandTabCompleter;
import ga.enimaloc.emutils.spigot.listener.PlayerListener;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class EmUtils extends JavaPlugin {

    private Connection connection;
    private List<UUID> uuidCache;

//    public static boolean authMe;

    /**
     * Main method used by Spigot/Bukkit to run the plugin
     */
    @Override
    public void onEnable() {
        uuidCache = new ArrayList<>();
        this.saveDefaultConfig();

//        if (authMe = (this.getServer().getPluginManager().isPluginEnabled("AuthMe")
//                && getServer().getPluginManager().getPlugin("AuthMe") != null))
//            this.getLogger().info(String.format("%s plugin loaded, enabling module!", "AuthMe"));
        try {
            List<String> options = new ArrayList<>();
            for (String key : getConfig().getConfigurationSection("database.options").getKeys(false))
                options.add(key + "=" + getConfig().getString("database.options." + key));

            if (!openConnection(
                    getConfig().getString("database.hostname"),
                    getConfig().getInt("database.port"),
                    options.isEmpty() ? "" : String.join("&", options),
                    getConfig().getString("database.database"),
                    getConfig().getString("database.username"),
                    getConfig().getString("database.password")
            )) {
                this.getLogger().warning("No database connection found, disabling plugin.");
                this.getServer().getPluginManager().disablePlugin(this);
                return;
            }
            setupDatabase();
        } catch (SQLException | ClassNotFoundException throwables) {
            throwables.printStackTrace();
        }

        // Register commands
        PluginCommand emutilsCommand = this.getCommand("emutils");
        emutilsCommand.setExecutor(new EmutilsCommand()); // /emutils
        emutilsCommand.setTabCompleter(new EmutilsCommandTabCompleter());
        // Register events listener
        this.getServer().getPluginManager().registerEvents(new PlayerListener(this), this); // Player Listener

        super.onEnable();
    }

    /**
     * Used to open a mysql connection
     *
     * @param host     hostname of the database
     * @param port     port of database
     * @param options  options of database
     * @param database database name
     * @param username username of connection to open
     * @param password password of username
     * @throws SQLException           If the connection failed
     * @throws ClassNotFoundException If the class <code>com.mysql.jdbc.Driver</code> is not found
     */
    public boolean openConnection(String host, int port, String options, String database, String username, String password) throws ClassNotFoundException, SQLException {
        if (connection != null && !connection.isClosed()) // Connection already open
            return true;

        synchronized (this) {
            Class.forName("com.mysql.jdbc.Driver"); // Get jdbc Driver
            try {
                connection =
                        DriverManager.getConnection(
                                "jdbc:mysql://" +
                                        host + ":" + port +
                                        "/" + database +
                                        (options.isEmpty() ? "" : "?" + options),
                                username,
                                password
                        ); // Open connection
            } catch (SQLException e) {
                return false;
            }
        }
        return true;
    }

    /**
     * Used to create all tables
     */
    public void setupDatabase() throws SQLException {
        if (getConnection().createStatement().execute(
                "create table if not exists emPlayers(uuid varchar(36) not null, mined_block longtext null, commands longtext null);"
        )) {
            getConnection().createStatement().execute(
                    "create unique index emPlayers_uuid_uindex on emPlayers (uuid);"
            );
            getConnection().createStatement().execute(
                    "alter table emPlayers add constraint emPlayers_pk primary key (uuid);"
            );
        }
    }

    /**
     * @return MySQL connection
     */
    public Connection getConnection() {
        return connection;
    }

    /**
     * @return all UUID connected until <code>caches.clear-uuid</code> seconds after disconnection
     */
    public List<UUID> getUuidCache() {
        return uuidCache;
    }

}
