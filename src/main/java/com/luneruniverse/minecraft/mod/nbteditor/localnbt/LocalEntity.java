package com.luneruniverse.minecraft.mod.nbteditor.localnbt;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import com.luneruniverse.minecraft.mod.nbteditor.NBTEditorClient;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.IdentifierInst;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVDrawableHelper;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVMatrix4f;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVMisc;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVQuaternionf;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVTextEvents;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Version;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt.manager.NBTManagers;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.registry.MVRegistry;
import com.luneruniverse.minecraft.mod.nbteditor.nbtreferences.EntityReference;
import com.luneruniverse.minecraft.mod.nbteditor.packets.SummonEntityC2SPacket;
import com.luneruniverse.minecraft.mod.nbteditor.packets.ViewEntityS2CPacket;
import com.luneruniverse.minecraft.mod.nbteditor.server.ServerMVMisc;
import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.ItemTagReferences;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;

import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.vehicle.AbstractBoatEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryKey;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class LocalEntity implements LocalNBT {
	
	public static LocalEntity deserialize(NbtCompound nbt, int defaultDataVersion) {
		NbtCompound tag = nbt.nbte$getCompoundOrDefault("tag");
		tag.putString("id", nbt.nbte$getStringOrDefault("id"));
		tag = MainUtil.updateDynamic(TypeReferences.ENTITY, tag, nbt.get("DataVersion"), defaultDataVersion);
		String id = tag.nbte$getStringOrDefault("id");
		tag.remove("id");
		return new LocalEntity(MVRegistry.ENTITY_TYPE.get(IdentifierInst.of(id)), tag);
	}
	
	private EntityType<?> entityType;
	private NbtCompound nbt;
	
	private Entity cachedEntity;
	private NbtCompound cachedNbt;
	
	public LocalEntity(EntityType<?> entityType, NbtCompound nbt) {
		this.entityType = entityType;
		this.nbt = nbt;
	}
	
	private Entity getCachedEntity() {
		if (cachedEntity != null && cachedEntity.getType() == entityType && Objects.equals(cachedNbt, nbt))
			return cachedEntity;
		
		cachedEntity = ServerMVMisc.createEntity(entityType, MainUtil.client.world);
		NBTManagers.ENTITY.setNbt(cachedEntity, nbt);
		
		if (cachedEntity instanceof LivingEntity livingEntity) {
			// Unlike yaw, these are not set properly by readNbt
			livingEntity.lastHeadYaw = livingEntity.headYaw;
			livingEntity.lastBodyYaw = livingEntity.bodyYaw;
		}
		
		cachedNbt = nbt.copy();
		
		return cachedEntity;
	}
	
	@Override
	public boolean isEmpty(Identifier id) {
		return false;
	}
	
	@Override
	public Text getName() {
		return MainUtil.getNbtNameSafely(nbt, "CustomName", () -> TextInst.of(getDefaultName()));
	}
	@Override
	public void setName(Text name) {
		if (name == null)
			getOrCreateNBT().remove("CustomName");
		else
			getOrCreateNBT().put("CustomName", TextInst.toMinecraft(name));
	}
	@Override
	public String getDefaultName() {
		return entityType.getName().getString();
	}
	
	@Override
	public Identifier getId() {
		return MVRegistry.ENTITY_TYPE.getId(entityType);
	}
	@Override
	public void setId(Identifier id) {
		this.entityType = MVRegistry.ENTITY_TYPE.get(id);
	}
	@Override
	public Set<Identifier> getIdOptions() {
		return MVRegistry.ENTITY_TYPE.getIds();
	}
	
	public EntityType<?> getEntityType() {
		return entityType;
	}
	public void setEntityType(EntityType<?> entityType) {
		this.entityType = entityType;
	}
	
	@Override
	public NbtCompound getNBT() {
		return nbt;
	}
	@Override
	public void setNBT(NbtCompound nbt) {
		this.nbt = nbt;
	}
	@Override
	public NbtCompound getOrCreateNBT() {
		return nbt;
	}
	
	@Override
	public void renderIcon(MatrixStack matrices, int x, int y, float tickDelta) {
		matrices.push();
		matrices.translate(0.0, 8.0, 0.0);
		
		MatrixStack renderMatrices = Version.<MatrixStack>newSwitch()
				.range("1.19.4", null, matrices)
				.range(null, "1.19.3", MatrixStack::new)
				.get();
		
		MVMatrix4f.ofScale(1, 1, -1).applyToPositionMatrix(matrices);
		MVQuaternionf rotation = LocalNBT.makeRotatingIcon(renderMatrices, x, y, 0.75f, true);
		rotation.conjugate();
		if (Version.<Boolean>newSwitch()
				.range("1.21.0", null, true)
				.range(null, "1.20.6", false)
				.get()) {
			rotation.rotateY((float) Math.PI);
		}
		MVDrawableHelper.applyModelViewMatrix();
		
		DiffuseLighting.enableGuiShaderLighting();
		VertexConsumerProvider.Immediate provider = MVDrawableHelper.getVertexConsumerProvider();
		EntityRenderDispatcher dispatcher = MainUtil.client.getEntityRenderDispatcher();
		dispatcher.setRenderShadows(false);
		rotation.applyToEntityRenderDispatcher(dispatcher);
		MVMisc.renderEntity(dispatcher, getCachedEntity(), 0, 0, 0, 0, tickDelta, renderMatrices, provider, 0xF000F0);
		dispatcher.setRenderShadows(true);
		provider.draw();
		
		matrices.pop();
		MVDrawableHelper.applyModelViewMatrix();
	}
	
	@Override
	public Optional<ItemStack> toItem(boolean cleanup) {
		ItemStack output = null;
		for (Item item : MVRegistry.ITEM) {
			if (item instanceof SpawnEggItem spawnEggItem && MVMisc.getEntityType(new ItemStack(spawnEggItem)) == entityType)
				output = new ItemStack(spawnEggItem);
		}
		if (output == null) {
			if (entityType == EntityType.ARMOR_STAND)
				output = new ItemStack(Items.ARMOR_STAND);
			else if (entityType == EntityType.ITEM_FRAME)
				output = new ItemStack(Items.ITEM_FRAME);
			else if (entityType == EntityType.GLOW_ITEM_FRAME)
				output = new ItemStack(Items.GLOW_ITEM_FRAME);
			else if (entityType == EntityType.PAINTING)
				output = new ItemStack(Items.PAINTING);
			else {
				output = Version.<ItemStack>newSwitch()
						.range("1.20.3", null, () -> {
							if (entityType == EntityType.COMMAND_BLOCK_MINECART)
								return new ItemStack(Items.COMMAND_BLOCK_MINECART);
							if (entityType == EntityType.FURNACE_MINECART)
								return new ItemStack(Items.FURNACE_MINECART);
							if (entityType == EntityType.MINECART)
								return new ItemStack(Items.MINECART);
							if (entityType == EntityType.CHEST_MINECART)
								return new ItemStack(Items.CHEST_MINECART);
							if (entityType == EntityType.HOPPER_MINECART)
								return new ItemStack(Items.HOPPER_MINECART);
							if (entityType == EntityType.TNT_MINECART)
								return new ItemStack(Items.TNT_MINECART);
							if (getCachedEntity() instanceof AbstractBoatEntity)
								return new ItemStack(MVMisc.getBoatItem(entityType, nbt));
							return new ItemStack(Items.PIG_SPAWN_EGG);
						})
						.range(null, "1.20.2", () -> new ItemStack(Items.PIG_SPAWN_EGG))
						.get();
			}
		}
		
		NbtCompound nbt = this.nbt.copy();
		nbt.putString("id", getId().toString());
		
		if (cleanup) {
			nbt.remove("Passengers"); // Passengers don't work on spawn eggs
			nbt.remove("UUID");
			nbt.remove("Pos");
			if (entityType == EntityType.ITEM_FRAME || entityType == EntityType.GLOW_ITEM_FRAME ||
					entityType == EntityType.PAINTING) {
				nbt.remove("Rotation");
				Version.newSwitch()
						.range("1.21.5", null, () -> nbt.remove("block_pos"))
						.range(null, "1.21.4", () -> {
							nbt.remove("TileX");
							nbt.remove("TileY");
							nbt.remove("TileZ");
						})
						.run();
				if (entityType == EntityType.PAINTING)
					nbt.remove("facing");
				else
					nbt.remove("Facing");
			}
		}
		
		ItemTagReferences.ENTITY_DATA.set(output, nbt);
		
		return Optional.of(output);
	}
	@Override
	public NbtCompound serialize() {
		NbtCompound output = new NbtCompound();
		output.putString("id", getId().toString());
		output.put("tag", nbt);
		output.putString("type", "entity");
		return output;
	}
	@Override
	public Text toHoverableText() {
		UUID uuid = nbt.nbte$getUuid("UUID").orElseGet(() -> new UUID(0, 0));
		return TextInst.bracketed(getName()).styled(
				style -> style.withHoverEvent(MVTextEvents.HoverAction.SHOW_ENTITY.newEvent(new HoverEvent.EntityContent(
						entityType, uuid, MainUtil.getNbtNameSafely(nbt, "CustomName", () -> null)))));
	}
	
	public CompletableFuture<Optional<EntityReference>> summon(RegistryKey<World> world, Vec3d pos) {
		return NBTEditorClient.SERVER_CONN
				.sendRequest(requestId -> new SummonEntityC2SPacket(requestId, world, pos, getId(), nbt.copy()), ViewEntityS2CPacket.class)
				.thenApply(optional -> optional.filter(ViewEntityS2CPacket::foundEntity)
						.map(packet -> {
							EntityReference ref = new EntityReference(packet.getWorld(), packet.getUUID(),
									MVRegistry.ENTITY_TYPE.get(packet.getId()), packet.getNbt());
							MainUtil.client.player.sendMessage(TextInst.translatable("nbteditor.get.entity")
									.append(ref.getLocalNBT().toHoverableText()), false);
							return ref;
						}));
	}
	
	@Override
	public LocalEntity copy() {
		return new LocalEntity(entityType, nbt.copy());
	}
	
	@Override
	public boolean equals(Object nbt) {
		if (nbt instanceof LocalEntity entity)
			return this.entityType == entity.entityType && this.nbt.equals(entity.nbt);
		return false;
	}
	
}
