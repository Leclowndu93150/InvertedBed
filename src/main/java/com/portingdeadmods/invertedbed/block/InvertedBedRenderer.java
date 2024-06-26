package com.portingdeadmods.invertedbed.block;

import com.portingdeadmods.invertedbed.Main;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.*;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.DoubleBlockCombiner;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;

public class InvertedBedRenderer implements BlockEntityRenderer<InvertedBedBlockEntity> {
    private final ModelPart headRoot;
    private final ModelPart footRoot;
    public static final ModelLayerLocation BED_HEAD = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(Main.MODID, "bed_head"), "main");
    public static final ModelLayerLocation BED_FOOT = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(Main.MODID, "bed_foot"), "main");

    public InvertedBedRenderer(BlockEntityRendererProvider.Context pContext) {
        this.headRoot = pContext.bakeLayer(BED_HEAD);
        this.footRoot = pContext.bakeLayer(BED_FOOT);
    }

    public static LayerDefinition createHeadLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        partdefinition.addOrReplaceChild("main", CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, 0.0F, 0.0F, 16.0F, 16.0F, 6.0F), PartPose.ZERO);
        partdefinition.addOrReplaceChild(
                "left_leg",
                CubeListBuilder.create().texOffs(50, 6).addBox(0.0F, 6.0F, 0.0F, 3.0F, 3.0F, 3.0F),
                PartPose.rotation((float) (Math.PI / 2), 0.0F, (float) (Math.PI / 2))
        );
        partdefinition.addOrReplaceChild(
                "right_leg",
                CubeListBuilder.create().texOffs(50, 18).addBox(-16.0F, 6.0F, 0.0F, 3.0F, 3.0F, 3.0F),
                PartPose.rotation((float) (Math.PI / 2), 0.0F, (float) Math.PI)
        );
        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    public static LayerDefinition createFootLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        partdefinition.addOrReplaceChild("main", CubeListBuilder.create().texOffs(0, 22).addBox(0.0F, 0.0F, 0.0F, 16.0F, 16.0F, 6.0F), PartPose.ZERO);
        partdefinition.addOrReplaceChild(
                "left_leg",
                CubeListBuilder.create().texOffs(50, 0).addBox(0.0F, 6.0F, -16.0F, 3.0F, 3.0F, 3.0F),
                PartPose.rotation((float) (Math.PI / 2), 0.0F, 0.0F)
        );
        partdefinition.addOrReplaceChild(
                "right_leg",
                CubeListBuilder.create().texOffs(50, 12).addBox(-16.0F, 6.0F, -16.0F, 3.0F, 3.0F, 3.0F),
                PartPose.rotation((float) (Math.PI / 2), 0.0F, (float) (Math.PI * 3.0 / 2.0))
        );
        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void render(InvertedBedBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
        Material material = Sheets.BED_TEXTURES[pBlockEntity.getColor().getId()];
        Level level = pBlockEntity.getLevel();
        if (level != null) {
            BlockState blockstate = pBlockEntity.getBlockState();
            DoubleBlockCombiner.NeighborCombineResult<? extends InvertedBedBlockEntity> neighborcombineresult = DoubleBlockCombiner.combineWithNeigbour(
                    Main.INVERTED_BED.get(),
                    InvertedBedBlock::getBlockType,
                    InvertedBedBlock::getConnectedDirection,
                    ChestBlock.FACING,
                    blockstate,
                    level,
                    pBlockEntity.getBlockPos(),
                    (p_112202_, p_112203_) -> false
            );
            int i = neighborcombineresult.<Int2IntFunction>apply(new BrightnessCombiner<>()).get(pPackedLight);
            this.renderPiece(
                    pPoseStack,
                    pBuffer,
                    blockstate.getValue(InvertedBedBlock.PART) == BedPart.HEAD ? this.headRoot : this.footRoot,
                    blockstate.getValue(InvertedBedBlock.FACING),
                    blockstate.getValue(InvertedBedBlock.CEILING),
                    material,
                    i,
                    pPackedOverlay,
                    false
            );
        } else {
            this.renderPiece(pPoseStack, pBuffer, this.headRoot, Direction.SOUTH, false, material, pPackedLight, pPackedOverlay, false);
            this.renderPiece(pPoseStack, pBuffer, this.footRoot, Direction.SOUTH, false, material, pPackedLight, pPackedOverlay, true);
        }
    }

    /**
     * @param pFoot {@code true} if piece to render is the foot of the bed, {@code
     *              false} otherwise or if being rendered by a {@link
     *              net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer}
     */
    private void renderPiece(
            PoseStack pPoseStack,
            MultiBufferSource pBufferSource,
            ModelPart pModelPart,
            Direction pDirection,
            boolean upsideDown,
            Material pMaterial,
            int pPackedLight,
            int pPackedOverlay,
            boolean pFoot
    ) {
        pPoseStack.pushPose();
        float y = 0;
        if (upsideDown) {
            y = 0.4375f;
        }
        pPoseStack.translate(0.0F, y, pFoot ? -1.0F : 0.0F);
        pPoseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
        if (pDirection.equals(Direction.EAST) || pDirection.equals(Direction.WEST)) {
            pPoseStack.mulPose(Axis.XN.rotationDegrees(180.0F));
            pPoseStack.translate(0, -1, 0);
        } else {
            pPoseStack.mulPose(Axis.YN.rotationDegrees(180.0F));
            pPoseStack.translate(-1, 0, 0);
        }
        pPoseStack.translate(0.5F, 0.5F, 0.5F);
        pPoseStack.mulPose(Axis.ZP.rotationDegrees(180.0F + pDirection.toYRot()));
        pPoseStack.translate(-0.5F, -0.5F, -0.5F);
        VertexConsumer vertexconsumer = pMaterial.buffer(pBufferSource, RenderType::entitySolid);
        pModelPart.render(pPoseStack, vertexconsumer, pPackedLight, pPackedOverlay);
        pPoseStack.popPose();
    }
}
