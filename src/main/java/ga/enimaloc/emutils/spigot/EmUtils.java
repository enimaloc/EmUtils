package ga.enimaloc.emutils.spigot;

import ga.enimaloc.emutils.spigot.commands.PlayerCommand;
import ga.enimaloc.emutils.spigot.listener.PlayerListener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Objects;

public class EmUtils extends JavaPlugin {

    public static boolean BUNGEE;
    public static EmUtils instance;

    @Override
    public void onEnable() {
        instance = this;
        BUNGEE = getServer().spigot().getConfig().getConfigurationSection("settings").getBoolean("settings.bungeecord");
        this.saveDefaultConfig();

        this.getCommand("player").setExecutor(new PlayerCommand());
        this.getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
//        if (BUNGEE) Bukkit.getMessenger().registerIncomingPluginChannel(this, "emutils:bungee", new MessageListener());

        super.onEnable();
    }

}
