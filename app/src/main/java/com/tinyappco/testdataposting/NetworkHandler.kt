package com.tinyappco.testdataposting

import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.Charset
import java.text.SimpleDateFormat
import java.util.*

class NetworkHandler(val deviceGUID: String) {

    private val urlString = "https://oneair.1809152.win.studentwebserver.co.uk/api/airqualities"

    interface NetworkErrorHandler {
        fun networkError(message: String)
    }

    private val errorHandler: NetworkErrorHandler? = null

    fun post(airQualityReading: AirQualityReading) {

        val thread = Thread {

            val url = URL(urlString)
            val con = url.openConnection() as HttpURLConnection

            con.requestMethod = "POST"
            con.setRequestProperty("Content-Type", "application/json; utf-8")
            con.setRequestProperty("Accept", "application/json")
            con.doOutput = true

            val jsonInputString = jsonEncode(airQualityReading)
            val input: ByteArray = jsonInputString.toByteArray(Charset.forName("UTF-8"))
            con.outputStream.write(input, 0, input.size)

            if (con.responseCode < 200 || con.responseCode > 299) {
                errorHandler?.networkError("Error, response code $con.responseCode")
            }
        }
        thread.start()
    }

    private fun jsonEncode(airQualityReading: AirQualityReading): String {
        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        format.timeZone = TimeZone.getTimeZone("UTC")
        val formattedDate = format.format(airQualityReading.date)
        return """{"Time":"$formattedDate","Latitude":${airQualityReading.latitude},"Longitude":${airQualityReading.longitude},"PM10":${airQualityReading.pm10},"PM25":${airQualityReading.pm25},"DeviceID":"$deviceGUID"}"""
    }

}
