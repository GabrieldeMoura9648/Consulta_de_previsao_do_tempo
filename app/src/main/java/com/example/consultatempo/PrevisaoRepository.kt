package com.example.consultatempo

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class PrevisaoRepository {

    suspend fun buscarPrevisao(cidade: String): Previsao = withContext(Dispatchers.IO) {
        val cidadeEncoded = URLEncoder.encode(cidade, "UTF-8")
        val geoUrl = URL("https://geocoding-api.open-meteo.com/v1/search?name=$cidadeEncoded&count=1")
        val geoConexao = geoUrl.openConnection() as HttpURLConnection
        
        val lat: Double
        val lon: Double
        val nomeEncontrado: String

        try {
            geoConexao.requestMethod = "GET"
            geoConexao.connectTimeout = 10000
            geoConexao.readTimeout = 10000

            val codigoRespostaGeo = geoConexao.responseCode
            val fluxoGeo = if (codigoRespostaGeo in 200..299) {
                geoConexao.inputStream
            } else {
                geoConexao.errorStream
            }

            val respostaGeo = fluxoGeo?.bufferedReader()?.use { it.readText() }
                ?: throw IOException("A API não retornou conteúdo.")

            if (codigoRespostaGeo !in 200..299) {
                throw IOException("A API de geolocalização retornou HTTP $codigoRespostaGeo.")
            }

            val jsonGeo = JSONObject(respostaGeo)
            
            if (!jsonGeo.has("results")) {
                throw IOException("Cidade não encontrada.")
            }
            
            val result = jsonGeo.getJSONArray("results").getJSONObject(0)
            lat = result.getDouble("latitude")
            lon = result.getDouble("longitude")
            nomeEncontrado = result.getString("name")
        } finally {
            geoConexao.disconnect()
        }

        val weatherUrl = URL("https://api.open-meteo.com/v1/forecast?latitude=$lat&longitude=$lon&current_weather=true")
        val weatherConexao = weatherUrl.openConnection() as HttpURLConnection

        try {
            weatherConexao.requestMethod = "GET"
            weatherConexao.connectTimeout = 10000
            weatherConexao.readTimeout = 10000

            val codigoRespostaW = weatherConexao.responseCode
            val fluxoW = if (codigoRespostaW in 200..299) {
                weatherConexao.inputStream
            } else {
                weatherConexao.errorStream
            }

            val respostaW = fluxoW?.bufferedReader()?.use { it.readText() }
                ?: throw IOException("A API não retornou conteúdo.")

            if (codigoRespostaW !in 200..299) {
                throw IOException("A API de clima retornou HTTP $codigoRespostaW.")
            }

            val jsonW = JSONObject(respostaW)
            
            val currentWeather = jsonW.getJSONObject("current_weather")
            val temperatura = currentWeather.getDouble("temperature")

            Previsao(
                cidadeNome = nomeEncontrado,
                temperaturaAtual = temperatura
            )
        } finally {
            weatherConexao.disconnect()
        }
    }
}
