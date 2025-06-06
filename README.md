#  <span style="color: #674ea7">**Explore-With-Me**</span>  
A microservice application that lets users share events and find company to join in activities.

------------------------------------------------------------------------------------------


## Feature "Comments"

### <a href="https://github.com/AGAYAN/java-explore-with-me-plus/pull/54">Pull Request Link</a>

 - Функциональность комментариев предоставляет пользователям возможность оставлять отзывы, 
делиться мыслями или задавать вопросы о событиях, создавая интерактивную среду в приложении.  
Функциональность включает настройки прав доступа для создания, просмотра, редактирования и модерации комментариев.  


 - Структура базы данных:  
В базе данных создана таблица `comments`, содержащая комментарии с привязкой к событиям и пользователям.
Каждый комментарий связан с событием, к которому он относится, и пользователем, его написавшим.


 - Функциональность комментариев доступна через набор эндпоинтов, разделенных на `Private`, `Admin` и `Public` APIs,
что позволяет предоставлять разные права доступа на основе ролей пользователей.
------------------------------------------------------------------------------------------


### PRIVATE API
<details>
<summary><code><span style="color: #0b8721">POST</span></code> <code><b>/users/{userId}/comments?eventId={eventId}</b></code> <code>Добавление нового комментария от текущего пользователя под событием`</code></summary>

##### Notes
 - Нельзя комментировать не опубликованное событие (Ожидается код ошибки 409)
 - Инициатор может оставлять комментарии, как интерактивную реакцию на сообщения от пользователей
 - Комментарий от инициатора помечается флажком `isInitiator = true`
##### Parameters  
>| name      | type             | data type | description                                            |
>|-----------|------------------|-----------|--------------------------------------------------------|
>| `userId`  | required (path)  | int       | ИД текущего пользователя который оставляет комментарий |
>| `eventId` | required (query) | int       | ИД события к кторому оставляется комментарий           |
##### Request body [^1]
>| name       | type     | data type | constraints                           |
>|------------|----------|-----------|---------------------------------------|
>|  content   | required | String    | maxLength = 5000, min length = 1      |
##### Responses
> | http code | reason                        |
> |-----------|-------------------------------|
> | `201`     | Коммент сохранен    + DTO     |
> | `400`     | Запрос составлен не корректно |
> | `404`     | Событие не найдено            |
> | `409`     | Событие не опубликовано       |
</details>

<details>
<summary><code><span style="color: #1ca885">PATCH</span></code> <code><b>/users/{userId}/comments/{commentId}</b></code> <code>Изменение текста своего комментария</code></summary>   

##### Notes
- Изменить текст комментария может только тот кто оставлял коммент (Ожидается код ошибки 409)
##### Parameters
>| name        | type            | data type | description                                  |
>|-------------|-----------------|-----------|----------------------------------------------|
>| `userId`    | required (path) | int       | ИД текущего владельца комментария            |
>| `commentId` | required (path) | int       | ИД комментария, в которое вносятся изменения |
##### Request body [^1]
>| name        | type     | data type | constraints                           |
>|-------------|----------|-----------|---------------------------------------|
>|   content   | required | String    | maxLength = 5000, min length = 1      |

##### Responses
> | http code | reason                                                  |
> |-----------|---------------------------------------------------------|
> | `200`     | Коммент обновлен   + DTO                                |
> | `400`     | Запрос составлен не корректно                           |
> | `404`     | Комментарий не найден                                   |
> | `409`     | Попытка изменения комментария посторонним пользователем |
</details>

<details>
<summary><code><span style="color: #c04239">DELETE</span></code> <code><b>/users/{userId}/comments/{commentId}</b></code> <code>Удаление своего комментария текущим пользователем</code></summary>   

##### Notes
- Удалить можно только собственный комментарий (Ожидается код ошибки 409)
##### Parameters
>| name        | type            | data type | description                       |
>|-------------|-----------------|-----------|-----------------------------------|
>| `userId`    | required (path) | int       | ИД текущего владельца комментария |
>| `commentId` | required (path) | int       | ИД комментария, которое удаляется |

##### Responses
> | http code | reason                                                 |
> |-----------|--------------------------------------------------------|
> | `204`     | Коммент удален                                         |
> | `400`     | Запрос составлен не корректно                          |
> | `404`     | Комментарий не найден                                  |
> | `409`     | Попытка удаления комментария посторонним пользователем |
</details>

<details>
<summary><code><span style="color: #1773c7">GET</span></code> <code><b>/users/{usersId}/comments</b></code> <code>Получение всех комментариев текущего пользователя</code></summary>   

##### Notes
- В случае, если не найдено ни одного комментария, возвращает пустой список
##### Parameters
>| name     | type                 | data type | description                                                                     |
>|----------|----------------------|-----------|---------------------------------------------------------------------------------|
>| `userId` | required (path)      | int       | ИД текущего владельца комментария                                               |

##### Responses
> | http code | reason                      |
> |-----------|-----------------------------|
> | `200`     | Успех,  список комментариев |
> | `404`     | Пользователь не найден      |
</details>

------------------------------------------------------------------------------------------

### ADMIN API
<details>
<summary><code><span style="color: #c04239">DELETE</span></code> <code><b>/admin/comments/{commentId}</b></code> <code>Удаление комментария админом</code></summary>   
 
##### Parameters
>| name        | type            | data type | description                       |
>|-------------|-----------------|-----------|-----------------------------------|
>| `commentId` | required (path) | int       | ИД комментария, которое удаляется |

##### Responses
> | http code | reason                                                 |
> |-----------|--------------------------------------------------------|
> | `204`     | Коммент удален                                         |
> | `400`     | Запрос составлен не корректно                          |
> | `404`     | Комментарий не найден                                  |
</details>

<details>
<summary><code><span style="color: #1773c7">GET</span></code> <code><b>/admin/comments</b></code> <code>Получение всех комментариев к определенному событию</code></summary>

##### Notes
- В случае, если не найдено ни одного комментария, возвращает пустой список
##### Parameters
>| name       | type                 | data type | description                                                                     |
>|------------|----------------------|-----------|---------------------------------------------------------------------------------|
>| `eventId`  | required (query)     | int       | ИД события комментарии которого выводятся                                       |
>| `from`     | default = 0 (query)  | int       | количество элементов, которые нужно пропустить для формирования текущего набора |
>| `size`     | default = 10 (query) | int       | количество элементов в наборе                                                   |

##### Responses
> | http code | reason                                                 |
> |-----------|--------------------------------------------------------|
> | `200`     | Успех,  список комментариев                            |
> | `400`     | Запрос составлен не корректно                          |
</details>

------------------------------------------------------------------------------------------

### PUBLIC API

<details>
<summary><code><span style="color: #1773c7">GET</span></code> <code><b>/events/{eventId}/comments</b></code> <code>Получение всех комментариев к определенному событию</code></summary>   

##### Notes
- В случае, если не найдено ни одного комментария, возвращает пустой список 
##### Parameters
>| name       | type                 | data type | description                                                                     |
>|------------|----------------------|-----------|---------------------------------------------------------------------------------|
>| `eventId`  | required (path)      | int       | ИД события комментарии которого выводятся                                       |
>| `from`     | default = 0 (query)  | int       | количество элементов, которые нужно пропустить для формирования текущего набора |
>| `size`     | default = 10 (query) | int       | количество элементов в наборе                                                   |

##### Responses
> | http code | reason                                                 |
> |-----------|--------------------------------------------------------|
> | `200`     | Успех,  список комментариев                            |
> | `400`     | Запрос составлен не корректно                          |
</details>

------------------------------------------------------------------------------------------
[^1]: Required