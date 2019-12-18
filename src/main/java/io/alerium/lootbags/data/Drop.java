package io.alerium.lootbags.data;

import org.bukkit.entity.EntityType;

public class Drop {

    private EntityType entityType;
    private Integer chance;

    public Drop(EntityType entityType, Integer chance) {
        this.entityType = entityType;
        this.chance = chance;
    }

    public EntityType getEntityType() {
        return entityType;
    }

    public Integer getChance() {
        return chance;
    }
}
