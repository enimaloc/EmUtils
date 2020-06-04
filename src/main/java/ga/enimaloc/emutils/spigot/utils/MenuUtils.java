package ga.enimaloc.emutils.spigot.utils;

import de.themoep.inventorygui.GuiElementGroup;
import de.themoep.inventorygui.InventoryGui;
import de.themoep.inventorygui.StaticGuiElement;
import ga.enimaloc.emutils.spigot.EmUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class MenuUtils {

    public static void selectPlayer(Player player, Predicate<ItemStack> onClick) {
        GuiElementGroup group = new GuiElementGroup('a');
        for (Player onlinePlayer : Bukkit.getServer().getOnlinePlayers()) {
            ItemStack isPH = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta skullMeta = (SkullMeta) isPH.getItemMeta();
            skullMeta.setOwningPlayer(onlinePlayer.getPlayer());
            isPH.setItemMeta(skullMeta);
            group.addElement(
                    new StaticGuiElement(
                            'p',
                            isPH,
                            c->onClick.test(c.getEvent().getCurrentItem()),
                            onlinePlayer.getDisplayName()));
        }
        InventoryGui gui = new InventoryGui(
                EmUtils.getInstance(),
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

        gui.show(player);
    }
}
