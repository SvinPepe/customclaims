package dev.customclaims.war;

import dev.customclaims.core.CoreServices;
import dev.customclaims.war.message.WarMessages;
import dev.customclaims.war.service.AfkTracker;
import dev.customclaims.war.service.BorderChunkService;
import dev.customclaims.war.service.CaptureBoostItemService;
import dev.customclaims.war.service.CaptureProgressService;
import dev.customclaims.war.service.PostWarProtectionService;
import dev.customclaims.war.service.RaidWindowService;
import dev.customclaims.war.service.WarManager;
import dev.customclaims.war.service.WarStorage;
import dev.customclaims.war.service.WarDisplayService;
import dev.customclaims.war.service.WarHudService;
import dev.customclaims.war.service.WarNotificationService;

public record WarServices(
        WarManager warManager,
        WarStorage warStorage,
        RaidWindowService raidWindowService,
        BorderChunkService borderChunkService,
        AfkTracker afkTracker,
        CaptureProgressService captureProgressService,
        CaptureBoostItemService captureBoostItemService,
        PostWarProtectionService postWarProtectionService,
        WarDisplayService displayService,
        WarHudService hudService,
        WarNotificationService notificationService,
        WarMessages messages
) {
    static WarServices create(CoreServices coreServices) {
        WarStorage warStorage = new WarStorage(coreServices.dataStorageService());
        RaidWindowService raidWindowService = new RaidWindowService();
        BorderChunkService borderChunkService = new BorderChunkService(coreServices.territoryService());
        AfkTracker afkTracker = new AfkTracker();
        CaptureProgressService captureProgressService = new CaptureProgressService(coreServices.partyService());
        CaptureBoostItemService captureBoostItemService = new CaptureBoostItemService();
        PostWarProtectionService postWarProtectionService = new PostWarProtectionService(coreServices.territoryStateService());
        WarDisplayService displayService = new WarDisplayService(coreServices);
        WarHudService hudService = new WarHudService(coreServices, displayService);
        WarNotificationService notificationService = new WarNotificationService(coreServices, displayService);
        WarMessages messages = new WarMessages();
        WarManager warManager = new WarManager(
                coreServices,
                warStorage,
                raidWindowService,
                borderChunkService,
                afkTracker,
                captureProgressService,
                postWarProtectionService,
                displayService,
                hudService,
                notificationService
        );

        return new WarServices(
                warManager,
                warStorage,
                raidWindowService,
                borderChunkService,
                afkTracker,
                captureProgressService,
                captureBoostItemService,
                postWarProtectionService,
                displayService,
                hudService,
                notificationService,
                messages
        );
    }
}
