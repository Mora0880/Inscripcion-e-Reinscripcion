package com.mora.matritech.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.mora.matritech.data.remote.supabase
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.storage.storage
import kotlinx.serialization.Serializable
import java.util.UUID

/**
 * Repository para gestionar inscripciones de estudiantes
 * Maneja la lógica de negocio con Supabase (Database + Storage)
 */
class EnrollmentRepository(private val context: Context) {

    companion object {
        private const val TAG = "EnrollmentRepository"
        private const val BUCKET_NAME = "documentos-inscripciones"
    }

    /**
     * Crea una nueva inscripción completa
     */
    suspend fun createEnrollment(
        nombre: String,
        apellido: String,
        documentoIdentidad: String,
        fechaNacimiento: String,
        telefono: String,
        email: String,
        dniUri: Uri,
        actaNacimientoUri: Uri,
        certificadoAcademicoUri: Uri,
        userId: String
    ): EnrollmentResult {
        return try {
            Log.d(TAG, "🔵 Iniciando inscripción para usuario: $userId")

            // 1. Crear inscripción
            val inscripcion = Inscripcion(
                usuario_id = userId,
                nombre = nombre,
                apellido = apellido,
                documento_identidad = documentoIdentidad,
                fecha_nacimiento = fechaNacimiento,
                telefono = telefono,
                email = email,
                estado = "pendiente"
            )

            val inscripcionCreada = supabase.from("inscripciones")
                .insert(inscripcion) {
                    select()
                }
                .decodeSingle<Inscripcion>()

            val inscripcionId = inscripcionCreada.id
                ?: throw Exception("No se pudo obtener el ID de la inscripción")

            // 2. Subir documentos
            val documentos = mutableListOf<Documento>()

            val dniUrl = uploadDocument(dniUri, userId, inscripcionId, "dni")
            documentos.add(
                Documento(
                    inscripcion_id = inscripcionId,
                    tipo = "dni",
                    nombre_archivo = "dni_$inscripcionId.jpg",
                    url_storage = dniUrl,
                    subido_por = userId
                )
            )

            val actaUrl = uploadDocument(actaNacimientoUri, userId, inscripcionId, "acta_nacimiento")
            documentos.add(
                Documento(
                    inscripcion_id = inscripcionId,
                    tipo = "acta_nacimiento",
                    nombre_archivo = "acta_$inscripcionId.jpg",
                    url_storage = actaUrl,
                    subido_por = userId
                )
            )

            val certificadoUrl =
                uploadDocument(certificadoAcademicoUri, userId, inscripcionId, "certificado_academico")
            documentos.add(
                Documento(
                    inscripcion_id = inscripcionId,
                    tipo = "certificado_academico",
                    nombre_archivo = "certificado_$inscripcionId.jpg",
                    url_storage = certificadoUrl,
                    subido_por = userId
                )
            )

            // 3. Guardar documentos en BD
            supabase.from("documentos").insert(documentos)

            EnrollmentResult.Success(inscripcionId)

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error creando inscripción", e)
            EnrollmentResult.Error(e.message ?: "Error desconocido")
        }
    }

    suspend fun getAllEnrollments(): List<Inscripcion> {
        return try {
            supabase.from("inscripciones")
                .select {
                    order("fecha_solicitud", Order.DESCENDING)
                }
                .decodeList<Inscripcion>()
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al obtener todas las inscripciones", e)
            emptyList()
        }
    }

    /**
     * Sube un documento a Supabase Storage
     */
    private suspend fun uploadDocument(
        uri: Uri,
        userId: String,
        inscripcionId: String,
        tipoDocumento: String
    ): String {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: throw Exception("No se pudo abrir el archivo")

            val bytes = inputStream.readBytes()
            inputStream.close()

            val extension = getFileExtension(uri)
            val fileName = "${tipoDocumento}_${UUID.randomUUID()}.$extension"
            val storagePath = "$userId/$inscripcionId/$fileName"

            val bucket = supabase.storage.from(BUCKET_NAME)
            bucket.upload(storagePath, bytes)

            return bucket.publicUrl(storagePath)

        } catch (e: Exception) {
            throw Exception("Error subiendo $tipoDocumento: ${e.message}")
        }
    }

    private fun getFileExtension(uri: Uri): String {
        val mimeType = context.contentResolver.getType(uri)
        return when {
            mimeType?.contains("jpeg") == true || mimeType?.contains("jpg") == true -> "jpg"
            mimeType?.contains("png") == true -> "png"
            mimeType?.contains("pdf") == true -> "pdf"
            else -> "jpg"
        }
    }

    /**
     * Inscripciones del usuario
     */
    suspend fun getUserEnrollments(userId: String): List<Inscripcion> {
        return try {
            supabase.from("inscripciones")
                .select {
                    filter {
                        eq("usuario_id", userId)
                    }
                    order("fecha_solicitud", Order.DESCENDING)
                }
                .decodeList<Inscripcion>()
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Inscripción por ID
     */
    suspend fun getEnrollmentById(inscripcionId: String): Inscripcion? {
        return try {
            supabase.from("inscripciones")
                .select {
                    filter {
                        eq("id", inscripcionId)
                    }
                }
                .decodeSingle<Inscripcion>()
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Documentos de una inscripción
     */
    suspend fun getEnrollmentDocuments(inscripcionId: String): List<Documento> {
        return try {
            supabase.from("documentos")
                .select {
                    filter {
                        eq("inscripcion_id", inscripcionId)
                    }
                }
                .decodeList<Documento>()
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Inscripciones pendientes (admin)
     */
    suspend fun getPendingEnrollments(): List<Inscripcion> {
        return try {
            supabase.from("inscripciones")
                .select {
                    filter {
                        eq("estado", "pendiente")
                    }
                    order("fecha_solicitud", Order.DESCENDING)
                }
                .decodeList<Inscripcion>()
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Aprobar inscripción - CORREGIDO ✅
     */
    suspend fun approveEnrollment(inscripcionId: String, adminId: String): EnrollmentResult {
        return try {
            Log.d(TAG, "🟢 Aprobando inscripción ID: $inscripcionId")
            Log.d(TAG, "👤 Admin ID: $adminId")

            // Crear el mapa de actualización
            val updateData = mapOf(
                "estado" to "aprobada",
                "revisado_por" to adminId,
                "fecha_revision" to "now()"
            )

            // Realizar el update
            supabase.from("inscripciones")
                .update(updateData) {
                    filter {
                        eq("id", inscripcionId)
                    }
                }

            Log.d(TAG, "✅ Inscripción aprobada exitosamente")
            EnrollmentResult.Success(inscripcionId)

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al aprobar inscripción", e)
            Log.e(TAG, "Detalle: ${e.message}")
            Log.e(TAG, "Stack trace: ${e.stackTraceToString()}")
            EnrollmentResult.Error(e.message ?: "Error al aprobar")
        }
    }

    /**
     * Rechazar inscripción - CORREGIDO ✅
     */
    suspend fun rejectEnrollment(
        inscripcionId: String,
        adminId: String,
        motivo: String
    ): EnrollmentResult {
        return try {
            Log.d(TAG, "🔴 Rechazando inscripción ID: $inscripcionId")
            Log.d(TAG, "👤 Admin ID: $adminId")
            Log.d(TAG, "📝 Motivo: $motivo")

            // Crear el mapa de actualización
            val updateData = mapOf(
                "estado" to "rechazada",
                "motivo_rechazo" to motivo,
                "revisado_por" to adminId,
                "fecha_revision" to "now()"
            )

            // Realizar el update
            supabase.from("inscripciones")
                .update(updateData) {
                    filter {
                        eq("id", inscripcionId)
                    }
                }

            Log.d(TAG, "✅ Inscripción rechazada exitosamente")
            EnrollmentResult.Success(inscripcionId)

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al rechazar inscripción", e)
            Log.e(TAG, "Detalle: ${e.message}")
            Log.e(TAG, "Stack trace: ${e.stackTraceToString()}")
            EnrollmentResult.Error(e.message ?: "Error al rechazar")
        }
    }
}

// ============================ MODELOS ============================

@Serializable
data class Inscripcion(
    val id: String? = null,
    val usuario_id: String,
    val estudiante_id: Int? = null,
    val nombre: String,
    val apellido: String,
    val documento_identidad: String,
    val fecha_nacimiento: String,
    val telefono: String,
    val email: String,
    val estado: String = "pendiente",
    val motivo_rechazo: String? = null,
    val fecha_solicitud: String? = null,
    val fecha_revision: String? = null,
    val revisado_por: String? = null,
    val created_at: String? = null,
    val updated_at: String? = null
)

@Serializable
data class Documento(
    val id: String? = null,
    val inscripcion_id: String,
    val tipo: String,
    val nombre_archivo: String,
    val url_storage: String,
    val tamano_bytes: Long? = null,
    val mime_type: String? = null,
    val subido_por: String? = null,
    val fecha_subida: String? = null,
    val created_at: String? = null
)

sealed class EnrollmentResult {
    data class Success(val inscripcionId: String) : EnrollmentResult()
    data class Error(val message: String) : EnrollmentResult()
}