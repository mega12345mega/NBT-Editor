package tsp.headdb.ported;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.item.ItemStack;
import net.minecraft.util.Formatting;

public enum Category {
	
    ALPHABET("alphabet", Formatting.YELLOW, 20),
    ANIMALS("animals", Formatting.DARK_AQUA, 21),
    BLOCKS("blocks", Formatting.DARK_GRAY, 22),
    DECORATION("decoration", Formatting.LIGHT_PURPLE, 23),
    FOOD_DRINKS("food-drinks", Formatting.GOLD, 24),
    HUMANS("humans", Formatting.DARK_BLUE, 29),
    HUMANOID("humanoid", Formatting.AQUA, 30),
    MISCELLANEOUS("miscellaneous", Formatting.DARK_GREEN, 31),
    MONSTERS("monsters", Formatting.RED, 32),
    PLANTS("plants", Formatting.GREEN, 33);
	
    private final String name;
    private final Formatting color;
    private final int location;
    private final Map<Category, Head> item = new HashMap<>();
    private static final Category[] values = values();
    
    Category(String name, Formatting color, int location) {
        this.name = name;
        this.color = color;
        this.location = location;
    }
    
    public String getName() {
        return name;
    }
    
    public Formatting getColor() {
        return color;
    }
    
    public int getLocation() {
        return location;
    }
    
    public ItemStack getItem() {
        if (item.containsKey(this)) {
            return item.get(this).getItemStack();
        }
        
        item.put(this, HeadAPI.getHeads(this).get(0));
        return getItem();
    }
    
    public static Category getByName(String name) {
        for (Category category : values) {
            if (category.getName().equalsIgnoreCase(name)) {
                return category;
            }
        }

        return null;
    }
    
    public static Category[] getValues() {
        return values;
    }
    
}
