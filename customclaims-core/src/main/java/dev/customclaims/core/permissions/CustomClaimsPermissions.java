package dev.customclaims.core.permissions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.server.permission.events.PermissionGatherEvent;
import net.neoforged.neoforge.server.permission.nodes.PermissionNode;
import net.neoforged.neoforge.server.permission.nodes.PermissionTypes;

public final class CustomClaimsPermissions {
    // Warfare nodes
    public static final String WAR_START = "opac.warfare.start";
    public static final String WAR_CANCEL = "opac.warfare.cancel";
    public static final String WAR_STATUS = "opac.warfare.status";
    public static final String WAR_ADMIN = "opac.warfare.admin";
    public static final String WAR_BYPASS = "opac.warfare.bypass";
    public static final String ROLLBACK = "opac.warfare.rollback";

    // Protection nodes
    public static final String CLAIMRULES = "opac.protection.claimrules";
    public static final String EXPLOSIONS_STATUS = "opac.protection.explosions.status";
    public static final String EXPLOSIONS_TOGGLE = "opac.protection.explosions.toggle";
    public static final String CREATE_STATUS = "opac.protection.create.status";
    public static final String CREATE_TOGGLE = "opac.protection.create.toggle";
    public static final String ASSEMBLY_STATUS = "opac.protection.assembly.status";
    public static final String ASSEMBLY_TOGGLE = "opac.protection.assembly.toggle";
    public static final String LIMITS_RESET = "opac.protection.limits.reset";
    public static final String PROTECTION_BYPASS = "opac.protection.bypass";
    public static final String PROTECTION_ADMIN = "opac.protection.admin";

    // Global nodes
    public static final String ADMIN = "opac.admin";
    public static final String BYPASS = "opac.bypass";

    // Legacy aliases
    public static final String LEGACY_ADMIN = "customclaims.admin";
    public static final String LEGACY_BYPASS = "customclaims.bypass";
    public static final String LEGACY_WAR_START = "customclaims.war.start";
    public static final String LEGACY_WAR_CANCEL = "customclaims.war.cancel";
    public static final String LEGACY_WAR_STATUS = "customclaims.war.status";
    public static final String LEGACY_WAR_ADMIN = "customclaims.war.admin";
    public static final String LEGACY_ROLLBACK = "customclaims.rollback";
    public static final String LEGACY_CLAIMRULES = "customclaims.claimrules";
    public static final String LEGACY_LIMITS_RESET = "customclaims.limits.reset";
    public static final String LEGACY_EXPLOSIONS_STATUS = "customclaims.explosions.status";
    public static final String LEGACY_EXPLOSIONS_TOGGLE = "customclaims.explosions.toggle";
    public static final String LEGACY_CREATE_STATUS = "customclaims.create.status";
    public static final String LEGACY_CREATE_TOGGLE = "customclaims.create.toggle";
    public static final String LEGACY_ASSEMBLY_STATUS = "customclaims.assembly.status";
    public static final String LEGACY_ASSEMBLY_TOGGLE = "customclaims.assembly.toggle";

    private static final Map<String, PermissionNode<Boolean>> NODES = new HashMap<>();
    private static final Map<String, String> ALIAS_MAP = new HashMap<>();

    public static final PermissionNode<Boolean> NODE_WAR_START = registerNode("opac", "warfare.start", true, "Start wars");
    public static final PermissionNode<Boolean> NODE_WAR_CANCEL = registerNode("opac", "warfare.cancel", false, "Cancel wars");
    public static final PermissionNode<Boolean> NODE_WAR_STATUS = registerNode("opac", "warfare.status", true, "View war status");
    public static final PermissionNode<Boolean> NODE_WAR_ADMIN = registerNode("opac", "warfare.admin", false, "War administration");
    public static final PermissionNode<Boolean> NODE_WAR_BYPASS = registerNode("opac", "warfare.bypass", false, "Bypass war restrictions");
    public static final PermissionNode<Boolean> NODE_ROLLBACK = registerNode("opac", "warfare.rollback", false, "Perform rollback");

    public static final PermissionNode<Boolean> NODE_CLAIMRULES = registerNode("opac", "protection.claimrules", true, "Use /claimrules GUI");
    public static final PermissionNode<Boolean> NODE_EXPLOSIONS_STATUS = registerNode("opac", "protection.explosions.status", true, "Check explosion rule");
    public static final PermissionNode<Boolean> NODE_EXPLOSIONS_TOGGLE = registerNode("opac", "protection.explosions.toggle", true, "Toggle explosion rule");
    public static final PermissionNode<Boolean> NODE_CREATE_STATUS = registerNode("opac", "protection.create.status", true, "Check Create machine rule");
    public static final PermissionNode<Boolean> NODE_CREATE_TOGGLE = registerNode("opac", "protection.create.toggle", true, "Toggle Create machine rule");
    public static final PermissionNode<Boolean> NODE_ASSEMBLY_STATUS = registerNode("opac", "protection.assembly.status", true, "Check assembly rule");
    public static final PermissionNode<Boolean> NODE_ASSEMBLY_TOGGLE = registerNode("opac", "protection.assembly.toggle", true, "Toggle assembly rule");
    public static final PermissionNode<Boolean> NODE_LIMITS_RESET = registerNode("opac", "protection.limits.reset", false, "Reset interaction limits");
    public static final PermissionNode<Boolean> NODE_PROTECTION_BYPASS = registerNode("opac", "protection.bypass", false, "Bypass protection limits");
    public static final PermissionNode<Boolean> NODE_PROTECTION_ADMIN = registerNode("opac", "protection.admin", false, "Protection administration");

    public static final PermissionNode<Boolean> NODE_ADMIN = registerNode("opac", "admin", false, "OPaC administration");
    public static final PermissionNode<Boolean> NODE_BYPASS = registerNode("opac", "bypass", false, "Global OPaC bypass");

    static {
        alias(LEGACY_ADMIN, ADMIN);
        alias(LEGACY_BYPASS, BYPASS);
        alias(LEGACY_WAR_START, WAR_START);
        alias(LEGACY_WAR_CANCEL, WAR_CANCEL);
        alias(LEGACY_WAR_STATUS, WAR_STATUS);
        alias(LEGACY_WAR_ADMIN, WAR_ADMIN);
        alias(LEGACY_ROLLBACK, ROLLBACK);
        alias(LEGACY_CLAIMRULES, CLAIMRULES);
        alias(LEGACY_LIMITS_RESET, LIMITS_RESET);
        alias(LEGACY_EXPLOSIONS_STATUS, EXPLOSIONS_STATUS);
        alias(LEGACY_EXPLOSIONS_TOGGLE, EXPLOSIONS_TOGGLE);
        alias(LEGACY_CREATE_STATUS, CREATE_STATUS);
        alias(LEGACY_CREATE_TOGGLE, CREATE_TOGGLE);
        alias(LEGACY_ASSEMBLY_STATUS, ASSEMBLY_STATUS);
        alias(LEGACY_ASSEMBLY_TOGGLE, ASSEMBLY_TOGGLE);
    }

    private static PermissionNode<Boolean> registerNode(String modId, String nodePath, boolean defaultVal, String description) {
        PermissionNode<Boolean> node = new PermissionNode<>(
                modId,
                nodePath,
                PermissionTypes.BOOLEAN,
                (player, uuid, ctx) -> defaultVal
        );
        node.setInformation(Component.literal(modId + "." + nodePath), Component.literal(description));
        NODES.put(modId + "." + nodePath, node);
        return node;
    }

    private static void alias(String legacy, String target) {
        ALIAS_MAP.put(legacy, target);
    }

    public static PermissionNode<Boolean> getNode(String permission) {
        String canonical = ALIAS_MAP.getOrDefault(permission, permission);
        return NODES.get(canonical);
    }

    public static void onGatherNodes(PermissionGatherEvent.Nodes event) {
        event.addNodes(new ArrayList<PermissionNode<?>>(NODES.values()));
    }

    public static List<PermissionNode<Boolean>> allNodes() {
        return Collections.unmodifiableList(new ArrayList<>(NODES.values()));
    }

    private CustomClaimsPermissions() {
    }
}
