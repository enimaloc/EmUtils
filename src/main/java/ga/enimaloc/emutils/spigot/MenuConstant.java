package ga.enimaloc.emutils.spigot;

import de.themoep.inventorygui.*;
import ga.enimaloc.emutils.spigot.entity.EmPlayer;
import ga.enimaloc.emutils.spigot.entity.Lang;
import ga.enimaloc.emutils.spigot.utils.MenuUtils;
import ga.enimaloc.emutils.spigot.utils.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MenuConstant {

    // Main GUI
    public static void main(Player player) {
        Lang lang = Lang.getLang(player);
        MenuUtils.defaultGUI(
               player,
                lang.get("inventory.main"),
                new String[]{
                        "abc      "
                },
               playerInfo('a', player, lang)
        );
    }

    private static GuiElement playerInfo(char slot, Player player, Lang lang) {
        return new StaticGuiElement(
                slot,
                new ItemStack(Material.PLAYER_HEAD),
                click -> {
                    MenuUtils.selectPlayer(player, targetIS -> {
                        MenuConstant.player(player, Bukkit.getPlayer(targetIS.getItemMeta().getDisplayName()));
                        return true;
                    });
                    return true;
                },
                lang.get("inventory.main.player_info")
        );
    }

    // Player GUI
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
     *
     * @param player {@link Player} to open GUI
     * @param target {@link Player target player}
     */
    public static void player(Player player, Player target) {
        EmPlayer emTarget = EmPlayer.get(target);
        Lang lang = Lang.getLang(player);

        MenuUtils.defaultGUI(
                player,
                lang.get("inventory.player_info", target.getDisplayName()),
                new String[]{
                        "         ",
                        "gh  a  op",
                        "ij bcd qr",
                        "kl  e  st",
                        "mn  f  uv",
                        "         ",
                },
                helmet('a', target),
                mainHand('b', target),
                chestplate('c', target),
                offHand('d', target),
                leggings('e', target),
                boots('f', target),

                generalInfo('g', player, target, lang, emTarget),
                xpStats('h', target, lang),
                lifeStats('i', target, lang),
                localisationStats('j', target, lang),

                minedBlock('k', target, lang, emTarget, player),
                commands('l', target)
        );
    }

    // Items
    private static GuiElement helmet(char slot, OfflinePlayer target) {
        return new DynamicGuiElement(
                slot,
                () -> new StaticGuiElement(slot, target.getPlayer().getInventory().getHelmet())
        );
    } // Helmet slot

    private static GuiElement mainHand(char slot, OfflinePlayer target) {
        return new DynamicGuiElement(
                slot,
                () -> new StaticGuiElement(slot, target.getPlayer().getInventory().getItemInMainHand())
        );
    } // Main hand slot

    private static GuiElement chestplate(char slot, OfflinePlayer target) {
        return new DynamicGuiElement(
                slot,
                () -> new StaticGuiElement(slot, target.getPlayer().getInventory().getChestplate())
        );
    } // Chestplate slot

    private static GuiElement offHand(char slot, OfflinePlayer target) {
        return new DynamicGuiElement(
                slot,
                () -> new StaticGuiElement(slot, target.getPlayer().getInventory().getItemInOffHand())
        );
    } // Off hand slot

    private static GuiElement leggings(char slot, OfflinePlayer target) {
        return new DynamicGuiElement(
                slot,
                () -> new StaticGuiElement(slot, target.getPlayer().getInventory().getLeggings())
        );
    } // Leggings slot

    private static GuiElement boots(char slot, OfflinePlayer target) {
        return new DynamicGuiElement(
                slot,
                () -> new StaticGuiElement(slot, target.getPlayer().getInventory().getBoots())
        );
    } // Boots slot

    private static GuiElement generalInfo(char slot, Player player, OfflinePlayer target, Lang lang, EmPlayer emTarget) {
        return new DynamicGuiElement(slot, () ->
                new StaticGuiElement(
                        slot, new ItemStack(Material.PAPER),
                        lang.get("inventory.player_info.general_info"),
                        lang.get("inventory.player_info.general_info.hostname", target.getPlayer().getAddress().getAddress().getHostAddress()),
                        lang.get("inventory.player_info.general_info.locale", target.getPlayer().getLocale()),
                        lang.get("inventory.player_info.general_info.first_played", StringUtils.getFormattedDate(target.getFirstPlayed(), player)),
                        lang.get("inventory.player_info.general_info.premium", lang.get("" + emTarget.isPremium())),
                        lang.get("inventory.player_info.general_info.online", lang.get("" + target.isOnline())),
                        lang.get("inventory.player_info.general_info.op", lang.get("" + target.isOp())),
                        lang.get("inventory.player_info.general_info.banned", lang.get("" + target.isBanned()))
                )
        );
    }

    private static GuiElement xpStats(char slot, OfflinePlayer target, Lang lang) {
        return new DynamicGuiElement(slot, () ->
                new StaticGuiElement(
                        slot, new ItemStack(Material.EXPERIENCE_BOTTLE),
                        lang.get("inventory.player_info.xp_stats"),
                        lang.get("inventory.player_info.xp_stats.level", target.getPlayer().getLevel()),
                        StringUtils.formatProgressBar(Math.round(target.getPlayer().getExp() * 100), 18) + " " + Math.round(target.getPlayer().getExp() * 100) + "%"
                )
        );
    }

    private static GuiElement lifeStats(char slot, OfflinePlayer target, Lang lang) {
        return new DynamicGuiElement(slot, () ->
                new StaticGuiElement(
                        slot, new ItemStack(Material.APPLE),
                        lang.get("inventory.player_info.life_stats"),
                        lang.get("inventory.player_info.life_stats.health", target.getPlayer().getHealth() / 2 + "/" + target.getPlayer().getHealthScale() / 2),
                        lang.get("inventory.player_info.life_stats.food", target.getPlayer().getFoodLevel()),
                        lang.get("inventory.player_info.life_stats.saturation", target.getPlayer().getSaturation())
                )
        );
    }

    private static GuiElement localisationStats(char slot, OfflinePlayer target, Lang lang) {
        return new DynamicGuiElement(slot, () ->
                new StaticGuiElement(
                        slot,
                        new ItemStack(Material.COMPASS),
                        lang.get("inventory.player_info.position"),
                        lang.get("inventory.player_info.position.x", target.getPlayer().getLocation().getX()),
                        lang.get("inventory.player_info.position.y", target.getPlayer().getLocation().getY()),
                        lang.get("inventory.player_info.position.z", target.getPlayer().getLocation().getZ()),
                        lang.get("inventory.player_info.position.yaw", target.getPlayer().getLocation().getYaw()),
                        lang.get("inventory.player_info.position.pitch", target.getPlayer().getLocation().getPitch()),
                        lang.get("inventory.player_info.position.target.distance", target.getPlayer().getCompassTarget().distance(target.getPlayer().getLocation())),
                        lang.get("inventory.player_info.position.target.x", target.getPlayer().getCompassTarget().getX()),
                        lang.get("inventory.player_info.position.target.y", target.getPlayer().getCompassTarget().getY()),
                        lang.get("inventory.player_info.position.target.z", target.getPlayer().getCompassTarget().getZ())
                ));
    }

    private static GuiElement minedBlock(char slot, OfflinePlayer target, Lang lang, EmPlayer emTarget, Player player) {
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
                            new BukkitRunnable() {
                                @Override
                                public void run() {
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

    private static GuiElement commands(char slot, OfflinePlayer target) {
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
                ChatColor.DARK_RED + "Disabled !"
        );
    }

}
