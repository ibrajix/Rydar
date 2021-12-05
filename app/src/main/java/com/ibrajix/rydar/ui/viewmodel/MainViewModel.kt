package com.ibrajix.rydar.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ibrajix.rydar.data.MainRepo
import com.ibrajix.rydar.data.Resource
import com.ibrajix.rydar.response.SearchPlaceResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(private val mainRepo: MainRepo) : ViewModel() {


    //register user
    private val _searchLocation = Channel<Resource<SearchPlaceResponse>>(Channel.BUFFERED)
    val searchLocation = _searchLocation.receiveAsFlow()

    fun doSearchLocation(input: String) {
        viewModelScope.launch {
            mainRepo.searchLocation(input)
                .catch { e ->
                    _searchLocation.send(Resource.error(e.toString()))
                }
                .collect {
                    _searchLocation.send(it)
                }
         }
    }


}