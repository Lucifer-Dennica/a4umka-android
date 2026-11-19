#!/bin/bash
# Скрипт для переименования a4umka → sovenok в проекте Android
# Запускать из КОРНЯ проекта (там, где папка app/ и build.gradle.kts)
#Usage: bash rename_a4umka_to_sovenok.sh

set -e

echo "=== Переименование a4umka → sovenok ==="

# 1. Заменяем во всех .kt файлах: package, import
echo "1/4: Обновляем package и import во всех .kt файлах..."
find app/src -name "*.kt" -type f -exec sed -i 's/com\.example\.a4umka/com.example.sovenok/g' {} +

# 2. Заменяем в build.gradle.kts (namespace)
echo "2/4: Обновляем namespace в build.gradle.kts..."
sed -i 's/com\.example\.a4umka/com.example.sovenok/g' app/build.gradle.kts

# 3. Переименовываем директорию с исходниками
echo "3/4: Переименовываем директорию com/example/a4umka → com/example/sovenok..."
if [ -d "app/src/main/java/com/example/a4umka" ]; then
    mv app/src/main/java/com/example/a4umka app/src/main/java/com/example/sovenok
    echo "  ✓ app/src/main/java/com/example/sovenok"
fi

# Также для androidTest и test (если есть)
if [ -d "app/src/androidTest/java/com/example/a4umka" ]; then
    mv app/src/androidTest/java/com/example/a4umka app/src/androidTest/java/com/example/sovenok
    echo "  ✓ androidTest переименована"
fi
if [ -d "app/src/test/java/com/example/a4umka" ]; then
    mv app/src/test/java/com/example/a4umka app/src/test/java/com/example/sovenok
    echo "  ✓ test переименована"
fi

# 4. Проверка
echo "4/4: Проверка — остались ли упоминания a4umka?"
remaining=$(grep -rn "a4umka" app/src --include="*.kt" --include="*.kts" 2>/dev/null | wc -l)
if [ "$remaining" -gt 0 ]; then
    echo "  ⚠ Осталось $remaining упоминаний a4umka:"
    grep -rn "a4umka" app/src --include="*.kt" --include="*.kts" 2>/dev/null
else
    echo "  ✓ Все упоминания a4umka удалены из .kt и .kts файлов"
fi

# Проверка applicationId
echo ""
echo "=== Проверка applicationId ==="
grep "applicationId" app/build.gradle.kts

# Проверка namespace
echo "=== Проверка namespace ==="
grep "namespace" app/build.gradle.kts

echo ""
echo "=== Готово! ==="
echo ""
echo "Что было сделано:"
echo "  1. Заменено 'com.example.a4umka' → 'com.example.sovenok' во всех .kt файлах"
echo "  2. Заменено 'com.example.a4umka' → 'com.example.sovenok' в app/build.gradle.kts (namespace)"
echo "  3. Переименована директория app/src/main/java/com/example/a4umka → sovenok"
echo ""
echo "Важно:"
echo "  - applicationId уже был com.sovenok.app — менять не нужно"
echo "  - app_name уже 'Совёнок' — менять не нужно"
echo "  - AndroidManifest.xml ссылается на namespace автоматически, трогать не нужно"
echo ""
echo "После запуска скрипта:"
echo "  1. Откройте проект в Android Studio"
echo "  2. Сделайте Build → Clean Project"
echo "  3. Сделайте Build → Rebuild Project"
echo "  4. Запустите gradle assembleDebug для проверки"
echo ""
echo "Если используете Git:"
echo "  git add -A"
echo "  git commit -m 'Рефакторинг: переименование пакета a4umka → sovenok'"
