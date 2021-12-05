package tsp.headdb.ported;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.luneruniverse.minecraft.mod.nbteditor.NBTEditorClient;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;

import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
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
			MainUtil.client.player.sendMessage(Text.of(Formatting.RED + "The head database is loading!"), false);
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
    
    private static final File FAVORITES_FILE = new File(NBTEditorClient.SETTINGS_FOLDER, "headdb_favorites.txt");
    private static final List<Integer> FAVORITES = new ArrayList<>();
    
    /**
     * Add a {@link Head} to a players favorites
     *
     * @param id The ID of the head
     */
    public static void addFavoriteHead(int id) {
    	if (FAVORITES.contains(id))
    		return;
    	
    	FAVORITES.add(id);
    	saveFavorites();
    }

    /**
     * Remove a {@link Head} from a players favorites
     *
     * @param id The ID of the head
     */
    public static void removeFavoriteHead(int id) {
    	if (FAVORITES.remove((Integer) id))
    		saveFavorites();
    }
    
    public static void toggleFavoriteHead(Head head) {
    	if (FAVORITES.contains(head.getId())) {
    		removeFavoriteHead(head.getId());
    		Utils.sendMessage("Removed &e" + head.getName() + " &7from favorites.");
    	} else {
    		addFavoriteHead(head.getId());
    		Utils.sendMessage("Added &e" + head.getName() + " &7to favorites.");
    	}
    }
    
    public static void loadFavorites() throws IOException {
    	FAVORITES.clear();
    	
    	if (!FAVORITES_FILE.exists())
    		return;
    	
    	for (String line : new String(Files.readAllBytes(FAVORITES_FILE.toPath())).replace("\r", "").split("\n")) {
    		if (line.isEmpty())
    			continue;
    		
    		try {
    			FAVORITES.add(Integer.parseInt(line));
    		} catch (NumberFormatException e) {
    			e.printStackTrace();
    		}
    	}
    }
    private static void saveFavorites() {
    	StringBuilder output = new StringBuilder();
    	for (int line : FAVORITES)
    		output.append(line).append('\n');
    	if (!FAVORITES.isEmpty())
    		output.deleteCharAt(output.length() - 1);
    	try {
			Files.write(FAVORITES_FILE.toPath(), output.toString().getBytes());
		} catch (IOException e) {
			e.printStackTrace();
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
        for (int id : FAVORITES) {
            Head head = getHeadByID(id);
            heads.add(head);
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
