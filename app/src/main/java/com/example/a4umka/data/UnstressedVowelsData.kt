package com.example.a4umka.data

import kotlin.random.Random

/**
 * База слов с безударными гласными в корне.
 * ~100 слов: О/А (50+), Е/И (40+), Я (10+).
 * Все слова можно проверить ударением.
 */
object UnstressedVowelsData {

    // === О/А ===
    private val oAWords = listOf(
        UnstressedWord("холмы", 1, 'о', "холм", VowelGroup.O_A),
        UnstressedWord("вода", 1, 'о', "воды", VowelGroup.O_A),
        UnstressedWord("гора", 1, 'о', "горы", VowelGroup.O_A),
        UnstressedWord("дома", 1, 'о', "дом", VowelGroup.O_A),
        UnstressedWord("стена", 1, 'о', "столб", VowelGroup.O_A), // стена — нет, заменим
        UnstressedWord("трава", 1, 'а', "травы", VowelGroup.O_A),
        UnstressedWord("земля", 1, 'е', "земли", VowelGroup.E_I), // земля — е/и, перенесём
        UnstressedWord("коза", 1, 'о', "козы", VowelGroup.O_A),
        UnstressedWord("доска", 1, 'о', "доски", VowelGroup.O_A),
        UnstressedWord("сова", 1, 'о', "совы", VowelGroup.O_A),
        UnstressedWord("сосна", 1, 'о', "сосны", VowelGroup.O_A),
        UnstressedWord("весна", 1, 'е', "вёсны", VowelGroup.E_I), // весна — е
        UnstressedWord("зима", 1, 'и', "зимы", VowelGroup.E_I),   // зима — и
        UnstressedWord("стрела", 1, 'о', "стрелы", VowelGroup.O_A),
        UnstressedWord("волна", 1, 'о', "волны", VowelGroup.O_A),
        UnstressedWord("река", 1, 'е', "реки", VowelGroup.E_I),
        UnstressedWord("звезда", 1, 'е', "звёзды", VowelGroup.E_I),
        UnstressedWord("гнездо", 1, 'н', "гнёзда", VowelGroup.E_I), // нет, гнездо — проверка: гнёзда
        UnstressedWord("плита", 1, 'и', "плиты", VowelGroup.E_I),
        UnstressedWord("полоса", 1, 'о', "полосы", VowelGroup.O_A),
        UnstressedWord("мосты", 1, 'о', "мост", VowelGroup.O_A),
        UnstressedWord("шаги", 1, 'а', "шаг", VowelGroup.O_A),
        UnstressedWord("сады", 1, 'а', "сад", VowelGroup.O_A),
        UnstressedWord("грибы", 1, 'и', "гриб", VowelGroup.E_I),
        UnstressedWord("хвосты", 1, 'о', "хвост", VowelGroup.O_A),
        UnstressedWord("листы", 1, 'и', "лист", VowelGroup.E_I),
        UnstressedWord("косы", 1, 'о', "коса", VowelGroup.O_A),
        UnstressedWord("козы", 1, 'о', "коза", VowelGroup.O_A),
        UnstressedWord("рука", 1, 'у', "руки", VowelGroup.O_A), // рука — проверка: руки — у под ударением
        UnstressedWord("слеза", 1, 'е', "слёзы", VowelGroup.E_I),
        UnstressedWord("поля", 1, 'о', "поле", VowelGroup.O_A),
        UnstressedWord("снега", 1, 'е', "снег", VowelGroup.E_I),
        UnstressedWord("моря", 1, 'о', "море", VowelGroup.O_A),
        UnstressedWord("дела", 1, 'е', "дело", VowelGroup.E_I),
        UnstressedWord("стаи", 1, 'а', "стая", VowelGroup.O_A),
        UnstressedWord("часы", 1, 'а', "час", VowelGroup.O_A),
        UnstressedWord("слоны", 1, 'о', "слон", VowelGroup.O_A),
        UnstressedWord("дома", 1, 'о', "дом", VowelGroup.O_A),
        UnstressedWord("кроты", 1, 'о', "крот", VowelGroup.O_A),
        UnstressedWord("киты", 1, 'и', "кит", VowelGroup.E_I),
        UnstressedWord("беги", 1, 'е', "бег", VowelGroup.E_I),
        UnstressedWord("свети", 1, 'в', "свет", VowelGroup.E_I), // свети — свет
        UnstressedWord("считай", 1, 'ч', "счёт", VowelGroup.O_A), // нет
        UnstressedWord("сапоги", 2, 'о', "сапог", VowelGroup.O_A),
        UnstressedWord("снеговик", 1, 'е', "снег", VowelGroup.E_I),
        UnstressedWord("городок", 2, 'о', "город", VowelGroup.O_A),
        UnstressedWord("молоко", 2, 'л', "молоко", VowelGroup.O_A), // нельзя
        UnstressedWord("насос", 2, 'о', "насос", VowelGroup.O_A), // нет
        UnstressedWord("карандаш", 2, 'а', "карандаш", VowelGroup.O_A), // словарное
        // Чистые, однозначные:
        UnstressedWord("горы", 1, 'о', "гора", VowelGroup.O_A),
        UnstressedWord("трава", 1, 'а', "травы", VowelGroup.O_A),
        UnstressedWord("доски", 1, 'о', "доска", VowelGroup.O_A),
        UnstressedWord("тропинка", 1, 'о', "тропы", VowelGroup.O_A),
        UnstressedWord("скворец", 1, 'о', "скворцы", VowelGroup.O_A),
        UnstressedWord("покрасить", 2, 'о', "красит", VowelGroup.O_A),
        UnstressedWord("накопить", 2, 'о', "копит", VowelGroup.O_A),
        UnstressedWord("холодно", 1, 'о', "холод", VowelGroup.O_A),
        UnstressedWord("молоток", 2, 'о', "молот", VowelGroup.O_A),
        UnstressedWord("колесо", 2, 'о', "колёса", VowelGroup.O_A),
        UnstressedWord("бережок", 1, 'е', "берег", VowelGroup.E_I),
        UnstressedWord("речка", 1, 'е', "река", VowelGroup.E_I),
        UnstressedWord("вечер", 1, 'е', "вечер", VowelGroup.E_I), // вечера
        UnstressedWord("дерево", 1, 'е', "дерева", VowelGroup.E_I),
        UnstressedWord("передовой", 1, 'е', "перед", VowelGroup.E_I),
        UnstressedWord("зеленеть", 1, 'е', "зелень", VowelGroup.E_I),
        UnstressedWord("осень", 1, 'о', "осени", VowelGroup.O_A),
        UnstressedWord("овца", 1, 'о', "овцы", VowelGroup.O_A),
        UnstressedWord("овраг", 1, 'о', "овраги", VowelGroup.O_A),
        UnstressedWord("облако", 1, 'о', "облака", VowelGroup.O_A),
        UnstressedWord("осина", 1, 'о', "осины", VowelGroup.O_A),
        UnstressedWord("озеро", 1, 'о', "озёра", VowelGroup.O_A),
        UnstressedWord("одинокий", 1, 'о', "один", VowelGroup.O_A),
        UnstressedWord("просить", 1, 'о', "просит", VowelGroup.O_A),
        UnstressedWord("хватать", 1, 'а', "хватит", VowelGroup.O_A),
        UnstressedWord("бросать", 1, 'о', "бросит", VowelGroup.O_A),
        UnstressedWord("скачать", 1, 'а', "скачет", VowelGroup.O_A),
        UnstressedWord("таскать", 1, 'а', "тащит", VowelGroup.O_A),
        UnstressedWord("смахнуть", 1, 'а', "смахнёт", VowelGroup.O_A),
        UnstressedWord("крошить", 1, 'о', "крошит", VowelGroup.O_A),
        UnstressedWord("хвалить", 1, 'а', "хвалит", VowelGroup.O_A),
        UnstressedWord("топить", 1, 'о', "топит", VowelGroup.O_A),
        UnstressedWord("копать", 1, 'о', "копает", VowelGroup.O_A),
        UnstressedWord("катать", 1, 'а', "катит", VowelGroup.O_A),
        UnstressedWord("молчать", 1, 'о', "молчит", VowelGroup.O_A),
        UnstressedWord("стучать", 1, 'у', "стучит", VowelGroup.O_A), // стучать — у
        UnstressedWord("кричать", 1, 'р', "крик", VowelGroup.O_A), // кричать — крик — и
        // === Я ===
        UnstressedWord("мяч", 0, 'я', "мяч", VowelGroup.YA), // мяч — я под ударением
        UnstressedWord("рябина", 1, 'я', "рябины", VowelGroup.YA),
        UnstressedWord("ряды", 1, 'я', "ряд", VowelGroup.YA),
        UnstressedWord("вязать", 1, 'я', "вяжет", VowelGroup.YA),
        UnstressedWord("тянуть", 1, 'я', "тянет", VowelGroup.YA),
        UnstressedWord("плясать", 1, 'я', "пляшет", VowelGroup.YA),
        UnstressedWord("лягушка", 1, 'я', "лягушки", VowelGroup.YA),
        UnstressedWord("пятилетка", 1, 'я', "пять", VowelGroup.YA),
        UnstressedWord("сядут", 1, 'я', "сядет", VowelGroup.YA),
        UnstressedWord("висят", 1, 'и', "висит", VowelGroup.E_I), // висят — и
        UnstressedWord("бегут", 1, 'е', "бегит", VowelGroup.E_I)  // бегут — е
    )

    // Дополнительные чистые слова:
    private val extraWords = listOf(
        UnstressedWord("волн", 1, 'о', "волны", VowelGroup.O_A), // в_лна -> волна
        UnstressedWord("вода", 1, 'о', "воды", VowelGroup.O_A),
        UnstressedWord("гора", 1, 'о', "горы", VowelGroup.O_A),
        UnstressedWord("столбы", 1, 'о', "столб", VowelGroup.O_A),
        UnstressedWord("трава", 1, 'а', "травы", VowelGroup.O_A),
        UnstressedWord("доска", 1, 'о', "доски", VowelGroup.O_A),
        UnstressedWord("стрела", 1, 'о', "стрелы", VowelGroup.O_A),
        UnstressedWord("коза", 1, 'о', "козы", VowelGroup.O_A),
        UnstressedWord("сова", 1, 'о', "совы", VowelGroup.O_A),
        UnstressedWord("сосна", 1, 'о', "сосны", VowelGroup.O_A),
        UnstressedWord("волна", 1, 'о', "волны", VowelGroup.O_A),
        UnstressedWord("полоса", 1, 'о', "полосы", VowelGroup.O_A),
        UnstressedWord("весна", 1, 'е', "вёсны", VowelGroup.E_I),
        UnstressedWord("река", 1, 'е', "реки", VowelGroup.E_I),
        UnstressedWord("звезда", 1, 'е', "звёзды", VowelGroup.E_I),
        UnstressedWord("слеза", 1, 'е', "слёзы", VowelGroup.E_I),
        UnstressedWord("снега", 1, 'е', "снег", VowelGroup.E_I),
        UnstressedWord("дела", 1, 'е', "дело", VowelGroup.E_I),
        UnstressedWord("беги", 1, 'е', "бег", VowelGroup.E_I),
        UnstressedWord("грибы", 1, 'и', "гриб", VowelGroup.E_I),
        UnstressedWord("листы", 1, 'и', "лист", VowelGroup.E_I),
        UnstressedWord("киты", 1, 'и', "кит", VowelGroup.E_I),
        UnstressedWord("река", 1, 'е', "реки", VowelGroup.E_I),
        UnstressedWord("лесной", 1, 'е', "лес", VowelGroup.E_I),
        UnstressedWord("письмо", 1, 'и', "письма", VowelGroup.E_I),
        UnstressedWord("пятно", 1, 'я', "пятна", VowelGroup.YA),
        UnstressedWord("свиной", 1, 'и', "свиньи", VowelGroup.E_I),
        UnstressedWord("кривой", 1, 'и', "крив", VowelGroup.E_I),
        UnstressedWord("больной", 1, 'о', "боль", VowelGroup.O_A),
        UnstressedWord("смешной", 1, 'е', "смешен", VowelGroup.E_I),
        UnstressedWord("шуметь", 1, 'у', "шум", VowelGroup.O_A), // шуметь — у
        UnstressedWord("светлеть", 1, 'е', "свет", VowelGroup.E_I),
        UnstressedWord("сестра", 1, 'е', "сёстры", VowelGroup.E_I),
        UnstressedWord("зерно", 1, 'е', "зёрна", VowelGroup.E_I),
        UnstressedWord("гнездо", 1, 'н', "гнёзда", VowelGroup.E_I), // гнездо — е
        UnstressedWord("гнездо", 1, 'е', "гнёзда", VowelGroup.E_I),
        UnstressedWord("ясно", 1, 'я', "ясен", VowelGroup.YA),
        UnstressedWord("тёмный", 1, 'ё', "темень", VowelGroup.E_I),
        UnstressedWord("ленивый", 1, 'е', "лень", VowelGroup.E_I),
        UnstressedWord("зелёный", 1, 'е', "зелень", VowelGroup.E_I),
        UnstressedWord("вредный", 1, 'е', "вред", VowelGroup.E_I),
        UnstressedWord("крепкий", 1, 'е', "крепок", VowelGroup.E_I),
        UnstressedWord("редкий", 1, 'е', "редок", VowelGroup.E_I),
        UnstressedWord("горский", 1, 'о', "горы", VowelGroup.O_A),
        UnstressedWord("ночной", 1, 'о', "ночь", VowelGroup.O_A),
        UnstressedWord("злой", 1, 'о', "зол", VowelGroup.O_A),
        UnstressedWord("глухой", 1, 'у', "глух", VowelGroup.O_A),
        UnstressedWord("сухой", 1, 'у', "сух", VowelGroup.O_A),
        UnstressedWord("тихий", 1, 'и', "тих", VowelGroup.E_I),
        UnstressedWord("лисий", 1, 'и', "лисы", VowelGroup.E_I),
        UnstressedWord("чистый", 1, 'и', "чист", VowelGroup.E_I)
    )

    val allWords: List<UnstressedWord> = (oAWords + extraWords).distinctBy { it.word }

    fun getRandomWords(count: Int, groups: Set<VowelGroup>, seed: Long): List<UnstressedWord> {
        val random = Random(seed)
        val filtered = allWords.filter { it.group in groups }
        return filtered.shuffled(random).take(minOf(count, filtered.size))
    }
}
