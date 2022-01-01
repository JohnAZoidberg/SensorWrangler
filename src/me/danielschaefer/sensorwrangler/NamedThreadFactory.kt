package me.danielschaefer.sensorwrangler

import java.util.concurrent.ThreadFactory

// TODO: Combine similar thread into one pool
//       See: Implementation of DefaultThreadFactory in Executors.java
class NamedThreadFactory constructor(var threadName: String) : ThreadFactory {
    private val group: ThreadGroup
    override fun newThread(r: Runnable): Thread {
        return Thread(group, r, threadName, 0).apply {
            isDaemon = false
            priority = Thread.NORM_PRIORITY
        }
    }

    init {
        val s = System.getSecurityManager()
        group = if (s != null) s.threadGroup else Thread.currentThread().threadGroup
    }
}
