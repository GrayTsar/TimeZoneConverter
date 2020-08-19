package com.graytsar.timezoneconverter

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ViewModelMain: ViewModel() {
    val currentLongName:MutableLiveData<String> = MutableLiveData<String>()
    val currentId:MutableLiveData<String> = MutableLiveData<String>()
    val currentTime:MutableLiveData<String> = MutableLiveData<String>()

    val diffTime:MutableLiveData<String> = MutableLiveData<String>()

    val selectedLongName:MutableLiveData<String> = MutableLiveData<String>()
    val selectedId:MutableLiveData<String> = MutableLiveData<String>()
    val selectedOffset:MutableLiveData<String> = MutableLiveData<String>()
}