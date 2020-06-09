package ga.enimaloc.emutils.spigot.commands;

import ga.enimaloc.emutils.spigot.Constant;
import ga.enimaloc.emutils.spigot.MenuConstant;
import ga.enimaloc.emutils.spigot.entity.Lang;
import ga.enimaloc.emutils.spigot.utils.MenuUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class EmutilsCommand implements CommandExecutor {

    /**
     * Executed when player execute {@code /emutils} command
     *
     * @param sender  command sender, mostly is {@link Player} or {@link org.bukkit.command.ConsoleCommandSender Console}
     * @param command command object
     * @param label   command or aliases executed
     * @param args    arguments after command
     * @return if false return Usage reply
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Constant.prefix + Lang.getLang("en").get("error.need_player"));
            return true;
        }
        Player player = (Player) sender;
        MenuConstant.main(player);
        return true;
    }

}
