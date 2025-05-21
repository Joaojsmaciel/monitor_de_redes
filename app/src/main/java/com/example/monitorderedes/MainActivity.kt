package com.example.monitorderedes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.monitorderedes.ui.theme.MonitorDeRedesTheme
import kotlin.random.Random

data class MetricaRede(
    val nome: String,
    val valor: Double,
    val unidade: String,
    val minAceitavel: Double,
    val maxAceitavel: Double
) {
    fun obterStatus(): String {
        return when {
            valor < minAceitavel -> "Abaixo do aceitável"
            valor > maxAceitavel -> "Acima do aceitável"
            else -> "Dentro do aceitável"
        }
    }
}

class SimuladorRede {
    private val aleatorio = Random(System.currentTimeMillis())
    
    fun gerarIntensidadeSinal(): Double = aleatorio.nextDouble(-70.0, -40.0)
    fun gerarLatencia(): Double = aleatorio.nextDouble(20.0, 80.0)
    fun gerarThroughput(): Double = aleatorio.nextDouble(100.0, 120.0)
    fun gerarFrequencia(): Double = aleatorio.nextDouble(2.4, 6.0)
    fun gerarRSRP(): Double = aleatorio.nextDouble(-115.0, -85.0)
    fun gerarRSRQ(): Double = aleatorio.nextDouble(-20.0, -10.0)
}

@Composable
fun CardMetrica(metrica: MetricaRede, aoClicar: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable(onClick = aoClicar),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = metrica.nome,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${metrica.valor} ${metrica.unidade}",

            )
            Text(
                text = metrica.obterStatus(),
                fontSize = 14.sp,
                color = when (metrica.obterStatus()) {
                    "Dentro do aceitável" -> MaterialTheme.colorScheme.primary
                    "Acima do aceitável" -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.error
                }
            )
        }
    }
}

@Composable
fun TelaDetalhesMetrica(nomeMetrica: String, aoVoltar: () -> Unit) {
    val simulador = remember { SimuladorRede() }
    val valoresRecentes = remember {
        List(5) {
            when (nomeMetrica) {
                "Intensidade do sinal" -> simulador.gerarIntensidadeSinal()
                "Latência" -> simulador.gerarLatencia()
                "Throughput" -> simulador.gerarThroughput()
                "Frequência" -> simulador.gerarFrequencia()
                "RSRP" -> simulador.gerarRSRP()
                "RSRQ" -> simulador.gerarRSRQ()
                else -> 0.0
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        IconButton(onClick = aoVoltar) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volta")
        }
        
        Text(
            text = nomeMetrica,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 16.dp)
        )
        
        LazyColumn {
            items(valoresRecentes) { valor ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = valor.toString(),
                            fontSize = 18.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NavegacaoApp() {
    val controladorNavegacao = rememberNavController()
    
    NavHost(navController = controladorNavegacao, startDestination = "principal") {
        composable("principal") {
            TelaPrincipal(
                aoClicarMetrica = { nomeMetrica ->
                    controladorNavegacao.navigate("detalhes/$nomeMetrica")
                }
            )
        }
        composable("detalhes/{nomeMetrica}") { entrada ->
            val nomeMetrica = entrada.arguments?.getString("nomeMetrica") ?: ""
            TelaDetalhesMetrica(
                nomeMetrica = nomeMetrica,
                aoVoltar = {
                    controladorNavegacao.popBackStack()
                }
            )
        }
    }
}

@Composable
fun TelaPrincipal(aoClicarMetrica: (String) -> Unit) {
    val simulador = remember { SimuladorRede() }
    val metricas = remember {
        mutableStateListOf(
            MetricaRede("Intensidade do sinal", simulador.gerarIntensidadeSinal(), "dBm", -100.0, -50.0),
            MetricaRede("Latência", simulador.gerarLatencia(), "ms", 1.0, 50.0),
            MetricaRede("Throughput", simulador.gerarThroughput(), "Mbps", 100.0, 1000.0),
            MetricaRede("Frequência", simulador.gerarFrequencia(), "GHz", 2.4, 6.0),
            MetricaRede("RSRP", simulador.gerarRSRP(), "dBm", -140.0, -44.0),
            MetricaRede("RSRQ", simulador.gerarRSRQ(), "dB", -20.0, -3.0)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Monitor de Rede 5G",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        LazyColumn {
            items(metricas) { metrica ->
                CardMetrica(
                    metrica = metrica,
                    aoClicar = { aoClicarMetrica(metrica.nome) }
                )
            }
        }
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MonitorDeRedesTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavegacaoApp()
                }
            }
        }
    }
}