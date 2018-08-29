![N|Solid](https://github.com/RobertApikyan/LifecycleMvp/blob/develop/intro/LifecycleCover.png?raw=true)

### MinSDK 14+

## LifecycleMvp

It's conviniant to write android applications with MVP arcitecture, because it's simple and lightweight. MVP arcitecture is based on view callbacks mechanisms, but in the other hand android arctiecture componens are much more oriented to MVVM arcitecture, which is based on observer and observable pattern, in our case observable is LiveData inside ViewModel. The real problem with this approach is to have a big number of liveData instances inside ViewModel. For example if we have ten methods inside MVP's view interface, the parallel to it in MVVM will be to have ten liveData instances inside ViewModel, which is not good, because it's not confortable to work with ten liveData instances. LifeycleMvp arcitecture solves this problem, but at the same time it uses LiveData, ViewModles and Lifecycles.

LifeycleMvp is implementation of [AbstractMvp](https://github.com/RobertApikyan/AbstractMvp) with [Android Arcitecture Components](https://developer.android.com/topic/libraries/architecture/).
[AbstractMvp](https://github.com/RobertApikyan/AbstractMvp) framework solves a number of issues related with classic MVP implementation. Read more about AbstractMvp [here](https://github.com/RobertApikyan/AbstractMvp). 





