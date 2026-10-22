@file:JvmName("MessageDecorations")

package com.example.notifyhub.text

import com.example.notifyhub.model.Notification

/**
 * Builds the footer line that [com.example.notifyhub.service.transformers.FooterTransformer]
 * appends to an outgoing message. Blank footers fall back to the product name.
 */
fun footerLine(footer: String, notification: Notification): String {
    val signature = footer.trim().ifEmpty { "NotifyHub" }
    val localeTag = notification.locale().takeIf { it != "en" }?.let { " [$it]" } ?: ""
    return "Appended footer: $signature$localeTag"
}

/** Short, log-friendly label: channel and a masked recipient. */
val Notification.routingLabel: String
    get() = "${channel() ?: "default"} -> ${maskEmail(recipient())}"

/**
 * Masks the local part of an e-mail address so it can be logged safely:
 * "jane@example.com" becomes "j***@example.com".
 * Values without a usable local part are returned unchanged.
 */
fun maskEmail(address: String): String {
    val at = address.indexOf('@')
    return if (at <= 1) address else "${address.first()}***${address.substring(at)}"
}
