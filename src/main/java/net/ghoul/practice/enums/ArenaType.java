package net.ghoul.practice.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ArenaType {
    STANDALONE,
    SHARED,
    THEBRIDGE,
    DUPLICATE
}
