package com.tinyappco.testdataposting

import java.io.Serializable
import java.util.*

class AirQualityReading(var id: Int?, var pm10: Int, var pm25: Int, var latitude: Double,
                        var longitude: Double, var date: Date) :Serializable, Comparable<AirQualityReading>  {

    override fun compareTo(other: AirQualityReading): Int {
        return date.compareTo(other.date)
    }
}