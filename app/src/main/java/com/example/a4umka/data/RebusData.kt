package com.example.a4umka.data

data class RebusWord(val word: String, val emoji: String)

object RebusData {
    // Словарь с эмодзи для каждой буквы. По несколько вариантов на популярные буквы.
    private val dictionary: Map<Char, List<RebusWord>> = mapOf(
        'А' to listOf(RebusWord("Арбуз", "🍉"), RebusWord("Ананас", "🍍"), RebusWord("Автобус", "🚌")),
        'Б' to listOf(RebusWord("Бабочка", "🦋"), RebusWord("Банан", "🍌"), RebusWord("Барабан", "🥁")),
        'В' to listOf(RebusWord("Велосипед", "🚲"), RebusWord("Виноград", "🍇"), RebusWord("Волк", "🐺")),
        'Г' to listOf(RebusWord("Гриб", "🍄"), RebusWord("Гитара", "🎸"), RebusWord("Груша", "🍐")),
        'Д' to listOf(RebusWord("Дом", "🏠"), RebusWord("Дерево", "🌳"), RebusWord("Дельфин", "🐬")),
        'Е' to listOf(RebusWord("Ель", "🌲"), RebusWord("Енот", "🦝")),
        'Ё' to listOf(RebusWord("Ёжик", "🦔"), RebusWord("Ёлка", "🎄")),
        'Ж' to listOf(RebusWord("Жук", "🐞"), RebusWord("Жираф", "🦒"), RebusWord("Жаба", "🐸")),
        'З' to listOf(RebusWord("Зебра", "🦓"), RebusWord("Зонт", "☂️"), RebusWord("Заяц", "🐰")),
        'И' to listOf(RebusWord("Игрушка", "🧸"), RebusWord("Индюк", "🦃"), RebusWord("Икра", "🍣")),
        'Й' to emptyList(), // Готовая буква
        'К' to listOf(RebusWord("Кот", "🐱"), RebusWord("Книга", "📚"), RebusWord("Ключ", "🔑")),
        'Л' to listOf(RebusWord("Лимон", "🍋"), RebusWord("Лиса", "🦊"), RebusWord("Луна", "🌙")),
        'М' to listOf(RebusWord("Мяч", "⚽"), RebusWord("Мышь", "🐭"), RebusWord("Машина", "🚗")),
        'Н' to listOf(RebusWord("Носорог", "🦏"), RebusWord("Ножницы", "✂️"), RebusWord("Небо", "☁️")),
        'О' to listOf(RebusWord("Облако", "☁️"), RebusWord("Огурец", "🥒"), RebusWord("Окно", "🪟")),
        'П' to listOf(RebusWord("Пингвин", "🐧"), RebusWord("Пицца", "🍕"), RebusWord("Птица", "🐦")),
        'Р' to listOf(RebusWord("Рыба", "🐟"), RebusWord("Ракета", "🚀"), RebusWord("Роза", "🌹")),
        'С' to listOf(RebusWord("Слон", "🐘"), RebusWord("Сыр", "🧀"), RebusWord("Самолет", "✈️")),
        'Т' to listOf(RebusWord("Тигр", "🐯"), RebusWord("Торт", "🍰"), RebusWord("Туча", "🌧️")),
        'У' to listOf(RebusWord("Утка", "🦆"), RebusWord("Улитка", "🐌"), RebusWord("Утюг", "🧺")),
        'Ф' to listOf(RebusWord("Фонарь", "🔦"), RebusWord("Футболка", "👕"), RebusWord("Фрукты", "🍎")),
        'Х' to listOf(RebusWord("Хлеб", "🍞"), RebusWord("Хомяк", "🐹"), RebusWord("Холод", "❄️")),
        'Ц' to listOf(RebusWord("Цветок", "🌸"), RebusWord("Цыпленок", "🐥"), RebusWord("Цирк", "🎪")),
        'Ч' to listOf(RebusWord("Часы", "⏰"), RebusWord("Черепаха", "🐢"), RebusWord("Чеснок", "🧄")),
        'Ш' to listOf(RebusWord("Шар", "🎈"), RebusWord("Шапка", "🧢"), RebusWord("Шоколад", "🍫")),
        'Щ' to listOf(RebusWord("Щенок", "🐶"), RebusWord("Щетка", "🪥"), RebusWord("Щука", "🐟")),
        'Ъ' to emptyList(), // Готовая буква
        'Ы' to emptyList(), // Готовая буква
        'Ь' to emptyList(), // Готовая буква
        'Э' to listOf(RebusWord("Эскимо", "🍦"), RebusWord("Экскаватор", "🚜")),
        'Ю' to listOf(RebusWord("Юбка", "👗"), RebusWord("Юла", "🎡")),
        'Я' to listOf(RebusWord("Якорь", "⚓"), RebusWord("Яблоко", "🍎"), RebusWord("Ягода", "🍓"))
    )

    fun getParsedWords(input: String): List<String> {
        return input.split("\n")
            .map { it.trim().uppercase() }
            .filter { it.isNotBlank() && it.length <= 18 }
    }

    fun getRebusWordsForWord(word: String): List<RebusWord?> {
        val result = mutableListOf<RebusWord?>()
        val usedWords = mutableSetOf<String>()

        for (char in word) {
            if (char == ' ') {
                result.add(null) // Пробел
                continue
            }
            val variants = dictionary[char]
            if (variants.isNullOrEmpty()) {
                result.add(null) // Для Й, Ы, Ь, Ъ или неизвестных
            } else {
                // Ищем вариант, который еще не использовался в этом слове
                val available = variants.filter { it.word !in usedWords }
                val chosen = if (available.isNotEmpty()) available.random() else variants.random()
                usedWords.add(chosen.word)
                result.add(chosen)
            }
        }
        return result
    }

    // Для листа ответов (просто слова подряд)
    fun getAnswersForWord(word: String): String {
        val rebuses = getRebusWordsForWord(word)
        return rebuses.mapIndexed { index, rebus ->
            val char = word[index]
            if (rebus != null) rebus.word else "буква $char"
        }.joinToString(", ")
    }
}
