package com.monmoy.cmfessentialremapper.domain.model

/**
 * Represents the configuration and hardware state of the CMF Essential Key.
 */
sealed interface EssentialKeyStatus {
    val title: String
    val description: String

    data object NotConfigured : EssentialKeyStatus {
        override val title: String = "Not Configured"
        override val description: String = "Essential Key has not been identified or saved yet."
    }

    data class Ready(
        val keyCode: Int = 0,
        val scanCode: Int = 250
    ) : EssentialKeyStatus {
        override val title: String = "Ready"
        override val description: String = "Key mapped (Code: $keyCode, Scan: $scanCode)."
    }

    data object ServiceDisabled : EssentialKeyStatus {
        override val title: String = "Service Disabled"
        override val description: String = "Accessibility Service is not running."
    }
}
