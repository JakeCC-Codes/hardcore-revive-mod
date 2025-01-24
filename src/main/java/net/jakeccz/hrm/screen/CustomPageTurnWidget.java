package net.jakeccz.hrm.screen;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.sound.SoundEvents;

public class CustomPageTurnWidget extends ButtonWidget {
    private final int u;
    private final int v;

    protected CustomPageTurnWidget(int x, int y, int u, int v, int width, int height, ButtonWidget.PressAction pressAction) {
        super(x, y, width, height, ScreenTexts.EMPTY, pressAction, DEFAULT_NARRATION_SUPPLIER);
        this.u = u;
        this.v = v;
    }

    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        int selected = this.isHovered() ? 1 : 0;

        context.drawTexture(RenderLayer::getGuiTexturedOverlay, HeadSelectorScreen.MENU_SELECTION_TEXTURE, this.getX(), this.getY(), u, v + ((this.getHeight() +1) * selected), this.getWidth(), this.getHeight(), 256, 256);
    }

    @Override
    public void playDownSound(SoundManager soundManager) {
        soundManager.play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F)); // Custom Sound
    }
}
