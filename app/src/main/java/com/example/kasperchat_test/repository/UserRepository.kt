package com.example.kasperchat_test.repository

import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.edit
import com.example.kasperchat_test.api.ApiService
import com.example.kasperchat_test.model.UpdateUserProfileRequest
import com.example.kasperchat_test.model.UserProfile
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val apiService: ApiService,
    private val prefs: SharedPreferences,
    private val gson: Gson
) {

    companion object {
        private const val USER_PROFILE_KEY = "user_profile"
    }

    // StateFlow, чтобы UI мог реактивно обновляться при изменении профиля
    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile

    init {
        // При создании репозитория загружаем профиль из SharedPreferences
        loadProfileFromPrefs()
    }

    private fun loadProfileFromPrefs() {
        val json = prefs.getString(USER_PROFILE_KEY, null)
        if (json != null) {
            try {
                _userProfile.value = gson.fromJson(json, UserProfile::class.java)
                Log.i("UserRepository", "User profile loaded from cache.")
            } catch (e: Exception) {
                Log.e("UserRepository", "Failed to parse cached user profile.", e)
                clearProfile() // Очищаем поврежденные данные
            }
        }
    }

    suspend fun fetchCurrentUserProfile(): Result<UserProfile> {
        return try {
            val response = apiService.getCurrentUserProfile()
            if (response.isSuccessful && response.body() != null) {
                val profile = response.body()!!
                saveProfile(profile) // Сохраняем и в кеш, и в StateFlow
                Result.success(profile)
            } else {
                // Если не авторизован, очищаем локальные данные
                if (response.code() == 401) {
                    clearProfile()
                }
                Result.failure(Exception("Failed to fetch profile: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateUserProfile(fullName: String, bio: String): Result<UserProfile> {
        return try {
            val request = UpdateUserProfileRequest(
                fullName = fullName,
                bio = bio,
                avatarUrl = _userProfile.value?.avatarUrl
            )
            val response = apiService.updateUserProfile(request)
            if (response.isSuccessful && response.body() != null) {
                val updatedProfile = response.body()!!
                saveProfile(updatedProfile) // Сохраняем обновленный профиль
                Result.success(updatedProfile)
            } else {
                Result.failure(Exception("Failed to update profile: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Сохраняет профиль в StateFlow и SharedPreferences
    fun saveProfile(profile: UserProfile) {
        _userProfile.value = profile
        try {
            val json = gson.toJson(profile)
            prefs.edit { putString(USER_PROFILE_KEY, json) }
        } catch (e: Exception) {
            Log.e("UserRepository", "Failed to save profile to cache.", e)
        }
    }

    // Очищает профиль
    fun clearProfile() {
        _userProfile.value = null
        prefs.edit { remove(USER_PROFILE_KEY) }
    }
}