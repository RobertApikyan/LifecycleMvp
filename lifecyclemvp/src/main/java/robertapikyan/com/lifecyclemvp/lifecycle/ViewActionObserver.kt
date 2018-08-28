package robertapikyan.com.lifecyclemvp.lifecycle

import com.robertapikyan.abstractmvp.presentation.view.IViewAction
import com.robertapikyan.abstractmvp.presentation.view.IViewActionObserver
import com.robertapikyan.abstractmvp.presentation.view.ViewHolder

/**
 * Basic implementation of IViewActionObserver
 */
class ViewActionObserver<V : LifecycleView> : IViewActionObserver<V> {

    private lateinit var viewHolder: ViewHolder<V>

    override fun onCreate(viewHolder: ViewHolder<V>) {
        this.viewHolder = viewHolder
    }

    override fun onInvoke(viewAction: IViewAction<V>) {
        if (::viewHolder.isInitialized && viewHolder.hasView()) {
            viewAction.invoke(viewHolder.getView()!!)
        }
    }
}