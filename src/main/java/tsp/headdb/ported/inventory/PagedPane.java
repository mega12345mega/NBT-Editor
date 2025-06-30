package tsp.headdb.ported.inventory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.lwjgl.glfw.GLFW;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.screens.containers.ClientHandledScreen;
import com.luneruniverse.minecraft.mod.nbteditor.screens.containers.ClientScreenHandler;
import com.luneruniverse.minecraft.mod.nbteditor.screens.widgets.InputOverlay;
import com.luneruniverse.minecraft.mod.nbteditor.screens.widgets.StringInput;
import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.ItemTagReferences;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import tsp.headdb.ported.HeadAPI;
import tsp.headdb.ported.Utils;

/**
 * A paged pane. Credits @ I Al Ianstaan
 */
public class PagedPane extends ClientHandledScreen {

    private SortedMap<Integer, Page> pages = new TreeMap<>();
    private int currentIndex;
    private int pageSize;

    protected Button controlBack;
    protected Button controlNext;
    protected Button controlMain;

    /**
     * @param pageSize The page size. inventory rows - 2
     */
    public PagedPane(int pageSize, int rows, String title) {
    	super(new ClientScreenHandler(rows), TextInst.of(MainUtil.colorize(title)));
        this.pageSize = pageSize;
        pages.put(0, new Page(pageSize));
    }

    /**
     * @param button The button to add
     */
    public void addButton(Button button) {
        for (Entry<Integer, Page> entry : pages.entrySet()) {
            if (entry.getValue().addButton(button)) {
                if (entry.getKey() == currentIndex) {
                    reRender();
                }
                return;
            }
        }
        Page page = new Page(pageSize);
        page.addButton(button);
        pages.put(pages.lastKey() + 1, page);

        reRender();
    }

    /**
     * @param button The Button to remove
     */
    public void removeButton(Button button) {
        for (Iterator<Entry<Integer, Page>> iterator = pages.entrySet().iterator(); iterator.hasNext(); ) {
            Entry<Integer, Page> entry = iterator.next();
            if (entry.getValue().removeButton(button)) {

                // we may need to delete the page
                if (entry.getValue().isEmpty()) {
                    // we have more than one page, so delete it
                    if (pages.size() > 1) {
                        iterator.remove();
                    }
                    // the currentIndex now points to a page that does not exist. Correct it.
                    if (currentIndex >= pages.size()) {
                        currentIndex--;
                    }
                }
                // if we modified the current one, re-render
                // if we deleted the current page, re-render too
                if (entry.getKey() >= currentIndex) {
                    reRender();
                }
                return;
            }
        }
    }

    /**
     * @return The amount of pages
     */
    public int getPageAmount() {
        return pages.size();
    }

    /**
     * @return The number of the current page (1 based)
     */
    public int getCurrentPage() {
        return currentIndex + 1;
    }

    /**
     * @param index The index of the new page
     */
    public void selectPage(int index) {
        if (index < 0 || index >= getPageAmount()) {
            throw new IllegalArgumentException(
                    "Index out of bounds s: " + index + " [" + 0 + ", " + getPageAmount() + ")"
            );
        }
        if (index == currentIndex) {
            return;
        }

        currentIndex = index;
        reRender();
    }

    /**
     * Renders the inventory again
     */
    public void reRender() {
        this.handler.getInventory().clear();
        pages.get(currentIndex).render(this.handler.getInventory());
        
        controlBack = null;
        controlNext = null;
        controlMain = null;
        createControls(this.handler.getInventory());
    }
    
    private boolean shiftKey;
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
    	if (keyCode == GLFW.GLFW_KEY_LEFT_SHIFT || keyCode == GLFW.GLFW_KEY_RIGHT_SHIFT)
    		shiftKey = true;
    	return super.keyPressed(keyCode, scanCode, modifiers);
    }
    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
    	if (keyCode == GLFW.GLFW_KEY_LEFT_SHIFT || keyCode == GLFW.GLFW_KEY_RIGHT_SHIFT)
    		shiftKey = false;
    	return super.keyReleased(keyCode, scanCode, modifiers);
    }
    
    @Override
    protected void onMouseClick(Slot slot, int slotId, int button, SlotActionType actionType) {
    	if (slot == null)
    		return;
    	slotId = slot.id;
    	
    	InventoryClickEvent event = new InventoryClickEvent(slot, slotId, button, actionType, ClickTypeMod.get(button == 1, shiftKey));
    	
    	// back item
        if (event.getSlotId() == getInventory().size() - 8) {
            if (controlBack != null) {
                controlBack.onClick(event);
            }
            return;
        }
        // next item
        else if (event.getSlotId() == getInventory().size() - 2) {
            if (controlNext != null) {
                controlNext.onClick(event);
            }
            return;
        }
        else if (event.getSlotId() == getInventory().size() - 5) {
            if (controlMain != null){
                controlMain.onClick(event);
            }
            return;
        }

        pages.get(currentIndex).handleClick(event);
    }
    
    @Override
    public void close() {
    	MainUtil.client.player.closeHandledScreen();
    }

    /**
     * Get the object's inventory.
     *
     * @return The inventory.
     */
    public Inventory getInventory() {
        return this.handler.getInventory();
    }

    /**
     * Creates the controls
     *
     * @param inventory The inventory
     */
    protected void createControls(Inventory inventory) {
        // create separator
        fillRow(
                inventory.size() / 9 - 2,
                new ItemStack(Items.BLACK_STAINED_GLASS_PANE),
                inventory
        );

        if (getCurrentPage() > 1) {
            String name = String.format(
                    Locale.ROOT,
                    "&3&lPage &a&l%d &7/ &c&l%d",
                    getCurrentPage() - 1, getPageAmount()
            );
            String lore = String.format(
                    Locale.ROOT,
                    "&7Previous: &c%d",
                    getCurrentPage() - 1
            );
            ItemStack itemStack = setMeta(HeadAPI.getHeadByValue("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODY1MmUyYjkzNmNhODAyNmJkMjg2NTFkN2M5ZjI4MTlkMmU5MjM2OTc3MzRkMThkZmRiMTM1NTBmOGZkYWQ1ZiJ9fX0=").getItemStack(), name, lore);
            controlBack = new Button(itemStack, event -> selectPage(currentIndex - 1));
            inventory.setStack(inventory.size() - 8, itemStack);
        }

        if (getCurrentPage() < getPageAmount()) {
            String name = String.format(
                    Locale.ROOT,
                    "&3&lPage &a&l%d &7/ &c&l%d",
                    getCurrentPage() + 1, getPageAmount()
            );
            String lore = String.format(
                    Locale.ROOT,
                    "&7Next: &c%d",
                    getCurrentPage() + 1
            );
            ItemStack itemStack = setMeta(HeadAPI.getHeadByValue("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMmEzYjhmNjgxZGFhZDhiZjQzNmNhZThkYTNmZTgxMzFmNjJhMTYyYWI4MWFmNjM5YzNlMDY0NGFhNmFiYWMyZiJ9fX0=").getItemStack(), name, lore);
            controlNext = new Button(itemStack, event -> selectPage(getCurrentPage()));
            inventory.setStack(inventory.size() - 2, itemStack);
        }

        {
            String name = String.format(
                    Locale.ROOT,
                    "&3&lPage &a&l%d &7/ &c&l%d",
                    getCurrentPage(), getPageAmount()
            );
            ItemStack itemStack = setMeta(HeadAPI.getHeadByValue("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvY2Q5MWY1MTI2NmVkZGM2MjA3ZjEyYWU4ZDdhNDljNWRiMDQxNWFkYTA0ZGFiOTJiYjc2ODZhZmRiMTdmNGQ0ZSJ9fX0=").getItemStack(),
                    name,
                    "&7Left-Click to go to the &cMain Menu",
                    "&7Right-Click to go to a &6Specific Page");
            controlMain = new Button(itemStack, event -> {
                if (event.getClickType() == ClickTypeMod.RIGHT) {
                	InputOverlay.show(
                			TextInst.of("Go to a Specific Page"),
                			StringInput.builder()
                					.withPlaceholder(TextInst.of("Page #"))
                					.withValidator(MainUtil.intPredicate(1, getPageAmount(), false))
                					.build(),
                			page -> selectPage(Integer.parseInt(page) - 1));
                } else {
                    InventoryUtils.openDatabase();
                }
            });
            inventory.setStack(inventory.size() - 5, itemStack);
        }
    }

    private void fillRow(int rowIndex, ItemStack itemStack, Inventory inventory) {
        int yMod = rowIndex * 9;
        for (int i = 0; i < 9; i++) {
            int slot = yMod + i;
            inventory.setStack(slot, setMeta(itemStack, ""));
        }
    }

    protected ItemStack setMeta(ItemStack itemStack, String name, String... lore) {
        itemStack.manager$setCustomName(TextInst.of(Utils.colorize(name)));
        ItemTagReferences.LORE.set(itemStack, Arrays.stream(lore).map(MainUtil::colorize).map(TextInst::of).collect(Collectors.toList()));
        return itemStack;
    }

    /**
     * @param player The {@link Player} to open it for
     */
    public void open() {
        reRender();
        MainUtil.client.setScreen(this);
    }

    private static class Page {
        private List<Button> buttons = new ArrayList<>();
        private int maxSize;

        Page(int maxSize) {
            this.maxSize = maxSize;
        }

        /**
         * @param event The click event
         */
        void handleClick(InventoryClickEvent event) {
            // user clicked in his own inventory. Silently drop it
            if (event.getSlotId() > event.getSlot().inventory.size()) {
                return;
            }
            if (event.getSlotId() >= buttons.size()) {
                return;
            }
            Button button = buttons.get(event.getSlotId());
            button.onClick(event);
        }

        /**
         * @return True if there is space left
         */
        boolean hasSpace() {
            return buttons.size() < maxSize * 9;
        }

        /**
         * @param button The {@link Button} to add
         *
         * @return True if the button was added, false if there was no space
         */
        boolean addButton(Button button) {
            if (!hasSpace()) {
                return false;
            }
            buttons.add(button);

            return true;
        }

        /**
         * @param button The {@link Button} to remove
         *
         * @return True if the button was removed
         */
        boolean removeButton(Button button) {
            return buttons.remove(button);
        }

        /**
         * @param inventory The inventory to render in
         */
        void render(Inventory inventory) {
            for (int i = 0; i < buttons.size(); i++) {
                Button button = buttons.get(i);

                inventory.setStack(i, button.getItemStack());
            }
        }

        /**
         * @return True if this page is empty
         */
        boolean isEmpty() {
            return buttons.isEmpty();
        }
    }

}
