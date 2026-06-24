package dev.customclaims.protection.service;

import dev.customclaims.core.api.model.TerritoryStatus;
import dev.customclaims.core.service.TerritoryService;
import dev.customclaims.protection.config.ProtectionConfig;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Drowned;
import net.minecraft.world.entity.monster.Husk;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.monster.ZombieVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.WanderingTrader;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.ChunkPos;

public final class VillagerProtectionService {
    private final TerritoryService territoryService;

    public VillagerProtectionService(TerritoryService territoryService) {
        this.territoryService = territoryService;
    }

    public boolean shouldBlockDamage(LivingEntity entity, DamageSource source) {
        if (!ProtectionConfig.ENABLE_VILLAGER_PROTECTION.get()) {
            return false;
        }
        if (!isProtectedEntity(entity)) {
            return false;
        }
        if (ProtectionConfig.PROTECT_VILLAGERS_ONLY_ON_CLAIMS.get() && !isOnClaim(entity)) {
            return false;
        }
        if (isWarChunk(entity) && !ProtectionConfig.PROTECT_VILLAGERS_IN_WAR_CHUNKS.get()) {
            return false;
        }
        if (isZombieDamage(source) && ProtectionConfig.ALLOW_ZOMBIE_DAMAGE.get()) {
            return false;
        }
        if (isWarChunk(entity) && isPlayerDamage(source)
                && ProtectionConfig.ALLOW_PLAYER_DAMAGE_TO_VILLAGERS_IN_WAR_CHUNKS.get()) {
            return false;
        }
        if (isWarChunk(entity) && isExplosionDamage(source)
                && ProtectionConfig.ALLOW_EXPLOSION_DAMAGE_TO_VILLAGERS_IN_WAR_CHUNKS.get()) {
            return false;
        }

        return (isPlayerDamage(source) && ProtectionConfig.PREVENT_PLAYER_DAMAGE.get())
                || (isFireDamage(source) && ProtectionConfig.PREVENT_FIRE_DAMAGE.get())
                || (source.is(DamageTypes.LAVA) && ProtectionConfig.PREVENT_LAVA_DAMAGE.get())
                || (source.is(DamageTypes.FALL) && ProtectionConfig.PREVENT_FALL_DAMAGE.get())
                || (source.is(DamageTypes.DROWN) && ProtectionConfig.PREVENT_DROWNING_DAMAGE.get())
                || (source.is(DamageTypes.IN_WALL) && ProtectionConfig.PREVENT_SUFFOCATION_DAMAGE.get())
                || (isExplosionDamage(source) && ProtectionConfig.PREVENT_EXPLOSION_DAMAGE.get())
                || (isProjectileDamage(source) && ProtectionConfig.PREVENT_PROJECTILE_DAMAGE.get())
                || (source.is(DamageTypes.FELL_OUT_OF_WORLD) && ProtectionConfig.PREVENT_VOID_DAMAGE.get());
    }

    public void afterBlockedDamage(LivingEntity entity) {
        if (ProtectionConfig.CLEAR_FIRE_ON_BLOCKED_DAMAGE.get()) {
            entity.clearFire();
        }
    }

    private boolean isProtectedEntity(LivingEntity entity) {
        return entity instanceof Villager
                || (ProtectionConfig.PROTECT_WANDERING_TRADERS.get() && entity instanceof WanderingTrader);
    }

    private boolean isOnClaim(LivingEntity entity) {
        if (!(entity.level() instanceof ServerLevel level)) {
            return false;
        }
        return territoryService.getStatus(level, new ChunkPos(entity.blockPosition())) != TerritoryStatus.UNCLAIMED;
    }

    private boolean isWarChunk(LivingEntity entity) {
        if (!(entity.level() instanceof ServerLevel level)) {
            return false;
        }
        return territoryService.getStatus(level, new ChunkPos(entity.blockPosition())) == TerritoryStatus.WAR_CONTESTED;
    }

    private boolean isPlayerDamage(DamageSource source) {
        return source.getEntity() instanceof Player;
    }

    private boolean isZombieDamage(DamageSource source) {
        Entity entity = source.getEntity();
        return entity instanceof Zombie
                || entity instanceof ZombieVillager
                || entity instanceof Husk
                || entity instanceof Drowned;
    }

    private boolean isFireDamage(DamageSource source) {
        return source.is(DamageTypes.IN_FIRE) || source.is(DamageTypes.ON_FIRE);
    }

    private boolean isExplosionDamage(DamageSource source) {
        return source.is(DamageTypes.EXPLOSION) || source.is(DamageTypes.PLAYER_EXPLOSION);
    }

    private boolean isProjectileDamage(DamageSource source) {
        return source.is(DamageTypes.ARROW)
                || source.is(DamageTypes.TRIDENT)
                || source.getDirectEntity() instanceof Projectile;
    }
}
