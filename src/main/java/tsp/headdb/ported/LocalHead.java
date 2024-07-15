package tsp.headdb.ported;

import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.Validate;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.ItemTagReferences;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public class LocalHead extends Head {

    private UUID uuid;
    private String name;

    public LocalHead(UUID uuid) {
        this.uuid = uuid;
    }

    @Override
    public ItemStack getItemStack() {
        Validate.notNull(uuid, "uuid must not be null!");
        
        ItemStack item = new ItemStack(Items.PLAYER_HEAD);
        item.manager$setCustomName(TextInst.of(Utils.colorize("&e" + name)));
        item.getOrCreateNbt().putString("SkullOwner", name);
        ItemTagReferences.LORE.set(item, List.of(TextInst.of(Utils.colorize("&7UUID: " + uuid.toString()))));
        
        return item;
    }

    @Override
    public UUID getUniqueId() {
        return uuid;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getValue() {
        return null;
    }

    @Override
    public Category getCategory() {
        return null;
    }

    @Override
    public int getId() {
        return -1;
    }

    @Override
    public List<String> getTags() {
        return null;
    }

    @Override
    public LocalHead withUniqueId(UUID uuid) {
        this.uuid = uuid;
        return this;
    }

    @Override
    public LocalHead withName(String name) {
        this.name = name;
        return this;
    }

}