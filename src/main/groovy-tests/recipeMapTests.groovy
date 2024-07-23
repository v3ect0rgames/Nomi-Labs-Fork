// Import Recipe Search Helpers, used for Chanced Item and Fluid Ingredients

import com.nomiceu.nomilabs.groovy.ChangeRecipeBuilder

import gregtech.api.recipes.RecipeBuilder
import gregtech.api.recipes.chance.output.impl.ChancedItemOutput
import gregtech.api.recipes.ingredients.nbtmatch.NBTCondition
import gregtech.api.recipes.ingredients.nbtmatch.NBTMatcher

import static com.nomiceu.nomilabs.groovy.GroovyHelpers.GTRecipeHelpers.*
import static gregtech.api.GTValues.*

// Find and Removing GT Recipe Helpers. Goes in Post Init.

// Building Test Recipes
mods.gregtech.sifter.recipeBuilder()
        .inputs(metaitem('nomilabs:dustImpureOsmiridium8020'))
        .outputs(item('minecraft:apple') * 64, item('minecraft:apple') * 64)
        .EUt(VA[LV]).duration(30)
        .buildAndRegister()

mods.gregtech.sifter.recipeBuilder()
        .inputs(item('minecraft:stick'))
        .outputs(item('minecraft:apple') * 64)
        .EUt(VA[LV]).duration(30)
        .buildAndRegister()

mods.gregtech.sifter.recipeBuilder()
        .inputs(item('minecraft:yellow_flower'))
        .outputs(item('minecraft:apple') * 64, item('minecraft:apple') * 64, item('minecraft:apple') * 64)
        .chancedOutput(item('minecraft:apple') * 64, 50, 1)
        .chancedFluidOutput(fluid('fluorine') * 2000, 50, 1)
        .EUt(VA[LV]).duration(30)
        .buildAndRegister()

mods.gregtech.sifter.recipeBuilder()
        .inputs(metaitem('nomilabs:dustOsmiridium8020'))
        .outputs(item('minecraft:apple') * 64, item('minecraft:apple') * 64, item('minecraft:apple') * 64)
        .chancedOutput(item('minecraft:apple') * 64, 50, 1)
        .chancedFluidOutput(fluid('fluorine') * 2000, 50, 1)
        .EUt(VA[LV]).duration(30)
        .buildAndRegister()

mods.gregtech.sifter.recipeBuilder()
        .inputs(metaitem('nomilabs:dustPureOsmiridium8020'))
        .outputs(item('minecraft:apple') * 64, item('minecraft:apple') * 64)
        .EUt(VA[LV]).duration(30)
        .buildAndRegister()

// Find/Remove By Input Extensions (Are Lists of: List<ItemStack> itemInputs, List<FluidStack> fluidInputs)
// mods.gregtech.<RECIPE_MAP>.removeByInput to remove, mods.gregtech.<RECIPE_MAP>.find to find (Returns null if no recipe found)
// Original: [long voltage, Inputs... (see above)] (Matches/Removes any recipe with that input, and that voltage or more, CHECKING AMOUNT)
// ALL FIND/REMOVE BY INPUT EXTENSIONS IGNORE THE AMOUNT!
// Three Extensions:
// [GTRecipeCategory category, Inputs... (see above)] (Matches/Removes any recipe with that input, and that category)
// [Inputs... (see above)] (Matches/Removes any recipe with that input)
// [Predicate<Recipe> predicate, Inputs... (see above)] (Matches/Removes any recipe with that input, and matching that predicate)
mods.gregtech.sifter.removeByInput([item('minecraft:yellow_flower')], null)

// Find/Remove By Output
// Outputs Specification: List<ItemStack> itemOutputs, List<FluidStack> fluidOutputs, List<ChancedItemOutput> chancedItemOutputs, List<ChancedFluidOutput> chancedFluidOutputs
// (You can also exclude the Chanced Items and Fluids, e.g. List<ItemStack> itemOutputs, List<FluidStack> fluidOutputs)
// Chanced Item/Fluid Outputs: chanced(item/fluid, chance, chanceBoost)
// mods.gregtech.<RECIPE_MAP>.removeByOutput to remove, mods.gregtech.<RECIPE_MAP>.findByOutput to find (Returns null if no recipes found)
// ALL FIND/REMOVE BY OUTPUT OPTIONS IGNORE THE AMOUNT!
// Four Options:
// [long voltage, Outputs... (see above)] (Matches/Removes any recipe with that output, and that voltage or more)
// [GTRecipeCategory category, Outputs... (see above)] (Matches/Removes any recipe with that output, and that category)
// [Outputs... (see above)] (Matches/Removes any recipe with that output)
// [Predicate<Recipe> predicate, Outputs... (see above)] (Matches/Removes any recipe with that output, and matching that predicate)
mods.gregtech.sifter.removeByOutput(50, [item('minecraft:apple') * 64, item('minecraft:apple') * 64, item('minecraft:apple') * 64], null, [chanced(item('minecraft:apple') * 64, 50, 1)], [chanced(fluid('fluorine') * 2000, 50, 1)])

// NBT Helpers for Recipe Builder
// inputNBT version with IIngredient
// wildInputNBT (parameter of IIngredient)
mods.gregtech.assembler.recipeBuilder()
    .inputNBT(ore('dyeBlue'), NBTMatcher.ANY, NBTCondition.ANY)
    .inputWildNBT(ore('dyeRed')) // Same as above (Except the OreDict of course)
    .outputs(item('minecraft:apple') * 64)
    .EUt(30).duration(VA[LV])
    .buildAndRegister()

// Change Recipes
// NOTE THAT PROPERTIES ARE NOT TRANSFERRED! (CLEANROOM, ASSEMBLY RESEARCH, ETC.)
// THIS IS BECAUSE BUILDERS CAN APPLY THESE THEMSELVES, CAUSING DUPE PROPERTIES!
// Can use Any of the Find by Input or Output Methods (`changeByInput` & `changeByOutput`)
// Simply the normal find method parameters, and returning a consumer of a ChangeRecipeBuilder (for Input)
// and a consumer of a Stream of ChangeRecipeBuilders (for Output)
// If error occurs (could not find recipes), a dummy recipe builder is returned for input, and an empty stream is returned for output.
// Can also call `changeAllRecipes` to change all recipes, filtering by predicate, category, both, or none.
// Note: Calling `replaceAndRegister` removes the original recipe **AND** adds the new one.
// calling `buildAndRegister` just adds the new one.

// Example 1: Changing All PBF recipes to be half duration
// Using Change All Recipes
mods.gregtech.primitive_blast_furnace.changeAllRecipes()
    .forEach { ChangeRecipeBuilder builder ->
        builder.changeDuration(duration -> (int) (duration / 2))
                .replaceAndRegister()
    }

// Example 2: Making All Electronic Circuit Recipes Output Double and require an Apple, whilst Changing (Adding) Recycling
mods.gregtech.circuit_assembler.changeByOutput([metaitem('circuit.electronic') * 2], null) // Excluding Chanced Output Specification
    .forEach { ChangeRecipeBuilder builder ->
        builder.changeEachOutput { stack ->
            stack.count *= 2
            return stack
        }.builder { RecipeBuilder recipe ->
            recipe.inputs(item('minecraft:apple'))
                .changeRecycling()
        }.replaceAndRegister()
    }

// Example 3: Changing a Macerator Recipe to Double the Chance of the Final Chanced Output
mods.gregtech.macerator.changeByInput([metaitem('plant_ball') * 2], null)
    .changeChancedOutputs { List<ChancedItemOutput> chancedOutputs ->
        ChancedItemOutput old = chancedOutputs.get(chancedOutputs.size() - 1)
        chancedOutputs.set(chancedOutputs.size() - 1, new ChancedItemOutput(old.ingredient, old.chance * 2, old.chanceBoost))
    }
    .replaceAndRegister()

// Example 4: Changing the Circuit Meta of a Recipe
mods.gregtech.assembler.changeByOutput([item('minecraft:iron_bars') * 4], null)
    .forEach { ChangeRecipeBuilder builder ->
        builder.changeCircuitMeta { meta -> meta * 2 }
            .replaceAndRegister()
    }

// Example 5: Adding Alternative Chemical Reactor Recipes
// Alternative = `buildAndRegister` not `replaceAndRegister`
mods.gregtech.chemical_reactor.changeByOutput(null, [fluid('polytetrafluoroethylene')]) // Change By Output ignores Amount
    .forEach { ChangeRecipeBuilder builder ->
        builder.changeCircuitMeta { meta -> meta + 10 }
            .builder { RecipeBuilder recipe ->
                recipe.fluidInputs(fluid('rhodium') * 144)
            }
            .changeDuration { duration -> (int) (duration / 2) }
            .changeEachFluidOutput { output ->
                output.amount *= 2
                return output
            }
            .buildAndRegister()
    }

// See {@link com.nomiceu.nomilabs.groovy.ChangeRecipeBuilder} for more functions!
