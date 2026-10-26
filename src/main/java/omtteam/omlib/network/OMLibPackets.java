package omtteam.omlib.network;

import omtteam.omlib.OMLib;
import omtteam.omlib.compat.network.NetworkCompat;
import omtteam.omlib.compat.network.PacketType;

/**
 * The packet types of the library, shared by all mods that use it: the config sync, those of the camouflage menu and those of the trusted player screen.
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

    // Trusted player screen and disowning (blocks with an owner)
    public static final PacketType<TrustPackets.Add> ADD_TRUSTED_PLAYER = NetworkCompat.registerC2S(
            OMLib.id("add_trusted_player"), TrustPackets.Add::write, TrustPackets.Add::read,
            TrustPacketHandler::handleAdd);
    public static final PacketType<TrustPackets.Remove> REMOVE_TRUSTED_PLAYER = NetworkCompat.registerC2S(
            OMLib.id("remove_trusted_player"), TrustPackets.Remove::write, TrustPackets.Remove::read,
            TrustPacketHandler::handleRemove);
    public static final PacketType<TrustPackets.ChangeAccess> CHANGE_ACCESS_LEVEL = NetworkCompat.registerC2S(
            OMLib.id("change_access_level"), TrustPackets.ChangeAccess::write, TrustPackets.ChangeAccess::read,
            TrustPacketHandler::handleChangeAccess);
    public static final PacketType<TrustPackets.Disown> DISOWN = NetworkCompat.registerC2S(
            OMLib.id("disown"), TrustPackets.Disown::write, TrustPackets.Disown::read,
            TrustPacketHandler::handleDisown);

    private OMLibPackets() {
    }

    /** Loads the class, registering all packet types. */
    public static void init() {
    }
}
