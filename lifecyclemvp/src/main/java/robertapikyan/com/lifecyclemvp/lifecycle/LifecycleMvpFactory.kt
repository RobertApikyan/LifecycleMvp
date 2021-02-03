package robertapikyan.com.lifecyclemvp.lifecycle

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import robertapikyan.com.abstractmvp.presentation.Mvp
import robertapikyan.com.abstractmvp.presentation.presenter.IPresenterHolder
import robertapikyan.com.abstractmvp.presentation.presenter.IPresenterLifecycleHandler
import robertapikyan.com.abstractmvp.presentation.presenter.Presenter

/**
 * LifecycleMvpFactory is implementation of Mvp.Factory. It provide's all lifecycle mvp components,
 * If you need to change some module, you can inherit from this class and provide your custom module
 * implementation
 */
open class LifecycleMvpFactory<V : LifecycleView, P : LifecyclePresenter<V>>(
        private val view: V,
        private val presenterFactory: Presenter.Factory<V, P>
) : Mvp.Factory<V, P> {

    /**
     * @return view, which might be implemented by lifecycle owner fragment or activity
     */
    override fun getView(): V {
        return view
    }

    /**
     * @return ViewActionDispatcher, which inherited from live Data, and able to cache pending events
     */
    override fun getViewActionDispatcher(): ViewActionDispatcherLiveData<V> {
        return ViewActionDispatcherLiveData()
    }

    /**
     * @return ViewActionObserver, which is simple implementation of IViewActionObserver interface
     */
    override fun getViewActionObserver(): ViewActionObserver<V> {
        return ViewActionObserver()
    }

    /**
     * This method is called only once, since presenter instance is persistence per activity scope.
     * @return LifecyclePresenter
     */
    override fun getPresenter(): P {
        return presenterFactory.createPresenter()
    }

    /**
     * Method returns IPresenterHolder implementation with ViewModel from android arc. components
     * @return ViewModelPresenterHolder
     */
    @Suppress("UNCHECKED_CAST")
    override fun getPresenterHolder(): IPresenterHolder<V, P> = when (view) {
        is androidx.fragment.app.Fragment -> ViewModelProvider(view as androidx.fragment.app.Fragment)
                .get(ViewModelPresenterHolder::class.java) as ViewModelPresenterHolder<V, P>
        is androidx.fragment.app.FragmentActivity -> ViewModelProvider(view as androidx.fragment.app.FragmentActivity)
                .get(ViewModelPresenterHolder::class.java) as ViewModelPresenterHolder<V, P>
        else -> throw IllegalArgumentException(view::class.java.name +
                " must be implemented by Fragment or FragmentActivity")
    }

    /**
     * Method returns IPresenterLifecycleHandler implementation with LifecycleObserver from
     * android arc. components.
     * @return PresenterLifecycleHandler
     */
    override fun getPresenterLifecycleHandler(): IPresenterLifecycleHandler {
        assertViewIsNotLifecycleOwner()
        val lifecycleOwner = view as LifecycleOwner
        val presenterLifecycleHandler = PresenterLifecycleHandler()
        lifecycleOwner.lifecycle.addObserver(presenterLifecycleHandler)
        return presenterLifecycleHandler
    }

    protected fun assertViewIsNotLifecycleOwner() {
        if (view !is LifecycleOwner)
            throw IllegalStateException(view::class.java.name + " must be implemented by LifecycleOwner Activity or Fragment")
    }
}