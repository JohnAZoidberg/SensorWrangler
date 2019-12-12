package me.danielschaefer.sensorwrangler.sensors;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

// Needs to be in Java because kotlin does not support SAM for interfaces defined in Kotlin
// See https://youtrack.jetbrains.com/issue/KT-7770
@FunctionalInterface
public interface ConnectionChangeListener {
    void onChanged(@NotNull Sensor sensor, @NotNull Boolean connected, @Nullable String reason);
}
