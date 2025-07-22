// File: viewmodel/UserProfileViewModel.kt
package com.fvk_solutions.kasperchat.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.fvk_solutions.kasperchat.model.UserProfile
import com.fvk_solutions.kasperchat.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

sealed class UpdateResult {
    data object Success : UpdateResult()
    data class Error(val message: String) : UpdateResult()
    data object Loading : UpdateResult()
    data object Idle : UpdateResult()
}
sealed class LoadAvatarResult {
    data class Success(val url: String) : LoadAvatarResult()
    data class Error(val message: String) : LoadAvatarResult()
    data object Loading : LoadAvatarResult()
    data object Idle : LoadAvatarResult()
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
    private val application: Application,
    private val userRepository: UserRepository // Внедряем ТОЛЬКО репозиторий
) : AndroidViewModel(application) {

    // Получаем профиль из репозитория
    val userProfile: LiveData<UserProfile?> = userRepository.userProfile.asLiveData()

    private val _updateResult = MutableLiveData<UpdateResult>(UpdateResult.Idle)
    val updateResult: LiveData<UpdateResult> = _updateResult

    private val _loadAvatarResult = MutableLiveData<LoadAvatarResult>(LoadAvatarResult.Idle)
    val loadAvatarResult: LiveData<LoadAvatarResult> = _loadAvatarResult

    private val _fetchResult = MutableLiveData<FetchResult>(FetchResult.Idle)
    val fetchResult: LiveData<FetchResult> = _fetchResult


    fun uploadAndSaveAvatar(file: File) {
        _loadAvatarResult.value = LoadAvatarResult.Loading
        viewModelScope.launch {
            // Шаг 1: Загрузить файл на сервер и получить URL
            val uploadResult = userRepository.uploadAvatar(file)

            uploadResult.onSuccess { newAvatarUrl ->
                // Шаг 2: Если загрузка успешна, обновить профиль пользователя с новым URL
                val currentUser = userProfile.value
                val updateProfileResult = userRepository.updateUserProfile(
                    fullName = currentUser?.fullName ?: "",
                    bio = currentUser?.bio ?: "",
                    avatarUrl = newAvatarUrl // Используем новый URL
                )

                updateProfileResult.onSuccess {
                    _loadAvatarResult.postValue(LoadAvatarResult.Success(newAvatarUrl))
                    // Обновление профиля в репозитории уже произошло, LiveData обновится автоматически
                }.onFailure { exception ->
                    _loadAvatarResult.postValue(LoadAvatarResult.Error(exception.message ?: "Failed to update profile with new avatar"))
                }
            }.onFailure { exception ->
                _loadAvatarResult.postValue(LoadAvatarResult.Error(exception.message ?: "Unknown upload error"))
            }
        }
    }

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