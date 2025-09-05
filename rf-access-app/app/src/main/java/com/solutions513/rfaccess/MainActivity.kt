package com.solutions513.rfaccess

import android.content.Intent
import android.nfc.NfcAdapter
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import com.solutions513.rfaccess.databinding.ActivityMainBinding
import com.solutions513.rfaccess.ui.auth.AuthActivity
import com.solutions513.rfaccess.ui.main.MainViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel
    private var nfcAdapter: NfcAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        
        // Check authentication
        if (FirebaseAuth.getInstance().currentUser == null) {
            startActivity(Intent(this, AuthActivity::class.java))
            finish()
            return
        }
        
        setupNFC()
        setupUI()
        observeViewModel()
        
        // Subscribe to FCM topic for this user
        subscribeToNotifications()
    }

    private fun setupNFC() {
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        
        binding.apply {
            if (nfcAdapter == null) {
                textNfcStatus.text = "NFC not supported on this device"
                textNfcStatus.setTextColor(getColor(R.color.error))
                cardNfcStatus.visibility = View.VISIBLE
            } else if (!nfcAdapter!!.isEnabled) {
                textNfcStatus.text = "Please enable NFC in your device settings"
                textNfcStatus.setTextColor(getColor(R.color.warning))
                cardNfcStatus.visibility = View.VISIBLE
                buttonEnableNfc.visibility = View.VISIBLE
            } else {
                cardNfcStatus.visibility = View.GONE
            }
        }
    }

    private fun setupUI() {
        binding.apply {
            buttonEnableNfc.setOnClickListener {
                startActivity(Intent(android.provider.Settings.ACTION_NFC_SETTINGS))
            }
            
            buttonRefresh.setOnClickListener {
                viewModel.checkForPendingPrograms()
            }
        }
    }

    private fun observeViewModel() {
        viewModel.pendingPrograms.observe(this) { programs ->
            binding.apply {
                if (programs.isEmpty()) {
                    cardNoPending.visibility = View.VISIBLE
                    cardPendingProgram.visibility = View.GONE
                } else {
                    cardNoPending.visibility = View.GONE
                    cardPendingProgram.visibility = View.VISIBLE
                    
                    val program = programs.first()
                    textProgramTitle.text = "RF Access Programming"
                    textProgramMessage.text = "You have a new access card ready to program"
                    
                    buttonStartProgramming.setOnClickListener {
                        startProgramming(program.id)
                    }
                }
            }
        }

        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }

    private fun startProgramming(programId: String) {
        val intent = Intent(this, ProgrammingActivity::class.java)
        intent.putExtra("program_id", programId)
        startActivity(intent)
    }

    private fun subscribeToNotifications() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseMessaging.getInstance().subscribeToTopic("user_$userId")
    }

    override fun onResume() {
        super.onResume()
        setupNFC()
        viewModel.checkForPendingPrograms()
    }
}
