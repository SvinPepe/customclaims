package dev.customclaims.protection;

import dev.customclaims.core.CoreServices;
import dev.customclaims.protection.service.ExplosionProtectionService;
import dev.customclaims.protection.service.ForeignInteractionLimitService;
import dev.customclaims.protection.service.OpenPartiesProtectionBypassService;
import dev.customclaims.protection.service.StorageProtectionService;
import dev.customclaims.protection.service.VillagerProtectionService;
import dev.customclaims.protection.service.WitherRulesService;

public record ProtectionServices(
        ExplosionProtectionService explosionProtectionService,
        ForeignInteractionLimitService foreignInteractionLimitService,
        OpenPartiesProtectionBypassService openPartiesProtectionBypassService,
        StorageProtectionService storageProtectionService,
        WitherRulesService witherRulesService,
        VillagerProtectionService villagerProtectionService
) {
    static ProtectionServices create(CoreServices coreServices) {
        ForeignInteractionLimitService foreignInteractionLimitService = new ForeignInteractionLimitService(
                coreServices.territoryService(),
                coreServices.permissionService()
        );
        OpenPartiesProtectionBypassService openPartiesProtectionBypassService = new OpenPartiesProtectionBypassService();
        ExplosionProtectionService explosionProtectionService = new ExplosionProtectionService(
                coreServices.territoryService(),
                openPartiesProtectionBypassService
        );
        StorageProtectionService storageProtectionService = new StorageProtectionService(
                coreServices.territoryService(),
                coreServices.permissionService()
        );
        WitherRulesService witherRulesService = new WitherRulesService();
        VillagerProtectionService villagerProtectionService = new VillagerProtectionService(coreServices.territoryService());

        return new ProtectionServices(
                explosionProtectionService,
                foreignInteractionLimitService,
                openPartiesProtectionBypassService,
                storageProtectionService,
                witherRulesService,
                villagerProtectionService
        );
    }
}
