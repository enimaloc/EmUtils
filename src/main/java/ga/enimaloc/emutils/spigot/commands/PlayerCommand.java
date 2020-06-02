package ga.enimaloc.emutils.spigot.commands;

import de.themoep.inventorygui.*;
import ga.enimaloc.emutils.spigot.Constant;
import ga.enimaloc.emutils.spigot.EmUtils;
import ga.enimaloc.emutils.spigot.entity.EmPlayer;
import ga.enimaloc.emutils.spigot.entity.Lang;
import ga.enimaloc.emutils.spigot.utils.MenuUtils;
import ga.enimaloc.emutils.spigot.utils.StringUtils;
import net.minecraft.server.v1_15_R1.IChatBaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class PlayerCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Constant.prefix+ Lang.getFromString("en").get("error.need_player"));
            return true;
        }
        Player player = (Player) sender;
        if(args.length == 0) MenuUtils.selectPlayer(player, targetIS->{
            display(player, Bukkit.getPlayer(targetIS.getItemMeta().getDisplayName())); return true;});
        else display(player, Bukkit.getPlayer(args[0]));
        return true;
    }

    private void display(Player player, Player target) {
        EmPlayer emTarget = EmPlayer.get(target);
        Lang lang = Lang.getFromString(player.getLocale().split("_")[0]);

        InventoryGui gui;
        final InventoryGui[] minedBlock = new InventoryGui[1];

        gui = new InventoryGui(
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
                                lang.get("inventory.player_info.general_info.hostname", target.getAddress().getAddress().getHostAddress()),
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

        gui.addElement(new DynamicGuiElement('j', () ->
                        new StaticGuiElement(
                                'j', new ItemStack(Material.DIAMOND_PICKAXE),
                                click -> {
                                    minedBlock[0] = new InventoryGui(
                                            EmUtils.instance,
                                            lang.get("inventory.mined_block", target.getDisplayName()),
                                            new String[]{
                                                    "aaaaaaaaa",
                                                    "aaaaaaaaa",
                                                    "aaaaaaaaa",
                                                    "aaaaaaaaa",
                                                    "aaaaaaaaa",
                                                    "b       c",
                                            }
                                    );
                                    List<GuiElement> elements = new ArrayList<>();
                                    for (Material m : emTarget.getSortedMinedBlocks().keySet()) {
                                        elements.add(
                                                new DynamicGuiElement(
                                                        'a',
                                                        () -> new StaticGuiElement(
                                                                'a',
                                                                new ItemStack(m),
                                                                " ",
                                                                lang.get("inventory.mined_block.count", emTarget.getMinedBlockCount(m))
                                                        )
                                                )
                                        );
                                    }
                                    minedBlock[0].addElement(new GuiElementGroup('a', elements.toArray(new GuiElement[0])));
                                    minedBlock[0].show(player);

                                    return true;
                                },
                                lang.get("inventory.player_info.mined_block"),
                                lang.get("inventory.player_info.mined_block.total", emTarget.getMinedBlocks().values().stream().mapToInt(Integer::intValue).sum())
                        )
                )
        );

        gui.addElement(new StaticGuiElement(
                'k',
                new ItemStack(Material.BOOK),
                click -> {
                    ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
                    BookMeta bookMeta = (BookMeta) book.getItemMeta();
                    StringBuilder stringBuilder = new StringBuilder();
                    String baseJson =
                            "{\"text\":\"[%date%]\",\"bold\":true,\"clickEvent\":{\"action\":\"run_command\",\"value\":\"%command%\"}},{\"text\":\" %command%\",\"color\":\"reset\",\"clickEvent\":{\"action\":\"suggest_command\",\"value\":\"%command%\"}}";
                    for (Map.Entry<Date, String> entry : emTarget.getCommandsList()) {
                        IChatBaseComponent component = IChatBaseComponent.ChatSerializer.a(
                                baseJson.replaceAll("%date%", StringUtils.getFormattedDate(entry.getKey()))
                                        .replaceAll("%command%", entry.getValue())
                        );
                    }
                    return true;
                }
        ));

        gui.setFiller(new ItemStack(Material.GRAY_STAINED_GLASS_PANE));
        gui.show(player);

        new BukkitRunnable(){
            @Override
            public void run(){
                if (player.getOpenInventory().getTitle().equals(gui.getTitle()))
                    gui.draw();
                else if (minedBlock[0] != null && player.getOpenInventory().getTitle().equals(minedBlock[0].getTitle()))
                    minedBlock[0].draw();
            }
        }.runTaskTimer(EmUtils.instance, 0L, 1L);
    }

}
