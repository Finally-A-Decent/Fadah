package info.preva1l.fadah.hooks;

import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.utils.TaskManager;
import org.jetbrains.annotations.ApiStatus;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class HookManager {
    private static final HookManager instance = new HookManager();

    private final Map<Class<? extends Hook>, Hook> registeredHooks = new HashMap<>();

    private HookManager() {}

    public void registerHook(Class<? extends Hook> hookClass) {
        Hook hook = registeredHooks.get(hookClass);
        if (hook != null) {
            final Hook existingHook = hook;
            Reloadable annotation = existingHook.getClass().getAnnotation(Reloadable.class);
            if (annotation == null) return;
            if (annotation.async()) {
                TaskManager.Async.run(Fadah.getINSTANCE(), () -> {
                    existingHook.disable();
                    if (!existingHook.enable()) {
                        registeredHooks.remove(hookClass);
                        return;
                    }
                    Fadah.getConsole().info("Hook " + hookClass.getName() + " reloaded asynchronously!");
                });
            } else {
                existingHook.disable();
                if (!existingHook.enable()) {
                    registeredHooks.remove(hookClass);
                    return;
                }
                Fadah.getConsole().info("Hook " + hookClass.getName() + " reloaded!");
            }
        }
        try {
            hook = hookClass.getDeclaredConstructor().newInstance();
            if (hook.enable()) {
                registeredHooks.put(hookClass, hook);
                Fadah.getConsole().info("Hook " + hookClass.getName() + " registered!");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void disableHook(Class<? extends Hook> hookClass) {
        Hook hook = registeredHooks.remove(hookClass);
        if (hook == null) return;
        hook.disable();
    }

    public int hookCount() {
        return registeredHooks.size();
    }

    /**
     * Get a hook
     * @param hook the class of the hook
     * @return an optional of the hook, empty if the hook is not registered
     * @since 1.6
     */
    @ApiStatus.Internal
    public <H extends Hook> Optional<H> getHook(Class<H> hook) {
        return Optional.ofNullable(registeredHooks.get(hook)).map(opt -> (H) opt);
    }

    public static HookManager i() {
        return instance;
    }
}
