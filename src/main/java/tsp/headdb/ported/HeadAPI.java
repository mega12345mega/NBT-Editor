package tsp.headdb.ported;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.luneruniverse.minecraft.mod.nbteditor.NBTEditor;
import com.luneruniverse.minecraft.mod.nbteditor.NBTEditorClient;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;

import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.Text;
import tsp.headdb.ported.inventory.InventoryUtils;

/**
 * This class provides simple methods
 * for interacting with the HeadDB plugin
 *
 * @author TheSilentPro
 */
public final class HeadAPI {

    private HeadAPI() {}

    private static final HeadDatabase database = new HeadDatabase();

    /**
     * Retrieves the main {@link HeadDatabase}
     *
     * @return Head Database
     */
    public static HeadDatabase getDatabase() {
        return database;
    }

    /**
     * Opens the database for a player
     * 
     */
    public static void openDatabase() {
        InventoryUtils.openDatabase();
    }

    /**
     * Opens a specific category of the database for a player
     * 
     * @param category Category to open
     */
    public static void openCategoryDatabase(Category category) {
        InventoryUtils.openCategoryDatabase(category);
    }

    /**
     * Opens the database with results of a specific search term
     *
     * @param search Search term
     */
    public static void openSearchDatabase(String search) {
        InventoryUtils.openSearchDatabase(search);
    }

    public static void openTagSearchDatabase(String tag) {
        InventoryUtils.openTagSearchDatabase(tag);
    }
    
    public static boolean checkUpdated() {
    	if (HeadAPI.getDatabase().isLastUpdateOld()) {
			MainUtil.client.player.sendMessage(Text.translatable("nbteditor.hdb.unloaded_database"), false);
			return false;
    	}
    	
    	return true;
    }

    /**
     * Retrieve a {@link Head} by it's ID
     *
     * @param id The ID of the head
     * @return The head
     */
    public static Head getHeadByID(int id) {
        return database.getHeadByID(id);
    }

    /**
     * Retrieve a {@link Head} by it's UUID
     *
     * @param uuid The UUID of the head
     * @return The head
     */
    public static Head getHeadByUniqueId(UUID uuid) {
        return database.getHeadByUniqueId(uuid);
    }

    public static List<Head> getHeadsByTag(String tag) {
        return database.getHeadsByTag(tag);
    }

    /**
     * Retrieves a {@link List} of {@link Head}'s matching a name
     *
     * @param name The name to match for
     * @return List of heads
     */
    public static List<Head> getHeadsByName(String name) {
        return database.getHeadsByName(name);
    }

    /**
     * Retrieves a {@link List} of {@link Head}'s in a {@link Category} matching a name
     *
     * @param category The category to search in
     * @param name The name to match for
     * @return List of heads
     */
    public static List<Head> getHeadsByName(Category category, String name) {
        return database.getHeadsByName(category, name);
    }

    /**
     * Retrieve a {@link Head} by it's value
     *
     * @param value The texture value
     * @return The head
     */
    public static Head getHeadByValue(String value) {
        return database.getHeadByValue(value);
    }

    /**
     * Retrieve a {@link List} of {@link Head}'s in a specific {@link Category}
     *
     * @param category The category to search in
     * @return List of heads
     */
    public static List<Head> getHeads(Category category) {
        return database.getHeads(category);
    }

    /**
     * Retrieve a {@link List} of all {@link Head}'s
     *
     * @return List of all heads
     */
    public static List<Head> getHeads() {
        return database.getHeads();
    }
    
    public static List<Category> getCategories() {
    	return database.getCategories();
    }
    
    private static final File FAVORITES_FILE = new File(NBTEditorClient.SETTINGS_FOLDER, "headdb_favorites.txt");
    private static final List<String> FAVORITES = new ArrayList<>();
    
    /**
     * Add a {@link Head} to a players favorites
     *
     * @param texture The texture of the head
     */
    public static void addFavoriteHead(String texture) {
    	if (FAVORITES.contains(texture))
    		return;
    	
    	FAVORITES.add(texture);
    	saveFavorites();
    }

    /**
     * Remove a {@link Head} from a players favorites
     *
     * @param texture The texture of the head
     */
    public static void removeFavoriteHead(String texture) {
    	if (FAVORITES.remove(texture))
    		saveFavorites();
    }
    
    public static void toggleFavoriteHead(Head head) {
    	if (FAVORITES.contains(head.getValue())) {
    		removeFavoriteHead(head.getValue());
    		Utils.sendMessage("Removed &e" + head.getName() + " &7from favorites.");
    	} else {
    		addFavoriteHead(head.getValue());
    		Utils.sendMessage("Added &e" + head.getName() + " &7to favorites.");
    	}
    }
    
    public static void loadFavorites() throws IOException {
    	FAVORITES.clear();
    	
    	if (!FAVORITES_FILE.exists())
    		return;
    	
    	String heads = new String(Files.readAllBytes(FAVORITES_FILE.toPath())).replace("\r", "");
    	if (heads.startsWith("v2\n")) {
    		
    		JsonArray headsArray = new Gson().fromJson(heads.substring("v2\n".length()), JsonArray.class);
    		for (JsonElement head : headsArray)
    			FAVORITES.add(head.getAsString());
    		
    	} else {
	    	
    		for (String line : heads.split("\n")) {
	    		if (line.isEmpty())
	    			continue;
	    		
	    		try {
	    			FAVORITES.add("LEGACY: " + Integer.parseInt(line));
	    		} catch (NumberFormatException e) {
	    			NBTEditor.LOGGER.error("Invalid legacy favorite", e);
	    		}
	    	}
    		
    	}
    }
    public static void resolveFavorites() {
    	List<String> legacyFavorites = FAVORITES.stream().filter(entry -> entry.startsWith("LEGACY: ")).toList();
    	for (String legacyFavorite : legacyFavorites) {
    		FAVORITES.remove(legacyFavorite);
    		
    		Head head = getHeadByID(Integer.parseInt(legacyFavorite.substring("LEGACY: ".length())));
    		FAVORITES.add(head.getValue());
    	}
    	saveFavorites();
    }
    private static void saveFavorites() {
    	JsonArray output = new JsonArray();
    	for (String favorite : FAVORITES)
    		output.add(favorite);
    	try {
			Files.write(FAVORITES_FILE.toPath(), ("v2\n" + output.toString()).getBytes());
		} catch (IOException e) {
			NBTEditor.LOGGER.error("Error while saving HeadDB favorites", e);
		}
    }

    /**
     * Retrieve a {@link List} of favorite {@link Head} for a player
     *
     * @param uuid The UUID of the player
     * @return List of favorite heads
     */
    public static List<Head> getFavoriteHeads() {
        List<Head> heads = new ArrayList<>();
        for (String favorite : FAVORITES) {
        	if (favorite.startsWith("LEGACY: ")) // Legacy favorites should already be resolved
        		continue;
            
            heads.add(getHeadByValue(favorite));
        }
        
        return heads;
    }

    /**
     * Retrieve a {@link List} of local heads.
     * These heads are from players that have joined the server at least once.
     *
     * @return List of {@link LocalHead}'s
     */
    public static List<LocalHead> getLocalHeads() {
        List<LocalHead> heads = new ArrayList<>();
        for (PlayerListEntry player : MainUtil.client.getNetworkHandler().getPlayerList()) {
            heads.add(new LocalHead(player.getProfile().getId())
                    .withName(player.getProfile().getName()));
        }

        return heads;
    }

    /**
     * Update the Head Database
     */
    public static void updateDatabase() {
        database.update();
    }

}
