package gregtech.api.terminal.app;

import gregtech.api.gui.resources.IGuiTexture;
import gregtech.api.terminal.gui.widgets.AnimaWidgetGroup;
import gregtech.api.terminal.os.TerminalOSWidget;
import gregtech.api.util.Position;
import gregtech.api.util.Size;
import net.minecraft.nbt.NBTTagCompound;

public abstract class AbstractApplication extends AnimaWidgetGroup {
    protected final String name;
    protected final IGuiTexture icon;
    protected TerminalOSWidget os;

    public AbstractApplication (String name, IGuiTexture icon) {
        super(Position.ORIGIN, new Size(333, 232));
        this.name = name;
        this.icon = icon;
    }

    public AbstractApplication setOs(TerminalOSWidget os) {
        this.os = os;
        return this;
    }

    public String getName() {
        return name;
    }

    public IGuiTexture getIcon() {
        return icon;
    }

    public abstract AbstractApplication createApp(boolean isClient, NBTTagCompound nbt);

    public void closeApp(boolean isClient, NBTTagCompound nbt) {
    }

    public boolean isBackgroundApp() {
        return false;
    }

    public TerminalOSWidget getOs() {
        return os;
    }
}
