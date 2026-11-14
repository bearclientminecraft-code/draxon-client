package client.module.combat;

import client.category.Category;

/** Реестр боевиков: Hover Totem, Pearl Key, Anchor Macro, Auto Crystal. */
public final class CombatModules {
    private CombatModules() {}

    public static void register(Category combat) {
        client.module.ModuleRegistry.register(new HoverTotemModule(combat));
        client.module.ModuleRegistry.register(new PearlKeyModule(combat));
        client.module.ModuleRegistry.register(new AnchorMacroModule(combat));
        client.module.ModuleRegistry.register(new AutoCrystalModule(combat));
    }
}
