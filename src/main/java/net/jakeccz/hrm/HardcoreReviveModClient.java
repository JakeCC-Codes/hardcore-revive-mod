package net.jakeccz.hrm;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.jakeccz.hrm.networking.NetworkingPacketRegistry;
import net.jakeccz.hrm.networking.payloads.ForceSavePacket;
import net.jakeccz.hrm.networking.payloads.SyncConfigPacket;
import net.jakeccz.hrm.screen.HeadSelectorScreen;
import net.jakeccz.hrm.screen.ScreenHandlerRegistry;
import net.minecraft.client.gui.screen.ingame.HandledScreens;

public class HardcoreReviveModClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        HandledScreens.register(ScreenHandlerRegistry.CRAFTABLE_HEAD_SCREEN_HANDLER, HeadSelectorScreen::new);

        ClientPlayNetworking.registerGlobalReceiver(SyncConfigPacket.ID, NetworkingPacketRegistry.ClientReceiverPackets::SyncConfig);
        ClientPlayNetworking.registerGlobalReceiver(ForceSavePacket.ID, NetworkingPacketRegistry.ClientReceiverPackets::ForceSave);
    }
}
