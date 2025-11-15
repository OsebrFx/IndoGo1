package com.example.indogo.utils

import android.content.Context
import android.content.SharedPreferences
import com.example.indogo.models.ConnectionType
import com.example.indogo.models.PaperWidth
import com.example.indogo.models.PrinterConfig

/**
 * SharedPreferences helper for printer configuration
 */
class PrinterPreferences(context: Context) {

    companion object {
        private const val PREF_NAME = "printer_preferences"

        // Preference keys
        private const val KEY_CONNECTION_TYPE = "connection_type"
        private const val KEY_WIFI_IP = "wifi_ip"
        private const val KEY_WIFI_PORT = "wifi_port"
        private const val KEY_BT_NAME = "bt_name"
        private const val KEY_BT_ADDRESS = "bt_address"
        private const val KEY_PAPER_WIDTH = "paper_width"
        private const val KEY_PRINTER_MODEL = "printer_model"
        private const val KEY_COMMAND_PROTOCOL = "command_protocol"
        private const val KEY_AUTO_RECONNECT = "auto_reconnect"
        private const val KEY_PRINT_DENSITY = "print_density"
        private const val KEY_PRINT_SPEED = "print_speed"

        // Default values
        private const val DEFAULT_WIFI_IP = "192.168.11.200"
        private const val DEFAULT_WIFI_PORT = 9100
        private const val DEFAULT_PRINTER_MODEL = "GA-E200I"
        private const val DEFAULT_COMMAND_PROTOCOL = "ESCIP05"
        private const val DEFAULT_PRINT_DENSITY = 15
        private const val DEFAULT_PRINT_SPEED = 4
    }

    private val preferences: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    /**
     * Save printer configuration
     */
    fun savePrinterConfig(config: PrinterConfig) {
        preferences.edit().apply {
            putString(KEY_CONNECTION_TYPE, config.connectionType.name)
            putString(KEY_WIFI_IP, config.wifiIpAddress)
            putInt(KEY_WIFI_PORT, config.wifiPort)
            putString(KEY_BT_NAME, config.bluetoothDeviceName)
            putString(KEY_BT_ADDRESS, config.bluetoothDeviceAddress)
            putString(KEY_PAPER_WIDTH, config.paperWidth.name)
            putString(KEY_PRINTER_MODEL, config.printerModel)
            putString(KEY_COMMAND_PROTOCOL, config.commandProtocol)
            putBoolean(KEY_AUTO_RECONNECT, config.autoReconnect)
            putInt(KEY_PRINT_DENSITY, config.printDensity)
            putInt(KEY_PRINT_SPEED, config.printSpeed)
        }.apply()
    }

    /**
     * Load printer configuration
     */
    fun getPrinterConfig(): PrinterConfig {
        val connectionTypeStr = preferences.getString(KEY_CONNECTION_TYPE, ConnectionType.WIFI.name)
        val connectionType = try {
            ConnectionType.valueOf(connectionTypeStr ?: ConnectionType.WIFI.name)
        } catch (e: Exception) {
            ConnectionType.WIFI
        }

        val paperWidthStr = preferences.getString(KEY_PAPER_WIDTH, PaperWidth.WIDTH_80MM.name)
        val paperWidth = try {
            PaperWidth.valueOf(paperWidthStr ?: PaperWidth.WIDTH_80MM.name)
        } catch (e: Exception) {
            PaperWidth.WIDTH_80MM
        }

        return PrinterConfig(
            connectionType = connectionType,
            wifiIpAddress = preferences.getString(KEY_WIFI_IP, DEFAULT_WIFI_IP) ?: DEFAULT_WIFI_IP,
            wifiPort = preferences.getInt(KEY_WIFI_PORT, DEFAULT_WIFI_PORT),
            bluetoothDeviceName = preferences.getString(KEY_BT_NAME, "") ?: "",
            bluetoothDeviceAddress = preferences.getString(KEY_BT_ADDRESS, "") ?: "",
            paperWidth = paperWidth,
            printerModel = preferences.getString(KEY_PRINTER_MODEL, DEFAULT_PRINTER_MODEL) ?: DEFAULT_PRINTER_MODEL,
            commandProtocol = preferences.getString(KEY_COMMAND_PROTOCOL, DEFAULT_COMMAND_PROTOCOL) ?: DEFAULT_COMMAND_PROTOCOL,
            autoReconnect = preferences.getBoolean(KEY_AUTO_RECONNECT, true),
            printDensity = preferences.getInt(KEY_PRINT_DENSITY, DEFAULT_PRINT_DENSITY),
            printSpeed = preferences.getInt(KEY_PRINT_SPEED, DEFAULT_PRINT_SPEED)
        )
    }

    /**
     * Check if printer is configured
     */
    fun isPrinterConfigured(): Boolean {
        val config = getPrinterConfig()
        return when (config.connectionType) {
            ConnectionType.WIFI -> config.wifiIpAddress.isNotEmpty()
            ConnectionType.BLUETOOTH -> config.bluetoothDeviceName.isNotEmpty() || config.bluetoothDeviceAddress.isNotEmpty()
            ConnectionType.USB -> false
        }
    }

    /**
     * Clear printer configuration
     */
    fun clearConfig() {
        preferences.edit().clear().apply()
    }

    /**
     * Save WiFi settings
     */
    fun saveWiFiSettings(ipAddress: String, port: Int = DEFAULT_WIFI_PORT) {
        preferences.edit().apply {
            putString(KEY_CONNECTION_TYPE, ConnectionType.WIFI.name)
            putString(KEY_WIFI_IP, ipAddress)
            putInt(KEY_WIFI_PORT, port)
        }.apply()
    }

    /**
     * Save Bluetooth settings
     */
    fun saveBluetoothSettings(deviceName: String, deviceAddress: String) {
        preferences.edit().apply {
            putString(KEY_CONNECTION_TYPE, ConnectionType.BLUETOOTH.name)
            putString(KEY_BT_NAME, deviceName)
            putString(KEY_BT_ADDRESS, deviceAddress)
        }.apply()
    }

    /**
     * Save paper width setting
     */
    fun savePaperWidth(paperWidth: PaperWidth) {
        preferences.edit().putString(KEY_PAPER_WIDTH, paperWidth.name).apply()
    }

    /**
     * Get paper width setting
     */
    fun getPaperWidth(): PaperWidth {
        val paperWidthStr = preferences.getString(KEY_PAPER_WIDTH, PaperWidth.WIDTH_80MM.name)
        return try {
            PaperWidth.valueOf(paperWidthStr ?: PaperWidth.WIDTH_80MM.name)
        } catch (e: Exception) {
            PaperWidth.WIDTH_80MM
        }
    }

    /**
     * Save print density
     */
    fun savePrintDensity(density: Int) {
        preferences.edit().putInt(KEY_PRINT_DENSITY, density.coerceIn(0, 15)).apply()
    }

    /**
     * Get print density
     */
    fun getPrintDensity(): Int {
        return preferences.getInt(KEY_PRINT_DENSITY, DEFAULT_PRINT_DENSITY)
    }
}
