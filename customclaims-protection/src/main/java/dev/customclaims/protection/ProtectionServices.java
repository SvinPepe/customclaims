package dev.customclaims.protection;

import dev.customclaims.core.CoreServices;
import dev.customclaims.protection.service.ExplosionProtectionService;
import dev.customclaims.protection.service.ForeignInteractionLimitService;
import dev.customclaims.protection.service.StorageProtectionService;
import dev.customclaims.protection.service.VillagerProtectionService;
import dev.customclaims.protection.service.WitherRulesService;

public record ProtectionServices(
        ExplosionProtectionService explosionProtectionService,
        ForeignInteractionLimitService foreignInteractionLimitService,
        StorageProtectionService storageProtectionService,
        WitherRulesService witherRulesService,
        VillagerProtectionService villagerProtectionService
) {
    static ProtectionServices create(CoreServices coreServices) {
        ForeignInteractionLimitService foreignInteractionLimitService = new ForeignInteractionLimitService(
                coreServices.territoryService(),
                coreServices.permissionService()
        );
        ExplosionProtectionService explosionProtectionService = new ExplosionProtectionService(coreServices.territoryService());
        StorageProtectionService storageProtectionService = new StorageProtectionService(
                coreServices.territoryService(),
                coreServices.permissionService()
        );
        WitherRulesService witherRulesService = new WitherRulesService();
        VillagerProtectionService villagerProtectionService = new VillagerProtectionService(coreServices.territoryService());

        return new ProtectionServices(
                explosionProtectionService,
                foreignInteractionLimitService,
                storageProtectionService,
                witherRulesService,
                villagerProtectionService
        );
    }
}
