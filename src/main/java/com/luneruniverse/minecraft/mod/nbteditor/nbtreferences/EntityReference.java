package com.luneruniverse.minecraft.mod.nbteditor.nbtreferences;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import com.luneruniverse.minecraft.mod.nbteditor.NBTEditorClient;
import com.luneruniverse.minecraft.mod.nbteditor.localnbt.LocalEntity;
import com.luneruniverse.minecraft.mod.nbteditor.packets.GetEntityC2SPacket;
import com.luneruniverse.minecraft.mod.nbteditor.packets.SetEntityC2SPacket;
import com.luneruniverse.minecraft.mod.nbteditor.packets.ViewEntityS2CPacket;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class EntityReference implements NBTReference<LocalEntity> {
	
	public static CompletableFuture<Optional<EntityReference>> getEntity(RegistryKey<World> world, UUID uuid) {
		return NBTEditorClient.SERVER_CONN
				.sendRequest(requestId -> new GetEntityC2SPacket(requestId, world, uuid), ViewEntityS2CPacket.class)
				.thenApply(optional -> optional.filter(ViewEntityS2CPacket::foundEntity)
						.map(packet -> new EntityReference(world, uuid, packet.getId(), packet.getNbt())));
	}
	
	private final RegistryKey<World> world;
	private final UUID uuid;
	private final Identifier id;
	private NbtCompound nbt;
	
	public EntityReference(RegistryKey<World> world, UUID uuid, Identifier id, NbtCompound nbt) {
		this.world = world;
		this.uuid = uuid;
		this.id = id;
		this.nbt = nbt;
	}
	
	public RegistryKey<World> getWorld() {
		return world;
	}
	public UUID getUUID() {
		return uuid;
	}
	
	@Override
	public LocalEntity getLocalNBT() {
		return new LocalEntity(id, nbt);
	}
	
	@Override
	public NbtCompound getNBT() {
		return nbt;
	}
	@Override
	public void saveNBT(Identifier id, NbtCompound toSave, Runnable onFinished) {
		if (!this.id.equals(id))
			throw new IllegalArgumentException("Entities cannot change their type!");
		nbt = toSave;
		ClientPlayNetworking.send(new SetEntityC2SPacket(world, uuid, toSave));
		onFinished.run();
	}
	
	@Override
	public void showParent() {
		MainUtil.client.setScreen(null);
	}
	
}
