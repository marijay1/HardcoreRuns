package com.panduuuh.hardcoreRuns.core;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DiscordService {
    private final ConfigurationManager config;
    private final Plugin plugin;

    public DiscordService(ConfigurationManager config, Plugin plugin) {
        this.config = config;
        this.plugin = plugin;
    }

    public void sendDeathAlert(Player player, long timeAlive, String deathMessage) {
        String webhookUrl = config.getWebhookUrl();
        if (webhookUrl.isEmpty()) return;

        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    JsonObject payload = createPayload(player, timeAlive, deathMessage);
                    sendWebhookRequest(webhookUrl, payload);
                } catch (Exception e) {
                    plugin.getLogger().warning("Discord alert failed: " + e.getMessage());
                }
            }
        }.runTaskAsynchronously(plugin);
    }

    private JsonObject createPayload(Player player, long timeAlive, String deathMessage) {
        JsonObject payload = new JsonObject();
        payload.addProperty("username", "Hardcore Runs");
        payload.addProperty("avatar_url", "https://imgur.com/a/kqJkXWI");

        JsonObject embed = new JsonObject();
        embed.addProperty("title", player.getName());
        embed.addProperty("color", 0xFF00FF);

        JsonObject author = new JsonObject();
        author.addProperty("name", "Stupidest Idiot Award goes to...");
        embed.add("author", author);

        JsonArray fields = new JsonArray();
        JsonObject field = new JsonObject();
        field.addProperty("name", deathMessage);
        field.addProperty("value", String.format("They ruined run #%d after %s",
                config.getAttempts(), formatTime(timeAlive)));
        field.addProperty("inline", false);
        fields.add(field);
        embed.add("fields", fields);

        JsonObject footer = new JsonObject();
        footer.addProperty("text", "Look at this stupid fucking muppet");
        embed.add("footer", footer);

        JsonObject image = new JsonObject();
        image.addProperty("url", "https://i.imgur.com/SNlTTOQ.jpeg");
        embed.add("image", image);

        JsonObject thumbnail = new JsonObject();
        thumbnail.addProperty("url", "https://i.imgur.com/qHS1Luk.png");
        embed.add("thumbnail", thumbnail);

        JsonArray embeds = new JsonArray();
        embeds.add(embed);
        payload.add("embeds", embeds);

        return payload;
    }

    private void sendWebhookRequest(String webhookUrl, JsonObject payload) {
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(webhookUrl).openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(payload.toString().getBytes());
            }

            if (conn.getResponseCode() >= 400) {
                plugin.getLogger().warning("Discord webhook failed: " + conn.getResponseMessage());
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to send Discord alert: " + e.getMessage());
        }
    }

    private String formatTime(long milliseconds) {
        long totalSeconds = milliseconds / 1000;
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        if (hours > 0) {
            return String.format("%d hours %d minutes", hours, minutes);
        } else if (minutes > 0) {
            return String.format("%d minutes %d seconds", minutes, seconds);
        } else {
            return String.format("%d seconds", seconds);
        }
    }
}