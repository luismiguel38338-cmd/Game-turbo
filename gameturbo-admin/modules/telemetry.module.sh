#!/usr/bin/env bash
# Módulo de verificación de telemetría local (solo lectura del proyecto)

module_telemetry_info() {
  echo "  [telemetry] Lectores activos: SystemTelemetryReader.kt"
  echo "  [telemetry] APIs oficiales: ActivityManager, StatFs, BatteryManager, PowerManager"
}
