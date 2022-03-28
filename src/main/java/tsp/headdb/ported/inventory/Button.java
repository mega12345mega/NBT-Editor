package tsp.headdb.ported.inventory;

import java.util.Objects;
import java.util.function.Consumer;

import net.minecraft.item.ItemStack;

/**
 * A button
 */
public class Button {

    private static int counter;
    private final int ID = counter++;

    private ItemStack itemStack;
    private Consumer<InventoryClickEvent> action;

    /**
     * @param itemStack The Item
     */
    public Button(ItemStack itemStack) {
        this(itemStack, event -> {
        });
    }

    /**
     * @param itemStack The Item
     * @param action The action
     */
    public Button(ItemStack itemStack, Consumer<InventoryClickEvent> action) {
        this.itemStack = itemStack;
        this.action = action;
    }



    /**
     * @return The icon
     */
    public ItemStack getItemStack() {
        return itemStack;
    }

    /**
     * @param action The new action
     */
    public void setAction(Consumer<InventoryClickEvent> action) {
        this.action = action;
    }

    /**
     * @param event The event that triggered it
     */
    public void onClick(InventoryClickEvent event) {
        action.accept(event);
    }

    // We do not want equals collisions. The default hashcode would not fulfil this contract.
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Button)) {
            return false;
        }
        Button button = (Button) o;
        return ID == button.ID;
    }

    @Override
    public int hashCode() {
        return Objects.hash(ID);
    }
}
