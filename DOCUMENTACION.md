# Documentación de GeArt_2.0

## ¿Qué es GeArt_2.0?

Imagina que eres un **artista** que dibuja increíblemente bien, o un **cliente** que necesita un dibujo especial (como un retrato de tu personaje favorito). GeArt_2.0 es una aplicación de teléfono (Android) que junta a estos dos tipos de personas.

- Los **clientes** publican: "Quiero un dibujo de mi perro, pago $20"
- Los **artistas** miran la lista y dicen: "Yo lo hago"
- Luego **chatean** para ponerse de acuerdo, el artista muestra avances, entrega el dibujo final, y el cliente pone una **calificación** (como 5 estrellas).

Todo se guarda en **Firebase**, que es como una computadora gigante en internet que nunca se apaga.

---

## ¿Cómo está organizado el proyecto?

Dentro de la carpeta `GeArt_20/` hay muchas carpetas y archivos. Cada uno tiene un trabajo específico.

```
GeArt_20/
├── build.gradle.kts              # La "receta" principal del proyecto
├── settings.gradle.kts           # Dice qué partes (módulos) existen
├── gradle.properties             # Configuraciones especiales de Gradle
├── gradlew / gradlew.bat         # Programas para construir la app
├── gradle/
│   ├── libs.versions.toml        # Lista de versiones de herramientas
│   └── wrapper/                  # Cosas internas de Gradle
└── app/                          # La app en sí misma
    ├── build.gradle.kts          # Receta del módulo "app"
    ├── google-services.json      # Conexión a Firebase
    ├── proguard-rules.pro        # Reglas para hacer la app más pequeña
    └── src/
        ├── androidTest/          # Pruebas en un teléfono de verdad
        ├── test/                 # Pruebas en la computadora
        └── main/
            ├── AndroidManifest.xml   # "Cédula de identidad" de la app
            ├── res/                  # Recursos (dibujos, colores, textos)
            └── java/com/example/geart_20/
                ├── model/            # Los "molde" de los datos
                ├── repository/       # Los que hablan con Firebase
                ├── viewmodel/        # Los que organizan la lógica
                └── ui/               # Los que dibujan listas en pantalla
```

---

## Los archivos Kotlin (el cerebro de la app)

Cada archivo `.kt` tiene código Kotlin. Kotlin es el idioma en que está escrita la app.

---

### 3.1 Paquete `model/` — Los moldes de los datos

> **¿Qué es un "modelo"?** Imagina que quieres guardar información de una persona. Necesitas saber: su nombre, su edad, su color favorito. El modelo es como un **formulario** que dice "todo artista TIENE que tener nombre, calificación, etc.". Así cuando guardas o lees datos, siempre sabes qué esperar.

---

#### `User.kt` — El formulario de un usuario

Archivo: `app/src/main/java/com/example/geart_20/model/User.kt`

**Sirve para:** Guardar quién es cada persona que usa la app. Ya sea cliente o artista.

Campos que tiene este formulario:

| Campo | ¿Qué guarda? | Ejemplo |
|-------|-------------|---------|
| `id` | Un número especial único para cada persona | `"abc123..."` |
| `name` | El nombre del usuario | `"Ana"` |
| `email` | El correo electrónico | `"ana@correo.com"` |
| `role` | ¿Es cliente o artista? | `"CLIENT"` o `"ARTIST"` |
| `profileImageUrl` | El link de su foto de perfil | `"https://fotos.com/ana.jpg"` |
| `bio` | Una descripción corta de ellos | `"Dibujo desde los 5 años"` |
| `socialLinks` | Link a sus redes sociales | `"https://instagram.com/ana"` |
| `commissionTableUrl` | Link a su tabla de precios (solo artistas) | `"https://imgur.com/precios.jpg"` |
| `rating` | Su calificación promedio (de 0 a 5) | `4.5` |
| `ratingCount` | Cuántas personas lo han calificado | `12` |

**¿Cómo se usa?** Cuando alguien se registra, se llena este formulario y se guarda en Firebase en la sección `users/{id}`.

---

#### `Commission.kt` — El formulario de un pedido de dibujo

Archivo: `app/src/main/java/com/example/geart_20/model/Commission.kt`

**Sirve para:** Guardar la información de un pedido: qué quiere el cliente, cuánto paga, en qué estado está.

Campos:

| Campo | ¿Qué guarda? | Ejemplo |
|-------|-------------|---------|
| `id` | Número único del pedido | `"-O5fG3..."` |
| `clientId` | ID del cliente que pide | `"abc123"` |
| `artistId` | ID del artista que hará el dibujo | `"def456"` |
| `title` | Título del pedido | `"Retrato de mi gato"` |
| `description` | Descripción detallada | `"Mi gato es naranja, con ojos verdes"` |
| `status` | En qué etapa está | `"PENDING"` (pendiente), `"DIRECT_REQUEST"` (directa), `"ACCEPTED"` (aceptada), `"COMPLETED"` (completada) |
| `price` | El precio en dólares | `25.00` |
| `referenceImageUrl` | Link de imagen de referencia | `"https://imgur.com/gato.jpg"` |
| `isRated` | Si ya calificaron al artista | `true` o `false` |
| `finalProductUrl` | Link del dibujo final entregado | `"https://imgur.com/resultado.jpg"` |

**¿Cómo se usa?** Un cliente llena un formulario en la pantalla "Solicitar Comisión" y se guarda en Firebase en `commissions/{id}`.

---

#### `Comment.kt` — El formulario de un comentario

Archivo: `app/src/main/java/com/example/geart_20/model/Comment.kt`

**Sirve para:** Guardar un comentario que alguien escribe en el perfil de otro usuario.

Campos:

| Campo | ¿Qué guarda? | Ejemplo |
|-------|-------------|---------|
| `id` | Número único del comentario | `"-X7hK2..."` |
| `fromUserId` | ID de la persona que comenta | `"abc123"` |
| `fromUserName` | Nombre de quien comenta | `"Pedro"` |
| `text` | El texto del comentario | `"Excelente artista, muy recomendado"` |
| `timestamp` | La hora exacta en que se escribió | `1234567890` (milisegundos desde 1970) |

**¿Cómo se usa?** En el perfil de un usuario, hay una sección de comentarios. Cada vez que alguien escribe uno, se guarda en `comments/{userIdDelPerfil}/{commentId}`.

---

#### `ChatMessage.kt` — El formulario de un mensaje del chat

Archivo: `app/src/main/java/com/example/geart_20/model/ChatMessage.kt`

**Sirve para:** Guardar cada mensaje que se envían el cliente y el artista en el chat de una comisión.

Campos:

| Campo | ¿Qué guarda? | Ejemplo |
|-------|-------------|---------|
| `id` | Número único del mensaje | `"-AbC12..."` |
| `senderId` | ID de quien envió el mensaje | `"abc123"` |
| `message` | El contenido: texto, link de imagen, o precio | `"Hola, aquí va el avance"` |
| `type` | El tipo de mensaje | `"TEXT"` (texto), `"PROGRESS_IMAGE"` (avance), `"FINAL_PRODUCT"` (final), `"PRICE_UPDATE"` (cambio de precio) |
| `timestamp` | La hora exacta del mensaje | `1234567890` |

**¿Cómo se usa?** Cuando alguien escribe en el chat, se crea uno de estos y se guarda en `chats/{commissionId}/{mensajeId}`.

---

### 3.2 Paquete `repository/` — Los que hablan con Firebase

> **¿Qué es un "repositorio"?** Es como un mensajero. Cuando la app necesita guardar algo en Firebase o leer algo de Firebase, le pide al repositorio que lo haga. Así el resto de la app no tiene que saber cómo funciona Firebase.

---

#### `AuthRepository.kt` — El mensajero de la entrada (login/registro)

Archivo: `app/src/main/java/com/example/geart_20/repository/AuthRepository.kt`

**Sirve para:** Registrar nuevos usuarios e iniciar sesión.

Conexiones que usa:
- `FirebaseAuth.getInstance()` — el sistema de autenticación de Firebase
- `FirebaseDatabase.getInstance().getReference("users")` — la sección donde se guardan los usuarios

**Función: `registerUser(email, pass, name, role)`**
- **¿Qué hace?** Crea una cuenta nueva en Firebase con email y contraseña. Luego crea un formulario `User` con los datos y lo guarda en `users/{uid}`.
- **¿Qué recibe?** `email` (correo), `pass` (contraseña), `name` (nombre), `role` (rol: CLIENT o ARTIST).
- **¿Qué devuelve?** `true` si todo salió bien, `false` si hubo error (ej: el correo ya existe).

**Función: `signIn(email, pass)`**
- **¿Qué hace?** Inicia sesión con el correo y contraseña.
- **¿Qué recibe?** `email` y `pass`.
- **¿Qué devuelve?** `true` si el usuario existe y la contraseña es correcta, `false` si no.

---

#### `UserRepository.kt` — El mensajero de los datos de usuario

Archivo: `app/src/main/java/com/example/geart_20/repository/UserRepository.kt`

**Sirve para:** Guardar y leer información de los usuarios.

**Función: `saveUser(user)`**
- **¿Qué hace?** Guarda todos los datos de un usuario (su formulario completo) en Firebase en `users/{user.id}`.
- **¿Qué recibe?** Un objeto `User` con todos los campos llenos.
- **¿Qué devuelve?** Nada (pero espera a que termine).

**Función: `getUserRole(uid)`**
- **¿Qué hace?** Va a Firebase, busca al usuario con ese `uid`, y mira qué rol tiene.
- **¿Qué recibe?** `uid` (el ID del usuario).
- **¿Qué devuelve?** El rol como texto (`"CLIENT"` o `"ARTIST"`), o `null` si no lo encuentra.

---

### 3.3 Paquete `viewmodel/` — Los organizadores

> **¿Qué es un "ViewModel"?** Es como un asistente personal de una pantalla. La pantalla le dice "quiero registrar a este usuario", y el ViewModel se encarga de llamar al mensajero (repository) y devolverle el resultado.

---

#### `LoginViewModel.kt` — El asistente de la pantalla de inicio

Archivo: `app/src/main/java/com/example/geart_20/viewmodel/LoginViewModel.kt`

**Sirve para:** Ayudar a la pantalla de Login/Registro a hacer su trabajo.

**Función: `handleLogin(email, pass, name, role)`**
- Llama a `AuthRepository.registerUser()`.
- Devuelve `true` o `false`.

**Función: `handleSignIn(email, pass)`**
- Llama a `AuthRepository.signIn()`.
- Devuelve `true` o `false`.

---

### 3.4 Paquete `ui/` — Los que dibujan listas

> **¿Qué es un "adaptador"?** Cuando tienes una lista de cosas (como una lista de artistas), el adaptador le dice a la pantalla cómo dibujar cada elemento de la lista. Es como un instructor: "El primer artista va aquí, el segundo aquí...".

---

#### `ArtistAdapter.kt` — El instructor de la lista de artistas

Archivo: `app/src/main/java/com/example/geart_20/ui/ArtistAdapter.kt`

**Sirve para:** Mostrar una lista de artistas en la pantalla "Explorar", cada uno con su foto, nombre, calificación y un botón "Ver".

**Clase interna: `ArtistViewHolder`** — El "asiento" que guarda los dibujos de cada artista:
- `ivPic` — la foto de perfil
- `tvName` — el nombre
- `tvRating` — la calificación con estrellas
- `btnView` — el botón "Ver" para abrir su perfil

**Función: `onCreateViewHolder(parent, viewType)`**
- **¿Qué hace?** Crea un "asiento" vacío para un artista. Infla el archivo `item_artist.xml` (el molde visual de un artista en la lista).

**Función: `onBindViewHolder(holder, position)`**
- **¿Qué hace?** Llena el "asiento" con los datos del artista en la posición `position` de la lista.
- Si el artista no tiene nombre, pone "Artista Anónimo".
- Muestra la calificación con 1 decimal y el número de reseñas.
- Si tiene foto de perfil, la carga con Glide (una herramienta para mostrar imágenes de internet) y la hace circular.
- El botón "Ver" abre `ProfileActivity` con el ID del artista.

**Función: `getItemCount()`**
- **¿Qué hace?** Dice cuántos artistas hay en la lista.

**Función: `updateList(newList)`**
- **¿Qué hace?** Cambia la lista completa de artistas (por ejemplo, cuando el usuario busca por nombre) y redibuja todo.

---

#### `CommissionAdapter.kt` — El instructor de la lista de comisiones

Archivo: `app/src/main/java/com/example/geart_20/ui/CommissionAdapter.kt`

**Sirve para:** Mostrar una lista de pedidos (comisiones). Cada tarjeta muestra título, descripción, precio y estado.

**Clase interna: `CommissionViewHolder`** — El asiento:
- `tvTitle` — título
- `tvDesc` — descripción
- `tvPrice` — precio
- `tvStatus` — estado

**Función: `onCreateViewHolder(parent, viewType)`**
- Crea un asiento usando `item_commission.xml`.

**Función: `onBindViewHolder(holder, position)`**
- Llena el asiento con los datos de una comisión.
- Si el título está vacío, pone "Sin título".
- Muestra el precio con `$` adelante.
- Al hacer clic en la tarjeta, ejecuta `onItemClick` (una función especial que la pantalla le pasa para hacer algo cuando tocas una comisión).

**Función: `getItemCount()`**
- Dice cuántas comisiones hay.

---

#### `ChatAdapter.kt` — El instructor de los mensajes del chat

Archivo: `app/src/main/java/com/example/geart_20/ui/ChatAdapter.kt`

**Sirve para:** Mostrar los mensajes del chat entre cliente y artista. Cada mensaje puede ser de texto, imagen de avance, producto final, o propuesta de precio.

Cuándo se crea recibe:
- `currentUserId` — quién está usando la app ahora
- `isClient` — si el usuario es el cliente (para mostrarle botón de aceptar precio)
- `messages` — la lista de mensajes
- `onAcceptPriceClick` — qué hacer cuando el cliente acepta un precio

**Clase interna: `ChatViewHolder`**:
- `llMessageRoot` — el contenedor principal
- `llBubble` — la burbuja de color
- `tvMessageText` — texto del mensaje
- `ivMessageImage` — imagen (avance o final)
- `tvImageLabel` — etiqueta de la imagen ("Avance de la obra" o "Producto Final Entregado")
- `llPriceUpdate` — contenedor de propuesta de precio
- `tvPriceText` — texto con el nuevo precio
- `btnAcceptPrice` — botón "Aceptar Precio"
- `tvTimestamp` — la hora del mensaje

**Función: `onCreateViewHolder(parent, viewType)`**
- Crea un asiento usando `item_chat_message.xml`.

**Función: `onBindViewHolder(holder, position)`**
Hace varias cosas importantes:

1. **Alineación**: Si el mensaje es del usuario actual, lo pone a la derecha con burbuja azul. Si es de otro, a la izquierda con burbuja gris.

2. **Hora**: Muestra la hora en formato HH:mm (ej: 14:30).

3. **Tipo de mensaje** (la parte más importante):
   - `"TEXT"` → muestra el texto.
   - `"PROGRESS_IMAGE"` → muestra una imagen con la etiqueta "Avance de la obra".
   - `"FINAL_PRODUCT"` → muestra una imagen con etiqueta dorada "Producto Final Entregado".
   - `"PRICE_UPDATE"` → muestra el precio propuesto y, si el usuario es el cliente y el mensaje es del artista, le muestra un botón verde "Aceptar Precio". Al hacer clic, ejecuta `onAcceptPriceClick`.

**Función: `getItemCount()`**
- Dice cuántos mensajes hay.

**Función: `updateMessages(newMessages)`**
- Cambia la lista de mensajes y redibuja todo (útil cuando llegan mensajes nuevos en tiempo real).

---

#### `CommentAdapter.kt` — El instructor de los comentarios

Archivo: `app/src/main/java/com/example/geart_20/ui/CommentAdapter.kt`

**Sirve para:** Mostrar una lista de comentarios en el perfil de un usuario.

**Clase interna: `CommentViewHolder`**:
- `tvAuthor` — nombre de quien escribió el comentario
- `tvText` — el texto del comentario

**Función: `onCreateViewHolder(parent, viewType)`**
- Crea asiento con `item_comment.xml`.

**Función: `onBindViewHolder(holder, position)`**
- Pone el nombre del autor y el texto.

**Función: `getItemCount()`**
- Dice cuántos comentarios hay.

---

### 3.5 Las Activities (Las pantallas)

> **¿Qué es una "Activity"?** Es una pantalla completa de la app. Como cuando abres Facebook y ves tu muro, eso es una Activity. Cada pantalla de GeArt_2.0 es una Activity.

---

#### `LoginActivity.kt` — La pantalla de entrada

Archivo: `app/src/main/java/com/example/geart_20/LoginActivity.kt`

**Sirve para:** Que el usuario se registre o inicie sesión.

**¿Qué hace cuando se abre (`onCreate`)?**

1. **Revisa si ya hay alguien con sesión**: Si el usuario ya inició sesión antes (Firebase recuerda), salta directamente a `MainActivity` sin mostrar la pantalla de login.

2. **Muestra el formulario** (`activity_login.xml`):
   - Un campo para escribir el **email**.
   - Un campo para escribir la **contraseña** (los puntitos negros la ocultan).
   - Dos botones de opción: "Cliente" o "Artista".
   - Botón "Registrarse" (deshabilitado hasta que elijas un rol).
   - Botón "Ingresar".

3. **Botón "Registrarse"**:
   - Toma el email, contraseña, nombre ("Usuario Nuevo") y rol.
   - Llama a `viewModel.handleLogin()`.
   - Si funciona, va a `MainActivity`.
   - Si no, muestra un mensaje como "El correo ya existe" o "Contraseña muy corta".

4. **Botón "Ingresar"**:
   - Toma email y contraseña.
   - Llama a `viewModel.handleSignIn()`.
   - Si funciona, va a `MainActivity`.
   - Si no, muestra "Credenciales incorrectas".

**Función: `irAMain()`**
- Abre `MainActivity` y cierra `LoginActivity` (para que no puedas volver atrás con el botón).

---

#### `MainActivity.kt` — El centro de control

Archivo: `app/src/main/java/com/example/geart_20/MainActivity.kt`

**Sirve para:** Es la pantalla más importante. Dependiendo de si eres artista o cliente, muestra cosas diferentes.

**¿Qué hace cuando se abre (`onCreate`)?**

1. Verifica que el usuario esté autenticado. Si no, envía al login.

2. Descarga los datos del usuario desde Firebase (`users/{uid}`).

3. Pregunta: ¿El usuario es `"ARTIST"` o `"CLIENT"`?
   - Si es **ARTISTA** → llama a `setupArtistPanel()`
   - Si es **CLIENTE** → llama a `setupClientPanel()`

---

#### Panel del Artista (`setupArtistPanel`)

Muestra la pantalla `activity_main_artist.xml` que tiene:

- **Título**: "Mercado de Comisiones - {nombre del artista}"
- **3 botones de filtro** para organizar las comisiones:
  - "Mercado" (azul) → Muestra comisiones PENDIENTES (sin artista asignado)
  - "Directas" (verde) → Muestra SOLICITUDES DIRECTAS para este artista
  - "En Progreso" (morado) → Muestra comisiones ACEPTADAS o COMPLETADAS de este artista
- **Lista de comisiones** (RecyclerView) que se actualiza sola en tiempo real
- **Botón "Mi Perfil"** → abre `ProfileActivity`
- **Botón "Cerrar Sesión"** → cierra sesión y vuelve al login

**Función: `updateDisplayedList()`** (dentro de `setupArtistPanel`)
- Esta función es como un colador: toma todas las comisiones y solo deja pasar las que coinciden con el filtro activo.

**Función: `acceptCommissionDialog(commission)`**
- Muestra una ventana que pregunta: "¿Quieres aceptar este trabajo por $X?"
- Si el artista dice que sí, cambia el estado a `"ACCEPTED"` y asigna al artista.

---

#### Panel del Cliente (`setupClientPanel`)

Muestra la pantalla `activity_main_client.xml` que tiene:

- **Botón "Solicitar Nueva Comisión"** → abre `CreateCommissionActivity`
- **Botón "Explorar Directorio de Artistas"** → abre `ExploreActivity`
- **Lista de sus comisiones** (solo las que este cliente pidió)
- **Botón "Mi Perfil"** → abre `ProfileActivity`
- **Botón "Cerrar Sesión"**

---

#### Función importante: `showCommissionDetailsDialog(commission)`

Cuando tocas una comisión en la lista, se abre esta ventana de detalles. Muestra:

- **Título** de la comisión
- **Descripción**
- **Precio** en verde
- **Estado** traducido al español:
  - `"PENDING"` → "Pendiente de un artista"
  - `"DIRECT_REQUEST"` → "Solicitud Directa (Privada)"
  - `"ACCEPTED"` → "En progreso"
  - `"COMPLETED"` → "Obra finalizada"
- **Perfil del cliente** (clickeable, te lleva a su perfil)
- **Perfil del artista** (si ya tiene uno asignado)
- **Imagen de referencia** (si el cliente subió una)

**Botones dinámicos** (cambian según quién mira y el estado):

| Situación | Botón |
|-----------|-------|
| Artista ve una PENDIENTE | "Aceptar Trabajo" |
| Artista ve una SOLICITUD DIRECTA | "Aceptar Solicitud" o "Rechazar" |
| Artista ve una ACEPTADA suya | "Abrir Chat" |
| Cliente ve una ACEPTADA suya | "Abrir Chat" |
| Cliente ve una COMPLETADA sin calificar | "Calificar Artista" |
| Cliente ve una COMPLETADA ya calificada | "Ya calificado" (deshabilitado) |

---

#### Función: `showRatingDialog(commission)`

Muestra 5 estrellitas para que el cliente califique al artista. Al enviar, llama a `updateArtistRating()`.

**Seguridad**: Si la comisión ya fue calificada, no deja hacerlo otra vez.

---

#### Función: `updateArtistRating(commission, newRating)`

Esta función es muy especial. Usa una **transacción** de Firebase. Una transacción es como: "ve al artista, lee su calificación actual, calcula la nueva, y guárdala, todo en un solo paso sin que nadie interrumpa".

La fórmula matemática que usa:
```
nuevoPromedio = (promedioViejo × cantidadVieja + nuevaNota) / (cantidadVieja + 1)
```

Después de actualizar la calificación, marca `isRated = true` en la comisión.

---

#### `ExploreActivity.kt` — La galería de artistas

Archivo: `app/src/main/java/com/example/geart_20/ExploreActivity.kt`

**Sirve para:** Que los clientes vean todos los artistas registrados y puedan buscar por nombre.

**¿Qué hace?**

1. Muestra la pantalla `activity_explore.xml` con:
   - Título "Directorio de Artistas"
   - Una barra de búsqueda con lupa
   - Una lista de artistas

2. Descarga TODOS los usuarios cuyo rol es `"ARTIST"` desde Firebase.

3. Los ordena de mayor a menor calificación (los mejores primero).

4. **Búsqueda en tiempo real**: Mientras escribes en la barra de búsqueda, la lista se filtra solita. Por ejemplo, si escribes "Ana", solo se ven los artistas que tienen "Ana" en su nombre.

---

#### `CreateCommissionActivity.kt` — El formulario de pedido

Archivo: `app/src/main/java/com/example/geart_20/CreateCommissionActivity.kt`

**Sirve para:** Que un cliente publique una nueva comisión.

**¿Qué hace?**

1. Muestra la pantalla `activity_create_commission.xml` con:
   - **Título** de la comisión (ej: "Retrato de mi OC")
   - **Descripción** detallada (ej: "Personaje original, cuerpo entero, estilo anime")
   - **Precio** sugerido (en dólares)
   - **Link de imagen de referencia** (opcional, ej: una foto de referencia)
   - Botón "Publicar Solicitud"

2. Cuando el cliente publica:
   - Valida que título, descripción y precio no estén vacíos.
   - Decide el estado inicial:
     - Si viene del perfil de un artista (con extra `TARGET_ARTIST_ID`), el estado es `"DIRECT_REQUEST"` (solicitud directa a ese artista).
     - Si no, el estado es `"PENDING"` (pública, cualquier artista puede verla).
   - Guarda la comisión en Firebase en `commissions/{pushKey}`.
   - Muestra "Comisión publicada con éxito" y cierra la pantalla.

---

#### `ChatActivity.kt` — El chat con el artista

Archivo: `app/src/main/java/com/example/geart_20/ChatActivity.kt`

**Sirve para:** Que el cliente y el artista hablen sobre una comisión.

**¿Qué hace cuando se abre (`onCreate`)?**

Recibe del Intent:
- `COMMISSION_ID` — de qué comisión es el chat
- `COMMISSION_TITLE` — el título para mostrar arriba
- `IS_CLIENT` — si el usuario es el cliente (para ocultar botones especiales)

**Si el usuario es el CLIENTE**: Oculta los botones de "Precio", "Progreso" y "Final" (esos solo los usa el artista).

**Función: `setupRecyclerView()`**
- Crea el `ChatAdapter` y lo conecta a la lista de mensajes.
- El chat se escucha EN VIVO: cuando alguien envía un mensaje, aparece automáticamente sin necesidad de recargar.
- Auto-scroll: los mensajes nuevos aparecen abajo y la lista se despliega sola.

**Función: `setupButtons()`**
- **Botón "Enviar"** (texto normal): Toma lo que escribiste y lo manda como tipo `"TEXT"`.
- **Botón "Precio"** (solo artista): Abre una ventana para escribir un nuevo monto. Lo manda como tipo `"PRICE_UPDATE"`.
- **Botón "Progreso"** (solo artista): Abre una ventana para pegar un link de imagen de avance. Lo manda como tipo `"PROGRESS_IMAGE"`.
- **Botón "Final"** (solo artista): Abre una ventana para pegar el link de la obra terminada. Lo manda como tipo `"FINAL_PRODUCT"` Y además marca la comisión como COMPLETED.

**Función: `mostrarDialogoInput(titulo, hint, tipoMensaje)`**
- Muestra una ventana con un campo de texto.
- Si es de precio, el teclado es numérico con decimales.
- Al enviar:
  - Crea el mensaje en Firebase.
  - Si es producto final, también llama a `entregarProductoFinal()`.

**Función: `enviarMensaje(contenido, tipo)`**
- Crea un objeto `ChatMessage` y lo guarda en Firebase en `chats/{commissionId}/{msgId}`.

**Función: `aceptarNuevoPrecio(nuevoPrecio)`**
- Actualiza el precio de la comisión en Firebase.
- Envía un mensaje automático: "El cliente ha aceptado el nuevo precio de $X."

**Función: `entregarProductoFinal(urlFinal)`**
- Guarda la URL del producto final en la comisión.
- Cambia el estado a `"COMPLETED"`.
- Muestra "Obra entregada exitosamente".

---

#### `ProfileActivity.kt` — El perfil del usuario

Archivo: `app/src/main/java/com/example/geart_20/ProfileActivity.kt`

**Sirve para:** Ver y editar perfiles. También para que los artistas vean sus solicitudes directas y su portafolio.

**¿Qué hace?**

1. Determina si el perfil que se ve es el **propio** o el de **otra persona**:
   - Si es propio: permite editar nombre, foto, bio, redes.
   - Si es de otro: oculta el botón "Guardar Cambios" y deshabilita los campos.

2. Carga los datos del usuario desde `users/{viewedUserId}`.

3. **Si el usuario es ARTISTA**:
   - Muestra "🎨 Artista ⭐ 4.5 (12 reseñas)".
   - **Botón "Solicitar Comisión a este Artista"** (solo si el visitante es cliente y no es su propio perfil): abre `CreateCommissionActivity` con el ID del artista para hacerle una solicitud directa.
   - **Solicitudes Directas Pendientes** (solo visible para el dueño del perfil): muestra una lista de comisiones con estado `"DIRECT_REQUEST"` para este artista.
   - **Portafolio** (visible para todos): muestra las comisiones COMPLETADAS del artista.

4. **Carga la foto de perfil** con Glide (la pone circular).

5. **Botón "Guardar Cambios"**: guarda nombre, foto, bio y redes en Firebase.

6. **Sección de Comentarios**:
   - Muestra todos los comentarios de ese perfil (desde `comments/{viewedUserId}`).
   - Campo para escribir un nuevo comentario y botón "Publicar Comentario".
   - Cuando publicas, se guarda con tu nombre y el texto.

---

### 3.6 Archivos de prueba (tests)

---

#### `ExampleUnitTest.kt`

Archivo: `app/src/test/java/com/example/geart_20/ExampleUnitTest.kt`

**Sirve para:** Un ejemplo de cómo se hacen pruebas en la computadora (sin teléfono).
- Solo verifica que `2 + 2 = 4`.

---

#### `ExampleInstrumentedTest.kt`

Archivo: `app/src/androidTest/java/com/example/geart_20/ExampleInstrumentedTest.kt`

**Sirve para:** Un ejemplo de cómo se hacen pruebas en un teléfono de verdad.
- Verifica que el nombre del paquete de la app sea `"com.example.geart_20"`.

---

## 4. Los archivos XML (la parte visual)

> **¿Qué es XML?** Es un idioma para describir cómo se ven las cosas en pantalla. Como si describieras un dibujo: "Hay un botón azul aquí, un texto allá, una lista acá".

---

### 4.1 `AndroidManifest.xml`

Archivo: `app/src/main/AndroidManifest.xml`

**Sirve para:** Es la "cédula de identidad" de la app. Le dice al teléfono:
- Cómo se llama la app (`GeArt_2.0`)
- Qué pantallas (Activities) tiene
- Cuál es la pantalla principal (LoginActivity)
- Que usa Firebase
- El iconito de la app

---

### 4.2 Los Layouts (los diseños de las pantallas)

#### `activity_login.xml` — La pantalla de entrada

**¿Qué se ve?**
- Un campo para escribir el email
- Un campo para escribir la contraseña (los puntitos la ocultan)
- Dos opciones: "Cliente" o "Artista"
- Botón "Registrarse"
- Botón "Ingresar"

**¿Cómo está organizado?** Es como una torre de bloques (LinearLayout vertical). Cada bloque está uno debajo del otro.

---

#### `activity_main_artist.xml` — El panel del artista

**¿Qué se ve?**
- Arriba: el nombre del artista con "Mercado de Comisiones"
- Tres botones de filtro: "Mercado" (azul), "Directas" (verde), "En Progreso" (morado)
- Una lista grande de comisiones en el medio
- Abajo a la izquierda: "Mi Perfil"
- Abajo a la derecha: "Cerrar Sesión" (rojo)

**¿Cómo está organizado?** Usa ConstraintLayout, que es como poner cosas en un mapa: "el título está arriba, los filtros están debajo del título, la lista está entre los filtros y el botón de cerrar sesión".

---

#### `activity_main_client.xml` — El panel del cliente

**¿Qué se ve?**
- Botón "Solicitar Nueva Comisión" (arriba)
- Botón "Explorar Directorio de Artistas" (azul, debajo)
- Lista de las comisiones del cliente
- "Mi Perfil" (abajo izquierda)
- "Cerrar Sesión" (rojo, abajo derecha)

---

#### `activity_explore.xml` — El directorio de artistas

**¿Qué se ve?**
- Título "Directorio de Artistas"
- Una barra de búsqueda con lupa 🔍 y fondo oscuro
- Una lista de artistas

---

#### `activity_create_commission.xml` — El formulario de pedido

**¿Qué se ve?**
- Título "Solicitar Comisión"
- Campo: "Título de la comisión (ej. Retrato de mi OC)"
- Campo: "Descripción detallada" (con espacio para 4 líneas)
- Campo: "Presupuesto sugerido ($)" (solo números)
- Campo: "Link de imagen de referencia (Opcional)"
- Botón grande "Publicar Solicitud"

Está dentro de un `ScrollView`, que permite hacer scroll hacia abajo si la pantalla es muy chica.

---

#### `activity_chat.xml` — La pantalla del chat

**¿Qué se ve?**
- Arriba: el título de la comisión
- El medio: la lista de mensajes
- Una barra con tres botones: "Precio" (verde), "Progreso" (azul), "Final" (morado) — SOLO visible para el artista
- Abajo: un campo de texto y un botón "Enviar"

Fondo oscuro `#121212` (casi negro).

---

#### `activity_profile.xml` — La pantalla de perfil

**¿Qué se ve?** (es larga, tiene scroll)

- La foto de perfil (circular, 120dp)
- El rol y la calificación
- Botón "Solicitar Comisión a este Artista" (verde, oculto hasta que se necesita)
- Campo: "Nombre o Seudónimo"
- Campo: "Link de Foto de Perfil"
- Campo: "Biografía o Descripción" (3 líneas)
- Campo: "Redes sociales"
- Botón "Guardar Cambios"
- "Tus Solicitudes Directas Pendientes" (solo para el artista dueño)
- "Trabajos Finalizados:" (el portafolio)
- Una línea separadora
- "Comentarios:"
- Lista de comentarios
- Campo para escribir comentario
- Botón "Publicar Comentario"

---

#### `item_artist.xml` — Una tarjeta de artista en la lista

**¿Cómo se ve cada artista?**
- A la izquierda: foto circular (64dp)
- Al lado: nombre en blanco grande, y calificación gris debajo
- A la derecha: botón "Ver"

Fondo oscuro `#1E1E1E`.

---

#### `item_commission.xml` — Una tarjeta de comisión

**¿Cómo se ve cada comisión?**
- Título en negro y negrita
- Descripción (máximo 2 líneas, si es más larga pone "...")
- Abajo: a la izquierda el precio en verde, a la derecha el estado en itálica

Está dentro de una `CardView` (una tarjeta con bordes redondeados y sombra).

---

#### `item_comment.xml` — Un comentario

- Nombre del autor en azul negrita
- Texto del comentario debajo

---

#### `item_chat_message.xml` — Un mensaje del chat

**¿Cómo se ve cada mensaje?**
- Una burbuja de color (dentro puede tener):
  - **Texto**: letras blancas
  - **Imagen**: de 200x200, con una etiqueta arriba
  - **Propuesta de precio**: el monto y un botón "Aceptar Precio" (verde)
- Debajo de la burbuja: la hora en gris chiquito

Los elementos que no se usan están ocultos (`visibility="gone"`). Por ejemplo, si es un mensaje de texto, la imagen y el precio están ocultos.

---

## 5. Los archivos de recursos (colores, textos, dibujos)

### `res/values/colors.xml`
Guarda los colores: solo blanco y negro por ahora.

### `res/values/strings.xml`
Guarda textos: solo el nombre de la app "GeArt_2.0".

### `res/values/themes.xml`
Define cómo se ve la app en **modo claro** (fondo blanco). Usa Material3 con "NoActionBar" (sin barra de título arriba).

### `res/values-night/themes.xml`
Define cómo se ve la app en **modo oscuro**. Misma configuración que el claro.

### `res/drawable/ic_launcher_background.xml`
El fondo del icono de la app: un cuadrado verde con líneas.

### `res/drawable-v24/ic_launcher_foreground.xml`
El dibujo de adelante del icono: el robot de Android (bugdroid) blanco.

### `res/mipmap-*/` — Los iconos de la app
Carpetas con los iconos de la app en diferentes tamaños (para que se vean bien en teléfonos chicos y grandes).

### `res/xml/backup_rules.xml` y `data_extraction_rules.xml`
Reglas de seguridad para hacer copias de seguridad de la app. Están vacías (sin reglas personalizadas).

---

## 6. Los archivos de construcción (Gradle)

> **¿Qué es Gradle?** Es un programa que cocina la app. Toma todos los ingredientes (código Kotlin, dibujos XML, librerías) y los mezcla para crear un archivo `.apk` que se instala en el teléfono.

---

### `settings.gradle.kts`
Dice:
- Dónde buscar las herramientas (Google, Maven Central)
- Cómo se llama el proyecto: "GeArt_2.0"
- Qué módulos tiene (solo `:app`)

### `build.gradle.kts` (raíz)
La receta principal. Usa dos plugins:
- `com.android.application` — para hacer apps Android
- `com.google.gms.google-services` — para conectar con Firebase

### `app/build.gradle.kts`
La receta del módulo `app`. Aquí están las cosas importantes:

**Datos de la app:**
| Dato | Valor |
|------|-------|
| `compileSdk` | 37 (versión más nueva de Android para la que compila) |
| `applicationId` | `com.example.geart_20` (identificador único) |
| `minSdk` | 23 (funciona en Android 6 en adelante) |
| `targetSdk` | 35 (optimizada para Android 15) |
| `versionCode` | 1 (primera versión) |
| `versionName` | "1.0" |

**Librerías que usa (como ingredientes):**

| Librería | ¿Para qué sirve? |
|----------|-----------------|
| Firebase BOM 34.14.1 | Controla versiones de todas las librerías de Firebase |
| Firebase Analytics | Mide cómo usan la app |
| Firebase Realtime Database | Guarda datos en tiempo real |
| Firebase Auth | Maneja registros e inicios de sesión |
| kotlinx-coroutines-play-services | Ayuda a hacer tareas en segundo plano con Firebase |
| Glide 4.16.0 | Muestra imágenes de internet |
| AndroidX Core KTX | Herramientas básicas de Android |
| AppCompat | Compatibilidad con versiones viejas de Android |
| Material | Diseño moderno (Material Design) |
| ConstraintLayout | Para organizar cosas en pantalla |
| JUnit | Para hacer pruebas |

### `gradle/libs.versions.toml`
Un catálogo que dice las versiones exactas de cada herramienta. Por ejemplo:
- AGP (Android Gradle Plugin): 9.1.1
- Core KTX: 1.19.0
- Material: 1.14.0

### `gradle.properties`
Propiedades de Gradle: cuánta memoria usar (2048 MB), codificación de texto (UTF-8).

### `gradle/wrapper/gradle-wrapper.properties`
Dice qué versión de Gradle usar (9.3.1) y dónde descargarla.

---

## 7. `google-services.json` — La llave de Firebase

Archivo: `app/google-services.json`

**Sirve para:** Conectar la app con Firebase. Es como una llave que dice "esta app puede usar mi proyecto de Firebase". Contiene:
- El ID del proyecto Firebase: `geart-d92cf`
- La URL de la base de datos: `https://geart-d92cf-default-rtdb.firebaseio.com`
- URLs de almacenamiento de imágenes
- La clave API para hablar con Firebase

---

## 8. Resumen de todo el flujo de la app

Para que entiendas cómo funciona todo junto, aquí está el viaje completo:

### Un cliente quiere un dibujo

1. **Abre la app** → ve `LoginActivity`
2. **Se registra** como Cliente (email, contraseña)
3. **Llega al panel de cliente** (`MainActivity` → `setupClientPanel`)
4. **Toca "Explorar Directorio de Artistas"** → ve `ExploreActivity` con la lista de artistas
5. **Encuentra un artista**, toca "Ver" → ve su perfil (`ProfileActivity`)
6. **Toca "Solicitar Comisión a este Artista"** → se abre `CreateCommissionActivity` con el artista ya seleccionado
7. **Llena el formulario** (título, descripción, precio, referencia) y publica
8. **La comisión se guarda** en Firebase con estado `"DIRECT_REQUEST"`

### El artista recibe el pedido

1. **Abre la app** → inicia sesión como Artista
2. **Llega al panel de artista** (`MainActivity` → `setupArtistPanel`)
3. **Toca el filtro "Directas"** → ve la solicitud directa
4. **Toca la comisión** → ve los detalles
5. **Toca "Aceptar Solicitud"** → la comisión cambia a `"ACCEPTED"`

### Cliente y artista trabajan juntos

1. **Cualquiera toca "Abrir Chat"** en los detalles de la comisión
2. **Entran al chat** (`ChatActivity`)
3. **Hablan**: el cliente explica mejor lo que quiere, el artista hace preguntas
4. **El artista envía "Precio"** → el cliente ve la propuesta y puede "Aceptar Precio"
5. **El artista envía "Progreso"** → muestra imágenes de cómo va el dibujo
6. **El artista envía "Final"** → entrega el dibujo terminado, la comisión se marca `"COMPLETED"`

### El cliente califica

1. El cliente ve la comisión con estado "COMPLETED"
2. Toca "Calificar Artista"
3. Elige de 1 a 5 estrellas
4. El promedio del artista se actualiza automáticamente

### Fin

¡El artista tiene más reseñas, el cliente tiene su dibujo, y todos están felices!

---

## 9. Diagrama de la estructura de Firebase

Así se guarda todo en la base de datos de Firebase:

```
/
├── users/
│   ├── {uid}/
│   │   ├── id: "abc123"
│   │   ├── name: "Ana"
│   │   ├── email: "ana@correo.com"
│   │   ├── role: "ARTIST"
│   │   ├── profileImageUrl: "https://..."
│   │   ├── bio: "Dibujo desde los 5 años"
│   │   ├── socialLinks: "https://..."
│   │   └── rating: 4.5
│   │   └── ratingCount: 12
│   └── {otroUid}/...
│
├── commissions/
│   ├── {commissionId}/
│   │   ├── id: "-O5fG3..."
│   │   ├── clientId: "abc123"
│   │   ├── artistId: "def456"
│   │   ├── title: "Retrato de mi gato"
│   │   ├── description: "Gato naranja con ojos verdes"
│   │   ├── status: "COMPLETED"
│   │   ├── price: 25.00
│   │   ├── referenceImageUrl: "https://..."
│   │   ├── isRated: true
│   │   └── finalProductUrl: "https://..."
│   └── {otraComision}/...
│
├── chats/
│   ├── {commissionId}/
│   │   ├── {mensajeId}/
│   │   │   ├── id: "-AbC12..."
│   │   │   ├── senderId: "abc123"
│   │   │   ├── message: "Hola, aquí va el avance"
│   │   │   ├── type: "PROGRESS_IMAGE"
│   │   │   └── timestamp: 1234567890
│   │   └── {otroMensaje}/...
│   └── {otroChat}/...
│
└── comments/
    ├── {userId}/
    │   ├── {commentId}/
    │   │   ├── id: "-X7hK2..."
    │   │   ├── fromUserId: "ghi789"
    │   │   ├── fromUserName: "Pedro"
    │   │   ├── text: "Excelente artista"
    │   │   └── timestamp: 1234567890
    │   └── {otroComentario}/...
    └── {otroUsuario}/...
```

---

## 10. Glosario para principiantes

| Término | Significado |
|---------|-------------|
| **Android** | Sistema operativo de teléfonos (como Windows pero en celus) |
| **Kotlin** | El idioma en que está escrita la app |
| **Firebase** | Un servicio de Google que guarda datos en internet |
| **Realtime Database** | Una base de datos que se actualiza sola cuando algo cambia |
| **Activity** | Una pantalla de la app (como una página web) |
| **Layout** | El diseño visual de una pantalla (dónde van los botones, textos, etc.) |
| **ViewModel** | Un asistente que ayuda a la pantalla a hacer su trabajo |
| **Repository** | Un mensajero que habla con Firebase |
| **Adapter** | Un instructor que dice cómo mostrar cada elemento de una lista |
| **ViewHolder** | Un "asiento" en la lista que guarda los dibujos de cada elemento |
| **RecyclerView** | Una lista que recicla sus elementos (en vez de crear 1000, crea solo los que se ven) |
| **Glide** | Un programa que descarga y muestra imágenes de internet |
| **Gradle** | Un programa que cocina la app para que funcione en el teléfono |
| **XML** | Un idioma para describir cómo se ven las cosas |
| **Intent** | Un mensaje para abrir otra pantalla y pasarle datos |
| **Model** | Un molde que dice qué datos guardar |
| **UID** | Un número único que identifica a cada usuario |
| **Push Key** | Un identificador único que Firebase genera automáticamente |
| **Transaction** | Una operación que se hace completa o no se hace (para evitar errores) |
