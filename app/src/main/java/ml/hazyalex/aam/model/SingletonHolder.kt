package ml.hazyalex.aam.model

open class SingletonHolder<out T : Any, in A>(creator: (A) -> T) {
    private var creator: ((A) -> T)? = creator

    @Volatile
    private var instance: T? = null

    fun getInstance(arg: A): T {
        return instance ?: run {
            synchronized(this) {
                instance ?: run {
                    creator!!(arg).also {
                        instance = it
                        creator = null
                    }
                }
            }
        }
    }
}
