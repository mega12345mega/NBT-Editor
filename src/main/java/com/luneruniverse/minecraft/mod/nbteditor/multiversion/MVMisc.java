package com.luneruniverse.minecraft.mod.nbteditor.multiversion;

import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.nio.FloatBuffer;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.joml.Matrix4f;
import org.joml.Vector2ic;
import org.joml.Vector3f;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.commands.ClientCommandRegistrationCallback;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.commands.FabricClientCommandSource;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;
import com.mojang.brigadier.CommandDispatcher;

import net.minecraft.SharedConstants;
import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.tooltip.TooltipPositioner;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.ItemStackArgumentType;
import net.minecraft.item.ItemGroup;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceFactory;
import net.minecraft.resource.ResourceManager;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class MVMisc {
	
	private static final Reflection.MethodInvoker ResourceManager_getResource =
			Reflection.getMethod(Version.<Class<?>>newSwitch()
					.range("1.19.0", null, () -> ResourceFactory.class)
					.range(null, "1.18.2", () -> ResourceManager.class)
					.get(),
					"method_14486",
					MethodType.methodType(Version.<Class<?>>newSwitch()
							.range("1.19.0", null, () -> Optional.class)
							.range(null, "1.18.2", () -> Reflection.getClass("net.minecraft.class_3298"))
							.get(),
							Identifier.class));
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
	
	private static final Supplier<Reflection.MethodInvoker> ItemStackArgumentType_itemStack =
			Reflection.getOptionalMethod(ItemStackArgumentType.class, "method_9776", MethodType.methodType(ItemStackArgumentType.class));
	public static Object registryAccess;
	public static ItemStackArgumentType getItemStackArg() {
		return Version.<ItemStackArgumentType>newSwitch()
				.range("1.19.0", null, () -> ItemStackArgumentType.itemStack((CommandRegistryAccess) registryAccess))
				.range(null, "1.18.2", () -> ItemStackArgumentType_itemStack.get().invoke(null)) // ItemStackArgumentType.itemStack()
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
	
	public static TexturedButtonWidget newTexturedButton(int x, int y, int width, int height, int hoveredVOffset, Identifier img, ButtonWidget.PressAction onPress, MVTooltip tooltip) {
		TexturedButtonWidget output = new TexturedButtonWidget(x, y, width, height, 0, 0, hoveredVOffset, img, width, height + hoveredVOffset, onPress);
		if (tooltip != null) {
			Version.newSwitch()
					.range("1.19.3", null, () -> output.setTooltip(tooltip.toNewTooltip()))
					.range(null, "1.19.2", () -> {
						try {
							Object oldTooltip = tooltip.toOldTooltip();
							Field field = ButtonWidget.class.getDeclaredField(Reflection.getFieldName(ButtonWidget.class,
									"field_25036", "Lnet/minecraft/class_4185$class_5316;"));
							field.setAccessible(true);
							field.set(output, oldTooltip);
						} catch (Exception e) {
							throw new RuntimeException("Error creating old button", e);
						}
					})
					.run();
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
	
	static final Class<?> Matrix4f_class = Version.<Class<?>>newSwitch()
			.range("1.19.3", null, () -> Reflection.getClass("org.joml.Matrix4f"))
			.range(null, "1.19.2", () -> Reflection.getClass("net.minecraft.class_1159"))
			.get();
	private static final Reflection.MethodInvoker MatrixStack_Entry_getPositionMatrix =
			Reflection.getMethod(MatrixStack.Entry.class, "method_23761", MethodType.methodType(Matrix4f_class));
	public static Object getPositionMatrix(MatrixStack.Entry matrix) {
		return MatrixStack_Entry_getPositionMatrix.invoke(matrix);
	}
	
	private static final Supplier<Reflection.MethodInvoker> Matrix4f_copy =
			Reflection.getOptionalMethod(Matrix4f_class, "method_22673", MethodType.methodType(Matrix4f_class));
	public static Object copyMatrix(Object matrix) {
		return Version.newSwitch()
				.range("1.19.3", null, () -> Reflection.newInstance(Matrix4f_class, new Class<?>[] {Reflection.getClass("org.joml.Matrix4fc")}, matrix)) // new Matrix4f((Matrix4f) matrix)
				.range(null, "1.19.2", () -> Matrix4f_copy.get().invoke(matrix))
				.get();
	}
	
	private static final Reflection.MethodInvoker VertexConsumer_vertex =
			Reflection.getMethod(VertexConsumer.class, "method_22918", MethodType.methodType(VertexConsumer.class, Matrix4f_class, float.class, float.class, float.class));
	public static VertexConsumer vertex(VertexConsumer buffer, Object matrix, float x, float y, float z) {
		return VertexConsumer_vertex.invoke(buffer, matrix, x, y, z);
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
				.range("1.20.0", null, () -> ((TooltipPositioner) positioner).getPosition(screen.width, screen.height, x, y, width, height))
				.range("1.19.3", "1.19.4", () -> TooltipPositioner_getPosition.get().invoke(positioner, screen, x, y, width, height))
				.get();
	}
	
	private static final Supplier<Reflection.MethodInvoker> Matrix4f_writeColumnMajor =
			Reflection.getOptionalMethod(MVMisc.Matrix4f_class, "method_4932", MethodType.methodType(void.class, FloatBuffer.class));
	public static float[] getTranslation(MatrixStack matrices) {
		Object matrix = MVMisc.getPositionMatrix(matrices.peek());
		return Version.<float[]>newSwitch()
				.range("1.19.3", null, () -> {
					Vector3f output = ((Matrix4f) matrix).getColumn(3, new Vector3f());
					return new float[] {output.x, output.y, output.z};
				})
				.range(null, "1.19.2", () -> {
					FloatBuffer buffer = FloatBuffer.allocate(16);
					Matrix4f_writeColumnMajor.get().invoke(matrix, buffer); // matrix.writeColumnMajor(buffer)
					float[] output = new float[3];
					buffer.get(12, output);
					return output;
				})
				.get();
	}
	
}
