package com.nivek.eclipse.client.render;

import com.nivek.eclipse.NivekEclipse;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Pecas visuais das formas de transformacao (caixas provisorias, reutilizam a textura do braco).
 * Tentaculo, asas (esquerda/direita), ombreiras (esquerda/direita) e coroa. Poses sao definidas no ImplantLayer-like FormLayer.
 */
public final class FormModel {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(NivekEclipse.MOD_ID, "forms"), "main");

    private FormModel() {}

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        root.addOrReplaceChild("tentacle",
                CubeListBuilder.create().texOffs(0, 0).addBox(-0.75F, 0.0F, -0.75F, 1.5F, 10.0F, 1.5F),
                PartPose.ZERO);
        root.addOrReplaceChild("wing_l",
                CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, -0.5F, 0.0F, 16.0F, 1.0F, 7.0F),
                PartPose.ZERO);
        root.addOrReplaceChild("wing_r",
                CubeListBuilder.create().texOffs(0, 0).addBox(-16.0F, -0.5F, 0.0F, 16.0F, 1.0F, 7.0F),
                PartPose.ZERO);
        root.addOrReplaceChild("pad_l",
                CubeListBuilder.create().texOffs(0, 12).addBox(-1.5F, -3.0F, -2.5F, 5.0F, 2.0F, 5.0F),
                PartPose.ZERO);
        root.addOrReplaceChild("pad_r",
                CubeListBuilder.create().texOffs(0, 12).addBox(-3.5F, -3.0F, -2.5F, 5.0F, 2.0F, 5.0F),
                PartPose.ZERO);
        root.addOrReplaceChild("crown",
                CubeListBuilder.create().texOffs(0, 12).addBox(-4.5F, -9.0F, -4.5F, 9.0F, 1.5F, 9.0F),
                PartPose.ZERO);

        return LayerDefinition.create(mesh, 64, 32);
    }
}
