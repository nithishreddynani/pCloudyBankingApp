package com.pcloudy.bankingg

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.pcloudy.bankingg.databinding.FragmentLoginBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.Executor

class LoginFragment : Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by activityViewModels()

    // Biometric authentication variables
    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().window.decorView.importantForAutofill = View.IMPORTANT_FOR_AUTOFILL_NO_EXCLUDE_DESCENDANTS

        setupViews()
        setupBiometricLogin()
        observeViewModel()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupViews() {
        binding.apply {
            buttonLogin.setOnClickListener { handleTraditionalLogin() }

            editPassword.addTextChangedListener(object : android.text.TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                    // Not needed
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    // Not needed
                }

                override fun afterTextChanged(s: android.text.Editable?) {
                    if (s != null && s.length >= 11) {
                        // Hide keyboard when text length reaches 11 characters
                        val imm = requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE)
                                as android.view.inputmethod.InputMethodManager
                        imm.hideSoftInputFromWindow(editPassword.windowToken, 0)
                    }
                }
            })

            sliderDelay.addOnChangeListener { _, value, _ ->
                viewModel.simulatedDelay = value.toLong()
                textDelay.text = "Delay: ${value.toInt()}ms"
            }

            sliderCrash.addOnChangeListener { _, value, _ ->
                viewModel.crashProbability = value
                textCrash.text = "Crash Probability: ${(value * 100).toInt()}%"
            }
        }
    }

    private fun setupBiometricLogin() {
        // Always show the biometric button
        binding.buttonBiometricLogin.visibility = View.VISIBLE

        // Setup simple click listener to show biometric prompt
        binding.buttonBiometricLogin.setOnClickListener {
            showBiometricPrompt()
        }

        // Setup biometric authentication
        executor = ContextCompat.getMainExecutor(requireContext())
        biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult
                ) {
                    // Biometric authentication successful
                    performBiometricLogin()
                }

                override fun onAuthenticationFailed() {
                    Snackbar.make(
                        binding.root,
                        "Biometric authentication failed",
                        Snackbar.LENGTH_SHORT
                    ).show()
                }

                override fun onAuthenticationError(
                    errorCode: Int,
                    errString: CharSequence
                ) {
                    Snackbar.make(
                        binding.root,
                        "Authentication error: $errString",
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
            })

        // Prompt info for biometric authentication
        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric Login")
            .setSubtitle("Authenticate using your biometric credential")
            .setNegativeButtonText("Cancel")
            .build()
    }

    private fun showBiometricPrompt() {
        biometricPrompt.authenticate(promptInfo)
    }

//    private fun showBiometricPrompt() {
//        biometricPrompt.authenticate(promptInfo)
//    }

    private fun handleTraditionalLogin() {
        val username = binding.editUsername.text.toString()
        val password = binding.editPassword.text.toString()

        // Validate credentials
        if (validateCredentials(username, password)) {
            performTraditionalLogin(username, password)
        }
    }

    private fun validateCredentials(username: String, password: String): Boolean {
        return when {
            username.isEmpty() -> {
                binding.usernameLayout.error = "Username cannot be empty"
                false
            }
            password.isEmpty() -> {
                binding.passwordLayout.error = "Password cannot be empty"
                false
            }
            else -> {
                binding.usernameLayout.error = null
                binding.passwordLayout.error = null
                true
            }
        }
    }

    private fun performTraditionalLogin(username: String, password: String) {
        lifecycleScope.launch {
            try {
                viewModel.simulateApiCall("/auth/login") {
                    if (username == "demo" && password == "password123") {
                        withContext(Dispatchers.Main) {
                            navigateToDashboard(username)
                        }
                    } else {
                        throw Exception("Invalid credentials")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Snackbar.make(binding.root, e.message ?: "Error occurred", Snackbar.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun performBiometricLogin() {
        lifecycleScope.launch {
            try {
                viewModel.simulateApiCall("/auth/login") {
                    withContext(Dispatchers.Main) {
                        navigateToDashboard("Biometric User")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Snackbar.make(binding.root, e.message ?: "Error occurred", Snackbar.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun navigateToDashboard(username: String) {
        try {
            // Try with SafeArgs
            val action = LoginFragmentDirections.actionLoginToDashboard(username)
            findNavController().navigate(action)
        } catch (e: Exception) {
            // Fallback to direct navigation
            findNavController().navigate(R.id.action_login_to_dashboard)
        }
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
            }
        }
    }
}