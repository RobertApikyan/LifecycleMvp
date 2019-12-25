package robertapikyan.com.lifecyclemvp.lifecycle

import android.arch.lifecycle.ViewModel
import robertapikyan.com.abstractmvp.presentation.presenter.IPresenterHolder

/**
 * ViewModelPresenterHolder is Android lifecycle persistence container for presenter.
 */
class ViewModelPresenterHolder<V : LifecycleView, P : LifecyclePresenter<V>> : ViewModel(), IPresenterHolder<V, P> {

    private lateinit var presenter: P

    override fun put(presenter: P) {
        this.presenter = presenter
    }

    override fun get(): P {
        assertPresenterNotNull()
        return presenter
    }

    override fun hasPresenter() = ::presenter.isInitialized

    override fun onCleared() {
        if (this::presenter.isInitialized){
            presenter.onDestroy()
        }
        super.onCleared()
    }

    private fun assertPresenterNotNull() {
        // actually this will never happen
        if (!hasPresenter())
            throw IllegalStateException("ViewModelPresenterHolder.get() method is called when, " +
                    "ViewModelPresenterHolder.hasPresenter() = false")
    }
}