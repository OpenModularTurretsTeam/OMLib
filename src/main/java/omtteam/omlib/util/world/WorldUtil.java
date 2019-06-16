package omtteam.omlib.util.world;

import com.google.common.base.Predicates;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Keridos on 20/07/16.
 * This Class implements some World related utility functions.
 */
@SuppressWarnings("unused")
public class WorldUtil {
    public static ArrayList<TileEntity> getTouchingTileEntities(World world, BlockPos pos) {
        ArrayList<TileEntity> list = new ArrayList<>();
        for (EnumFacing facing : EnumFacing.VALUES) {
            list.add(world.getTileEntity(pos.offset(facing)));
        }
        return list;
    }

    @SuppressWarnings("unchecked")
    public static <T> ArrayList<T> getTouchingTileEntitiesByClass(World world, BlockPos pos, Class<T> clazz) {
        ArrayList<T> list = new ArrayList<>();
        for (EnumFacing facing : EnumFacing.VALUES) {
            TileEntity tileEntity = world.getTileEntity(pos.offset(facing));
            if (clazz.isInstance(tileEntity)) {
                list.add((T) tileEntity);
            }
        }
        return list;
    }

    public static ArrayList<IBlockState> getTouchingBlockStates(World world, BlockPos pos) {
        ArrayList<IBlockState> list = new ArrayList<>();
        for (EnumFacing facing : EnumFacing.VALUES) {
            list.add(world.getBlockState(pos.offset(facing)));
        }
        return list;
    }

    public static List<RayTraceResult> traceEntities(Entity toIgnore, Vec3d base, Vec3d target, World world) {
        Entity pointedEntity = null;
        RayTraceResult result = null;
        Vec3d vec3d3 = null;
        AxisAlignedBB search = new AxisAlignedBB(base.x, base.y, base.z, target.x, target.y, target.z).grow(.5, .5, .5);
        @SuppressWarnings("Guava") List<Entity> list = world.getEntitiesInAABBexcluding(toIgnore, search, Predicates.and(EntitySelectors.NOT_SPECTATING, entity -> entity != null && entity.canBeCollidedWith()));
        List<RayTraceResult> resultList = new ArrayList<>();

        for (Entity entity1 : list) {
            AxisAlignedBB aabb = entity1.getEntityBoundingBox().grow(entity1.getCollisionBorderSize());
            RayTraceResult raytraceresult = aabb.calculateIntercept(base, target);

            if (aabb.contains(base)) {
                pointedEntity = entity1;
                vec3d3 = raytraceresult == null ? base : raytraceresult.hitVec;
            } else if (raytraceresult != null && raytraceresult.typeOfHit == RayTraceResult.Type.BLOCK) {
                pointedEntity = entity1;
                vec3d3 = raytraceresult.hitVec;
            }
            if (pointedEntity != null) {
                result = new RayTraceResult(pointedEntity, vec3d3);
            }
            if (result != null) {
                resultList.add(result);
            }
        }
        resultList.sort(Comparator.comparingDouble(o -> base.distanceTo(o.hitVec)));
        return resultList;
    }
}
