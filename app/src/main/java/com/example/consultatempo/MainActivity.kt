package com.example.consultatempo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.consultatempo.ui.theme.ConsultaTempoTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ConsultaTempoTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    var cidadeTexto by remember { mutableStateOf("") }
                    var previsao by remember { mutableStateOf<Previsao?>(null) }
                    var carregando by remember { mutableStateOf(false) }
                    var mensagem by remember { mutableStateOf("Digite o nome de uma cidade para consultar a previsão.") }

                    val escopo = rememberCoroutineScope()
                    val repository = remember { PrevisaoRepository() }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Previsão do Tempo",
                            style = MaterialTheme.typography.headlineMedium
                        )
                        Text(
                            text = "Consulte a temperatura atual de qualquer cidade."
                        )

                        OutlinedTextField(
                            value = cidadeTexto,
                            onValueChange = { cidadeTexto = it },
                            label = { Text("Nome da cidade") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Button(
                            onClick = {
                                val cidadeBusca = cidadeTexto.trim()

                                if (cidadeBusca.isEmpty()) {
                                    mensagem = "Por favor, informe o nome de uma cidade."
                                    previsao = null
                                    return@Button
                                }

                                escopo.launch {
                                    carregando = true
                                    mensagem = "Consultando a API..."
                                    previsao = null

                                    try {
                                        val resultado = repository.buscarPrevisao(cidadeBusca)
                                        previsao = resultado
                                        mensagem = "Previsão consultada com sucesso."
                                    } catch (e: Exception) {
                                        mensagem = "Não foi possível consultar a previsão: " +
                                                (e.message ?: "erro inesperado")
                                    } finally {
                                        carregando = false
                                    }
                                }
                            },
                            enabled = !carregando,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Consultar")
                        }

                        if (carregando) {
                            CircularProgressIndicator()
                        }

                        Text(text = mensagem)

                        previsao?.let {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Cidade: ${it.cidadeNome}",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "Temperatura: ${it.temperaturaAtual} °C",
                                style = MaterialTheme.typography.headlineSmall
                            )
                        }
                    }
                }
            }
        }
    }
}
