package me.spartacus04.jext.listeners

import io.github.bananapuncher714.nbteditor.NBTEditor
import me.spartacus04.jext.JextState.CONFIG
import me.spartacus04.jext.JextState.DISCS
import me.spartacus04.jext.listeners.utils.JextListener
import me.spartacus04.jext.utils.Constants
import me.spartacus04.jext.utils.isRecordFragment
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.DecoratedPot
import org.bukkit.event.EventHandler
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.entity.EntityChangeBlockEvent
import org.bukkit.event.player.PlayerInteractEvent
import kotlin.random.Random

internal class DecoratedPotEvent : JextListener("1.21") {
    @EventHandler
    fun onPotBreak(event: BlockBreakEvent) {
        if(event.block.type != Material.DECORATED_POT) return

        val loottable = NBTEditor.getString(event.block, "LootTable") ?: return
        val seed = NBTEditor.getLong(event.block, "LootTableSeed")

        updateLootTable(event.block, loottable.replace("minecraft:", ""), seed)
    }

    @EventHandler
    fun onProjectileDestroyPot(event: EntityChangeBlockEvent) {
        if(!event.to.isAir || event.block.type != Material.DECORATED_POT) return

        val loottable = NBTEditor.getString(event.block, "LootTable") ?: return
        val seed = NBTEditor.getLong(event.block, "LootTableSeed")

        updateLootTable(event.block, loottable.replace("minecraft:", ""), seed)
    }

    @EventHandler
    fun onBlockClick(event: PlayerInteractEvent) {
        if(event.clickedBlock?.type != Material.DECORATED_POT) return

        val loottable = NBTEditor.getString(event.clickedBlock!!, "LootTable") ?: return
        val seed = NBTEditor.getLong(event.clickedBlock!!, "LootTableSeed")

        updateLootTable(event.clickedBlock!!, loottable.replace("minecraft:", ""), seed)
    }

    private fun updateLootTable(block: Block, lootTable: String, seed: Long) {
        val items = ArrayList<Constants.ChanceStack>()

        DISCS.forEach {
            if(it.lootTables.contains(lootTable))
                items.add(Constants.ChanceStack(it.lootTables[lootTable]!!, it.discItemStack))

            if(it.fragmentLootTables.contains(lootTable))
                items.add(
                    Constants.ChanceStack(
                        it.fragmentLootTables[lootTable]!!,
                        it.fragmentItemStack!!
                    )
                )
        }

        val lootTableItems = Constants.WEIGHTED_LOOT_TABLE_ITEMS[lootTable]!!

        NBTEditor.set(block, null, "LootTable")

        val totalItems = lootTableItems + items.sumOf { it.chance }
        val random = (0 until totalItems).random(Random(seed))

        if(random >= lootTableItems) {
            var remainingChance = random - lootTableItems

            for(item in items) {
                remainingChance -= item.chance

                if(remainingChance < 0) {
                    val decoratedPot = block.state as DecoratedPot
                    decoratedPot.inventory.item = item.stack.apply {
                        if(this.type.isRecordFragment) {
                            this.amount = if(CONFIG.DISC_LIMIT.containsKey(lootTable)) CONFIG.DISC_LIMIT[lootTable]!!
                            else if(CONFIG.DISC_LIMIT.containsKey("chests/*")) CONFIG.DISC_LIMIT["chests/*"]!!
                            else 2
                        }
                    }

                    break
                }
            }
        }
    }
}