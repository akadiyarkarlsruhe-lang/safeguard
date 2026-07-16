package com.example.safeguard

import android.content.Intent
import android.net.VpnService
import android.os.ParcelFileDescriptor
import android.util.Log
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.ByteBuffer

class LocalBlockerVpn : VpnService(), Runnable {
    private var mThread: Thread? = null
    private var mInterface: ParcelFileDescriptor? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (mThread == null) {
            mThread = Thread(this, "SafeGuardVpnThread")
            mThread?.start()
        }
        return START_STICKY
    }

    override fun onDestroy() {
        if (mThread != null) {
            mThread?.interrupt()
        }
        super.onDestroy()
    }

    override fun run() {
        try {
            val builder = Builder()
            builder.setMtu(1500)
            builder.addAddress("10.0.0.2", 24)
            builder.addRoute("0.0.0.0", 0)
            // Nutze CleanBrowsing Family Filter (Blockiert Glücksspiel & Pornos auf DNS-Ebene)
            builder.addDnsServer("185.228.168.168") 
            builder.addDnsServer("185.228.169.168")
            builder.setSession("SafeGuardBlocker")

            mInterface = builder.establish()
            
            val input = FileInputStream(mInterface?.fileDescriptor)
            val packet = ByteBuffer.allocate(32767)
            while (!Thread.interrupted()) {
                val length = input.read(packet.array())
                if (length > 0) {
                    packet.clear()
                }
                Thread.sleep(100)
            }
        } catch (e: Exception) {
            Log.e("SafeGuardVpn", "Fehler im VPN Service", e)
        } finally {
            try {
                mInterface?.close()
            } catch (e: Exception) {}
            mInterface = null
            mThread = null
        }
    }
}
