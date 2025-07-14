package com.ggetters.app.ui.startup.views

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.ggetters.app.R

class VerificationActivity : AppCompatActivity() {
    private lateinit var otpFields: Array<EditText>
    private lateinit var submitButton: Button
    private lateinit var resendButton: Button
    private lateinit var timerText: TextView
    private var timer: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verification)

        otpFields = arrayOf(
            findViewById(R.id.otp1),
            findViewById(R.id.otp2),
            findViewById(R.id.otp3),
            findViewById(R.id.otp4)
        )
        submitButton = findViewById(R.id.submitButton)
        resendButton = findViewById(R.id.resendButton)
        timerText = findViewById(R.id.timerText)

        // Autofocus and tab
        for (i in otpFields.indices) {
            otpFields[i].addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    if (s?.length == 1 && i < otpFields.size - 1) {
                        otpFields[i + 1].requestFocus()
                    }
                }
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            })
        }

        submitButton.setOnClickListener {
            // TODO: Backend - Verify OTP
            // On success:
            startActivity(Intent(this, VerificationSuccessOverlay::class.java))
        }
        resendButton.setOnClickListener {
            // TODO: Backend - Resend OTP
            startTimer()
        }
        startTimer()
    }

    private fun startTimer() {
        timer?.cancel()
        resendButton.isEnabled = false
        timer = object : CountDownTimer(30000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timerText.text = "Resend in ${millisUntilFinished / 1000}s"
            }
            override fun onFinish() {
                resendButton.isEnabled = true
                timerText.text = "Resend Code"
            }
        }.start()
    }
} 