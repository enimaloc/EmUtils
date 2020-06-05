package ga.enimaloc.emutils.spigot;

import ga.enimaloc.emutils.spigot.commands.PlayerCommand;
import ga.enimaloc.emutils.spigot.listener.PlayerListener;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class EmUtils extends JavaPlugin {

    /**
     * @return instance of EmUtils class
     */
    public static EmUtils getInstance() {
        return instance;
    }
    private static EmUtils instance;

//    public static boolean authMe;

    /**
     * Main method used by Spigot/Bukkit to run the plugin
     */
    @Override
    public void onEnable() {
        instance = this;
        this.saveDefaultConfig();

//        if (authMe = (this.getServer().getPluginManager().isPluginEnabled("AuthMe")
//                && getServer().getPluginManager().getPlugin("AuthMe") != null))
//            this.getLogger().info(String.format("%s plugin loaded, enabling module!", "AuthMe"));
        try {
            List<String> options = new ArrayList<>();
            for (String key : getConfig().getConfigurationSection("database.options").getKeys(false))
                options.add(key+"="+getConfig().getString("database.options."+key));

            openConnection(
                    getConfig().getString("database.hostname"),
                    getConfig().getInt("database.port"),
                    options.isEmpty() ? "" : String.join("&", options),
                    getConfig().getString("database.database"),
                    getConfig().getString("database.username"),
                    getConfig().getString("database.password")
                    );
        } catch (SQLException | ClassNotFoundException throwables) {
            throwables.printStackTrace();
        }

        // Register commands
        this.getCommand("player").setExecutor(new PlayerCommand()); // /player
        // Register events listener
        this.getServer().getPluginManager().registerEvents(new PlayerListener(this), this); // Player Listener

        super.onEnable();
    }

    /**
     * Used to open a mysql connection
     * @param host hostname of the database
     * @param port port of database
     * @param options options of database
     * @param database database name
     * @param username username of connection to open
     * @param password password of username
     * @throws SQLException If the connection failed
     * @throws ClassNotFoundException If the class <code>com.mysql.jdbc.Driver</code> is not found
     */
    public void openConnection(String host, int port, String options, String database, String username, String password) throws SQLException, ClassNotFoundException {
        if (connection != null && !connection.isClosed()) // Connection already open
            return;

        synchronized (this) {
            Class.forName("com.mysql.jdbc.Driver"); // Get jdbc Driver
            connection =
                    DriverManager.getConnection(
                            "jdbc:mysql://" +
                                    host+":"+port +
                                    "/" + database +
                                    (options.isEmpty() ? "" : "?"+options),
                            username,
                            password
                    ); // Open connection
        }
    }

    /**
     * @return MySQL connection
     */
    public Connection getConnection() {
        return connection;
    }
    private Connection connection;
}
