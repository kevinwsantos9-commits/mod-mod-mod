package com.nivek.eclipse.transformation;

import com.nivek.eclipse.data.EclipseData;
import com.nivek.eclipse.implant.ImplantManager;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.Vec3;

/**
 * Transformacoes (Fase 10.1): uma forma ativa por vez, validada no servidor.
 * Efeitos = modificadores de atributo permanentes e idempotentes (um id por forma e atributo),
 * mais custo de energia por segundo. Visual 3D das formas (tentaculos/asas/armadura): etapas futuras.
 */
public final class FormManager {
    private static final int MIN_ENERGY_TO_START = 20;
    private static final double GLIDE_FALL_SPEED = 0.1;
    private static final double GLIDE_PUSH = 0.03;
    private static final double GLIDE_MAX_SPEED = 0.6;
    private static int timer = 0;

    private FormManager() {}

    public static void register() {
        PayloadTypeRegistry.playC2S().register(FormPayload.TYPE, FormPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(FormPayload.TYPE,
                (payload, context) -> toggle(context.player(), payload.form()));
        ServerTickEvents.END_SERVER_TICK.register(FormManager::tick);
    }

    /** Forma ativa do jogador, ou null (checa tambem a mutacao minima). */
    public static Form active(ServerPlayer player) {
        int index = EclipseData.getForm(player);
        Form[] forms = Form.values();
        if (index < 0 || index >= forms.length) return null;
        Form form = forms[index];
        return EclipseData.getMutationLevel(player) >= form.requiredMutation ? form : null;
    }

    private static void tick(MinecraftServer server) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            glide(player);
        }
        timer++;
        boolean second = timer % 20 == 0;
        boolean visual = timer % 5 == 0;
        if (!second && !visual) return;
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            Form form = active(player);
            if (second) {
                if (form == null && EclipseData.getForm(player) >= 0) EclipseData.setForm(player, -1);
                if (form != null) {
                    int energy = EclipseData.getEnergy(player);
                    if (energy < form.upkeep) {
                        EclipseData.setForm(player, -1);
                        form = null;
                        player.displayClientMessage(Component.literal("Energia esgotada: forma desfeita."), true);
                    } else {
                        EclipseData.setEnergy(player, energy - form.upkeep);
                    }
                }
                apply(player);
            }
            if (visual && form != null && player.level() instanceof ServerLevel level) {
                ParticleOptions particle = switch (form) {
                    case TENTACLES -> ParticleTypes.SOUL_FIRE_FLAME;
                    case ARMOR -> ParticleTypes.END_ROD;
                    case WINGS -> ParticleTypes.CLOUD;
                    case ECLIPSE -> ParticleTypes.PORTAL;
                };
                level.sendParticles(particle, player.getX(), player.getY() + 1.0, player.getZ(),
                        3, 0.6, 0.8, 0.6, 0.02);
            }
        }
    }

    /**
     * Planeio das Asas (Asas e Forma Eclipse): no ar, segurando agachar, a queda e limitada e o jogador
     * avanca na direcao do olhar. Velocidade enviada ao cliente com hurtMarked.
     */
    private static void glide(ServerPlayer player) {
        Form form = active(player);
        if (form != Form.WINGS && form != Form.ECLIPSE) return;
        if (player.onGround() || !player.isShiftKeyDown() || player.isInWater() || player.isSpectator()) return;
        Vec3 motion = player.getDeltaMovement();
        if (motion.y >= -GLIDE_FALL_SPEED) return;
        Vec3 look = player.getLookAngle();
        double vx = motion.x + look.x * GLIDE_PUSH;
        double vz = motion.z + look.z * GLIDE_PUSH;
        double horizontal = Math.sqrt(vx * vx + vz * vz);
        if (horizontal > GLIDE_MAX_SPEED) {
            vx = vx / horizontal * GLIDE_MAX_SPEED;
            vz = vz / horizontal * GLIDE_MAX_SPEED;
        }
        player.setDeltaMovement(vx, -GLIDE_FALL_SPEED, vz);
        player.hurtMarked = true;
        player.resetFallDistance();
    }

    private static void toggle(ServerPlayer player, int index) {
        Form[] forms = Form.values();
        if (index < 0 || index >= forms.length) return;
        if (!player.isAlive() || player.isSpectator()) return;
        Form form = forms[index];
        if (EclipseData.getMutationLevel(player) < form.requiredMutation) {
            player.displayClientMessage(Component.literal(
                    "Forma bloqueada: requer mutacao nivel " + form.requiredMutation + "."), true);
            return;
        }
        if (EclipseData.getForm(player) == index) {
            EclipseData.setForm(player, -1);
            player.displayClientMessage(Component.literal("Forma desfeita: " + form.label + "."), true);
        } else {
            if (EclipseData.getEnergy(player) < MIN_ENERGY_TO_START) {
                player.displayClientMessage(Component.literal(
                        "Energia insuficiente: minimo " + MIN_ENERGY_TO_START + "."), true);
                return;
            }
            EclipseData.setForm(player, index);
            player.displayClientMessage(Component.literal("Forma ativa: " + form.label + "."), true);
            if (player.level() instanceof ServerLevel level) {
                level.sendParticles(ParticleTypes.END_ROD, player.getX(), player.getY() + 1.0, player.getZ(),
                        40, 0.5, 0.9, 0.5, 0.12);
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.2F, 1.4F);
            }
        }
        apply(player);
    }

    /** Aplica os modificadores da forma ativa e remove os das outras (idempotente). */
    public static void apply(ServerPlayer player) {
        Form active = active(player);
        for (Form form : Form.values()) {
            boolean on = form == active;
            switch (form) {
                case TENTACLES -> {
                    set(player, Attributes.ATTACK_DAMAGE, "form_tentacles_attack", 4.0, AttributeModifier.Operation.ADD_VALUE, on);
                    set(player, Attributes.ENTITY_INTERACTION_RANGE, "form_tentacles_reach", 1.5, AttributeModifier.Operation.ADD_VALUE, on);
                }
                case ARMOR -> {
                    set(player, Attributes.ARMOR, "form_armor_armor", 8.0, AttributeModifier.Operation.ADD_VALUE, on);
                    set(player, Attributes.ARMOR_TOUGHNESS, "form_armor_toughness", 4.0, AttributeModifier.Operation.ADD_VALUE, on);
                    set(player, Attributes.KNOCKBACK_RESISTANCE, "form_armor_kb", 0.5, AttributeModifier.Operation.ADD_VALUE, on);
                }
                case WINGS -> {
                    set(player, Attributes.MOVEMENT_SPEED, "form_wings_speed", 0.15, AttributeModifier.Operation.ADD_MULTIPLIED_BASE, on);
                    set(player, Attributes.SAFE_FALL_DISTANCE, "form_wings_fall", 10.0, AttributeModifier.Operation.ADD_VALUE, on);
                    if (on) player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 40, 0, true, false, false));
                }
                case ECLIPSE -> {
                    set(player, Attributes.ATTACK_DAMAGE, "form_eclipse_attack", 6.0, AttributeModifier.Operation.ADD_VALUE, on);
                    set(player, Attributes.ENTITY_INTERACTION_RANGE, "form_eclipse_reach", 2.0, AttributeModifier.Operation.ADD_VALUE, on);
                    set(player, Attributes.ARMOR, "form_eclipse_armor", 10.0, AttributeModifier.Operation.ADD_VALUE, on);
                    set(player, Attributes.ARMOR_TOUGHNESS, "form_eclipse_toughness", 6.0, AttributeModifier.Operation.ADD_VALUE, on);
                    set(player, Attributes.KNOCKBACK_RESISTANCE, "form_eclipse_kb", 0.6, AttributeModifier.Operation.ADD_VALUE, on);
                    set(player, Attributes.MOVEMENT_SPEED, "form_eclipse_speed", 0.20, AttributeModifier.Operation.ADD_MULTIPLIED_BASE, on);
                    set(player, Attributes.SAFE_FALL_DISTANCE, "form_eclipse_fall", 10.0, AttributeModifier.Operation.ADD_VALUE, on);
                    if (on) player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 40, 0, true, false, false));
                }
            }
        }
    }

    private static void set(ServerPlayer player, net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute> attribute,
                            String name, double amount, AttributeModifier.Operation op, boolean on) {
        ImplantManager.setModifier(player, attribute, name, amount, op, on);
    }
}
