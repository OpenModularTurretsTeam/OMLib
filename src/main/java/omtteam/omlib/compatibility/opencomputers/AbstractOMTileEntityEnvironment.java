package omtteam.omlib.compatibility.opencomputers;

import li.cil.oc.api.Network;
import li.cil.oc.api.network.ManagedEnvironment;
import li.cil.oc.api.network.Message;
import li.cil.oc.api.network.Node;
import li.cil.oc.api.network.Visibility;
import net.minecraft.nbt.NBTTagCompound;

/**
 * Created by Keridos on 19/07/17.
 * This Class
 */
public abstract class AbstractOMTileEntityEnvironment<T> implements ManagedEnvironment {
    public static final String NODE_TAG = "node";
    protected final T tileEntity;
    private Node _node;

    public AbstractOMTileEntityEnvironment(final T tileEntity, final String name) {
        this.tileEntity = tileEntity;
        setNode(Network.newNode(this, Visibility.Network).
                withComponent(name).
                create());
    }

    public Node node() {
        return this._node;
    }

    protected void setNode(Node value) {
        this._node = value;
    }

    public boolean canUpdate() {
        return false;
    }

    public void update() {
    }

    public void onConnect(Node node) {
    }

    public void onDisconnect(Node node) {
    }

    public void onMessage(Message message) {
    }

    public void load(NBTTagCompound nbt) {
        if (this.node() != null) {
            this.node().load(nbt.getCompoundTag("node"));
        }

    }

    public void save(NBTTagCompound nbt) {
        if (this.node() != null) {
            NBTTagCompound nodeTag;
            if (this.node().address() == null) {
                Network.joinNewNetwork(this.node());
                nodeTag = new NBTTagCompound();
                this.node().save(nodeTag);
                nbt.setTag("node", nodeTag);
                this.node().remove();
            } else {
                nodeTag = new NBTTagCompound();
                this.node().save(nodeTag);
                nbt.setTag("node", nodeTag);
            }
        }

    }
}
