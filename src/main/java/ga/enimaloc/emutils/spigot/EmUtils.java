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

    private static EmUtils instance;
    public static EmUtils getInstance() {
        return instance;
    }

//    public static boolean authMe;

    private Connection connection;

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

        this.getCommand("player").setExecutor(new PlayerCommand());
        this.getServer().getPluginManager().registerEvents(new PlayerListener(this), this);

        super.onEnable();
    }

    public void openConnection(String host, int port, String options, String database, String username, String password) throws SQLException, ClassNotFoundException {
        if (connection != null && !connection.isClosed()) {
            return;
        }

        synchronized (this) {
            if (connection != null && !connection.isClosed()) {
                return;
            }
            Class.forName("com.mysql.jdbc.Driver");
            connection =
                    DriverManager.getConnection(
                            "jdbc:mysql://" +
                                    host+":"+port +
                                    "/" + database +
                                    (options.isEmpty() ? "" : "?"+options),
                            username,
                            password
                    );
        }
    }

    public Connection getConnection() {
        return connection;
    }
}
