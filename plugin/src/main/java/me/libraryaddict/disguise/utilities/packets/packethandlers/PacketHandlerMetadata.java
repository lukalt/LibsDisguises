package me.libraryaddict.disguise.utilities.packets.packethandlers;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.WrappedDataValue;
import com.comphenix.protocol.wrappers.WrappedWatchableObject;
import me.libraryaddict.disguise.DisguiseConfig;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.utilities.packets.IPacketHandler;
import me.libraryaddict.disguise.utilities.packets.LibsPackets;
import me.libraryaddict.disguise.utilities.packets.PacketsHandler;
import me.libraryaddict.disguise.utilities.reflection.NmsVersion;
import me.libraryaddict.disguise.utilities.reflection.ReflectionManager;
import me.libraryaddict.disguise.utilities.reflection.WatcherValue;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by libraryaddict on 3/01/2019.
 */
public class PacketHandlerMetadata implements IPacketHandler {
    private PacketsHandler packetsHandler;

    public PacketHandlerMetadata(PacketsHandler packetsHandler) {
        this.packetsHandler = packetsHandler;
    }

    @Override
    public PacketType[] getHandledPackets() {
        return new PacketType[]{PacketType.Play.Server.ENTITY_METADATA};
    }

    @Override
    public void handle(Disguise disguise, PacketContainer sentPacket, LibsPackets packets, Player observer, Entity entity) {
        packets.clear();

        if (!DisguiseConfig.isMetaPacketsEnabled()) {
            return;
        }

        List<WatcherValue> watcherValues = WatcherValue.getValues(disguise.getWatcher(), sentPacket);

        List<WatcherValue> watchableObjects = disguise.getWatcher().convert(observer, watcherValues);

        if (watchableObjects.isEmpty()) {
            return;
        }

        PacketContainer metaPacket = ReflectionManager.getMetadataPacket(entity.getEntityId(), watchableObjects);

        packets.addPacket(metaPacket);
    }
}

