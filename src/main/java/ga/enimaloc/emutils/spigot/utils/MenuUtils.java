package ga.enimaloc.emutils.spigot.utils;

import com.gmail.filoghost.hiddenstring.HiddenStringUtils;
import de.themoep.inventorygui.GuiElement;
import de.themoep.inventorygui.GuiElementGroup;
import de.themoep.inventorygui.InventoryGui;
import de.themoep.inventorygui.StaticGuiElement;
import ga.enimaloc.emutils.spigot.EmUtils;
import ga.enimaloc.emutils.spigot.MenuConstant;
import ga.enimaloc.emutils.spigot.entity.EmPlayer;
import ga.enimaloc.emutils.spigot.entity.Lang;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;
import java.util.function.Predicate;

public class MenuUtils {

    /**
     * Generate select player GUI
     *
     * @param player  {@link Player} to display the GUI
     * @param onClick {@link Predicate<ItemStack>} accepted when a user click on player head
     */
    public static void selectPlayer(Player player, Predicate<ItemStack> onClick) {
        GuiElementGroup group = new GuiElementGroup('a');
        for (UUID uuid : EmUtils.getPlugin(EmUtils.class).getUuidCache()) {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
            ItemStack isPH = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta skullMeta = (SkullMeta) isPH.getItemMeta();
            skullMeta.setOwningPlayer(offlinePlayer);
            isPH.setItemMeta(skullMeta);
            group.addElement(
                    new StaticGuiElement(
                            'p',
                            isPH,
                            c -> onClick.test(c.getEvent().getCurrentItem()),
                            offlinePlayer.getName(),
                            HiddenStringUtils.encodeString(uuid.toString())
                    ));
        }
        MenuUtils.defaultGUI(
                player,
                "Select player",
                new String[]{
                        "aaaaaaaaa",
                        "aaaaaaaaa",
                        "aaaaaaaaa",
                        "aaaaaaaaa",
                        "aaaaaaaaa",
                        "b       c"
                },
                group);
    }

    public static InventoryGui defaultGUI(Player player, String title, String[] rows, GuiElement... elements) {
        // Gui setup
        InventoryGui gui = new InventoryGui(
                EmUtils.getPlugin(EmUtils.class),
                title,
                rows,
                elements
        );

        gui.setFiller(new ItemStack(Material.GRAY_STAINED_GLASS_PANE)); // Set item for empty slot
        gui.show(player); // Open gui to player

        // Update gui
        new BukkitRunnable() {
            @Override
            public void run() {
                if (player.getOpenInventory().getTitle().equals(gui.getTitle()))
                    gui.draw();
//                else this.cancel();
            }
        }.runTaskTimer(EmUtils.getPlugin(EmUtils.class), 0L, 1L);
        return gui;
    }
}
