package robertapikyan.com.lifecyclemvp.lifecycle

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.OnLifecycleEvent
import com.robertapikyan.abstractmvp.presentation.presenter.IPresenterLifecycle
import com.robertapikyan.abstractmvp.presentation.presenter.IPresenterLifecycleHandler

/**
 * PresenterLifecycleHandler is IPresenterLifecycleHandler implementation with LifecycleObserver,
 * which allows to handle lifecycle events.
 */
class PresenterLifecycleHandler :
        IPresenterLifecycleHandler,
        LifecycleObserver {

    private var lifecycle: IPresenterLifecycle? = null

    override fun onCreate(presenterLifecycle: IPresenterLifecycle) {
        lifecycle = presenterLifecycle
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun onCreate() {
        lifecycle?.onViewAttach()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onStart() {
        lifecycle?.onViewStart()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onStop() {
        lifecycle?.onViewStop()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        lifecycle?.onViewDetach()
    }
}