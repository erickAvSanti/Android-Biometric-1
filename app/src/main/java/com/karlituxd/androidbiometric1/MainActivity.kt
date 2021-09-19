package com.karlituxd.androidbiometric1

import android.app.Activity
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.*
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import java.util.concurrent.Executor

class MainActivity : AppCompatActivity() {
    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo
    private lateinit var promptInfoBuilder: BiometricPrompt.PromptInfo.Builder

    private var biometricOrDeviceCredentialAvailable = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val biometricManager = BiometricManager.from(this)
        var message: String
        val availableRules = BIOMETRIC_STRONG or DEVICE_CREDENTIAL or BIOMETRIC_WEAK
        when (val res = biometricManager.canAuthenticate(availableRules)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                message = "App can authenticate using biometrics or device credential"
                biometricOrDeviceCredentialAvailable = true
            }

            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE ->
                message = "No biometric features available on this device."
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE ->
                message ="Biometric features are currently unavailable."
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                message = "Biometric and device credential features are currently unavailable"
                // Prompts the user to create credentials that your app accepts.
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    val enrollIntent = Intent(Settings.ACTION_BIOMETRIC_ENROLL).apply {
                        putExtra(
                            Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
                            BIOMETRIC_STRONG or DEVICE_CREDENTIAL
                        )
                    }
                    val startForResult =
                        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                            if (result.resultCode == Activity.RESULT_OK) {
                                //TODO
                            }
                        }
                    startForResult.launch(enrollIntent)
                }
            }
            else -> {
                message = "No biometric or device credentials properties defined, status = $res"
            }
        }
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        if(biometricOrDeviceCredentialAvailable){
            executor = ContextCompat.getMainExecutor(this)
            biometricPrompt = BiometricPrompt(this, executor,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationError(errorCode: Int,
                                                       errString: CharSequence) {
                        super.onAuthenticationError(errorCode, errString)
                        Toast.makeText(applicationContext,
                            "Authentication error: $errString", Toast.LENGTH_SHORT)
                            .show()

                    }

                    override fun onAuthenticationSucceeded(
                        result: BiometricPrompt.AuthenticationResult) {
                        super.onAuthenticationSucceeded(result)
                        Toast.makeText(applicationContext,
                            "Authentication succeeded!", Toast.LENGTH_SHORT)
                            .show()
                    }

                    override fun onAuthenticationFailed() {
                        super.onAuthenticationFailed()
                        Toast.makeText(applicationContext, "Authentication failed",
                            Toast.LENGTH_SHORT)
                            .show()
                    }
                })

            promptInfoBuilder = BiometricPrompt.PromptInfo.Builder()
                .setTitle("Biometric login for my app")
                .setSubtitle("Log in using your biometric credential")
                .setAllowedAuthenticators(availableRules)
            promptInfo = promptInfoBuilder.build()

            val biometricLoginButton =
                findViewById<Button>(R.id.biometric_login)
            biometricLoginButton.setOnClickListener {
                biometricPrompt.authenticate(promptInfo)
            }
        }
    }
}