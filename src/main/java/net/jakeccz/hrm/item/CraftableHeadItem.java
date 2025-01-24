package net.jakeccz.hrm.item;

import net.jakeccz.hrm.screen.HeadSelectorScreenHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.*;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;


public class CraftableHeadItem extends Item {

    public CraftableHeadItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        user.openHandledScreen(createScreenHandlerFactory(hand));
        return ActionResult.PASS;
    }

    private NamedScreenHandlerFactory createScreenHandlerFactory(Hand hand) {
        return new SimpleNamedScreenHandlerFactory((syncId, inventory, player) -> {
            return new HeadSelectorScreenHandler(syncId, inventory, hand);
        }, Text.translatable("menu.head_selection.title"));
    }
}
