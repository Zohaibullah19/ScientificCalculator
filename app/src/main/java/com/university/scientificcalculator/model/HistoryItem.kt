package com.university.scientificcalculator.model

/**
 * A single completed calculation kept in the in-memory history list.
 *
 * @param expression the expression exactly as the user entered it
 * @param result the formatted result that was displayed to the user
 */
data class HistoryItem(
    val expression: String,
    val result: String
)
