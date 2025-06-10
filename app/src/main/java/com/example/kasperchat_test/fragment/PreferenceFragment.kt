package com.example.kasperchat_test.fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.kasperchat_test.GlideApp
import com.example.kasperchat_test.R
import com.example.kasperchat_test.databinding.FragmentPreferenceBinding
import com.example.kasperchat_test.viewmodel.LoginViewModel
import com.example.kasperchat_test.viewmodel.UpdateResult
import com.example.kasperchat_test.viewmodel.UserProfileViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PreferenceFragment : Fragment() {

    private var _binding: FragmentPreferenceBinding? = null
    private val binding get() = _binding!!

    private val userProfileViewModel: UserProfileViewModel by viewModels()
    private val loginViewModel: LoginViewModel by viewModels()

    // Переменные для хранения исходных значений
    private var initialFullName: String? = null
    private var initialBio: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPreferenceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Изначально скрываем кнопку сохранения
        binding.buttonSave.visibility = View.GONE
        setupUI()
        observeViewModel()
        setupTextWatchers()
    }

    private fun setupUI() {
        binding.toolbar.setNavigationOnClickListener { findNavController().popBackStack() }

        binding.buttonSave.setOnClickListener {
            val fullName = binding.editTextFullName.text.toString()
            val bio = binding.editTextBio.text.toString()
            userProfileViewModel.saveUserProfile(fullName, bio)
        }
        binding.buttonLogout.setOnClickListener {
            loginViewModel.clearAuthToken()
            // Используйте navigate для перехода к LoginFragment и popUpTo для очистки стека до него
            findNavController().navigate(R.id.loginFragment)
            findNavController().popBackStack(R.id.loginFragment, false)

        }
    }

    private fun observeViewModel() {
        userProfileViewModel.fetchCurrentUserProfile()

        userProfileViewModel.userProfile.observe(viewLifecycleOwner) { user ->
            user?.let {
                // Сохраняем исходные значения, если они еще не установлены
                if (initialFullName == null) {
                    initialFullName = it.fullName
                }
                if (initialBio == null) {
                    initialBio = it.bio
                }

                binding.editTextFullName.setText(it.fullName)
                binding.editTextBio.setText(it.bio)
                Log.d("PreferenceFragment", "User profile loaded: $it")
                context?.let { context ->
                    GlideApp.with(context)
                        .load(it.avatarUrl)
                        .placeholder(R.drawable.ic_avatar)
                        .error(R.drawable.ic_avatar)
                        .circleCrop()
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .into(binding.imageViewAvatar)
                }
                Log.d("PreferenceFragment", "User avatar loaded: ${it.avatarUrl}")
                // Нет необходимости вызывать fetchCurrentUserProfile() здесь снова,
                // так как данные уже загружены и отображены.
            }
        }

        userProfileViewModel.updateResult.observe(viewLifecycleOwner) { result ->
            binding.progressBar.isVisible = result is UpdateResult.Loading
            // Блокируем кнопку сохранения только во время загрузки, но видимость управляется отдельно
            binding.buttonSave.isEnabled = result !is UpdateResult.Loading

            when (result) {
                is UpdateResult.Success -> {
                    Toast.makeText(context, "Профиль успешно сохранен", Toast.LENGTH_SHORT).show()
                    // После успешного сохранения обновляем initialValues
                    initialFullName = binding.editTextFullName.text.toString()
                    initialBio = binding.editTextBio.text.toString()
                    updateSaveButtonVisibility() // Обновляем видимость кнопки (она должна скрыться)
                    // findNavController().popBackStack() // Можно оставить, если нужно уходить с экрана
                    userProfileViewModel.onResultHandled()
                }
                is UpdateResult.Error -> {
                    Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
                    userProfileViewModel.onResultHandled()
                }
                else -> { /* Idle, Loading */ }
            }
        }
    }

    private fun setupTextWatchers() {
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                updateSaveButtonVisibility()
            }
        }
        binding.editTextFullName.addTextChangedListener(textWatcher)
        binding.editTextBio.addTextChangedListener(textWatcher)
    }

    private fun updateSaveButtonVisibility() {
        val currentFullName = binding.editTextFullName.text.toString()
        val currentBio = binding.editTextBio.text.toString()

        // Показываем кнопку, если хотя бы одно поле отличается от исходного
        // и исходные значения были загружены (не null)
        val hasChanges = (initialFullName != null && currentFullName != initialFullName) ||
                (initialBio != null && currentBio != initialBio)

        binding.buttonSave.visibility = if (hasChanges) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Сбрасываем исходные значения при уничтожении View,
        // чтобы при следующем входе они корректно загрузились
        initialFullName = null
        initialBio = null
        _binding = null
    }
}