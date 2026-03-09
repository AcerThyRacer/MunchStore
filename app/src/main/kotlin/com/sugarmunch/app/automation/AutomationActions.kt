package com.sugarmunch.app.automation

/**
 * Result of executing an automation action.
 * Represents the outcome of action execution with success, failure, or partial success states.
 */
sealed class ActionResult {
    abstract val message: String

    /**
     * Action completed successfully
     */
    data class Success(override val message: String) : ActionResult()

    /**
     * Action failed with an error message
     */
    data class Failure(override val message: String) : ActionResult()

    /**
     * Action partially completed with some sub-results
     */
    data class PartialSuccess(
        override val message: String,
        val subResults: List<ActionResult>
    ) : ActionResult()

    /**
     * Check if the action had any successful execution
     */
    val isSuccess: Boolean
        get() = this is Success || (this is PartialSuccess && subResults.any { it is Success })
}
