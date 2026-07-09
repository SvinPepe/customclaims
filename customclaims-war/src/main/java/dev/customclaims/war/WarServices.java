package dev.customclaims.war;

import dev.customclaims.core.CoreServices;
import dev.customclaims.war.message.WarMessages;
import dev.customclaims.war.service.AfkTracker;
import dev.customclaims.war.service.BorderChunkService;
import dev.customclaims.war.service.CaptureBoostItemService;
import dev.customclaims.war.service.CaptureProgressService;
import dev.customclaims.war.service.DailyWarStartLimitService;
import dev.customclaims.war.service.PostWarProtectionService;
import dev.customclaims.war.service.RaidWindowService;
import dev.customclaims.war.service.WarManager;
import dev.customclaims.war.service.WarStorage;
import dev.customclaims.war.service.WarDisplayService;
import dev.customclaims.war.service.WarHudService;
import dev.customclaims.war.service.WarLivesService;
import dev.customclaims.war.service.WarNotificationService;
import dev.customclaims.war.service.WarScoreboardService;

public record WarServices(
        WarManager warManager,
        WarStorage warStorage,
        DailyWarStartLimitService dailyWarStartLimitService,
        RaidWindowService raidWindowService,
        BorderChunkService borderChunkService,
        AfkTracker afkTracker,
        CaptureProgressService captureProgressService,
        CaptureBoostItemService captureBoostItemService,
        PostWarProtectionService postWarProtectionService,
        WarDisplayService displayService,
        WarHudService hudService,
        WarNotificationService notificationService,
        WarLivesService livesService,
        WarScoreboardService scoreboardService,
        WarMessages messages
) {
    static WarServices create(CoreServices coreServices) {
        WarStorage warStorage = new WarStorage(coreServices.dataStorageService());
        DailyWarStartLimitService dailyWarStartLimitService = new DailyWarStartLimitService(coreServices.dataStorageService());
        RaidWindowService raidWindowService = new RaidWindowService();
        BorderChunkService borderChunkService = new BorderChunkService(coreServices.territoryService());
        AfkTracker afkTracker = new AfkTracker();
        WarLivesService livesService = new WarLivesService(coreServices);
        CaptureProgressService captureProgressService = new CaptureProgressService(coreServices.partyService(), livesService);
        CaptureBoostItemService captureBoostItemService = new CaptureBoostItemService();
        PostWarProtectionService postWarProtectionService = new PostWarProtectionService(coreServices.territoryStateService());
        WarDisplayService displayService = new WarDisplayService(coreServices);
        WarHudService hudService = new WarHudService(coreServices, displayService);
        WarNotificationService notificationService = new WarNotificationService(coreServices, displayService);
        WarScoreboardService scoreboardService = new WarScoreboardService();
        WarMessages messages = new WarMessages();
        WarManager warManager = new WarManager(
                coreServices,
                warStorage,
                dailyWarStartLimitService,
                raidWindowService,
                borderChunkService,
                afkTracker,
                captureProgressService,
                postWarProtectionService,
                displayService,
                hudService,
                notificationService,
                livesService,
                scoreboardService
        );

        return new WarServices(
                warManager,
                warStorage,
                dailyWarStartLimitService,
                raidWindowService,
                borderChunkService,
                afkTracker,
                captureProgressService,
                captureBoostItemService,
                postWarProtectionService,
                displayService,
                hudService,
                notificationService,
                livesService,
                scoreboardService,
                messages
        );
    }
}