package client.module.combat;

import client.category.Category;
import client.module.ModuleRegistry;

/**
 * Реестр боевых модулей. Тринадцать модулей категории Combat плюс существующий
 * Pearl Key (ручной бинд жемчуга, вне списка 13).
 */
public final class CombatModules {
    private CombatModules() {
    }

    public static void register(Category combat) {
        ModuleRegistry.register(new AutoCrystalModule(combat));
        ModuleRegistry.register(new AnchorMacroModule(combat));
        ModuleRegistry.register(new AutoTotemModule(combat));
        ModuleRegistry.register(new AutoMaceModule(combat));
        ModuleRegistry.register(new HoverTotemModule(combat));
        ModuleRegistry.register(new ShieldBreakerModule(combat));
        ModuleRegistry.register(new AutoTotemHitModule(combat));
        ModuleRegistry.register(new TriggerBotModule(combat));
        ModuleRegistry.register(new MaceSwapModule(combat));
        ModuleRegistry.register(new AutoDtapModule(combat));
        ModuleRegistry.register(new StunSlamModule(combat));
        ModuleRegistry.register(new AutoHitCrystalModule(combat));
        ModuleRegistry.register(new SafeAnchorModule(combat));
        ModuleRegistry.register(new PearlKeyModule(combat));
    }
}
