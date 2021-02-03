package robertapikyan.com.lifecyclemvp.lifecycle

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import robertapikyan.com.abstractmvp.presentation.view.*
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.locks.ReentrantLock

/**
 * ViewActionDispatcherLiveData is  IViewActionDispatcher implementation with LiveData and
 * ability to cache view actions when LiveData is not active.
 * ViewActions caching is implemented thread safe, and guaranty that cached view Action will be
 * delivered to view, when it's become active again
 */
class ViewActionDispatcherLiveData<V : IView> : LiveData<IViewAction<V>>(),
        IViewActionDispatcher<V> {

    private companion object {
        const val START_VERSION = -1L
    }

    @Volatile
    private var isActive = false

    @Volatile
    private lateinit var viewActionObserver: IViewActionObserver<V>

    private val pending = AtomicBoolean(false)
    private val dispatching = AtomicBoolean(false)
    private val uiHandler by lazy { Handler(Looper.getMainLooper()) }

    private val pendingActions: Queue<IViewAction<V>>
            by lazy { LinkedList<IViewAction<V>>() }

    private val lock by lazy { ReentrantLock() }

    private var currentVersion = START_VERSION // version will be incremented every time when onActive is called

    /**
     * This method is called by MVP library
     */
    override fun setViewActionObserver(viewHolder: ViewHolder<V>, viewActionObserver: IViewActionObserver<V>) {
        val view = viewHolder.getView()
                ?: throw IllegalStateException("View is null in ViewActionDispatcherLiveData::setViewActionObserver")

        if (view !is LifecycleOwner) throw IllegalArgumentException(view::class.java.canonicalName
                ?: "View"
                +
                " might be implemented by LifecycleOwner activity or fragment")

        this.viewActionObserver = viewActionObserver

        observe(view, Observer {
            if (pending.compareAndSet(true, false) &&
                    it != null) {

                if (it !is DisposableViewAction<V>)
                    dispatchToObserver(it)

                if (it is DisposableViewAction<V> && !it.isVersionChanged(currentVersion))
                    dispatchToObserver(it)
            }
        })
    }

    override fun onViewAction(actionType: ActionType,
                              viewAction: IViewAction<V>) {
        when (actionType) {
            ActionType.STICKY -> sendSticky(viewAction)
            ActionType.IMMEDIATE -> sendImmediate(DisposableViewAction(currentVersion, viewAction))
        }
    }

    override fun onActive() {
        currentVersion++
        isActive = true
        performPendingActions()
    }

    override fun onInactive() {
        isActive = false
    }

    override fun setValue(value: IViewAction<V>?) {
        pending.set(true)
        super.setValue(value)
    }

    override fun postValue(value: IViewAction<V>?) {
        uiHandler.post {
            setValue(value)
        }
    }

    private fun dispatchToObserver(it: IViewAction<V>) {
        dispatching.set(true)
        viewActionObserver.onInvoke(it)
        dispatching.set(false)
    }

    private fun performPendingActions() {
        if (pendingActions.isEmpty()) return

        lock.lock()

        while (!pendingActions.isEmpty())
            pendingActions.poll()?.run(::sendSticky)

        lock.unlock()
    }

    private fun sendImmediate(viewAction: IViewAction<V>) {
        if (dispatching.get()) {
            uiHandler.post {
                value = viewAction
            }
        } else if (!isMainThread()) {
            postValue(viewAction)
        } else {
            value = viewAction
        }
    }

    private fun sendSticky(viewAction: IViewAction<V>) {
        if (isActive) {
            sendImmediate(viewAction)
        } else {
            addPendingAction(viewAction)
        }
    }

    private fun addPendingAction(viewAction: IViewAction<V>) {
        lock.lock()
        pendingActions.add(viewAction)
        lock.unlock()
    }

    private fun isMainThread() = Looper.getMainLooper().thread == Thread.currentThread()

    private class DisposableViewAction<V : IView>(
            private val version: Long,
            private val viewAction: IViewAction<V>) : IViewAction<V> {

        fun isVersionChanged(currentVersion: Long) = version != currentVersion

        override fun invoke(view: V) {
            viewAction.invoke(view)
        }
    }
}