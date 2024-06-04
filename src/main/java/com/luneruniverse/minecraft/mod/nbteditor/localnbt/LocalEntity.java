package com.luneruniverse.minecraft.mod.nbteditor.localnbt;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.joml.Matrix4f;

import com.luneruniverse.minecraft.mod.nbteditor.NBTEditorClient;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVDrawableHelper;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVRegistry;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.nbtreferences.EntityReference;
import com.luneruniverse.minecraft.mod.nbteditor.packets.SummonEntityC2SPacket;
import com.luneruniverse.minecraft.mod.nbteditor.packets.ViewEntityS2CPacket;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;

import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
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
	
	public static LocalEntity deserialize(NbtCompound nbt) {
		return new LocalEntity(new Identifier(nbt.getString("id")), nbt.getCompound("tag"));
	}
	
	private Identifier id;
	private NbtCompound nbt;
	
	public LocalEntity(Identifier id, NbtCompound nbt) {
		this.id = id;
		this.nbt = nbt;
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
			getOrCreateNBT().putString("CustomName", Text.Serialization.toJsonString(name));
	}
	@Override
	public String getDefaultName() {
		return MVRegistry.ENTITY_TYPE.get(id).getName().getString();
	}
	
	@Override
	public Identifier getId() {
		return id;
	}
	@Override
	public void setId(Identifier id) {
		this.id = id;
	}
	@Override
	public Set<Identifier> getIdOptions() {
		return MVRegistry.ENTITY_TYPE.getIds();
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
		LocalNBT.makeRotatingIcon(matrices, x, y, 0.75f);
		matrices.multiplyPositionMatrix(new Matrix4f().scaling(1, 1, -1));
		
		DiffuseLighting.method_34742();
		Entity entity = MVRegistry.ENTITY_TYPE.get(id).create(MainUtil.client.world);
		entity.readNbt(nbt);
		VertexConsumerProvider.Immediate provider = MVDrawableHelper.getDrawContext(matrices).getVertexConsumers();
		MainUtil.client.getEntityRenderDispatcher().getRenderer(entity).render(entity, 0, 0, matrices, provider, 255);
		provider.draw();
		
		matrices.pop();
	}
	
	@Override
	public Optional<ItemStack> toItem() {
		EntityType<?> entity = MVRegistry.ENTITY_TYPE.get(id);
		ItemStack output = null;
		for (Item item : MVRegistry.ITEM) {
			if (item instanceof SpawnEggItem spawnEggItem && spawnEggItem.getEntityType(null) == entity)
				output = new ItemStack(spawnEggItem);
		}
		if (output == null) {
			if (entity == EntityType.ARMOR_STAND)
				output = new ItemStack(Items.ARMOR_STAND);
			else
				output = new ItemStack(Items.PIG_SPAWN_EGG);
		}
		
		NbtCompound nbt = this.nbt.copy();
		nbt.putString("id", id.toString());
		output.setSubNbt("EntityTag", nbt);
		
		return Optional.of(output);
	}
	@Override
	public NbtCompound serialize() {
		NbtCompound output = new NbtCompound();
		output.putString("id", id.toString());
		output.put("tag", nbt);
		output.putString("type", "entity");
		return output;
	}
	@Override
	public Text toHoverableText() {
		UUID uuid = (nbt.containsUuid("UUID") ? nbt.getUuid("UUID") : UUID.nameUUIDFromBytes(new byte[] {0, 0, 0, 0}));
		return TextInst.bracketed(getName()).styled(
				style -> style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ENTITY, new HoverEvent.EntityContent(
						MVRegistry.ENTITY_TYPE.get(id), uuid, MainUtil.getNbtNameSafely(nbt, "CustomName", () -> null)))));
	}
	
	public CompletableFuture<Optional<EntityReference>> summon(RegistryKey<World> world, Vec3d pos) {
		return NBTEditorClient.SERVER_CONN
				.sendRequest(requestId -> new SummonEntityC2SPacket(requestId, world, pos, id, nbt), ViewEntityS2CPacket.class)
				.thenApply(optional -> optional.filter(ViewEntityS2CPacket::foundEntity)
						.map(packet -> {
							EntityReference ref = new EntityReference(packet.getWorld(), packet.getUUID(), packet.getId(), packet.getNbt());
							MainUtil.client.player.sendMessage(TextInst.translatable("nbteditor.get.entity")
									.append(ref.getLocalNBT().toHoverableText()), false);
							return ref;
						}));
	}
	
	@Override
	public LocalEntity copy() {
		return new LocalEntity(id, nbt.copy());
	}
	
	@Override
	public boolean equals(Object nbt) {
		if (nbt instanceof LocalEntity entity)
			return this.id.equals(entity.id) && this.nbt.equals(entity.nbt);
		return false;
	}
	
}
