package tsp.headdb.ported;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public enum Category {
	
    ALPHABET("alphabet", "nbteditor.hdb.category.alphabet", Formatting.YELLOW, 20),
    ANIMALS("animals", "nbteditor.hdb.category.animals", Formatting.DARK_AQUA, 21),
    BLOCKS("blocks", "nbteditor.hdb.category.blocks", Formatting.DARK_GRAY, 22),
    DECORATION("decoration", "nbteditor.hdb.category.decoration", Formatting.LIGHT_PURPLE, 23),
    FOOD_DRINKS("food-drinks", "nbteditor.hdb.category.food_drinks", Formatting.GOLD, 24),
    HUMANS("humans", "nbteditor.hdb.category.humans", Formatting.DARK_BLUE, 29),
    HUMANOID("humanoid", "nbteditor.hdb.category.humanoid", Formatting.AQUA, 30),
    MISCELLANEOUS("miscellaneous", "nbteditor.hdb.category.miscellaneous", Formatting.DARK_GREEN, 31),
    MONSTERS("monsters", "nbteditor.hdb.category.monsters", Formatting.RED, 32),
    PLANTS("plants", "nbteditor.hdb.category.plants", Formatting.GREEN, 33);
	
	private final String name;
    private final Text translatedName;
    private final Formatting color;
    private final int location;
    private final Map<Category, Head> item = new HashMap<>();
    private static final Category[] values = values();
    
    Category(String name, String translatedName, Formatting color, int location) {
        this.name = name;
    	this.translatedName = Text.translatable(translatedName);
        this.color = color;
        this.location = location;
    }
    
    public String getName() {
    	return name;
    }
    
    public String getTranslatedName() {
        return translatedName.getString();
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
            if (category.getTranslatedName().equalsIgnoreCase(name)) {
                return category;
            }
        }

        return null;
    }
    
    public static Category[] getValues() {
        return values;
    }
    
}
