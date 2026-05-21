package com.ldaniel1505.lpzrecords.data.network

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.postgrest.Postgrest

object SupabaseClient {

    val client = createSupabaseClient(
        // Tu URL real armada con tu Project ID
        supabaseUrl = "https://bdamfhopgzjhdnqfvdua.supabase.co",

        // Aquí pega tu llave larga (la que copiaste de sb_publishable_...)
        supabaseKey = "sb_publishable_h39QiEizmoyZa6pZATFqEQ_A3OWyBE3"
    ) {
        // En versiones estables se instala usando la clase directa con mayúscula
        install(Auth)
        install(Postgrest)
    }
}