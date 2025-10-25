package org.ton.sdk.bitstring.unsafe

/**
 * Marks declarations whose usage may brake some BitString invariants.
 *
 * Consider using other APIs instead when possible.
 * Otherwise, make sure to read documentation describing an unsafe API.
 */
@MustBeDocumented
@Retention(AnnotationRetention.BINARY)
@RequiresOptIn(
    level = RequiresOptIn.Level.WARNING,
    message = "This is an unsafe API and its use requires care. " +
            "Make sure you fully understand documentation of the declaration marked as UnsafeIoApi"
)
public annotation class UnsafeBitStringApi
