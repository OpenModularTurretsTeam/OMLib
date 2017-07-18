package omtteam.omlib.compatibility.opencomputers;

import li.cil.oc.api.Network;
import li.cil.oc.api.network.Visibility;
import li.cil.oc.api.prefab.ManagedEnvironment;

/**
 * Created by Keridos on 19/07/17.
 * This Class
 */
public class AbstractOMTileEntityEnvironment<T> extends ManagedEnvironment {
    protected final T tileEntity;

    public AbstractOMTileEntityEnvironment(final T tileEntity, final String name) {
        this.tileEntity = tileEntity;
        setNode(Network.newNode(this, Visibility.Network).
                withComponent(name).
                create());
    }
}
