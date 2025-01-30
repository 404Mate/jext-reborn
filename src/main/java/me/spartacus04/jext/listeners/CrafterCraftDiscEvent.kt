package me.spartacus04.jext.listeners

import me.spartacus04.jext.JextState.DISCS
import me.spartacus04.jext.discs.Disc
import me.spartacus04.jext.listeners.utils.JextListener
import me.spartacus04.jext.utils.Constants.JEXT_FRAGMENT_OUTPUT
import org.bukkit.block.Crafter
import org.bukkit.event.EventHandler
import org.bukkit.event.block.CrafterCraftEvent

@Suppress("UnstableApiUsage")
internal class CrafterCraftDiscEvent : JextListener("1.21") {
    @EventHandler
    fun onCrafterCraft(e: CrafterCraftEvent) {
        if (e.result.type != JEXT_FRAGMENT_OUTPUT) return
        if(e.block.state !is Crafter) return

        val isCustomDisc = (e.block.state as Crafter).inventory.any {
            return@any Disc.isCustomDisc(it)
        }

        // check if every disc has same namespace, if they have the same namespace return the namespace else null
        val namespace = (e.block.state as Crafter).inventory.map {
            Disc.fromItemstack(it)?.namespace
        }.distinct().singleOrNull()
        println(namespace)

        if (isCustomDisc && namespace != null) {
            e.result = DISCS[namespace]!!.discItemStack
        } else if (isCustomDisc) {
            e.isCancelled = true
        }
    }
}