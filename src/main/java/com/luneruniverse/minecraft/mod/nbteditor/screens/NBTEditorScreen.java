package com.luneruniverse.minecraft.mod.nbteditor.screens;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import org.lwjgl.glfw.GLFW;

import com.luneruniverse.minecraft.mod.nbteditor.NBTEditor;
import com.luneruniverse.minecraft.mod.nbteditor.integrations.NBTAutocompleteIntegration;
import com.luneruniverse.minecraft.mod.nbteditor.localnbt.LocalItem;
import com.luneruniverse.minecraft.mod.nbteditor.localnbt.LocalNBT;
import com.luneruniverse.minecraft.mod.nbteditor.misc.MixinLink;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.IdentifierInst;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVElement;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVMisc;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVTooltip;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.nbtreferences.NBTReference;
import com.luneruniverse.minecraft.mod.nbteditor.nbtreferences.itemreferences.ItemReference;
import com.luneruniverse.minecraft.mod.nbteditor.screens.nbtmenugenerators.MenuGenerator;
import com.luneruniverse.minecraft.mod.nbteditor.screens.nbtmenugenerators.StringMenuGenerator;
import com.luneruniverse.minecraft.mod.nbteditor.screens.util.FancyConfirmScreen;
import com.luneruniverse.minecraft.mod.nbteditor.screens.util.StringInputScreen;
import com.luneruniverse.minecraft.mod.nbteditor.screens.util.TextAreaScreen;
import com.luneruniverse.minecraft.mod.nbteditor.screens.widgets.List2D;
import com.luneruniverse.minecraft.mod.nbteditor.screens.widgets.NamedTextFieldWidget;
import com.luneruniverse.minecraft.mod.nbteditor.screens.widgets.SuggestingTextFieldWidget;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;
import com.luneruniverse.minecraft.mod.nbteditor.util.NbtFormatter;
import com.luneruniverse.minecraft.mod.nbteditor.util.TextUtil;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidIdentifierException;

public class NBTEditorScreen<L extends LocalNBT> extends LocalEditorScreen<L> {
	
	private class RecursiveMenuGenerator implements MenuGenerator {
		@Override
		public List<NBTValue> getElements(NBTEditorScreen<?> screen, NbtElement source) {
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
		public void addElement(NBTEditorScreen<?> screen, NbtElement source, Consumer<String> requestOverwrite, String force) {
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
			NbtElement lastPart = localNBT.getOrCreateNBT();
			for (String part : realPath) {
				MenuGenerator gen = MenuGenerator.TYPES.get(lastPart.getType());
				if (gen == null)
					return;
				path.add(lastPart = gen.getElement(lastPart, part));
			}
			lastPart = source;
			for (int i = path.size() - 2; i >= 0; i--) {
				NbtElement part = path.get(i);
				MenuGenerator.TYPES.get(part.getType()).setElement(part, realPath.get(i + 1), lastPart);
				lastPart = part;
			}
		}
	}
	
	
	private static String copiedKey;
	private static NbtElement copiedValue;
	
	
	private NamedTextFieldWidget type;
	private NamedTextFieldWidget count;
	
	private NamedTextFieldWidget path;
	private SuggestingTextFieldWidget value;
	private List2D editor;
	private Map<String, Integer> scrollPerFolder;
	
	private List<String> realPath;
	private NBTValue upValue;
	private NBTValue selectedValue;
	private boolean json;
	private MenuGenerator currentGen;
	private final MenuGenerator gen;
	private NbtElement nbt;
	
	@SuppressWarnings({ "serial", "deprecation" })
	public NBTEditorScreen(NBTReference<L> ref) {
		super(TextInst.of("NBT Editor"), ItemReference.toItemPartsRef(ref));
		
		this.scrollPerFolder = new HashMap<>();
		
		this.realPath = new ArrayList<>() {
			public String toString() {
				return String.join("/", this);
			}
		};
		this.gen = new RecursiveMenuGenerator();
		this.nbt = localNBT.getOrCreateNBT();
	}
	
	@Override
	protected void initEditor() {
		if (realPath.isEmpty() && ((NbtCompound) this.nbt).contains("")) {
			client.setScreen(new ConfirmScreen(value -> {
				if (value) {
					((NbtCompound) this.nbt).remove("");
					localNBT.setNBT((NbtCompound) this.nbt);
					save();
					client.setScreen(NBTEditorScreen.this);
				} else
					close();
			}, TextInst.translatable("nbteditor.nbt.empty_key.title"), TextInst.translatable("nbteditor.nbt.empty_key.desc"),
					TextInst.translatable("nbteditor.nbt.empty_key.yes"), TextInst.translatable("nbteditor.nbt.empty_key.no")));
			
			return;
		}
		
		
		MVMisc.setKeyboardRepeatEvents(true);
		
		name.setChangedListener(str -> {
			if (str.equals(localNBT.getDefaultName()))
				localNBT.setName(null);
			else
				localNBT.setName(TextInst.of(str));
			
			genEditor();
		});
		
		this.addDrawableChild(MVMisc.newButton(16, height - 16 * 2, 20, 20, TextInst.translatable("nbteditor.nbt.add"), btn -> {
			add();
		}));
		this.addDrawableChild(MVMisc.newButton(16 + 16 + 8, height - 16 * 2, 20, 20, TextInst.translatable("nbteditor.nbt.remove"), btn -> {
			remove();
		}));
		this.addDrawableChild(MVMisc.newButton(16 + (16 + 8) * 2, height - 16 * 2, 48, 20, TextInst.translatable("nbteditor.nbt.copy"), btn -> {
			copy();
		}));
		this.addDrawableChild(MVMisc.newButton(16 + (16 + 8) * 2 + (48 + 4), height - 16 * 2, 48, 20, TextInst.translatable("nbteditor.nbt.cut"), btn -> {
			cut();
		}));
		this.addDrawableChild(MVMisc.newButton(16 + (16 + 8) * 2 + (48 + 4) * 2, height - 16 * 2, 48, 20, TextInst.translatable("nbteditor.nbt.paste"), btn -> {
			paste();
		}));
		this.addDrawableChild(MVMisc.newButton(16 + (16 + 8) * 2 + (48 + 4) * 3, height - 16 * 2, 48, 20, TextInst.translatable("nbteditor.nbt.rename"), btn -> {
			rename();
		}));
		
		
		
		Set<Identifier> allTypes = localNBT.getIdOptions();
		type = new NamedTextFieldWidget(16 + (32 + 8) * 2, 16 + 8 + 32, 208, 16).name(TextInst.translatable("nbteditor.nbt.identifier"));
		type.setMaxLength(Integer.MAX_VALUE);
		type.setText(localNBT.getId().toString());
		if (allTypes == null)
			type.setEditable(false);
		else {
			type.setChangedListener(str -> {
				Identifier id;
				try {
					id = IdentifierInst.of(str);
				} catch (InvalidIdentifierException e) {
					return;
				}
				if (!allTypes.contains(id))
					return;
				if (!ConfigScreen.isAirEditable() && localNBT.isEmpty(id))
					return;
				
				localNBT.setId(id);
				if (localNBT instanceof LocalItem item && item.getCount() == 0)
					item.setCount(count.getText().isEmpty() || count.getText().equals("+") ? 1 : Integer.parseInt(count.getText()));
				
				genEditor();
			});
		}
		this.addDrawableChild(type);
		
		count = new NamedTextFieldWidget(16, 16 + 8 + 32, 72, 16).name(TextInst.translatable("nbteditor.nbt.count"));
		count.setMaxLength(Integer.MAX_VALUE);
		if (localNBT instanceof LocalItem item) {
			count.setText((ConfigScreen.isAirEditable() ? Math.max(1, item.getCount()) : item.getCount()) + "");
			count.setChangedListener(str -> {
				if (str.isEmpty() || str.equals("+"))
					return;
				
				item.setCount(Integer.parseInt(str));
				checkSave();
			});
			count.setTextPredicate(MainUtil.intPredicate(1, Integer.MAX_VALUE, true));
		} else {
			count.setText("1");
			count.setEditable(false);
		}
		this.addDrawableChild(count);
		
		path = new NamedTextFieldWidget(16, 16 + 8 + 32 + 16 + 8, 288, 16).name(TextInst.translatable("nbteditor.nbt.path"));
		path.setMaxLength(Integer.MAX_VALUE);
		path.setText(realPath.toString());
		path.setChangedListener(str -> {
			String[] parts = str.split("/");
			NbtElement nbt = localNBT.getOrCreateNBT();
			for (String part : parts) {
				MenuGenerator gen = MenuGenerator.TYPES.get(nbt.getType());
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
		this.addDrawableChild(path);
		
		value = new SuggestingTextFieldWidget(this, 16, 16 + 8 + 32 + (16 + 8) * 2, 288, 16).name(TextInst.translatable("nbteditor.nbt.value"));
		value.setRenderTextProvider((str, index) -> {
			return TextUtil.substring(NbtFormatter.FORMATTER.formatSafely(value.getText()).text(), index, index + str.length()).asOrderedText();
		});
		value.setMaxLength(Integer.MAX_VALUE);
		value.setText("");
		value.setEditable(false);
		value.setChangedListener(str -> {
			if (selectedValue != null) {
				selectedValue.setUnsafe(!NbtFormatter.FORMATTER.formatSafely(value.getText()).isSuccess());
				if (selectedValue.isUnsafe())
					return;
				selectedValue.valueChanged(str, nbt -> {
					gen.setElement(this.nbt, selectedValue.getKey(), nbt);
					updateName();
				});
				if (realPath.isEmpty()) {
					for (List2D.List2DValue element : editor.getElements()) {
						if (element instanceof NBTValue value)
							value.updateInvalidComponent(localNBT, null);
					}
				} else
					upValue.updateInvalidComponent(localNBT, realPath.get(0));
				checkSave();
			}
		});
		value.suggest((str, cursor) -> NBTAutocompleteIntegration.INSTANCE
				.filter(ac -> selectedValue != null)
				.map(ac -> ac.getSuggestions(localNBT, realPath, selectedValue.getKey(), str, cursor))
				.orElseGet(() -> new SuggestionsBuilder("", 0).buildFuture()));
		this.addDrawableChild(value);
		
		this.addDrawableChild(MVMisc.newButton(16 + 288 + 10, 16 + 8 + 32 + (16 + 8) * 2 - 2, 75, 20, TextInst.translatable("nbteditor.nbt.value_expand"), btn -> {
			if (selectedValue == null) {
				client.setScreen(new TextAreaScreen(this, nbt.toString(), NbtFormatter.FORMATTER, false, str -> {
					try {
						NbtElement newNbt = MixinLink.parseSpecialElement(new StringReader(str));
						if (realPath.isEmpty()) {
							if (newNbt instanceof NbtCompound)
								nbt = newNbt;
							else {
								nbt = new NbtCompound();
								((NbtCompound) nbt).put("value", newNbt);
							}
							localNBT.setNBT((NbtCompound) nbt);
						} else {
							String lastPathPart = realPath.remove(realPath.size() - 1);
							genEditor();
							gen.setElement(nbt, lastPathPart, newNbt);
							realPath.add(lastPathPart);
						}
					} catch (CommandSyntaxException e) {
						NBTEditor.LOGGER.error("Error parsing nbt from Expand", e);
					}
				}).suggest((str, cursor) -> NBTAutocompleteIntegration.INSTANCE
						.map(ac -> ac.getSuggestions(localNBT, realPath, null, str, cursor))
						.orElseGet(() -> new SuggestionsBuilder("", 0).buildFuture())));
			} else
				client.setScreen(new TextAreaScreen(this, selectedValue.getValueText(json), NbtFormatter.FORMATTER,
						false, str -> value.setText(str)).suggest((str, cursor) -> NBTAutocompleteIntegration.INSTANCE
								.map(ac -> ac.getSuggestions(localNBT, realPath, selectedValue.getKey(), str, cursor))
								.orElseGet(() -> new SuggestionsBuilder("", 0).buildFuture())));
		}));
		
		final int editorY = 16 + 8 + 32 + (16 + 8) * 3;
		editor = new List2D(16, editorY, width - 16 * 2, height - editorY - 16 * 2 - 8, 4, 32, 32, 8)
				.setFinalEventHandler(new MVElement() {
					@Override
					public boolean mouseClicked(double mouseX, double mouseY, int button) {
						selectedValue = null;
						value.setText("");
						value.setEditable(false);
						return true;
					}
				});
		genEditor();
		this.addSelectableChild(editor);
	}
	private void genEditor() {
		checkSave();
		
		selectedValue = null;
		value.setText("");
		value.setEditable(false);
		
		updateName();
		
		editor.clearElements();
		
		this.nbt = localNBT.getOrCreateNBT();
		this.json = false;
		this.currentGen = MenuGenerator.TYPES.get(NbtElement.COMPOUND_TYPE);
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
			if ((value = this.currentGen.getElement(this.nbt, key)) != null && (generator = MenuGenerator.TYPES.get(value.getType())) != null) {
				this.nbt = value;
				this.currentGen = generator;
				if (this.currentGen instanceof StringMenuGenerator)
					this.json = true;
			} else {
				keys.remove();
				removing = true;
			}
		}
		if (removing) {
			path.text = realPath.toString();
			path.setSelectionStart(path.getText().length());
			path.setSelectionEnd(path.getText().length());
		}
		
		if (realPath.isEmpty())
			upValue = null;
		else {
			upValue = new NBTValue(this, null, null);
			upValue.updateInvalidComponent(localNBT, realPath.get(0));
			editor.addElement(upValue);
		}
		
		List<NBTValue> elements = gen.getElements(this, this.nbt);
		if (elements == null) {
			selectNbt(null, true);
			return;
		} else {
			if (realPath.isEmpty())
				elements.forEach(element -> element.updateInvalidComponent(localNBT, null));
			elements.sort((a, b) -> a.getKey().compareToIgnoreCase(b.getKey()));
			elements.forEach(editor::addElement);
		}
		
		editor.setScroll(Math.max(editor.getMaxScroll(), scrollPerFolder.computeIfAbsent(realPath.toString(), key -> 0)));
	}
	private void updateName() {
		String newName = localNBT.getName().getString();
		if (!name.text.equals(newName)) {
			name.text = newName;
			name.setSelectionStart(0);
			name.setSelectionEnd(0);
		}
	}
	@Override
	protected boolean isNameEditable() {
		return true;
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
			path.text = realPath.toString();
			path.setSelectionStart(path.getText().length());
			path.setSelectionEnd(path.getText().length());
			genEditor();
		} else {
			selectedValue = key;
			value.setText(key.getValueText(json));
			value.setEditable(true);
		}
	}
	
	@Override
	protected void preRenderEditor(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		MVTooltip.setOneTooltip(true, false);
		editor.render(matrices, mouseX, mouseY, delta); // So the tab completion renders on top correctly
		MVTooltip.renderOneTooltip(matrices, mouseX, mouseY);
	}
	@Override
	protected void renderEditor(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		if (NBTAutocompleteIntegration.INSTANCE.isEmpty())
			renderTip(matrices, "nbteditor.nbt_ac.tip");
	}
	
	@Override
	public void removed() {
		MVMisc.setKeyboardRepeatEvents(false);
	}
	
	@Override
	protected boolean save() {
		if (localNBT.isEmpty() && localNBT.getNBT() != null && !localNBT.getNBT().isEmpty()) {
			MainUtil.client.setScreen(new FancyConfirmScreen(value -> {
				if (value)
					super.save();
				
				MainUtil.client.setScreen(NBTEditorScreen.this);
			}, TextInst.translatable("nbteditor.nbt.saving_air.title"), TextInst.translatable("nbteditor.nbt.saving_air.desc"),
					TextInst.translatable("nbteditor.nbt.saving_air.yes"), TextInst.translatable("nbteditor.nbt.saving_air.no"))
					.setParent(this));
			return false;
		}
		
		if (localNBT instanceof LocalItem && localNBT.getNBT() != null) {
			List<NBTValue> elements = MenuGenerator.TYPES.get(NbtElement.COMPOUND_TYPE).getElements(this, localNBT.getNBT());
			elements.forEach(element -> element.updateInvalidComponent(localNBT, null));
			if (elements.stream().anyMatch(NBTValue::isInvalidComponent)) {
				MainUtil.client.setScreen(new FancyConfirmScreen(value -> {
					if (value)
						super.save();
					
					MainUtil.client.setScreen(NBTEditorScreen.this);
				}, TextInst.translatable("nbteditor.nbt.saving_invalid_components.title"), TextInst.translatable("nbteditor.nbt.saving_invalid_components.desc"),
						TextInst.translatable("nbteditor.nbt.saving_invalid_components.yes"), TextInst.translatable("nbteditor.nbt.saving_invalid_components.no"))
						.setParent(this));
				return false;
			}
		}
		
		return super.save();
	}
	
	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
			close();
			return true;
		}
		
		return !this.type.keyPressed(keyCode, scanCode, modifiers) && !this.type.isActive() &&
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
			else if (keyCode == GLFW.GLFW_KEY_R)
				rename();
			else if (keyCode == GLFW.GLFW_KEY_N)
				add();
		}
		
		return super.keyPressed(keyCode, scanCode, modifiers);
	}
	
	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double xAmount, double yAmount) {
		boolean output = super.mouseScrolled(mouseX, mouseY, xAmount, yAmount);
		scrollPerFolder.put(realPath.toString(), editor.getScroll());
		return output;
	}
	
	@Override
	public void onFilesDropped(List<Path> paths) {
		if (!(nbt instanceof NbtCompound))
			return;
		for (Path path : paths) {
			File file = path.toFile();
			if (file.isFile() && file.getName().endsWith(".nbt")) {
				try (FileInputStream in = new FileInputStream(file)) {
					NbtCompound nbt = MainUtil.readNBT(in);
					for (String key : nbt.getKeys())
						gen.setElement(this.nbt, key, nbt.get(key));
					genEditor();
				} catch (Exception e) {
					NBTEditor.LOGGER.error("Error while importing a .nbt file", e);
				}
			}
		}
	}
	
	
	@Override
	public boolean shouldPause() {
		return true;
	}
	
	
	private void add() {
		gen.addElement(this, this.nbt, force -> {
			if (force == null)
				genEditor();
			else {
				client.setScreen(new FancyConfirmScreen(value -> {
					if (value)
						gen.addElement(NBTEditorScreen.this, this.nbt, success2 -> genEditor(), force);
					
					client.setScreen(NBTEditorScreen.this);
				}, TextInst.translatable("nbteditor.nbt.overwrite.title"), TextInst.translatable("nbteditor.nbt.overwrite.desc"),
						TextInst.translatable("nbteditor.nbt.overwrite.yes"), TextInst.translatable("nbteditor.nbt.overwrite.no")));
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
					client.setScreen(new FancyConfirmScreen(value -> {
						if (value) {
							gen.renameElement(this.nbt, selectedKey, key, true);
							genEditor();
						}
						
						client.setScreen(NBTEditorScreen.this);
					}, TextInst.translatable("nbteditor.nbt.overwrite.title"), TextInst.translatable("nbteditor.nbt.overwrite.desc"),
							TextInst.translatable("nbteditor.nbt.overwrite.yes"), TextInst.translatable("nbteditor.nbt.overwrite.no")));
				}
			});
		}
	}
	
	
	public void getKey(String defaultValue, Consumer<String> keyConsumer) {
		new StringInputScreen(this, keyConsumer, str -> !str.isEmpty())
				.suggest((str, cursor) -> NBTAutocompleteIntegration.INSTANCE
						.map(ac -> ac.getSuggestions(localNBT, realPath, str, null, cursor,
								currentGen.getElements(this, nbt).stream().map(NBTValue::getKey).toList()))
						.orElseGet(() -> new SuggestionsBuilder("", 0).buildFuture()))
				.show(defaultValue);
	}
	public void getKey(Consumer<String> keyConsumer) {
		getKey(null, keyConsumer);
	}
	
}
