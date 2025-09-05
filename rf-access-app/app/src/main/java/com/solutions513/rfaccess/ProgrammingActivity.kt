package com.solutions513.rfaccess

import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.MifareClassic
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.solutions513.rfaccess.databinding.ActivityProgrammingBinding
import com.solutions513.rfaccess.ui.programming.ProgrammingViewModel
import kotlinx.coroutines.launch

class ProgrammingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProgrammingBinding
    private lateinit var viewModel: ProgrammingViewModel
    private var nfcAdapter: NfcAdapter? = null
    private var pendingIntent: PendingIntent? = null
    private var intentFiltersArray: Array<IntentFilter>? = null
    private var techListsArray: Array<Array<String>>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        binding = ActivityProgrammingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        viewModel = ViewModelProvider(this)[ProgrammingViewModel::class.java]
        
        val programId = intent.getStringExtra("program_id") ?: run {
            finish()
            return
        }
        
        setupNFC()
        setupUI()
        observeViewModel()
        
        viewModel.loadProgram(programId)
    }

    private fun setupNFC() {
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        
        pendingIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_MUTABLE
        )

        val ndef = IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED)
        try {
            ndef.addDataType("*/*")
        } catch (e: IntentFilter.MalformedMimeTypeException) {
            throw RuntimeException("fail", e)
        }
        intentFiltersArray = arrayOf(ndef)

        techListsArray = arrayOf(
            arrayOf(MifareClassic::class.java.name)
        )
    }

    private fun setupUI() {
        binding.apply {
            buttonCancel.setOnClickListener {
                finish()
            }
            
            buttonTryAgain.setOnClickListener {
                viewModel.resetProgramming()
                showProgrammingInstructions()
            }
        }
    }

    private fun observeViewModel() {
        viewModel.programmingState.observe(this) { state ->
            when (state) {
                is ProgrammingViewModel.ProgrammingState.Ready -> {
                    showProgrammingInstructions()
                }
                is ProgrammingViewModel.ProgrammingState.Programming -> {
                    showProgrammingProgress()
                }
                is ProgrammingViewModel.ProgrammingState.Success -> {
                    showSuccess()
                }
                is ProgrammingViewModel.ProgrammingState.Error -> {
                    showError(state.message)
                }
            }
        }
    }

    private fun showProgrammingInstructions() {
        binding.apply {
            layoutInstructions.visibility = View.VISIBLE
            layoutProgress.visibility = View.GONE
            layoutSuccess.visibility = View.GONE
            layoutError.visibility = View.GONE
            
            textInstructions.text = "Hold your RF Access Card to the back of your phone to program it"
            animationView.setAnimation("nfc_tap_animation.json")
            animationView.playAnimation()
        }
    }

    private fun showProgrammingProgress() {
        binding.apply {
            layoutInstructions.visibility = View.GONE
            layoutProgress.visibility = View.VISIBLE
            layoutSuccess.visibility = View.GONE
            layoutError.visibility = View.GONE
            
            textProgress.text = "Programming your RF Access Card..."
            progressBar.isIndeterminate = true
        }
    }

    private fun showSuccess() {
        binding.apply {
            layoutInstructions.visibility = View.GONE
            layoutProgress.visibility = View.GONE
            layoutSuccess.visibility = View.VISIBLE
            layoutError.visibility = View.GONE
            
            textSuccess.text = "Your RF Access Card has been programmed successfully!"
            animationView.setAnimation("success_animation.json")
            animationView.playAnimation()
            
            buttonDone.setOnClickListener {
                finish()
            }
        }
    }

    private fun showError(message: String) {
        binding.apply {
            layoutInstructions.visibility = View.GONE
            layoutProgress.visibility = View.GONE
            layoutSuccess.visibility = View.GONE
            layoutError.visibility = View.VISIBLE
            
            textError.text = "Programming failed. Please try again."
            buttonTryAgain.visibility = View.VISIBLE
        }
    }

    override fun onResume() {
        super.onResume()
        nfcAdapter?.enableForegroundDispatch(
            this,
            pendingIntent,
            intentFiltersArray,
            techListsArray
        )
    }

    override fun onPause() {
        super.onPause()
        nfcAdapter?.disableForegroundDispatch(this)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        
        if (NfcAdapter.ACTION_TECH_DISCOVERED == intent.action) {
            val tag: Tag? = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)
            tag?.let {
                handleNfcTag(it)
            }
        }
    }

    private fun handleNfcTag(tag: Tag) {
        lifecycleScope.launch {
            viewModel.programCard(tag)
        }
    }
}
