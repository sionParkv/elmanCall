package com.elmansoft.elmancall.ui.active

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ActiveViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is active Fragment"
    }
    val text: LiveData<String> = _text
}