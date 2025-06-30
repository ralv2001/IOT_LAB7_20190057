# IOT_LAB7_20190057 - **RICARDO ALVARADO RUIZ, LEER TODO ANTES DE REALIZAR LA REVISIÓN**
Este es mi laboratorio 7 (el último siu) :)

## **Instrucciones de testeo de la aplicación**

Se ha hecho uso del servicio de nube AWS mediante la función AWS lambda para el almacenamiento de las imágenes en AWS S3. A continuación, se detallan los pasos para poder testear la aplicación:

Paso 1: Registrarse/Loguearse en la aplicación. Se recomienda que se haga con google, ya que esto lo hará lo más rápido posible. Sin embargo, también hay la opción de registrarse poniendo un usuario y contraseña (dando clic en el botón de registrarse) sin necesidad de acceder con google y luego iniciar sesión con ese usuario.

![image](https://github.com/user-attachments/assets/29ef047a-72af-4c63-a748-e4a1062735e0)

Paso 2: Un vez ya logueado, dirigirse a la sección de "Perfil" en la esquina inferior derecha y dar clic en "Seleccionar" para seleccionar la imagen que posteriormente se subirá a la nube.

![image](https://github.com/user-attachments/assets/375fb492-9c4e-45bb-b870-0121f309e8be)

Paso 3: Seleccionar una imagen, de preferencia, antigua, para no confindir la imagen seleccionada para subir a la nube con la imagen posteriormente descargada de la nube (que va a ser esa misma que se va a subir a la nube)

![image](https://github.com/user-attachments/assets/24f8399d-e1ec-4cef-a08a-924131278046)

Paso 4: Se habilitará el botón "Carga". Hacer clic ahí para poder subir la imagen a la nube. Una vez se haya hecho clic, se habilitará el botón "Descarga".

![image](https://github.com/user-attachments/assets/4b698927-fabb-4168-af30-5c6b7c56f2a9)

Paso 5: Hacer clic en el botón "Descarga" para descargar la imagen que se acaba de subir a la nube. Se mostrarán 3 cosas:
  1. La imagen descargada en la parte de abajo.
  2. Un link para que se verifique que la imagen es visible desde internet. Si desea, puede dar clic en el enlace para verificar que la imagen se ha cargado efectivamente en el S3, ya que esa URL es pública y accesible desde cualquier navegador web. Además, se tiene un botón "Copiar URL" para copiar la dirección y ponerla manualmente en el navegador y verificar que la imagen está disponible online.
     
     ![image](https://github.com/user-attachments/assets/fcb351e6-7762-4aca-9e56-0dcaafa5c2f4)
     
  4. Un toast que diga que la imagen se descargó correctamente y que ahora está guardada en la galería.

     ![image](https://github.com/user-attachments/assets/c37a999d-d542-406d-904f-f93916729595)

![image](https://github.com/user-attachments/assets/6de66ba0-1ccb-435f-8d2b-b08d0267a14d)

Paso 6: Verificar que la imagen haya sido descargada correctamente.
Dirigirse a la app "Google Photos", seleccionar "Collections" y luego "On this device".

![image](https://github.com/user-attachments/assets/63ccab1c-857e-462e-86d9-1f8c9c068e57)

Verificar que se creó el directorio "IOT_LAB7_20190057" en la galería y que la imagen descargada de la nuebe esté efectivamente en ese directorio.

![image](https://github.com/user-attachments/assets/fa116f7d-22a3-4fd4-a986-fe1e0ac911f5)

Por último, se ha añadido al JP encargado del laboratorio, Rodrigo Ruiz Cavero, permisos de visualización del firebase del laboratorio para que se pueda realizar la verificación correspondiente:

![image](https://github.com/user-attachments/assets/e80fbe42-7905-435e-9f9b-c02a0f047ff6)

## **Uso de Inteligencia artificial**
Se utilizó inteligencia artificial para ayuda en la resolución de bugs en los códigos Java, así como para generar comentarios que ayuden a identificar mejor las funcionalidades del código. También se utilizó IA para el desarrollo del código Java que conecta con AWS Lambda y AWS S3, específicamente en los archivos "CloudStorage.java" y "PerfilFragment.java".

## **Detalles Técnicos del Cloud Storage**
- **Servicio utilizado:** AWS S3 con AWS Lambda
- **Endpoint:** API Gateway + Lambda function
- **Formato de imagen:** JPEG con compresión 80%
- **Codificación:** Base64 para transferencia
- **URLs generadas:** Públicas con acceso directo desde navegador
- **Directorio local:** `Pictures/IOT_LAB7_20190057/`

## **Información del Proyecto:** 
La versión de Android utilizada es:  
**API 31 ("S", Android 12.0)**

## **Entorno de Desarrollo**
**Android Studio**  
Emulador configurado:  
**Pixel 4 API 31 (Android 12.0 "S") x86_64**

## **Configuración del Proyecto**
**Nombre:** LAB6_20190057
**Package name:** com.example.LAB6_20190057
