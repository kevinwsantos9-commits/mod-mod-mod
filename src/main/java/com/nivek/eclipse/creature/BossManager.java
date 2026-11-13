package com.nivek.eclipse.creature;

import com.nivek.eclipse.data.EclipseData;
import com.nivek.eclipse.evolution.EvolutionManager;
import com.nivek.eclipse.event.MeteorEvent;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Ravager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

/**
 * Chefe (Fase 09.2): "Experimento Alfa", um Ravager reforcado com barra de vida e 3 fases.
 * Fase 1: normal. Fase 2 (< 66% de vida): velocidade. Fase 3 (< 33%): barra vermelha e 2 experimentos de reforco.
 * Um chefe por jogador. Entidade propria (modelo/IA) fica para etapas futuras.
 */
public final class BossManager {
    public static final String TAG = "nivekeclipse_boss";
    public static final int MIN_MUTATION = 3;

    private static final String NAME = "Experimento Alfa";
    private static final int CHECK_INTERVAL = 600;   // 30 s
    private static final int CHANCE_ONE_IN = 8;
    private static final int BAR_RANGE = 64;
    private static final int MIN_DIST = 25;
    private static final int MAX_DIST = 35;

    private static final class Boss {
        final Mob mob;
        final ServerBossEvent bar;
        int phase = 1;

        Boss(Mob mob, ServerBossEvent bar) {
            this.mob = mob;
            this.bar = bar;
        }
    }

    /** Chefe ativo por jogador dono (quem o invocou/atraiu). */
    private static final Map<UUID, Boss> ACTIVE = new HashMap<>();
    private static int checkTimer = 0;
    private static int barTimer = 0;

    private BossManager() {}

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(BossManager::tick);
        ServerLivingEntityEvents.AFTER_DEATH.register(BossManager::onDeath);
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
            for (Boss b : ACTIVE.values()) b.bar.removeAllPlayers();
            ACTIVE.clear();
            checkTimer = 0;
            barTimer = 0;
        });
    }

    private static void tick(MinecraftServer server) {
        if (++checkTimer >= CHECK_INTERVAL) {
            checkTimer = 0;
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                if (player.isSpectator() || EclipseData.getMutationLevel(player) < MIN_MUTATION) continue;
                if (!(player.level() instanceof ServerLevel level)) continue;
                if (level.dimension() != Level.OVERWORLD || !MeteorEvent.isNightTime(level)) continue;
                if (level.getRandom().nextInt(CHANCE_ONE_IN) == 0) spawn(player, level);
            }
        }
        if (++barTimer >= 10) {
            barTimer = 0;
            updateBosses(server);
        }
    }

    private static void updateBosses(MinecraftServer server) {
        Iterator<Map.Entry<UUID, Boss>> it = ACTIVE.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, Boss> entry = it.next();
            Boss boss = entry.getValue();
            // sumiu sem morrer por jogador (despawn, void, comando kill): limpa a barra
            if (!boss.mob.isAlive() || !(boss.mob.level() instanceof ServerLevel level)) {
                boss.bar.removeAllPlayers();
                it.remove();
                continue;
            }
            float progress = Math.max(0.0F, Math.min(1.0F, boss.mob.getHealth() / boss.mob.getMaxHealth()));
            boss.bar.setProgress(progress);

            for (ServerPlayer p : new ArrayList<>(boss.bar.getPlayers())) {
                if (p.level() != level || p.distanceTo(boss.mob) > BAR_RANGE) boss.bar.removePlayer(p);
            }
            for (ServerPlayer p : level.players()) {
                if (!p.isSpectator() && p.distanceTo(boss.mob) <= BAR_RANGE) boss.bar.addPlayer(p);
            }

            if (boss.phase == 1 && progress < 0.66F) {
                boss.phase = 2;
                boss.mob.addEffect(new MobEffectInstance(MobEffects.SPEED, 12000, 0, false, false, false));
                announce(boss, level, "O Experimento Alfa se enfurece!");
            }
            if (boss.phase == 2 && progress < 0.33F) {
                boss.phase = 3;
                boss.bar.setColor(BossEvent.BossBarColor.RED);
                announce(boss, level, "O Experimento Alfa chama reforcos!");
                ServerPlayer owner = server.getPlayerList().getPlayer(entry.getKey());
                if (owner != null && owner.level() == level) {
                    CreatureManager.spawnNear(owner, level);
                    CreatureManager.spawnNear(owner, level);
                }
            }
        }
    }

    private static void announce(Boss boss, ServerLevel level, String text) {
        level.playSound(null, boss.mob.getX(), boss.mob.getY(), boss.mob.getZ(),
                SoundEvents.RAVAGER_ROAR, SoundSource.HOSTILE, 2.0F, 0.6F);
        level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, boss.mob.getX(), boss.mob.getY() + 1.5, boss.mob.getZ(),
                40, 0.8, 1.0, 0.8, 0.05);
        for (ServerPlayer p : boss.bar.getPlayers()) {
            p.displayClientMessage(Component.literal(text), true);
        }
    }

    private static void onDeath(net.minecraft.world.entity.LivingEntity entity, net.minecraft.world.damagesource.DamageSource source) {
        if (!entity.getTags().contains(TAG)) return;
        Iterator<Boss> it = ACTIVE.values().iterator();
        while (it.hasNext()) {
            Boss boss = it.next();
            if (boss.mob == entity) {
                boss.bar.removeAllPlayers();
                it.remove();
                break;
            }
        }
        if (!(source.getEntity() instanceof ServerPlayer killer)) return;
        EvolutionManager.addMaterials(killer, 60 + killer.getRandom().nextInt(31));
        EvolutionManager.addXp(killer, 300);
        EclipseData.setBossKills(killer, EclipseData.getBossKills(killer) + 1);
        killer.displayClientMessage(Component.literal("Experimento Alfa derrotado!"), false);
        if (killer.level() instanceof ServerLevel sl) {
            ExperienceOrb.award(sl, entity.position(), 40);
        }
    }

    /** Cria o chefe 25-35 blocos do jogador. Retorna false se ja ha um ativo para ele ou nao achou lugar. */
    public static boolean spawn(ServerPlayer player, ServerLevel level) {
        if (ACTIVE.containsKey(player.getUUID())) return false;
        RandomSource r = level.getRandom();
        for (int attempt = 0; attempt < 8; attempt++) {
            double angle = r.nextDouble() * Math.PI * 2;
            double dist = MIN_DIST + r.nextInt(MAX_DIST - MIN_DIST + 1);
            int x = (int) Math.floor(player.getX() + Math.cos(angle) * dist);
            int z = (int) Math.floor(player.getZ() + Math.sin(angle) * dist);
            if (!level.hasChunkAt(new BlockPos(x, 64, z))) continue;
            int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
            BlockPos feet = new BlockPos(x, y, z);
            if (!level.getBlockState(feet).isAir() || !level.getFluidState(feet.below()).isEmpty()) continue;

            Ravager mob = new Ravager(EntityType.RAVAGER, level);
            int mutation = Math.max(1, EclipseData.getMutationLevel(player));
            double health = 150.0 + 50.0 * mutation;
            if (mob.getAttribute(Attributes.MAX_HEALTH) != null) {
                mob.getAttribute(Attributes.MAX_HEALTH).setBaseValue(health);
            }
            mob.setHealth((float) health);
            mob.setCustomName(Component.literal(NAME));
            mob.setCustomNameVisible(true);
            mob.setGlowingTag(true);
            mob.setPersistenceRequired();
            mob.addTag(TAG);
            mob.setTarget(player);
            mob.setPos(x + 0.5, y, z + 0.5);
            level.addFreshEntity(mob);

            ServerBossEvent bar = new ServerBossEvent(Component.literal(NAME),
                    BossEvent.BossBarColor.PURPLE, BossEvent.BossBarOverlay.NOTCHED_10);
            bar.addPlayer(player);
            ACTIVE.put(player.getUUID(), new Boss(mob, bar));
            player.displayClientMessage(Component.literal("Algo enorme desperta por perto..."), true);
            return true;
        }
        return false;
    }
}
