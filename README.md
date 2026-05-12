# App Delivery: Simulación Concurrente (Productor-Consumidor)

Esta aplicación es una demostración visual y pedagógica del problema clásico de sincronización de hilos **Productor-Consumidor**. Utiliza una temática de restaurante donde los hilos "Clientes" generan pedidos y los hilos "Cocineros" los procesan, interactuando a través de una "Bandeja" (Buffer) de capacidad limitada.

## Características

* **Visualización en Tiempo Real:** Interfaz web que muestra el estado de cada hilo (Trabajando, Esperando, Sección Crítica).
* **Configuración Dinámica:** Sliders para ajustar en caliente la cantidad de productores, consumidores y el tamaño del buffer.
* **Backend Robusto:** Implementación en Java puro utilizando semáforos para el control de concurrencia.
* **Frontend Minimalista:** Comunicación mediante una API REST ligera servida por `com.sun.net.httpserver`.

## Estructura del Proyecto

* `MainServer.java`: Punto de entrada. Inicia el servidor HTTP y gestiona el ciclo de vida de los hilos.
* `Buffer.java`: El monitor de recursos compartidos. Utiliza semáforos para garantizar la exclusión mutua y gestionar la ocupación.
* `Productor.java` / `Consumidor.java`: Lógica de comportamiento de los hilos.
* `index.html` / `styles.css` / `api.js`: Interfaz de usuario y lógica de comunicación/renderizado.

## Detalles Técnicos de Sincronización

El núcleo de la sincronización reside en `Buffer.java` mediante tres semáforos:

1. **Mutex:** Garantiza que solo un hilo acceda a la sección crítica del buffer a la vez.
2. **Vacíos:** Controla los espacios disponibles para producir (evita el desbordamiento).
3. **Llenos:** Controla los elementos disponibles para consumir (evita el subdesbordamiento).

## Requisitos

* Java JDK 8 o superior.
* Navegador web moderno.

## Ejecución

1. Compila los archivos fuente:
```bash
javac *.java

```


2. Ejecuta el servidor:
```bash
java MainServer

```


3. Accede a la interfaz web en:
`http://localhost:8080`

## Estados del Sistema

| Estado | Descripción Visual | Significado Técnico |
| --- | --- | --- |
| **TRABAJANDO** | Azul | El hilo realiza tareas propias (fuera del buffer). |
| **ESPERANDO** | Gris (Opaco) | El hilo está bloqueado en un semáforo (`acquire`). |
| **SECCIÓN CRÍTICA** | Verde (Resaltado) | El hilo tiene el control del Mutex y está modificando el Buffer. |

---
