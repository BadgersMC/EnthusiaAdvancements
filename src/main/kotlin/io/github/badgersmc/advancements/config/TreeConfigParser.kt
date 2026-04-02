package io.github.badgersmc.advancements.config

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import io.github.badgersmc.advancements.domain.*
import net.badgersmc.nexus.annotations.Service
import java.nio.file.Path
import java.util.logging.Logger
import kotlin.io.path.exists
import kotlin.io.path.listDirectoryEntries

@Service
class TreeConfigParser(
    private val config: AdvancementsConfig
) {

    private val logger = Logger.getLogger("EnthusiaAdvancements")

    fun parseAll(pluginDataFolder: Path): List<TreeDef> {
        val treesDir = pluginDataFolder.resolve(config.treesDirectory)
        if (!treesDir.exists()) {
            logger.warning("Trees directory does not exist: $treesDir")
            return emptyList()
        }

        val confFiles = treesDir.listDirectoryEntries("*.conf")
        if (confFiles.isEmpty()) {
            logger.warning("No .conf files found in $treesDir")
            return emptyList()
        }

        return confFiles.mapNotNull { file ->
            try {
                val treeDef = parseFile(file)
                val errors = treeDef.validate()
                if (errors.isNotEmpty()) {
                    errors.forEach { logger.severe("Validation error in ${file.fileName}: $it") }
                    null
                } else {
                    treeDef
                }
            } catch (e: Exception) {
                logger.severe("Failed to parse ${file.fileName}: ${e.message}")
                null
            }
        }
    }

    private fun parseFile(path: Path): TreeDef {
        val conf = ConfigFactory.parseFile(path.toFile())

        val namespace = conf.getString("namespace")
        val backgroundTexture = conf.getString("background-texture")
        val nodeConfigs = conf.getConfigList("nodes")

        require(nodeConfigs.isNotEmpty()) { "Tree '$namespace' has no nodes" }

        val nodes = nodeConfigs.map { parseNode(it) }
        return TreeDef(namespace, backgroundTexture, nodes)
    }

    private fun parseNode(conf: Config): AdvancementNodeDef {
        val key = conf.getString("key")
        val title = conf.getString("title")
        val description = conf.getStringList("description")
        val icon = conf.getString("icon")
        val frame = FrameType.valueOf(conf.getString("frame"))
        val x = conf.getDouble("x").toFloat()
        val y = conf.getDouble("y").toFloat()
        val maxProgression = conf.getInt("max-progression")

        val parentKey = if (conf.hasPath("parent")) conf.getString("parent") else null
        val requirement = if (conf.hasPath("requirement")) parseRequirement(conf.getConfig("requirement")) else null
        val rewards = if (conf.hasPath("rewards")) conf.getConfigList("rewards").map { parseReward(it) } else emptyList()
        val showToast = if (conf.hasPath("show-toast")) conf.getBoolean("show-toast") else true
        val announceChat = if (conf.hasPath("announce-chat")) conf.getBoolean("announce-chat") else true

        return AdvancementNodeDef(
            key = key,
            title = title,
            description = description,
            icon = icon,
            frame = frame,
            x = x,
            y = y,
            maxProgression = maxProgression,
            parentKey = parentKey,
            requirement = requirement,
            rewards = rewards,
            showToast = showToast,
            announceChat = announceChat
        )
    }

    private fun parseRequirement(conf: Config): Requirement {
        val type = RequirementType.valueOf(conf.getString("type"))
        val target = if (conf.hasPath("target")) conf.getString("target") else null
        val amount = if (conf.hasPath("amount")) conf.getInt("amount") else 1
        return Requirement(type, target, amount)
    }

    private fun parseReward(conf: Config): Reward {
        return when (val type = conf.getString("type")) {
            "command" -> Reward.Command(conf.getString("command"))
            "experience" -> Reward.Experience(conf.getInt("amount"))
            "item" -> Reward.Item(
                conf.getString("material"),
                if (conf.hasPath("amount")) conf.getInt("amount") else 1
            )
            "message" -> Reward.Message(conf.getString("text"))
            else -> throw IllegalArgumentException("Unknown reward type: $type")
        }
    }
}
