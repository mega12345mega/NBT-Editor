package com.luneruniverse.minecraft.mod.nbteditor.multiversion;

import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;

/**
 * Makes {@link ScreenHandlerSlotUpdateS2CPacket} get passed to the last shown {@link HandledScreen} from the server
 */
public interface PassContainerSlotUpdates {
	
}
