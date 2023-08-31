package com.graytsar.timezoneconverter

/**
 * Data class for time zone to be displayed in the UI.
 */
data class UITimeZone(
    /**
     * Time zone id. e.g. "Europe/Berlin"
     */
    val id: String,
    /**
     * Time zone long name. e.g. "Central European Standard Time"
     */
    val longName: String,
    /**
     * Zone offset from UTC. e.g. "+01:00"
     */
    val offset: String,
    /**
     * Time zone short name. e.g. "CEST"
     */
    val shortName: String
)