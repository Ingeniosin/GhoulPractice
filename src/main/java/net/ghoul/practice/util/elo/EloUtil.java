package net.ghoul.practice.util.elo;

import net.ghoul.practice.kit.Kit;
import net.ghoul.practice.profile.Profile;

import java.util.stream.Stream;

public class EloUtil {

    private static final KFactor[] K_FACTORS = {
            new KFactor(0, 800, 25),
            new KFactor(1001, 1200, 20),
            new KFactor(1401, 1600, 15),
            new KFactor(1801, 2000, 10),
            new KFactor(2201, 2400, 5)
    };

    private static final int DEFAULT_K_FACTOR = 25;
    private static final int WIN = 1;
    private static final int LOSS = 0;

    public static int getNewRating(int rating, int opponentRating, boolean won) {
        if (won) {
            return EloUtil.getNewRating(rating, opponentRating, EloUtil.WIN);
        } else {
            return EloUtil.getNewRating(rating, opponentRating, EloUtil.LOSS);
        }
    }

    public static int getNewRating(int rating, int opponentRating, int score) {
        double kFactor = EloUtil.getKFactor(rating);
        double expectedScore = EloUtil.getExpectedScore(rating, opponentRating);
        int newRating = EloUtil.calculateNewRating(rating, score, expectedScore, kFactor);

        if (score == 1) {
            if (newRating == rating) {
                newRating++;
            }
        }
        return newRating;
    }

    private static int calculateNewRating(int oldRating, int score, double expectedScore, double kFactor) {
        return oldRating + (int) (kFactor * (score - expectedScore));
    }

    private static double getKFactor(int rating) {
        for (int i = 0; i < EloUtil.K_FACTORS.length; i++) {
            if (rating >= EloUtil.K_FACTORS[i].getStartIndex() && rating <= EloUtil.K_FACTORS[i].getEndIndex()) {
                return EloUtil.K_FACTORS[i].getValue();
            }
        }
        return EloUtil.DEFAULT_K_FACTOR;
    }

    private static double getExpectedScore(int rating, int opponentRating) {
        return 1 / (1 + Math.pow(10, ((double) (opponentRating - rating) / 400)));
    }

    public static int getGlobalElo(Profile profile) {
        int[] wrapper = new int[2];
        Stream<Kit> kit = Kit.getKits().stream().filter(kits -> kits.getGameRules().isRanked());
        kit.forEach(kits -> {
            wrapper[0] = wrapper[0] + 1;
            wrapper[1] = wrapper[1] + profile.getStatisticsData().get(kits).getElo();
        });
        return wrapper[1] / wrapper[0];
    }

    public static String getEloRange(int elo) {
        String range = "";

        if (elo < 799) {
            range = "&8Bronze IV";
        } else if (elo >= 800 && elo < 850) {
            range = "&8Bronze III";
        } else if (elo >= 850 && elo < 900) {
            range = "&8Bronze II";
        } else if (elo >= 900 && elo < 950) {
            range = "&8Bronze I";
        } else if (elo >= 950 && elo < 1000) {
            range = "&7Silver IV";
        } else if (elo >= 1000 && elo < 1050) {
            range = "&7Silver III";
        } else if (elo >= 1050 && elo < 1100) {
            range = "&7Silver II";
        } else if (elo >= 1100 && elo < 1150) {
            range = "&7Silver I";
        } else if (elo >= 1150 && elo < 1200) {
            range = "&6Gold IV";
        } else if (elo >= 1200 && elo < 1250) {
            range = "&6Gold III";
        } else if (elo >= 1250 && elo < 1300) {
            range = "&6Gold II";
        } else if (elo >= 1300 && elo < 1350) {
            range = "&6Gold I";
        } else if (elo >= 1350 && elo < 1400) {
            range = "&aEmerald IV";
        } else if (elo >= 1400 && elo < 1450) {
            range = "&aEmerald III";
        } else if (elo >= 1450 && elo < 1500) {
            range = "&aEmerald II";
        } else if (elo >= 1500 && elo < 1550) {
            range = "&aEmerald I";
        } else if (elo >= 1550 && elo < 1600) {
            range = "&dDiamond IV";
        } else if (elo >= 1600 && elo < 1700) {
            range = "&dDiamond III";
        } else if (elo >= 1700 && elo < 1800) {
            range = "&dDiamond II";
        } else if (elo >= 1800 && elo < 1900) {
            range = "&dDiamond I";
        } else if (elo >= 1900 && elo < 2000) {
            range = "&bMasters";
        } else if (elo >= 2000) {
            range = "&4GrandMasters";
        }
        return range;
    }

    public static String getEloRangeColor(int elo) {
        String range = "";

        if (elo >= 0 && elo < 950) {
            range = "&8";
        } else if (elo >= 950 && elo < 1150) {
            range = "&7";
        } else if (elo >= 1150 && elo < 1350) {
            range = "&6";
        } else if (elo >= 1350 && elo < 1550) {
            range = "&a";
        } else if (elo >= 1550 && elo < 1900) {
            range = "&d";
        } else if (elo >= 1900 && elo < 2000) {
            range = "&b";
        } else if (elo >= 2000) {
            range = "&4";
        }
        return range;
    }
}
