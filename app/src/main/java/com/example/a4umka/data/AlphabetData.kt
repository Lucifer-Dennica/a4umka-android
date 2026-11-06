package com.example.a4umka.data

data class AlphabetLetter(
    val char: Char,
    val uppercase: String,
    val lowercase: String,
    val word: String
)

object AlphabetData {
    val russianAlphabet = listOf(
        AlphabetLetter('А', "А", "а", "Арбуз"),
        AlphabetLetter('Б', "Б", "б", "Бабочка"),
        AlphabetLetter('В', "В", "в", "Виноград"),
        AlphabetLetter('Г', "Г", "г", "Гриб"),
        AlphabetLetter('Д', "Д", "д", "Дом"),
        AlphabetLetter('Е', "Е", "е", "Ель"),
        AlphabetLetter('Ё', "Ё", "ё", "Ёжик"),
        AlphabetLetter('Ж', "Ж", "ж", "Жук"),
        AlphabetLetter('З', "З", "з", "Зебра"),
        AlphabetLetter('И', "И", "и", "Игрушка"),
        AlphabetLetter('Й', "Й", "й", "Йогурт"),
        AlphabetLetter('К', "К", "к", "Кот"),
        AlphabetLetter('Л', "Л", "л", "Лиса"),
        AlphabetLetter('М', "М", "м", "Мяч"),
        AlphabetLetter('Н', "Н", "н", "Носорог"),
        AlphabetLetter('О', "О", "о", "Облако"),
        AlphabetLetter('П', "П", "п", "Пингвин"),
        AlphabetLetter('Р', "Р", "р", "Рыба"),
        AlphabetLetter('С', "С", "с", "Слон"),
        AlphabetLetter('Т', "Т", "т", "Тигр"),
        AlphabetLetter('У', "У", "у", "Утка"),
        AlphabetLetter('Ф', "Ф", "ф", "Фонарь"),
        AlphabetLetter('Х', "Х", "х", "Хлеб"),
        AlphabetLetter('Ц', "Ц", "ц", "Цветок"),
        AlphabetLetter('Ч', "Ч", "ч", "Часы"),
        AlphabetLetter('Ш', "Ш", "ш", "Шар"),
        AlphabetLetter('Щ', "Щ", "щ", "Щенок"),
        AlphabetLetter('Ъ', "Ъ", "ъ", "Подъезд"),
        AlphabetLetter('Ы', "Ы", "ы", "Сыр"),
        AlphabetLetter('Ь', "Ь", "ь", "Медведь"),
        AlphabetLetter('Э', "Э", "э", "Эскимо"),
        AlphabetLetter('Ю', "Ю", "ю", "Юбка"),
        AlphabetLetter('Я', "Я", "я", "Якорь")
    )
}
