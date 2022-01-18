package net.ghoul.practice.kit;

import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
public class KitGameRules {

    private boolean ranked, infinitespeed, infinitestrength, partyffa, partysplit, antifoodloss, ffacenter,
                    noitems, build, sumo, spleef, combo, timed, waterkill, bowhp, voidspawn, healthRegeneration, showHealth;
    private int hitDelay = 20;
}
