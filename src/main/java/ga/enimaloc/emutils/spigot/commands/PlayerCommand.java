package ga.enimaloc.emutils.spigot.commands;

import de.themoep.inventorygui.DynamicGuiElement;
import de.themoep.inventorygui.InventoryGui;
import de.themoep.inventorygui.StaticGuiElement;
import ga.enimaloc.emutils.spigot.Constant;
import ga.enimaloc.emutils.spigot.EmUtils;
import ga.enimaloc.emutils.spigot.entity.EmPlayer;
import ga.enimaloc.emutils.spigot.entity.Lang;
import ga.enimaloc.emutils.spigot.utils.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Date;

public class PlayerCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Constant.prefix+ Lang.getFromString("en").get("error.need_player"));
            return true;
        }
        if (args.length == 0) return false;
        Player player = (Player) sender;
        Player target = Bukkit.getPlayer(args[0]);
        EmPlayer emTarget = EmPlayer.get(target);
        Lang lang = Lang.getFromString(player.getLocale().split("_")[0]);

        InventoryGui gui = new InventoryGui(
                EmUtils.instance,
                lang.get("inventory.player_info", target.getDisplayName()),
                new String[]{
                        "         ",
                        "gh  a  op",
                        "ij bcd qr",
                        "kl  e  st",
                        "mn  f  uv",
                        "         ",
                }
        );
        gui.addElement(new DynamicGuiElement('a', () -> new StaticGuiElement('a', target.getInventory().getHelmet())));
        gui.addElement(new DynamicGuiElement('b', () -> new StaticGuiElement('b', target.getInventory().getItemInMainHand())));
        gui.addElement(new DynamicGuiElement('c', () -> new StaticGuiElement('c', target.getInventory().getChestplate())));
        gui.addElement(new DynamicGuiElement('d', () -> new StaticGuiElement('d', target.getInventory().getItemInOffHand())));
        gui.addElement(new DynamicGuiElement('e', () -> new StaticGuiElement('e', target.getInventory().getLeggings())));
        gui.addElement(new DynamicGuiElement('f', () -> new StaticGuiElement('f', target.getInventory().getBoots())));
        gui.addElement(new DynamicGuiElement('g', () ->
                        new StaticGuiElement(
                                'g', new ItemStack(Material.PAPER),
                                lang.get("inventory.player_info.general_info"),
                                lang.get("inventory.player_info.general_info.hostname", target.getAddress().getHostName()),
                                lang.get("inventory.player_info.general_info.locale", target.getLocale()),
                                lang.get("inventory.player_info.general_info.first_played", StringUtils.getFormattedDate(target.getFirstPlayed())),
                                lang.get("inventory.player_info.general_info.premium", lang.get(""+emTarget.isPremium())),
                                lang.get("inventory.player_info.general_info.online", lang.get(""+target.isOnline())),
                                lang.get("inventory.player_info.general_info.op", lang.get(""+target.isOp())),
                                lang.get("inventory.player_info.general_info.banned", lang.get(""+target.isBanned()))
                        )
                )
        );
        gui.addElement(new DynamicGuiElement('h', () ->
                        new StaticGuiElement(
                                'h', new ItemStack(Material.EXPERIENCE_BOTTLE),
                                lang.get("inventory.player_info.xp_stats"),
                                lang.get("inventory.player_info.xp_stats.level", target.getLevel()),
                                StringUtils.formatProgressBar(Math.round(target.getExp()*100), 18)+" "+Math.round(target.getExp()*100)+"%"
                        )
                )
        );
        gui.addElement(new DynamicGuiElement('i', () ->
                        new StaticGuiElement(
                                'i', new ItemStack(Material.APPLE),
                                lang.get("inventory.player_info.life_stats"),
                                lang.get("inventory.player_info.life_stats.health", target.getHealth()/2+"/"+target.getHealthScale()/2),
                                lang.get("inventory.player_info.life_stats.food", target.getFoodLevel()),
                                lang.get("inventory.player_info.life_stats.saturation", target.getSaturation())
                        )
                )
        );

        gui.setFiller(new ItemStack(Material.GRAY_STAINED_GLASS_PANE));
        gui.show(player);

        new BukkitRunnable(){
            @Override
            public void run(){
                gui.draw();
            }
        }.runTaskTimer(EmUtils.instance, 0L, 1L);

        return true;
    }

}
