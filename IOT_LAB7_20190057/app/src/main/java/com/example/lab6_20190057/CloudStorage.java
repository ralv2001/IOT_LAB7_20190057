package com.example.lab6_20190057;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CloudStorage {

    private static final String TAG = "CloudStorage";
    private static final String AWS_ENDPOINT = "https://yedtogfjv1.execute-api.us-east-1.amazonaws.com/prod/file-operations";

    private OkHttpClient client;
    private Context context;

    // Interfaces para callbacks
    public interface UploadCallback {
        void onSuccess(String downloadUrl);
        void onError(String error);
    }

    public interface DownloadCallback {
        void onSuccess(String imagePath);
        void onError(String error);
    }

    public interface ConnectionCallback {
        void onConnected();
        void onError(String error);
    }

    public CloudStorage(Context context) {
        this.context = context;
        inicializarConexion();
    }

    /**
     * Método requerido: Conexión al servicio de almacenamiento
     * Inicializa la conexión con AWS S3 a través de API Gateway
     */
    public void conectarServicioAlmacenamiento(ConnectionCallback callback) {
        Log.d(TAG, "🔗 Iniciando conexión al servicio de almacenamiento AWS...");

        try {
            // Test de conectividad simulando un download válido
            JSONObject testJson = new JSONObject();
            testJson.put("action", "download");
            testJson.put("s3_key", "test_connection");

            RequestBody testBody = RequestBody.create(
                    testJson.toString(),
                    MediaType.get("application/json; charset=utf-8")
            );

            Request request = new Request.Builder()
                    .url(AWS_ENDPOINT)
                    .post(testBody)
                    .addHeader("Content-Type", "application/json")
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "❌ Error de conexión: " + e.getMessage());
                    callback.onError("Error de conexión: " + e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    response.close();
                    // Si obtenemos cualquier respuesta del servidor (200, 400, 500), significa que está funcionando
                    if (response.code() >= 200 && response.code() < 600) {
                        Log.d(TAG, "✅ Conexión exitosa al servicio AWS (endpoint responde con código: " + response.code() + ")");
                        callback.onConnected();
                    } else {
                        Log.e(TAG, "❌ Error de conexión. Código: " + response.code());
                        callback.onError("Error de conexión. Código: " + response.code());
                    }
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "❌ Error al conectar: " + e.getMessage());
            callback.onError("Error al conectar: " + e.getMessage());
        }
    }

    /**
     * Método requerido: Guardar archivo
     * Sube una imagen al servicio de almacenamiento AWS S3
     */
    public void guardarArchivo(Bitmap imagen, String nombreArchivo, String userId, UploadCallback callback) {
        Log.d(TAG, "📤 Iniciando subida de archivo: " + nombreArchivo);

        try {
            // Convertir Bitmap a Base64
            String base64Image = convertirBitmapABase64(imagen);

            // Crear JSON para la petición
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("action", "upload");
            jsonBody.put("file_data", "data:image/jpeg;base64," + base64Image);
            jsonBody.put("file_name", nombreArchivo);
            jsonBody.put("user_id", userId);

            // Crear petición HTTP
            RequestBody body = RequestBody.create(
                    jsonBody.toString(),
                    MediaType.get("application/json; charset=utf-8")
            );

            Request request = new Request.Builder()
                    .url(AWS_ENDPOINT)
                    .post(body)
                    .addHeader("Content-Type", "application/json")
                    .build();

            // Ejecutar petición
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "❌ Error al subir archivo: " + e.getMessage());
                    callback.onError("Error al subir archivo: " + e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        String responseBody = response.body().string();
                        Log.d(TAG, "📡 Respuesta del servidor: " + responseBody);

                        if (response.isSuccessful()) {
                            JSONObject jsonResponse = new JSONObject(responseBody);

                            if (jsonResponse.has("download_link")) {
                                String downloadUrl = jsonResponse.getString("download_link");
                                Log.d(TAG, "✅ Archivo subido exitosamente. URL: " + downloadUrl);
                                callback.onSuccess(downloadUrl);
                            } else if (jsonResponse.has("file_info")) {
                                JSONObject fileInfo = jsonResponse.getJSONObject("file_info");
                                String downloadUrl = fileInfo.getString("file_url");
                                Log.d(TAG, "✅ Archivo subido exitosamente. URL: " + downloadUrl);
                                callback.onSuccess(downloadUrl);
                            } else {
                                Log.e(TAG, "❌ Respuesta sin URL de descarga");
                                callback.onError("Respuesta sin URL de descarga");
                            }
                        } else {
                            Log.e(TAG, "❌ Error del servidor: " + response.code() + " - " + responseBody);
                            callback.onError("Error del servidor: " + response.code());
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "❌ Error procesando respuesta: " + e.getMessage());
                        callback.onError("Error procesando respuesta: " + e.getMessage());
                    }
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "❌ Error preparando archivo: " + e.getMessage());
            callback.onError("Error preparando archivo: " + e.getMessage());
        }
    }

    /**
     * Método requerido: Obtener archivo
     * Descarga una imagen desde AWS S3 y la guarda en el dispositivo
     */
    public void obtenerArchivo(String s3Key, DownloadCallback callback) {
        Log.d(TAG, "📥 Iniciando descarga de archivo: " + s3Key);

        try {
            // Crear JSON para solicitar descarga
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("action", "download");
            jsonBody.put("s3_key", s3Key);

            Log.d(TAG, "📤 JSON enviado para descarga: " + jsonBody.toString());

            RequestBody body = RequestBody.create(
                    jsonBody.toString(),
                    MediaType.get("application/json; charset=utf-8")
            );

            Request request = new Request.Builder()
                    .url(AWS_ENDPOINT)
                    .post(body)
                    .addHeader("Content-Type", "application/json")
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "❌ Error al descargar archivo: " + e.getMessage());
                    callback.onError("Error al descargar archivo: " + e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        String responseBody = response.body().string();
                        Log.d(TAG, "📡 Respuesta descarga (" + response.code() + "): " + responseBody.substring(0, Math.min(500, responseBody.length())) + "...");

                        if (response.isSuccessful()) {
                            JSONObject jsonResponse = new JSONObject(responseBody);
                            Log.d(TAG, "🔍 Keys disponibles en respuesta: " + jsonResponse.keys().toString());

                            if (jsonResponse.has("file_data")) {
                                // Método original con Base64
                                String base64Data = jsonResponse.getString("file_data");
                                Log.d(TAG, "📊 Datos Base64 recibidos, longitud: " + base64Data.length());

                                if (base64Data.contains(",")) {
                                    base64Data = base64Data.split(",")[1];
                                    Log.d(TAG, "🔧 Prefijo removido, nueva longitud: " + base64Data.length());
                                }

                                String imagePath = guardarImagenEnDispositivo(base64Data, s3Key);

                                if (imagePath != null) {
                                    Log.d(TAG, "✅ Archivo descargado y guardado en: " + imagePath);
                                    callback.onSuccess(imagePath);
                                } else {
                                    Log.e(TAG, "❌ Error guardando imagen en dispositivo");
                                    callback.onError("Error guardando imagen en dispositivo");
                                }
                            } else if (jsonResponse.has("download_url")) {
                                // Método alternativo: descargar desde URL directa
                                String downloadUrl = jsonResponse.getString("download_url");
                                Log.d(TAG, "🔗 Descargando desde URL: " + downloadUrl);

                                descargarDesdeUrl(downloadUrl, s3Key, callback);
                            } else if (jsonResponse.has("success") && !jsonResponse.getBoolean("success")) {
                                String errorMsg = jsonResponse.optString("message", "Error desconocido");
                                Log.e(TAG, "❌ Error del servidor: " + errorMsg);
                                callback.onError("Error del servidor: " + errorMsg);
                            } else {
                                Log.e(TAG, "❌ Respuesta sin datos de imagen. Keys: " + jsonResponse.keys().toString());
                                callback.onError("Respuesta sin datos de imagen");
                            }
                        } else {
                            Log.e(TAG, "❌ Error del servidor: " + response.code() + " - " + responseBody);
                            callback.onError("Error del servidor: " + response.code());
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "❌ Error procesando descarga: " + e.getMessage());
                        e.printStackTrace();
                        callback.onError("Error procesando descarga: " + e.getMessage());
                    }
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "❌ Error preparando descarga: " + e.getMessage());
            callback.onError("Error preparando descarga: " + e.getMessage());
        }
    }



    private void descargarDesdeUrl(String downloadUrl, String s3Key, DownloadCallback callback) {
        Log.d(TAG, "🌐 Descargando imagen desde URL directa...");

        Request request = new Request.Builder()
                .url(downloadUrl)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "❌ Error descargando desde URL: " + e.getMessage());
                callback.onError("Error descargando desde URL: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    if (response.isSuccessful()) {
                        byte[] imageBytes = response.body().bytes();
                        Log.d(TAG, "📊 Imagen descargada, tamaño: " + imageBytes.length + " bytes");

                        String imagePath = guardarImagenBytesEnDispositivo(imageBytes, s3Key);

                        if (imagePath != null) {
                            Log.d(TAG, "✅ Imagen guardada en: " + imagePath);
                            callback.onSuccess(imagePath);
                        } else {
                            callback.onError("Error guardando imagen en dispositivo");
                        }
                    } else {
                        Log.e(TAG, "❌ Error descargando imagen: " + response.code());
                        callback.onError("Error descargando imagen: " + response.code());
                    }
                } catch (Exception e) {
                    Log.e(TAG, "❌ Error procesando descarga URL: " + e.getMessage());
                    callback.onError("Error procesando descarga: " + e.getMessage());
                }
            }
        });
    }

    private String guardarImagenBytesEnDispositivo(byte[] imageBytes, String fileName) {
        try {
            // Usar MediaStore para guardar en la galería pública
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                // Android 10+ (API 29+) - Usar MediaStore
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DISPLAY_NAME, "Lab7_" + System.currentTimeMillis() + ".jpg");
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
                values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/Laboratorio7");

                Uri uri = context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

                if (uri != null) {
                    OutputStream outputStream = context.getContentResolver().openOutputStream(uri);
                    outputStream.write(imageBytes);
                    outputStream.close();

                    Log.d(TAG, "💾 Imagen guardada en galería: " + uri.toString());
                    return uri.toString();
                }
            } else {
                // Android 9 y anteriores - Usar carpeta Pictures
                File picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                File lab7Dir = new File(picturesDir, "Laboratorio7");

                if (!lab7Dir.exists()) {
                    lab7Dir.mkdirs();
                }

                String imageName = "Lab7_" + System.currentTimeMillis() + ".jpg";
                File imageFile = new File(lab7Dir, imageName);

                FileOutputStream fos = new FileOutputStream(imageFile);
                fos.write(imageBytes);
                fos.close();

                // Notificar a la galería que hay una nueva imagen
                MediaScannerConnection.scanFile(context, new String[]{imageFile.getAbsolutePath()},
                        new String[]{"image/jpeg"}, null);

                Log.d(TAG, "💾 Imagen guardada en: " + imageFile.getAbsolutePath());
                return imageFile.getAbsolutePath();
            }

        } catch (Exception e) {
            Log.e(TAG, "❌ Error guardando imagen en galería: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }


    // Métodos auxiliares

    private void inicializarConexion() {
        client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
        Log.d(TAG, "🔧 Cliente HTTP inicializado");
    }

    private String convertirBitmapABase64(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
        byte[] imageBytes = baos.toByteArray();
        return Base64.encodeToString(imageBytes, Base64.DEFAULT);
    }

    private String guardarImagenEnDispositivo(String base64Data, String fileName) {
        try {
            // Decodificar Base64
            byte[] imageBytes = Base64.decode(base64Data, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);

            // Crear directorio si no existe
            File directory = new File(context.getFilesDir(), "imagenes_descargadas");
            if (!directory.exists()) {
                directory.mkdirs();
            }

            // Crear archivo
            String imageName = "downloaded_" + fileName + ".jpg";
            File imageFile = new File(directory, imageName);

            // Guardar imagen
            FileOutputStream fos = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            fos.close();

            Log.d(TAG, "💾 Imagen guardada en: " + imageFile.getAbsolutePath());
            return imageFile.getAbsolutePath();

        } catch (Exception e) {
            Log.e(TAG, "❌ Error guardando imagen: " + e.getMessage());
            return null;
        }
    }

    /**
     * Método helper para extraer S3 key de una URL de descarga
     */
    /**
     * Método helper para extraer S3 key de una URL de descarga
     */
    public static String extraerS3KeyDeUrl(String downloadUrl) {
        try {
            System.out.println("🔍 URL original: " + downloadUrl);

            // Buscar el patrón después de .amazonaws.com/
            if (downloadUrl.contains(".amazonaws.com/")) {
                String[] parts = downloadUrl.split(".amazonaws.com/");
                if (parts.length > 1) {
                    String keyPart = parts[1];
                    // Remover parámetros de query (?AWSAccessKeyId...)
                    if (keyPart.contains("?")) {
                        keyPart = keyPart.split("\\?")[0];
                    }
                    System.out.println("🔑 S3 Key extraída: " + keyPart);
                    return keyPart;
                }
            }

            // Fallback: buscar patrón laboratorio7/
            if (downloadUrl.contains("laboratorio7/")) {
                int startIndex = downloadUrl.indexOf("laboratorio7/");
                String keyPart = downloadUrl.substring(startIndex);
                // Remover parámetros de query
                if (keyPart.contains("?")) {
                    keyPart = keyPart.split("\\?")[0];
                }
                System.out.println("🔑 S3 Key extraída (fallback): " + keyPart);
                return keyPart;
            }

            System.out.println("⚠️ No se pudo extraer S3 key, retornando URL original");
            return downloadUrl;

        } catch (Exception e) {
            System.out.println("❌ Error extrayendo S3 key: " + e.getMessage());
            return downloadUrl;
        }
    }
}