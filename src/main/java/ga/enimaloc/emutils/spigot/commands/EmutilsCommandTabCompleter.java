package ga.enimaloc.emutils.spigot.commands;

import ga.enimaloc.emutils.spigot.EmUtils;
import ga.enimaloc.emutils.spigot.entity.EmPlayer;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class EmutilsCommandTabCompleter implements TabCompleter {

    /**
     * Trigger when user try to autocomplete
     *
     * @param sender  command sender, mostly is {@link Player} or {@link org.bukkit.command.ConsoleCommandSender Console}
     * @param command command object
     * @param label   command or aliases executed
     * @param args    arguments after command(triggered when space)
     * @return autocomplete list
     */
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> proposition = new ArrayList<>();
        switch (args.length) {
            case 1:
                proposition.add("player");
                break;

            case 2:
                if ("player".equalsIgnoreCase(args[0])) {
                    for (UUID uuid : EmUtils.getPlugin(EmUtils.class).getUuidCache()) {
                        proposition.add(Bukkit.getOfflinePlayer(uuid).getName());
                    }
                }
                break;
        }
        return proposition;
    }

}
