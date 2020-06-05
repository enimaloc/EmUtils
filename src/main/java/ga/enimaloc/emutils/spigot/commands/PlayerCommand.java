package ga.enimaloc.emutils.spigot.commands;

import de.themoep.inventorygui.*;
import fr.xephi.authme.api.v3.AuthMeApi;
import fr.xephi.authme.api.v3.AuthMePlayer;
import ga.enimaloc.emutils.spigot.Constant;
import ga.enimaloc.emutils.spigot.EmUtils;
import ga.enimaloc.emutils.spigot.entity.EmPlayer;
import ga.enimaloc.emutils.spigot.entity.Lang;
import ga.enimaloc.emutils.spigot.utils.MenuUtils;
import ga.enimaloc.emutils.spigot.utils.StringUtils;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import net.minecraft.server.v1_15_R1.IChatBaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class PlayerCommand implements CommandExecutor {

    /**
     * Executed when player execute {@code /player} command
     * @param sender command sender, mostly is {@link Player} or {@link org.bukkit.command.ConsoleCommandSender Console}
     * @param command command object
     * @param label command or aliases executed
     * @param args arguments after command
     * @return if false return Usage reply
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Constant.prefix+ Lang.getLang("en").get("error.need_player"));
            return true;
        }
        Player player = (Player) sender;
        if(args.length == 0) MenuUtils.selectPlayer(player, targetIS->{
            display(player, Bukkit.getPlayer(targetIS.getItemMeta().getDisplayName())); return true;});
        else display(player, Bukkit.getPlayer(args[0]));
        return true;
    }

    /**
     * Display GUI
     * @param player {@link Player} to open GUI
     * @param target {@link Player target player}
     */
    private void display(Player player, Player target) {
        EmPlayer emTarget = EmPlayer.get(target);
        Lang lang = Lang.getLang(player);

        InventoryGui gui;
        final InventoryGui[] minedBlock = new InventoryGui[1];

        // Gui setup
        gui = new InventoryGui(
                EmUtils.getInstance(),
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

        // Helmet slot
        gui.addElement(new DynamicGuiElement('a', () -> new StaticGuiElement('a', target.getInventory().getHelmet())));
        // Main hand slot
        gui.addElement(new DynamicGuiElement('b', () -> new StaticGuiElement('b', target.getInventory().getItemInMainHand())));
        // Chestplate slot
        gui.addElement(new DynamicGuiElement('c', () -> new StaticGuiElement('c', target.getInventory().getChestplate())));
        // Other hand slot
        gui.addElement(new DynamicGuiElement('d', () -> new StaticGuiElement('d', target.getInventory().getItemInOffHand())));
        // Leggings slot
        gui.addElement(new DynamicGuiElement('e', () -> new StaticGuiElement('e', target.getInventory().getLeggings())));
        // Boots slot
        gui.addElement(new DynamicGuiElement('f', () -> new StaticGuiElement('f', target.getInventory().getBoots())));

        // General info
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

        // XP Stats
        gui.addElement(new DynamicGuiElement('h', () ->
                        new StaticGuiElement(
                                'h', new ItemStack(Material.EXPERIENCE_BOTTLE),
                                lang.get("inventory.player_info.xp_stats"),
                                lang.get("inventory.player_info.xp_stats.level", target.getLevel()),
                                StringUtils.formatProgressBar(Math.round(target.getExp()*100), 18)+" "+Math.round(target.getExp()*100)+"%"
                        )
                )
        );

        // Life stats
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

        // Mined block
        gui.addElement(new DynamicGuiElement('j', () ->
                        new StaticGuiElement(
                                'j', new ItemStack(Material.DIAMOND_PICKAXE),
                                click -> {
                                    minedBlock[0] = new InventoryGui(
                                            EmUtils.getInstance(),
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
                                                        () -> {
                                                            ItemStack item = new ItemStack(m);
                                                            ItemMeta itemMeta = item.getItemMeta();
                                                            itemMeta.setLore(
                                                                    Collections.singletonList(
                                                                            lang.get(
                                                                                    "inventory.mined_block.count",
                                                                                    emTarget.getMinedBlockCount(m)
                                                                            )
                                                                    )
                                                            );
                                                            item.setItemMeta(itemMeta);
                                                            return new StaticGuiElement(
                                                                    'a',
                                                                    item
                                                            );
                                                        }
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

        // Commands
        gui.addElement(new StaticGuiElement(
                'k',
                new ItemStack(Material.BOOK),
                click -> {
//                    ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
//                    BookMeta bookMeta = (BookMeta) book.getItemMeta();
//                    StringBuilder stringBuilder = new StringBuilder();
//                    for (Map.Entry<Date, String> entry : emTarget.getCommandsList()) {
//                        BaseComponent[] components =
//                                new ComponentBuilder("[" + StringUtils.getFormattedDate(entry.getKey()) + "] ")
//                                    .bold(true)
//                                    .append(entry.getValue())
//                                    .event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, entry.getValue()))
//                                    .create();
//                        bookMeta.spigot().addPage(components);
//                    }
//                    book.setItemMeta(bookMeta);
//                    player.openBook(book);
                    return true;
                },
                ChatColor.DARK_RED+"Disabled !"
        ));

        gui.setFiller(new ItemStack(Material.GRAY_STAINED_GLASS_PANE)); // Set item for empty slot
        gui.show(player); // Open gui to player

        // Update gui
        new BukkitRunnable(){
            @Override
            public void run(){
                if (player.getOpenInventory().getTitle().equals(gui.getTitle()))
                    gui.draw();
                else if (minedBlock[0] != null && player.getOpenInventory().getTitle().equals(minedBlock[0].getTitle()))
                    minedBlock[0].draw();
            }
        }.runTaskTimer(EmUtils.getInstance(), 0L, 1L);
    }
}
