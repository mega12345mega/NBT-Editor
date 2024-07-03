package tsp.headdb.ported.inventory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.screens.containers.ClientHandledScreen;
import com.luneruniverse.minecraft.mod.nbteditor.screens.util.StringInputScreen;
import com.luneruniverse.minecraft.mod.nbteditor.util.Lore;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import tsp.headdb.ported.Category;
import tsp.headdb.ported.Head;
import tsp.headdb.ported.HeadAPI;
import tsp.headdb.ported.LocalHead;
import tsp.headdb.ported.Utils;

public class InventoryUtils {

    private static final Map<String, Integer> uiLocation = new HashMap<>();
    private static final Map<String, ItemStack> uiItem = new HashMap<>();

    public static int getUILocation(String category, int slot) {
        // Try to use the cached value first.
        if (uiLocation.containsKey(category)) return uiLocation.get(category);
        
        // No valid value in the config file, return the given default.
        uiLocation.put(category, slot);
        return slot;
    }

    public static ItemStack getUIItem(String category, ItemStack item) {
        // Try to use the cached item first.
        if (uiItem.containsKey(category)) return uiItem.get(category);
        
        // No valid head or item in the config file, return the given default.
        uiItem.put(category, item);
        return item;
    }

    public static void openLocalMenu() {
        PagedPane pane = new PagedPane(4, 6, Utils.colorize("&c&lHeadDB &8- &aLocal Heads"));

        List<LocalHead> heads = HeadAPI.getLocalHeads();
        for (LocalHead localHead : heads) {
            pane.addButton(new Button(localHead.getItemStack(), e -> {
                if (e.getClickType() == ClickTypeMod.LEFT_SHIFT) {
                    purchaseHead(localHead, 64, "local", localHead.getName());
                    return;
                }
                if (e.getClickType() == ClickTypeMod.LEFT) {
                    purchaseHead(localHead, 1, "local", localHead.getName());
                    return;
                }
                if (e.getClickType() == ClickTypeMod.RIGHT) {
//                    player.closeInventory();
                    Utils.sendMessage("&cLocal heads can not be added to favorites!");
                }
            }));
        }

        pane.open();
    }

    public static void openFavoritesMenu() {
        PagedPane pane = new PagedPane(4, 6, Utils.colorize("&c&lHeadDB &8- &eFavorites"));

        List<Head> heads = HeadAPI.getFavoriteHeads();
        for (Head head : heads) {
            pane.addButton(new Button(head.getItemStack(), e -> {
                if (e.getClickType() == ClickTypeMod.LEFT_SHIFT) {
                    purchaseHead(head, 64, head.getCategory().getName(), head.getName());
                    return;
                }
                if (e.getClickType() == ClickTypeMod.LEFT) {
                    purchaseHead(head, 1, head.getCategory().getName(), head.getName());
                }
                if (e.getClickType() == ClickTypeMod.RIGHT) {
                    HeadAPI.removeFavoriteHead(head.getValue());
                    openFavoritesMenu();
                    Utils.sendMessage("Removed &e" + head.getName() + " &7from favorites.");
                }
            }));
        }

        pane.open();
    }

    public static PagedPane openSearchDatabase(String search) {
        PagedPane pane = new PagedPane(4, 6, Utils.colorize("&c&lHeadDB &8- &eSearch: " + search));

        List<Head> heads = HeadAPI.getHeadsByName(search);
        for (Head head : heads)
            pane.addButton(genButton(head));

        pane.open();
        return pane;
    }

    public static void openTagSearchDatabase(String tag) {
        PagedPane pane = new PagedPane(4, 6, Utils.colorize("&c&lHeadDB &8- &eTag Search: " + tag));

        List<Head> heads = HeadAPI.getHeadsByTag(tag);
        for (Head head : heads) {
            pane.addButton(genButton(head));
        }

        pane.open();
    }

    public static void openCategoryDatabase(Category category) {
        PagedPane pane = new PagedPane(4, 6, Utils.colorize("&c&lHeadDB &8- &e" + category.getTranslatedName()));

        List<Head> heads = HeadAPI.getHeads(category);
        for (Head head : heads) {
            pane.addButton(genButton(head));
        }

        pane.open();
    }
    
    private static Button genButton(Head head) {
    	return new Button(head.getItemStack(), e -> {
            if (e.getClickType() == ClickTypeMod.LEFT_SHIFT)
                purchaseHead(head, 64, head.getCategory().getName(), head.getName());
            else if (e.getClickType() == ClickTypeMod.LEFT)
                purchaseHead(head, 1, head.getCategory().getName(), head.getName());
            else if (e.getClickType() == ClickTypeMod.RIGHT)
                HeadAPI.toggleFavoriteHead(head);
        });
    }

    public static void openDatabase() {
    	ClientHandledScreen screen = new ClientHandledScreen(ClientHandledScreen.createGenericScreenHandler(6), MainUtil.client.player.getInventory(), TextInst.of(Utils.colorize("&c&lHeadDB &8(" + HeadAPI.getHeads().size() + ")"))) {
    		@Override
    		protected void onMouseClick(Slot slot, int slotId, int button, SlotActionType actionType) {
    			if (slot == null)
    				return;
    			slotId = slot.id;
    			
    			Inventory inventory = this.handler.getInventory();
    			
                if (inventory != null) {
                    ItemStack item = slot.getStack();

                    if (item != null && !item.isEmpty()) {
                        String name = MainUtil.stripColor(item.getName().getString().toLowerCase());
                        if (name.equalsIgnoreCase("favorites")) {
                            InventoryUtils.openFavoritesMenu();
                            return;
                        }
                        if (name.equalsIgnoreCase("local")) {
                            InventoryUtils.openLocalMenu();
                            return;
                        }
                        if (name.equalsIgnoreCase("search")) {
                        	new StringInputScreen(this, (text) -> {
                        		InventoryUtils.openSearchDatabase(text);
                        	}, (text) -> true).show("Search query");
                            return;
                        }

                        Category category = Category.getByName(name);

                        if (category != null) {
                            HeadAPI.openCategoryDatabase(category);
                        }
                    }
                }
    		}
    	};
        Inventory inventory = screen.getScreenHandler().getInventory();

        for (Category category : Category.getValues()) {
            ItemStack item = getUIItem(category.getName(), category.getItem());
            item.manager$setCustomName(TextInst.of(Utils.colorize(category.getColor() + "&l" + category.getTranslatedName().toUpperCase())));
            Lore lore = new Lore(item);
            lore.clearLore();
            lore.addLine(TextInst.of(Utils.colorize("&e" + TextInst.translatable("nbteditor.hdb.head_count", HeadAPI.getHeads(category).size()).getString())));
            inventory.setStack(getUILocation(category.getName(), category.getLocation()), item);
        }

        if (true) {
            inventory.setStack(getUILocation("favorites", 39), buildButton(
                getUIItem("favorites", new ItemStack(Items.BOOK)),
                "&eFavorites",
                "",
                "&8Click to view your favorites")
            );
        }

        if (true) {
            inventory.setStack(getUILocation("search", 40), buildButton(
                getUIItem("search", new ItemStack(Items.DARK_OAK_SIGN)),
                "&9Search",
                "",
                "&8Click to open search menu"
            ));
        }

        if (true) {
            inventory.setStack(getUILocation("local", 41), buildButton(
                getUIItem("local", new ItemStack(Items.COMPASS)),
                "&aLocal",
                "",
                "&8Online Players"
            ));
        }

        fill(inventory);
        MainUtil.client.setScreen(screen);
    }

    public static void fill(Inventory inv) {
        ItemStack item = getUIItem("fill", new ItemStack(Items.BLACK_STAINED_GLASS_PANE));
        // Do not bother filling the inventory if item to fill it with is AIR.
        if (item == null || item.isEmpty()) return;

        // Fill any non-empty inventory slots with the given item.
        int size = inv.size();
        for (int i = 0; i < size; i++) {
            ItemStack slotItem = inv.getStack(i);
            if (slotItem == null || slotItem.isEmpty()) {
                inv.setStack(i, item);
            }
        }
    }

    private static ItemStack buildButton(ItemStack item, String name, String... lore) {
        item.manager$setCustomName(TextInst.of(Utils.colorize(name)));
        
        Lore list = new Lore(item);
        list.clearLore();
        for (String line : lore) {
            list.addLine(TextInst.of(Utils.colorize(line)));
        }
        
        return item;
    }
    
    
    public static void purchaseHead(Head head, int amount, String category, String description) {
        ItemStack item = head.getItemStack();
        item.setCount(amount);
        MainUtil.getWithMessage(item);
    }

}
