package tsp.headdb.ported;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.luneruniverse.minecraft.mod.nbteditor.NBTEditor;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;

/**
 * This is the Database that holds all heads
 *
 * @author TheSilentPro
 */
public class HeadDatabase {

    private final long refresh;
    private int timeout;
    private final Map<Category, List<Head>> HEADS = new HashMap<>();
    private long updated;

    public HeadDatabase() {
        this.refresh = 3600;
        this.timeout = 5000;
    }

    public HeadDatabase(long refresh) {
        this.refresh = refresh;
        this.timeout = 5000;
    }

    public Head getHeadByValue(String value) {
        List<Head> heads = getHeads();
        for (Head head : heads) {
            if (head.getValue().equals(value)) {
                return head;
            }
        }

        return null;
    }

    public Head getHeadByID(int id) {
        List<Head> heads = getHeads();
        for (Head head : heads) {
            if (head.getId() == id) {
                return head;
            }
        }

        return null;
    }

    public Head getHeadByUniqueId(UUID uuid) {
        List<Head> heads = getHeads();
        for (Head head : heads) {
            if (head.getUniqueId().equals(uuid)) {
                return head;
            }
        }

        return null;
    }

    public List<Head> getHeadsByTag(String tag) {
        List<Head> result = new ArrayList<>();
        List<Head> heads = getHeads();
        tag = tag.toLowerCase(Locale.ROOT);
        for (Head head : heads) {
            for (String t : head.getTags()) {
                if (t.toLowerCase(Locale.ROOT).contains(tag)) {
                    result.add(head);
                }
            }
        }

        return result;
    }

    public List<Head> getHeadsByName(Category category, String name) {
        List<Head> result = new ArrayList<>();
        List<Head> heads = getHeads(category);
        for (Head head : heads) {
            String hName = MainUtil.stripColor(head.getName().toLowerCase(Locale.ROOT));
            if (hName.contains(MainUtil.stripColor(name.toLowerCase(Locale.ROOT)))) {
                result.add(head);
            }
        }

        return result;
    }

    public List<Head> getHeadsByName(String name) {
    	if (name.startsWith("id:")) {
    		try {
    			Head head = getHeadByID(Integer.parseInt(name.substring(3)));
    			if (head != null) {
    				List<Head> output = new ArrayList<>();
    				output.add(head);
    				return output;
    			}
    		} catch (NumberFormatException e) {}
    	}
    	
    	
        List<Head> result = new ArrayList<>();
        for (Category category : Category.values()) {
            result.addAll(getHeadsByName(category, name));
        }

        return result;
    }

    public List<Head> getHeads(Category category) {
        return HEADS.get(category);
    }

    public List<Head> getHeads() {
        if (!HEADS.isEmpty() && !isLastUpdateOld()) {
            List<Head> heads = new ArrayList<>();
            for (Category category : HEADS.keySet()) {
                heads.addAll(HEADS.get(category));
            }
            return heads;
        }

        update();
        return getHeads();
    }
    
    public List<Category> getCategories() {
    	return Collections.unmodifiableList(new ArrayList<>(HEADS.keySet()));
    }

    /**
     * Gets all heads from the api provider
     *
     * @return Map containing each head in it's category. Returns null if the fetching failed.
     */
    public Map<Category, List<Head>> getHeadsNoCache() {
        Map<Category, List<Head>> result = new HashMap<>();
        Category[] categories = Category.getValues();

        int id = 1;
        for (Category category : categories) {
            NBTEditor.LOGGER.debug("Caching heads from: " + category.getName());
            long start = System.currentTimeMillis();
            List<Head> heads = new ArrayList<>();
            try {
                String line;
                StringBuilder response = new StringBuilder();

                URLConnection connection = new URL("https://minecraft-heads.com/scripts/api.php?cat=" + category.getName() + "&tags=true").openConnection();
                connection.setConnectTimeout(timeout);
                connection.setRequestProperty("User-Agent", "NBTEditor-DatabaseUpdater");
                try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    while ((line = in.readLine()) != null) {
                        response.append(line);
                    }
                }
                Gson parser = new Gson();
                JsonArray array = parser.fromJson(response.toString(), JsonArray.class);
                for (JsonElement o : array) {
                    JsonObject obj = o.getAsJsonObject();
                    String rawUUID = obj.get("uuid").getAsString();

                    UUID uuid;
                    if (Utils.validateUniqueId(rawUUID)) {
                        uuid = UUID.fromString(rawUUID);
                    } else {
                        NBTEditor.LOGGER.debug("UUID " + rawUUID + " is invalid. Using random one for head id: " + id);
                        uuid = UUID.randomUUID();
                    }

                    Head head = new Head(id)
                            .withName(obj.get("name").getAsString())
                            .withUniqueId(uuid)
                            .withValue(obj.get("value").getAsString())
                            .withTags(obj.get("tags").isJsonNull() ? "None" : obj.get("tags").getAsString())
                            .withCategory(category);

                    id++;
                    heads.add(head);
                }

                long elapsed = (System.currentTimeMillis() - start);
                NBTEditor.LOGGER.debug(category.getName() + " -> Done! Time: " + elapsed + "ms (" + TimeUnit.MILLISECONDS.toSeconds(elapsed) + "s)");
            } catch (JsonParseException | IOException e) {
                NBTEditor.LOGGER.error("Failed to fetch heads (no-cache) | Stack Trace:");
                e.printStackTrace();
                return null;
            }

            updated = System.nanoTime();
            result.put(category, heads);
        }

        return result;
    }

    /**
     * Updates the cached heads
     *
     * @return Returns true if the update was successful.
     */
    public boolean update() {
        Map<Category, List<Head>> heads = getHeadsNoCache();
        if (heads == null) {
            NBTEditor.LOGGER.error("Failed to update database! Check above for any errors.");
            return false;
        }

        HEADS.clear();
        HEADS.putAll(heads);
        return true;
    }

    public long getLastUpdate() {
        long now = System.nanoTime();
        long elapsed = now - updated;
        return TimeUnit.NANOSECONDS.toSeconds(elapsed);
    }

    public boolean isLastUpdateOld() {
        return getLastUpdate() >= refresh;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public int getTimeout() {
        return timeout;
    }

    public long getRefresh() {
        return refresh;
    }

}
