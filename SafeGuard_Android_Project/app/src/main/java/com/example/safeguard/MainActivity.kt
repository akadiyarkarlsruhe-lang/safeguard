package com.example.safeguard

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.VpnService
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val layout = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            gravity = android.view.Gravity.CENTER
            setPadding(50, 50, 50, 50)
            background = android.graphics.drawable.ColorDrawable(android.graphics.Color.parseColor("#0b0f19"))
        }

        val title = TextView(this).apply {
            text = "SafeGuard Blocker"
            textSize = 24f
            setTextColor(android.graphics.Color.WHITE)
            gravity = android.view.Gravity.CENTER
            setPadding(0, 0, 0, 40)
        }

        val statusText = TextView(this).apply {
            text = "Status: Inaktiv"
            textSize = 16f
            setTextColor(android.graphics.Color.parseColor("#94a3b8"))
            gravity = android.view.Gravity.CENTER
            setPadding(0, 0, 0, 80)
        }

        val startButton = Button(this).apply {
            text = "Schutz aktivieren"
            setBackgroundColor(android.graphics.Color.parseColor("#06b6d4"))
            setTextColor(android.graphics.Color.WHITE)
        }

        layout.addView(title)
        layout.addView(statusText)
        layout.addView(startButton)
        setContentView(layout)

        val dpm = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val adminComponent = ComponentName(this, MyDeviceAdminReceiver::class.java)

        if (dpm.isDeviceOwnerApp(packageName)) {
            try {
                dpm.setUninstallBlocked(adminComponent, packageName, true)
                statusText.text = "Status: Deinstallationsschutz AKTIV"
                statusText.setTextColor(android.graphics.Color.parseColor("#4ade80"))
            } catch (e: Exception) {
                statusText.text = "Fehler beim Setzen des Schutzes"
            }
        } else {
            statusText.text = "Status: Kein Device Owner\n(Bitte via ADB einrichten)"
            statusText.setTextColor(android.graphics.Color.parseColor("#f87171"))
        }

        startButton.setOnClickListener {
            val intent = VpnService.prepare(this)
            if (intent != null) {
                startActivityForResult(intent, 0)
            } else {
                onActivityResult(0, RESULT_OK, null)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            val intent = Intent(this, LocalBlockerVpn::class.java)
            startService(intent)
            Toast.makeText(this, "VPN-Filter gestartet", Toast.LENGTH_SHORT).show()
        }
    }
}
