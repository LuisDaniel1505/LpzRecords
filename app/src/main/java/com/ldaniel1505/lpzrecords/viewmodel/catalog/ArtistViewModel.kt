package com.ldaniel1505.lpzrecords.viewmodel.catalog

import androidx.collection.emptyIntList
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ldaniel1505.lpzrecords.data.model.Artist
import com.ldaniel1505.lpzrecords.data.model.Product
import com.ldaniel1505.lpzrecords.data.network.SupabaseClient
import com.patrykandpatrick.vico.compose.cartesian.axis.VerticalAxis.ItemPlacer.Companion.count
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Optional.empty

class ArtistViewModel : ViewModel(){


    var artists by mutableStateOf<List<Artist>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var selectArtist by mutableStateOf<Artist?>(null)
    fun loadArtist() {

        viewModelScope.launch {
            errorMessage = ""
            try {
                val result = withContext(Dispatchers.IO) {
                    SupabaseClient.client.from("artists").select().decodeList<Artist>()
                }
                artists = result
            } catch (e: Exception) {
                errorMessage = "Error: No se pudo cargar los Artistas + ${e.localizedMessage}"
            }
        }
    }

    fun loadArtistById(artistId: Int) {
        isLoading = true
        errorMessage = null

        viewModelScope.launch {
            try {
                val columns = Columns.raw("""
                id,
                name,
                biography,
                musical_genre
            """.trimIndent())

                val result = withContext(Dispatchers.IO) {
                    SupabaseClient.client
                        .from("artists")
                        .select(columns = columns){
                            filter {
                                eq("id", artistId)
                            }
                        }
                        .decodeSingle<Artist>()
                }

                selectArtist = result

            } catch (e: Exception) {
                errorMessage = "Error al cargar al artista: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
    }
}