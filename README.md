# rest-api-with-cats

Данный сервис представляет из себя справочник автомобилей с возможностью добавления, удаления, вывода списка и отображения статистики по базе.

## Конфигурация

В файле конфигурации [application.conf](/src/main/resources/application.conf) можно указать по какому адресу и на каком порту будет доступен сервер, а также
адрес и учётные данные пользователя БД, которая будет использоваться для хранения данных.

## Описание методов API

| Метод | URL | Описание |
| ----- | ------ | ------ |
| POST | /api/cars | Сохранить в базе автомобиль с переданными в теле запроса параметрами (номер, марка, цвет, год выпуска) в формате JSON |
| DELETE | /api/cars/id | Удалить из базы автомобиль с номером `id` |
| GET | /api/cars | Вывести список всех автомобилей в формате JSON |
| GET | /api/cars/id | Вывести JSON с параметрами автомобиля с номером `id` |
| GET | /api/cars/?paramName1=paramValue1&paramName2=paramValue2&... | Вывести список автомобилей с заданными параметрами  в формате JSON |
| GET | /api/stats | Вывести статистику по базе (кол-во записей, дата и время добавления первой и последней записи) в формате JSON |

## Примеры запросов к API

```
curl -XPOST "http://localhost:8761/api/cars" --header "Content-Type: application/json" --data '{"id": "a05st", "manufacturer": "mazda", "color": "red", "releaseYear" : 2015}'
curl -XPOST "http://localhost:8761/api/cars" --header "Content-Type: application/json" --data '{"id": "b09cd", "manufacturer": "honda", "color": "blue", "releaseYear" : 2005}'
curl -XPOST "http://localhost:8761/api/cars" --header "Content-Type: application/json" --data '{"id": "k34hl", "manufacturer": "lada", "color": "black", "releaseYear" : 2008}'
```
```
curl -XDELETE "http://localhost:8761/api/cars/k34hl"
```
```
curl "http://localhost:8761/api/cars" | json_pp

[
   {
      "color" : "red",
      "id" : "a05st",
      "manufacturer" : "mazda",
      "releaseYear" : "2015"
   },
   {
      "color" : "blue",
      "id" : "b09cd",
      "manufacturer" : "honda",
      "releaseYear" : "2005"
   }
]
```
```
curl "http://localhost:8761/api/cars/a05st" | json_pp

{
   "color" : "red",
   "id" : "a05st",
   "manufacturer" : "mazda",
   "releaseYear" : "2015"
}
```
```
curl "http://localhost:8761/api/cars?color=blue" | json_pp

[
   {
      "color" : "blue",
      "id" : "b09cd",
      "manufacturer" : "honda",
      "releaseYear" : "2005"
   }
]
```
```
curl "http://localhost:8761/api/cars?year=2015&color=red" | json_pp

[
   {
      "color" : "red",
      "id" : "a05st",
      "manufacturer" : "mazda",
      "releaseYear" : "2015"
   }
]
```
## Описание использованных инструментов

Приложение написано практически полностью (везде, где это возможно) в функциональном стиле. Для сохранения ссылочной прозрачности и контроля побочных эффектов используется
монада `IO` из библиотеки `cats-effect`, однако она фиксируется только на финальном этапе запуска, большая часть кода полиморфна относительно типа эффекта и при необходимости
его можно изменить. Общение сервера с клиентом организовано инструментами библиотеки `http4s`, работа с форматом JSON, кодирование и декодирование объектов, реализовано при помощи
библиотеки `circe`, а генерация и отправка SQL-команд к базе данных производится с использованием библиотеки `doobie`.

## Архитектура прилолжения

Объект `ConfigLoader` отвечает за декодирование содержимого файла конфигурациию.
С использованием полученных конфигураций, метод `transactor` объекта `Database` создаёт ресурс, который в будущем будет использоваться для коммуникации с БД,
а метод `initialize` создаёт необходимые таблицы в случае их отсуствия. Объекты `StatRepository` и `CarRepository`
служат для генерации SQL-команд к базе данных, результаты которых возрващаются "обёрнутыми" в специальный тип `ConnectionIO`.
Внутри `StatRoutes` и `CarRoutes` реализована непосредственно логика обработки запросов, то есть их декодирования и генерации ответов.
Наконец, внутри объекта `Main` совмещаются обработчики запросов `StatRoutes` и `CarRoutes`, затем создаётся и запускается сервер.
