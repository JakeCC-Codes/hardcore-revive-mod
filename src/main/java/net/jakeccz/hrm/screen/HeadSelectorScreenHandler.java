package net.jakeccz.hrm.screen;

import com.mojang.authlib.GameProfile;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.jakeccz.hrm.item.ItemRegistry;
import net.jakeccz.hrm.networking.NetworkingPacketRegistry;
import net.jakeccz.hrm.networking.payloads.SkullGiverPacket;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.screen.*;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public class HeadSelectorScreenHandler extends ScreenHandler {
    private PlayerListEntry[] playerList;
    private final Hand handWithItem;

    private int maxPages; // area: 107 spacing: 21
    private static final int maxPerPage = 5;
    private PlayerListEntry[][] pages;
    private int currentPage;

    public HeadSelectorScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, null);
    }

    public HeadSelectorScreenHandler(int syncId, PlayerInventory playerInventory, @Nullable Hand handWithItem) {
        this(net.jakeccz.hrm.screen.ScreenHandlerRegistry.CRAFTABLE_HEAD_SCREEN_HANDLER, syncId, playerInventory, handWithItem);
    }

    protected HeadSelectorScreenHandler(ScreenHandlerType<?> type, int syncId, PlayerInventory playerInventory, @Nullable Hand hand) {
        super(type, syncId);
        PlayerEntity player = playerInventory.player;

        this.handWithItem = Objects.requireNonNullElseGet(hand, () -> player.getOffHandStack().getItem() == ItemRegistry.CRAFTABLE_HEAD ? Hand.OFF_HAND : Hand.MAIN_HAND);
    }

    public void sendPlayerList(PlayerListEntry[] playerListEntries) {
        this.playerList = playerListEntries;
        this.maxPages = (this.playerList.length / maxPerPage) +1;
        this.currentPage = 0;

        this.pages = new PlayerListEntry[maxPages][maxPerPage];
        int k = 0;
        for (int i=0; i<maxPages; i++) {
            for (int j=0; j<maxPerPage; j++) {
                if (k > this.playerList.length -1) { continue; }
                this.pages[i][j] = this.playerList[k];
                k++;
            }
        }

    }

    public void giveSkull(PlayerListEntry skullOwner) {
        GameProfile skullOwnerProfile = skullOwner.getProfile();
        ClientPlayNetworking.send(new SkullGiverPacket(skullOwnerProfile.getName(), skullOwnerProfile.getId(), handWithItem == Hand.MAIN_HAND ? 1 : 0));
    }

    public PlayerListEntry[] getPage() {
        PlayerListEntry[] playerEntities = new PlayerListEntry[playerList.length];
        for (int i=0; i<pages[currentPage].length; i++) {
            if (pages[currentPage][i] == null) { continue; }
            playerEntities[i] = pages[currentPage][i];
        }
        return playerEntities;
    }
    public void nextPage() {
        int nextPageNum = currentPage +1;

        if (nextPageNum >= maxPages) { return; }
        currentPage = nextPageNum;
    }
    public void previousPage() {
        int previousPageNum = currentPage -1;

        if (previousPageNum <= 1) { return; }
        currentPage = previousPageNum;
    }

    public int getMaxPageLength() {
        return maxPages;
    }
    public int getMaxPerPageLength() {
        return maxPerPage;
    }
    public int getCurrentPage() {
        return currentPage;
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slot) {
        return null;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return true;
    }
}
