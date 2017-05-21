package omtteam.omlib.util;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Keridos on 21/05/17.
 * This Class
 */
@MethodsReturnNonnullByDefault
public class CommandTextComponent implements ITextComponent {
    private Style style;
    private String unformattedText;
    private ArrayList<ITextComponent> sibling = new ArrayList<>();

    private CommandTextComponent(Style style, String unformattedText, ArrayList<ITextComponent> sibling) {
        this.style = style;
        this.unformattedText = unformattedText;
        this.sibling = sibling;
    }

    public CommandTextComponent(String unformattedText) {
        this.unformattedText = unformattedText;
    }

    @Override
    @ParametersAreNonnullByDefault
    public ITextComponent setStyle(Style style) {
        this.style = style;
        return this;
    }

    @Override
    public Style getStyle() {
        return style;
    }

    @Override
    @ParametersAreNonnullByDefault
    public ITextComponent appendText(String text) {
        unformattedText += text;
        return this;
    }

    @Override
    @ParametersAreNonnullByDefault
    public ITextComponent appendSibling(ITextComponent component) {
        getSiblings().add(component);
        return this;
    }

    @Override
    public String getUnformattedComponentText() {
        return unformattedText;
    }

    @Override
    public String getUnformattedText() {
        return unformattedText;
    }

    @Override
    public String getFormattedText() {
        return unformattedText;
    }

    @Override
    public List<ITextComponent> getSiblings() {
        return sibling;
    }

    @Override
    public ITextComponent createCopy() {
        return new CommandTextComponent(this.style, this.unformattedText, this.sibling);
    }

    @Override
    public Iterator<ITextComponent> iterator() {
        return this.sibling.iterator();
    }
}
