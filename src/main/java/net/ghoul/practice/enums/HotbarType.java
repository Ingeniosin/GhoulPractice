package net.ghoul.practice.enums;

import java.beans.ConstructorProperties;

public enum HotbarType {

    SELECT_QUEUE_TYPE(null),
    QUEUE_JOIN_RANKED(null), 
    QUEUE_JOIN_UNRANKED(null), 
    QUEUE_JOIN_KITPVP(null),
    QUEUE_LEAVE(null), 
    PARTY_EVENTS(null), 
    PARTY_CREATE("party create"), 
    PARTY_DISBAND("party disband"), 
    PARTY_LEAVE("party leave"), 
    PARTY_INFORMATION("party info"),
    OTHER_PARTIES(null), 
    PARTY_INFO(null),
    SETTINGS_MENU(null),
    STATS(null),
    COSMETICS(null),
    KIT_EDITOR(null), 
    SPECTATE_STOP("stopspectating"), 
    VIEW_INVENTORY(null), 
    EVENT_JOIN("event"), 
    SUMO_LEAVE("sumo leave"),
    DEFAULT_KIT(null), 
    DIAMOND_KIT(null), 
    BARD_KIT(null), 
    ROGUE_KIT(null), 
    ARCHER_KIT(null),
    MATCHSLIST(null);
    
    private String command;
    
    @ConstructorProperties({ "command" })
    private HotbarType(final String command) {
        this.command = command;
    }
    
    public String getCommand() {
        return this.command;
    }
}
