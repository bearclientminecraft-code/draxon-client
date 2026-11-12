# Draxon Client V1

Основа клиента для Minecraft Java Edition 1.21.11 (Fabric). Содержит ClickGUI с пятью пустыми категориями, поиском и системой настроек. Игровых модулей пока нет.

## Требования
- Java 21
- Gradle (или установленный Gradle 8.x)
- Доступ к `maven.fabricmc.net` и `maven.minecraftforge`-не нужен (Forge/NeoForge не используются)

## Сборка
1. Заполните в `gradle.properties` значения `loader_version`, `fabric_version`, `loom_version` актуальными версиями с https://fabricmc.net/develop.
2. Выполните:
   ```
   gradle build
   ```
3. Готовый JAR: `build/libs/draxon-client-1.0.0-V1.jar`.
4. Положите JAR в `mods/` вместе с Fabric API для 1.21.11.

## Управление
- **Right Shift** — открыть/закрыть ClickGUI (клавишу можно изменить в `config/draxon.json`, поле `clickGuiKey`, код GLFW).
- **ESC** — закрыть ClickGUI.
- Перетаскивание панелей за заголовок; позиции сохраняются.
- Колесо мыши — прокрутка содержимого панели.

## Структура
```
src/main/java/client/
  DraxonClient.java          точка входа
  category/                  категории и иконки, CategoryRegistry
  module/                    базовый класс Module и ModuleRegistry
  config/ConfigManager.java  сохранение настроек в config/draxon.json
  keybind/KeybindManager.java клавиши и защита от конфликтов
  render/                    примитивы отрисовки и цвета
  gui/                       ClickGuiScreen, CategoryPanel, SearchBar
  gui/components/            Component, Button, TextField
  gui/animation/Animation.java
  gui/theme/Theme.java       темы (midnight, graphite)
```

## Добавление модулей
Наследуйте `client.module.Module`, укажите категорию из `CategoryRegistry` и вызовите `ModuleRegistry.register(...)` в `DraxonClient.onInitializeClient()`. Модуль появится в панели категории через `CategoryPanel.addComponent(...)`, когда будет добавлен элемент интерфейса.

## Добавление категорий и тем
- Категория: `CategoryRegistry.register(new Category(id, name, icon))`.
- Тема: создайте `Theme` через приватный конструктор внутри класса и зарегистрируйте её в `Theme.register`, затем укажите имя в настройках `theme`.

## Статус проверки
Код написан без доступа к Minecraft-зависимостям, поэтому не скомпилирован и не запускался в игре. При первой сборке возможны ошибки, связанные с точными сигнатурами API 1.21.11 (см. раздел ниже).

## Места для проверки при первой сборке
- Сигнатуры событий ввода `Screen`: `mouseClicked(MouseButtonEvent, boolean)`, `keyPressed(KeyEvent)`, `charTyped(CharacterEvent)` и методы `KeyEvent`/`MouseButtonEvent`.
- `KeyMapping.Category.register(ResourceLocation)` и `ResourceLocation.fromNamespaceAndPath`.
- `Screen.renderBackground(GuiGraphics, int, int, float)`, которое даёт размытие фона.
- Имена полей `minecraft`/`font` и метода `minecraft.setScreen`.
