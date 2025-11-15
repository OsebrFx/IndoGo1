package com.example.indogo.models

/**
 * Printer connection types
 */
enum class ConnectionType {
    WIFI,
    BLUETOOTH,
    USB
}

/**
 * Printer connection status
 */
enum class PrinterStatus {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    PRINTING,
    ERROR,
    PAPER_OUT,
    OFFLINE
}

/**
 * Paper width options for thermal printer
 */
enum class PaperWidth(val widthMm: Int, val widthDots: Int) {
    WIDTH_58MM(58, 384),    // 58mm paper, 384 dots
    WIDTH_80MM(80, 576)     // 80mm paper, 576 dots
}

/**
 * Print result sealed class
 */
sealed class PrintResult {
    object Success : PrintResult()
    data class Error(val message: String, val exception: Exception? = null) : PrintResult()
    object Cancelled : PrintResult()
}

/**
 * Printer configuration data class
 */
data class PrinterConfig(
    val connectionType: ConnectionType = ConnectionType.BLUETOOTH,
    val wifiIpAddress: String = "",
    val wifiPort: Int = 9100,
    val bluetoothDeviceName: String = "",
    val bluetoothDeviceAddress: String = "",
    val paperWidth: PaperWidth = PaperWidth.WIDTH_80MM,
    val printerModel: String = "GA-E200I",
    val commandProtocol: String = "ESCIP05",
    val autoReconnect: Boolean = true,
    val printDensity: Int = 15, // 0-15, 15 is darkest
    val printSpeed: Int = 4     // 0-9, higher is faster
)

/**
 * Print job data class
 */
data class PrintJob(
    val jobId: String,
    val ticket: Ticket,
    val copies: Int = 1,
    val timestamp: Long = System.currentTimeMillis()
)
