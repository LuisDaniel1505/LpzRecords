package com.ldaniel1505.lpzrecords.viewmodel.catalog

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ldaniel1505.lpzrecords.data.model.Category
import com.ldaniel1505.lpzrecords.data.network.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CategoryViewModel: ViewModel() {

    var categories by mutableStateOf<List<Category>>(emptyList())
        private set
    var errorMessage by mutableStateOf<String?>(null)

    fun loadCategories(){
        viewModelScope.launch {
            try {
                val result = withContext(Dispatchers.IO){
                    SupabaseClient.client.from("categories").select().decodeList<Category>()
                }
                categories = result

            }catch(e: Exception){
                errorMessage = "Error: No se pudo cargar las Categorias + ${e.localizedMessage}"
            }
        }
    }
}