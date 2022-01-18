package net.ghoul.practice.kit;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class KitLeaderboards {
    private String name, godName;
    private int elo;
}
