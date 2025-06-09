# Sistema de Mensajería Instantánea - Trabajo Práctico (Grupo 2)

Este proyecto consiste en el desarrollo de un sistema de mensajería instantánea centralizado, implementado en Java, con almacenamiento local de conversaciones, encriptación de mensajes de extremo a extremo y tolerancia a fallas para alta disponibilidad.

## Descripción General

El sistema permite a los usuarios enviar y recibir mensajes a través de un servidor central, que almacena y distribuye los mensajes. Además, cuenta con un sistema de directorio para registrar y consultar información básica sobre los usuarios conectados, facilitando la gestión de contactos y la agenda personal.

### Características Principales

- **Arquitectura centralizada cliente-servidor** con tolerancia a fallas.
- **Gestión de contactos** a través de un servidor de directorio.
- **Almacenamiento local** de conversaciones en el dispositivo del usuario, con soporte para múltiples formatos.
- **Mensajes encriptados** de extremo a extremo mediante esquemas simétricos seleccionables.
- **Alta disponibilidad** mediante técnicas de redundancia y recuperación ante fallas.
- **Interfaz gráfica de usuario (GUI)** para facilitar la interacción.

---

## Patrones de Diseño GoF Utilizados

- **State**: Cambia el comportamiento del servidor según su estado (primario/secundario).
- **Singleton**: Previene referencias múltiples al controlador principal y desacopla componentes.
- **Abstract Factory**: Permite seleccionar el formato de persistencia de contactos y chats (TXT, JSON, XML).
- **Observer**: Facilita la recepción de mensajes del servidor al cliente en tiempo real.
- **Strategy**: Permite elegir el algoritmo de encriptación/desencriptación de mensajes.

---

## Persistencia y Encriptación

### Persistencia de Conversaciones

El sistema permite elegir entre tres formatos para almacenar las conversaciones de usuario:
- **TXT** (texto plano)
- **JSON**
- **XML**

La selección del formato se realiza mediante el patrón **Abstract Factory**.

### Algoritmos de Encriptación

Los mensajes son encriptados de extremo a extremo utilizando uno de los siguientes algoritmos, seleccionables por el usuario:
- **Cesar**
- **XOR**
- **AES**

La selección y aplicación del algoritmo se realiza mediante el patrón **Strategy**.

---

## Disponibilidad y Tolerancia a Fallos

Para mejorar la disponibilidad y robustez del sistema, se implementaron las siguientes tácticas:

### Detección de Fallas

- **Heartbeat**: El servidor primario envía pulsos periódicos al servidor secundario. Si el secundario no recibe un pulso en más de 5 segundos, asume que el primario ha fallado.

### Recuperación de Fallas

- **Redundancia Pasiva**: Se implementa un servidor secundario (backup) que mantiene su estado sincronizado con el primario y puede asumir el rol principal en caso de falla.
- **Reintento**: Ante fallos de conexión, envío o recepción, el cliente reintenta la operación con el servidor principal y, si falla, con el servidor de respaldo (hasta dos veces).
- **Resincronización de Estado**: Si el servidor principal falla y se reinicia, recupera su estado anterior sincronizándose con la instancia de respaldo.

---

## Funcionalidades

- Registro de usuarios con nickname, IP y puerto en el servidor de directorios (nickname único).
- Consulta y agregado de contactos mediante el directorio.
- Envío de mensajes a través del servidor con almacenamiento temporal si el destinatario está desconectado.
- Almacenamiento local y recuperación del historial de conversaciones tras reinicio.
- Selección de formato de persistencia y algoritmo de cifrado.
- Encriptación de extremo a extremo utilizando una clave compartida.
- GUI para gestionar contactos, conversaciones y configuraciones.

---

## Manual de Usuario

### Instalación

1. Clonar el repositorio:
   ```sh
   git clone https://github.com/facundodelga/Mensajeria-P2P-GRUPO-2.git
   ```
2. Compilar el proyecto:
   ```sh
   cd Mensajeria-P2P-GRUPO-2
   # Usar Maven, Gradle o IDE de su preferencia
   ```
3. Ejecutar la aplicación:
   ```sh
   # Según el método de compilación
   java -jar target/MensajeriaP2P.jar
   ```

### Configuración

- Al iniciar, el usuario puede elegir el formato de almacenamiento local (TXT, JSON, XML) y el algoritmo de encriptación (Cesar, XOR, AES).
- Ingresar nickname, IP y puerto para registrarse en el servidor de directorios.

### Operación

- Agregar nuevos contactos desde el directorio.
- Iniciar conversaciones y enviar mensajes encriptados.
- Al reiniciar la aplicación, las conversaciones previas estarán disponibles según el formato seleccionado.

---

## Documentación Técnica

La documentación incluye:
- Diagramas de casos de uso, dominio, componentes, despliegue, clases, secuencia y paquetes.
- Detalles sobre la implementación de patrones GoF y tácticas de disponibilidad.
- Ejemplos de uso y escenarios de recuperación ante fallos.

---

## Créditos

- **Grupo 2** - Trabajo Práctico de Analisis y Diseño de Sistemas II
- Universidad Nacional de Mar del Plata

---

## Licencia

Este proyecto es de uso educativo y está sujeto a las normas de la materia.
