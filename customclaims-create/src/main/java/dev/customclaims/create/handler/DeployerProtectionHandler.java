package dev.customclaims.create.handler;

public final class DeployerProtectionHandler {
    public boolean canDeployerInteract() {
        // TODO: Wire Create deployer events/API once the exact 1.21.1 compat surface is selected.
        return false;
    }
}
