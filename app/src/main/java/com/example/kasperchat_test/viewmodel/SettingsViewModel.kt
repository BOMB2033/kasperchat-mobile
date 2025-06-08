package com.example.kasperchat_test.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
) : ViewModel() {
    private val _isLoggedOut = MutableLiveData<Boolean>()
    val isLoggedOut: LiveData<Boolean> = _isLoggedOut // TODO Реализовать настройки профиля

    fun logout() {
//        loginViewModel.clearAuthToken()
        _isLoggedOut.postValue(true)
    }
}