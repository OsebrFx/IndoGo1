package com.example.indogo

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.indogo.databinding.ActivityPrinterSettingsBinding
import com.example.indogo.models.*
import com.example.indogo.printer.ThermalPrinterService
import kotlinx.coroutines.launch

class PrinterSettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPrinterSettingsBinding
    private lateinit var printerService: ThermalPrinterService
    private var selectedBluetoothDevice: BluetoothDevice? = null

    private val bluetoothPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            loadBluetoothDevices()
        } else {
            Toast.makeText(
                this,
                R.string.error_bluetooth_permission,
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPrinterSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        printerService = ThermalPrinterService.getInstance(this)

        setupUI()
        loadCurrentConfig()
        observePrinterStatus()
    }

    private fun setupUI() {
        // Connection type radio buttons
        binding.rgConnectionType.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rbWifi -> {
                    binding.cardWifiSettings.visibility = View.VISIBLE
                    binding.cardBluetoothSettings.visibility = View.GONE
                }
                R.id.rbBluetooth -> {
                    binding.cardWifiSettings.visibility = View.GONE
                    binding.cardBluetoothSettings.visibility = View.VISIBLE
                    checkBluetoothPermissions()
                }
            }
        }

        // Bluetooth device spinner
        binding.spinnerBluetoothDevices.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val devices = printerService.getPairedBluetoothDevices()
                if (position in devices.indices) {
                    selectedBluetoothDevice = devices[position]
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                selectedBluetoothDevice = null
            }
        }

        // Save button
        binding.btnSaveSettings.setOnClickListener {
            saveConfiguration()
        }

        // Test connection button
        binding.btnTestConnection.setOnClickListener {
            testConnection()
        }

        // Print test button
        binding.btnPrintTest.setOnClickListener {
            printTestPage()
        }
    }

    private fun loadCurrentConfig() {
        val config = printerService.getConfig()

        // Set connection type
        when (config.connectionType) {
            ConnectionType.WIFI -> {
                binding.rbWifi.isChecked = true
                binding.etWifiIp.setText(config.wifiIpAddress)
                binding.etWifiPort.setText(config.wifiPort.toString())
            }
            ConnectionType.BLUETOOTH -> {
                binding.rbBluetooth.isChecked = true
                loadBluetoothDevices()
            }
            ConnectionType.USB -> {
                // USB not supported yet
            }
        }

        // Set paper width
        when (config.paperWidth) {
            PaperWidth.WIDTH_58MM -> binding.rbPaper58mm.isChecked = true
            PaperWidth.WIDTH_80MM -> binding.rbPaper80mm.isChecked = true
        }
    }

    private fun checkBluetoothPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Android 12+
            val permissions = arrayOf(
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_SCAN
            )
            if (!hasPermissions(permissions)) {
                bluetoothPermissionLauncher.launch(permissions)
            } else {
                loadBluetoothDevices()
            }
        } else {
            // Android 11 and below
            val permissions = arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            if (!hasPermissions(permissions)) {
                bluetoothPermissionLauncher.launch(permissions)
            } else {
                loadBluetoothDevices()
            }
        }
    }

    private fun hasPermissions(permissions: Array<String>): Boolean {
        return permissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun loadBluetoothDevices() {
        if (!printerService.isBluetoothAvailable()) {
            Toast.makeText(this, R.string.bluetooth_not_available, Toast.LENGTH_SHORT).show()
            return
        }

        val devices = printerService.getPairedBluetoothDevices()
        if (devices.isEmpty()) {
            Toast.makeText(this, R.string.no_paired_devices, Toast.LENGTH_SHORT).show()
            return
        }

        val deviceNames = devices.map { "${it.name} (${it.address})" }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, deviceNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerBluetoothDevices.adapter = adapter

        // Select current device if configured
        val config = printerService.getConfig()
        val currentDeviceIndex = devices.indexOfFirst {
            it.address == config.bluetoothDeviceAddress
        }
        if (currentDeviceIndex >= 0) {
            binding.spinnerBluetoothDevices.setSelection(currentDeviceIndex)
        }
    }

    private fun saveConfiguration() {
        val config = buildConfiguration()
        if (config != null) {
            printerService.saveConfig(config)
            Toast.makeText(this, R.string.save, Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun buildConfiguration(): PrinterConfig? {
        val connectionType = when (binding.rgConnectionType.checkedRadioButtonId) {
            R.id.rbWifi -> ConnectionType.WIFI
            R.id.rbBluetooth -> ConnectionType.BLUETOOTH
            else -> return null
        }

        val paperWidth = when (binding.rgPaperWidth.checkedRadioButtonId) {
            R.id.rbPaper58mm -> PaperWidth.WIDTH_58MM
            R.id.rbPaper80mm -> PaperWidth.WIDTH_80MM
            else -> PaperWidth.WIDTH_80MM
        }

        return when (connectionType) {
            ConnectionType.WIFI -> {
                val ip = binding.etWifiIp.text.toString().trim()
                val portStr = binding.etWifiPort.text.toString().trim()

                if (ip.isEmpty()) {
                    Toast.makeText(this, R.string.error_invalid_ip, Toast.LENGTH_SHORT).show()
                    return null
                }

                val port = portStr.toIntOrNull() ?: 9100

                PrinterConfig(
                    connectionType = ConnectionType.WIFI,
                    wifiIpAddress = ip,
                    wifiPort = port,
                    paperWidth = paperWidth
                )
            }
            ConnectionType.BLUETOOTH -> {
                val device = selectedBluetoothDevice
                if (device == null) {
                    Toast.makeText(this, R.string.select_device, Toast.LENGTH_SHORT).show()
                    return null
                }

                PrinterConfig(
                    connectionType = ConnectionType.BLUETOOTH,
                    bluetoothDeviceName = device.name ?: "Unknown",
                    bluetoothDeviceAddress = device.address,
                    paperWidth = paperWidth
                )
            }
            else -> null
        }
    }

    private fun testConnection() {
        val config = buildConfiguration() ?: return

        binding.btnTestConnection.isEnabled = false
        binding.btnTestConnection.text = getString(R.string.connecting_to_printer)

        lifecycleScope.launch {
            val result = printerService.testConnection(config)

            binding.btnTestConnection.isEnabled = true
            binding.btnTestConnection.text = getString(R.string.test)

            if (result.isSuccess && result.getOrNull() == true) {
                Toast.makeText(
                    this@PrinterSettingsActivity,
                    R.string.connection_success,
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(
                    this@PrinterSettingsActivity,
                    R.string.connection_failed,
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun printTestPage() {
        binding.btnPrintTest.isEnabled = false
        binding.btnPrintTest.text = getString(R.string.printing_ticket)

        lifecycleScope.launch {
            val result = printerService.printTestPage()

            binding.btnPrintTest.isEnabled = true
            binding.btnPrintTest.text = getString(R.string.print_test_page)

            when (result) {
                is PrintResult.Success -> {
                    Toast.makeText(
                        this@PrinterSettingsActivity,
                        R.string.test_print_success,
                        Toast.LENGTH_SHORT
                    ).show()
                }
                is PrintResult.Error -> {
                    Toast.makeText(
                        this@PrinterSettingsActivity,
                        "${getString(R.string.test_print_failed)}: ${result.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
                PrintResult.Cancelled -> {
                    Toast.makeText(
                        this@PrinterSettingsActivity,
                        "Cancelled",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun observePrinterStatus() {
        lifecycleScope.launch {
            printerService.printerStatus.collect { status ->
                updateStatusUI(status)
            }
        }
    }

    private fun updateStatusUI(status: PrinterStatus) {
        val (statusText, statusColor) = when (status) {
            PrinterStatus.DISCONNECTED -> getString(R.string.status_disconnected) to getColor(android.R.color.holo_red_dark)
            PrinterStatus.CONNECTING -> getString(R.string.status_connecting) to getColor(android.R.color.holo_orange_dark)
            PrinterStatus.CONNECTED -> getString(R.string.status_connected) to getColor(android.R.color.holo_green_dark)
            PrinterStatus.PRINTING -> getString(R.string.status_printing) to getColor(android.R.color.holo_blue_dark)
            PrinterStatus.ERROR -> getString(R.string.status_error) to getColor(android.R.color.holo_red_dark)
            PrinterStatus.PAPER_OUT -> getString(R.string.status_paper_out) to getColor(android.R.color.holo_orange_dark)
            PrinterStatus.OFFLINE -> getString(R.string.status_offline) to getColor(android.R.color.holo_red_dark)
        }

        binding.tvPrinterStatus.text = statusText
        binding.tvPrinterStatus.setTextColor(statusColor)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (printerService.isConnected() && !isChangingConfigurations) {
            printerService.disconnect()
        }
    }
}
