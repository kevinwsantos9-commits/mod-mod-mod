package com.nivek.eclipse.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.nivek.eclipse.transformation.Form;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;

/** Desenha a forma de transformacao ativa (indice em EclipseRenderState) com animacao simples. */
public class FormLayer extends RenderLayer<PlayerRenderState, PlayerModel> {
    private final ModelPart tentacle;
    private final ModelPart wingL;
    private final ModelPart wingR;
    private final ModelPart padL;
    private final ModelPart padR;
    private final ModelPart crown;

    public FormLayer(RenderLayerParent<PlayerRenderState, PlayerModel> parent, EntityModelSet modelSet) {
        super(parent);
        ModelPart root = modelSet.bakeLayer(FormModel.LAYER);
        this.tentacle = root.getChild("tentacle");
        this.wingL = root.getChild("wing_l");
        this.wingR = root.getChild("wing_r");
        this.padL = root.getChild("pad_l");
        this.padR = root.getChild("pad_r");
        this.crown = root.getChild("crown");
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                       PlayerRenderState state, float yRot, float xRot) {
        EclipseRenderState data = (EclipseRenderState) state;
        if (data.nivekeclipse$getMutation() < 1 || state.isInvisible) return;
        int index = data.nivekeclipse$getForm();
        Form[] forms = Form.values();
        if (index < 0 || index >= forms.length) return;
        Form form = forms[index];

        PlayerModel model = getParentModel();
        VertexConsumer vc = buffer.getBuffer(RenderType.entityCutoutNoCull(BioArmModel.TEXTURE));
        float t = state.ageInTicks;

        boolean tentacles = form == Form.TENTACLES || form == Form.ECLIPSE;
        boolean armor = form == Form.ARMOR || form == Form.ECLIPSE;
        boolean wings = form == Form.WINGS || form == Form.ECLIPSE;

        if (tentacles) {
            // quatro tentaculos nas costas, balancando em fases diferentes
            float[][] anchors = {{-2.5F, 2.0F}, {2.5F, 2.0F}, {-1.0F, 6.0F}, {1.0F, 6.0F}};
            for (int i = 0; i < anchors.length; i++) {
                float side = anchors[i][0] < 0 ? -1.0F : 1.0F;
                tentacle.setPos(anchors[i][0], anchors[i][1], 2.4F);
                tentacle.setRotation(0.9F + (float) Math.sin(t * 0.12F + i * 1.7F) * 0.3F,
                        0.0F,
                        side * (0.35F + (float) Math.sin(t * 0.09F + i) * 0.15F));
                draw(poseStack, vc, packedLight, model.body, tentacle);
            }
        }
        if (wings) {
            float flap = (float) Math.sin(t * 0.18F) * 0.25F;
            wingL.setPos(1.0F, 2.0F, 2.3F);
            wingL.setRotation(0.0F, 0.0F, -0.6F + flap);
            wingR.setPos(-1.0F, 2.0F, 2.3F);
            wingR.setRotation(0.0F, 0.0F, 0.6F - flap);
            draw(poseStack, vc, packedLight, model.body, wingL);
            draw(poseStack, vc, packedLight, model.body, wingR);
        }
        if (armor) {
            draw(poseStack, vc, packedLight, model.leftArm, padL);
            draw(poseStack, vc, packedLight, model.rightArm, padR);
            draw(poseStack, vc, packedLight, model.head, crown);
        }
    }

    private static void draw(PoseStack poseStack, VertexConsumer vc, int light, ModelPart bodyPart, ModelPart piece) {
        poseStack.pushPose();
        bodyPart.translateAndRotate(poseStack);
        piece.render(poseStack, vc, light, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }
}
