package dev.customclaims.protection;

import dev.customclaims.core.CoreServices;
import dev.customclaims.protection.service.ClaimRulesCooldownService;
import dev.customclaims.protection.service.ClaimRulesService;
import dev.customclaims.protection.service.CreateMachinesProtectionService;
import dev.customclaims.protection.service.ExplosionProtectionService;
import dev.customclaims.protection.service.ForeignInteractionLimitService;
import dev.customclaims.protection.service.OpenPartiesProtectionBypassService;
import dev.customclaims.protection.service.StorageProtectionService;
import dev.customclaims.protection.service.VillagerProtectionService;
import dev.customclaims.protection.service.WitherRulesService;

public record ProtectionServices(
        ExplosionProtectionService explosionProtectionService,
        CreateMachinesProtectionService createMachinesProtectionService,
        ClaimRulesCooldownService claimRulesCooldownService,
        ClaimRulesService claimRulesService,
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
                coreServices.dataStorageService(),
                openPartiesProtectionBypassService
        );
        CreateMachinesProtectionService createMachinesProtectionService = new CreateMachinesProtectionService(
                coreServices.territoryService(),
                coreServices.territoryStateService(),
                coreServices.partyService(),
                coreServices.dataStorageService()
        );
        ClaimRulesCooldownService claimRulesCooldownService = new ClaimRulesCooldownService(
                coreServices.dataStorageService(),
                coreServices.permissionService()
        );
        ClaimRulesService claimRulesService = new ClaimRulesService(
                coreServices.partyService(),
                coreServices.permissionService(),
                explosionProtectionService,
                createMachinesProtectionService,
                claimRulesCooldownService
        );
        StorageProtectionService storageProtectionService = new StorageProtectionService(
                coreServices.territoryService(),
                coreServices.permissionService()
        );
        WitherRulesService witherRulesService = new WitherRulesService();
        VillagerProtectionService villagerProtectionService = new VillagerProtectionService(coreServices.territoryService());

        return new ProtectionServices(
                explosionProtectionService,
                createMachinesProtectionService,
                claimRulesCooldownService,
                claimRulesService,
                foreignInteractionLimitService,
                openPartiesProtectionBypassService,
                storageProtectionService,
                witherRulesService,
                villagerProtectionService
        );
    }
}
