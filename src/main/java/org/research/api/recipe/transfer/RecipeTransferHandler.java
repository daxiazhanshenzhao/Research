package org.research.api.recipe.transfer;

import com.mojang.logging.LogUtils;
import lombok.Getter;
import lombok.Setter;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import org.research.api.init.PacketInit;
import org.research.network.research.TransferRecipePaket;
import org.slf4j.Logger;

import java.util.Collections;
import java.util.List;

@Getter
@Setter
public class RecipeTransferHandler<C extends AbstractContainerMenu, R> {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final RecipeTransferBuilder<C, R> recipeBuilder;

    public RecipeTransferHandler(RecipeTransferBuilder<C, R> recipeBuilder) {
        this.recipeBuilder = recipeBuilder;
    }

    public void transferRecipe(C container, R recipe,Player player, boolean maxTransfer) {



        List<Slot> craftingSlots = Collections.unmodifiableList(recipeBuilder.getRecipeSlots(container,recipe));
        List<Slot> inventorySlots = Collections.unmodifiableList(recipeBuilder.getInventorySlots(container, recipe));

        get(craftingSlots,inventorySlots);

//        TransferRecipePaket paket = new TransferRecipePaket(
//                recipeBuilder.getMenuType(),
//                recipeBuilder.getRecipeSlotIndices(craftingSlots),
//                recipeBuilder.getInventorySlotIndices(inventorySlots),
//                maxTransfer
//        );
//
//        PacketInit.sendToServer(paket);
    }

    public TransferOperation get(List<Slot> craftingSlots, List<Slot> inventorySlots){



    }
}
