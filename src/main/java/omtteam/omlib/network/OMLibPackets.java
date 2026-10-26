package omtteam.omlib.network;

import omtteam.omlib.OMLib;
import omtteam.omlib.compat.network.NetworkCompat;
import omtteam.omlib.compat.network.PacketType;

/**
 * The packet types of the library, shared by all mods that use it: the config sync and those of the camouflage menu.
 */
public final class OMLibPackets {
    public static final PacketType<ConfigSyncPacket> CONFIG_SYNC =
            NetworkCompat.registerS2C(OMLib.id("config_sync"), ConfigSyncPacket::write, ConfigSyncPacket::read);

    public static final PacketType<CamoPackets.SetLight> CAMO_LIGHT = NetworkCompat.registerC2S(
            OMLib.id("camo_light"), CamoPackets.SetLight::write, CamoPackets.SetLight::read,
            CamoPacketHandler::handleSetLight);

    public static final PacketType<CamoPackets.OpenMenu> OPEN_CAMO_MENU = NetworkCompat.registerC2S(
            OMLib.id("open_camo_menu"), CamoPackets.OpenMenu::write, CamoPackets.OpenMenu::read,
            CamoPacketHandler::handleOpenMenu);

    public static final PacketType<CamoPackets.Back> CAMO_BACK = NetworkCompat.registerC2S(
            OMLib.id("camo_back"), CamoPackets.Back::write, CamoPackets.Back::read,
            CamoPacketHandler::handleBack);

    private OMLibPackets() {
    }

    /** Loads the class, registering all packet types. */
    public static void init() {
    }
}
