package com.panduuuh.hardcoreruns;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HardcoreRunsMetaData {
    private int theServerRunNumber;
    private final Map<UUID, Integer> thePlayerRunNumbers;
    private final Map<UUID, String> thePlayerIds;

    public HardcoreRunsMetaData() {
        theServerRunNumber = 1;
        thePlayerRunNumbers = new HashMap<>();
        thePlayerIds = new HashMap<>();
    }

    public int getServerRunNumber() {
        return theServerRunNumber;
    }

    public void incrementServerRunNumber() {
        theServerRunNumber += 1;
    }

    @Nullable
    public Integer getPlayerRunNumber(Player aPlayer) {
        return thePlayerRunNumbers.get(aPlayer.getUniqueId());
    }

    public void recordPlayer(Player aPlayer) {
        final UUID myPlayerId = aPlayer.getUniqueId();
        thePlayerRunNumbers.put(myPlayerId, theServerRunNumber);
        thePlayerIds.put(myPlayerId, aPlayer.getName());
    }
}