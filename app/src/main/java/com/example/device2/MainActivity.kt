package com.example.car

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.RetrofitService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStream
import java.util.*

class MainActivity : AppCompatActivity() {

    private val TAG = "Car"
    private val NAME = "Car"
    private val MY_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    private var m_bluetoothAdapter: BluetoothAdapter? = null
    private var m_bluetoothServerSocket: BluetoothServerSocket? = null
    private var m_bluetoothSocket: BluetoothSocket? = null
    private var m_inputStream: BufferedReader? = null
    private val mHandler = Handler(Looper.getMainLooper())

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        m_bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if(m_bluetoothAdapter == null) {
            Toast.makeText(this, "This device doesn't support Bluetooth", Toast.LENGTH_SHORT).show()
            return
        }
        if(!m_bluetoothAdapter!!.isEnabled) {
            val enableBluetoothIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBluetoothIntent, 0)
        }

        val discoverableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE)
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300)
        startActivity(discoverableIntent)

        startServerSocket()
    }

    @SuppressLint("MissingPermission")
    private fun startServerSocket() {

        // val socket: BluetoothSocket = device.createRfcommSocketToServiceRecord(MY_UUID)
        Thread {

            try {

                m_bluetoothServerSocket = m_bluetoothAdapter?.listenUsingRfcommWithServiceRecord(NAME, MY_UUID)
                runOnUiThread {
                    Toast.makeText(this, "Server socket started", Toast.LENGTH_SHORT).show()
                }

                m_bluetoothSocket = m_bluetoothServerSocket?.accept()
                runOnUiThread {
                    Toast.makeText(this, "Client socket connected", Toast.LENGTH_SHORT).show()
                }

                val inputStream = m_bluetoothSocket?.inputStream
                val buffer = ByteArray(1024)

                var numBytes: Int
                var pin = ""

                // Read data from the input stream until the PIN code has been fully received
                while (true) {
                    numBytes = inputStream?.read(buffer) ?: -1

                    if (numBytes != -1) {
                        val received = String(buffer, 0, numBytes)
                        pin += received
                        // If the received PIN code is complete, break out of the loop
                        //we will receive both pin and id of reservation
                        if (pin.length == 6) {
                            break
                        }
                    }
                }

                runOnUiThread {
                    Toast.makeText(this, "Received PIN code: $pin", Toast.LENGTH_SHORT).show()
                    val dataTextView = findViewById<TextView>(R.id.data)
                    dataTextView.text = pin.substring(0, 4)
                }
                runOnUiThread {
                    Toast.makeText(this, "Received ID is: $pin", Toast.LENGTH_SHORT).show()
                    val id = findViewById<TextView>(R.id.id)
                    id.text = pin.substring(4)
                }
                val id="56789 120 34"
                val pinReceived=pin.substring(0, 4)
                val idReservation : Int=pin.substring(4).toInt()



                CoroutineScope(Dispatchers.Main).launch {
                    val pincar:String = getcarpin(idReservation)!!
                    verifyPinCode(pinReceived,pincar)
                }

            } catch (e: IOException) {
                runOnUiThread {
                    Toast.makeText(this, "Error accepting socket connection: ${e.message}", Toast.LENGTH_SHORT).show()
                }
                return@Thread
            }

        }.start()
    }

    @SuppressLint("MissingPermission")
    private fun verifyPinCode(PinReceived:String, PinCar:String) {
        if (PinReceived == PinCar) {
            val adapter = BluetoothAdapter.getDefaultAdapter()
            val device = adapter.getRemoteDevice("device_address") // replace with the address of device 2
            val socket = device.createRfcommSocketToServiceRecord(UUID.fromString("UUID")) // replace with the UUID of your app
            socket.connect()
            val outputStream = socket.outputStream
            outputStream.write("PIN codes matched!".toByteArray(Charsets.UTF_8))
            outputStream.close()
            socket.close()
        }
    }
    private suspend fun getcarpin(id: Int?): String? {
        var pinCar: String? = null
        withContext(Dispatchers.IO) {
            val response = RetrofitService.Endpoint.getcar(id!!)
            if (response.isSuccessful) {
                pinCar = response.body()
            }
        }
        return pinCar
    }






}