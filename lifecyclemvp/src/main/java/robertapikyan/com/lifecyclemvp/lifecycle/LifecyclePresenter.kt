package robertapikyan.com.lifecyclemvp.lifecycle

import com.robertapikyan.abstractmvp.presentation.presenter.Presenter
import com.robertapikyan.abstractmvp.presentation.view.ActionType
import com.robertapikyan.abstractmvp.presentation.view.IViewAction

/**
 * LifecyclePresenter is base class for LifecycleMvp library.
 * In order to create presenter inherit it from LifecyclePresenter,
 * and use view {} scope methods in order to create viewAction and
 * to get access to view.
 * do not call view{} scope methods inside the presenter constructor,
 * It will throw IllegalStateException seance viewActionDispatcher is not initialized yet.
 * If you do not want to throw exception override throwIfPresenterIsNotCreated() method and return false value.
 * Make your initial viewActions after super.setViewActionObserver(viewActionDispatcher) method call, after that
 * point viewActionDispatcher will be initialized.
 */
open class LifecyclePresenter<V : LifecycleView> : Presenter<V>() {

    /**
     * Use this method in order to define viewAction, which by default will be sent with ActionType.STICKY.
     * @param action ViewAction
     */
    protected fun view(action: V.() -> Unit) =
            view(ActionType.STICKY, action)

    /**
     * Use this method in order to define viewAction
     * @param actionType:ActionType, define SIMPLE, STICKY
     * @param action ViewAction
     */
    protected fun view(actionType: ActionType,
                       action: V.() -> Unit) =
            view(actionType, IViewAction.fromLambda(action))

    protected fun view(actionType: ActionType = ActionType.STICKY,
                       action: IViewAction<V>) {
        viewActionDispatcher.onViewAction(actionType, action)
    }

    /**
     * Use this method in order to define viewAction,
     * which by default will be sent as IMMEDIATE action
     */
    protected fun viewImmediate(action: V.() -> Unit) = view(ActionType.IMMEDIATE, action)
}