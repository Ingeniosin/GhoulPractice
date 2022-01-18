package net.ghoul.practice.enums;

import net.ghoul.practice.util.chat.CC;

import java.beans.ConstructorProperties;

public enum PartyPrivacyType {
    OPEN(CC.GREEN + "Public"),
    CLOSED(CC.RED + "Private");
    
    private final String string;
    
    @ConstructorProperties({ "string" })
    PartyPrivacyType(final String string) {
        this.string=string;
    }
    
    @Override
    public String toString() {
        return this.string;
    }
}
