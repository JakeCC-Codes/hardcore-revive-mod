package net.jakeccz.hrm.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.jakeccz.hrm.util.HardcoreReviveModConfig;
import net.jakeccz.hrm.util.HardcoreReviveModUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.scoreboard.Team;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Nullables;
import net.minecraft.world.GameMode;
import org.joml.Vector2i;

import java.util.Comparator;

public class HeadSelectorScreen extends HandledScreen<HeadSelectorScreenHandler> {
    public static final Identifier MENU_SELECTION_TEXTURE = HardcoreReviveModUtil.createId("textures/gui/head_selector_gui.png");
    private final ButtonWidget[] buttonCache = new ButtonWidget[handler.getMaxPerPageLength()];
    private CustomPageTurnWidget nextPageButton;
    private CustomPageTurnWidget previousPageButton;
    private int lastPage = 1;
    private static final Comparator<PlayerListEntry> SPECTATOR_PLAYERS = Comparator.comparingInt(entry -> entry.getGameMode() == GameMode.SPECTATOR ? 0 : 1);
    private static final Comparator<PlayerListEntry> ENTRY_ORDERING = SPECTATOR_PLAYERS
            .thenComparing(entry -> Nullables.mapOrElse(entry.getScoreboardTeam(), Team::getName, ""))
            .thenComparing(entry -> entry.getProfile().getName(), String::compareToIgnoreCase);


    public HeadSelectorScreen(HeadSelectorScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Override
    protected void init() {
        super.init();
        backgroundWidth = 147;
        titleX = 48;
        playerInventoryTitleY = 1000;
        if (client == null || client.player == null) { return; }
        handler.sendPlayerList(collectPlayerEntries());
    }

    private PlayerListEntry[] collectPlayerEntries() {
        return this.client.player.networkHandler.getListedPlayerListEntries().stream().sorted(ENTRY_ORDERING).toList().toArray(new PlayerListEntry[0]);
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.setShaderTexture(0, MENU_SELECTION_TEXTURE);
        int x = (width - backgroundWidth) / 2;
        int y = (height - backgroundHeight) / 2;
        Vector2i rightArrowPos = new Vector2i(x + 79, y + 137);
        Vector2i leftArrowPos = new Vector2i(x + 57, y + 137);

        context.drawTexture(RenderLayer::getGuiTexturedOverlay, MENU_SELECTION_TEXTURE, x, y, 0, 0, backgroundWidth, backgroundHeight, 256, 256); //And this...

        if (handler.getCurrentPage() == lastPage) { return; }
        lastPage = handler.getCurrentPage();

        renderOption(context, x, y);
        renderPageButtons(rightArrowPos, leftArrowPos, 149, 0, 11, 17);
    }

    private void renderOption(DrawContext context, int x, int y) {
        Vector2i optionPos = new Vector2i(13, 26);
        int spacing = 21;

        PlayerListEntry[] page = handler.getPage();

        for (ButtonWidget buttonWidget : buttonCache) {
            if (buttonWidget == null) {
                continue;
            }
            remove(buttonWidget);
        }

        for (int i=0; i<page.length; i++) {
            buttonCache[i] = addPlayerOptionButton(page[i], x + optionPos.x, y + optionPos.y + (spacing * i), 121, 20);
            if (HardcoreReviveModConfig.RESTRICT_MENU_ACCESS)
                buttonCache[i].active = page[i].getGameMode() == GameMode.SPECTATOR;
            else
                buttonCache[i].active = true;
        }
        /*
        context.drawTexture(TEXTURE, x + optionPos.x, y + optionPos.y, 13, 171, 121, 20); // Disabled_off
        context.drawTexture(TEXTURE, x + optionPos.x, y + optionPos.y, 13, 191, 121, 20); // Disabled_on
        context.drawTexture(TEXTURE, x + optionPos.x, y + optionPos.y, 13, 211, 121, 20); // Enabled_on
        context.drawTexture(TEXTURE, x + optionPos.x, y + optionPos.y, 13, 231, 121, 20); // Enabled_off
        */
    }
    protected void renderPageButtons(Vector2i rightArrowPos, Vector2i leftArrowPos, int u, int v, int width, int height) {
        this.nextPageButton = this.addDrawableChild(new CustomPageTurnWidget(rightArrowPos.x, rightArrowPos.y, u, v, width, height, button -> handler.nextPage()));
        this.previousPageButton = this.addDrawableChild(new CustomPageTurnWidget(leftArrowPos.x, leftArrowPos.y, u + width +3, v, width, height, button -> handler.previousPage()));

        boolean visibility = handler.getMaxPageLength() > 1;

        previousPageButton.visible = visibility;
        nextPageButton.visible = visibility;
    }
    protected ButtonWidget addPlayerOptionButton(PlayerListEntry player, int x, int y, int width, int height) {
        Text status = Text.translatable("menu.head_selection.status." + (player.getGameMode() == GameMode.SPECTATOR), player.getProfile().getName());
        return this.addDrawableChild(ButtonWidget.builder(status, button -> this.closeMenu(player)).dimensions(x, y, width, height).build());
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (super.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        } else {
            return switch (keyCode) {
                case 266 -> {
                    this.previousPageButton.onPress();
                    yield true;
                }
                case 267 -> {
                    this.nextPageButton.onPress();
                    yield true;
                }
                default -> false;
            };
        }
    }

    protected void closeMenu(PlayerListEntry skullOwner) {
        handler.giveSkull(skullOwner);
        this.close();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
        drawMouseoverTooltip(context, mouseX, mouseY);
    }
}
