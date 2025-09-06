# BookSwap - Платформа обмена книгами

## Описание проекта
BookSwap - это современная веб-платформа для обмена книгами между пользователями. Платформа позволяет пользователям регистрироваться, добавлять свои книги, искать интересные издания других пользователей и обмениваться ими.

## Технологии
- **Java 17** - основной язык программирования
- **Spring Boot 3.1.0** - основной фреймворк
- **Spring MVC** - веб-фреймворк
- **Spring Data JPA** - работа с базой данных
- **Spring Security** - аутентификация и авторизация
- **PostgreSQL** - база данных
- **Thymeleaf** - шаблонизатор для frontend
- **Bootstrap 5** - CSS фреймворк
- **Docker & Docker Compose** - контейнеризация
- **Swagger/OpenAPI** - документация API
- **OkHttp** - HTTP клиент для внешних API

## Архитектура

### JPA Сущности с связями:
1. **User** - пользователи системы (Spring Security интеграция)
2. **Book** - книги для обмена
3. **Author** - авторы книг (Many-to-Many с Book)
4. **Genre** - жанры книг (Many-to-Many с Book)
5. **BookCondition** - состояние книги (One-to-One с Book)
6. **Review** - отзывы о книгах (Many-to-One с User и Book)
7. **BookExchange** - обмены книгами (Many-to-One с User и Book)

### Внешние API (без готовых библиотек):
- **Google Books API** - получение информации о книгах
- **Currency Exchange API** - курсы валют для международных обменов

## Безопасность
- Защита от CSRF атак
- Защита от XSS атак
- Content Security Policy
- Session Fixation Protection
- Роле-ориентированная авторизация
- Валидация входных данных

## Запуск проекта

### С помощью Docker (рекомендуется):
```bash
docker-compose up -d
```

### Локальный запуск:
1. Установите PostgreSQL и создайте базу данных `bookswap`
2. Настройте подключение в `application.yml`
3. Запустите приложение:
```bash
mvn spring-boot:run
```

## Endpoints

### Web страницы:
- `GET /` - главная страница
- `GET /login` - страница входа
- `GET /register` - страница регистрации
- `GET /books` - каталог книг
- `GET /profile` - профиль пользователя

### REST API:
- `GET /api/books` - получить все книги
- `POST /api/books` - создать новую книгу
- `GET /api/books/{id}` - получить книгу по ID
- `PUT /api/books/{id}` - обновить книгу
- `DELETE /api/books/{id}` - удалить книгу
- `GET /api/books/search` - поиск книг
- `GET /api/external/google-books/{query}` - поиск через Google Books API

### Swagger документация:
- `http://localhost:8080/swagger-ui.html`

## База данных
Проект использует PostgreSQL с автоматическим созданием схемы через JPA.

### Подключение к БД:
- Host: localhost:5432
- Database: bookswap
- Username: bookswap_user
- Password: bookswap_password

## Структура проекта
```
src/
├── main/
│   ├── java/com/bookswap/
│   │   ├── BookSwapApplication.java
│   │   ├── config/
│   │   │   └── SecurityConfig.java
│   │   ├── controller/
│   │   │   ├── HomeController.java
│   │   │   └── api/BookApiController.java
│   │   ├── entity/
│   │   │   ├── User.java
│   │   │   ├── Book.java
│   │   │   ├── Author.java
│   │   │   ├── Genre.java
│   │   │   ├── BookCondition.java
│   │   │   ├── Review.java
│   │   │   └── BookExchange.java
│   │   ├── repository/
│   │   │   ├── UserRepository.java
│   │   │   ├── BookRepository.java
│   │   │   ├── AuthorRepository.java
│   │   │   ├── GenreRepository.java
│   │   │   ├── ReviewRepository.java
│   │   │   └── BookExchangeRepository.java
│   │   └── service/
│   │       ├── UserService.java
│   │       ├── BookService.java
│   │       └── ExternalApiService.java
│   ├── resources/
│   │   ├── application.yml
│   │   ├── templates/
│   │   │   ├── layout.html
│   │   │   └── home.html
│   │   └── static/css/
│   │       └── style.css
```

## Соответствие техническому заданию

✅ **Spring Boot** - использован как основной фреймворк
✅ **Минимум 5 JPA сущностей** - создано 7 сущностей
✅ **Связи M2M/O2M/O2O** - все типы связей реализованы
✅ **Spring Security** - полная интеграция с ролями
✅ **Защита от атак** - CSRF, XSS, Session Fixation
✅ **PostgreSQL** - основная база данных
✅ **REST API** - полный CRUD API
✅ **Swagger документация** - интегрирована
✅ **Внешние API без библиотек** - OkHttp для Google Books и Currency API
✅ **Docker контейнеризация** - docker-compose с PostgreSQL
✅ **CRUD функциональность** - реализована для всех сущностей
✅ **Принципы SOLID** - архитектура следует принципам

## Автор
Семестровая работа по Spring MVC Framework

## Лицензия
MIT License 