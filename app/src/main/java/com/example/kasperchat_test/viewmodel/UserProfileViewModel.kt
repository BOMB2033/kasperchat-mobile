package com.example.kasperchat_test.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kasperchat_test.api.ApiService
import com.example.kasperchat_test.model.UserProfile
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserProfileViewModel @Inject constructor( //TODO реализовать настройку профиля
    private val apiService: ApiService,
    private val gson: Gson
) : ViewModel() {
    private val _userProfile = MutableLiveData<UserProfile?>()
    val userProfile: LiveData<UserProfile?> = _userProfile

    private val _users = MutableLiveData<List<UserProfile>>()
    val users: LiveData<List<UserProfile>> = _users

    fun fetchCurrentUserProfile() {
        viewModelScope.launch {
            try {
                val response = apiService.getCurrentUserProfile()
                if (response.isSuccessful && response.body() != null) {
                    _userProfile.postValue(response.body()!!)
                }
            } catch (e: Exception) {
                // Обработка ошибки
            }
        }
    }

    fun searchUsers() {
        viewModelScope.launch {
            try {
                val response = apiService.getUsers()
                if (response.isSuccessful && response.body() != null) {
                    _users.postValue(response.body()!!)
                }
            } catch (e: Exception) {
                // Обработка ошибки
            }
        }
    }
}