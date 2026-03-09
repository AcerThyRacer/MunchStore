package com.sugarmunch.app.util

import android.content.Context
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import java.lang.ref.WeakReference

/**
 * Context utility functions to prevent memory leaks.
 *
 * Common causes of context leaks:
 * 1. Storing Activity/Fragment context in long-lived objects
 * 2. Static references to views with context
 * 3. Anonymous inner classes holding context references
 * 4. Threads/runnables with context references
 * 5. Singletons holding activity context instead of application context
 *
 * Usage:
 * ```
 * // Good: Use application context
 * val appContext = context.applicationContext
 *
 * // Good: Use WeakReference for views
 * val viewRef = WeakReference(myView)
 *
 * // Bad: Don't store activity context in singleton
 * // val context = activity  // LEAK!
 * ```
 */
object ContextUtils {

    /**
     * Safely get application context.
     * Always returns the application context, never an activity context.
     */
    fun getApplicationContext(context: Context): Context {
        return context.applicationContext
    }

    /**
     * Check if context is safe to use (not from destroyed activity).
     * Returns true if context is application context or activity is not finishing.
     */
    fun isContextSafe(context: Context): Boolean {
        return when (context) {
            is android.app.Activity -> !context.isFinishing && !context.isDestroyed
            is android.app.Service -> true // Services are generally safe
            else -> true // Application context is always safe
        }
    }

    /**
     * Wrap a view in a WeakReference to prevent leaks.
     * Useful for storing view references in long-lived objects.
     */
    fun weakView(view: View?): WeakReference<View> {
        return WeakReference(view)
    }

    /**
     * Safely access a view from WeakReference.
     * Returns null if view was garbage collected.
     */
    fun <T : View> weakViewGet(ref: WeakReference<View>?): T? {
        @Suppress("UNCHECKED_CAST")
        return ref?.get() as T?
    }

    /**
     * Execute a block only if view is still valid.
     */
    inline fun <T : View> WeakReference<T>.withView(block: (T) -> Unit) {
        get()?.let { if (!it.isAttachedToWindow || it.context != null) block(it) }
    }

    /**
     * Lifecycle-aware context wrapper.
     * Automatically clears context reference when lifecycle ends.
     */
    class LifecycleContext(
        private val lifecycleOwner: LifecycleOwner,
        private val contextProvider: () -> Context?
    ) {
        private var contextRef: WeakReference<Context>? = null

        init {
            lifecycleOwner.lifecycle.addObserver(object : LifecycleObserver {
                @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
                fun onDestroy() {
                    contextRef?.clear()
                    contextRef = null
                }
            })
        }

        val context: Context?
            get() = contextRef?.get() ?: contextProvider()?.also {
                contextRef = WeakReference(it.applicationContext)
            }

        fun <T> withContext(block: (Context) -> T): T? {
            return context?.let(block)
        }
    }

    /**
     * Create a lifecycle-aware context wrapper.
     */
    fun lifecycleContext(
        lifecycleOwner: LifecycleOwner,
        contextProvider: () -> Context?
    ): LifecycleContext {
        return LifecycleContext(lifecycleOwner, contextProvider)
    }
}

/**
 * Extension function to safely get application context.
 */
fun Context.safeApplicationContext(): Context {
    return applicationContext
}

/**
 * Extension function to check if context is from a destroyed activity.
 */
val Context.isFromDestroyedActivity: Boolean
    get() = this is android.app.Activity && (isFinishing || isDestroyed)

/**
 * Extension function to safely use a view with weak reference.
 */
inline fun <T : View> T?.withSafeView(crossinline block: T.() -> Unit) {
    this?.let { view ->
        if (!view.isAttachedToWindow || view.context != null) {
            view.block()
        }
    }
}

/**
 * Interface for classes that need to be context-aware but avoid leaks.
 */
interface ContextAware {
    val contextReference: WeakReference<Context>
    
    val context: Context?
        get() = contextReference.get()
    
    val safeContext: Context
        get() = contextReference.get()?.applicationContext
            ?: throw IllegalStateException("Context not available")
}

/**
 * Base class for objects that need context but want to avoid leaks.
 * Uses WeakReference to prevent holding strong references to activities.
 */
abstract class ContextAwareBase(initialContext: Context) : ContextAware {
    override val contextReference = WeakReference(initialContext.applicationContext)
}
