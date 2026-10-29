package dev.customclaims.core.platform;

import dev.customclaims.core.permissions.CustomClaimsPermissions;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.server.permission.PermissionAPI;
import net.neoforged.neoforge.server.permission.events.PermissionGatherEvent;
import net.neoforged.neoforge.server.permission.nodes.PermissionNode;
import net.neoforged.neoforge.server.permission.nodes.PermissionTypes;

public final class NeoForgePermissionHelper implements IPermissionHelper {
    private static final Map<String, PermissionNode<Boolean>> NODES = new HashMap<>();

    static {
        registerNode("opac", "warfare.start", true, "Start wars");
        registerNode("opac", "warfare.cancel", false, "Cancel wars");
        registerNode("opac", "warfare.status", true, "View war status");
        registerNode("opac", "warfare.admin", false, "War administration");
        registerNode("opac", "warfare.bypass", false, "Bypass war restrictions");
        registerNode("opac", "warfare.rollback", false, "Perform rollback");

        registerNode("opac", "protection.claimrules", true, "Use /claimrules GUI");
        registerNode("opac", "protection.explosions.status", true, "Check explosion rule");
        registerNode("opac", "protection.explosions.toggle", true, "Toggle explosion rule");
        registerNode("opac", "protection.create.status", true, "Check Create machine rule");
        registerNode("opac", "protection.create.toggle", true, "Toggle Create machine rule");
        registerNode("opac", "protection.assembly.status", true, "Check assembly rule");
        registerNode("opac", "protection.assembly.toggle", true, "Toggle assembly rule");
        registerNode("opac", "protection.limits.reset", false, "Reset interaction limits");
        registerNode("opac", "protection.bypass", false, "Bypass protection limits");
        registerNode("opac", "protection.admin", false, "Protection administration");

        registerNode("opac", "admin", false, "OPaC administration");
        registerNode("opac", "bypass", false, "Global OPaC bypass");
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

    public static PermissionNode<Boolean> getNode(String permission) {
        String canonical = CustomClaimsPermissions.resolveCanonical(permission);
        return NODES.get(canonical);
    }

    public static void onGatherNodes(PermissionGatherEvent.Nodes event) {
        event.addNodes(new ArrayList<PermissionNode<?>>(NODES.values()));
    }

    public static List<PermissionNode<Boolean>> allNodes() {
        return Collections.unmodifiableList(new ArrayList<>(NODES.values()));
    }

    @Override
    public boolean hasPermission(ServerPlayer player, String permissionNode) {
        if (player == null || permissionNode == null) {
            return false;
        }
        PermissionNode<Boolean> node = getNode(permissionNode);
        if (node != null) {
            try {
                return PermissionAPI.getPermission(player, node);
            } catch (Exception ignored) {
            }
        }
        return false;
    }
}
