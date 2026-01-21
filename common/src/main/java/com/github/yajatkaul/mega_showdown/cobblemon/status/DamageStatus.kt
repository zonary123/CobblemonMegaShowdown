package com.github.yajatkaul.mega_showdown.cobblemon.status

import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.pokemon.status.PersistentStatus
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import kotlin.math.max
import kotlin.math.round
import kotlin.random.Random

class DamageStatus(
    name: ResourceLocation,
    showdownName: String,
    applyMessage: String,
    removeMessage: String,
    defaultDuration: IntRange,

    private val chance: Int,
    private val damagePercent: Double,
    private val healingAbility: String
) : PersistentStatus(
    name,
    showdownName,
    applyMessage,
    removeMessage,
    defaultDuration
) {
    override fun onSecondPassed(player: ServerPlayer, pokemon: Pokemon, random: Random) {
        // 1 in x chance to damage y% of their HP with a minimum of 1
        if (!pokemon.isFainted() && random.nextInt(chance) == 0) {
            pokemon.currentHealth -= max(
                1,
                round(pokemon.maxHealth * damagePercent).toInt()
            ) * (if (pokemon.ability.template.name == healingAbility) -1 else 1)
            // Only way that's happened is if the Pokémon has z heal
            if (pokemon.currentHealth == pokemon.maxHealth) {
                pokemon.status = null
            }
        }
    }
}
