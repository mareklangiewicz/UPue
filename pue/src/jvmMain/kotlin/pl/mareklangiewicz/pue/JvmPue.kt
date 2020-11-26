package pl.mareklangiewicz.pue

import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

// TODO LATER: test it (this is just first fast attempt to implement something simple - it will probably be rewritten)
class ExecutorScheduler(private val ses: ScheduledExecutorService) : IScheduler {

    constructor(threads: Int = Runtime.getRuntime().availableProcessors() * 2) : this(Executors.newScheduledThreadPool(threads))

    override fun schedule(delay: Long, action: (Unit) -> Unit): (Cancel) -> Unit {
        val sf = ses.schedule({ action(Unit) }, delay, TimeUnit.MILLISECONDS)
        return { sf.cancel(true) }
    }

    override val now = { _: Unit -> System.currentTimeMillis() }
}

fun <A, V> IPuee<A, V>.sync(): IPuee<A, V> = { a: A -> synchronized(this) { this(a) } }
// it synchronizes calls and assures the happens-before relationship between calls.


/**
 * This operator makes sure all items are pushed serially and that there is the happens-before relationship between pushes.
 */
fun <A, C> IPusher<A, C>.lsync(): IPusher<A, C> = lift { it.sync() }
