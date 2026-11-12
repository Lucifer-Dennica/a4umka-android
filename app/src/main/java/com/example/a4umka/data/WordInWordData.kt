package com.example.a4umka.data

data class WordPair(
    val longWord: String,
    val shortWord: String,
    val emoji: String
)

object WordInWordData {
    private val allPairs = listOf(
        WordPair("ДОЧКА", "ДОЧЬ", "👧"),
        WordPair("ОБЛАКО", "ЛАК", "💅"),
        WordPair("ПОДУШКА", "ДУШ", "🚿"),
        WordPair("КРОТ", "РОТ", "👄"),
        WordPair("ЗАБОР", "БОР", "🌲"),
        WordPair("КОСА", "ОСА", "🐝"),
        WordPair("БАБОЧКА", "БОЧКА", "🦋"),
        WordPair("ХОЛОДИЛЬНИК", "ХОЛОД", "🧊"),
        WordPair("ТАРЕЛКА", "ЕЛКА", "🍽️"),
        WordPair("КОНФЕТА", "ФЕН", "🍬"),
        WordPair("МАШИНА", "ШИНА", "🚗"),
        WordPair("БАРАБАН", "РАБ", "🥁"),
        WordPair("КАРТИНА", "ТИНА", "🖼️"),
        WordPair("ЛОШАДКА", "ШАР", "🐴"),
        WordPair("ПАЛИТРА", "ЛАПА", "🎨")
    )

    fun getRandomPairs(count: Int): List<WordPair> {
        return allPairs.shuffled().take(count)
    }
}
