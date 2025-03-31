package com.luneruniverse.minecraft.mod.nbteditor.multiversion;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.lang.invoke.MethodType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.joml.Vector2ic;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.luneruniverse.minecraft.mod.nbteditor.misc.MixinLink;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVShaders.MVShaderAndLayer;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.commands.ClientCommandRegistrationCallback;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.commands.FabricClientCommandSource;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt.NBTManagers;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.serialization.JsonOps;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.ParentElement;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.BookScreen;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.gui.tooltip.TooltipPositioner;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.BlockStateArgumentType;
import net.minecraft.command.argument.ItemStackArgumentType;
import net.minecraft.command.argument.TextArgumentType;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.component.type.SuspiciousStewEffectsComponent;
import net.minecraft.component.type.SuspiciousStewEffectsComponent.StewEffect;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtSizeTracker;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.potion.Potion;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.profiler.Profilers;
import net.minecraft.world.BlockRenderView;

public class MVMisc {
	
	private static final Supplier<Reflection.MethodInvoker> ResourceFactory_getResource =
			Reflection.getOptionalMethod(ResourceFactory.class, "method_14486", MethodType.methodType(Resource.class, Identifier.class));
	private static final Supplier<Reflection.MethodInvoker> Resource_getInputStream =
			Reflection.getOptionalMethod(Resource.class, "method_14482", MethodType.methodType(InputStream.class));
	public static Optional<InputStream> getResource(Identifier id) throws IOException {
		try {
			return Version.<Optional<InputStream>>newSwitch()
					.range("1.19.0", null, () -> MainUtil.client.getResourceManager().getResource(id).map(resource -> {
						try {
							return resource.getInputStream();
						} catch (IOException e) {
							throw new UncheckedIOException(e);
						}
					}))
					.range(null, "1.18.2", () -> {
						Resource resource = ResourceFactory_getResource.get().invoke(MainUtil.client.getResourceManager(), id);
						if (resource == null)
							return Optional.empty();
						return Optional.of(Resource_getInputStream.get().invokeThrowable(UncheckedIOException.class, resource));
					})
					.get();
		} catch (UncheckedIOException e) {
			if (e.getMessage() != null) {
				IOException checkedE = new IOException(e.getMessage(), e.getCause());
				checkedE.setStackTrace(e.getStackTrace());
				throw checkedE;
			}
			throw e.getCause();
		}
	}
	
	public static Object registryAccess;
	private static final Supplier<Reflection.MethodInvoker> ItemStackArgumentType_itemStack =
			Reflection.getOptionalMethod(ItemStackArgumentType.class, "method_9776", MethodType.methodType(ItemStackArgumentType.class));
	public static ItemStackArgumentType getItemStackArg() {
		return Version.<ItemStackArgumentType>newSwitch()
				.range("1.19.0", null, () -> ItemStackArgumentType.itemStack((CommandRegistryAccess) registryAccess))
				.range(null, "1.18.2", () -> ItemStackArgumentType_itemStack.get().invoke(null)) // ItemStackArgumentType.itemStack()
				.get();
	}
	private static final Supplier<Reflection.MethodInvoker> BlockStateArgumentType_blockState =
			Reflection.getOptionalMethod(BlockStateArgumentType.class, "method_9653", MethodType.methodType(BlockStateArgumentType.class));
	public static BlockStateArgumentType getBlockStateArg() {
		return Version.<BlockStateArgumentType>newSwitch()
				.range("1.19.0", null, () -> BlockStateArgumentType.blockState((CommandRegistryAccess) registryAccess))
				.range(null, "1.18.2", () -> BlockStateArgumentType_blockState.get().invoke(null)) // BlockStateArgumentType.blockState()
				.get();
	}
	private static final Supplier<Reflection.MethodInvoker> TextArgumentType_text =
			Reflection.getOptionalMethod(TextArgumentType.class, "method_9281", MethodType.methodType(TextArgumentType.class));
	public static TextArgumentType getTextArg() {
		return Version.<TextArgumentType>newSwitch()
				.range("1.20.5", null, () -> TextArgumentType.text((CommandRegistryAccess) registryAccess))
				.range(null, "1.20.4", () -> TextArgumentType_text.get().invoke(null))
				.get();
	}
	
	public static void registerCommands(Consumer<CommandDispatcher<FabricClientCommandSource>> callback) {
		Version.newSwitch()
				.range("1.19.0", null, () -> ClientCommandRegistrationCallback.EVENT.register((dispatcher, access) -> {
					registryAccess = access;
					callback.accept(dispatcher);
				}))
				.range(null, "1.18.2", () -> ClientCommandRegistrationCallback.EVENT.register((dispatcher, access) -> {
					callback.accept(dispatcher);
				}))
				.run();
	}
	
	public static ButtonWidget newButton(int x, int y, int width, int height, Text message, ButtonWidget.PressAction onPress, MVTooltip tooltip) {
		if (Version.<Boolean>newSwitch()
				.range("1.19.4", null, false)
				.range(null, "1.19.3", true)
				.get()) {
			if (height > 20) {
				y += (height - 20) / 2;
				height = 20;
			}
		}
		final int finalY = y;
		final int finalHeight = height;
		return Version.<ButtonWidget>newSwitch()
				.range("1.19.3", null, () -> {
					Tooltip newTooltip = (tooltip == null ? null : tooltip.toNewTooltip());
					return ButtonWidget.builder(message, onPress).dimensions(x, finalY, width, finalHeight).tooltip(newTooltip).build();
				})
				.range(null, "1.19.2", () -> {
					try {
						Object oldTooltip = (tooltip == null ? MVTooltip.EMPTY : tooltip).toOldTooltip();
						return ButtonWidget.class.getConstructor(int.class, int.class, int.class, int.class, Text.class,
								ButtonWidget.PressAction.class, Reflection.getClass("net.minecraft.class_4185$class_5316"))
								.newInstance(x, finalY, width, finalHeight, message, onPress, oldTooltip);
					} catch (Exception e) {
						throw new RuntimeException("Error creating old button", e);
					}
				})
				.get();
	}
	public static ButtonWidget newButton(int x, int y, int width, int height, Text message, ButtonWidget.PressAction onPress) {
		return newButton(x, y, width, height, message, onPress, null);
	}
	
	public static ButtonWidget newTexturedButton(int x, int y, int width, int height, int hoveredVOffset, Identifier img, ButtonWidget.PressAction onPress, MVTooltip tooltip) {
		ButtonWidget output = Version.<ButtonWidget>newSwitch()
				.range("1.20.2", null, () -> new MVTexturedButtonWidget_1_20_2(
						x, y, width, height, 0, 0, hoveredVOffset, img, width, height + hoveredVOffset, onPress))
				.range(null, "1.20.1", () -> Reflection.newInstance(TexturedButtonWidget.class,
						new Class<?>[] {int.class, int.class, int.class, int.class, int.class, int.class, int.class, Identifier.class, int.class, int.class, ButtonWidget.PressAction.class},
						x, y, width, height, 0, 0, hoveredVOffset, img, width, height + hoveredVOffset, onPress))
				.get();
		if (tooltip != null) {
			Version.newSwitch()
					.range("1.19.3", null, () -> output.setTooltip(tooltip.toNewTooltip()))
					.range(null, "1.19.2", () -> {
						Object oldTooltip = tooltip.toOldTooltip();
						Reflection.getField(ButtonWidget.class, "field_25036", "Lnet/minecraft/class_4185$class_5316;").set(output, oldTooltip);
					})
					.run();
		}
		return output;
	}
	public static ButtonWidget newTexturedButton(int x, int y, int width, int height, int hoveredVOffset, Identifier img, ButtonWidget.PressAction onPress) {
		return newTexturedButton(x, y, width, height, hoveredVOffset, img, onPress, null);
	}
	
	private static final Supplier<Reflection.MethodInvoker> CreativeInventoryScreen_getSelectedTab =
			Reflection.getOptionalMethod(CreativeInventoryScreen.class, "method_2469", MethodType.methodType(int.class));
	private static final Supplier<Reflection.FieldReference> ItemGroup_INVENTORY =
			Reflection.getOptionalField(ItemGroup.class, "field_7918", "Lnet/minecraft/class_1761;");
	private static final Supplier<Reflection.MethodInvoker> ItemGroup_getIndex =
			Reflection.getOptionalMethod(ItemGroup.class, "method_7741", MethodType.methodType(int.class));
	public static boolean isCreativeInventoryTabSelected() {
		if (MainUtil.client.currentScreen instanceof CreativeInventoryScreen screen) {
			return Version.<Boolean>newSwitch()
					.range("1.19.3", null, () -> screen.isInventoryTabSelected())
					.range(null, "1.19.2", () -> // screen.getSelectedTab() == ItemGroup.INVENTORY.getIndex()
							(int) CreativeInventoryScreen_getSelectedTab.get().invoke(screen) ==
							(int) ItemGroup_getIndex.get().invoke(ItemGroup_INVENTORY.get().get(null)))
					.get();
		}
		return false;
	}
	
	private static final Supplier<Reflection.MethodInvoker> Keyboard_setRepeatEvents =
			Reflection.getOptionalMethod(Keyboard.class, "method_1462", MethodType.methodType(void.class, boolean.class));
	public static void setKeyboardRepeatEvents(boolean repeatEvents) {
		Version.newSwitch()
				.range("1.19.3", null, () -> {}) // Repeat events are now always on
				.range(null, "1.19.2", () -> Keyboard_setRepeatEvents.get().invoke(MainUtil.client.keyboard, repeatEvents))
				.run();
	}
	
	public static boolean isValidChar(char c) {
		return c != '§' && c >= ' ' && c != 127;
	}
	public static String stripInvalidChars(String str, boolean allowLinebreaks) {
		StringBuilder output = new StringBuilder();
		for (char c : str.toCharArray()) {
			if (isValidChar(c)) {
				output.append(c);
			} else if (allowLinebreaks && c == '\n') {
				output.append(c);
			}
		}
		return output.toString();
	}
	
	private static final Supplier<Reflection.MethodInvoker> Text_asString =
			Reflection.getOptionalMethod(Text.class, "method_10851", MethodType.methodType(String.class));
	public static String getContent(Text text) {
		return Version.<String>newSwitch()
				.range("1.19.0", null, () -> {
					StringBuilder output = new StringBuilder();
					text.getContent().visit(str -> {
						output.append(str);
						return Optional.empty();
					});
					return output.toString();
				})
				.range(null, "1.18.2", () -> Text_asString.get().invoke(text))
				.get();
	}
	
	private static final Supplier<Reflection.MethodInvoker> TooltipPositioner_getPosition =
			Reflection.getOptionalMethod(() -> TooltipPositioner.class, () -> "method_47944", () ->
			MethodType.methodType(Vector2ic.class, Screen.class, int.class, int.class, int.class, int.class));
	public static Vector2ic getPosition(Object positioner, Screen screen, int x, int y, int width, int height) {
		return Version.<Vector2ic>newSwitch()
				.range("1.20.0", null, () -> ((TooltipPositioner) positioner).getPosition(
						MainUtil.client.getWindow().getScaledWidth(), MainUtil.client.getWindow().getScaledHeight(), x, y, width, height))
				.range("1.19.3", "1.19.4", () -> TooltipPositioner_getPosition.get().invoke(positioner, screen, x, y, width, height))
				.get();
	}
	
	private static final Supplier<Class<?>> SuspiciousStewItem = Reflection.getOptionalClass("net.minecraft.class_1830");
	private static final Supplier<Reflection.MethodInvoker> SuspiciousStewItem_addEffectsToStew =
			Reflection.getOptionalMethod(SuspiciousStewItem, () -> "method_53209", () -> MethodType.methodType(void.class, ItemStack.class, List.class));
	private static final Supplier<Reflection.MethodInvoker> SuspiciousStewItem_addEffectToStew =
			Reflection.getOptionalMethod(SuspiciousStewItem, () -> "method_8021", () -> MethodType.methodType(void.class, ItemStack.class, StatusEffect.class, int.class));
	public static void addEffectToStew(ItemStack item, StatusEffect effect, int duration) {
		Version.newSwitch()
				.range("1.20.5", null, () -> item.apply(MVComponentType.SUSPICIOUS_STEW_EFFECTS, new SuspiciousStewEffectsComponent(List.of()), effects -> effects.with(new StewEffect(Registries.STATUS_EFFECT.getEntry(effect), duration))))
				.range("1.20.2", "1.20.4", () -> SuspiciousStewItem_addEffectsToStew.get().invoke(null, item, List.of(Reflection.newInstance(StewEffect.class, new Class<?>[] {StatusEffect.class, int.class}, effect, duration))))
				.range(null, "1.20.1", () -> SuspiciousStewItem_addEffectToStew.get().invoke(null, item, effect, duration))
				.run();
	}
	
	private static final Supplier<Reflection.MethodInvoker> ClientPlayNetworkHandler_sendPacket =
			Reflection.getOptionalMethod(ClientPlayNetworkHandler.class, "method_2883", MethodType.methodType(void.class, Packet.class));
	public static void sendC2SPacket(Packet<?> packet) {
		Version.newSwitch()
				.range("1.20.2", null, () -> MainUtil.client.getNetworkHandler().sendPacket(packet))
				.range(null, "1.20.1", () -> ClientPlayNetworkHandler_sendPacket.get().invoke(MainUtil.client.getNetworkHandler(), packet))
				.run();
	}
	
	private static final Supplier<Reflection.MethodInvoker> NbtIo_read =
			Reflection.getOptionalMethod(NbtIo.class, "method_10627", MethodType.methodType(NbtCompound.class, DataInput.class));
	private static final Supplier<Reflection.MethodInvoker> NbtIo_readCompressed =
			Reflection.getOptionalMethod(NbtIo.class, "method_10629", MethodType.methodType(NbtCompound.class, InputStream.class));
	private static final Supplier<Reflection.MethodInvoker> NbtIo_write =
			Reflection.getOptionalMethod(NbtIo.class, "method_10628", MethodType.methodType(void.class, NbtCompound.class, DataOutput.class));
	private static final Supplier<Reflection.MethodInvoker> NbtIo_writeCompressed =
			Reflection.getOptionalMethod(NbtIo.class, "method_10634", MethodType.methodType(void.class, NbtCompound.class, OutputStream.class));
	public static NbtCompound nbtInternal(Supplier<NbtCompound> newWrite, Supplier<NbtCompound> oldWrite) throws IOException {
		try {
			return Version.<NbtCompound>newSwitch()
					.range("1.20.3", null, newWrite)
					.range(null, "1.20.2", () -> {
						try {
							return oldWrite.get();
						} catch (RuntimeException e) {
							if (e.getCause() instanceof InvocationTargetException invocationException) {
								if (invocationException.getCause() instanceof IOException ioException)
									throw new UncheckedIOException(ioException);
							}
							throw e;
						}
					})
					.get();
		} catch (UncheckedIOException e) {
			throw e.getCause();
		}
	}
	public static void nbtInternal(Runnable newWrite, Runnable oldWrite) throws IOException {
		nbtInternal(() -> {
			newWrite.run();
			return null;
		}, () -> {
			oldWrite.run();
			return null;
		});
	}
	public static NbtCompound readNbt(InputStream stream) throws IOException {
		return nbtInternal(() -> {
			try {
				return NbtIo.readCompound(new DataInputStream(stream), NbtSizeTracker.ofUnlimitedBytes());
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		}, () -> NbtIo_read.get().invoke(null, new DataInputStream(stream)));
	}
	public static NbtCompound readCompressedNbt(InputStream stream) throws IOException {
		return nbtInternal(() -> {
			try {
				return NbtIo.readCompressed(stream, NbtSizeTracker.ofUnlimitedBytes());
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		}, () -> NbtIo_readCompressed.get().invoke(null, stream));
	}
	public static void writeNbt(NbtCompound nbt, OutputStream stream) throws IOException {
		nbtInternal(() -> {
			try {
				NbtIo.write(nbt, new DataOutputStream(stream));
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		}, () -> NbtIo_write.get().invoke(null, nbt, new DataOutputStream(stream)));
	}
	public static void writeCompressedNbt(NbtCompound nbt, OutputStream stream) throws IOException {
		nbtInternal(() -> {
			try {
				NbtIo.writeCompressed(nbt, stream);
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		}, () -> NbtIo_writeCompressed.get().invoke(null, nbt, stream));
	}
	public static NbtCompound readNbt(File file) throws IOException {
		try (FileInputStream stream = new FileInputStream(file)) {
			return readNbt(stream);
		}
	}
	public static NbtCompound readCompressedNbt(File file) throws IOException {
		try (FileInputStream stream = new FileInputStream(file)) {
			return readCompressedNbt(stream);
		}
	}
	public static void writeNbt(NbtCompound nbt, File file) throws IOException {
		try (FileOutputStream stream = new FileOutputStream(file)) {
			writeNbt(nbt, stream);
		}
	}
	public static void writeCompressedNbt(NbtCompound nbt, File file) throws IOException {
		try (FileOutputStream stream = new FileOutputStream(file)) {
			writeCompressedNbt(nbt, stream);
		}
	}
	
	public static String getClickEventActionName(ClickEvent.Action action) {
		// Should be #getName() until 1.20.2 and #asString() at and after 1.20.3
		// But this seems to be equivalent (at least currently)
		return action.name().toLowerCase();
	}
	public static String getHoverEventActionName(HoverEvent.Action<?> action) {
		// Should be #getName() until 1.20.2 and #asString() at and after 1.20.3
		// But this seems to be equivalent (at least currently)
		String str = action.toString();
		return str.substring("<action ".length(), str.length() - ">".length());
	}
	public static ClickEvent.Action getClickEventAction(String name) {
		// Should be .byName() until 1.20.2 and doesn't have a clear replacement at and after 1.20.3
		// But this seems to be equivalent (at least currently)
		return ClickEvent.Action.valueOf(name.toUpperCase());
	}
	private static final Supplier<Reflection.MethodInvoker> HoverEvent$Action_contentsToJson =
			Reflection.getOptionalMethod(HoverEvent.Action.class, "method_27669", MethodType.methodType(JsonElement.class, Object.class));
	public static JsonElement getHoverEventContentsJson(HoverEvent event) {
		return Version.<JsonElement>newSwitch()
				.range("1.20.3", null, () -> HoverEvent.CODEC.encodeStart(JsonOps.INSTANCE, event).result().orElseThrow().getAsJsonObject().get("contents"))
				.range(null, "1.20.2", () -> HoverEvent$Action_contentsToJson.get().invoke(event.getAction(), event.getValue(event.getAction())))
				.get();
	}
	private static final Supplier<Reflection.MethodInvoker> HoverEvent_fromJson =
			Reflection.getOptionalMethod(HoverEvent.class, "method_27664", MethodType.methodType(HoverEvent.class, JsonObject.class));
	public static HoverEvent getHoverEvent(JsonObject json) {
		return Version.<HoverEvent>newSwitch()
				.range("1.20.3", null, () -> HoverEvent.CODEC.parse(JsonOps.INSTANCE, json).result().orElseThrow())
				.range(null, "1.20.2", () -> HoverEvent_fromJson.get().invoke(null, json))
				.get();
	}
	
	private static final Supplier<Reflection.MethodInvoker> Tessellator_getBuffer =
			Reflection.getOptionalMethod(Tessellator.class, "method_1349", MethodType.methodType(BufferBuilder.class));
	private static final Supplier<Reflection.MethodInvoker> BufferBuilder_begin =
			Reflection.getOptionalMethod(BufferBuilder.class, "method_1328", MethodType.methodType(void.class, VertexFormat.DrawMode.class, VertexFormat.class));
	private static final Supplier<Reflection.MethodInvoker> RenderSystem_setShader =
			Reflection.getOptionalMethod(RenderSystem.class, "setShader", MethodType.methodType(void.class, Supplier.class));
	public static VertexConsumer beginDrawingShader(MatrixStack matrices, MVShaderAndLayer shader) {
		return Version.<VertexConsumer>newSwitch()
				.range("1.20.0", null, () -> MVDrawableHelper.getDrawContext(matrices).vertexConsumers.getBuffer(shader.layer()))
				.range(null, "1.19.4", () -> {
					RenderSystem_setShader.get().invoke(null, (Supplier<ShaderProgram>) () -> shader.shader().shader);
					BufferBuilder builder = Tessellator_getBuffer.get().invoke(Tessellator.getInstance());
					BufferBuilder_begin.get().invoke(builder, shader.layer().getDrawMode(), shader.layer().getVertexFormat());
					return builder;
				})
				.get();
	}
	private static final Supplier<Reflection.MethodInvoker> BufferBuilder_end =
			Reflection.getOptionalMethod(BufferBuilder.class, "method_1326", MethodType.methodType(void.class));
	private static final Supplier<Reflection.MethodInvoker> BufferRenderer_draw =
			Reflection.getOptionalMethod(BufferRenderer.class, "method_1309", MethodType.methodType(void.class, BufferBuilder.class));
	public static void endDrawingShader(MatrixStack matrices, VertexConsumer vertexConsumer) {
		Version.newSwitch()
				.range("1.20.0", null, () -> MVDrawableHelper.getDrawContext(matrices).vertexConsumers.draw())
				.range("1.19.0", "1.19.4", () -> BufferRenderer.drawWithGlobalProgram(((BufferBuilder) vertexConsumer).end()))
				.range(null, "1.18.2", () -> {
					BufferBuilder_end.get().invoke(vertexConsumer);
					BufferRenderer_draw.get().invoke(null, vertexConsumer);
				})
				.run();
	}
	
	private static final Supplier<Reflection.MethodInvoker> TextFieldWidget_setCursor =
			Reflection.getOptionalMethod(TextFieldWidget.class, "method_1883", MethodType.methodType(void.class, int.class));
	public static void setCursor(TextFieldWidget textField, int cursor) {
		Version.newSwitch()
				.range("1.20.2", null, () -> textField.setCursor(cursor, false))
				.range(null, "1.20.1", () -> TextFieldWidget_setCursor.get().invoke(textField, cursor))
				.run();
	}
	
	private static final Supplier<Reflection.MethodInvoker> BlockRenderManager_renderBlock =
			Reflection.getOptionalMethod(BlockRenderManager.class, "method_3355", MethodType.methodType(boolean.class, BlockState.class, BlockPos.class, BlockRenderView.class, MatrixStack.class, VertexConsumer.class, boolean.class, java.util.Random.class));
	public static void renderBlock(BlockRenderManager renderer, BlockState state, BlockPos pos, BlockRenderView world, MatrixStack matrices, VertexConsumer vertexConsumer, boolean cull) {
		Version.newSwitch()
				.range("1.19.0", null, () -> renderer.renderBlock(state, pos, world, matrices, vertexConsumer, cull, Random.create()))
				.range(null, "1.18.2", () -> BlockRenderManager_renderBlock.get().invoke(renderer, state, pos, world, matrices, vertexConsumer, cull, new java.util.Random()))
				.run();
	}
	
	private static final Supplier<Reflection.MethodInvoker> SpawnEggItem_getEntityType_NbtCompound =
			Reflection.getOptionalMethod(SpawnEggItem.class, "method_8015", MethodType.methodType(EntityType.class, NbtCompound.class));
	private static final Supplier<Reflection.MethodInvoker> SpawnEggItem_getEntityType_ItemStack =
			Reflection.getOptionalMethod(SpawnEggItem.class, "method_8015", MethodType.methodType(EntityType.class, ItemStack.class));
	public static EntityType<?> getEntityType(ItemStack item) {
		SpawnEggItem spawnEggItem = (SpawnEggItem) item.getItem();
		return Version.<EntityType<?>>newSwitch()
				.range("1.21.4", null, () -> spawnEggItem.getEntityType(DynamicRegistryManagerHolder.get(), item))
				.range("1.20.5", "1.21.3", () -> SpawnEggItem_getEntityType_ItemStack.get().invoke(spawnEggItem, item))
				.range(null, "1.20.4", () -> SpawnEggItem_getEntityType_NbtCompound.get().invoke(spawnEggItem, item.manager$getNbt()))
				.get();
	}
	
	public static StatusEffectInstance newStatusEffectInstance(StatusEffect effect, int duration) {
		return Version.<StatusEffectInstance>newSwitch()
				.range("1.20.5", null, () -> new StatusEffectInstance(Registries.STATUS_EFFECT.getEntry(effect), duration))
				.range(null, "1.20.4", () -> Reflection.newInstance(StatusEffectInstance.class, new Class<?>[] {StatusEffect.class, int.class}, effect, duration))
				.get();
	}
	public static StatusEffectInstance newStatusEffectInstance(StatusEffect effect, int duration, int amplifier, boolean ambient, boolean showParticles, boolean showIcon) {
		return Version.<StatusEffectInstance>newSwitch()
				.range("1.20.5", null, () -> new StatusEffectInstance(Registries.STATUS_EFFECT.getEntry(effect), duration, amplifier, ambient, showParticles, showIcon))
				.range(null, "1.20.4", () -> Reflection.newInstance(StatusEffectInstance.class, new Class<?>[] {StatusEffect.class, int.class, int.class, boolean.class, boolean.class, boolean.class}, effect, duration, amplifier, ambient, showParticles, showIcon))
				.get();
	}
	
	private static final Supplier<Reflection.MethodInvoker> StatusEffectInstance_getEffectType =
			Reflection.getOptionalMethod(StatusEffectInstance.class, "method_5579", MethodType.methodType(StatusEffect.class));
	public static StatusEffect getEffectType(StatusEffectInstance effect) {
		return Version.<StatusEffect>newSwitch()
				.range("1.20.5", null, () -> effect.getEffectType().value())
				.range(null, "1.20.4", () -> StatusEffectInstance_getEffectType.get().invoke(effect))
				.get();
	}
	
	public static BookScreen.Contents getBookContents(List<Text> pages) {
		if (NBTManagers.COMPONENTS_EXIST)
			return new BookScreen.Contents(pages);
		
		return (BookScreen.Contents) Proxy.newProxyInstance(MVMisc.class.getClassLoader(),
				new Class<?>[] {BookScreen.Contents.class}, (obj, method, args) -> {
			if (method.getName().equals("method_17560")) // getPageCount
				return pages.size();
			if (method.getName().equals("method_17561")) // getPageUnchecked
				return (StringVisitable) pages.get((int) args[0]);
			
			if (method.getName().equals("method_17563")) { // default getPage
				int index = (int) args[0];
				return (index >= 0 && index < pages.size() ? pages.get(index) : StringVisitable.EMPTY);
			}
			
			throw new IllegalArgumentException("Unknown method: " + method);
		});
	}
	
	public static boolean isWrittenBookContents(BookScreen.Contents contents) {
		return Version.<Boolean>newSwitch()
				.range("1.20.5", null, () -> MixinLink.WRITTEN_BOOK_CONTENTS.getIfPresent(contents) != null)
				.range(null, "1.20.4", () -> Reflection.getClass("net.minecraft.class_3872$class_3933").isInstance(contents))
				.get();
	}
	
	private static final Supplier<Class<?>> SystemToast$Type = Reflection.getOptionalClass("net.minecraft.class_370$class_371");
	private static final Object SystemToast$Type_PACK_LOAD_FAILURE =
			Version.<Object>newSwitch()
					.range("1.20.3", null, () -> null)
					.range(null, "1.20.2", () -> Reflection.getField(SystemToast$Type.get(), "field_21809", "Lnet/minecraft/class_370$class_371;").get(null))
					.get();
	public static void showToast(Text title, Text description) {
		MainUtil.client.getToastManager().add(Version.<SystemToast>newSwitch()
				.range("1.20.3", null, () -> new SystemToast(SystemToast.Type.PACK_LOAD_FAILURE, title, description))
				.range(null, "1.20.2", () -> Reflection.newInstance(SystemToast.class,
						new Class<?>[] {SystemToast$Type.get(), Text.class, Text.class},
						SystemToast$Type_PACK_LOAD_FAILURE, title, description))
				.get());
	}
	
	private static final Supplier<Reflection.MethodInvoker> ParentElement_setInitialFocus =
			Reflection.getOptionalMethod(ParentElement.class, "method_20085", MethodType.methodType(void.class, Element.class));
	public static void setInitialFocus(Screen screen, Element element, Consumer<Element> superCall) {
		Version.newSwitch()
				.range("1.19.4", null, () -> {
					superCall.accept(element);
					screen.setFocused(element);
				})
				.range(null, "1.19.3", () -> ParentElement_setInitialFocus.get().invoke(screen, element))
				.run();
	}
	
	private static final Supplier<Reflection.MethodInvoker> VertexConsumer_next =
			Reflection.getOptionalMethod(VertexConsumer.class, "method_1344", MethodType.methodType(void.class));
	public static void nextVertex(VertexConsumer vertexConsumer) {
		Version.newSwitch()
				.range("1.21.0", null, () -> {})
				.range(null, "1.20.6", () -> VertexConsumer_next.get().invoke(vertexConsumer))
				.run();
	}
	
	private static final Supplier<Reflection.MethodInvoker> VertexConsumer_vertex =
			Reflection.getOptionalMethod(VertexConsumer.class, "method_22912", MethodType.methodType(VertexConsumer.class, double.class, double.class, double.class));
	public static VertexConsumer startVertex(VertexConsumer vertexConsumer, double x, double y, double z) {
		return Version.<VertexConsumer>newSwitch()
				.range("1.21.0", null, () -> vertexConsumer.vertex((float) x, (float) y, (float) z))
				.range(null, "1.20.6", () -> VertexConsumer_vertex.get().invoke(vertexConsumer, x, y, z))
				.get();
	}
	
	private static final Supplier<Reflection.MethodInvoker> MinecraftClient_getTickDelta =
			Reflection.getOptionalMethod(MinecraftClient.class, "method_1488", MethodType.methodType(float.class));
	public static float getTickDelta() {
		return Version.<Float>newSwitch()
				.range("1.21.0", null, () -> MainUtil.client.getRenderTickCounter().getTickDelta(true))
				.range(null, "1.20.6", () -> MinecraftClient_getTickDelta.get().invoke(MainUtil.client))
				.get();
	}
	
	public static EquipmentSlot getEquipmentSlot(EquipmentSlot.Type type, int entityId) {
		for (EquipmentSlot slot : EquipmentSlot.values()) {
			if (slot.getType() == type && slot.getEntitySlotId() == entityId)
				return slot;
		}
		throw new IllegalArgumentException("Unknown equipment slot: type=" + type + ", entityId=" + entityId);
	}
	
	public static void onRegistriesLoad(Runnable callback) {
		Version.newSwitch()
				.range("1.20.5", null, () -> DynamicRegistryManagerHolder.onDefaultManagerLoad(callback))
				.range(null, "1.20.4", callback)
				.run();
	}
	
	private static final Supplier<Reflection.MethodInvoker> VertexConsumer_light =
			Reflection.getOptionalMethod(VertexConsumer.class, "method_22916", MethodType.methodType(VertexConsumer.class, int.class));
	public static void setVertexLight(VertexConsumer vertexConsumer, int uv) {
		Version.newSwitch()
				.range("1.21.0", null, () -> vertexConsumer.light(uv))
				.range(null, "1.20.6", () -> VertexConsumer_light.get().invoke(vertexConsumer, uv))
				.run();
	}
	
	public static <T> T withDefaultRegistryManager(Supplier<T> callback) {
		if (NBTManagers.COMPONENTS_EXIST)
			return DynamicRegistryManagerHolder.withDefaultManager(callback);
		return callback.get();
	}
	public static void withDefaultRegistryManager(Runnable callback) {
		if (NBTManagers.COMPONENTS_EXIST)
			DynamicRegistryManagerHolder.withDefaultManager(callback);
		else
			callback.run();
	}
	
	private static final Supplier<Reflection.MethodInvoker> ScreenHandler_setStackInSlot =
			Reflection.getOptionalMethod(ScreenHandler.class, "method_7619", MethodType.methodType(void.class, int.class, ItemStack.class));
	public static void setStackInSlot(ScreenHandler screenHandler, ScreenHandlerSlotUpdateS2CPacket packet) {
		Version.newSwitch()
				.range("1.17.1", null, () -> screenHandler.setStackInSlot(packet.getSlot(), packet.getRevision(), packet.getStack()))
				.range(null, "1.17", () -> ScreenHandler_setStackInSlot.get().invoke(screenHandler, packet.getSlot(), packet.getStack()))
				.run();
	}
	
	private static final Supplier<Reflection.MethodInvoker> TooltipComponent_getHeight =
			Reflection.getOptionalMethod(TooltipComponent.class, "method_32661", MethodType.methodType(int.class));
	public static int getTooltipComponentHeight(TooltipComponent line) {
		return Version.<Integer>newSwitch()
				.range("1.21.2", null, () -> line.getHeight(MainUtil.client.textRenderer))
				.range(null, "1.21.1", () -> TooltipComponent_getHeight.get().invoke(line))
				.get();
	}
	
	private static final Supplier<Reflection.MethodInvoker> Entity_getCommandSource =
			Reflection.getOptionalMethod(Entity.class, "method_5671", MethodType.methodType(ServerCommandSource.class));
	public static ServerCommandSource getCommandSource(Entity entity) {
		return Version.<ServerCommandSource>newSwitch()
				.range("1.21.2", null, () -> new ServerCommandSource(
						CommandOutput.DUMMY, entity.getPos(), entity.getRotationClient(), null, 0,
						entity.getName().getString(), entity.getDisplayName(), null, entity))
				.range(null, "1.21.1", () -> Entity_getCommandSource.get().invoke(entity))
				.get();
	}
	
	private static final Supplier<Reflection.MethodInvoker> MinecraftClient_getProfiler =
			Reflection.getOptionalMethod(MinecraftClient.class, "method_16011", MethodType.methodType(Profiler.class));
	public static Profiler getProfiler() {
		return Version.<Profiler>newSwitch()
				.range("1.21.2", null, () -> Profilers.get())
				.range(null, "1.21.1", () -> MinecraftClient_getProfiler.get().invoke(MainUtil.client))
				.get();
	}
	
	public static PotionContentsComponent newPotionContentsComponent(Optional<RegistryEntry<Potion>> potion, Optional<Integer> customColor, List<StatusEffectInstance> customEffects) {
		return Version.<PotionContentsComponent>newSwitch()
				.range("1.21.2", null, () -> new PotionContentsComponent(potion, customColor, customEffects, Optional.empty()))
				.range(null, "1.21.1", () -> Reflection.newInstance(PotionContentsComponent.class, new Class<?>[] {Optional.class, Optional.class, List.class}, potion, customColor, customEffects))
				.get();
	}
	
	private static final Supplier<Reflection.MethodInvoker> EntityRenderDispatcher_render =
			Reflection.getOptionalMethod(EntityRenderDispatcher.class, "method_3954", MethodType.methodType(void.class, Entity.class, double.class, double.class, double.class, float.class, float.class, MatrixStack.class, VertexConsumerProvider.class, int.class));
	public static void renderEntity(EntityRenderDispatcher dispatcher, Entity entity, double x, double y, double z, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
		Version.newSwitch()
				.range("1.21.2", null, () -> dispatcher.render(entity, x, y, z, tickDelta, matrices, vertexConsumers, light))
				.range(null, "1.21.1", () -> EntityRenderDispatcher_render.get().invoke(dispatcher, entity, x, y, z, yaw, tickDelta, matrices, vertexConsumers, light))
				.run();
	}
	
	// From MinecraftClient#addBlockEntityNbt (1.21.3)
	// Edited to remove x, y, & z
	@SuppressWarnings("deprecation")
	public static void addBlockEntityNbtWithoutXYZ(ItemStack item, BlockEntity entity) {
		NbtCompound blockEntityTag = entity.createComponentlessNbtWithIdentifyingData(DynamicRegistryManagerHolder.get());
		blockEntityTag.remove("x");
		blockEntityTag.remove("y");
		blockEntityTag.remove("z");
		entity.removeFromCopiedStackNbt(blockEntityTag);
		BlockItem.setBlockEntityData(item, entity.getType(), blockEntityTag);
		item.applyComponentsFrom(entity.createComponentMap());
	}
	
	// From BlockEntityRenderDispatcher#renderEntity (1.21.3)
	// Edited to input a tickDelta and use default light and overlay values
	public static <T extends BlockEntity> boolean renderBlockEntity(BlockEntityRenderDispatcher dispatcher, T entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider provider) {
		BlockEntityRenderer<T> renderer = dispatcher.get(entity);
		if (renderer == null)
			return true;
		try {
			renderer.render(entity, tickDelta, matrices, provider, 0xF000F0, OverlayTexture.DEFAULT_UV);
		} catch (Throwable e) {
			CrashReport report = CrashReport.create(e, "Rendering Block Entity");
			CrashReportSection entitySection = report.addElement("Block Entity Details");
			entity.populateCrashReport(entitySection);
			throw new CrashException(report);
		}
		return false;
	}
	
}
