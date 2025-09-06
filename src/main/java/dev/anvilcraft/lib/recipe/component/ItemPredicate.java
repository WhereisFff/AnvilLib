package dev.anvilcraft.lib.recipe.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.anvilcraft.lib.util.CodecUtil;
import net.minecraft.advancements.critereon.DataComponentMatchers;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.component.DataComponentExactPredicate;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.core.component.predicates.DataComponentPredicate;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

import java.util.Optional;

/**
 * 物品谓词
 * <p>
 * 用于定义物品匹配规则，包括物品类型、数量范围、组件和子谓词
 * </p>
 *
 * @param items      物品集合
 * @param count      数量范围
 * @param components 数据组件谓词
 */
public record ItemPredicate(
    Optional<HolderSet<Item>> items, MinMaxBounds.Ints count, DataComponentMatchers components
) implements IItemStackPredicate {
    /**
     * ItemPredicate编解码器
     */
    public static final Codec<ItemPredicate> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        RegistryCodecs.homogeneousList(Registries.ITEM).optionalFieldOf("items").forGetter(ItemPredicate::items),
        MinMaxBounds.Ints.CODEC.optionalFieldOf("count", MinMaxBounds.Ints.ANY).forGetter(ItemPredicate::count),
        DataComponentMatchers.CODEC.forGetter(ItemPredicate::components)
    ).apply(instance, ItemPredicate::new));

    /**
     * ItemPredicate流编解码器
     */
    public static final StreamCodec<RegistryFriendlyByteBuf, ItemPredicate> STREAM_CODEC = CodecUtil.codec2Stream(ItemPredicate.CODEC);

    @Override
    public boolean test(ItemStack itemStack) {
        return this.testIgnoreCount(itemStack) && this.testCount(itemStack.getCount());
    }

    @Override
    public boolean testCount(int count) {
        return this.count.matches(count);
    }

    /**
     * 构建器类，用于构建ItemPredicate实例
     */
    @SuppressWarnings("UnusedReturnValue")
    public static class Builder {
        private final HolderGetter<Item> getter;
        @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
        private Optional<HolderSet<Item>> items = Optional.empty();
        private MinMaxBounds.Ints count;
        private final DataComponentMatchers.Builder components;

        /**
         * 构造一个构建器
         */
        private Builder(HolderGetter<Item> getter) {
            this.getter = getter;
            this.count = MinMaxBounds.Ints.ANY;
            this.components = DataComponentMatchers.Builder.components();
        }

        /**
         * 创建一个物品构建器
         *
         * @return 构建器实例
         */
        public static Builder item(HolderGetter<Item> getter) {
            return new Builder(getter);
        }

        /**
         * 设置物品
         *
         * @param items 物品数组
         * @return 构建器实例
         */
        public Builder of(ItemLike... items) {
            //noinspection deprecation
            this.items = Optional.of(HolderSet.direct((item) -> item.asItem().builtInRegistryHolder(), items));
            return this;
        }

        /**
         * 设置物品标签
         *
         * @param tag 物品标签
         * @return 构建器实例
         */
        public Builder of(TagKey<Item> tag) {
            this.items = Optional.of(this.getter.getOrThrow(tag));
            return this;
        }

        /**
         * 设置物品堆栈
         *
         * @param stack 物品堆栈
         * @return 构建器实例
         */
        public <D> Builder of(ItemStack stack) {
            Item item = stack.getItem();
            ItemStack defaultInstance = item.getDefaultInstance();
            this.of(item);
            DataComponentMap.Builder componentMap = DataComponentMap.builder();
            for (TypedDataComponent<?> component : item.components()) {
                Object o = defaultInstance.get(component.type());
                if (o != null && o.equals(component.value())) continue;
                //noinspection unchecked
                componentMap.set((DataComponentType<D>) component.type(), (D) component.value());
            }
            this.components.exact(DataComponentExactPredicate.allOf(componentMap.build()));
            return this;
        }

        /**
         * 设置数量范围
         *
         * @param count 数量范围
         * @return 构建器实例
         */
        public Builder withCount(MinMaxBounds.Ints count) {
            this.count = count;
            return this;
        }

        /**
         * 添加子谓词
         *
         * @param type      子谓词类型
         * @param predicate 子谓词
         * @param <T>       子谓词类型
         * @return 构建器实例
         */
        public <T extends DataComponentPredicate> Builder withSubPredicate(DataComponentPredicate.Type<T> type, T predicate) {
            this.components.partial(type, predicate);
            return this;
        }

        /**
         * 构建ItemPredicate实例
         *
         * @return ItemPredicate实例
         */
        public ItemPredicate build() {
            return new ItemPredicate(this.items, this.count, this.components.build());
        }
    }
}