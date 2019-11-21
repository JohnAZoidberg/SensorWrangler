package me.danielschaefer.sensorwrangler

enum class Picker {
    FileOpen, FileSave, Directory, None
}

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.PROPERTY)
annotation class Preference(val description: String, val picker: Picker = Picker.None)
