package com.max.lock.common.init;

import com.max.lock.common.Lock;
import com.max.lock.common.recipe.KeyRecipe;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;




public final class LockRecipeSerializers {
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS = DeferredRegister.create(Lock.MOD_ID,
            Registries.RECIPE_SERIALIZER);

    public static final RegistrySupplier<RecipeSerializer<KeyRecipe>> KEY = SERIALIZERS.register("crafting_key",
            () -> new SimpleCraftingRecipeSerializer<>(KeyRecipe::new));

    private LockRecipeSerializers() {
    }

    public static void register() {
        SERIALIZERS.register();
    }
}
