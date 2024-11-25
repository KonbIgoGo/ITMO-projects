# Тесты к курсу «Введение в программирование»

[Условия домашних заданий](https://www.kgeorgiy.info/courses/prog-intro/homeworks.html)

## Домашнее задание 14. Обработка ошибок

Модификации
 * *Base*
    * Класс `ExpressionParser` должен реализовывать интерфейс
        [TripleParser](java/expression/exceptions/TripleParser.java)


## Домашнее задание 13. Разбор выражений

Модификации
 * *Base*
    * Класс `ExpressionParser` должен реализовывать интерфейс
        [TripleParser](java/expression/parser/TripleParser.java)
    * Результат разбора должен реализовывать интерфейс
        [TripleExpression](java/expression/TripleExpression.java)
    * [Исходный код тестов](java/expression/parser/ParserTest.java)
        * Первый аргумент: `easy` или `hard`
        * Последующие аргументы: модификации
 * *Not*
    * Дополнительно реализуйте унарную операцию
      `~` – побитное отрицание, `~-5` равно 4.


## Домашнее задание 12. Выражения

Модификации
 * *Base*
    * Реализуйте интерфейс [Expression](java/expression/Expression.java)
    * [Исходный код тестов](java/expression/ExpressionTest.java)
        * Первый аргумент: `easy` или `hard`.
        * Последующие аргументы: модификации.
 * *Triple*
    * Дополнительно реализуйте поддержку выражений с тремя переменными: `x`, `y` и `z`.
    * Интерфейс/тесты [TripleExpression](java/expression/TripleExpression.java).


## Домашнее задание 11. Игра m,n,k

Тесты не предусмотрены. Решение должно находиться в пакете `game`.

Модификации
 * *Олимпийская система*
    * Добавьте поддержку турниров по 
      [олимпийской системе](https://ru.wikipedia.org/wiki/Олимпийская_система).
    * Стороны в матче выбираются случайно.
    * При ничьей игроки играют до результативной партии.
    * Выбывшие на одном круге делят одно место.


## Домашнее задание 9. Markdown to HTML

Модификации
 * *Базовая*
    * [Исходный код тестов](java/md2html/Md2HtmlTester.java)
    * [Откомпилированные тесты](artifacts/Md2HtmlTest.jar)
        * Аргументы командной строки: модификации
 * *Quote*
    * Добавьте поддержку `''цитат''`: `<q>цитат</q>`


## Домашнее задание 7. Разметка

Модификации
 * *Base*
    * Исходный код тестов:
        * [MarkupTester.java](java/markup/MarkupTester.java)
        * [MarkupTest.java](java/markup/MarkupTest.java)
        * Аргументы командной строки: модификации
    * Откомпилированных тестов не существуют,
      так как они зависят от вашего кода
 * *BBCode*
    * Дополнительно реализуйте метод `toBBCode`, генерирующий [BBCode](https://en.wikipedia.org/wiki/BBCode)-разметку:
      * выделеный текст окружается тегом `[i]`;
      * сильно выделеный текст окружается тегом `[b]`;
      * зачеркнутый текст окружается тегом `[s]`.


## Домашнее задание 6. Подсчет слов++

Модификации
 * *Base*
    * Класс должен иметь имя `Wspp`
    * Исходный код тестов:
        [WsppTest.java](java/wspp/WsppTest.java),
        [WsppTester.java](java/wspp/WsppTester.java)
    * Откомпилированные тесты: [WsppTest.jar](artifacts/WsppTest.jar)
        * Аргументы командной строки: модификации
 * *Position*
    * Вместо номеров вхождений во всем файле надо указывать
      `<номер строки>:<номер в строке>`,
      где номер в строке считается с конца
    * Класс должен иметь имя `WsppPosition`


## Домашнее задание 5. Свой сканнер

Модификации
 * *Base*
    * Исходный код тестов: [FastReverseTest.java](java/reverse/FastReverseTest.java)
    * Откомпилированные тесты: [FastReverseTest.jar](artifacts/FastReverseTest.jar)
        * Аргументы командной строки: модификации
 * *MinRAbc*
    * Вместо каждого числа выведите минимум из чисел, предшествующих
      ему в строки и его самого
    * Во вводе и выводе десятичные числа пишутся буквами:
      нулю соответствует буква `a`, единице – `b` и так далее
    * Класс должен иметь имя `ReverseMinRAbc`


## Домашнее задание 4. Подсчет слов

Модификации
 * *Base*
    * Класс должен иметь имя `WordStatInput`
    * Исходный код тестов:
        [WordStatTest.java](java/wordStat/WordStatTest.java),
        [WordStatTester.java](java/wordStat/WordStatTester.java),
        [WordStatChecker.java](java/wordStat/WordStatChecker.java)
    * Откомпилированные тесты: [WordStatTest.jar](artifacts/WordStatTest.jar)
        * Аргументы командной строки: модификации
 * *CountPrefixL*
    * Выходной файл должен содержать все различные префиксы длины 3
      слов, встречающихся во входном файле, упорядоченые
      по возрастанию числа вхождений, а при равном числе вхождений –
      по порядку первого вхождения во входном файле.
      Слова длины меньшей 3 игнорируются.
    * Класс должен иметь имя `WordStatCountPrefixL`




## Домашнее задание 3. Реверс

Модификации
 * *Base*
    * Исходный код тестов:
        [ReverseTest.java](java/reverse/ReverseTest.java),
        [ReverseTester.java](java/reverse/ReverseTester.java)
    * Откомпилированные тесты: [ReverseTest.jar](artifacts/ReverseTest.jar)
        * Аргументы командной строки: модификации
 * *MinR*
    * Вместо каждого числа выведите минимум из чисел, предшествующих
      ему в строки и его самого
    * Класс должен иметь имя `ReverseMinR`


## Домашнее задание 2. Сумма чисел

Модификация
 * *Double*
    * Входные данные являются 64-битными числами с формате с плавающей точкой
    * Класс должен иметь имя `SumDouble`


Для того, чтобы протестировать программу:

 1. Скачайте откомпилированные тесты ([SumTest.jar](artifacts/SumTest.jar))
 1. Откомпилируйте `Sum.java`
 1. Проверьте, что создался `Sum.class`
 1. В каталоге, в котором находится `Sum.class`, выполните команду
    ```
       java -ea -jar <путь к SumTest.jar> Base
    ```
    * Например, если `SumTest.jar` находится в текущем каталоге, выполните команду
    ```
        java -ea -jar SumTest.jar Base
    ```
 1. Для ускорени отладки рекомендуется сделать скрипт, выполняющий шаги 2−4.

Исходный код тестов:

* [SumTest.java](java/sum/SumTest.java)
* [SumTester.java](java/sum/SumTester.java)
* [Базовые классы](java/base/)
 