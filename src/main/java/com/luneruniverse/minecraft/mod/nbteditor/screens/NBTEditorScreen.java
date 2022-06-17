package com.luneruniverse.minecraft.mod.nbteditor.screens;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.lwjgl.glfw.GLFW;

import com.luneruniverse.minecraft.mod.nbteditor.util.ItemReference;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;
import com.luneruniverse.minecraft.mod.nbteditor.util.NbtFormatter;
import com.luneruniverse.minecraft.mod.nbteditor.util.StringNbtWriterQuoted;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.command.argument.NbtElementArgumentType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.AbstractNbtList;
import net.minecraft.nbt.AbstractNbtNumber;
import net.minecraft.nbt.NbtByte;
import net.minecraft.nbt.NbtByteArray;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtDouble;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtFloat;
import net.minecraft.nbt.NbtInt;
import net.minecraft.nbt.NbtIntArray;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtLong;
import net.minecraft.nbt.NbtLongArray;
import net.minecraft.nbt.NbtShort;
import net.minecraft.nbt.NbtString;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidIdentifierException;
import net.minecraft.util.registry.Registry;

public class NBTEditorScreen extends Screen {
	
	public static interface MenuGenerator {
		public List<NBTValue> getElements(NBTEditorScreen screen, NbtElement source);
		public NbtElement getElement(NbtElement source, String key);
		public default boolean hasEmptyKey(NBTEditorScreen screen, NbtElement source) {
			List<NBTValue> elements = getElements(screen, source);
			if (elements == null)
				return false;
			return elements.stream().anyMatch(value -> value.getKey().isEmpty());
		}
		public void setElement(NbtElement source, String key, NbtElement value);
		public void addElement(NBTEditorScreen screen, NbtElement source, Consumer<String> requestOverwrite, String force);
		public void removeElement(NbtElement source, String key);
		public void pasteElement(NbtElement source, String key, NbtElement value);
		public boolean renameElement(NbtElement source, String key, String newKey, boolean force);
	}
	public static class ListMenuGenerator<T extends NbtElement, L extends AbstractNbtList<? extends NbtElement>> implements MenuGenerator {
		private final T defaultValue;
		public ListMenuGenerator(T defaultValue) {
			this.defaultValue = defaultValue;
		}
		
		@SuppressWarnings("unchecked")
		@Override
		public List<NBTValue> getElements(NBTEditorScreen screen, NbtElement source) {
			AbstractNbtList<? extends NbtElement> nbt = (AbstractNbtList<T>) source;
			List<NBTValue> output = new ArrayList<>();
			for (int i = 0; i < nbt.size(); i++)
				output.add(new NBTValue(screen, i + "", nbt.get(i), nbt));
			return output;
		}
		@SuppressWarnings("unchecked")
		@Override
		public NbtElement getElement(NbtElement source, String key) {
			try {
				return ((AbstractNbtList<T>) source).get(Integer.parseInt(key));
			} catch (NumberFormatException e) {
				return null;
			}
		}
		@SuppressWarnings("unchecked")
		@Override
		public void setElement(NbtElement source, String key, NbtElement value) {
			try {
				AbstractNbtList<T> list = (AbstractNbtList<T>) source;
				int index = Integer.parseInt(key);
				if (list.size() == 1 && index == 0 && list instanceof NbtList) {
					NbtList nonGenericList = (NbtList) list;
					nonGenericList.remove(0);
					nonGenericList.add(value);
				} else if (list.getHeldType() == value.getType())
					list.set(index, (T) value);
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
		}
		@SuppressWarnings("unchecked")
		@Override
		public void addElement(NBTEditorScreen screen, NbtElement source, Consumer<String> requestOverwrite, String force) {
			((AbstractNbtList<T>) source).add((T) defaultValue.copy());
			requestOverwrite.accept(null);
		}
		@SuppressWarnings("unchecked")
		@Override
		public void removeElement(NbtElement source, String key) {
			try {
				((AbstractNbtList<T>) source).remove(Integer.parseInt(key));
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
		}
		@SuppressWarnings("unchecked")
		@Override
		public void pasteElement(NbtElement source, String key, NbtElement value) {
			AbstractNbtList<T> list = (AbstractNbtList<T>) source;
			if (list.getHeldType() == value.getType())
				list.add((T) value);
			else if (list.getHeldType() == NbtType.STRING)
				list.add((T) NbtString.of(value.toString()));
			else if (value instanceof AbstractNbtNumber) {
				AbstractNbtNumber num = (AbstractNbtNumber) value;
				
				switch (list.getHeldType()) {
					case NbtType.BYTE:
						list.add((T) NbtByte.of(num.byteValue()));
						break;
					case NbtType.SHORT:
						list.add((T) NbtShort.of(num.shortValue()));
						break;
					case NbtType.INT:
						list.add((T) NbtInt.of(num.intValue()));
						break;
					case NbtType.LONG:
						list.add((T) NbtLong.of(num.longValue()));
						break;
					case NbtType.FLOAT:
						list.add((T) NbtFloat.of(num.floatValue()));
						break;
					case NbtType.DOUBLE:
						list.add((T) NbtDouble.of(num.doubleValue()));
						break;
				}
			}
		}
		@SuppressWarnings("unchecked")
		@Override
		public boolean renameElement(NbtElement source, String key, String newKey, boolean force) {
			AbstractNbtList<T> list = (AbstractNbtList<T>) source;
			try {
				NbtElement value = getElement(source, key);
				int keyInt = Integer.parseInt(key);
				int newKeyInt = Integer.parseInt(newKey);
				if (newKeyInt < 0)
					throw new NumberFormatException(newKeyInt + " is less than 0!");
				
				if (newKeyInt >= list.size()) {
					list.remove(keyInt);
					list.add((T) value);
				} else {
					list.remove(keyInt);
					list.add(newKeyInt, (T) value);
				}
				
				return true;
			} catch (NumberFormatException e) {
				e.printStackTrace();
				return true;
			}
		}
	}
	public static final Map<Integer, MenuGenerator> menuGenerators;
	public static final Map<Integer, MenuGenerator> listMenuGenerators;
	static {
		menuGenerators = new HashMap<>();
		menuGenerators.put(NbtType.COMPOUND, new MenuGenerator() {
			@Override
			public List<NBTValue> getElements(NBTEditorScreen screen, NbtElement source) {
				NbtCompound nbt = (NbtCompound) source;
				return nbt.getKeys().stream().map(key -> new NBTValue(screen, key, nbt.get(key))).collect(Collectors.toList());
			}
			@Override
			public NbtElement getElement(NbtElement source, String key) {
				return ((NbtCompound) source).get(key);
			}
			@Override
			public void setElement(NbtElement source, String key, NbtElement value) {
				((NbtCompound) source).put(key, value);
			}
			@Override
			public void addElement(NBTEditorScreen screen, NbtElement source, Consumer<String> requestOverwrite, String force) {
				Consumer<String> main = key -> {
					NbtCompound nbt = (NbtCompound) source;
					if (nbt.contains(key) && force == null)
						requestOverwrite.accept(key);
					else {
						nbt.put(key, NbtInt.of(0));
						requestOverwrite.accept(null);
					}
				};
				if (force == null)
					screen.getKey(main);
				else
					main.accept(force);
			}
			@Override
			public void removeElement(NbtElement source, String key) {
				((NbtCompound) source).remove(key);
			}
			@Override
			public void pasteElement(NbtElement source, String key, NbtElement value) {
				NbtCompound nbt = (NbtCompound) source;
				if (nbt.contains(key)) {
					if (nbt.contains(key + " - Copy")) {
						int i = 2;
						while (nbt.contains(key + " - Copy (" + i + ")"))
							i++;
						key += " - Copy (" + i + ")";
					} else
						key += " - Copy";
				}
				nbt.put(key, value);
			}
			@Override
			public boolean renameElement(NbtElement source, String key, String newKey, boolean force) {
				NbtCompound nbt = (NbtCompound) source;
				if (nbt.contains(newKey) && !force)
					return false;
				NbtElement value = nbt.get(key);
				nbt.remove(key);
				nbt.put(newKey, value);
				return true;
			}
		});
		
		listMenuGenerators = new HashMap<>();
		
		listMenuGenerators.put(NbtType.BYTE, new ListMenuGenerator<NbtByte, NbtByteArray>(NbtByte.of((byte) 0)));
		listMenuGenerators.put(NbtType.INT, new ListMenuGenerator<NbtInt, NbtIntArray>(NbtInt.of(0)));
		listMenuGenerators.put(NbtType.LONG, new ListMenuGenerator<NbtLong, NbtLongArray>(NbtLong.of(0L)));
		
		listMenuGenerators.put(NbtType.BYTE_ARRAY, new ListMenuGenerator<NbtByteArray, NbtList>(new NbtByteArray(new byte[0])));
		listMenuGenerators.put(NbtType.COMPOUND, new ListMenuGenerator<NbtCompound, NbtList>(new NbtCompound()));
		listMenuGenerators.put(NbtType.DOUBLE, new ListMenuGenerator<NbtDouble, NbtList>(NbtDouble.of(0)));
		listMenuGenerators.put(NbtType.FLOAT, new ListMenuGenerator<NbtFloat, NbtList>(NbtFloat.of(0)));
		listMenuGenerators.put(NbtType.INT_ARRAY, new ListMenuGenerator<NbtIntArray, NbtList>(new NbtIntArray(new int[0])));
		listMenuGenerators.put(NbtType.LIST, new ListMenuGenerator<NbtList, NbtList>(new NbtList()));
		listMenuGenerators.put(NbtType.LONG_ARRAY, new ListMenuGenerator<NbtLongArray, NbtList>(new NbtLongArray(new long[0])));
		listMenuGenerators.put(NbtType.SHORT, new ListMenuGenerator<NbtShort, NbtList>(NbtShort.of((short) 0)));
		listMenuGenerators.put(NbtType.STRING, new ListMenuGenerator<NbtString, NbtList>(NbtString.of("")));
		
		listMenuGenerators.put(0, listMenuGenerators.get(NbtType.INT));
		
		MenuGenerator listMenuGeneratorSwitch = new MenuGenerator() {
			@Override
			public List<NBTValue> getElements(NBTEditorScreen screen, NbtElement source) {
				return getRealGen(source).getElements(screen, source);
			}
			@Override
			public NbtElement getElement(NbtElement source, String key) {
				return getRealGen(source).getElement(source, key);
			}
			@Override
			public void setElement(NbtElement source, String key, NbtElement value) {
				getRealGen(source).setElement(source, key, value);
			}
			@Override
			public void addElement(NBTEditorScreen screen, NbtElement source, Consumer<String> requestOverwrite, String force) {
				getRealGen(source).addElement(screen, source, requestOverwrite, force);
			}
			@Override
			public void removeElement(NbtElement source, String key) {
				getRealGen(source).removeElement(source, key);
			}
			@Override
			public void pasteElement(NbtElement source, String key, NbtElement value) {
				getRealGen(source).pasteElement(source, key, value);
			}
			@Override
			public boolean renameElement(NbtElement source, String key, String newKey, boolean force) {
				return getRealGen(source).renameElement(source, key, newKey, force);
			}
			@SuppressWarnings("unchecked")
			private MenuGenerator getRealGen(NbtElement source) {
				return listMenuGenerators.get((int) ((AbstractNbtList<? extends NbtElement>) source).getHeldType());
			}
		};
		menuGenerators.put(NbtType.LIST, listMenuGeneratorSwitch);
		menuGenerators.put(NbtType.BYTE_ARRAY, listMenuGeneratorSwitch);
		menuGenerators.put(NbtType.INT_ARRAY, listMenuGeneratorSwitch);
		menuGenerators.put(NbtType.LONG_ARRAY, listMenuGeneratorSwitch);
		
		menuGenerators.put(NbtType.STRING, new MenuGenerator() {
			@Override
			public List<NBTValue> getElements(NBTEditorScreen screen, NbtElement source) {
				NbtElement nbt = getRealNbt(source);
				if (nbt == null)
					return null;
				MenuGenerator gen = menuGenerators.get((int) nbt.getType());
				if (gen == null || gen == this)
					return null;
				return gen.getElements(screen, nbt);
			}
			@Override
			public NbtElement getElement(NbtElement source, String key) {
				NbtElement nbt = getRealNbt(source);
				if (nbt == null)
					return null;
				MenuGenerator gen = menuGenerators.get((int) nbt.getType());
				if (gen == null || gen == this)
					return null;
				return gen.getElement(nbt, key);
			}
			@Override
			public void setElement(NbtElement source, String key, NbtElement value) {
				NbtElement nbt = getRealNbt(source);
				if (nbt == null)
					return;
				MenuGenerator gen = menuGenerators.get((int) nbt.getType());
				if (gen == null || gen == this)
					return;
				gen.setElement(nbt, key, value);
				save(source, nbt);
			}
			@Override
			public void addElement(NBTEditorScreen screen, NbtElement source, Consumer<String> requestOverwrite, String force) {
				NbtElement nbt = getRealNbt(source);
				if (nbt == null)
					return;
				MenuGenerator gen = menuGenerators.get((int) nbt.getType());
				if (gen == null || gen == this)
					return;
				gen.addElement(screen, nbt, force2 -> {
					if (force2 == null)
						save(source, nbt);
					requestOverwrite.accept(force2);
				}, force);
			}
			@Override
			public void removeElement(NbtElement source, String key) {
				NbtElement nbt = getRealNbt(source);
				if (nbt == null)
					return;
				MenuGenerator gen = menuGenerators.get((int) nbt.getType());
				if (gen == null || gen == this)
					return;
				gen.removeElement(nbt, key);
				save(source, nbt);
			}
			@Override
			public void pasteElement(NbtElement source, String key, NbtElement value) {
				NbtElement nbt = getRealNbt(source);
				if (nbt == null)
					return;
				MenuGenerator gen = menuGenerators.get((int) nbt.getType());
				if (gen == null || gen == this)
					return;
				gen.pasteElement(nbt, key, value);
				save(source, nbt);
			}
			public boolean renameElement(NbtElement source, String key, String newKey, boolean force) {
				NbtElement nbt = getRealNbt(source);
				if (nbt == null)
					return true;
				MenuGenerator gen = menuGenerators.get((int) nbt.getType());
				if (gen == null || gen == this)
					return true;
				boolean output = gen.renameElement(nbt, key, newKey, force);
				save(source, nbt);
				return output;
			}
			private NbtElement getRealNbt(NbtElement str) {
				try {
					return NbtElementArgumentType.nbtElement().parse(new StringReader(((NbtString) str).asString()));
				} catch (CommandSyntaxException e) {
					return null;
				}
			}
			private void save(NbtElement source, NbtElement nbt) {
				((NbtString) source).value = new StringNbtWriterQuoted().apply(nbt);
			}
		});
	}
	
	public MenuGenerator gen = new MenuGenerator() {
		@Override
		public List<NBTValue> getElements(NBTEditorScreen screen, NbtElement source) {
			return currentGen.getElements(screen, source);
		}
		@Override
		public NbtElement getElement(NbtElement source, String key) {
			return currentGen.getElement(source, key);
		}
		@Override
		public void setElement(NbtElement source, String key, NbtElement value) {
			currentGen.setElement(source, key, value);
			recursiveUpdate(source);
		}
		@Override
		public void addElement(NBTEditorScreen screen, NbtElement source, Consumer<String> requestOverwrite, String force) {
			currentGen.addElement(screen, source, request -> {
				if (request == null)
					recursiveUpdate(source);
				requestOverwrite.accept(request);
			}, force);
		}
		@Override
		public void removeElement(NbtElement source, String key) {
			currentGen.removeElement(source, key);
			recursiveUpdate(source);
		}
		@Override
		public void pasteElement(NbtElement source, String key, NbtElement value) {
			currentGen.pasteElement(source, key, value);
			recursiveUpdate(source);
		}
		@Override
		public boolean renameElement(NbtElement source, String key, String newKey, boolean force) {
			boolean output = currentGen.renameElement(source, key, newKey, force);
			recursiveUpdate(source);
			return output;
		}
		
		private void recursiveUpdate(NbtElement source) {
			List<NbtElement> path = new ArrayList<>();
			NbtElement lastPart = item.getOrCreateNbt();
			for (String part : realPath) {
				MenuGenerator gen = menuGenerators.get((int) lastPart.getType());
				if (gen == null)
					return;
				path.add(lastPart = gen.getElement(lastPart, part));
			}
			lastPart = source;
			for (int i = path.size() - 2; i >= 0; i--) {
				NbtElement part = path.get(i);
				menuGenerators.get((int) part.getType()).setElement(part, realPath.get(i + 1), lastPart);
				lastPart = part;
			}
		}
	};
	
	
	
	
	private static final int itemX = 16 + 32 + 8;
	private static final int itemY = 16;
	
	private static String copiedKey;
	private static NbtElement copiedValue;
	
	
	private final ItemReference ref;
	private ItemStack savedItem;
	private ItemStack item;
	
	private boolean saved;
	private NamedTextFieldWidget name;
	private ButtonWidget saveBtn;
	
	private NamedTextFieldWidget type;
	private NamedTextFieldWidget count;
	
	private NamedTextFieldWidget path;
	private NamedTextFieldWidget value;
	private List2D editor;
	private Map<String, Integer> scrollPerFolder;
	
	private List<String> realPath;
	private NBTValue selectedValue;
	private MenuGenerator currentGen;
	private NbtElement nbt;
	
	@SuppressWarnings("serial")
	public NBTEditorScreen(ItemReference ref) {
		super(Text.of("NBT Editor"));
		
		this.ref = ref;
		this.savedItem = ref.getItem().copy();
		this.item = this.savedItem.copy();
		
		this.scrollPerFolder = new HashMap<>();
		
		this.realPath = new ArrayList<>() {
			public String toString() {
				return String.join("/", this);
			}
		};
		this.nbt = item.getOrCreateNbt();
	}
	
	@Override
	protected void init() {
		if (realPath.isEmpty() && ((NbtCompound) this.nbt).contains("")) {
			client.setScreen(new ConfirmScreen(value -> {
				if (value) {
					((NbtCompound) this.nbt).remove("");
					item.setNbt((NbtCompound) this.nbt);
					save();
					client.setScreen(NBTEditorScreen.this);
				} else
					client.setScreen(null);
			}, Text.translatable("nbteditor.emptykey.title"), Text.translatable("nbteditor.emptykey.message"),
					Text.translatable("nbteditor.emptykey.yes"), Text.translatable("nbteditor.emptykey.no")));
			
			return;
		}
		
		
		super.init();
		this.client.keyboard.setRepeatEvents(true);
		this.clearChildren();
		
		name = new NamedTextFieldWidget(textRenderer, 16 + (32 + 8) * 2, 16 + 8, 100, 16, Text.of("")).name(Text.translatable("nbteditor.name"));
		name.setMaxLength(Integer.MAX_VALUE);
		name.setText(MainUtil.getItemNameSafely(item).getString());
		name.setChangedListener(str -> {
			if (str.equals(item.getItem().getName().getString()))
				item.setCustomName(null);
			else
				item.setCustomName(Text.of(str));
			
			genEditor();
		});
		this.addSelectableChild(name);
		
		this.addDrawableChild(saveBtn = new ButtonWidget(16 + (32 + 8) * 2 + 100 + 8, 16 + 6, 100, 20, Text.translatable("nbteditor.save"), btn -> {
			save();
		}));
		this.addDrawableChild(new ButtonWidget(16, height - 16 * 2, 20, 20, Text.translatable("nbteditor.add"), btn -> {
			add();
		}));
		this.addDrawableChild(new ButtonWidget(16 + 16 + 8, height - 16 * 2, 20, 20, Text.translatable("nbteditor.remove"), btn -> {
			remove();
		}));
		this.addDrawableChild(new ButtonWidget(16 + (16 + 8) * 2, height - 16 * 2, 48, 20, Text.translatable("nbteditor.copy"), btn -> {
			copy();
		}));
		this.addDrawableChild(new ButtonWidget(16 + (16 + 8) * 2 + (48 + 4), height - 16 * 2, 48, 20, Text.translatable("nbteditor.cut"), btn -> {
			cut();
		}));
		this.addDrawableChild(new ButtonWidget(16 + (16 + 8) * 2 + (48 + 4) * 2, height - 16 * 2, 48, 20, Text.translatable("nbteditor.paste"), btn -> {
			paste();
		}));
		this.addDrawableChild(new ButtonWidget(16 + (16 + 8) * 2 + (48 + 4) * 3, height - 16 * 2, 48, 20, Text.translatable("nbteditor.rename"), btn -> {
			rename();
		}));
		
		
		
		type = new NamedTextFieldWidget(textRenderer, 16 + (32 + 8) * 2, 16 + 8 + 32, 208, 16, Text.of("")).name(Text.translatable("nbteditor.identifier"));
		type.setMaxLength(Integer.MAX_VALUE);
		type.setText(Registry.ITEM.getId(item.getItem()).toString());
		type.setChangedListener(str -> {
			try {
				if (!Registry.ITEM.containsId(new Identifier(str)))
					return;
			} catch (InvalidIdentifierException e) {
				return;
			}
			
			NbtCompound fullData = new NbtCompound();
			item.writeNbt(fullData);
			fullData.putString("id", str);
			ItemStack editedItem = ItemStack.fromNbt(fullData);
			if (editedItem == ItemStack.EMPTY)
				return;
			
			item = editedItem;
			updateName();
		});
		this.addSelectableChild(type);
		
		count = new NamedTextFieldWidget(textRenderer, 16, 16 + 8 + 32, 72, 16, Text.of("")).name(Text.translatable("nbteditor.count"));
		count.setMaxLength(Integer.MAX_VALUE);
		count.setText(item.getCount() + "");
		count.setChangedListener(str -> {
			if (str.isEmpty())
				return;
			
			item.setCount(Integer.parseInt(str));
		});
		count.setTextPredicate((str) -> {
			if (str.isEmpty())
				return true;
			
			try {
				return Integer.parseInt(str) > 0;
			} catch (NumberFormatException e) {
				return false;
			}
		});
		this.addSelectableChild(count);
		
		path = new NamedTextFieldWidget(textRenderer, 16, 16 + 8 + 32 + 16 + 8, 288, 16, Text.translatable("nbteditor.path")).name(Text.translatable("nbteditor.path"));
		path.setMaxLength(Integer.MAX_VALUE);
		path.setText(realPath.toString());
		path.setChangedListener(str -> {
			String[] parts = str.split("/");
			NbtElement nbt = item.getOrCreateNbt();
			for (String part : parts) {
				MenuGenerator gen = menuGenerators.get((int) nbt.getType());
				if (gen == null)
					return;
				nbt = gen.getElement(nbt, part);
				if (nbt == null)
					return;
			}
			realPath.clear();
			realPath.addAll(Arrays.asList(parts));
			genEditor();
		});
		this.addSelectableChild(path);
		
		value = new NamedTextFieldWidget(textRenderer, 16, 16 + 8 + 32 + (16 + 8) * 2, 288, 16, Text.translatable("nbteditor.value")).name(Text.translatable("nbteditor.value"));
		value.setRenderTextProvider((str, index) -> {
			return MainUtil.substring(NbtFormatter.formatElementSafe(value.getText()).getValue(), index, index + str.length()).asOrderedText();
		});
		value.setMaxLength(Integer.MAX_VALUE);
		value.setText("");
		value.setEditable(false);
		value.setChangedListener(str -> {
			if (selectedValue != null) {
				selectedValue.setUnsafe(!NbtFormatter.formatElementSafe(value.getText()).getKey());
				if (selectedValue.isUnsafe())
					return;
				selectedValue.valueChanged(str, (nbt) -> {
					gen.setElement(this.nbt, selectedValue.getKey(), nbt);
					updateName();
				});
			}
		});
		this.addSelectableChild(value);
		
		this.addDrawableChild(new ButtonWidget(16 + 288 + 10, 16 + 8 + 32 + (16 + 8) * 2 - 2, 75, 20, Text.translatable("nbteditor.value_expand"), btn -> {
			if (selectedValue != null)
				client.setScreen(new TextAreaScreen(this, selectedValue.getValueText(), NbtFormatter::formatElementSafe, str -> value.setText(str)));
		}));
		
		final int editorY = 16 + 8 + 32 + (16 + 8) * 3;
		editor = new List2D(16, editorY, width - 16 * 2, height - editorY - 16 * 2 - 8, 4, 32, 32, 8)
				.setFinalEventHandler(new Element() {
					@Override
					public boolean mouseClicked(double mouseX, double mouseY, int button) {
						selectedValue = null;
						value.setText("");
						value.setEditable(false);
						return true;
					}
				});
		genEditor();
		this.addDrawableChild(editor);
	}
	private void genEditor() {
		selectedValue = null;
		value.setText("");
		value.setEditable(false);
		
		updateName();
		
		editor.clearElements();
		
		this.nbt = item.getOrCreateNbt();
		this.currentGen = menuGenerators.get(NbtType.COMPOUND);
		Iterator<String> keys = realPath.iterator();
		boolean removing = false;
		NbtElement value = null;
		MenuGenerator generator = null;
		while (keys.hasNext()) {
			String key = keys.next();
			if (removing) {
				keys.remove();
				continue;
			}
			if ((value = this.currentGen.getElement(this.nbt, key)) != null && (generator = menuGenerators.get((int) value.getType())) != null) {
				this.nbt = value;
				this.currentGen = generator;
			}
			else {
				keys.remove();
				removing = true;
			}
		}
		if (removing) {
			path.text = realPath.toString();
			path.setSelectionStart(path.getText().length());
			path.setSelectionEnd(path.getText().length());
		}
		
		if (!realPath.isEmpty())
			editor.addElement(new NBTValue(this, null, null));
		
		List<NBTValue> elements = gen.getElements(this, this.nbt);
		if (elements == null)
			selectNbt(null, true);
		else {
			elements.sort((a, b) -> a.getKey().compareToIgnoreCase(b.getKey()));
			elements.forEach(editor::addElement);
		}
		
		editor.setScroll(Math.max(editor.getMaxScroll(), scrollPerFolder.computeIfAbsent(realPath.toString(), key -> 0)));
	}
	private void updateName() {
		name.text = MainUtil.getItemNameSafely(item).getString();
		name.setSelectionStart(name.getText().length());
		name.setSelectionEnd(name.getText().length());
	}
	
	void selectNbt(NBTValue key, boolean isFolder) {
		if (isFolder) {
			if (key == null)
				realPath.remove(realPath.size() - 1);
			else
				realPath.add(key.getKey());
			selectedValue = null;
			value.setText("");
			value.setEditable(false);
			path.setText(realPath.toString());
			genEditor();
		} else {
			selectedValue = key;
			value.setText(key.getValueText());
			value.setEditable(true);
		}
	}
	
	@Override
	public void close() {
		if (saved)
			ref.showParent();
		else {
			client.setScreen(new ConfirmScreen(value -> {
				if (value)
					save();
				
				ref.showParent();
			}, Text.translatable("nbteditor.notsaved.title"), Text.translatable("nbteditor.notsaved.message"),
					Text.translatable("nbteditor.notsaved.yes"), Text.translatable("nbteditor.notsaved.no")));
		}
	}
	
	@Override
	public void removed() {
		this.client.keyboard.setRepeatEvents(false);
	}
	
	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		saveBtn.active = !saved;
		
		super.renderBackground(matrices);
		super.render(matrices, mouseX, mouseY, delta);
		
		MainUtil.renderLogo(matrices);
		
		drawItem(item, itemX, itemY, 2, 2);
		name.render(matrices, mouseX, mouseY, delta);
		type.render(matrices, mouseX, mouseY, delta);
		count.render(matrices, mouseX, mouseY, delta);
		path.render(matrices, mouseX, mouseY, delta);
		value.render(matrices, mouseX, mouseY, delta);
	}
	
	private void drawItem(ItemStack stack, int x, int y, float scaleX, float scaleY) {
		x /= scaleX;
		y /= scaleY;
		
		MatrixStack matrixStack = RenderSystem.getModelViewStack();
		matrixStack.push();
		matrixStack.translate(0.0D, 0.0D, 32.0D);
		matrixStack.scale(scaleX, scaleY, 1);
		RenderSystem.applyModelViewMatrix();
		this.setZOffset(200);
		this.itemRenderer.zOffset = 200.0F;
		this.itemRenderer.renderInGuiWithOverrides(stack, x, y);
		this.itemRenderer.renderGuiItemOverlay(this.textRenderer, stack, x, y, null);
		this.setZOffset(0);
		this.itemRenderer.zOffset = 0.0F;
		matrixStack.pop();
		RenderSystem.applyModelViewMatrix();
	}
	
	
	
	@Override
	public void tick() {
		this.name.tick();
		this.type.tick();
		this.count.tick();
		this.path.tick();
		this.value.tick();
		
		item.getOrCreateNbt(); // Make sure both items have NBT defined, so no NBT and empty NBT comes out equal
		savedItem.getOrCreateNbt();
		saved = ItemStack.areEqual(item, savedItem);
	}
	
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
			close();
			return true;
		}
		
		return !this.name.keyPressed(keyCode, scanCode, modifiers) && !this.name.isActive() &&
				!this.type.keyPressed(keyCode, scanCode, modifiers) && !this.type.isActive() &&
				!this.count.keyPressed(keyCode, scanCode, modifiers) && !this.count.isActive() &&
				!this.path.keyPressed(keyCode, scanCode, modifiers) && !this.path.isActive() &&
				!this.value.keyPressed(keyCode, scanCode, modifiers) && !this.value.isActive()
				? keyPressed2(keyCode, scanCode, modifiers) : true;
	}
	private boolean keyPressed2(int keyCode, int scanCode, int modifiers) {
		if (keyCode == GLFW.GLFW_KEY_DELETE || keyCode == GLFW.GLFW_KEY_BACKSPACE)
			remove();
		else if (keyCode == GLFW.GLFW_KEY_ENTER) {
			if (!this.realPath.isEmpty())
				selectNbt(null, true);
		}
		if ((modifiers & GLFW.GLFW_MOD_CONTROL) != 0) {
			if (keyCode == GLFW.GLFW_KEY_C)
				copy();
			else if (keyCode == GLFW.GLFW_KEY_X)
				cut();
			else if (keyCode == GLFW.GLFW_KEY_V)
				paste();
			else if (keyCode == GLFW.GLFW_KEY_S)
				save();
			else if (keyCode == GLFW.GLFW_KEY_R)
				rename();
			else if (keyCode == GLFW.GLFW_KEY_N)
				add();
		}
		
		return super.keyPressed(keyCode, scanCode, modifiers);
	}
	
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		this.name.mouseClicked(mouseX, mouseY, button);
		this.type.mouseClicked(mouseX, mouseY, button);
		this.count.mouseClicked(mouseX, mouseY, button);
		this.path.mouseClicked(mouseX, mouseY, button);
		this.value.mouseClicked(mouseX, mouseY, button);
		
		return super.mouseClicked(mouseX, mouseY, button);
	}
	
	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
		boolean output = super.mouseScrolled(mouseX, mouseY, amount);
		scrollPerFolder.put(realPath.toString(), editor.getScroll());
		return output;
	}
	
	
	@Override
	public boolean shouldPause() {
		return true;
	}
	
	
	private void save() {
		savedItem = item.copy();
		saveBtn.setMessage(Text.translatable("nbteditor.saving"));
		ref.saveItem(savedItem, () -> {
			saveBtn.setMessage(Text.translatable("nbteditor.save"));
		});
	}
	private void add() {
		gen.addElement(this, this.nbt, force -> {
			if (force == null)
				genEditor();
			else {
				client.setScreen(new ConfirmScreen(value -> {
					if (value)
						gen.addElement(NBTEditorScreen.this, this.nbt, success2 -> genEditor(), force);
					
					client.setScreen(NBTEditorScreen.this);
				}, Text.translatable("nbteditor.overwrite.title"), Text.translatable("nbteditor.overwrite.message"),
						Text.translatable("nbteditor.overwrite.yes"), Text.translatable("nbteditor.overwrite.no")));
			}
		}, null);
	}
	private void remove() {
		if (this.selectedValue != null) {
			gen.removeElement(this.nbt, this.selectedValue.getKey());
			genEditor();
		}
	}
	private void copy() {
		if (this.selectedValue != null) {
			copiedKey = this.selectedValue.getKey();
			copiedValue = gen.getElement(this.nbt, this.selectedValue.getKey()).copy();
		}
	}
	private void cut() {
		if (this.selectedValue != null) {
			copiedKey = this.selectedValue.getKey();
			copiedValue = gen.getElement(this.nbt, this.selectedValue.getKey()).copy();
			
			gen.removeElement(this.nbt, this.selectedValue.getKey());
			genEditor();
		}
	}
	private void paste() {
		if (copiedKey != null) {
			gen.pasteElement(this.nbt, copiedKey, copiedValue.copy());
			genEditor();
		}
	}
	private void rename() {
		if (this.selectedValue != null) {
			String selectedKey = this.selectedValue.getKey();
			
			getKey(selectedKey, (key) -> {
				if (gen.renameElement(this.nbt, selectedKey, key, false))
					genEditor();
				else {
					client.setScreen(new ConfirmScreen(value -> {
						if (value) {
							gen.renameElement(this.nbt, selectedKey, key, true);
							genEditor();
						}
						
						client.setScreen(NBTEditorScreen.this);
					}, Text.translatable("nbteditor.overwrite.title"), Text.translatable("nbteditor.overwrite.message"),
							Text.translatable("nbteditor.overwrite.yes"), Text.translatable("nbteditor.overwrite.no")));
				}
			});
		}
	}
	
	
	public void getKey(String defaultValue, Consumer<String> keyConsumer) {
		new StringInputScreen(this, keyConsumer, str -> !str.isEmpty()).show(defaultValue);
	}
	public void getKey(Consumer<String> keyConsumer) {
		getKey(null, keyConsumer);
	}
	
}
