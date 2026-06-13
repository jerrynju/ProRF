package com.prorf.engineering.quantity

/** Physical dimension — enforces type-safe unit conversion in Quantity. */
enum class Dimension {
    POWER,
    GAIN,
    NOISE_FIGURE,
    FREQUENCY,
    DISTANCE,
    TEMPERATURE,
    BANDWIDTH,
    GAIN_PER_DISTANCE,
    DIMENSIONLESS,
}
