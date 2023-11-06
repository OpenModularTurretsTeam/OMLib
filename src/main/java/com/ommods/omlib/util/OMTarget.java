package com.ommods.omlib.util;

import net.minecraft.world.entity.LivingEntity;

public class OMTarget {

    protected TargetType targetType;
    protected float HP;
    protected float maxHP;
    protected int armor;

    public OMTarget(LivingEntity entity) {
        this.HP = entity.getHealth();
        this.maxHP = entity.getMaxHealth();
        this.armor = entity.getArmorValue();
    }

    public OMTarget() {
    }

    public float getHP() {
        return HP;
    }

    public float getMaxHP() {
        return maxHP;
    }

    public int getArmor() {
        return armor;
    }

    public TargetType getTargetType() {
        return targetType;
    }

    public void setTargetType(TargetType targetType) {
        this.targetType = targetType;
    }
}
