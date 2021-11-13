package gg.moonflower.pollen.api.sync;

import dev.architectury.injectables.annotations.ExpectPlatform;
import gg.moonflower.pollen.api.event.EventDispatcher;
import gg.moonflower.pollen.api.event.EventListener;
import gg.moonflower.pollen.api.event.events.lifecycle.TickEvent;
import gg.moonflower.pollen.api.platform.Platform;
import gg.moonflower.pollen.core.network.login.ClientboundSyncPlayerDataKeysPacket;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.ApiStatus;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Manages data on players and syncs its data to clients based on specified properties.
 * <p>System based on <a href=https://github.com/MrCrayfish/Obfuscate/blob/1.16.X/src/main/java/com/mrcrayfish/obfuscate/common/data/SyncedPlayerData.java>Obfuscate</a> but modified to work cleaner on multiple platforms.
 *
 * @author Ocelot
 * @since 1.0.0
 **/
public class SyncedDataManager {

    private static final Map<ResourceLocation, SyncedDataKey<?>> REGISTERED_KEYS = new HashMap<>();
    private static final Map<Integer, SyncedDataKey<?>> KEY_LOOKUP = new Int2ObjectArrayMap<>();
    private static final Map<Integer, SyncedDataKey<?>> CLIENT_KEY_LOOKUP = new Int2ObjectArrayMap<>();
    private static int nextId;
    private static boolean dirty;

    private static Map<Integer, SyncedDataKey<?>> getKeyLookup() {
        return !CLIENT_KEY_LOOKUP.isEmpty() ? CLIENT_KEY_LOOKUP : KEY_LOOKUP;
    }

    @ApiStatus.Internal
    public static void init() {
        EventDispatcher.register(SyncedDataManager.class);
    }

    @EventListener
    @ApiStatus.Internal
    public static void onPlayerTick(TickEvent.LivingEntityEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer) || !dirty)
            return;
        sync((ServerPlayer) event.getEntity());
    }

    @EventListener
    @ApiStatus.Internal
    public static void onServerTick(TickEvent.ServerEvent.Post event) {
        dirty = false;
    }

    @ApiStatus.Internal
    @ExpectPlatform
    public static void sync(ServerPlayer player) {
        Platform.error();
    }

    @ApiStatus.Internal
    public static void syncKeys(ClientboundSyncPlayerDataKeysPacket pkt) {
        CLIENT_KEY_LOOKUP.clear();
        pkt.getMappings().forEach((name, id) -> CLIENT_KEY_LOOKUP.put(id, byName(name)));
    }

    @ApiStatus.Internal
    public static void markDirty() {
        dirty = true;
    }

    public static synchronized void register(SyncedDataKey<?> key) {
        if (REGISTERED_KEYS.put(key.getKey(), key) != null)
            throw new IllegalStateException("Duplicate data key: " + key.getKey());
        KEY_LOOKUP.put(nextId++, key);
    }

    @ExpectPlatform
    public static <T> void set(Player player, SyncedDataKey<T> key, T value) {
        Platform.error();
    }

    @ExpectPlatform
    public static <T> T get(Player player, SyncedDataKey<T> key) {
        return Platform.error();
    }

    public static SyncedDataKey<?> byName(ResourceLocation name) {
        if (!REGISTERED_KEYS.containsKey(name))
            throw new IllegalStateException("Unknown synced data key: " + name);
        return REGISTERED_KEYS.get(name);
    }

    public static int getId(SyncedDataKey<?> key) {
        return getKeyLookup().entrySet().stream().filter(entry -> entry.getValue() == key).mapToInt(Map.Entry::getKey).findFirst().orElseThrow(() -> new IllegalStateException("Attempted to get id of unregistered key: " + key.getKey()));
    }

    public static SyncedDataKey<?> byId(int id) {
        if (!getKeyLookup().containsKey(id))
            throw new IllegalStateException("Unknown synced data key with id: " + id);
        return getKeyLookup().get(id);
    }

    public static Stream<Integer> getIds() {
        return getKeyLookup().keySet().stream();
    }
}
