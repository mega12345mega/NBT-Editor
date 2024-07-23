package com.luneruniverse.minecraft.mod.nbteditor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.DynamicRegistryManagerHolder;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Version;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.networking.MVNetworking;
import com.luneruniverse.minecraft.mod.nbteditor.packets.ContainerScreenS2CPacket;
import com.luneruniverse.minecraft.mod.nbteditor.packets.GetBlockC2SPacket;
import com.luneruniverse.minecraft.mod.nbteditor.packets.GetEntityC2SPacket;
import com.luneruniverse.minecraft.mod.nbteditor.packets.GetLecternBlockC2SPacket;
import com.luneruniverse.minecraft.mod.nbteditor.packets.OpenEnderChestC2SPacket;
import com.luneruniverse.minecraft.mod.nbteditor.packets.ProtocolVersionS2CPacket;
import com.luneruniverse.minecraft.mod.nbteditor.packets.SetBlockC2SPacket;
import com.luneruniverse.minecraft.mod.nbteditor.packets.SetCursorC2SPacket;
import com.luneruniverse.minecraft.mod.nbteditor.packets.SetEntityC2SPacket;
import com.luneruniverse.minecraft.mod.nbteditor.packets.SetSlotC2SPacket;
import com.luneruniverse.minecraft.mod.nbteditor.packets.SummonEntityC2SPacket;
import com.luneruniverse.minecraft.mod.nbteditor.packets.ViewBlockS2CPacket;
import com.luneruniverse.minecraft.mod.nbteditor.packets.ViewEntityS2CPacket;
import com.luneruniverse.minecraft.mod.nbteditor.server.NBTEditorServer;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

public class NBTEditor implements ModInitializer {
	
	public static final Logger LOGGER = LogManager.getLogger("nbteditor");
	public static NBTEditorServer SERVER;
	
	@Override
	public void onInitialize() {
		MVNetworking.registerPacket(ContainerScreenS2CPacket.ID, ContainerScreenS2CPacket::new);
		MVNetworking.registerPacket(GetBlockC2SPacket.ID, GetBlockC2SPacket::new);
		MVNetworking.registerPacket(GetEntityC2SPacket.ID, GetEntityC2SPacket::new);
		MVNetworking.registerPacket(GetLecternBlockC2SPacket.ID, GetLecternBlockC2SPacket::new);
		MVNetworking.registerPacket(OpenEnderChestC2SPacket.ID, OpenEnderChestC2SPacket::new);
		MVNetworking.registerPacket(ProtocolVersionS2CPacket.ID, ProtocolVersionS2CPacket::new);
		MVNetworking.registerPacket(SetBlockC2SPacket.ID, SetBlockC2SPacket::new);
		MVNetworking.registerPacket(SetCursorC2SPacket.ID, SetCursorC2SPacket::new);
		MVNetworking.registerPacket(SetEntityC2SPacket.ID, SetEntityC2SPacket::new);
		MVNetworking.registerPacket(SetSlotC2SPacket.ID, SetSlotC2SPacket::new);
		MVNetworking.registerPacket(SummonEntityC2SPacket.ID, SummonEntityC2SPacket::new);
		MVNetworking.registerPacket(ViewBlockS2CPacket.ID, ViewBlockS2CPacket::new);
		MVNetworking.registerPacket(ViewEntityS2CPacket.ID, ViewEntityS2CPacket::new);
		
		SERVER = new NBTEditorServer();
		
		Version.newSwitch()
				.range("1.20.5", null, () -> ServerLifecycleEvents.SERVER_STARTING.register(DynamicRegistryManagerHolder::setServerManager))
				.range(null, "1.20.4", () -> {})
				.run();
	}
	
}
