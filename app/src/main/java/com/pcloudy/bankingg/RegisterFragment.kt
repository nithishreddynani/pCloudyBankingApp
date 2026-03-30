package com.pcloudy.bankingg

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.pcloudy.bankingg.databinding.FragmentRegisterBinding
import kotlinx.coroutines.launch

class RegisterFragment : Fragment() {
    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonRegister.setOnClickListener { handleRegister() }
        binding.textLoginLink.setOnClickListener { findNavController().navigateUp() }
    }

    private fun handleRegister() {
        val name     = binding.editName.text.toString().trim()
        val username = binding.editUsername.text.toString().trim()
        val password = binding.editPassword.text.toString()
        val confirm  = binding.editConfirmPassword.text.toString()

        if (!validate(name, username, password, confirm)) return

        lifecycleScope.launch {
            binding.progressBar.visibility = View.VISIBLE
            binding.buttonRegister.isEnabled = false
            try {
                val response = viewModel.registerUser(username, password, name)
                Snackbar.make(
                    binding.root,
                    "Account created! Welcome, ${response.user.name} (${response.user.accountNo})",
                    Snackbar.LENGTH_LONG
                ).show()
                // Navigate to dashboard, clear back stack so user can't go back to register
                findNavController().navigate(R.id.action_register_to_dashboard)
            } catch (e: Exception) {
                Snackbar.make(binding.root, e.message ?: "Registration failed", Snackbar.LENGTH_LONG).show()
            } finally {
                binding.progressBar.visibility = View.GONE
                binding.buttonRegister.isEnabled = true
            }
        }
    }

    private fun validate(name: String, username: String, password: String, confirm: String): Boolean {
        binding.nameLayout.error = null
        binding.usernameLayout.error = null
        binding.passwordLayout.error = null
        binding.confirmPasswordLayout.error = null

        if (name.isBlank()) {
            binding.nameLayout.error = "Full name is required"
            return false
        }
        if (username.isBlank()) {
            binding.usernameLayout.error = "Username is required"
            return false
        }
        if (password.length < 6) {
            binding.passwordLayout.error = "Password must be at least 6 characters"
            return false
        }
        if (password != confirm) {
            binding.confirmPasswordLayout.error = "Passwords do not match"
            return false
        }
        return true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
