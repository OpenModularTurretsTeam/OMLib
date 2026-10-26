package omtteam.omlib.config.mod;

import omtteam.omlib.config.Comment;
import omtteam.omlib.config.Range;

/** A config as a mod has it: in a package of its own, with a section class next to it. */
public class SampleConfig {
    @Comment("Group of values.")
    public Group group = new Group();

    public static class Group {
        @Comment("A number.") @Range(min = 1, max = 10)
        public int level = 5;
    }
}
