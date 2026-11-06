package omtteam.omlib.config.mod;

import omtteam.omlib.config.Comment;
import omtteam.omlib.config.Range;

/** A config whose fields are private, in a package of its own: the library has to reach them by reflection. */
public class PrivateConfig {
    @Comment("A private number.") @Range(min = 1, max = 10)
    private int level = 5;

    private Section section = new Section();

    public int level() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public Section section() {
        return section;
    }

    public static class Section {
        @Range(min = 0, max = 1)
        private double chance = 0.25D;

        public double chance() {
            return chance;
        }

        public void setChance(double chance) {
            this.chance = chance;
        }
    }
}
