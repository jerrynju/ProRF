package com.prorf.dsl

data class DslSchemaRegistry(
    private val nodeTypes: Map<String, DslNodeSchema>,
) {
    fun findNode(type: String): DslNodeSchema? = nodeTypes[type]

    companion object {
        fun default(): DslSchemaRegistry = DslSchemaRegistry(
            mapOf(
                "Transmitter" to DslNodeSchema(
                    type = "Transmitter",
                    parameters = mapOf(
                        "power" to DslParameterSchema("power", DslDimension.POWER),
                        "frequency" to DslParameterSchema("frequency", DslDimension.FREQUENCY),
                    ),
                    outputs = mapOf("power" to DslPortSchema("power", DslDimension.POWER)),
                ),
                "SignalSource" to DslNodeSchema(
                    type = "SignalSource",
                    parameters = mapOf(
                        "power" to DslParameterSchema("power", DslDimension.POWER),
                        "frequency" to DslParameterSchema("frequency", DslDimension.FREQUENCY),
                    ),
                    outputs = mapOf("power" to DslPortSchema("power", DslDimension.POWER)),
                ),
                "FreeSpacePath" to DslNodeSchema(
                    type = "FreeSpacePath",
                    parameters = mapOf(
                        "distance" to DslParameterSchema("distance", DslDimension.DISTANCE),
                        "frequency" to DslParameterSchema("frequency", DslDimension.FREQUENCY),
                    ),
                    inputs = mapOf("power" to DslPortSchema("power", DslDimension.POWER)),
                    outputs = mapOf("power" to DslPortSchema("power", DslDimension.POWER)),
                ),
                "FreeSpacePathLoss" to DslNodeSchema(
                    type = "FreeSpacePathLoss",
                    parameters = mapOf(
                        "distance" to DslParameterSchema("distance", DslDimension.DISTANCE),
                        "frequency" to DslParameterSchema("frequency", DslDimension.FREQUENCY),
                    ),
                    inputs = mapOf("power" to DslPortSchema("power", DslDimension.POWER)),
                    outputs = mapOf("power" to DslPortSchema("power", DslDimension.POWER)),
                ),
                "Receiver" to DslNodeSchema(
                    type = "Receiver",
                    parameters = mapOf(
                        "noiseFigure" to DslParameterSchema("noiseFigure", DslDimension.GAIN),
                    ),
                    inputs = mapOf("power" to DslPortSchema("power", DslDimension.POWER)),
                    outputs = mapOf(
                        "snr" to DslPortSchema("snr", DslDimension.GAIN),
                        "link_margin" to DslPortSchema("link_margin", DslDimension.GAIN),
                        "ber" to DslPortSchema("ber", DslDimension.DIMENSIONLESS),
                    ),
                ),
                "PowerAmplifier" to DslNodeSchema(
                    type = "PowerAmplifier",
                    parameters = mapOf(
                        "gain" to DslParameterSchema("gain", DslDimension.GAIN),
                        "nf" to DslParameterSchema("nf", DslDimension.GAIN),
                        "p1db" to DslParameterSchema("p1db", DslDimension.POWER),
                    ),
                    inputs = mapOf("power" to DslPortSchema("power", DslDimension.POWER)),
                    outputs = mapOf("power" to DslPortSchema("power", DslDimension.POWER)),
                ),
            )
        )
    }
}

data class DslNodeSchema(
    val type: String,
    val inputs: Map<String, DslPortSchema> = emptyMap(),
    val outputs: Map<String, DslPortSchema> = emptyMap(),
    val parameters: Map<String, DslParameterSchema> = emptyMap(),
)

data class DslPortSchema(val name: String, val dimension: DslDimension)

data class DslParameterSchema(val name: String, val dimension: DslDimension)

enum class DslDimension {
    POWER,
    FREQUENCY,
    DISTANCE,
    TEMPERATURE,
    GAIN,
    DIMENSIONLESS,
    UNKNOWN,
}

object DslUnits {
    private val dimensions = mapOf(
        "W" to DslDimension.POWER,
        "dBm" to DslDimension.POWER,
        "Hz" to DslDimension.FREQUENCY,
        "kHz" to DslDimension.FREQUENCY,
        "MHz" to DslDimension.FREQUENCY,
        "GHz" to DslDimension.FREQUENCY,
        "m" to DslDimension.DISTANCE,
        "km" to DslDimension.DISTANCE,
        "K" to DslDimension.TEMPERATURE,
        "dB" to DslDimension.GAIN,
        "dBi" to DslDimension.GAIN,
        "count" to DslDimension.DIMENSIONLESS,
    )

    fun dimensionOf(unit: String): DslDimension? = dimensions[unit]
}
