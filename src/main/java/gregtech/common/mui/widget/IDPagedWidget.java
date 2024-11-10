package gregtech.common.mui.widget;

import net.minecraft.util.ResourceLocation;

import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.widget.Widget;
import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class IDPagedWidget<W extends IDPagedWidget<W>> extends Widget<W> {

    private final Map<ResourceLocation, IWidget> pages = new Object2ObjectOpenHashMap<>();
    private IWidget currentPage;
    private ResourceLocation currentPageID = null;
    private ResourceLocation defaultPageID = null;

    @Override
    public void afterInit() {
        if (defaultPageID == null) {
            throw new IllegalStateException("IDPagedWidget has no default page!");
        }
        setPage(defaultPageID);
    }

    public void setPage(ResourceLocation page) {
        if (!pages.containsKey(page)) {
            throw new IndexOutOfBoundsException();
        }
        this.currentPageID = page;
        if (this.currentPage != null) {
            this.currentPage.setEnabled(false);
        }
        this.currentPage = this.pages.get(this.currentPageID);
        this.currentPage.setEnabled(true);
    }

    public Collection<IWidget> getPages() {
        return this.pages.values();
    }

    public IWidget getCurrentPage() {
        return this.currentPage;
    }

    public ResourceLocation getCurrentPageID() {
        return this.currentPageID;
    }

    @Override
    public @Unmodifiable @NotNull List<IWidget> getChildren() {
        return ImmutableList.copyOf(this.pages.values());
    }

    public W addPage(ResourceLocation id, IWidget widget) {
        this.pages.put(id, widget);
        widget.setEnabled(false);
        return getThis();
    }

    public W setDefaultPage(ResourceLocation id) {
        defaultPageID = id;
        return getThis();
    }
}
