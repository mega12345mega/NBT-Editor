package com.luneruniverse.minecraft.mod.nbteditor.multiversion;

import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.commands.ClientCommandRegistrationCallback;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.commands.FabricClientCommandSource;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;
import com.mojang.brigadier.CommandDispatcher;

import net.minecraft.SharedConstants;
import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.command.argument.ItemStackArgumentType;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceFactory;
import net.minecraft.resource.ResourceManager;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class MultiVersionMisc {
	
	private static final Supplier<Reflection.MethodInvoker> Text_shallowCopy =
			Reflection.getOptionalMethod(Text.class, "method_27661", MethodType.methodType(MutableText.class));
	public static EditableText copyText(Text text) {
		return new EditableText(switch (Version.get()) {
			case v1_19_4, v1_19_3, v1_19 -> text.copy();
			case v1_18_v1_17 -> Text_shallowCopy.get().invoke(text); // text.shallowCopy()
		});
	}
	
	private static final Reflection.MethodInvoker ResourceManager_getResource =
			Reflection.getMethod(switch (Version.get()) {
				case v1_19_4, v1_19_3, v1_19 -> ResourceFactory.class;
				case v1_18_v1_17 -> ResourceManager.class;
			}, "method_14486", MethodType.methodType(switch (Version.get()) {
				case v1_19_4, v1_19_3, v1_19 -> Optional.class;
				case v1_18_v1_17 -> Reflection.getClass("net.minecraft.class_3298");
			}, Identifier.class));
	private static final Supplier<Reflection.MethodInvoker> Resource_getInputStream =
			Reflection.getOptionalMethod(Reflection.getClass("net.minecraft.class_3298"), "method_14482", MethodType.methodType(InputStream.class));
	@SuppressWarnings("unchecked")
	public static Optional<InputStream> getResource(Identifier id) throws IOException {
		Object output = ResourceManager_getResource.invoke(MinecraftClient.getInstance().getResourceManager(), id);
		if (output instanceof Optional) {
			if (((Optional<Resource>) output).isEmpty())
				return Optional.empty();
			return Optional.of(((Optional<Resource>) output).get().getInputStream());
		}
		if (output == null)
			return Optional.empty();
		return Optional.of(Resource_getInputStream.get().invoke(output));
	}
	
	private static final Supplier<Reflection.MethodInvoker> ItemStackArgumentType_itemStack_registryAccess =
			Reflection.getOptionalMethod(() -> ItemStackArgumentType.class, () -> "method_9776",
			() -> MethodType.methodType(ItemStackArgumentType.class, Reflection.getClass("net.minecraft.class_7157")));
	private static final Supplier<Reflection.MethodInvoker> ItemStackArgumentType_itemStack =
			Reflection.getOptionalMethod(ItemStackArgumentType.class, "method_9776", MethodType.methodType(ItemStackArgumentType.class));
	public static Object registryAccess;
	public static ItemStackArgumentType getItemStackArg() {
		return switch (Version.get()) {
			case v1_19_4, v1_19_3, v1_19 -> ItemStackArgumentType_itemStack_registryAccess.get().invoke(null, registryAccess); // ItemStackArgumentType.itemStack(registryAccess)
			case v1_18_v1_17 -> ItemStackArgumentType_itemStack.get().invoke(null); // ItemStackArgumentType.itemStack()
		};
	}
	
	public static void registerCommands(Consumer<CommandDispatcher<FabricClientCommandSource>> callback) {
		switch (Version.get()) {
			case v1_19_4, v1_19_3, v1_19 -> ClientCommandRegistrationCallback.EVENT.register((dispatcher, access) -> {
				registryAccess = access;
				callback.accept(dispatcher);
			});
			case v1_18_v1_17 -> ClientCommandRegistrationCallback.EVENT.register((dispatcher, access) -> {
				callback.accept(dispatcher);
			});
		}
	}
	
	public static ButtonWidget newButton(int x, int y, int width, int height, Text message, ButtonWidget.PressAction onPress, MultiVersionTooltip tooltip) {
		switch (Version.get()) {
			case v1_19_4 -> {}
			case v1_19_3, v1_19, v1_18_v1_17 -> {
				if (height > 20) {
					y += (height - 20) / 2;
					height = 20;
				}
			}
		}
		return switch (Version.get()) {
			case v1_19_4, v1_19_3 -> {
				Tooltip newTooltip = (tooltip == null ? null : tooltip.toNewTooltip());
				yield ButtonWidget.builder(message, onPress).dimensions(x, y, width, height).tooltip(newTooltip).build();
			}
			case v1_19, v1_18_v1_17 -> {
				try {
					Object oldTooltip = (tooltip == null ? MultiVersionTooltip.EMPTY : tooltip).toOldTooltip();
					yield ButtonWidget.class.getConstructor(int.class, int.class, int.class, int.class, Text.class,
							ButtonWidget.PressAction.class, Reflection.getClass("net.minecraft.class_4185$class_5316"))
							.newInstance(x, y, width, height, message, onPress, oldTooltip);
				} catch (Exception e) {
					throw new RuntimeException("Error creating old button", e);
				}
			}
		};
	}
	public static ButtonWidget newButton(int x, int y, int width, int height, Text message, ButtonWidget.PressAction onPress) {
		return newButton(x, y, width, height, message, onPress, null);
	}
	
	public static TexturedButtonWidget newTexturedButton(int x, int y, int width, int height, int hoveredVOffset, Identifier img, ButtonWidget.PressAction onPress, MultiVersionTooltip tooltip) {
		TexturedButtonWidget output = new TexturedButtonWidget(x, y, width, height, 0, 0, hoveredVOffset, img, width, height + hoveredVOffset, onPress);
		if (tooltip != null) {
			switch (Version.get()) {
				case v1_19_4, v1_19_3 -> output.setTooltip(tooltip.toNewTooltip());
				case v1_19, v1_18_v1_17 -> {
					try {
						Object oldTooltip = tooltip.toOldTooltip();
						Field field = ButtonWidget.class.getDeclaredField(Reflection.getFieldName(ButtonWidget.class,
								"field_25036", "Lnet/minecraft/class_4185$class_5316;"));
						field.setAccessible(true);
						field.set(output, oldTooltip);
					} catch (Exception e) {
						throw new RuntimeException("Error creating old button", e);
					}
				}
			};
		}
		return output;
	}
	public static TexturedButtonWidget newTexturedButton(int x, int y, int width, int height, int hoveredVOffset, Identifier img, ButtonWidget.PressAction onPress) {
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
			return switch (Version.get()) {
				case v1_19_4, v1_19_3 -> screen.isInventoryTabSelected();
				case v1_19, v1_18_v1_17 -> // screen.getSelectedTab() == ItemGroup.INVENTORY.getIndex()
						(int) CreativeInventoryScreen_getSelectedTab.get().invoke(screen) ==
						(int) ItemGroup_getIndex.get().invoke(ItemGroup_INVENTORY.get().get(null));
			};
		}
		return false;
	}
	
	private static final Supplier<Reflection.MethodInvoker> Keyboard_setRepeatEvents =
			Reflection.getOptionalMethod(Keyboard.class, "method_1462", MethodType.methodType(void.class, boolean.class));
	public static void setKeyboardRepeatEvents(boolean repeatEvents) {
		switch (Version.get()) {
			case v1_19_4, v1_19_3 -> {} // Repeat events are now always on
			case v1_19, v1_18_v1_17 -> Keyboard_setRepeatEvents.get().invoke(MainUtil.client.keyboard, repeatEvents);
		}
	}
	
	static final Class<?> Matrix4f_class = switch (Version.get()) {
		case v1_19_4, v1_19_3 -> Reflection.getClass("org.joml.Matrix4f");
		case v1_19, v1_18_v1_17 -> Reflection.getClass("net.minecraft.class_1159");
	};
	private static final Reflection.MethodInvoker MatrixStack_Entry_getPositionMatrix =
			Reflection.getMethod(MatrixStack.Entry.class, "method_23761", MethodType.methodType(Matrix4f_class));
	public static Object getPositionMatrix(MatrixStack.Entry matrix) {
		return MatrixStack_Entry_getPositionMatrix.invoke(matrix);
	}
	
	private static final Supplier<Reflection.MethodInvoker> Matrix4f_copy =
			Reflection.getOptionalMethod(Matrix4f_class, "method_22673", MethodType.methodType(Matrix4f_class));
	public static Object copyMatrix(Object matrix) {
		return switch (Version.get()) {
			case v1_19_4, v1_19_3 -> Reflection.newInstance(Matrix4f_class, new Class<?>[] {Reflection.getClass("org.joml.Matrix4fc")}, matrix); // new Matrix4f((Matrix4f) matrix)
			case v1_19, v1_18_v1_17 -> Matrix4f_copy.get().invoke(matrix);
		};
	}
	
	private static final Reflection.MethodInvoker VertexConsumer_vertex =
			Reflection.getMethod(VertexConsumer.class, "method_22918", MethodType.methodType(VertexConsumer.class, Matrix4f_class, float.class, float.class, float.class));
	public static VertexConsumer vertex(VertexConsumer buffer, Object matrix, float x, float y, float z) {
		return VertexConsumer_vertex.invoke(buffer, matrix, x, y, z);
	}
	
	private static final Supplier<Reflection.MethodInvoker> DrawableHelper_setZOffset =
			Reflection.getOptionalMethod(DrawableHelper.class, "method_25304", MethodType.methodType(void.class, int.class));
	private static final Supplier<Reflection.FieldReference> ItemRenderer_zOffset =
			Reflection.getOptionalField(ItemRenderer.class, "field_4730", "F");
	private static final Supplier<Reflection.MethodInvoker> ItemRenderer_renderInGuiWithOverrides =
			Reflection.getOptionalMethod(ItemRenderer.class, "method_4023", MethodType.methodType(void.class, ItemStack.class, int.class, int.class));
	private static final Supplier<Reflection.MethodInvoker> ItemRenderer_renderGuiItemOverlay =
			Reflection.getOptionalMethod(ItemRenderer.class, "method_4025", MethodType.methodType(void.class, TextRenderer.class, ItemStack.class, int.class, int.class));
	public static final void renderItem(MatrixStack matrices, float zOffset, boolean setScreenZOffset, ItemStack item, int x, int y) {
		ItemRenderer itemRenderer = MainUtil.client.getItemRenderer();
		TextRenderer textRenderer = MainUtil.client.textRenderer;
		switch (Version.get()) {
			case v1_19_4 -> {
				itemRenderer.renderInGuiWithOverrides(matrices, item, x, y);
				itemRenderer.renderGuiItemOverlay(matrices, textRenderer, item, x, y);
			}
			case v1_19_3, v1_19, v1_18_v1_17 -> {
				if (setScreenZOffset)
					DrawableHelper_setZOffset.get().invoke(MainUtil.client.currentScreen, (int) zOffset);
				ItemRenderer_zOffset.get().set(itemRenderer, zOffset);
				ItemRenderer_renderInGuiWithOverrides.get().invoke(itemRenderer, item, x, y);
				ItemRenderer_renderGuiItemOverlay.get().invoke(itemRenderer, textRenderer, item, x, y);
				ItemRenderer_zOffset.get().set(itemRenderer, 0.0F);
				if (setScreenZOffset)
					DrawableHelper_setZOffset.get().invoke(MainUtil.client.currentScreen, 0);
			}
		}
	}
	
	public static String stripInvalidChars(String str, boolean allowLinebreaks) {
		StringBuilder output = new StringBuilder();
		for (char c : str.toCharArray()) {
			if (SharedConstants.isValidChar(c)) {
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
		return switch (Version.get()) {
			case v1_19_4, v1_19_3, v1_19 -> {
				StringBuilder output = new StringBuilder();
				text.getContent().visit(str -> {
					output.append(str);
					return Optional.empty();
				});
				yield output.toString();
			}
			case v1_18_v1_17 -> Text_asString.get().invoke(text);
		};
	}
	
}
