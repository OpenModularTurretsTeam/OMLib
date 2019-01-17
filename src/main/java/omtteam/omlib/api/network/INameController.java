package omtteam.omlib.api.network;

/**
 * Use this to set a name for the network.
 * Only one of these can access per network.
 */
public interface INameController extends IController {
    /**
     * Return the name for the network.
     *
     * @return if loaded and existing, the device, null otherwise
     */
    public String getNetworkName();
}
