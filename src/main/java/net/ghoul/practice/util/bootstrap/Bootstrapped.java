package net.ghoul.practice.util.bootstrap;

import lombok.Getter;
import net.ghoul.practice.Ghoul;

@Getter
public class Bootstrapped {

    protected final Ghoul Array;

    public Bootstrapped(Ghoul Array) {
        this.Array=Array;
    }

}
