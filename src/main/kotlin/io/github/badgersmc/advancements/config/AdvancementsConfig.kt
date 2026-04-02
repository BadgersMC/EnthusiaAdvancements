package io.github.badgersmc.advancements.config

import net.badgersmc.nexus.config.Comment
import net.badgersmc.nexus.config.ConfigFile

@ConfigFile("advancements")
class AdvancementsConfig {

    @Comment("Enable debug logging")
    var debug: Boolean = false

    @Comment("Directory containing tree config files (relative to plugin data folder)")
    var treesDirectory: String = "trees"
}
