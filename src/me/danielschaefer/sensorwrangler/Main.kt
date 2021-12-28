package me.danielschaefer.sensorwrangler

import javafx.application.Application
import me.danielschaefer.sensorwrangler.javafx.App

class Main {
  companion object {
    @JvmStatic
    fun main(args: Array<String>) {
        Application.launch(App::class.java, *args)
    }
  }
}
