package com.panduuuh.hardcoreRuns.core;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.util.Base64;
import java.util.UUID;
import java.util.HashMap;
import java.util.Map;

public class SharedInventoryManager {
    private final Plugin plugin;
    private final Logger logger;
    private final ConfigurationManager config;
    private final Map<UUID, Inventory> sharedInventories = new HashMap<>();
    private final File dataFolder;
    private final File sharedInventoryFile;
    private final JSONParser jsonParser = new JSONParser();

    public SharedInventoryManager(Plugin plugin, ConfigurationManager config, Logger logger) {
        this.plugin = plugin;
        this.config = config;
        this.logger = logger;
        this.dataFolder = plugin.getDataFolder();
        this.sharedInventoryFile = new File(dataFolder, "shared_inventories.json");
    }

    public void initialize() {
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }

        if (!sharedInventoryFile.exists()) {
            try {
                sharedInventoryFile.createNewFile();
                saveSharedInventories();
            } catch (IOException e) {
                logger.severe("Failed to create shared inventory file: " + e.getMessage());
            }
        } else {
            loadSharedInventories();
        }
    }

    private void loadSharedInventories() {
        if (!sharedInventoryFile.exists() || sharedInventoryFile.length() == 0) {
            return;
        }

        try (FileReader reader = new FileReader(sharedInventoryFile)) {
            JSONObject jsonObject = (JSONObject) jsonParser.parse(reader);
            String currentRunId = (String) jsonObject.get("currentRunId");

            if (currentRunId != null && currentRunId.equals(config.getActiveWorld())) {
                JSONArray inventoriesArray = (JSONArray) jsonObject.get("inventories");

                if (inventoriesArray != null) {
                    for (Object obj : inventoriesArray) {
                        JSONObject inventoryObj = (JSONObject) obj;
                        UUID id = UUID.fromString((String) inventoryObj.get("id"));
                        String encodedInventory = (String) inventoryObj.get("data");

                        try {
                            ItemStack[] contents = deserializeInventory(encodedInventory);
                            Inventory inventory = Bukkit.createInventory(null, contents.length, "Shared Inventory");
                            inventory.setContents(contents);
                            sharedInventories.put(id, inventory);
                            logger.info("Loaded shared inventory: " + id);
                        } catch (Exception e) {
                            logger.warning("Failed to load shared inventory: " + e.getMessage());
                        }
                    }
                }
            } else {
                logger.info("Run ID has changed. Starting with fresh shared inventories.");
                sharedInventories.clear();
            }
        } catch (IOException | ParseException e) {
            logger.severe("Error loading shared inventories: " + e.getMessage());
        }
    }

    public void saveSharedInventories() {
        JSONObject root = new JSONObject();
        root.put("currentRunId", config.getActiveWorld());

        JSONArray inventoriesArray = new JSONArray();
        for (Map.Entry<UUID, Inventory> entry : sharedInventories.entrySet()) {
            JSONObject inventoryObj = new JSONObject();
            inventoryObj.put("id", entry.getKey().toString());
            inventoryObj.put("data", serializeInventory(entry.getValue().getContents()));
            inventoriesArray.add(inventoryObj);
        }

        root.put("inventories", inventoriesArray);

        try (FileWriter writer = new FileWriter(sharedInventoryFile)) {
            writer.write(root.toJSONString());
            writer.flush();
        } catch (IOException e) {
            logger.severe("Failed to save shared inventories: " + e.getMessage());
        }
    }

    private String serializeInventory(ItemStack[] contents) {
        if (contents == null) {
            logger.warning("Attempted to serialize null inventory contents");
            return "";
        }
        
        try (ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
             BukkitObjectOutputStream bukkitOut = new BukkitObjectOutputStream(byteStream)) {

            bukkitOut.writeInt(contents.length);
            for (ItemStack item : contents) {
                bukkitOut.writeObject(item);
            }

            return Base64.getEncoder().encodeToString(byteStream.toByteArray());
        } catch (IOException e) {
            logger.severe("Failed to serialize inventory: " + e.getMessage());
            return "";
        }
    }


    private ItemStack[] deserializeInventory(String encoded) {
        try (ByteArrayInputStream byteStream = new ByteArrayInputStream(Base64.getDecoder().decode(encoded));
             BukkitObjectInputStream bukkitIn = new BukkitObjectInputStream(byteStream)) {

            int length = bukkitIn.readInt();
            ItemStack[] items = new ItemStack[length];
            for (int i = 0; i < length; i++) {
                items[i] = (ItemStack) bukkitIn.readObject();
            }

            return items;
        } catch (IOException | ClassNotFoundException e) {
            logger.severe("Failed to deserialize inventory: " + e.getMessage());
            return new ItemStack[0];
        }
    }


    public void updatePlayerInventory(Player source) {
        if (!config.isInventoryShared()) {
            return;
        }

        UUID sharedId = UUID.fromString(config.getSharedInventoryId());
        Inventory sharedInventory = getOrCreateSharedInventory(sharedId, 36);

        ItemStack[] contents = source.getInventory().getContents();
        sharedInventory.setContents(contents);

        Bukkit.getOnlinePlayers().forEach(player -> {
            if (player != source) {
                player.getInventory().setContents(contents);
            }
        });
    }

    public void syncPlayerWithSharedInventory(Player player) {
        if (!config.isInventoryShared()) {
            return;
        }

        UUID sharedId = UUID.fromString(config.getSharedInventoryId());
        Inventory sharedInventory = getOrCreateSharedInventory(sharedId, 36);
        player.getInventory().setContents(sharedInventory.getContents());
    }

    private Inventory getOrCreateSharedInventory(UUID id, int size) {
        if (!sharedInventories.containsKey(id)) {
            Inventory inventory = Bukkit.createInventory(null, size, "Shared Inventory");
            sharedInventories.put(id, inventory);
        }
        return sharedInventories.get(id);
    }

    public void shutdown() {
        saveSharedInventories();
    }

    public void reset() {
        sharedInventories.clear();
        saveSharedInventories();
    }

    public Plugin getPlugin() {
        return plugin;
    }
}