package net.ghoul.practice.enums;

import net.ghoul.practice.Ghoul;
import net.ghoul.practice.util.chat.CC;

import java.beans.ConstructorProperties;
import java.text.MessageFormat;

public enum PartyMessageType {
    YOU_HAVE_BEEN_INVITED(Ghoul.getInstance().getMessagesConfig().getString("Party.YOU_HAVE_BEEN_INVITED")),
    CLICK_TO_JOIN(Ghoul.getInstance().getMessagesConfig().getString("Party.CLICK_TO_JOIN")),
    PLAYER_INVITED(Ghoul.getInstance().getMessagesConfig().getString("Party.PLAYER_INVITED")),
    PLAYER_JOINED(Ghoul.getInstance().getMessagesConfig().getString("Party.PLAYER_JOINED")),
    PLAYER_LEFT(Ghoul.getInstance().getMessagesConfig().getString("Party.PLAYER_LEFT")),
    CREATED(Ghoul.getInstance().getMessagesConfig().getString("Party.CREATED")),
    DISBANDED(Ghoul.getInstance().getMessagesConfig().getString("Party.DISBANDED")),
    PUBLIC(Ghoul.getInstance().getMessagesConfig().getString("Party.PUBLIC")),
    PRIVACY_CHANGED(Ghoul.getInstance().getMessagesConfig().getString("Party.PRIVACY_CHANGED"));
    
    private final String message;
    
    public String format(final Object... objects) {
        return CC.translate(new MessageFormat(this.message).format(objects));
    }
    
    @ConstructorProperties({ "message" })
    PartyMessageType(final String message) {
        this.message = message;
    }
}
