package com.example.indogo.printer

import android.graphics.Bitmap
import java.nio.charset.Charset

/**
 * ESCIP05 Command Protocol Implementation for Gainsha GA-E200I Thermal Printer
 * This class provides ESC/POS compatible commands for thermal printing
 */
object ESCIP05Commands {

    // Control characters
    private const val ESC: Byte = 0x1B
    private const val GS: Byte = 0x1D
    private const val LF: Byte = 0x0A
    private const val CR: Byte = 0x0D
    private const val HT: Byte = 0x09

    /**
     * Initialize printer - reset to default settings
     */
    fun initPrinter(): ByteArray {
        return byteArrayOf(ESC, 0x40)
    }

    /**
     * Feed paper by n lines
     */
    fun feedLines(lines: Int): ByteArray {
        return byteArrayOf(ESC, 0x64, lines.toByte())
    }

    /**
     * Feed and cut paper
     */
    fun feedAndCut(): ByteArray {
        val feed = feedLines(4)
        val cut = byteArrayOf(GS, 0x56, 0x00) // Full cut
        return feed + cut
    }

    /**
     * Partial cut paper
     */
    fun partialCut(): ByteArray {
        val feed = feedLines(3)
        val cut = byteArrayOf(GS, 0x56, 0x01) // Partial cut
        return feed + cut
    }

    /**
     * Set text alignment
     * @param alignment 0 = Left, 1 = Center, 2 = Right
     */
    fun setAlignment(alignment: Int): ByteArray {
        return byteArrayOf(ESC, 0x61, alignment.toByte())
    }

    /**
     * Alignment helper methods
     */
    fun alignLeft(): ByteArray = setAlignment(0)
    fun alignCenter(): ByteArray = setAlignment(1)
    fun alignRight(): ByteArray = setAlignment(2)

    /**
     * Set text size
     * @param width 0-7 (width multiplier)
     * @param height 0-7 (height multiplier)
     */
    fun setTextSize(width: Int, height: Int): ByteArray {
        val size = ((width and 0x07) shl 4) or (height and 0x07)
        return byteArrayOf(GS, 0x21, size.toByte())
    }

    /**
     * Common text size presets
     */
    fun textSizeNormal(): ByteArray = setTextSize(0, 0)
    fun textSizeDouble(): ByteArray = setTextSize(1, 1)
    fun textSizeTriple(): ByteArray = setTextSize(2, 2)
    fun textSizeWide(): ByteArray = setTextSize(1, 0)
    fun textSizeTall(): ByteArray = setTextSize(0, 1)

    /**
     * Set text style (bold, underline, etc.)
     */
    fun setBold(enabled: Boolean): ByteArray {
        return byteArrayOf(ESC, 0x45, if (enabled) 1 else 0)
    }

    fun setUnderline(mode: Int): ByteArray {
        // 0 = off, 1 = 1-dot thick, 2 = 2-dot thick
        return byteArrayOf(ESC, 0x2D, mode.toByte())
    }

    fun setInverse(enabled: Boolean): ByteArray {
        return byteArrayOf(GS, 0x42, if (enabled) 1 else 0)
    }

    /**
     * Set line spacing
     */
    fun setLineSpacing(dots: Int): ByteArray {
        return byteArrayOf(ESC, 0x33, dots.toByte())
    }

    fun resetLineSpacing(): ByteArray {
        return byteArrayOf(ESC, 0x32)
    }

    /**
     * Print text with newline
     */
    fun printLine(text: String): ByteArray {
        return text.toByteArray(Charset.forName("GB2312")) + byteArrayOf(LF)
    }

    /**
     * Print text without newline
     */
    fun printText(text: String): ByteArray {
        return text.toByteArray(Charset.forName("GB2312"))
    }

    /**
     * Print text centered
     */
    fun printLineCentered(text: String): ByteArray {
        return alignCenter() + printLine(text) + alignLeft()
    }

    /**
     * Print horizontal line (dashes)
     */
    fun printDivider(paperWidth: Int = 48): ByteArray {
        val line = "-".repeat(paperWidth)
        return printLine(line)
    }

    /**
     * Print double horizontal line (equals)
     */
    fun printDoubleDivider(paperWidth: Int = 48): ByteArray {
        val line = "=".repeat(paperWidth)
        return printLine(line)
    }

    /**
     * Set print density
     * @param density 0-15 (0 = lightest, 15 = darkest)
     */
    fun setPrintDensity(density: Int): ByteArray {
        val n = density.coerceIn(0, 15)
        return byteArrayOf(ESC, 0x7E, n.toByte())
    }

    /**
     * Print barcode
     * @param data Barcode data
     * @param type Barcode type (65-73 for different formats)
     * @param height Barcode height in dots (1-255)
     */
    fun printBarcode(data: String, type: Int = 73, height: Int = 162): ByteArray {
        val setBarcodeHeight = byteArrayOf(GS, 0x68, height.toByte())
        val setBarcodeWidth = byteArrayOf(GS, 0x77, 2) // Module width
        val setHRIPosition = byteArrayOf(GS, 0x48, 2) // Print HRI below barcode
        val setBarcodeFont = byteArrayOf(GS, 0x66, 0) // Font A

        val barcodeCommand = byteArrayOf(GS, 0x6B, type.toByte(), data.length.toByte())
        val barcodeData = data.toByteArray(Charset.forName("US-ASCII"))

        return setBarcodeHeight + setBarcodeWidth + setHRIPosition +
               setBarcodeFont + barcodeCommand + barcodeData
    }

    /**
     * Print QR Code
     * @param data QR code data
     * @param size Module size (1-16)
     */
    fun printQRCode(data: String, size: Int = 6): ByteArray {
        val qrData = data.toByteArray(Charset.forName("UTF-8"))
        val pL = (qrData.size + 3) % 256
        val pH = (qrData.size + 3) / 256

        // Model - QR Code Model 2
        val setModel = byteArrayOf(GS, 0x28, 0x6B, 0x04, 0x00, 0x31, 0x41, 0x32, 0x00)

        // Size
        val setSize = byteArrayOf(GS, 0x28, 0x6B, 0x03, 0x00, 0x31, 0x43, size.toByte())

        // Error correction level (48=L, 49=M, 50=Q, 51=H)
        val setErrorLevel = byteArrayOf(GS, 0x28, 0x6B, 0x03, 0x00, 0x31, 0x45, 0x31)

        // Store data
        val storeCommand = byteArrayOf(GS, 0x28, 0x6B, pL.toByte(), pH.toByte(), 0x31, 0x50, 0x30)

        // Print QR code
        val printCommand = byteArrayOf(GS, 0x28, 0x6B, 0x03, 0x00, 0x31, 0x51, 0x30)

        return setModel + setSize + setErrorLevel + storeCommand + qrData + printCommand
    }

    /**
     * Print bitmap image
     */
    fun printBitmap(bitmap: Bitmap, mode: Int = 0): ByteArray {
        val width = bitmap.width
        val height = bitmap.height

        val commands = mutableListOf<Byte>()

        // Set line spacing to 24 dots
        commands.addAll(byteArrayOf(ESC, 0x33, 24).toList())

        // Process bitmap line by line (24 dots at a time)
        var y = 0
        while (y < height) {
            // ESC * m nL nH
            commands.add(ESC)
            commands.add(0x2A)
            commands.add(33) // 24-dot double-density
            commands.add((width % 256).toByte())
            commands.add((width / 256).toByte())

            // Process 24 lines at a time
            for (x in 0 until width) {
                for (k in 0 until 3) { // 3 bytes = 24 dots
                    var slice = 0
                    for (b in 0 until 8) {
                        val py = y + k * 8 + b
                        if (py < height) {
                            val pixel = bitmap.getPixel(x, py)
                            val luminance = (0.299 * ((pixel shr 16) and 0xFF) +
                                           0.587 * ((pixel shr 8) and 0xFF) +
                                           0.114 * (pixel and 0xFF)).toInt()
                            if (luminance < 128) { // Black pixel
                                slice = slice or (1 shl (7 - b))
                            }
                        }
                    }
                    commands.add(slice.toByte())
                }
            }
            commands.add(LF)
            y += 24
        }

        // Reset line spacing
        commands.addAll(resetLineSpacing().toList())

        return commands.toByteArray()
    }

    /**
     * Open cash drawer (if connected)
     */
    fun openCashDrawer(): ByteArray {
        return byteArrayOf(ESC, 0x70, 0x00, 0x32, 0xFA.toByte())
    }

    /**
     * Beep (if supported)
     */
    fun beep(times: Int = 1, duration: Int = 5): ByteArray {
        return byteArrayOf(ESC, 0x42, times.toByte(), duration.toByte())
    }

    /**
     * Get printer status
     */
    fun getStatus(): ByteArray {
        return byteArrayOf(GS, 0x61, 0x00)
    }

    /**
     * Reset printer
     */
    fun reset(): ByteArray {
        return initPrinter()
    }

    /**
     * Combine multiple commands
     */
    operator fun ByteArray.plus(other: ByteArray): ByteArray {
        return this + other
    }

    /**
     * Helper to create empty space
     */
    fun emptyLine(): ByteArray {
        return byteArrayOf(LF)
    }

    fun emptyLines(count: Int): ByteArray {
        return ByteArray(count) { LF }
    }
}
