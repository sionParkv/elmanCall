package com.elmansoft.elmancall.ui.change

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ChangeViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is Change Fragment"
    }
    val text: LiveData<String> = _text
}