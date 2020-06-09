package ga.enimaloc.emutils.spigot.commands;

import de.themoep.inventorygui.*;
import ga.enimaloc.emutils.spigot.Constant;
import ga.enimaloc.emutils.spigot.EmUtils;
import ga.enimaloc.emutils.spigot.MenuConstant;
import ga.enimaloc.emutils.spigot.entity.EmPlayer;
import ga.enimaloc.emutils.spigot.entity.Lang;
import ga.enimaloc.emutils.spigot.utils.MenuUtils;
import ga.enimaloc.emutils.spigot.utils.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PlayerCommand implements CommandExecutor {

    /**
     * Executed when player execute {@code /player} command
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
        if (args.length == 0) MenuUtils.selectPlayer(player, targetIS -> {
            MenuConstant.player(player, Bukkit.getPlayer(targetIS.getItemMeta().getDisplayName()));
            return true;
        });
        else MenuConstant.player(player, Bukkit.getPlayer(args[0]));
        return true;
    }

}
