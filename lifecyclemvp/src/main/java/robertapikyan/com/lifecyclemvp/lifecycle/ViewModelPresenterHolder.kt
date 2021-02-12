package robertapikyan.com.lifecyclemvp.lifecycle

import androidx.lifecycle.ViewModel
import robertapikyan.com.abstractmvp.presentation.presenter.IPresenterHolder

/**
 * ViewModelPresenterHolder is Android lifecycle persistence container for presenter.
 */
class ViewModelPresenterHolder<V : LifecycleView, P : LifecyclePresenter<V>> : ViewModel(), IPresenterHolder<V, P> {

    private val presenters = LinkedHashMap<Any,P>()

    override fun put(presenterKey: Any, presenter: P) {
        this.presenters[presenterKey] = presenter
    }

    override fun get(presenterKey: Any): P {
        return presenters[presenterKey]
                ?: throw IllegalStateException("ViewModelPresenterHolder.get($presenterKey) method is called when, " +
                        "ViewModelPresenterHolder.hasPresenter($presenterKey) = false")
    }

    override fun hasPresenter(presenterKey: Any) = presenters.containsKey(presenterKey)

    override fun onCleared() {
        for (presenter in presenters.values) {
            presenter.onDestroy()
        }
        super.onCleared()
    }
}