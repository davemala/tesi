# python
"""
ble_hr_post.py
- Scans for a BLE device by substring of its name (or uses an address arg).
- Subscribes to the Heart Rate Measurement characteristic (0x2A37).
- Parses heart rate value and posts JSON { "bpm": <int>, "timestamp": <ms> } to http://localhost:8080/api/hr
"""

import asyncio
import struct
import time
import sys
import requests
from bleak import BleakScanner, BleakClient

HR_CHAR_UUID = "00002a37-0000-1000-8000-00805f9b34fb"
POST_URL = "http://localhost:8080/api/hr"  # change if your server runs elsewhere

def parse_heart_rate(data: bytearray) -> int:
    # per BLE spec: first byte = flags, bit0 = HR value format (0 = 8-bit, 1 = 16-bit)
    flags = data[0]
    hr_format_16 = (flags & 0x01) != 0
    if hr_format_16:
        hr = struct.unpack_from("<H", data, 1)[0]
    else:
        hr = data[1]
    return int(hr)

def post_reading(bpm: int):
    payload = {"bpm": bpm, "timestamp": int(time.time() * 1000)}
    try:
        resp = requests.post(POST_URL, json=payload, timeout=5)
        resp.raise_for_status()
        print(f"Posted: {payload}")
    except Exception as e:
        print(f"POST failed: {e}")

def hr_notification_handler(sender: int, data: bytearray):
    try:
        bpm = parse_heart_rate(data)
        print(f"Received HR: {bpm} bpm")
        # offload blocking POST to thread pool so bleak event loop isn't blocked
        loop = asyncio.get_event_loop()
        loop.run_in_executor(None, post_reading, bpm)
    except Exception as e:
        print(f"Error parsing HR: {e}")

async def run(target_name_or_addr: str | None = None):
    print("Scanning for BLE devices (5s)...")
    devices = await BleakScanner.discover(timeout=5.0)
    device = None
    if target_name_or_addr:
        # try to find by exact address first, then by name substring
        for d in devices:
            if d.address.lower() == target_name_or_addr.lower() or (d.name and target_name_or_addr.lower() in d.name.lower()):
                device = d
                break
    else:
        # if no target provided, pick the first device advertising a name that looks like a HRM
        for d in devices:
            if d.name and any(tok in d.name.lower() for tok in ("heartrate", "hr", "polar", "wahoo", "garmin", "suunto")):
                device = d
                break

    if device is None:
        print("No target device found. Devices discovered:")
        for d in devices:
            print(f"  {d.address} - {d.name}")
        return

    print(f"Connecting to {device.address} ({device.name}) ...")
    async with BleakClient(device.address) as client:
        if not client.is_connected:
            print("Failed to connect")
            return
        print("Connected. Subscribing to HR characteristic...")
        await client.start_notify(HR_CHAR_UUID, hr_notification_handler)
        print("Subscribed. Waiting for notifications (Ctrl-C to exit)...")
        try:
            while True:
                await asyncio.sleep(1.0)
        except asyncio.CancelledError:
            pass
        finally:
            await client.stop_notify(HR_CHAR_UUID)

if __name__ == "__main__":
    target = sys.argv[1] if len(sys.argv) > 1 else None
    try:
        asyncio.run(run(target))
    except KeyboardInterrupt:
        print("Exiting.")
