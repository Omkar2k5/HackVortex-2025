package com.example.smartfianacetracker

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import com.example.smartfianacetracker.databinding.ActivityMainBinding
import com.example.smartfianacetracker.activities.LoginActivity
import com.example.smartfianacetracker.utils.FirebaseManager
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.FirebaseApp
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var isServiceRunning = false
    private lateinit var serviceToggle: SwitchMaterial
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var firebaseManager: FirebaseManager

    private val requiredPermissions = mutableListOf(
        Manifest.permission.RECEIVE_SMS,
        Manifest.permission.READ_SMS
    ).apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        try {
            super.onCreate(savedInstanceState)
            Log.d(TAG, "Starting MainActivity")

            // Initialize Firebase
            if (!isFirebaseInitialized()) {
                FirebaseApp.initializeApp(this)
                Log.d(TAG, "Firebase initialized")
            }

            // Initialize FirebaseManager
            firebaseManager = FirebaseManager.getInstance(this)

            // Check authentication status first
            if (!checkAuthenticationStatus()) {
                redirectToLogin()
                return
            }

            // Setup view binding
            binding = ActivityMainBinding.inflate(layoutInflater)
            setContentView(binding.root)

            // Setup toolbar
            setSupportActionBar(binding.toolbar)
            supportActionBar?.title = "FinanceBuddy"

            // Display user information
            displayUserInfo()

            // Initialize Firebase structure for authenticated user
            initializeFirebaseStructure()

            // Initialize preferences and toggle
            setupPreferencesAndToggle()

            // Check permissions immediately when app starts
            if (!areAllPermissionsGranted()) {
                showPermissionExplanationDialog()
            } else {
                initializeServiceIfEnabled()
            }

            Log.d(TAG, "MainActivity onCreate completed")
        } catch (e: Exception) {
            Log.e(TAG, "Error in onCreate", e)
            handleError("Error initializing app", e)
        }
    }

    private fun setupPreferencesAndToggle() {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        serviceToggle = findViewById(R.id.serviceToggle)

        // Initialize switch state from preferences
        serviceToggle.isChecked = sharedPreferences.getBoolean("service_enabled", false)

        serviceToggle.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                if (areAllPermissionsGranted()) {
                    startSmsService()
                } else {
                    showPermissionExplanationDialog()
                    serviceToggle.isChecked = false
                }
            } else {
                stopSmsService()
            }
            sharedPreferences.edit().putBoolean("service_enabled", isChecked).apply()

            // Update service status display
            updateServiceStatus()
        }
    }

    private fun areAllPermissionsGranted(): Boolean {
        return requiredPermissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun showPermissionExplanationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Permissions Required")
            .setMessage("This app needs SMS and notification permissions to monitor your financial transactions through SMS messages. Please grant these permissions to continue.")
            .setPositiveButton("Grant Permissions") { _, _ ->
                requestPermissions()
            }
            .setNegativeButton("Cancel") { _, _ ->
                Toast.makeText(this, "App requires permissions to function properly", Toast.LENGTH_LONG).show()
            }
            .setCancelable(false)
            .show()
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            requiredPermissions.toTypedArray(),
            PERMISSIONS_REQUEST_CODE
        )
    }

    private fun initializeServiceIfEnabled() {
        if (sharedPreferences.getBoolean("service_enabled", false)) {
            startSmsService()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSIONS_REQUEST_CODE -> {
                if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                    Log.d(TAG, "All permissions granted")
                    Toast.makeText(this, "Permissions granted successfully", Toast.LENGTH_SHORT).show()
                    initializeServiceIfEnabled()
                } else {
                    Log.w(TAG, "Some permissions denied")
                    Toast.makeText(this, "App needs all permissions to function properly", Toast.LENGTH_LONG).show()
                    serviceToggle.isChecked = false
                    sharedPreferences.edit().putBoolean("service_enabled", false).apply()
                }
            }
        }
    }

    private fun isFirebaseInitialized(): Boolean {
        try {
            FirebaseApp.getInstance()
            return true
        } catch (e: IllegalStateException) {
            return false
        }
    }

    private fun checkAuthenticationStatus(): Boolean {
        val isLoggedIn = firebaseManager.isLoggedIn()
        Log.d(TAG, "Authentication status: $isLoggedIn")
        return isLoggedIn
    }

    private fun redirectToLogin() {
        Log.d(TAG, "Redirecting to login")
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun displayUserInfo() {
        val currentUser = firebaseManager.getCurrentUser()
        if (currentUser != null) {
            val email = currentUser.email ?: "Unknown"
            val displayName = currentUser.displayName ?: email.substringBefore("@")

            // Update toolbar subtitle with user info
            supportActionBar?.subtitle = "Welcome, $displayName"

            // Update user info card
            updateUserInfoCard(displayName, email)

            Log.d(TAG, "User logged in: $email")
        }
    }

    private fun updateUserInfoCard(displayName: String, email: String) {
        try {
            val userNameText = findViewById<android.widget.TextView>(R.id.userNameText)
            val userEmailText = findViewById<android.widget.TextView>(R.id.userEmailText)
            val serviceStatusText = findViewById<android.widget.TextView>(R.id.serviceStatusText)
            val serviceStatusIcon = findViewById<android.widget.ImageView>(R.id.serviceStatusIcon)

            userNameText?.text = "Welcome, $displayName"
            userEmailText?.text = email

            // Update service status
            updateServiceStatus()
        } catch (e: Exception) {
            Log.e(TAG, "Error updating user info card", e)
        }
    }

    private fun updateServiceStatus() {
        try {
            val serviceStatusText = findViewById<android.widget.TextView>(R.id.serviceStatusText)
            val serviceStatusIcon = findViewById<android.widget.ImageView>(R.id.serviceStatusIcon)

            val isServiceEnabled = sharedPreferences.getBoolean("service_enabled", false)

            if (isServiceEnabled) {
                serviceStatusText?.text = "Service: Active"
                serviceStatusText?.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_light))
                serviceStatusIcon?.setColorFilter(ContextCompat.getColor(this, android.R.color.holo_green_light))
            } else {
                serviceStatusText?.text = "Service: Inactive"
                serviceStatusText?.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_light))
                serviceStatusIcon?.setColorFilter(ContextCompat.getColor(this, android.R.color.holo_red_light))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating service status", e)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                showLogoutConfirmation()
                true
            }
            R.id.action_profile -> {
                showUserProfile()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showLogoutConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Logout") { _, _ ->
                performLogout()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun performLogout() {
        // Stop SMS service if running
        if (serviceToggle.isChecked) {
            stopSmsService()
            serviceToggle.isChecked = false
            sharedPreferences.edit().putBoolean("service_enabled", false).apply()
        }

        // Sign out from Firebase
        firebaseManager.signOut()
            .addOnCompleteListener { task ->
                if (task.isSuccessful()) {
                    Log.d(TAG, "Logout successful")
                    Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
                    redirectToLogin()
                } else {
                    Log.e(TAG, "Logout failed", task.exception)
                    Toast.makeText(this, "Logout failed", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun showUserProfile() {
        val currentUser = firebaseManager.getCurrentUser()
        if (currentUser != null) {
            val email = currentUser.email ?: "Unknown"
            val displayName = currentUser.displayName ?: "Not set"
            val uid = currentUser.uid

            AlertDialog.Builder(this)
                .setTitle("User Profile")
                .setMessage("Email: $email\nDisplay Name: $displayName\nUser ID: $uid")
                .setPositiveButton("OK", null)
                .show()
        }
    }

    private fun initializeFirebaseStructure() {
        try {
            Log.d(TAG, "Initializing Firebase structure for authenticated user")
            val database = FirebaseDatabase.getInstance("https://skn-hackfest-default-rtdb.asia-southeast1.firebasedatabase.app/")
            database.setPersistenceEnabled(true)

            val currentUser = firebaseManager.getCurrentUser()
            if (currentUser != null) {
                val userRef = database.getReference("users").child(currentUser.uid)

                // Update last login timestamp
                userRef.child("lastLogin").setValue(System.currentTimeMillis())
                    .addOnSuccessListener {
                        Log.d(TAG, "User last login updated")
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "Failed to update last login", e)
                    }

                // Test connection
                userRef.child("service_status").setValue("connected_" + System.currentTimeMillis())
                    .addOnSuccessListener {
                        Log.d(TAG, "Firebase connection test successful")
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "Firebase connection test failed", e)
                        handleError("Database connection failed", e)
                    }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing Firebase", e)
            handleError("Firebase initialization failed", e)
        }
    }

    private fun startSmsService() {
        try {
            val serviceIntent = Intent(this, SmsService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent)
            } else {
                startService(serviceIntent)
            }
            Toast.makeText(this, "SMS monitoring service started", Toast.LENGTH_SHORT).show()

            // Update service status display
            updateServiceStatus()
        } catch (e: Exception) {
            Log.e(TAG, "Error starting service: ${e.message}")
            Toast.makeText(this, "Failed to start service", Toast.LENGTH_SHORT).show()
            serviceToggle.isChecked = false
            sharedPreferences.edit().putBoolean("service_enabled", false).apply()

            // Update service status display
            updateServiceStatus()
        }
    }

    private fun stopSmsService() {
        try {
            stopService(Intent(this, SmsService::class.java))
            Toast.makeText(this, "SMS monitoring service stopped", Toast.LENGTH_SHORT).show()

            // Update service status display
            updateServiceStatus()
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping service: ${e.message}")
            Toast.makeText(this, "Failed to stop service", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleError(message: String, error: Exception) {
        val errorMessage = "$message: ${error.message}"
        Log.e(TAG, errorMessage, error)
        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
    }

    companion object {
        private const val TAG = "MainActivity"
        private const val PERMISSIONS_REQUEST_CODE = 123
    }
}