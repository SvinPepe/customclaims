package dev.customclaims.war.message;

public final class WarMessages {
    public String noParty() {
        return "You must be in a party to start a war.";
    }

    public String noClaim() {
        return "This chunk is not claimed by another party.";
    }

    public String ownClaim() {
        return "You cannot attack your own party claim.";
    }
}
