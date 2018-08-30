![N|Solid](https://github.com/RobertApikyan/LifecycleMvp/blob/develop/intro/LifecycleCover.png?raw=true)

### MinSDK 14+

## LifecycleMvp

It's conviniant to write android applications with MVP arcitecture, because it's simple and lightweight.

LifeycleMvp is implementation of [AbstractMvp](https://github.com/RobertApikyan/AbstractMvp) with [Android Arcitecture Components](https://developer.android.com/topic/libraries/architecture/).
[AbstractMvp](https://github.com/RobertApikyan/AbstractMvp) framework solves a number of issues related with classic MVP implementation. Read more about AbstractMvp [here](https://github.com/RobertApikyan/AbstractMvp). 

#### Let's try it on a ColorApp ...
Here we have ColorApp, which main point is to display different color when the user is tapping on the screen.
It has only one activity, and for that activity we gonna create ColorView interface and ColorPresenter class.

###### Step 1. Creating ColorView and ColorPresenter.
```kotlin
// View
// inherit from LifecycleView
interface ColorView : LifecycleView { 
    fun setNewColor(color: Int) // Set the color value as a view background
}
// Presenter
// inherit from LifecyclePresenter and define View generic type as a ColorView
class ColorPresenter : LifecyclePresenter<ColorView>() { 
    ...
}
```

###### Step 2. Implement ColorView interface by ColorActivity
```kotlin
class ColorActivity : AppCompatActivity(), ColorView {
  ...
  fun setNewColor(color:Int){
    mBackgroundView.setBackgroundColor(color)
  }
  ...
}
```

###### Step 3. Receive presenter instance in ColorActivity
Here we receiving presenter instance by calling ```LifecycleMvp.from(this, ::ColorPresenter)``` where "this" is ColorView implementation, ::ColorPresenter is Presneter's factory lambda. 
```kotlin
...
override fun onCreate(savedInstanceState: Bundle?) {
        ...
        // Receiving presenter instance 
        presenter = LifecycleMvp.from(this, ::ColorPresenter)
        mColorChangeView.setOnClickListener {
           presenter.onColorViewClick()
        }
}
...
```
The Important think here is that our presenter is lifecycle persistance. After configuration change, such as rotation we gonna receive the same presenter instance. 

###### Step 4. Let's define our ColorPresenter
```kotlin
 class ColorPresenter : LifecyclePresenter<ColorView>() { 
    // Here we hold current display color
    private var currentColor = -1
    
    // this method called when user clicks on mColorBackground
    fun onColorViewClick() {
        // set new color value
        currentColor = ColorGenerator.generateColor()
        // call to change UI color
        onColorChanged()
    }
    
    // this method opens the view scope and send viewAction to view
    private fun onColorChanged() = view {
        setNewColor(currentColor) // UI color will be changed
    }
}
```
###### Done. Full example of ColorApp is here 
Here new think is view{ } method, which receives lambda(```V.() -> Unit```) ViewAction as an argument This viewAction will be invoked by LifecycleMvp framework. 

###### Note. 
``
It's impossible to come up with NullPointerException while trying to access view instance at the time when view is detached from presenter and there is no need to make nullability check every time before accessing view instance.
``
#### UNDER THE HOOD
Lets understand what is heppening when we call ```view { ... }``` or ``` viewImmediate { ... } ``` methods.
![N|Solid](https://github.com/RobertApikyan/LifecycleMvp/blob/develop/intro/lifecycleMvpSchem.png?raw=true)

1. At first when we call ``` view { setNewColor(currentColor) } ``` new ViewAction instance is created, and passed to [ViewActionDispatcherLiveData](https://github.com/RobertApikyan/LifecycleMvp/blob/develop/lifecyclemvp/src/main/java/robertapikyan/com/lifecyclemvp/lifecycle/ViewActionDispatcherLiveData.kt). 
2. [ViewActionDispatcherLiveData](https://github.com/RobertApikyan/LifecycleMvp/blob/develop/lifecyclemvp/src/main/java/robertapikyan/com/lifecyclemvp/lifecycle/ViewActionDispatcherLiveData.kt) is [IViewActionDispatcher](https://github.com/RobertApikyan/AbstractMvp/blob/master/abstractMvp/src/main/java/robertapikyan/com/abstractmvp/presentation/view/IViewActionDispatcher.kt)(from [AbstactMvp](https://github.com/RobertApikyan/AbstractMvp)) implementation with LiveData from Android arc. components. It holds viewActions and dipatch them to [ViewActionObserver](https://github.com/RobertApikyan/LifecycleMvp/blob/develop/lifecyclemvp/src/main/java/robertapikyan/com/lifecyclemvp/lifecycle/ViewActionObserver.kt) 
3.[ViewActionObserver](https://github.com/RobertApikyan/LifecycleMvp/blob/develop/lifecyclemvp/src/main/java/robertapikyan/com/lifecyclemvp/lifecycle/ViewActionObserver.kt) receives viewActions and invoke them, with passing the view instance.
4.After [ViewActionObserver](https://github.com/RobertApikyan/LifecycleMvp/blob/develop/lifecyclemvp/src/main/java/robertapikyan/com/lifecyclemvp/lifecycle/ViewActionObserver.kt) invokes viewAction, ``` setNewColor(color:Int) ``` method will be called inside ColorActivity.

##### view { ... } and viewImmediate { ... }
When viewAction is created via ``` view { ... } ``` method, [ViewActionDispatcherLiveData](https://github.com/RobertApikyan/LifecycleMvp/blob/develop/lifecyclemvp/src/main/java/robertapikyan/com/lifecyclemvp/lifecycle/ViewActionDispatcherLiveData.kt) will cache the viewActions if view is detached, and send them when view will become attached again. If viewAction is created via ``` viewImmediate{ ... } ``` method, it will be send it only if view is attached, otherwise viewAction will be lost.

##### It's conveniant way to use view { ... } and view { ... } methods with different type of expressions in kotlin language such as if or when.

```kotlin
// this method is defined inside presenter
...
// using view { ... } with IF
fun onComplete(items:List<Item>) {
    if(items.isEmpty()) view {
        showEmptyResult()
    } else view {
        showItems(items)
    }
}
// using view { ... } with WHEN
fun onComplete(genre:FilmGenres) = when (genre) {
            FilmGenres.HORROR -> view { 
                showHorrors()
            }
            FilmGenres.COMEDY -> view { 
                showComedy()
            }
            FilmGenres.ROMANCE -> view {
                showRomance()
            }
        }
...
```






