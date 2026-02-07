package org.research.api.recipe.transfer;

import com.mojang.logging.LogUtils;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.slf4j.Logger;

@Getter
@Setter
public class RecipeTransferHandler<C extends AbstractContainerMenu, R> {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final RecipeTransferBuilder<C, R> recipeBuilder;

    public RecipeTransferHandler(RecipeTransferBuilder<C, R> recipeBuilder) {
        this.recipeBuilder = recipeBuilder;
    }


    public void transferRecipe(C container, R recipe, LocalPlayer player) {



    }
}
