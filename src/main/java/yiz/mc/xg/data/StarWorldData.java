package yiz.mc.xg.data;

import java.util.UUID;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

public class StarWorldData extends SavedData {
    private static final String DATA_NAME = "yizxgmod_star";

    private UUID starBodyPlayerUUID;

    public StarWorldData() {
        this.starBodyPlayerUUID = null;
    }

    public boolean hasStarBodyClaimed() {
        return starBodyPlayerUUID != null;
    }

    public UUID getStarBodyPlayerUUID() {
        return starBodyPlayerUUID;
    }

    public void claimStarBody(Player player) {
        this.starBodyPlayerUUID = player.getUUID();
        setDirty();
    }

    public static StarWorldData get(Level level) {
        if (level.getServer() != null) {
            ServerLevel overworld = level.getServer().getLevel(Level.OVERWORLD);
            if (overworld != null) {
                return overworld.getDataStorage().computeIfAbsent(
                        new SavedData.Factory<>(StarWorldData::new, StarWorldData::load),
                        DATA_NAME
                );
            }
        }
        return new StarWorldData();
    }

    public static StarWorldData load(CompoundTag tag, HolderLookup.Provider provider) {
        StarWorldData data = new StarWorldData();
        if (tag.contains("StarBodyPlayer")) {
            data.starBodyPlayerUUID = tag.getUUID("StarBodyPlayer");
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        if (starBodyPlayerUUID != null) {
            tag.putUUID("StarBodyPlayer", starBodyPlayerUUID);
        }
        return tag;
    }
}
