package com.github.yajatkaul.mega_showdown.battle.effect;

import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.api.types.ElementalType;
import com.cobblemon.mod.common.api.types.ElementalTypes;
import com.cobblemon.mod.common.battles.ActiveBattlePokemon;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ScalableParticleOptionsBase;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TerrainEffect extends AbstractFieldHandler {

  private static final float TERRAIN_PARTICLE_SCALE = 1.5f;
  private static final int MAX_RADIUS = 12;
  private static final int PARTICLES_PER_RING = 24;

  private static final Map<ElementalType, ScalableParticleOptionsBase> PARTICLE_CACHE = new ConcurrentHashMap<>();

  public static void handleTerrain(PokemonBattle battle, String terrainName, int ticks) {
    ElementalType type = getTypeForTerrain(terrainName);
    if (type == null) return;

    ServerLevel level = null;
    List<Vec3> positions = new ArrayList<>();

    for (ActiveBattlePokemon pokemon : battle.getActivePokemon()) {
      if (pokemon.getPosition() != null) {
        positions.add(pokemon.getPosition().getSecond());
        level = pokemon.getPosition().getFirst();
      }
    }

    if (positions.isEmpty() || level == null) return;

    Vec3 centre = Vec3.ZERO;
    for (Vec3 pos : positions) {
      centre = centre.add(pos);
    }
    centre = centre.scale(1.0 / positions.size());

    double maxDistance = 0;
    for (Vec3 a : positions) {
      for (Vec3 b : positions) {
        maxDistance = Math.max(maxDistance, a.distanceTo(b));
      }
    }
    maxDistance = Math.min(maxDistance + 2, MAX_RADIUS);

    int ring = (ticks % (int) maxDistance) + 1;
    createTerrainRing(level, centre, ring, type);
  }

  @Nullable
  private static ElementalType getTypeForTerrain(String terrain) {
    return switch (terrain) {
      case "mistyterrain" -> ElementalTypes.FAIRY;
      case "electricterrain" -> ElementalTypes.ELECTRIC;
      case "grassyterrain" -> ElementalTypes.GRASS;
      case "psychicterrain" -> ElementalTypes.PSYCHIC;
      default -> null;
    };
  }

  private static void createTerrainRing(ServerLevel level, Vec3 centre, double radius, ElementalType type) {
    float step = (float) (2 * Math.PI / PARTICLES_PER_RING);
    ScalableParticleOptionsBase particle = getCachedParticle(type);

    for (int i = 0; i < PARTICLES_PER_RING; i++) {
      float angle = i * step;
      Vec3 pos = centre.add(
        Mth.cos(angle) * radius,
        0.1,
        Mth.sin(angle) * radius
      );
      level.sendParticles(particle, pos.x(), pos.y(), pos.z(), 1, 0, 0, 0, 0);
    }
  }

  private static ScalableParticleOptionsBase getCachedParticle(ElementalType type) {
    return PARTICLE_CACHE.computeIfAbsent(type, t ->
      new DustParticleOptions(Vec3.fromRGB24(t.getHue()).toVector3f(), TERRAIN_PARTICLE_SCALE)
    );
  }
}
