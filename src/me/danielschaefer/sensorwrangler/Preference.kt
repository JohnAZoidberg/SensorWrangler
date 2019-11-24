package me.danielschaefer.sensorwrangler

enum class Picker {
    FileOpen, FileSave, Directory, None
}

/**
 * Preferences for the Preferences tab in Settings
 *
 * @see Settings
 *
 * @param description Description of this preference
 * @param explanation Longer explanation, shown as a tooltip
 * @param picker Which file/directory picker to show, for choosing the path, if any
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.PROPERTY)
annotation class Preference(val description: String, val explanation: String, val picker: Picker = Picker.None) {

}
