![N|Solid](https://github.com/RobertApikyan/LifecycleMvp/blob/develop/intro/LifecycleCover.png?raw=true)

### MinSDK 14+
[![](https://jitpack.io/v/RobertApikyan/LifecycleMvp.svg)](https://jitpack.io/#RobertApikyan/LifecycleMvp) [![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

## LifecycleMvp

It's convenient to write android applications with MVP architecture, because it's simple and lightweight.

LifecycleMvp is implementation of [AbstractMvp](https://github.com/RobertApikyan/AbstractMvp) with [Android Arcitecture Components](https://developer.android.com/topic/libraries/architecture/).
[AbstractMvp](https://github.com/RobertApikyan/AbstractMvp) framework solves a number of issues related with classic MVP implementation. Read more about AbstractMvp [here](https://github.com/RobertApikyan/AbstractMvp). 

#### Let's try it on a [ColorApp](https://github.com/RobertApikyan/ColorsApp)
Here we have a ColorApp. It's main point is to display different colors when the user is tapping on the screen.
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
Here we receiving presenter instance by calling ```LifecycleMvp.from(this, ::ColorPresenter)``` where "this"- is ColorView implementation, ::ColorPresenter is Presenter's constructor's reference. Alternatively it can be writen like this ``` LifecycleMvp.from(this) { ColorPresenter() } ```
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
###### The Important think here is that our presenter is lifecycle persistence. After configuration change, such as rotation we gonna receive the same presenter instance.

###### Step 4. Let's define our ColorPresenter
```kotlin
 class ColorPresenter : LifecyclePresenter<ColorView>() { 
    // Here we hold current display color
    private var currentColor = -1
    
    // This method called when user clicks on mColorBackground
    fun onColorViewClick() {
        // set new color value
        currentColor = ColorGenerator.generateColor()
        // call to change UI color
        onColorChanged()
    }
    
    // This method opens the view scope and send viewAction to view
    private fun onColorChanged() = view {
        setNewColor(currentColor) // UI color will be changed
    }
}
```
Here new think is view{ } method, which receives lambda(```V.() -> Unit```) ViewAction as an argument This viewAction will be invoked by LifecycleMvp framework. 

###### Note. 
``
It's impossible to come up with NullPointerException while trying to access view instance at the time when view is detached from presenter and there is no need to make nullability check every time before accessing view instance.
``
###### Done. Full example of ColorApp is [here](https://github.com/RobertApikyan/ColorsApp) 

#### UNDER THE HOOD
Lets understand what is happening when we call ```view { ... }``` or ``` viewImmediate { ... } ``` methods.
![N|Solid](https://github.com/RobertApikyan/LifecycleMvp/blob/develop/intro/lifecycleMvpSchem.png?raw=true)

1. First when we call ``` view { setNewColor(currentColor) } ``` new ViewAction instance is created, and passed to [ViewActionDispatcherLiveData](https://github.com/RobertApikyan/LifecycleMvp/blob/develop/lifecyclemvp/src/main/java/robertapikyan/com/lifecyclemvp/lifecycle/ViewActionDispatcherLiveData.kt). 

2. [ViewActionDispatcherLiveData](https://github.com/RobertApikyan/LifecycleMvp/blob/develop/lifecyclemvp/src/main/java/robertapikyan/com/lifecyclemvp/lifecycle/ViewActionDispatcherLiveData.kt) is [IViewActionDispatcher](https://github.com/RobertApikyan/AbstractMvp/blob/master/abstractMvp/src/main/java/robertapikyan/com/abstractmvp/presentation/view/IViewActionDispatcher.kt)(from [AbstactMvp](https://github.com/RobertApikyan/AbstractMvp)) implementation with LiveData from Android arcitecture components. It holds viewActions and dispatch them to [ViewActionObserver](https://github.com/RobertApikyan/LifecycleMvp/blob/develop/lifecyclemvp/src/main/java/robertapikyan/com/lifecyclemvp/lifecycle/ViewActionObserver.kt).

3. [ViewActionObserver](https://github.com/RobertApikyan/LifecycleMvp/blob/develop/lifecyclemvp/src/main/java/robertapikyan/com/lifecyclemvp/lifecycle/ViewActionObserver.kt) receives viewActions and invoke them, with passing the view instance.

4. After [ViewActionObserver](https://github.com/RobertApikyan/LifecycleMvp/blob/develop/lifecyclemvp/src/main/java/robertapikyan/com/lifecyclemvp/lifecycle/ViewActionObserver.kt) invokes viewAction, ``` setNewColor(color:Int) ``` method will be called inside ColorActivity.

#### view { ... } and viewImmediate { ... }
When viewAction is created via ``` view { ... } ``` method, [ViewActionDispatcherLiveData](https://github.com/RobertApikyan/LifecycleMvp/blob/develop/lifecyclemvp/src/main/java/robertapikyan/com/lifecyclemvp/lifecycle/ViewActionDispatcherLiveData.kt) will cache the viewActions if view is detached, and send them when view will become attached again. If viewAction is created via ``` viewImmediate{ ... } ``` method, it will be send it only if view is attached, otherwise viewAction will be lost. This method can be used only after presenter's onCreate() lifecycle method call. This method is calling by [AbstactMvp](https://github.com/RobertApikyan/AbstractMvp) framework (Later more detail about LifecyclePresenter lifecycle). This methods can be called from worker thread, but the execution of viewAction will be automatically switched to the main thread.

#### It's comfortable to use view { ... } and viewImmediate { ... } methods with different type of expressions in kotlin language such as if or when.

```kotlin
// this methods are defined inside presenter
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
#### LifecyclePresenter's Lifecycle 
LifecyclePresenter's has five lifecycle methods.

1. First one is ``` onCreate() ```, which is the initial stating point for presenter. As we know this method is called by [AbstactMvp](https://github.com/RobertApikyan/AbstractMvp) framework, when presenter instance is created, since LifecyclePresenter is Activity lifecycle persistence, it will be called only once.  ``` onCreate() ``` is called when [AbstactMvp](https://github.com/RobertApikyan/AbstractMvp) framework finish binding all components together. (more about AbstractMvp components [here](https://github.com/RobertApikyan/AbstractMvp)). Only onCreate() method is related with presenter's lifecycle, upcoming methods are bounded with viewController lifecycle.

2. ``` onViewAttach() ``` This method is called with ViewController's onCreate()
3. ``` onViewStart() ``` This method is called with ViewController's onStart()
4. ``` onViewStop() ``` This method is called with ViewController's onStop()
5. ``` onViewDetach() ``` This method is called with ViewController's onDestroy()

#### LifecycleMvpFactory Class
[AbstactMvp](https://github.com/RobertApikyan/AbstractMvp) framework uses [Mvp.Factory<V,P>](https://github.com/RobertApikyan/AbstractMvp/blob/master/abstractMvp/src/main/java/robertapikyan/com/abstractmvp/presentation/Mvp.kt) factory, in order to get all components instances. [LifecycleMvpFactory](https://github.com/RobertApikyan/LifecycleMvp/blob/develop/lifecyclemvp/src/main/java/robertapikyan/com/lifecyclemvp/lifecycle/LifecycleMvpFactory.kt) implement [Mvp.Factory<V,P>](https://github.com/RobertApikyan/AbstractMvp/blob/master/abstractMvp/src/main/java/robertapikyan/com/abstractmvp/presentation/Mvp.kt)
interface and provides all necessary lifecycle components. If you need to change some component implementation you can inherit from [LifecycleMvpFactory](https://github.com/RobertApikyan/LifecycleMvp/blob/develop/lifecyclemvp/src/main/java/robertapikyan/com/lifecyclemvp/lifecycle/LifecycleMvpFactory.kt) class and override component provider method that you want to change.

### Summary
LifecycleMvp library is AbstractMvp implementation with LiveData, ViewModels and Lifecycle from Android Architecture Components.

## Download
### Gradle 
#### Add to project level build.gradle
```groovy
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```
#### Add dependency to app module level build.gradle
```groovy
dependencies {
    implementation 'com.github.RobertApikyan:LifecycleMvp:1.0.4'
}
```
### Maven
```xml
<repositories>
	<repository>
	<id>jitpack.io</id>
	<url>https://jitpack.io</url>
	</repository>
</repositories>
```
#### Add dependency
```xml
<dependency>
	<groupId>com.github.RobertApikyan</groupId>
	<artifactId>LifecycleMvp</artifactId>
	<version>1.0.4</version>
</dependency>
```

 
### Done.

[![View Robert Apikyan profile on LinkedIn](https://www.linkedin.com/img/webpromo/btn_viewmy_160x33.png)](https://www.linkedin.com/in/robert-apikyan-24b915130/)

License
-------

    Copyright 2018 Robert Apikyan

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.








