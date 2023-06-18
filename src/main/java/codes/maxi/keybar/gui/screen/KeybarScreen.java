package codes.maxi.keybar.gui.screen;
import codes.maxi.keybar.util.ColorUtil;
import codes.maxi.searchables.api.SearchableComponent;
import codes.maxi.searchables.api.SearchableType;
import com.google.common.base.MoreObjects;
import lombok.NonNull;
import codes.maxi.keybar.binding.Binding;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.MultilineTextField;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import static codes.maxi.keybar.Keybar.kb;
import static codes.maxi.keybar.Keybar.mc;

@Environment(EnvType.CLIENT)
public class KeybarScreen extends Screen {
    private static final Component USAGE_TEXT = Component.translatable("keybar.usage");

    private static final SearchableType<Binding> keybindingSearcher = new SearchableType.Builder<Binding>()
            .defaultComponent(SearchableComponent.create("name", keybinding -> Optional.of(I18n.get(keybinding.getName()))))
            .component(SearchableComponent.create("category", keybinding -> Optional.of(I18n.get(keybinding.getCategory()))))
            .build();

    private final Font textRenderer;
    private final MultilineTextField searchBox;

    private static final Component cursor = Component.literal("_");
    private static final float cursorBlinkInterval = 5f;
    private static final int innerCursorWidth = 1;
    private static final int textColor = ColorUtil.fromRGBA(255, 255, 255, 255);
    private static final int subtextColor = ColorUtil.fromRGBA(127, 127, 127, 255);
    private static final int cursorColor = ColorUtil.fromRGBA(255, 255, 255, 255);
    private static final int textHighlightColor = ColorUtil.fromRGBA(0, 0, 127, 255);
    private static final int searchBoxBackgroundColor = ColorUtil.fromRGBA(0 , 0, 0, 150);
    private static final int suggestionBackgroundColor = ColorUtil.fromRGBA(0 , 0, 0, 127);
    private static final int hardSelectedSuggestionSidebarColor = ColorUtil.fromRGBA(15, 204, 252, 255);
    private static final int softSelectedSuggestionBackgroundColor = ColorUtil.fromRGBA(0 , 0, 0, 200);
    private static final int selectedSuggestionSidebarWidth = 3;
    private static final int cursorMarginLeft = 0;
    private static final boolean textShadow = false;
    private static final int xPadding = 5;
    private static final int searchBoxPaddingTop = 2;
    private static final int suggestionPaddingTop = 2;
    private static final int suggestionNameKeyGap = 2;

    private float cursorBlinkRemaining = 0f;
    private boolean cursorVisible = false;

    private int x = 0;
    private int y = 0;
    private int contentWidth = 300;
    private int contentHeight = 0;

    private int searchBoxHeight = 20;

    private int suggestionHeight = 12;

    private int suggestionsStartY;

    @NonNull
    private List<Binding> suggestions = new ArrayList<>();
    private int visibleSuggestions = 0;

    /**
     * Suggestion selected via keyboard or scrolling.
     */
    private int hardSelection = -1;
    /**
     * Suggestion selected via cursor.
     */
    private int softSelection = -1;
    private int scrollAmount = 0;

    private boolean queryNeedsNarrating = false;

    private double mx = -1;
    private double my = -1;

    public KeybarScreen() {
        super(Component.translatable("keybar.title"));

        textRenderer = mc.font;
        searchBox = new MultilineTextField(textRenderer, width);
        searchBox.setValueListener(value -> {
            updateSuggestions();
            queryNeedsNarrating = true;
        });
    }

    @Override
    public boolean isPauseScreen() {
        return false; // Prevent singleplayer games from pausing
    }

    @Override
    protected void init() {
        contentHeight = (int) (this.height * 0.5f);

        x = (width - contentWidth) / 2;
        y = (int) (this.height * 0.25f);

        updateSuggestions();
    }

    private void renderSearchBox(GuiGraphics ctx, float delta) {
        String text = searchBox.value();
        final int cursorPos = searchBox.cursor();
        final int textY = y + ((searchBoxHeight - textRenderer.lineHeight)/2) + searchBoxPaddingTop;

        // Background
        ctx.fill(x, y, x+ contentWidth, y+searchBoxHeight, searchBoxBackgroundColor);

        // Text
        final int textX = x + xPadding;

        // Selection
        if(searchBox.hasSelection()) {
            MultilineTextField.StringView selection = searchBox.getSelected();
            final int x = textX + textRenderer.width(text.substring(0, selection.beginIndex()));
            final int y = textY;

            final int w = textRenderer.width(searchBox.getSelectedText());

            ctx.fill(x, y, x + w, y + textRenderer.lineHeight, textHighlightColor);
        }

        // Text
        final int textRight = ctx.drawString(textRenderer, text, textX, textY, textColor, textShadow);

        // Cursor
        if((cursorBlinkRemaining -= delta) < 0) {
            cursorVisible = !cursorVisible;
            cursorBlinkRemaining = cursorBlinkInterval;
        }
        if(cursorVisible) {
            if(cursorPos == text.length()) { // Cursor is at end of text
                ctx.drawString(textRenderer, cursor, textRight + cursorMarginLeft, textY, cursorColor, textShadow);
            }
            else { // Cursor is inside text
                final int x = textX + textRenderer.width(text.substring(0, cursorPos));
                final int y = textY;
                ctx.fill(x, y, x + innerCursorWidth, y + textRenderer.lineHeight, cursorColor);
            }
        }
    }

    private void updateSuggestions() {
        suggestions = keybindingSearcher.filterEntries(kb.bindings.getOrdered(), searchBox.value());
        softSelection = 0;
        hardSelection = softSelection;
        scrollAmount = 0;
    }
    private void renderSuggestions(GuiGraphics ctx) {
        final int[] yOffset = {y + searchBoxHeight};
        final int maxY = y + contentHeight;
        final boolean[] end = {false};
        final int[] i = {-1};

        suggestionsStartY = yOffset[0];

        suggestions.forEach(suggestion -> {
            i[0]++;
            if(i[0] < scrollAmount) return;
            if(i[0] < 0) return;
            if(end[0]) return;
            if(yOffset[0] + suggestionHeight > maxY) {
                visibleSuggestions = i[0];
                end[0] = true;
                return;
            }
            final int availWidth = contentWidth - (xPadding * 2);

            // Background
            ctx.fill(x, yOffset[0], x+contentWidth, yOffset[0] + suggestionHeight, softSelection == i[0] ? softSelectedSuggestionBackgroundColor : suggestionBackgroundColor);

            if(hardSelection == i[0]) { // Hard Selected Suggestion
                // Sidebars
                ctx.fill(x, yOffset[0], x+selectedSuggestionSidebarWidth, yOffset[0] + suggestionHeight, hardSelectedSuggestionSidebarColor); // left
                ctx.fill(x+contentWidth-selectedSuggestionSidebarWidth, yOffset[0], x+contentWidth, yOffset[0] + suggestionHeight, hardSelectedSuggestionSidebarColor); // right
            }

            final int xOffset = x + xPadding;

            String keys = MoreObjects.firstNonNull(suggestion.toString(), "");
            final int keysWidth = textRenderer.width(keys);
            String category = textRenderer.plainSubstrByWidth(I18n.get(suggestion.getCategory()) + ": ", availWidth - keysWidth - suggestionNameKeyGap);
            final int categoryWidth = textRenderer.width(category);
            String name = textRenderer.plainSubstrByWidth(I18n.get(suggestion.getName()), availWidth - categoryWidth - keysWidth - suggestionNameKeyGap);

            ctx.drawString(textRenderer, category, xOffset, yOffset[0] + suggestionPaddingTop, subtextColor, textShadow);
            ctx.drawString(textRenderer, name, xOffset + categoryWidth, yOffset[0] + suggestionPaddingTop, textColor, textShadow);
            ctx.drawString(textRenderer, keys, xOffset + (availWidth - keysWidth), yOffset[0] + suggestionPaddingTop, subtextColor, textShadow);
            yOffset[0] += suggestionHeight;
        });

        if(!end[0]) visibleSuggestions = i[0] + 1;
        if(visibleSuggestions == 0) softSelection = -1;
        else if(softSelection != -1) {
            softSelection = softSelection % visibleSuggestions;
            if(softSelection < 0) softSelection += visibleSuggestions;
        }
    }

    private void hardSelNext() {
        if(visibleSuggestions == 0) hardSelection = -1;
        else {
            hardSelection = hardSelection + 1;
            if(hardSelection >= visibleSuggestions) {
                if(hardSelection >= suggestions.size()) {
                    hardSelection = 0;
                    scrollAmount = 0;
                }
                else {
                    scrollAmount++;
                }
            }
        }
        mouseMoved(mx, my);
    }
    private void hardSelPrev() {
        if(visibleSuggestions == 0) hardSelection = -1;
        else {
            hardSelection = hardSelection - 1;
            if(hardSelection < 0) {
                hardSelection = suggestions.size() - 1;
                final int suggestionsPerPage = (int)Math.floor((double) (contentHeight - searchBoxHeight) / suggestionHeight);
                scrollAmount = suggestions.size() - (suggestions.size() % suggestionsPerPage);
            }
            else if(hardSelection < scrollAmount) scrollAmount--;
        }
    }

    private void activate(int selection) {
        Binding suggestion = suggestions.get(selection);
        if(suggestion != null) {
            onClose();
            kb.scheduler.scheduleForNextTickStart(mc -> suggestion.activate());
        }
    }

    @Override
    public boolean keyPressed(int key, int scan, int mod) {
        switch (key) {
            case GLFW.GLFW_KEY_ENTER -> activate(hardSelection);
            case GLFW.GLFW_KEY_DOWN, GLFW.GLFW_KEY_PAGE_DOWN -> hardSelNext();
            case GLFW.GLFW_KEY_UP, GLFW.GLFW_KEY_PAGE_UP -> hardSelPrev();
            default -> this.searchBox.keyPressed(key);
        }

        return super.keyPressed(key, scan, mod);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double amount) {
        this.mx = mx;
        this.my = my;
        if(!isMouseOverSuggestions(mx, my)) return false;

        if(amount > 0) {
            if(scrollAmount >= 1) scrollAmount--;
        }
        else if(amount < 0) {
            if(scrollAmount < suggestions.size() - 1) scrollAmount++;
        }

        mouseMoved(mx, my); // update selection by mouse
        return true;
    }

    private boolean isMouseOverSuggestions(double mx, double my) {
        my -= suggestionsStartY;
        mx -= x;

        if(mx < 0 || mx > contentWidth) return false;
        if(my < 0 || my > visibleSuggestions * suggestionHeight) return false;
        return true;
    }

    @Override
    public void mouseMoved(double mx, double my) {
        this.mx = mx;
        this.my = my;
        if(!isMouseOverSuggestions(mx, my)) {
            softSelection = -1;
            return;
        }

        softSelection = scrollAmount + (int) Math.floor((my - suggestionsStartY) / suggestionHeight);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        this.mx = mx;
        this.my = my;
        if(button == 0) {
            my -= suggestionsStartY;
            mx -= x;

            if(mx < 0 || mx > contentWidth) {
                onClose();
                return true;
            }
            if(my < 0 || my > visibleSuggestions * suggestionHeight) {
                onClose();
                return true;
            }

            softSelection = scrollAmount + (int) Math.floor(my / suggestionHeight);
            activate(softSelection);
        }

        onClose();
        return true;
    }


    @Override
    public boolean charTyped(char chr, int mod) {
        String str = Character.toString(chr);
        if(SharedConstants.isAllowedChatCharacter(chr)) {
            this.searchBox.insertText(str);
            updateSuggestions();
        }
        return false;
    }

    @Override
    public void render(GuiGraphics ctx, int mouseX, int mouseY, float delta) {
        renderSearchBox(ctx, delta);
        renderSuggestions(ctx);

        super.render(ctx, mouseX, mouseY, delta);
    }



    @Override
    protected void updateNarrationState(NarrationElementOutput messageBuilder) {
        messageBuilder.add(NarratedElementType.TITLE, this.getTitle());
        messageBuilder.add(NarratedElementType.USAGE, USAGE_TEXT);


        if(queryNeedsNarrating) {
            String query = searchBox.value();
            if(!query.isEmpty()) {
                messageBuilder.nest().add(NarratedElementType.TITLE, Component.translatable("keybar.query", query));
                int results = suggestions.size();
                if (results == 0)
                    messageBuilder.nest().add(NarratedElementType.TITLE, Component.translatable("keybar.no_results"));
                else if (results == 1)
                    messageBuilder.nest().add(NarratedElementType.TITLE, Component.translatable("keybar.one_result"));
                else
                    messageBuilder.nest().add(NarratedElementType.TITLE, Component.translatable("keybar.many_results", results));
            }
        }
        else {
            Binding selected = softSelection == -1 ? null : suggestions.get(softSelection);
            if (selected != null) {
                messageBuilder.nest().add(NarratedElementType.TITLE, Component.translatable("keybar.result", I18n.get(selected.getCategory()), I18n.get(selected.getName())));
                messageBuilder.nest().add(NarratedElementType.POSITION, Component.translatable("keybar.position", softSelection + 1, suggestions.size()));
            }
        }
    }
}
