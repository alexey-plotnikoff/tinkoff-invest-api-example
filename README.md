# Описание
Участие в конкурсе "Tinkoff Invest Robot Contest".

Документация доступна по [ссылке](https://tinkoff.github.io/investAPI/).

[Репозиторий](https://github.com/Tinkoff/investAPI/) новой API.

Для участия в конкурсе регистрируйтесь по [ссылке](https://meetup.tinkoff.ru/event/tinkoff-invest-robot-contest/).

# Добавление новой стратегии
Приложение спроектировано так, что, можно добавлять неограниченное множество торговых стратегий.
Но в запущенном приложении только **одна** стратегия активна.

Поддержка трейдингом списка ценных бумаг должна заниматься стратегия.

У стратегии обязательно должен быть уникальный код в рамках приложения(для определения активной стратегии).
Для добавления новой стратегии необходимо реализовать интерфейс `TradingStrategy`.

Каждую новую стратегию и все классы, необходимые для её работы, нужно складывать в отдельный подпакет пакета
`ru.plotnikov.tinkoff.invest.strategies`, чтобы в будущем не образовался хаос из классов стратегий и их зависимостей.

# Описание стратегии NoStrategy
Данная стратегия ничего не делает :).

Если вдруг Вам нужно просто запустить приложение без торгов, то используйте эту стратегию.
Например, для того, чтобы посмотреть свой портфель через endpoint'ы приложения или покопаться с песочницей 
через endpoint'ы.

# Описание стратегии IntervalStrategy
Интервальная стратегия.
На заданном промежутке(в днях) при помощи параметра `trading.algorithm.corridor-length`(env-переменная 
`CORRIDOR_LENGTH`) и параметра `trading.algorithm.corridor-percentage`
(env-переменная `CORRIDOR_PERCENTAGE`) строится коридор для торговых значений.

`CORRIDOR_LENGTH` - за сколько последних дней собрать историю свечей от текущего дня(по дефолту 7).
Свечи собираются с интервалом `CANDLE_INTERVAL_1_MIN`.

`CORRIDOR_PERCENTAGE` - какой процент цен должен находиться внутри коридора(по дефолту 80%).
На основании собранных свечей из параметра `CORRIDOR_LENGTH` стратегия ищет мин. и макс. значение цены.
На первом шаге, внутри мин. и макс. значения цены будет 100% цен.
Затем, от макс. значения цены вычитается 1 копейка, а к мин. значению цены прибавляется 1 копейка
(происходит сужение коридора). Если в новом коридоре значений цен всё ещё процент кол-ва цен больше
`CORRIDOR_PERCENTAGE`, то сужение коридора на 1 копейку сверху и снизу повторяется.
Цена определяется по цене закрытия.

При запуске приложения для каждой ценной бумаги нужно определить следующее действие: покупка или продажа.
Если на счету "0" ценных бумаг, то следующее действие - покупка, иначе - продажа.
Кол-во бумаг к покупке/продаже определяется через параметр `trading.quantity.[FIGI-CODE].[QUANTITY]`.

Стратегия каждую минуту запрашивает текущую цену закрытия свечи ценной бумаги и если она "пробивает"
коридор - происходит покупка или продажа.

Один раз в сутки стратегия обновляет коридор, обеспечивая адаптацию к меняющемуся рынку.

Стратегия поддерживается работу со списком ценных бумаг.

# Как проверить гипотезу на песочнице

Для проверки стратегии создан тест `TheoryTest`(он задизаблен, т.к. при тестировании не нужен).
Представьте, что Вы перенеслись в прошлое, например, сегодня 1 февраля 2022 и Вы знаете все свечи на рынке,
которые будут в будущем. Если так, то Вы легко можете проверить работу алгоритма(основанного на свечах, 
как `IntervalStrategy`) на исторических данных.

Что нужно для проверки стратегии:
1. Раздизаблите тест
2. Укажите свой токен к инвестициям любым удобным для Вас способом, например через переменную окружения "TOKEN"
3. Определите день, в который Вы хотите перенестись в прошлое(укажите его в `TheoryTest`)
4. Укажите figi ценных бумаг которые Вы хотите протестировать(и их кол-во)
5. Запустите тест

Что делает тест(**работает только в песочнице**):
1. Создаёт счёт
2. Пополняет счёт на 100 000,00 рублей
3. Переносит Вас в прошлое
4. `IntervalStrategy` инициализируется в прошлом
5. Накидывает все свечи от момента в прошлом до настоящего сегодня(через CANDLE_INTERVAL_1_MIN)
6. Стратегия начинает торговать и выводит рез-ты
7. Закрывает открытый счёт

Пример рез-та для бумаг BBG004731489(Норникель) и BBG0013HGFT4(USD), кол-во равно по 1 и если мы вернулись
в 1 февраля 2022:
```
BUY: [figi = 'BBG004731489', price = '2102600']
SELL: [figi = 'BBG004731489', price = '2225600']
BUY: [figi = 'BBG004731489', price = '2100400']
SELL: [figi = 'BBG004731489', price = '2237800']
BUY: [figi = 'BBG004731489', price = '2100000']
BUY: [figi = 'BBG0013HGFT4', price = '7743']
SELL: [figi = 'BBG0013HGFT4', price = '8050']
BUY: [figi = 'BBG0013HGFT4', price = '7720']
SELL: [figi = 'BBG0013HGFT4', price = '8190']
BUY: [figi = 'BBG0013HGFT4', price = '7726']
Profit: [figi = 'BBG0013HGFT4', total = '777', operations = '[StatOperation{action=BUY, sum=7743}, StatOperation{action=SELL, sum=8050}, StatOperation{action=BUY, sum=7720}, StatOperation{action=SELL, sum=8190}, StatOperation{action=BUY, sum=7726}]']
Profit: [figi = 'BBG004731489', total = '260400', operations = '[StatOperation{action=BUY, sum=2102600}, StatOperation{action=SELL, sum=2225600}, StatOperation{action=BUY, sum=2100400}, StatOperation{action=SELL, sum=2237800}, StatOperation{action=BUY, sum=2100000}]']
````

Как видно, данная стратегия помогла нам заработать:
1. 7,77 рублей по бумаге BBG0013HGFT4
2. 2 604 рублей по бумаге BBG004731489

Из недостатков: пока работает только с бумагами, которые торгуются за рубли.

# Работа с типом "Деньги"
Мой опыт говорит о том, что для работы с деньгами нужно использовать целочисленный тип(int/long) и все операции 
проводить в копейках, что я и сделал в данном приложении. Для конвертации используется класс MoneyUtils.
Да, бывают валюты в которых кол-во "копеек"(называются по своему в каждой стране) достигает не 100, 
а 1000, но в связи с низкой вероятности их использования в торговом роботе, я их не учёл.

# Endpoint'ы
1. Статистика робота
   1. `GET:/stat` - Статистика робота
2. Для работы с песочницей
   1. `GET:/sandbox/create` - Создать счёт в песочнице
   2. `GET:/sandbox/receipt/{sum}` - Пополнить счёт в рублях
   3. `GET:/sandbox/accounts` - Посмотреть список счетов
   4. `GET:/sandbox/portfolio` - Посмотреть портфель
   5. `GET:/sandbox/positions` - Посмотреть позиции
   6. `GET:/sandbox/order-state/{id}` - Посмотреть статус торгового поручения
   7. `GET:/sandbox/orders` - Посмотреть список торговых поручений
3. Для получения информации по ценным бумагам
   1. `GET:/info/all-bonds` - Посмотреть список облигаций
   2. `GET:/info/all-shares` - Посмотреть список акций
   3. `GET:/info/all-currencies` - Посмотреть список валют
   4. `GET:/info/all-etfs` - Посмотреть список фондов
   5. `GET:/info/all-futures` - Посмотреть список фьючерсов
4. Личный портфель
   1. `GET:/private/accounts` - Получить список счетов
   2. `GET:/private/info` - Получить информацию о статусе пользователя

# Настройки IDE
Поскольку робот написан на [Micronaut](https://micronaut.io/), то для запуска в IDE нужно установить галочку
"Enable annotation processing" в настройках. Подробнее [тут](https://docs.micronaut.io/latest/guide/#ideSetup).

Пример для IDEA:
![IDEA, Enable annotation processing](https://docs.micronaut.io/latest/img/intellij-annotation-processors.png)

# Собрать под GraalVM

Не забудьте установить переменные окружения `JAVA_HOME`(17) и `GRAALVM_HOME`.

Собрать нативный образ локально:
```
./gradlew nativeCompile
```

Собрать нативный образ для Docker:
```
./gradlew dockerBuildNative
```

# Статистика по нативному приложению

Время запуска робота ~36ms

![Startup](https://raw.githubusercontent.com/alexey-plotnikoff/tinkoff-invest-api-example/main/doc/graalvm/startup.png)

Потребляемая память после запуска и инициализации свечей для одной бумаги составляет менее 60mb(длина коридора 7 дней)
![Memory](https://raw.githubusercontent.com/alexey-plotnikoff/tinkoff-invest-api-example/main/doc/graalvm/memory.png)

# Как запускать

Укажите следующие переменные окружения:
1. `TOKEN` - String, Ваш токен
2. `SANDBOX` - Boolean, подключаться в режиме песочницы или нет
3. `ACCOUNT_ID` - String, ID счёта на котором хотите запустить стратегию
4. `STRATEGY_TYPE` - String, код стратегии, пока доступно только 2 варианта: `no` и `interval`
5. Выбор бумаг для торговли определяется через параметр `trading.figi`
6. Кол-во бумаг для торговли определяется через параметр `trading.quantity`

# Настройки Telegram-bota
Для подключения telegram-бота - [создайте](https://core.telegram.org/bots#3-how-do-i-create-a-bot) его и укажите 
необходимые данные:
1. username бота - параметр `trading.telegram.username`
2. токен бота - параметр `trading.telegram.access_token`
3. id чата - параметр `trading.telegram.chat_id`