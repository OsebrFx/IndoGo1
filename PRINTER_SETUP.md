# Thermal Printer Setup Guide

## Gainsha GA-E200I Thermal Printer Integration

This document provides detailed instructions for setting up and using the thermal printer functionality in the IndoGo flight booking application.

---

## Table of Contents

1. [Overview](#overview)
2. [Printer Specifications](#printer-specifications)
3. [Prerequisites](#prerequisites)
4. [Setup Instructions](#setup-instructions)
5. [WiFi Configuration](#wifi-configuration)
6. [Bluetooth Configuration](#bluetooth-configuration)
7. [Using the Print Functionality](#using-the-print-functionality)
8. [Troubleshooting](#troubleshooting)
9. [Technical Details](#technical-details)

---

## Overview

The IndoGo app now supports thermal printing of flight tickets using the **Gainsha GA-E200I** thermal printer. The printer supports:

- **WiFi** connection
- **Bluetooth** connection
- **ESCIP05** command protocol
- **58mm** and **80mm** paper widths
- QR code generation and printing
- Multiple copies printing
- Full and simple receipt formats

---

## Printer Specifications

| Specification | Details |
|--------------|---------|
| **Model** | Gainsha GA-E200I |
| **Protocol** | ESCIP05 (ESC/POS compatible) |
| **Connection** | WiFi, Bluetooth |
| **Paper Width** | 58mm / 80mm thermal paper |
| **Resolution** | 203 DPI (dots per inch) |
| **Print Speed** | Up to 90mm/s |

---

## Prerequisites

### Hardware Requirements
- Gainsha GA-E200I thermal printer
- Thermal paper rolls (58mm or 80mm)
- Android device (Android 7.0 or higher)

### Software Requirements
- IndoGo app installed on Android device
- Required permissions granted (Bluetooth, WiFi, Location)

---

## Setup Instructions

### 1. First-Time Setup

1. **Install the App**
   - Install the IndoGo app on your Android device
   - Grant all required permissions when prompted

2. **Power On the Printer**
   - Ensure the printer is powered on
   - Load thermal paper correctly
   - Wait for the printer to initialize (ready light should be solid)

3. **Open Printer Settings**
   - Open the IndoGo app
   - Tap the **Settings icon** (gear icon) in the top menu
   - Select **Printer Settings**

---

## WiFi Configuration

### Step 1: Connect Printer to WiFi Network

1. **Configure Printer WiFi** (refer to printer manual)
   - Press and hold the printer's WiFi button
   - Connect to the printer's WiFi hotspot from your phone
   - Use the printer's web interface to connect it to your WiFi network
   - Note the IP address assigned to the printer

### Step 2: Configure App

1. Open **Printer Settings** in the IndoGo app
2. Select **WiFi** as the connection type
3. Enter the printer's **IP address** (e.g., `192.168.1.100`)
4. Enter the **Port** (default: `9100`)
5. Select **Paper Width** (58mm or 80mm)
6. Tap **Test** to test the connection
7. If successful, tap **Save**

### WiFi Configuration Example

```
IP Address: 192.168.11.200  (DEFAULT - Pre-configured)
Port: 9100
Paper Width: 80mm
```

### ⚠️ IMPORTANT - Configuration actuelle de votre imprimante

Selon les informations de votre imprimante:
- **IP**: 192.168.11.200 ✅ (Déjà configuré dans l'app)
- **Port**: 9100 ✅ (Déjà configuré dans l'app)
- **Masque**: 255.255.255.0
- **Passerelle**: 192.168.11.1
- **Statut PHY**: **DISCONNECTED** ⚠️

**PROBLÈME ACTUEL: PHY Connect status: Disconnected**

Cela signifie que le **câble réseau RJ45 n'est PAS branché** à l'imprimante.

**SOLUTION RAPIDE:**
1. Branchez un câble Ethernet RJ45 de l'imprimante à votre routeur/box WiFi
2. Vérifiez que le voyant du port Ethernet s'allume
3. Le statut devrait passer à "PHY Connect status: Connected"
4. L'application se connectera automatiquement à 192.168.11.200:9100

**Note:** L'application est déjà pré-configurée avec votre adresse IP. Il suffit de brancher le câble !

### Common WiFi IP Addresses
- **Votre configuration actuelle**: `192.168.11.200` (Ethernet)
- If using printer's hotspot: `192.168.192.168`
- If on local network: Check printer display or print configuration page

---

## Bluetooth Configuration

### Step 1: Pair the Printer

1. **Enable Bluetooth** on your Android device
2. **Turn on the printer** and enable Bluetooth pairing mode
   - Press and hold the Bluetooth button on the printer
   - The Bluetooth LED should blink
3. **Pair from Android Settings**
   - Go to Android Settings > Bluetooth
   - Search for devices
   - Select your printer (e.g., "GA-E200I" or similar)
   - Enter pairing code if prompted (usually `0000` or `1234`)
   - Wait for "Paired" status

### Step 2: Configure App

1. Open **Printer Settings** in the IndoGo app
2. Select **Bluetooth** as the connection type
3. Select your printer from the **Paired Devices** dropdown
4. Select **Paper Width** (58mm or 80mm)
5. Tap **Test** to test the connection
6. If successful, tap **Save**

### Permissions Required for Bluetooth

The app will request these permissions:
- **Bluetooth** - To communicate with the printer
- **Location** - Required for Bluetooth device discovery on Android 10+
- **Nearby devices** - Required on Android 12+

---

## Using the Print Functionality

### Printing a Ticket

1. **Select a Flight**
   - Browse flights in the main screen
   - Tap on a flight to view ticket details

2. **View Ticket Details**
   - Review passenger information
   - Check boarding details
   - Verify QR code is displayed

3. **Print the Ticket**
   - Tap the **Print Ticket** floating action button (FAB)
   - Choose print format:
     - **Full Ticket** - Complete ticket with all details
     - **Simple Receipt** - Compact version
     - **Test Page** - Test printer connectivity
   - Select number of copies (1, 2, or 3)
   - Confirm the print

4. **Collect Printed Ticket**
   - Wait for printing to complete
   - Check the printed ticket for clarity
   - The printer will auto-cut the paper

### Print Formats

#### Full Ticket Format
- Airline name and logo
- Flight route (DEL → BLR)
- Passenger details (name, PNR, booking reference)
- Flight information (number, class, date)
- Boarding details (seat, gate, terminal, time)
- Baggage allowance
- Payment details
- QR code for scanning
- Terms and conditions

#### Simple Receipt Format
- Airline name
- Flight route
- Essential details only
- Smaller QR code
- Compact layout

---

## Troubleshooting

### Connection Issues

#### WiFi Connection Failed

**Problem:** Cannot connect to printer via WiFi

**Solutions:**
1. Verify printer is on the same WiFi network
2. Check the IP address is correct
3. Ensure firewall is not blocking port 9100
4. Try pinging the printer IP from your device
5. Restart the printer and try again
6. Check if printer WiFi is enabled

**Test Command:**
```bash
# From Android terminal or ADB
ping <printer-ip-address>
```

#### Bluetooth Connection Failed

**Problem:** Cannot connect to printer via Bluetooth

**Solutions:**
1. Ensure printer is paired in Android Bluetooth settings
2. Check if Bluetooth is enabled on both devices
3. Remove pairing and pair again
4. Make sure printer is in range (within 10 meters)
5. Restart Bluetooth on your device
6. Grant all required permissions
7. Check if another device is connected to the printer

### Print Quality Issues

#### Faded or Light Prints

**Solutions:**
1. Increase print density in settings
2. Check thermal paper quality
3. Clean the print head
4. Replace thermal paper if expired
5. Adjust printer temperature settings

#### Partial Prints

**Solutions:**
1. Ensure sufficient paper is loaded
2. Check for paper jams
3. Verify correct paper width setting (58mm vs 80mm)
4. Clean paper feed rollers

### QR Code Issues

#### QR Code Not Scanning

**Solutions:**
1. Increase QR code size in print settings
2. Improve print density
3. Ensure adequate lighting when scanning
4. Use a dedicated QR code scanner app
5. Clean the print head for better clarity

### Paper Issues

#### Paper Jam

**Solutions:**
1. Turn off the printer
2. Open the printer cover
3. Carefully remove jammed paper
4. Check for torn paper pieces
5. Reload paper correctly
6. Close cover and restart

#### Out of Paper

**Indicator:** Printer status shows "Paper Out"

**Solution:**
1. Load new thermal paper roll
2. Ensure paper is inserted correctly
3. Close the paper cover properly
4. Wait for printer to detect paper

---

## Technical Details

### Architecture

```
IndoGo App
├── UI Layer
│   ├── MainActivity (Flight list)
│   ├── TicketDetailActivity (Ticket view & print)
│   └── PrinterSettingsActivity (Configuration)
├── Service Layer
│   ├── ThermalPrinterService (Main service)
│   ├── PrinterConnectionManager (WiFi & Bluetooth)
│   └── TicketPrintFormatter (Format tickets)
├── Protocol Layer
│   └── ESCIP05Commands (Printer commands)
└── Models
    ├── Ticket (Ticket data)
    ├── PrinterConfig (Configuration)
    └── PrinterStatus (Status tracking)
```

### Supported Commands

The app implements the following ESCIP05 commands:

| Command | Purpose |
|---------|---------|
| `ESC @` | Initialize printer |
| `ESC a n` | Set text alignment |
| `GS ! n` | Set text size |
| `ESC E n` | Set bold mode |
| `GS k m d` | Print barcode |
| `GS ( k` | Print QR code |
| `GS V m` | Cut paper |
| `ESC d n` | Feed paper |

### Paper Specifications

#### 58mm Paper
- Width: 58mm ± 0.5mm
- Diameter: ≤ 50mm
- Core diameter: 12mm
- Print width: 48mm (384 dots)

#### 80mm Paper
- Width: 80mm ± 0.5mm
- Diameter: ≤ 80mm
- Core diameter: 12mm
- Print width: 72mm (576 dots)

### Network Configuration

#### WiFi Connection
- **Protocol:** TCP/IP
- **Port:** 9100 (default)
- **Timeout:** 5 seconds
- **Auto-reconnect:** Enabled

#### Bluetooth Connection
- **Protocol:** SPP (Serial Port Profile)
- **UUID:** `00001101-0000-1000-8000-00805F9B34FB`
- **Auto-reconnect:** Enabled

### Permissions Required

```xml
<!-- Bluetooth -->
<uses-permission android:name="android.permission.BLUETOOTH" />
<uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
<uses-permission android:name="android.permission.BLUETOOTH_CONNECT" />
<uses-permission android:name="android.permission.BLUETOOTH_SCAN" />

<!-- Network -->
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />

<!-- Location (required for Bluetooth on Android 10+) -->
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
```

---

## FAQs

### Q: Can I use other thermal printer models?

**A:** This implementation is optimized for the Gainsha GA-E200I. Other ESC/POS compatible printers may work but are not officially supported.

### Q: How do I print multiple tickets at once?

**A:** Select the number of copies (1-3) when printing. For more copies, print multiple times.

### Q: Can I customize the ticket layout?

**A:** Yes, modify `TicketPrintFormatter.kt` to customize the layout. The full ticket and simple receipt formats can be adjusted.

### Q: Does the printer need to stay connected?

**A:** The printer automatically disconnects after printing to save power. It reconnects when needed.

### Q: How do I update printer firmware?

**A:** Refer to the Gainsha GA-E200I user manual for firmware update procedures. The app does not support firmware updates.

### Q: What if I get a "Printer Offline" error?

**A:** Check that the printer is powered on, has paper, and is connected to the same network (WiFi) or paired (Bluetooth).

---

## Support

For technical support:
- **App Issues:** Contact IndoGo support
- **Printer Issues:** Refer to Gainsha GA-E200I manual
- **Connection Issues:** Check network/Bluetooth settings

---

## Version History

| Version | Date | Changes |
|---------|------|---------|
| 1.0.0 | 2025-11-14 | Initial release with thermal printer support |

---

## License

This implementation is part of the IndoGo flight booking application.

© 2025 IndoGo. All rights reserved.
