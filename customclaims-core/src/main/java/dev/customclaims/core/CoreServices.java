package dev.customclaims.core;

import dev.customclaims.core.api.ClaimAdapter;
import dev.customclaims.core.api.PartyAdapter;
import dev.customclaims.core.log.ActionLogService;
import dev.customclaims.core.log.WarLogService;
import dev.customclaims.core.opc.OpenPartiesClaimAdapter;
import dev.customclaims.core.rollback.NoopRollbackService;
import dev.customclaims.core.rollback.RollbackService;
import dev.customclaims.core.service.ClaimService;
import dev.customclaims.core.service.ConfigManager;
import dev.customclaims.core.service.DataStorageService;
import dev.customclaims.core.service.MessageService;
import dev.customclaims.core.service.PartyService;
import dev.customclaims.core.service.PermissionService;
import dev.customclaims.core.service.TerritoryService;
import dev.customclaims.core.service.TerritoryStateService;

public record CoreServices(
        ClaimAdapter claimAdapter,
        PartyAdapter partyAdapter,
        PartyService partyService,
        ClaimService claimService,
        TerritoryStateService territoryStateService,
        TerritoryService territoryService,
        PermissionService permissionService,
        ConfigManager configManager,
        DataStorageService dataStorageService,
        MessageService messageService,
        WarLogService warLogService,
        ActionLogService actionLogService,
        RollbackService rollbackService
) {
    static CoreServices create() {
        OpenPartiesClaimAdapter adapter = new OpenPartiesClaimAdapter();
        ConfigManager configManager = new ConfigManager();
        DataStorageService dataStorageService = new DataStorageService();
        MessageService messageService = new MessageService();
        PermissionService permissionService = new PermissionService();
        PartyService partyService = new PartyService(adapter);
        ClaimService claimService = new ClaimService(adapter);
        TerritoryStateService territoryStateService = new TerritoryStateService();
        TerritoryService territoryService = new TerritoryService(adapter, adapter, territoryStateService);
        WarLogService warLogService = new WarLogService(dataStorageService);
        ActionLogService actionLogService = new ActionLogService(dataStorageService);
        RollbackService rollbackService = new NoopRollbackService();

        return new CoreServices(
                adapter,
                adapter,
                partyService,
                claimService,
                territoryStateService,
                territoryService,
                permissionService,
                configManager,
                dataStorageService,
                messageService,
                warLogService,
                actionLogService,
                rollbackService
        );
    }
}
