package com.mora.matritech.data.repository

import com.mora.matritech.model.Institucion
import com.mora.matritech.utils.putNullable
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put


class InstitucionRepository (
    private val supabaseClient: SupabaseClient
) {
    companion object {
        private const val TABLE_NAME = "instituciones"
    }

    suspend fun getAllInstituciones(): Result<List<Institucion>> = withContext(Dispatchers.IO) {
        try {
            val instituciones = supabaseClient
                .from(TABLE_NAME)
                .select()
                .decodeList<Institucion>()

            Result.success(instituciones)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getInstitucionById(id: String): Result<Institucion?> = withContext(Dispatchers.IO) {
        try {
            val institucion = supabaseClient
                .from(TABLE_NAME)
                .select {
                    filter {
                        eq("id", id)
                    }
                }
                .decodeSingleOrNull<Institucion>()

            Result.success(institucion)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createInstitucion(institucion: Institucion): Result<Institucion> = withContext(Dispatchers.IO) {
        try {
            val json = buildJsonObject {
                put("nombre_institucion", institucion.nombre)
                putNullable("provincia", institucion.provincia)
                putNullable("direccion_especifica", institucion.direccion)
                putNullable("codigo_identificacion", institucion.codigoIdentificacion)
                putNullable("ano_laboracion", institucion.anoLaboracion)
                putNullable("tipo_institucion", institucion.tipoInstitucion)
                putNullable("nivel_educativo", institucion.nivelEducativo)
                putNullable("regimen", institucion.regimen)
                putNullable("contacto", institucion.contacto)
            }

            val created = supabaseClient
                .from(TABLE_NAME)
                .insert(json) {
                    select()
                }
                .decodeSingle<Institucion>()

            Result.success(created)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    suspend fun updateInstitucion(institucion: Institucion): Result<Institucion> = withContext(Dispatchers.IO) {
        try {
            val updated = supabaseClient
                .from(TABLE_NAME)
                .update(institucion) {
                    filter {
                        eq("id", institucion.id!!)
                    }
                    select()
                }
                .decodeSingle<Institucion>()

            Result.success(updated)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteInstitucion(id: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            supabaseClient
                .from(TABLE_NAME)
                .delete {
                    filter {
                        eq("id", id)
                    }
                }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Soft delete - solo cambiar el estado activo
    suspend fun toggleActivo(id: String, activo: Boolean): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            supabaseClient
                .from(TABLE_NAME)
                .update({
                    set("activo", activo)
                }) {
                    filter {
                        eq("id", id)
                    }
                }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}