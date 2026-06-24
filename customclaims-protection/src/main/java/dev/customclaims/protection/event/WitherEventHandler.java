package dev.customclaims.protection.event;

import dev.customclaims.protection.CustomClaimsProtectionMod;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

public final class WitherEventHandler {
    private WitherEventHandler() {
    }

    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (!(event.getEntity() instanceof WitherBoss wither)) {
            return;
        }

        if (CustomClaimsProtectionMod.services().witherRulesService().isWitherBlocked(event.getLevel())) {
            event.setCanceled(true);
            wither.discard();
            CustomClaimsProtectionMod.LOGGER.info("Blocked illegal Wither spawn in {}", event.getLevel().dimension().location());
        }
    }
}
