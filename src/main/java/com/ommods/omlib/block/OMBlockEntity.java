package com.ommods.omlib.block;

import com.ommods.omlib.util.OMPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public abstract class OMBlockEntity extends BlockEntity {
    protected OMPlayer owner;

    public OMBlockEntity(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
        super(pType, pPos, pBlockState);
    }


    public OMPlayer getOwner() {
        return owner;
    }

    public void setOwner(OMPlayer owner) {
        this.owner = owner;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        super.deserializeNBT(tag);
        this.owner = new OMPlayer();
        this.owner.deserializeNBT(tag.getCompound("owner"));
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = super.serializeNBT();
        tag.put("owner", this.owner.serializeNBT());
        return tag;
    }
}
