package dev.anvilcraft.lib.v2.piston.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import dev.anvilcraft.lib.v2.piston.IMoveableEntityBlock;
import dev.anvilcraft.lib.v2.piston.injection.IPistonMovingBlockEntityExtension;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.piston.PistonMovingBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

@Mixin(PistonMovingBlockEntity.class)
abstract class PistonMovingBlockEntityMixin extends BlockEntity implements IPistonMovingBlockEntityExtension {
    @Shadow
    private BlockState movedState;
    @Unique
    private static final Codec<BlockEntityType<?>> anvillib$TYPE_CODEC = BuiltInRegistries.BLOCK_ENTITY_TYPE.byNameCodec();
    @Unique
    private static final String anvillib$MOVEABLE_BLOCK_ENTITY = "anvillib:moveable_block_entity";
    @Unique
    private @Nullable BlockEntity anvillib$blockEntity = null;

    public PistonMovingBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    @Override
    public void anvillib$setBlockEntity(@Nullable BlockEntity blockEntity) {
        this.anvillib$blockEntity = blockEntity;
    }

    @Override
    public @Nullable BlockEntity anvillib$clearBlockEntity() {
        BlockEntity blockEntity = this.anvillib$blockEntity;
        this.anvillib$blockEntity = null;
        return blockEntity;
    }

    @Override
    public @Nullable BlockEntity anvillib$getBlockEntity() {
        return this.anvillib$blockEntity;
    }

    @Inject(method = "loadAdditional", at = @At("TAIL"))
    @SuppressWarnings("NameDoesntMatchTargetClass")
    private void loadAdditional(CompoundTag input, HolderLookup.Provider registries, CallbackInfo ci) {
        if (!input.contains(anvillib$MOVEABLE_BLOCK_ENTITY, Tag.TAG_COMPOUND)) return;
        CompoundTag valueInput = input.getCompound(anvillib$MOVEABLE_BLOCK_ENTITY);
        DataResult<BlockEntityType<?>> entityType = anvillib$TYPE_CODEC.decode(registries.createSerializationContext(NbtOps.INSTANCE), valueInput.get("id"))
            .map(Pair::getFirst);
        if (entityType.isError()) return;
        int x = !input.contains("x") ? 0 : valueInput.getInt("x");
        int y = !input.contains("y") ? 0 : valueInput.getInt("y");
        int z = !input.contains("z") ? 0 : valueInput.getInt("z");
        BlockEntityType<?> blockEntityType = entityType.getOrThrow();
        this.anvillib$blockEntity = blockEntityType.create(new BlockPos(x, y, z), this.movedState);
        if (this.anvillib$blockEntity == null) return;
        this.anvillib$blockEntity.loadWithComponents(valueInput, registries);
    }

    @Inject(method = "saveAdditional", at = @At("TAIL"))
    @SuppressWarnings("NameDoesntMatchTargetClass")
    private void saveAdditional(CompoundTag input, HolderLookup.Provider registries, CallbackInfo ci) {
        if (this.anvillib$blockEntity == null) return;
        CompoundTag child = this.anvillib$blockEntity.saveWithFullMetadata(registries);
        input.put(anvillib$MOVEABLE_BLOCK_ENTITY, child);
    }

    @Inject(
        method = "tick",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/Level;" + "setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z",
            shift = At.Shift.AFTER,
            ordinal = 1
        )
    )
    private static void tick(
        Level level,
        BlockPos pos,
        BlockState state,
        PistonMovingBlockEntity blockEntity,
        CallbackInfo ci,
        @Local(name = "blockstate") BlockState blockstate
    ) {
        if (level.isClientSide()) return;
        if (!(blockstate.getBlock() instanceof IMoveableEntityBlock block)) return;
        BlockEntity be = blockEntity.anvillib$clearBlockEntity();
        if (be == null) return;
        be.worldPosition = pos;
        be.clearRemoved();
        level.removeBlockEntity(pos);
        level.setBlockEntity(be);
        block.notifyMoved(level, pos, blockstate, be);
    }

    @Inject(
        method = "finalTick",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/Level;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z",
            shift = At.Shift.AFTER
        )
    )
    private void finalTick(CallbackInfo ci, @Local(name = "blockstate") BlockState blockstate) {
        if (this.level == null || this.level.isClientSide()) return;
        // noinspection ConstantValue
        if (!(this instanceof IPistonMovingBlockEntityExtension blockEntity1)) return;
        if (!(blockstate.getBlock() instanceof IMoveableEntityBlock block)) return;
        BlockEntity blockEntity = blockEntity1.anvillib$clearBlockEntity();
        if (blockEntity == null) return;
        blockEntity.worldPosition = this.worldPosition;
        blockEntity.clearRemoved();
        this.level.removeBlockEntity(this.worldPosition);
        this.level.setBlockEntity(blockEntity);
        block.notifyMoved(this.level, this.worldPosition, this.movedState, blockEntity);
    }
}
