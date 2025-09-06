package dev.anvilcraft.lib.init;

import com.mojang.serialization.Codec;
import dev.anvilcraft.lib.AnvilLib;
import dev.anvilcraft.lib.data.advancement.predicate.item.AndPredicate;
import dev.anvilcraft.lib.data.advancement.predicate.item.NotPredicate;
import dev.anvilcraft.lib.data.advancement.predicate.item.OrPredicate;
import net.minecraft.core.component.predicates.DataComponentPredicate;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class LibItemSubPredicates {
    private static final DeferredRegister<DataComponentPredicate.Type<?>> DF = DeferredRegister.create(
        BuiltInRegistries.DATA_COMPONENT_PREDICATE_TYPE,
        AnvilLib.MOD_ID
    );

    public static final DeferredHolder<DataComponentPredicate.Type<?>, DataComponentPredicate.Type<AndPredicate>> AND = register(
        "and",
        AndPredicate.CODEC
    );

    public static final DeferredHolder<DataComponentPredicate.Type<?>, DataComponentPredicate.Type<OrPredicate>> OR = register(
        "or",
        OrPredicate.CODEC
    );

    public static final DeferredHolder<DataComponentPredicate.Type<?>, DataComponentPredicate.Type<NotPredicate>> NOT = register(
        "not",
        NotPredicate.CODEC
    );

    public static <T extends DataComponentPredicate> DeferredHolder<DataComponentPredicate.Type<?>, DataComponentPredicate.Type<T>> register(
        String name,
        Codec<T> codec
    ) {
        return DF.register(name, () -> new DataComponentPredicate.Type<>(codec));
    }

    public static void initialize(IEventBus modEventBus) {
        DF.register(modEventBus);
    }
}
