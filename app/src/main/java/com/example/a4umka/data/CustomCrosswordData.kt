package com.example.a4umka.data

import kotlin.random.Random

object CustomCrosswordData {
    val themes = mapOf(
        "Животные" to """
            ЛИСА — Рыжая плутовка из сказок
            ВОЛК — Серый разбойник
            ЗАЯЦ — Трусишка с длинными ушами
            МЕДВЕДЬ — Спит всю зиму в берлоге
            ЕЖ — Колючий клубок
            БЕЛКА — Прыгает по веткам с орешком
            СЛОН — Самый большой на суше
            ТИГР — Полосатый хищник
            ЗЕБРА — Лошадь в полоску
            КРОКОДИЛ — Зубастый житель рек
            ЖИРАФ — Самый высокий
            СОВА — Ухает ночью
            БЕГЕМОТ — Толстокожий и любит воду
        """.trimIndent(),

        "Школа" to """
            РУЧКА — Пишет синим по бумаге
            ТЕТРАДЬ — В ней пишут домашнее задание
            ДОСКА — На ней пишут мелом
            ПОРТФЕЛЬ — В нем носят учебники
            ЗВОНОК — Зовет на урок и с урока
            УРОК — Время, когда учитель объясняет
            ПАРТА — Стол, за которым сидят ученики
            УЧИТЕЛЬ — Ведет урок
            УЧЕНИК — Тот, кто учится
            ПЕРЕМЕНА — Время отдыха между уроками
            ДНЕВНИК — Туда ставят оценки
            ЛИНЕЙКА — С ней чертят прямые линии
            КАРАНДАШ — Им можно стереть написанное
        """.trimIndent(),

        "Еда" to """
            ЯБЛОКО — Фрукт, который упал на Ньютона
            ХЛЕБ — Всему голова
            МОЛОКО — Белый напиток от коровы
            СУП — Первое блюдо
            СЫР — Продукт с дырками
            БАНАН — Желтый фрукт для обезьян
            ПИЦЦА — Круглая лепешка с начинкой
            КАША — Ее варят из крупы
            ЯГОДА — Маленький сочный плод
            ОРЕХ — Твердый плод в скорлупе
            МЕД — Сладкий продукт от пчел
            ЛИМОН — Самый кислый фрукт
            АРБУЗ — Большая полосатая ягода
        """.trimIndent(),

        "Транспорт" to """
            МАШИНА — Едет по дороге на четырех колесах
            САМОЛЕТ — Летает в небе
            КОРАБЛЬ — Плавает по морю
            ПОЕЗД — Едет по рельсам
            ВЕЛОСИПЕД — У него есть педали
            АВТОБУС — Везет пассажиров по городу
            ТРАМВАЙ — Едет по рельсам и звенит
            САМОКАТ — Отталкиваются ногой
            ВЕРТОЛЕТ — У него есть пропеллер сверху
            ЛОДКА — Маленькое судно на веслах
            ГРУЗОВИК — Перевозит тяжелые вещи
        """.trimIndent(),

        "Космос" to """
            КОСМОС — Там летают ракеты и звезды
            ЛУНА — Спутник Земли
            СОЛНЦЕ — Звезда, которая светит днем
            РАКЕТА — Летит в космос
            ЗВЕЗДА — Светится ночью в небе
            ПЛАНЕТА — Земля, Марс или Юпитер
            ЗЕМЛЯ — Наш дом
            МАРС — Красная планета
            КОМЕТА — Летит с длинным хвостом
            ТЕЛЕСКОП — В него смотрят на звезды
            СПУТНИК — Летает вокруг планеты
        """.trimIndent(),

        "Сказки" to """
            БУРАТИНО — Деревянный мальчик с длинным носом
            ЗОЛУШКА — Потеряла хрустальную туфельку
            КОЛОБОК — Убежал от бабушки и дедушки
            РЕПКА — Ее тянули всей семьей
            ТЕРЕМОК — Домик, где жили звери
            РУСАЛОЧКА — Живет в море
            ДЮЙМОВОЧКА — Очень маленькая девочка
            ЧИПОЛЛИНО — Мальчик-луковка
            МАУГЛИ — Мальчик, который вырос в джунглях
            КАРЛСОН — Мужчина в самом расцвете сил
            БЕЛОСНЕЖКА — Жила с семью гномами
        """.trimIndent(),

        "Природа" to """
            ДЕРЕВО — У него есть ствол и ветки
            ЦВЕТОК — Растет из земли и пахнет
            РЕКА — Течет вода
            ГОРА — Очень высокая
            ОБЛАКО — Плывет по небу
            ЛУЖА — Остается после дождя
            ТРАВА — Зеленая и мягкая
            ЛИСТОК — Растет на дереве
            КАМЕНЬ — Твердый и тяжелый
            РАДУГА — Появляется после дождя
            СНЕГ — Падает зимой с неба
            ВЕТЕР — Его не видно, но он дует
        """.trimIndent(),

        "Спорт" to """
            МЯЧ — С ним играют в футбол
            ЛЫЖИ — На них катаются зимой
            КОНЬКИ — На них катаются на льду
            ФУТБОЛ — Игра, где забивают голы ногами
            ТЕННИС — Игра с ракеткой и мячиком
            ПЛАВАНИЕ — Спорт в воде
            БОКС — Спорт с перчатками
            ШАХМАТЫ — Игра с королем и ферзем
            РАКЕТКА — Ею бьют по воланчику
            КАТОК — Там заливают лед
            СТАДИОН — Там проходят соревнования
        """.trimIndent(),

        "Профессии" to """
            ВРАЧ — Лечит людей
            УЧИТЕЛЬ — Учит детей в школе
            ПОЖАРНЫЙ — Тушит пожары
            ПОВАР — Готовит еду
            ПИЛОТ — Управляет самолетом
            ШОФЕР — Водит машину
            МАЛЯР — Красит стены
            АКТЕР — Играет в театре
            ПАРИКМАХЕР — Стрижет людей
            ПОЛИЦЕЙСКИЙ — Ловит преступников
            ПРОДАВЕЦ — Работает в магазине
            СТРОИТЕЛЬ — Строит дома
        """.trimIndent(),

        "Новый год" to """
            ЕЛКА — Главное дерево праздника
            СНЕГ — Белый и пушистый
            ПОДАРОК — Его кладут под елку
            ГИРЛЯНДА — Ею украшают елку
            СНЕЖИНКА — Падает с неба зимой
            САНКИ — На них катаются с горки
            ЗВЕЗДА — Украшает верхушку елки
            ХЛОПУШКА — Громко взрывается
            МАНДАРИН — Оранжевый фрукт праздника
            СВЕЧА — Горит и тает
            КОНФЕТА — Сладкий подарок
        """.trimIndent()
    )

    fun parseInput(input: String): List<CustomWordPair> {
        return input.lines().mapNotNull { line ->
            val parts = line.split("—", "-", ":").map { it.trim() }
            if (parts.size >= 2) {
                val word = parts[0].uppercase().replace("Ё", "Е").filter { it.isLetter() }
                val question = parts[1]
                if (word.length >= 3 && word.length <= 12) {
                    CustomWordPair(word, question)
                } else null
            } else null
        }
    }

    fun generateGrid(words: List<CustomWordPair>, seed: Long): CrosswordGrid {
        val random = Random(seed)
        val placed = mutableListOf<PlacedWord>()
        val cells = mutableMapOf<Pair<Int, Int>, Char>()
        val failedWords = mutableListOf<String>()
        var numCounter = 1

        if (words.isEmpty()) return CrosswordGrid(cells, placed, failedWords)

        // Сортируем по длине (сначала длинные)
        val sortedWords = words.sortedByDescending { it.word.length }

        // Ставим первое слово
        val first = sortedWords.first()
        placed.add(PlacedWord(first.word, "❓", 10, 10, true, numCounter++))
        for (i in first.word.indices) {
            cells[Pair(10 + i, 10)] = first.word[i]
        }

        // Пытаемся пристроить остальные
        for (wordObj in sortedWords.drop(1)) {
            var foundSpot = false
            val placedSnapshot = placed.toList().shuffled(random)
            
            for (pw in placedSnapshot) {
                if (foundSpot) break
                for (i in pw.word.indices) {
                    val char = pw.word[i]
                    val intersectIndex = wordObj.word.indexOf(char)
                    if (intersectIndex != -1) {
                        if (pw.isAcross) {
                            val nx = pw.x + i
                            val ny = pw.y - intersectIndex
                            if (isValidPlacement(cells, wordObj.word, nx, ny, false)) {
                                placed.add(PlacedWord(wordObj.word, "❓", nx, ny, false, numCounter++))
                                for (j in wordObj.word.indices) {
                                    cells[Pair(nx, ny + j)] = wordObj.word[j]
                                }
                                foundSpot = true
                                break
                            }
                        } else {
                            val nx = pw.x - intersectIndex
                            val ny = pw.y + i
                            if (isValidPlacement(cells, wordObj.word, nx, ny, true)) {
                                placed.add(PlacedWord(wordObj.word, "❓", nx, ny, true, numCounter++))
                                for (j in wordObj.word.indices) {
                                    cells[Pair(nx + j, ny)] = wordObj.word[j]
                                }
                                foundSpot = true
                                break
                            }
                        }
                    }
                }
            }
            if (!foundSpot) {
                failedWords.add(wordObj.word)
            }
        }
        return CrosswordGrid(cells, placed, failedWords)
    }

    private fun isValidPlacement(cells: Map<Pair<Int, Int>, Char>, word: String, x: Int, y: Int, isAcross: Boolean): Boolean {
        for (i in word.indices) {
            val cx = if (isAcross) x + i else x
            val cy = if (isAcross) y else y + i
            val existing = cells[Pair(cx, cy)]
            
            if (existing != null && existing != word[i]) return false
            
            if (existing == null) {
                if (isAcross) {
                    if (cells[Pair(cx, cy - 1)] != null || cells[Pair(cx, cy + 1)] != null) return false
                } else {
                    if (cells[Pair(cx - 1, cy)] != null || cells[Pair(cx + 1, cy)] != null) return false
                }
            }
        }
        return true
    }
}
