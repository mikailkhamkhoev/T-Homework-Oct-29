## Запуск Mock Сервера
### Команда
```bash
docker compose -f docker/mockserver/docker-compose.yml up -d
```
### Результат
```bash
[+] Running 2/2
 ✔ Network mockserver_default               Created                                                                        0.0s 
 ✔ Container market-pulse-instruments-mock  Started                                                                        0.2s 
```

## Обычный ответ: 200, 6 инструментов
### Команда
```bash
curl http://localhost:1080/v1/instruments
```
### Результат
```bash
[ {
  "ticker" : "SBER",
  "name" : "Сбер",
  "type" : "STOCK",
  "currency" : "RUB",
  "price" : 250.0,
  "changePercent" : 1.2
}, {
  "ticker" : "GAZP",
  "name" : "Газпром",
  "type" : "STOCK",
  "currency" : "RUB",
  "price" : 120.5,
  "changePercent" : -0.8
}, {
  "ticker" : "LKOH",
  "name" : "Лукойл",
  "type" : "STOCK",
  "currency" : "RUB",
  "price" : 7100.0,
  "changePercent" : 2.3
}, {
  "ticker" : "AAPL",
  "name" : "Apple",
  "type" : "STOCK",
  "currency" : "USD",
  "price" : 225.3,
  "changePercent" : 0.5
}, {
  "ticker" : "OFZ26233",
  "name" : "ОФЗ 26233",
  "type" : "BOND",
  "currency" : "RUB",
  "price" : 780.0,
  "changePercent" : -0.15
}, {
  "ticker" : "FXUS",
  "name" : "FinEx FXUS",
  "type" : "ETF",
  "currency" : "USD",
  "price" : 6.85,
  "changePercent" : 0.9
} ]
```

## Ошибка: 500
### Команда
```bash
curl -i -H "X-Mock-Scenario: error" http://localhost:1080/v1/instruments
```
### Результат
```bash
HTTP/1.1 500 Internal Server Error
connection: keep-alive
content-length: 45

instruments-service: simulated internal error
```

## timeout: 200, пустой массив, задержка 10 с
### Команда
```bash
time -p curl -i -H "X-Mock-Scenario: timeout" http://localhost:1080/v1/instruments
```
### Результат
``` bash
HTTP/1.1 200 OK
Content-Type: application/json
connection: keep-alive
content-length: 3

[ ]real 10.01
user 0.00
sys 0.01
```

## Остановка
### Команда
```bash
docker compose -f docker/mockserver/docker-compose.yml down
```
### Результат
```bash
[+] Running 2/2
 ✔ Container market-pulse-instruments-mock  Removed                                                                        0.5s 
 ✔ Network mockserver_default               Removed                                                                        0.1s 
```