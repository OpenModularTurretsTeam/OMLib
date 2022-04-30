package omtteam.omlib.api.render;

import net.minecraft.client.renderer.RenderGlobal;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import omtteam.omlib.api.render.object.RenderObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@SideOnly(Side.CLIENT)
public class RenderManager {
    private static RenderManager instance;
    private final List<RenderObject> renderListWorldLastEvent;

    private RenderManager() {
        renderListWorldLastEvent = new ArrayList<>();
    }

    public static RenderManager getInstance() {
        if (instance == null) {
            instance = new RenderManager();
        }
        return instance;
    }

    public void addRenderObjectToList(RenderObject object) {
        renderListWorldLastEvent.add(object);
    }

    public void renderWorldLastEvent(RenderGlobal renderGlobal, float partialTicks) {
        Iterator<RenderObject> iterator = renderListWorldLastEvent.iterator();
        while (iterator.hasNext()) {
            RenderObject renderObject = iterator.next();
            renderObject.render(renderGlobal, partialTicks);
            if (renderObject.isFinished()) {
                iterator.remove();
            }
        }
    }
}
