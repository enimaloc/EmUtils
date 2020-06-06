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
     * Display GUI <br>
     * a = helmet <br>
     * b = main hand <br>
     * c = chestplate <br>
     * d = other hand <br>
     * e = leggings <br>
     * f = boots <br>
     * g = general info <br>
     * h = xp stats <br>
     * i = life stats <br>
     * j = mined blocks <br>
     * k = commands <br>
     * l =  <br>
     * m =  <br>
     * n =  <br>
     * o =  <br>
     * p =  <br>
     * q =  <br>
     * r =  <br>
     * s =  <br>
     * t =  <br>
     * u =  <br>
     * v =  <br>
     * w =  <br>
     * x =  <br>
     * y =  <br>
     * z =  <br>
     * @param player {@link Player} to open GUI
     * @param target {@link Player target player}
     */
    private void display(Player player, Player target) {
        EmPlayer emTarget = EmPlayer.get(target);
        Lang lang = Lang.getLang(player);

        InventoryGui gui;

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

        gui.addElement(helmet('a', target));
        gui.addElement(mainHand('b', target));
        gui.addElement(chestplate('c', target));
        gui.addElement(offHand('d', target));
        gui.addElement(leggings('e', target));
        gui.addElement(boots('f', target));

        gui.addElement(generalInfo('g', target, lang, emTarget));
        gui.addElement(xpStats('h', target, lang));
        gui.addElement(lifeStats('i', target, lang));

        gui.addElement(minedBlock('j', target, lang, emTarget, player));
        gui.addElement(commands('k', target));

        gui.setFiller(new ItemStack(Material.GRAY_STAINED_GLASS_PANE)); // Set item for empty slot
        gui.show(player); // Open gui to player

        // Update gui
        new BukkitRunnable(){
            @Override
            public void run(){
                if (player.getOpenInventory().getTitle().equals(gui.getTitle()))
                    gui.draw();
//                else this.cancel();
            }
        }.runTaskTimer(EmUtils.getInstance(), 0L, 1L);
    }

    private GuiElement helmet(char slot, OfflinePlayer target) {
        return new DynamicGuiElement(
                slot,
                () -> new StaticGuiElement(slot, target.getPlayer().getInventory().getHelmet())
        );
    }
    private GuiElement mainHand(char slot, OfflinePlayer target) {
        return new DynamicGuiElement(
                slot,
                () -> new StaticGuiElement(slot, target.getPlayer().getInventory().getItemInMainHand())
        );
    }
    private GuiElement chestplate(char slot, OfflinePlayer target) {
        return new DynamicGuiElement(
                slot,
                () -> new StaticGuiElement(slot, target.getPlayer().getInventory().getChestplate())
        );
    }
    private GuiElement offHand(char slot, OfflinePlayer target) {
        return new DynamicGuiElement(
                slot,
                () -> new StaticGuiElement(slot, target.getPlayer().getInventory().getItemInOffHand())
        );
    }
    private GuiElement leggings(char slot, OfflinePlayer target) {
        return new DynamicGuiElement(
                slot,
                () -> new StaticGuiElement(slot, target.getPlayer().getInventory().getLeggings())
        );
    }
    private GuiElement boots(char slot, OfflinePlayer target) {
        return new DynamicGuiElement(
                slot,
                () -> new StaticGuiElement(slot, target.getPlayer().getInventory().getBoots())
        );
    }

    private GuiElement generalInfo(char slot, OfflinePlayer target, Lang lang, EmPlayer emTarget) {
        return new DynamicGuiElement(slot, () ->
                new StaticGuiElement(
                        slot, new ItemStack(Material.PAPER),
                        lang.get("inventory.player_info.general_info"),
                        lang.get("inventory.player_info.general_info.hostname", target.getPlayer().getAddress().getAddress().getHostAddress()),
                        lang.get("inventory.player_info.general_info.locale", target.getPlayer().getLocale()),
                        lang.get("inventory.player_info.general_info.first_played", StringUtils.getFormattedDate(target.getFirstPlayed())),
                        lang.get("inventory.player_info.general_info.premium", lang.get(""+emTarget.isPremium())),
                        lang.get("inventory.player_info.general_info.online", lang.get(""+target.isOnline())),
                        lang.get("inventory.player_info.general_info.op", lang.get(""+target.isOp())),
                        lang.get("inventory.player_info.general_info.banned", lang.get(""+target.isBanned()))
                )
        );
    }
    private GuiElement xpStats(char slot, OfflinePlayer target, Lang lang) {
        return new DynamicGuiElement(slot, () ->
                new StaticGuiElement(
                        slot, new ItemStack(Material.EXPERIENCE_BOTTLE),
                        lang.get("inventory.player_info.xp_stats"),
                        lang.get("inventory.player_info.xp_stats.level", target.getPlayer().getLevel()),
                        StringUtils.formatProgressBar(Math.round(target.getPlayer().getExp()*100), 18)+" "+Math.round(target.getPlayer().getExp()*100)+"%"
                )
        );
    }
    private GuiElement lifeStats(char slot, OfflinePlayer target, Lang lang) {
        return new DynamicGuiElement(slot, () ->
                new StaticGuiElement(
                        slot, new ItemStack(Material.APPLE),
                        lang.get("inventory.player_info.life_stats"),
                        lang.get("inventory.player_info.life_stats.health", target.getPlayer().getHealth()/2+"/"+target.getPlayer().getHealthScale()/2),
                        lang.get("inventory.player_info.life_stats.food", target.getPlayer().getFoodLevel()),
                        lang.get("inventory.player_info.life_stats.saturation", target.getPlayer().getSaturation())
                )
        );
    }

    private GuiElement minedBlock(char slot, OfflinePlayer target, Lang lang, EmPlayer emTarget, Player player) {
        return new DynamicGuiElement(slot, () ->
                new StaticGuiElement(
                        slot, new ItemStack(Material.DIAMOND_PICKAXE),
                        click -> {
                            InventoryGui minedBlock = new InventoryGui(
                                    EmUtils.getInstance(),
                                    lang.get("inventory.mined_block", target.getName()),
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
                            minedBlock.addElement(new GuiElementGroup('a', elements.toArray(new GuiElement[0])));
                            minedBlock.show(player);

                            // Update inventory
                            new BukkitRunnable(){
                                @Override
                                public void run(){
                                    if (player.getOpenInventory().getTitle().equals(minedBlock.getTitle()))
                                        minedBlock.draw();
                                    else this.cancel();
                                }
                            }.runTaskTimer(EmUtils.getInstance(), 0L, 1L);

                            return true;
                        },
                        lang.get("inventory.player_info.mined_block"),
                        lang.get("inventory.player_info.mined_block.total", emTarget.getMinedBlocks().values().stream().mapToInt(Integer::intValue).sum())
                )
        );
    }
    private GuiElement commands(char slot, OfflinePlayer target) {
        return new StaticGuiElement(
                slot,
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
        );
    }
}
