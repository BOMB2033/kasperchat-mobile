package com.example.kasperchat_test.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kasperchat_test.databinding.FragmentMemberManagementBinding

class MemberManagementFragment : Fragment() {

    private var _binding: FragmentMemberManagementBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMemberManagementBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            buttonCloseMemberManagement.setOnClickListener {
                parentFragmentManager.popBackStack()
            }

            rvMembers.layoutManager = LinearLayoutManager(requireContext())
            // Здесь добавьте адаптер для RecyclerView
            // rvMembers.adapter = MembersAdapter(...)

            // Покажите ProgressBar при загрузке
            progressBarMembers.visibility = View.VISIBLE
            // Скрыть ProgressBar после загрузки данных
            // progressBarMembers.visibility = View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}