package robertapikyan.com.lifecyclemvp.lifecycle

import robertapikyan.com.abstractmvp.presentation.Mvp
import robertapikyan.com.abstractmvp.presentation.presenter.Presenter

/**
 * Helper methods for receiving presenter instance.
 */
class LifecycleMvp {

    companion object {
        /**
         * Basic Usage
         * val presenter = LifecycleMvp.from(this,::LoginPresenter)
         * 'this' is activity or fragment with implement's Login interface
         */
        fun <V : LifecycleView, P : LifecyclePresenter<V>> from(view: V,
                                                                presenterKey: String,
                                                                presenterFactory: () -> P) =
                from(view, presenterKey, Presenter.Factory.fromLambda(presenterFactory))

        inline fun <V : LifecycleView, reified P : LifecyclePresenter<V>> from(view: V,
                                                                               noinline presenterFactory: () -> P) =
                from(view, P::class.java.canonicalName
                        ?: P::class.java.name, Presenter.Factory.fromLambda(presenterFactory))

        fun <V : LifecycleView, P : LifecyclePresenter<V>> from(view: V,
                                                                presenterKey: String,
                                                                presenterFactory: Presenter.Factory<V, P>) =
                from(LifecycleMvpFactory(view, presenterKey, presenterFactory))

        inline fun <V : LifecycleView, reified P : LifecyclePresenter<V>> from(view: V,
                                                                               presenterFactory: Presenter.Factory<V, P>) =
                from(LifecycleMvpFactory(view, P::class.java.canonicalName
                        ?: P::class.java.name, presenterFactory))

        /**
         * Use this method and provide custom LifecycleMvpFactory class
         */
        fun <V : LifecycleView, P : LifecyclePresenter<V>> from(lifecycleMvpFactory: LifecycleMvpFactory<V, P>) =
                Mvp.from(lifecycleMvpFactory)
    }
}