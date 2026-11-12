# Происхождение материалов BLOODHELPER

Собственный код и документация nomielua распространяются по [закрытой лицензии BloodHelper](LICENSE).
Границы лицензии описаны в [LICENSING.md](LICENSING.md). Этот выбор не меняет
условия сторонних частей и не заменяет отсутствующие разрешения на их распространение.

Этот файл фиксирует использованные источники и сведения о лицензиях, доступные
при подготовке проекта и проверке 15 октября 2026 года. Новые классы BLOODHELPER находятся
в пакете `ru.bloodhelper.client`. Minecraft, Fabric API и Fabric Loader
используются как внешние зависимости и не включаются целиком в JAR клиента.

## Changes for Cooldown

- Проект: [Changes for Cooldown](https://modrinth.com/mod/changes-for-cooldown).
- Исследованная версия: **1.0.2 для Minecraft 1.21.11**,
  [версия Modrinth Quyq5rfb](https://modrinth.com/mod/changes-for-cooldown/version/Quyq5rfb).
- Автор в исходном `fabric.mod.json`: **Yz1kk**.
- Пользователь сообщил о разрешении владельца на перенос функций.

В BLOODHELPER адаптированы отображение активных кулдаунов предметов и соглашение
о выделенной группе `minecraft:trident` для PvP-таймера. Реализация перенесена
на собственную конфигурацию и общий интерфейс, добавлены рабочие переключатели,
масштаб, прогресс, обработка неизвестных групп и редактор HUD. Исходный мод,
его AutoConfig/Cloth Config и интеграция Mod Menu в JAR BLOODHELPER не упакованы.

В источнике есть несовпадение обозначений лицензии: страница Modrinth и
`fabric.mod.json` указывают **MIT**, а вложенный файл
`LICENSE_changes-for-cooldown` содержит **CC0 1.0 Universal**. Оба исходных
свидетельства сохранены без исправления:

- [Оригинальный текст вложенной лицензии](docs/licenses/LICENSE_changes-for-cooldown).
- [Оригинальный fabric.mod.json версии 1.0.2](docs/licenses/changes-for-cooldown-1.0.2-fabric.mod.json).

Эта запись не заменяет противоречащие друг другу сведения новой лицензией и не
присваивает авторство исходной функции разработчику BLOODHELPER.

## Punch ClickGUI

Источник — предоставленный пользователем `Punch_ClickGUI_Package.rar`, Java-пакеты
`org.blade`. Ранее подготовленный `Punch_ClickGUI_Clean` использован как образец
интерфейса. В полученном наборе исходников отдельный файл лицензии и полное
описание авторства отсутствуют.

Punch использовался как визуальный референс ранних вариантов `BloodHelperScreen`
и `UiDraw`. В версии 1.0.4 основная компоновка меню заменена адаптацией Expensive,
описанной ниже. Внутренние зависимости рендера Punch, его менеджер функций
и игровые модули в клиент не включены.

## Expensive 4.0: GUI и файлы OTF

Источник: опубликованный репозиторий
[dest4590/Expensive_4.0](https://github.com/dest4590/Expensive_4.0), коммит
`3cf31b7ea09d28312e9743b2f5dd71498ba682a5`. В его `gradle.properties` указана
Minecraft **1.20.1**. В BLOODHELPER GUI адаптирован к **1.21.11**; версия донора
не выдаётся за 1.21.11. Связь публикации с предоставленной темой
[YouGame 387054](https://yougame.biz/threads/387054/) не подтверждена.

За основу взяты компоновка `MenuScreen`, боковые категории, две колонки
карточек, заголовки, чекбоксы и настройки внутри карточек. Фон `background.png`
и вкладка `tab.png` перекрашены с сохранением геометрии, узора и прозрачности.
У иконок ico/render/palette/settings RGB заменён белой маской для окрашивания
темой; размеры и альфа-канал сохранены. Check, search и steve перенесены побайтно.
Список файлов, исходные пути, коммит и SHA-256 находятся в
`assets/bloodhelper/textures/expensive/provenance.json`.

Побайтово перенесены два шрифтовых файла из
`src/main/resources/assets/minecraft/expensive/fonts/`:

| Файл | Встроенное имя | SHA-256 |
| --- | --- | --- |
| `sfpromedium.otf` | SF Pro Display Medium | `cbfd17919856efcedabdd73781dad2984326928ae7c9635bef6227b1a7d71d21` |
| `sfprosemibold.otf` | SF Pro Display Semibold | `53a55ec7e0a90c0e1980b9d95e3cb93851b4e473b575693813a1b150066661ff` |

В проекте файлы находятся в `assets/bloodhelper/fonts/`; дополнительная запись
о них — `CustomFont-provenance.txt`. Собственный `CustomFont` загружает OTF через
AWT, получает метрики и растеризует глифы в свои атласы. Донорские классы рендера
не используются. Код игровых функций, управление движением, сетевые обработчики,
аккаунты, авторизация, загрузчик и сборочные зависимости донора не перенесены.
Архив донора исследован как данные; чужой клиент не собирался и не запускался.

Исходный `LICENSE` содержит `Copyright (c) 2024 salam4ik, dedinside, ****` и
`All rights reserved.` Он сохранён без изменений в
[docs/licenses/LICENSE_Expensive_4.0](docs/licenses/LICENSE_Expensive_4.0).
Встроенные сведения об авторских правах шрифтов также сохранены. Публикация
репозитория и этот перечень происхождения не заменяют условия правообладателей
новой лицензией. Оформление и шрифты не объявляются оригинальными работами
BLOODHELPER. Подробности адаптации:
[docs/EXPENSIVE_GUI_RU.md](docs/EXPENSIVE_GUI_RU.md).

## KineticLyrics / Wyvern

Источник — предоставленный пользователем `zzz.rar`, семь файлов с пакетами
`wtf.wyvern`, включая `KineticLyrics`, `LyricParticle3D`, LRC-парсер и LRCLIB-клиент.
Отдельного текста лицензии или полного описания авторства в полученном архиве
нет.

В BLOODHELPER адаптирована идея синхронизированных строк в трёхмерном мире,
закреплённых в месте появления, и режимы анимации. Реализация использует Fabric
1.21.11, собственный LRC-сервис, рендер Minecraft и мост Windows GSMTC. Wyvern,
его шрифты, система модулей и библиотека `mediatransport4j` не упакованы в клиент.

## Изображения Bloodstone

Источник: [Trard/bloodstone3](https://github.com/Trard/bloodstone3), коммит
`782ea4fc458a7f3d4c9738629d2d2ad3ff59676f`.

- `pack.png` — исходная пиксельная иконка сердца.
- `assets/logo/textures/bloodstone.png` и части
  `assets/logo/textures/bloodstone_big/logo_1.png`–`logo_4.png` — исходный логотип.
- Предыдущая составная версия BLOOD/сердце/HELPER сохранена в `branding/legacy`.
- В версии 1.0.2 логотип меню заменён изображением, присланным пользователем
  12 октября 2026 (`branding/source/user-logo-2026-10-12.png`). Убраны только
  пустые поля и внешняя подложка; буквы и цвета не перерисованы.
- 13 октября 2026 по уточнению пользователя возвращено прежнее изображение:
  BLOOD над HELPER, по центру. Слова разделены на полную высоту, их противоположная
  перспектива исправлена геометрически; обе строки имеют одинаковые видимые размеры.
  Буквы, фактура и объём не перерисованы, но пиксели готового PNG меняются при
  геометрической обработке. Исходный файл сохранён без изменений; расположение,
  обработка и контрольные суммы описаны в `branding/manifest.json`.

Исходные изображения и сведения о происхождении сохранены в `branding/source/`,
размеры и SHA-256 — в [branding/manifest.json](branding/manifest.json).
[Описание генерации](branding/README_RU.md) содержит прямые ссылки на каждый
исходный файл по зафиксированному коммиту. В полученных материалах отдельные
условия лицензии изображений не указаны; проект сохраняет исходное авторство
Bloodstone и не объявляет эти изображения своими оригинальными работами.

## Внешние компоненты и сервисы

| Компонент | Использование и источник |
| --- | --- |
| Fabric Loader | Внешний загрузчик модов. [Исходная лицензия Apache-2.0](https://github.com/FabricMC/fabric-loader/blob/master/LICENSE). |
| Fabric API | Внешняя зависимость событий, GUI, ввода и рендера. [Исходная лицензия Apache-2.0](https://github.com/FabricMC/fabric-api/blob/1.21.11/LICENSE). |
| Minecraft / официальные имена классов Mojang | Игра, API и имена для разработки; полные игровые файлы в JAR BLOODHELPER не распространяются. |
| KuGou | Дополнительный внешний источник LRC-текстов: HTTPS `lyrics.kugou.com/search` и `/download`. Запросы только при включённом тексте и выборе KuGou/автоматического поиска; база текстов не распространяется. |
| LRCLIB | Внешний сервис синхронизированных текстов песен: [lrclib.net](https://lrclib.net). Тексты запрашиваются во время работы только при включённой функции; готовая база песен в клиент не включена. |
| Windows GSMTC | Системный API текущей медиасессии. [Документация Microsoft](https://learn.microsoft.com/en-us/uwp/api/windows.media.control.globalsystemmediatransportcontrolssessionmanager). Мост PowerShell написан для BLOODHELPER. |

Клиент не заявляет аффилированность с Mojang, Microsoft, Fabric, Punch, Expensive, Delta, Wyvern,
LRCLIB или авторами исходного мода и ресурспака.

## Ранее исследованный Delta 1.21.11 и оформление HUD

Источник: [Kuriom/delta1.21.11](https://github.com/Kuriom/delta1.21.11),
коммит `5f7b662ca903ef24585bf06b8d325c39b47dea0e`.
В `gradle.properties` указана версия Minecraft 1.21.11; в `fabric.mod.json` —
Delta 3.0.14. Происхождение самой публикации независимо не удостоверено.
Переданная пользователем тема [YouGame 387054](https://yougame.biz/threads/387054/)
возвращала HTTP 429, поэтому её связь с этим репозиторием не подтверждена.

В предыдущих версиях были перенесены четыре шрифтовых ресурса:

- `assets/delta/fonts/onest_regular.json` и `onest_regular.png` — Onest,
  используемый исходным `GUIPanel` в меню;
- `assets/delta/fonts/gt_regular.json` и `gt_regular.png` — атлас HUD,
  используемый исходными `Widget` и `CooldownsWidget`. Название `gt_regular`
  сохранено: семейство и правообладатель по нему не выдумываются.

Это MSDF/MTSDF-атласы с исходными метриками. В 1.0.4 они, прежний адаптер
к Minecraft `Font` и связанные текстовые шейдеры удалены. Текущий выпуск
не упаковывает Onest/`gt_regular`: меню, поля ввода и HUD используют
OTF-рендерер `CustomFont`, описанный выше.

По визуальному коду HUD адаптированы компактные отдельные шапки/строки,
расположение иконок, разделители и выравнивание времени. Система модулей, игровые функции,
сетевые обработчики, авторизация и загрузчик Delta не включены. Целый чужой
клиент не собирался и не запускался.

Отдельной лицензии шрифтов в просмотренном исходном проекте нет.
Поле `license` в его `fabric.mod.json` содержит `Minecraft EULA`; это исходное
обозначение не используется как новая лицензия на сторонние шрифты.
Атласы и чужое оформление не объявляются оригинальными работами BLOODHELPER.

## Inter и 3D-тексты

Существующий шрифт на основе [Inter 4.1](https://github.com/rsms/inter/releases/tag/v4.1),
Copyright 2016 The Inter Project Authors, сохранён для текстов песен в мире.
3D-рендерер продолжает использовать Minecraft `Font` через `UiFonts.world()`;
меню и HUD работают через `CustomFont` с OTF. Новому OTF-рендереру Inter
не служит скрытым запасным шрифтом. Исходный variable TTF загружен из
[Google Fonts](https://github.com/google/fonts/tree/main/ofl/inter); SHA-256
сохранён в `branding/fonts/manifest.json`. Статические производные начертания
550/650 с optical size 14 переименованы в BloodHelper UI. Полный текст SIL
Open Font License сохранён в `docs/licenses/OFL-Inter.txt` и включён в JAR.

## Иконки интерфейса 1.0.3

Геометрия 24 значков и генератор SDF-атласа `branding/build_ui_icons.py`
написаны для BLOODHELPER. Этот набор сохраняется для HUD, редактора и части
дополнительных элементов меню; импортированные PNG Expensive перечислены
отдельно выше. Набор не является шрифтовым или игровым кодом Delta.
Сердце в иконке самого приложения
по-прежнему происходит из указанных выше ресурсов Bloodstone.

## Liquid Glass 1.3.1: открытые GLSL-реализации

Исходники проверены 13 октября 2026 года. Оптические формулы и алгоритм размытия
адаптированы для рендера Fabric/Minecraft 1.21.11 из двух проектов:

- [Hla-aung/liquid-glass](https://github.com/Hla-aung/liquid-glass/tree/16e6c369a3f3d17857f830268a3e8713665cf7ab),
  коммит `16e6c369a3f3d17857f830268a3e8713665cf7ab`. Использованы идеи и формулы
  `liquid-glass/engine.ts` (ConvexCircle, градиент поверхности, нормаль, Snell и
  толщина) и `liquid-glass/shaders.ts` (дисперсия по RGB у кромки).
  **MIT, Copyright (c) 2026 Htet Lin Aung**.
  [Исходная лицензия без изменений](docs/licenses/LICENSE_Hla-aung_liquid-glass.txt).
- [ybouane/liquidglass](https://github.com/ybouane/liquidglass/blob/59af227103795f06421a96e2c7d3dceb081decd2/src/shaders.ts),
  коммит `59af227103795f06421a96e2c7d3dceb081decd2`. Из `src/shaders.ts`
  адаптированы девять весов separable Gaussian blur, круговой `bevelHeight`,
  нормаль по конечным разностям, переход от размытого центра к резкой кромке,
  Fresnel и направленные блики. README и package.json объявляют **MIT**;
  отдельного LICENSE и copyright-уведомления в этом коммите нет.
  [Атрибуция и стандартный текст MIT](docs/licenses/LICENSE_ybouane_liquidglass.txt),
  [исходный package.json](docs/licenses/ybouane-liquidglass-package.json).
  Неизвестные юридическое имя и год copyright не добавлены.

В BLOODHELPER написаны Minecraft RenderPipeline/GPU lifecycle, захват текущего
мира, работа с mip 0/mip 1 и отдельной промежуточной текстурой, материалы HUD и
меню, цветовые преобразования и упаковка параметров GUI. Отрицательный порядок
аргументов `smoothstep`, встречающийся в исходном веб-шейдере, не переносился.
JavaScript, React, DOM-capture, внешние зависимости и чужие сборочные скрипты в
клиент не включены и не выполнялись.

Ссылки на формулы, отличия, ограничения и SHA-256:
[docs/LIQUID_GLASS_RU.md](docs/LIQUID_GLASS_RU.md),
[provenance JSON](docs/licenses/liquid-glass-provenance.json).
Полные уведомления MIT включены в JAR через `META-INF/licenses` и в исходный архив.

## Lucide: векторные кнопки музыки

В `UiIcons` и `media_vector.fsh` пропорции `play`, `pause`, `skip-back`,
`skip-forward` адаптированы из [официального репозитория Lucide](https://github.com/lucide-icons/lucide),
коммит `440a1e1dfb1e712505308da46bead3c28b1f1398`.
Исходные SVG сохранены в `docs/licenses/lucide-media/`; ссылки, SHA-256 и
описание адаптации — в `docs/licenses/lucide-media-provenance.json`.

В Minecraft эти четыре значка рисуются геометрически: аналитические расстояния
до треугольников, скруглённых прямоугольников и отрезков вычисляются в GLSL
с антиалиасингом на конечном масштабе. Для читаемости маленьких кнопок контуры
сделаны заполненными. PNG-атлас и ванильные текстовые символы при их рендере
не используются. Плавная волна активности — собственная векторная геометрия
BLOODHELPER, она не измеряет звуковой сигнал проигрывателя.

## Noto Sans CJK: японский, китайский и корейский текст

Для символов, отсутствующих в основном шрифте, добавлен встроенный резервный
шрифт на основе [Noto Sans CJK JP 2.004](https://github.com/notofonts/noto-cjk/releases/tag/Sans2.004),
официальный репозиторий `notofonts/noto-cjk`, коммит
`523d033d6cb47f4a80c58a35753646f5c3608a78`. Использован исходный
`Sans/Variable/TTF/NotoSansCJKjp-VF.ttf`. Copyright из самого шрифта:
`© 2014-2021 Adobe (http://www.adobe.com/), with Reserved Font Name 'Source'.`

Из variable TTF получено статическое начертание веса 400 с помощью fonttools
4.60.1; производное семейство переименовано в **BloodHelper CJK**.
Глифы, cmap, исходные copyright/лицензионные записи и OpenType-таблицы сохранены.
Сборочный скрипт, исходный URL, параметры и SHA-256 находятся в
`branding/fonts/build_cjk_font.py` и `branding/fonts/cjk_manifest.json`.
Полный [SIL Open Font License 1.1](docs/licenses/OFL-NotoSansCJK.txt) включён
в JAR через `META-INF/licenses`.

Меню и HUD растеризуют этот TTF тем же сглаженным AWT-рендерером `CustomFont`;
латиница и кириллица сохраняют прежний основной OTF. Выбор шрифта выполняется
по кодовым точкам Unicode, включая суррогатные пары и комбинируемые знаки.
Тот же файл используется как TrueType fallback для текстов в мире; системные
шрифты не требуются. Японская локальная форма иероглифов служит общей CJK-формой.

Полный исходный объединённый текст ISC/MIT сохранён без изменений в
`docs/licenses/LICENSE_Lucide.txt` и включён в JAR как
`META-INF/licenses/LICENSE_Lucide.txt`.

## Zoomify: плавное приближение

Источник — [isXander/Zoomify](https://github.com/isXander/Zoomify), тег
**2.15.2+1.21.11**, коммит `f5f923279401eac8bbb6b7ac406c869f7377c7b2`.
Автор — isXander; исходник объявляет **LGPL-3.0-or-later**.

`ZoomState.java`, `ZoomInterpolation.java` и `ZoomGameRendererMixin.java` адаптируют интерполяцию,
геометрические шаги колеса и компонуемое преобразование FOV. Адаптация использует
Java, собственные настройки BLOODHELPER, удержание СКМ, ограничение до 100×,
обработку потери фокуса и приоритет перед совпадающим биндом Quick Commands.
Оригинальный интерфейс настроек, отдельный Zoomify и его зависимости в JAR
BLOODHELPER не включены. Начальный масштаб BLOODHELPER по умолчанию — 2,5×;
переходы 1,0/0,5 с используют исходные экспоненциальные кривые Zoomify.
Сглаживание колеса не зависит от FPS и общего выключателя анимаций GUI.

[Описание адаптации](docs/licenses/README_Zoomify.md),
[точные исходные пути и SHA-256](docs/licenses/zoomify-provenance.json),
[LGPL](docs/licenses/LICENSE_Zoomify_LGPL-3.0.txt) и
[сопутствующий GPL](docs/licenses/LICENSE_Zoomify_GPL-3.0.txt) включены в исходный
архив и JAR через `META-INF/licenses`. Адаптированные файлы сохраняют лицензионные
заголовки; исходный архив содержит код и сборочные файлы для их изменения и пересборки.


Каталог VisualHat и 63 события килл-эффектов сверены с
[Trard/bloodstone3, 324f6828a23c26bec96e7b9749a7c8c691293966](https://github.com/Trard/bloodstone3/tree/324f6828a23c26bec96e7b9749a7c8c691293966).
В мод включены только номера CustomModelData, идентификаторы, названия и связи моделей.
Геометрия шляп, текстуры и аудиофайлы берутся из загруженного пользователем ресурспака;
каталог не распространяет эти ассеты. Служебные модели инструмента 556/557
и ванильная тыква 6013 исключены. Венок 6020 использует исходную композицию из 29 моделей.

## Evolution feature adaptations

Selected WorldLyrics, AbilityTrails and LootHighlight portions from Evolution
ClickGUI 1.0.0 by darkich were adapted under permission reported by the project
owner on 2026-10-16. Upstream states All-Rights-Reserved. See
`META-INF/licenses/README_Evolution.md` (source: `docs/licenses/README_Evolution.md`)
for the limited scope and provenance. These portions are not relicensed MIT.


Attribution correction (project owner, 2026-10-16): credit the adapted WorldLyrics, AbilityTrails and LootHighlight features to Minions_. darkich integrated the GUI. Original archive metadata is historical identification, not the feature authorship claim. The previously reported permission scope is unchanged.

## Better Name Visibility

Adapted from Hoetty's Better Name Visibility (MIT, Copyright 2022 Hoetty). See `docs/licenses/README_BetterNameVisibility.md` and `docs/licenses/BetterNameVisibility-MIT.txt`. Source: https://codeberg.org/Hoetty/better-name-visibility

## Optional local AI assistant: Qwen2.5 and llama.cpp

Qwen2.5-0.5B-Instruct by Qwen Team (Apache-2.0) and llama.cpp by its contributors (MIT) are optional external components. They are downloaded only after explicit setup confirmation and are not bundled in this JAR. Model weights are used without fine-tuning. See [assistant documentation](docs/AI_ASSISTANT_RU.md), [Qwen license](docs/licenses/QWEN_APACHE_2.0.txt), and [llama.cpp license](docs/licenses/LLAMA_CPP_MIT.txt).

Sources: https://huggingface.co/Qwen/Qwen2.5-0.5B-Instruct-GGUF and https://github.com/ggml-org/llama.cpp/releases/tag/b11719.

Skins: Rudy geometry/textures retain the supplied Pyz1k archive conditions; Verity by ChocoChip67 is CC-BY-4.0. See [Skins asset provenance](docs/licenses/README_Skins.md).


## Original HUD mods (1.1.19)

AppleSkin 3.0.8+mc1.21.11 by squeek502 is included as an unmodified nested Fabric mod. Source: https://github.com/squeek502/AppleSkin . License: Unlicense; see docs/licenses/AppleSkin-UNLICENSE.txt. Its own nested libraries retain their licenses.

Reach Display by Wolren is a separate, optional original mod, not copied into BloodHelper. On Minecraft 26.3 the companion mod is Advanced Reach Display by Wolren and Blueskiez77 (GPL-3.0-or-later). Native rendering and configuration remain owned by those mods.

## Chat Patches integration

Optional integration with the separately installed Chat Patches by OBro1961 (LGPL-3.0-only). No upstream implementation is copied or bundled by this integration. See [integration provenance](docs/licenses/README_ChatPatches.md).

## ReGlass

Встроенный рендер стекла адаптирован из [RedxAx/ReGlass](https://github.com/RedxAx/ReGlass), commit `352a67f8ccfbfaa709f60cc354d26ca4a5f3af78`. Copyright (c) 2025 ReStudio By RedxAx, MIT. Полный текст лицензии: `META-INF/licenses/REGLASS-LICENSE.txt`. Перенесены расчёты нормали скруглённой поверхности, преломления, дисперсии, Fresnel и блика; используется общий захват/размытие BloodHelper. При установленном внешнем ReGlass встроенный рендер отключён, используется публичный API внешнего мода.


## XZ for Java

XZ for Java 1.10 by Lasse Collin and contributors (0BSD) is bundled to decode the original CJK font without changing any glyph or rendering metrics. Source: https://github.com/tukaani-project/xz-java/tree/v1.10 . License: `META-INF/licenses/XZ-JAVA-0BSD.txt`. The CJK font retains its existing font license.


## Local music decoding libraries

The JAR includes unmodified Java Sound libraries from the soundlibs distribution:

- MP3SPI 1.9.5.4 and VorbisSPI 1.0.3.3 by JavaZOOM.
- JLayer 1.0.1.4 by JavaZOOM and contributors.
- JOrbis 0.0.17.4, Copyright 2000 ymnk / JCraft, Inc., based on Xiph Vorbis.
- Tritonus Share 0.3.7.4 by Matthias Pfisterer and contributors.

Their source headers permit GNU Library General Public License version 2 or later; the supplied LGPL-2.1 text is in `META-INF/licenses/LICENSE_local-music_LGPL-2.1.txt`. Library JARs remain separate in `META-INF/jars`; they are not obfuscated with the private staff implementation. Replacement of these libraries and reverse engineering for debugging modifications to them are permitted under their licenses. The release's `third-party-sources/` directory contains the matching unmodified source JARs, including copyright notices.

Distribution sources: https://github.com/pdudits/soundlibs and https://repo.maven.apache.org/maven2/com/googlecode/soundlibs/ .
