package io.github.badgersmc.advancements.application.ports

interface PluginHook {
    val pluginName: String
    fun isAvailable(): Boolean
    fun onEnable()
}
