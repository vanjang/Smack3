package com.cochipcho.smack3.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class HomeViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
//        value = "This is home Fragment"
        value = ""
    }
    val text: LiveData<String> = _text
}