package dev.customclaims.protection.event;

import dev.customclaims.protection.CustomClaimsProtectionMod;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

public final class VillagerDamageHandler {
    private VillagerDamageHandler() {
    }

    public static void onLivingDamage(LivingIncomingDamageEvent event) {
        if (CustomClaimsProtectionMod.services().villagerProtectionService().shouldBlockDamage(event.getEntity(), event.getSource())) {
            event.setCanceled(true);
            CustomClaimsProtectionMod.services().villagerProtectionService().afterBlockedDamage(event.getEntity());
        }
    }
}
