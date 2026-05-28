package dev.anvilcraft.lib.v2.piston.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.PistonHeadRenderer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.piston.PistonMovingBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PistonHeadRenderer.class)
abstract class PistonHeadRendererMixin implements BlockEntityRenderer<PistonMovingBlockEntity> {
    @Inject(
        method = "render("
                 + "Lnet/minecraft/world/level/block/piston/PistonMovingBlockEntity;"
                 + "FLcom/mojang/blaze3d/vertex/PoseStack;"
                 + "Lnet/minecraft/client/renderer/MultiBufferSource;II)V",
        at = @At("TAIL")
    )
    private void render(
        PistonMovingBlockEntity blockEntity,
        float partialTick,
        PoseStack poseStack,
        MultiBufferSource bufferSource,
        int packedLight,
        int packedOverlay,
        CallbackInfo ci
    ) {
        BlockEntity be = blockEntity.anvillib$getBlockEntity();
        if (be == null) return;
        poseStack.pushPose();
        poseStack.translate(blockEntity.getXOff(partialTick), blockEntity.getYOff(partialTick), blockEntity.getZOff(partialTick));
        BlockEntityRenderer<BlockEntity> renderer = Minecraft.getInstance()
            .getBlockEntityRenderDispatcher()
            .getRenderer(be);
        if (renderer != null) {
            renderer.render(be, partialTick, poseStack, bufferSource, packedLight, packedOverlay);
        }
        poseStack.popPose();
    }
}
