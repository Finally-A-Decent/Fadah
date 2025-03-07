package info.preva1l.fadah.hooks;

public abstract class Hook {
    protected boolean enabled = false;

    public boolean enable() {
        enabled = onEnable();
        return enabled;
    }

    public void disable() {
        enabled = false;
        onDisable();
    }

    protected abstract boolean onEnable();

    protected void onDisable() {
    }
}
