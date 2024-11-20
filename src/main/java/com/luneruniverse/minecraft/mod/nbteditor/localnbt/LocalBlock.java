package com.luneruniverse.minecraft.mod.nbteditor.localnbt;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.DynamicRegistryManagerHolder;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.EditableText;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.IdentifierInst;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVDrawableHelper;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVMatrix4f;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVMisc;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVRegistry;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Version;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt.NBTManagers;
import com.luneruniverse.minecraft.mod.nbteditor.nbtreferences.BlockReference;
import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.ItemTagReferences;
import com.luneruniverse.minecraft.mod.nbteditor.util.BlockStateProperties;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtString;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public class LocalBlock implements LocalNBT {
	
	public static LocalBlock deserialize(NbtCompound nbt, int defaultDataVersion) {
		NbtElement dataVersion = nbt.get("DataVersion");
		
		String id = MainUtil.updateDynamic(TypeReferences.BLOCK_NAME, NbtString.of(nbt.getString("id")), dataVersion, defaultDataVersion).value;
		Block block = MVRegistry.BLOCK.get(IdentifierInst.of(id));
		
		BlockStateProperties state = new BlockStateProperties(block.getDefaultState());
		state.setValues(MainUtil.updateDynamic(TypeReferences.BLOCK_STATE, nbt.getCompound("state"), dataVersion, defaultDataVersion));
		
		NbtCompound tag = null;
		if (nbt.contains("tag", NbtElement.COMPOUND_TYPE)) {
			tag = nbt.getCompound("tag");
			tag.putString("id", nbt.getString("id"));
			tag = MainUtil.updateDynamic(TypeReferences.BLOCK_ENTITY, tag, dataVersion, defaultDataVersion);
			tag.remove("id");
		}
		
		return new LocalBlock(block, state, tag);
	}
	
	private Block block;
	private BlockStateProperties state;
	private NbtCompound nbt;
	
	private BlockEntity cachedBlockEntity;
	private BlockStateProperties cachedState;
	private NbtCompound cachedNbt;
	
	public LocalBlock(Block block, BlockStateProperties state, NbtCompound nbt) {
		this.block = block;
		this.state = state;
		this.nbt = nbt;
	}
	
	private BlockEntity getCachedBlockEntity() {
		if (!(block instanceof BlockEntityProvider entityProvider))
			return null;
		
		if (cachedBlockEntity != null && cachedBlockEntity.getCachedState().getBlock() == block &&
				cachedState.equals(state) && Objects.equals(cachedNbt, nbt)) {
			return cachedBlockEntity;
		}
		
		cachedBlockEntity = entityProvider.createBlockEntity(new BlockPos(0, 1000, 0), state.applyTo(block.getDefaultState()));
		cachedBlockEntity.setWorld(MainUtil.client.world);
		if (nbt != null)
			NBTManagers.BLOCK_ENTITY.setNbt(cachedBlockEntity, nbt);
		
		cachedState = state.copy();
		cachedNbt = nbt.copy();
		
		return cachedBlockEntity;
	}
	
	public boolean isBlockEntity() {
		return block instanceof BlockEntityProvider;
	}
	
	@Override
	public boolean isEmpty(Identifier id) {
		return MVRegistry.BLOCK.get(id) == Blocks.AIR;
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
		return ((Text) block.getName()).getString();
	}
	
	@Override
	public Identifier getId() {
		return MVRegistry.BLOCK.getId(block);
	}
	@Override
	public void setId(Identifier id) {
		this.block = MVRegistry.BLOCK.get(id);
		this.state = this.state.mapTo(block.getDefaultState());
	}
	@Override
	public Set<Identifier> getIdOptions() {
		return MVRegistry.BLOCK.getIds();
	}
	
	public Block getBlock() {
		return block;
	}
	public void setBlock(Block block) {
		this.block = block;
	}
	
	public BlockStateProperties getState() {
		return state;
	}
	public void setState(BlockStateProperties state) {
		this.state = state;
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
	public void renderIcon(MatrixStack matrices, int x, int y) {
		RenderSystem.disableCull();
		
		matrices.push();
		MatrixStack renderMatrices = Version.<MatrixStack>newSwitch()
				.range("1.19.4", null, matrices)
				.range(null, "1.19.3", MatrixStack::new)
				.get();
		MVMatrix4f.ofScale(1, 1, -1).applyToPositionMatrix(renderMatrices);
		LocalNBT.makeRotatingIcon(renderMatrices, x, y, 1, true);
		renderMatrices.translate(-0.5, -0.5, -0.5);
		
		VertexConsumerProvider.Immediate provider = MVDrawableHelper.getVertexConsumerProvider();
		MVMisc.renderBlock(MainUtil.client.getBlockRenderManager(), state.applyTo(block.getDefaultState()),
				new BlockPos(0, 1000, 0), MainUtil.client.world, renderMatrices,
				provider.getBuffer(RenderLayer.getCutout()), false);
		if (isBlockEntity()) {
			MainUtil.client.getBlockEntityRenderDispatcher().renderEntity(getCachedBlockEntity(),
					renderMatrices, provider, 0xF000F0, OverlayTexture.DEFAULT_UV);
		}
		provider.draw();
		
		matrices.pop();
		
		RenderSystem.enableCull();
	}
	
	@Override
	public Optional<ItemStack> toItem() {
		for (Item item : MVRegistry.ITEM) {
			if (item instanceof BlockItem blockItem && blockItem.getBlock() == block) {
				ItemStack output = new ItemStack(blockItem);
				if (nbt != null) {
					if (NBTManagers.COMPONENTS_EXIST) {
						if (block instanceof BlockEntityProvider provider) {
							BlockEntity entity = provider.createBlockEntity(new BlockPos(0, 1000, 0), state.applyTo(block.getDefaultState()));
							entity.setWorld(MainUtil.client.world);
							NBTManagers.BLOCK_ENTITY.setNbt(entity, nbt);
							MainUtil.client.addBlockEntityNbt(output, entity, DynamicRegistryManagerHolder.getManager());
							
							NbtCompound blockEntityDataTag = ItemTagReferences.BLOCK_ENTITY_DATA.get(output);
							blockEntityDataTag.remove("x");
							blockEntityDataTag.remove("y");
							blockEntityDataTag.remove("z");
							ItemTagReferences.BLOCK_ENTITY_DATA.set(output, blockEntityDataTag);
						}
					} else {
						NbtCompound nbt = new NbtCompound();
						nbt.put("BlockEntityTag", this.nbt);
						output.manager$setNbt(nbt);
					}
				}
				ItemTagReferences.BLOCK_STATE.set(output, state.getValuesMap());
				return Optional.of(output);
			}
		}
		return Optional.empty();
	}
	@Override
	public NbtCompound serialize() {
		NbtCompound output = new NbtCompound();
		output.putString("id", getId().toString());
		output.put("state", state.getValues());
		if (nbt != null && (!nbt.isEmpty() || isBlockEntity()))
			output.put("tag", nbt);
		output.putString("type", "block");
		return output;
	}
	@Override
	public Text toHoverableText() {
		EditableText tooltip = TextInst.translatable("gui.entity_tooltip.type", block.getName());
		if (!state.getProperties().isEmpty())
			tooltip.append("\n" + state);
		Text customName = MainUtil.getNbtNameSafely(nbt, "CustomName", () -> null);
		if (customName != null)
			tooltip = TextInst.literal("").append(customName).append("\n").append(tooltip);
		final Text finalTooltip = tooltip;
		return TextInst.bracketed(getName()).styled(
				style -> style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, finalTooltip)));
	}
	
	public BlockReference place(BlockPos pos) {
		BlockReference ref = BlockReference.getBlockWithoutNBT(pos);
		ref.saveLocalNBT(this, TextInst.translatable("nbteditor.get.block").append(toHoverableText()));
		return ref;
	}
	
	@Override
	public LocalBlock copy() {
		return new LocalBlock(block, state.copy(), nbt == null ? null : nbt.copy());
	}
	
	@Override
	public boolean equals(Object nbt) {
		if (nbt instanceof LocalBlock block)
			return this.block == block.block && this.state.equals(block.state) && Objects.equals(this.nbt, block.nbt);
		return false;
	}
	
}
