package ga.enimaloc.emutils.spigot.utils;

import de.tr7zw.changeme.nbtapi.NBTCompound;
import de.tr7zw.changeme.nbtapi.NBTItem;
import de.tr7zw.changeme.nbtapi.NBTListCompound;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class SkullUtils {
    public static final ItemStack PREVIOUS_SKULL = createSkull("Black Arrow Left", "5fecc571-bcbb-4aaa-b53c-b5d8715dbe37", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjgyYWQxYjljYjRkZDIxMjU5YzBkNzVhYTMxNWZmMzg5YzNjZWY3NTJiZTM5NDkzMzgxNjRiYWM4NGE5NmUifX19");
    public static final ItemStack NEXT_SKULL = createSkull("Black Arrow Right", "79f13daf-4884-40ab-8e35-95e472463321", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzdhZWU5YTc1YmYwZGY3ODk3MTgzMDE1Y2NhMGIyYTdkNzU1YzYzMzg4ZmYwMTc1MmQ1ZjQ0MTlmYzY0NSJ9fX0=");

    public static ItemStack createSkull(String Name, String Id, String textureValue) {
        return getSkull(new ItemStack(Material.PLAYER_HEAD), Name, Id, textureValue);
    }
    public static ItemStack getSkull(ItemStack head, String Name, String Id, String textureValue) {
        NBTItem nbti = new NBTItem(head);
        NBTCompound skull = nbti.addCompound("SkullOwner");
        skull.setString("Name", Name);
        skull.setString("Id", Id);
        NBTListCompound texture = skull.addCompound("Properties").getCompoundList("textures").addCompound();
        texture.setString("Value", textureValue);

        return nbti.getItem();
    }
}
