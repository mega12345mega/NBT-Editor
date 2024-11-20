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
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVRegistry;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Version;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt.NBTManagers;
import com.luneruniverse.minecraft.mod.nbteditor.nbtreferences.EntityReference;
import com.luneruniverse.minecraft.mod.nbteditor.packets.SummonEntityC2SPacket;
import com.luneruniverse.minecraft.mod.nbteditor.packets.ViewEntityS2CPacket;
import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.ItemTagReferences;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
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
		NbtCompound tag = nbt.getCompound("tag");
		tag.putString("id", nbt.getString("id"));
		tag = MainUtil.updateDynamic(TypeReferences.ENTITY, tag, nbt.get("DataVersion"), defaultDataVersion);
		String id = tag.getString("id");
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
		
		cachedEntity = entityType.create(MainUtil.client.world);
		NBTManagers.ENTITY.setNbt(cachedEntity, nbt);
		
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
			getOrCreateNBT().putString("CustomName", TextInst.toJsonString(name));
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
	public void renderIcon(MatrixStack matrices, int x, int y) {
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
		RenderSystem.applyModelViewMatrix();
		
		DiffuseLighting.method_34742();
		VertexConsumerProvider.Immediate provider = MVDrawableHelper.getVertexConsumerProvider();
		EntityRenderDispatcher dispatcher = MainUtil.client.getEntityRenderDispatcher();
		dispatcher.setRenderShadows(false);
		rotation.applyToEntityRenderDispatcher(dispatcher);
		dispatcher.render(getCachedEntity(), 0, 0, 0, 0, 0, renderMatrices, provider, 0xF000F0);
		dispatcher.setRenderShadows(true);
		provider.draw();
		
		matrices.pop();
		RenderSystem.applyModelViewMatrix();
	}
	
	@Override
	public Optional<ItemStack> toItem() {
		ItemStack output = null;
		for (Item item : MVRegistry.ITEM) {
			if (item instanceof SpawnEggItem spawnEggItem && MVMisc.getEntityType(new ItemStack(spawnEggItem)) == entityType)
				output = new ItemStack(spawnEggItem);
		}
		if (output == null) {
			if (entityType == EntityType.ARMOR_STAND)
				output = new ItemStack(Items.ARMOR_STAND);
			else
				output = new ItemStack(Items.PIG_SPAWN_EGG);
		}
		
		NbtCompound nbt = this.nbt.copy();
		nbt.putString("id", getId().toString());
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
		UUID uuid = (nbt.containsUuid("UUID") ? nbt.getUuid("UUID") : UUID.nameUUIDFromBytes(new byte[] {0, 0, 0, 0}));
		return TextInst.bracketed(getName()).styled(
				style -> style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ENTITY, new HoverEvent.EntityContent(
						entityType, uuid, MainUtil.getNbtNameSafely(nbt, "CustomName", () -> null)))));
	}
	
	public CompletableFuture<Optional<EntityReference>> summon(RegistryKey<World> world, Vec3d pos) {
		return NBTEditorClient.SERVER_CONN
				.sendRequest(requestId -> new SummonEntityC2SPacket(requestId, world, pos, getId(), nbt), ViewEntityS2CPacket.class)
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
