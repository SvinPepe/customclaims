package dev.customclaims.war.message;

public final class WarMessages {
    public String noParty() {
        return "You must have a valid war side to start a war.";
    }

    public String noClaim() {
        return "This chunk is not claimed by another side.";
    }

    public String ownClaim() {
        return "You cannot attack your own claim.";
    }
}
