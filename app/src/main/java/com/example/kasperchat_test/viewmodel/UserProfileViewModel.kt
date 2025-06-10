// File: viewmodel/UserProfileViewModel.kt
package com.example.kasperchat_test.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.kasperchat_test.api.ApiService
import com.example.kasperchat_test.model.UpdateUserProfileRequest
import com.example.kasperchat_test.model.UserProfile
import com.example.kasperchat_test.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class UpdateResult {
    data object Success : UpdateResult()
    data class Error(val message: String) : UpdateResult()
    data object Loading : UpdateResult()
    data object Idle : UpdateResult()
}
sealed class FetchResult {
    data object Success : FetchResult()
    data class Error(val message: String) : FetchResult()
    data object Loading : FetchResult()
    data object Idle : FetchResult()
}
// File: viewmodel/UserProfileViewModel.kt
@HiltViewModel
class UserProfileViewModel @Inject constructor(
    application: Application,
    private val userRepository: UserRepository // Внедряем ТОЛЬКО репозиторий
) : AndroidViewModel(application) {

    // Получаем профиль из репозитория
    val userProfile: LiveData<UserProfile?> = userRepository.userProfile.asLiveData()

    private val _updateResult = MutableLiveData<UpdateResult>(UpdateResult.Idle)
    val updateResult: LiveData<UpdateResult> = _updateResult

    private val _fetchResult = MutableLiveData<FetchResult>(FetchResult.Idle)
    val fetchResult: LiveData<FetchResult> = _fetchResult

    fun saveUserProfile(fullName: String, bio: String) {
        _updateResult.value = UpdateResult.Loading
        viewModelScope.launch {
            val result = userRepository.updateUserProfile(fullName, bio)
            result.onSuccess {
                _updateResult.postValue(UpdateResult.Success)
            }.onFailure { exception ->
                _updateResult.postValue(UpdateResult.Error(exception.message ?: "Unknown error"))
            }
        }
    }

    fun onResultHandled() {
        _updateResult.value = UpdateResult.Idle
    }
    fun onResultFetchHandled(){
        _fetchResult.value = FetchResult.Idle
    }

    fun fetchCurrentUserProfile() {
        _fetchResult.value = FetchResult.Loading
        viewModelScope.launch {
            userRepository.fetchCurrentUserProfile()
                .onSuccess {
                    _fetchResult.postValue(FetchResult.Success)
                }.onFailure { exception ->
                    _fetchResult.postValue(FetchResult.Error(exception.message ?: "Unknown error"))
                }
        }
    }
}