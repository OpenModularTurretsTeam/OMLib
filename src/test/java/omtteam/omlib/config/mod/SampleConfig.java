package omtteam.omlib.config.mod;

import omtteam.omlib.config.Comment;
import omtteam.omlib.config.ConfigRanges;
import omtteam.omlib.config.ConfigValidator;
import omtteam.omlib.config.Range;

/** A config as a mod has it: in a package of its own, with a section class next to it. */
public class SampleConfig {
    @Comment("Version of this file's layout.") @Range(min = 1)
    public int configVersion = 2;

    @Comment("Group of values.")
    public Group group = new Group();

    public static class Group {
        @Comment("A number.") @Range(min = 1, max = 10)
        public int level = 5;
    }

    /** What a mod passes to the manager as its validator. */
    public void validate(ConfigValidator validator) {
        ConfigRanges.validate(this, validator, "");
    }
}
