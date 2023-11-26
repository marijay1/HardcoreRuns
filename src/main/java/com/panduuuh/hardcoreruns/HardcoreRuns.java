package com.panduuuh.hardcoreruns;

import com.google.common.base.Stopwatch;
import com.google.gson.Gson;
import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.MVWorldManager;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import com.onarandombox.MultiverseNetherPortals.MultiverseNetherPortals;
import org.bukkit.*;
import org.bukkit.World.Environment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.*;
import org.jetbrains.annotations.Nullable;
import org.popcraft.chunky.api.ChunkyAPI;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static org.bukkit.World.Environment.*;

public class HardcoreRuns extends JavaPlugin implements Listener {
    private static final String DISCORD_WEBHOOK_URL = "https://discord.com/api/webhooks/986074325629153330/dJH7TM1lToreBkydYrbIYUNseeYdTX6DCsAVV-EMys0ug-WLD0j7tY3_Tzgi_Ovi0JHf";
    private static final String META_DATA_FILE_NAME = "hardcore_runs_meta_data.json";
    private static final Gson GSON = new Gson();

    private Stopwatch theStopWatch;
    private final MultiverseCore theCore;
    private final MultiverseNetherPortals theNetherPortals;
    private final MVWorldManager theWorldManager;
    private final HardcoreRunsMetaData theMetaData;

    private HardcoreRunWorld theCurrentWorld;
    private HardcoreRunWorld theNextWorld;

    public HardcoreRuns() {
        theStopWatch = Stopwatch.createUnstarted();
        theCore = (MultiverseCore) Bukkit.getServer().getPluginManager().getPlugin("Multiverse-Core");
        theNetherPortals = (MultiverseNetherPortals) Bukkit.getServer().getPluginManager().getPlugin("Multiverse-NetherPortals");
        theWorldManager = Objects.requireNonNull(theCore).getMVWorldManager();
        
        try {
            theMetaData = loadMetaData();
        } catch (IOException aException) {
            throw new RuntimeException(aException);
        }
    }

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        initializeWorlds();
        createScoreboard();
        theStopWatch.start();
    }

    @Override
    public void onDisable() {
        try {
            saveMetaData();
        } catch (IOException aException) {
            throw new RuntimeException(aException);
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent aPlayerJoinEvent) {
        final Player myPlayer = aPlayerJoinEvent.getPlayer();
        @Nullable final Integer myPlayerRunNumber = theMetaData.getPlayerRunNumber(myPlayer);
        if (myPlayerRunNumber != null && myPlayerRunNumber != theMetaData.getServerRunNumber()) {
            resetPlayer(myPlayer);
            myPlayer.getInventory().clear();
        }

        myPlayer.teleport(theWorldManager.getMVWorld(theCurrentWorld.normalWorld().getCBWorld()).getSpawnLocation());
        theMetaData.recordPlayer(myPlayer);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent aPlayerDeathEvent) {
        endCurrentRun(aPlayerDeathEvent);
        triggerNewRun(aPlayerDeathEvent.getEntity());
    }

    private void endCurrentRun(PlayerDeathEvent aPlayerDeathEvent) {
        theStopWatch.stop();

        final String myElapsedRunTime = formatElapsedRunTime();
        for (Player myOnlinePlayer : Bukkit.getOnlinePlayers()) {
            myOnlinePlayer.sendTitle("This run has ended after: " + myElapsedRunTime, aPlayerDeathEvent.getDeathMessage(), 5, 20, 5);
        }

        aPlayerDeathEvent.getDrops().clear();
        sendDiscordMessage(aPlayerDeathEvent);
    }

    private void triggerNewRun(Player aDeadPlayer) {
        resetPlayersToNewWorld(aDeadPlayer);
        theStopWatch.reset();
        theStopWatch.start();
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent aPlayerRespawnEvent) {
        Player myPlayer = aPlayerRespawnEvent.getPlayer();

        World myWorld = theCurrentWorld.normalWorld().getCBWorld();
        aPlayerRespawnEvent.setRespawnLocation(myWorld.getSpawnLocation());
        resetPlayer(myPlayer);
    }

    private HardcoreRunsMetaData loadMetaData() throws IOException {
        final File myDataFolder = getDataFolder();
        if (!myDataFolder.exists()) {
            myDataFolder.mkdir();
        }

        File myMetaDataFile = new File(myDataFolder, META_DATA_FILE_NAME);
        if (!myMetaDataFile.exists()) {
            myMetaDataFile.createNewFile();
        }

        HardcoreRunsMetaData myMetaData;
        try (FileReader myReader = new FileReader(myMetaDataFile)) {
            myMetaData = GSON.fromJson(myReader, HardcoreRunsMetaData.class);
        }

        if (myMetaData == null) {
            myMetaData = new HardcoreRunsMetaData();
        }

        return myMetaData;
    }

    private void saveMetaData() throws IOException {
        final String myJson = GSON.toJson(theMetaData);
        final File myMetaDataFile = new File(getDataFolder(), META_DATA_FILE_NAME);
        try (FileWriter myWriter = new FileWriter(myMetaDataFile)) {
            myWriter.write(myJson);
        }
    }

    private void initializeWorlds() {
        final String myCurrentNormalWorldName = resolveWorldName(NORMAL, theMetaData.getServerRunNumber());
        final String myCurrentNetherWorldName = resolveWorldName(NETHER, theMetaData.getServerRunNumber());
        final String myCurrentEndWorldName = resolveWorldName(THE_END, theMetaData.getServerRunNumber());

        if (!theWorldManager.isMVWorld(myCurrentNormalWorldName)) {
            createWorld(NORMAL, theMetaData.getServerRunNumber());
            createWorld(NETHER, theMetaData.getServerRunNumber());
            createWorld(THE_END, theMetaData.getServerRunNumber());
            linkWorlds(myCurrentNormalWorldName, myCurrentNetherWorldName, myCurrentEndWorldName);
        }

        theCurrentWorld = new HardcoreRunWorld(theWorldManager.getMVWorld(myCurrentNormalWorldName),
                theWorldManager.getMVWorld(myCurrentNetherWorldName),
                theWorldManager.getMVWorld(myCurrentEndWorldName));

        final int myNextServerRunNumber = theMetaData.getServerRunNumber() + 1;
        final String myNextNormalWorldName = resolveWorldName(NORMAL, myNextServerRunNumber);
        final String myNextNetherWorldName = resolveWorldName(NETHER, myNextServerRunNumber);
        final String myNextEndWorldName = resolveWorldName(THE_END, myNextServerRunNumber);

        if (!theWorldManager.isMVWorld(myNextNormalWorldName)) {
            createWorld(NORMAL, myNextServerRunNumber);
            createWorld(NETHER, myNextServerRunNumber);
            createWorld(THE_END, myNextServerRunNumber);
            linkWorlds(myNextNormalWorldName, myNextNetherWorldName, myNextEndWorldName);
        }

        theNextWorld = new HardcoreRunWorld(theWorldManager.getMVWorld(myNextNormalWorldName),
                theWorldManager.getMVWorld(myNextNetherWorldName),
                theWorldManager.getMVWorld(myNextEndWorldName));
    }

    private void updateWorlds() {
        theWorldManager.deleteWorld(theCurrentWorld.normalWorld().getName(), true, true);
        theWorldManager.deleteWorld(theCurrentWorld.netherWorld().getName(), true, true);
        theWorldManager.deleteWorld(theCurrentWorld.endWorld().getName(), true, true);
        theCurrentWorld = theNextWorld;

        final int myNextServerRunNumber = theMetaData.getServerRunNumber() + 1;
        final String myNextNormalWorldName = resolveWorldName(NORMAL, myNextServerRunNumber);
        final String myNextNetherWorldName = resolveWorldName(NETHER, myNextServerRunNumber);
        final String myNextEndWorldName = resolveWorldName(THE_END, myNextServerRunNumber);

        createWorld(NORMAL, myNextServerRunNumber);
        createWorld(NETHER, myNextServerRunNumber);
        createWorld(THE_END, myNextServerRunNumber);
        linkWorlds(myNextNormalWorldName, myNextNetherWorldName, myNextEndWorldName);

        theNextWorld = new HardcoreRunWorld(theWorldManager.getMVWorld(myNextNormalWorldName),
                theWorldManager.getMVWorld(myNextNetherWorldName),
                theWorldManager.getMVWorld(myNextEndWorldName));
    }

    private void createWorld(Environment anEnvironment, int aRunNumber) {
        String myWorldName = resolveWorldName(anEnvironment, aRunNumber);
        theWorldManager.addWorld(myWorldName, anEnvironment,null, WorldType.NORMAL,true, null);
        theWorldManager.getMVWorld(myWorldName).setAdjustSpawn(true);
        if (anEnvironment == NORMAL) {
            preGenerateWorld(myWorldName);
        }
    }

    private String resolveWorldName(Environment anEnvironment, int aRunNumber) {
        return String.format("world_run_%s_%s", aRunNumber, anEnvironment.name().toLowerCase());
    }

    private void linkWorlds(String anOverWorld, String aNetherWorld, String anEndWorld) {
        theNetherPortals.addWorldLink(anOverWorld, aNetherWorld, PortalType.NETHER);
        theNetherPortals.addWorldLink(aNetherWorld, anOverWorld, PortalType.NETHER);

        theNetherPortals.addWorldLink(anOverWorld, anEndWorld, PortalType.ENDER);
        theNetherPortals.addWorldLink(anEndWorld, anOverWorld, PortalType.ENDER);
    }

    private void createScoreboard() {
        ScoreboardManager scoreboardManager = Bukkit.getScoreboardManager();
        Scoreboard scoreboard = scoreboardManager.getMainScoreboard();
        Objective healthObjective = scoreboard.getObjective("health");

        if (healthObjective == null) {
            healthObjective = scoreboard.registerNewObjective("health","health", Criterias.HEALTH);
        }

        healthObjective.setDisplaySlot(DisplaySlot.SIDEBAR);
        healthObjective.setDisplayName(String.format("Run #%d", theMetaData.getServerRunNumber()));
    }

    private void resetPlayersToNewWorld(Player aDeadPlayer) {
        theMetaData.incrementServerRunNumber();
        updateWorlds();

        for (Player myPlayer : Bukkit.getOnlinePlayers()) {
            if (myPlayer != aDeadPlayer) {
                final World myWorld = theCurrentWorld.normalWorld().getCBWorld();
                myPlayer.teleport(myWorld.getSpawnLocation());
                resetPlayer(myPlayer);
            }
            theMetaData.recordPlayer(myPlayer);
        }
    }

    private void resetPlayer(Player aPlayer) {
        aPlayer.setHealth(20);
        aPlayer.setFoodLevel(20);
        aPlayer.setExp(0);
        aPlayer.setLevel(0);
        aPlayer.getActivePotionEffects().clear();
        aPlayer.setGameMode(GameMode.SURVIVAL);
    }

    private void preGenerateWorld(String aWorldName) {
        ChunkyAPI myChunkyAPI = Bukkit.getServer().getServicesManager().load(ChunkyAPI.class);
        if (myChunkyAPI != null) {
            myChunkyAPI.startTask(aWorldName, "square", 0, 0, 500, 500, "concentric");
        }
    }

    private void sendDiscordMessage(PlayerDeathEvent aDeathEvent) {
        DiscordWebhook myDiscordWebhook = new DiscordWebhook(DISCORD_WEBHOOK_URL);
        myDiscordWebhook.setAvatarUrl("https://imgur.com/a/kqJkXWI");
        myDiscordWebhook.setUsername("Hardcore Runs");
        myDiscordWebhook.addEmbed(new DiscordWebhook.EmbedObject()
                .setAuthor("Stupidest Idiot Award goes to...", null, null)
                .setTitle(aDeathEvent.getEntity().getName())
                .addField(aDeathEvent.getDeathMessage(), String.format("They ruined run #%d after %d minute(s) and %d second(s)", theMetaData.getServerRunNumber(), theStopWatch.elapsed(TimeUnit.MINUTES), theStopWatch.elapsed(TimeUnit.SECONDS)), false)
                .setImage("https://i.imgur.com/SNlTTOQ.jpeg")
                .setThumbnail("https://i.imgur.com/qHS1Luk.png")
                .setFooter("Look at this stupid fucking muppet", null)
                .setColor(java.awt.Color.MAGENTA));
        try {
            myDiscordWebhook.execute();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String formatElapsedRunTime() {
        long mySeconds = theStopWatch.elapsed(TimeUnit.SECONDS);
        long myMinutes = theStopWatch.elapsed(TimeUnit.MINUTES);
        long myHours = theStopWatch.elapsed(TimeUnit.HOURS);

        if (myHours > 0) {
            return String.format("%d:%02d:%02d", myHours, myMinutes % 60, mySeconds % 60);
        } else {
            return String.format("%02d:%02d", myMinutes, mySeconds % 60);
        }
    }

    private record HardcoreRunWorld(MultiverseWorld normalWorld, MultiverseWorld netherWorld, MultiverseWorld endWorld) {}
}
