package com.example.stocklist

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import androidx.savedstate.read
import kotlin.random.Random


// Stock data class
data class Stock(
    val symbol: String,
    val companyName: String,
    val price: Double,
    val changePercent: Double,
    val marketCap: String,
    val peRatio: Double,
    val volume: String,
    val description: String
)

@Composable
fun StockApp(onNavHostReady: (NavController) -> Unit = {}) {
    val navController = rememberNavController()
    onNavHostReady(navController) // For Web browser history integration
    NavHost(navController = navController, startDestination = "StockListScreen") {
        composable("StockListScreen") {
            StockListScreen(navController)
        }
        composable("StockDetailScreen/{symbol}") { backStackEntry ->
            val symbol = backStackEntry.arguments?.read { "symbol" } ?: ""
            StockDetailScreen(symbol, navController)
        }
    }
}

@Composable
fun StockListScreen(navController: NavController) {
    val stocks = listOf(
        Stock("AAPL", "Apple Inc.", 192.53, 1.25, "3.12T", 29.8, "12.5M", "Leading tech company specializing in consumer electronics and software."),
        Stock("MSFT", "Microsoft Corporation", 447.67, -0.85, "3.33T", 38.2, "8.9M", "Global leader in software, cloud computing, and AI solutions."),
        Stock("GOOGL", "Alphabet Inc.", 183.45, 2.10, "2.27T", 27.4, "15.3M", "Parent company of Google, focusing on search, ads, and cloud services.")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.linearGradient(colors = listOf(Color(0xFF1E3A8A), Color(0xFF3B82F6))))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Stock Market",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(stocks) { stock ->
                StockCard(stock) {
                    // Navigate to StockDetailScreen with the stock's symbol
                    navController.navigate("StockDetailScreen/${stock.symbol}")
                }
            }
        }
    }
}

@Composable
fun StockCard(stock: Stock, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clip(RoundedCornerShape(12.dp))
            .shadow(4.dp, RoundedCornerShape(12.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = getRandomNiceColor())
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = stock.symbol, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text(text = stock.companyName, fontSize = 14.sp, color = Color.White.copy(alpha = 0.9f), maxLines = 1)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "$${stock.price}", fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Color.White)
                Text(
                    text = "${stock.changePercent}%",
                    fontSize = 16.sp,
                    color = if (stock.changePercent >= 0) Color(0xFF4CAF50) else Color(0xFFF44336),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun StockDetailScreen(symbol: String, navController: NavController) {
    val stocks = listOf(
        Stock("AAPL", "Apple Inc.", 192.53, 1.25, "3.12T", 29.8, "12.5M", "Leading tech company specializing in consumer electronics and software."),
        Stock("MSFT", "Microsoft Corporation", 447.67, -0.85, "3.33T", 38.2, "8.9M", "Global leader in software, cloud computing, and AI solutions."),
        Stock("GOOGL", "Alphabet Inc.", 183.45, 2.10, "2.27T", 27.4, "15.3M", "Parent company of Google, focusing on search, ads, and cloud services.")
    )
    val stock = stocks.find { it.symbol == symbol } ?: stocks[0]

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.linearGradient(colors = listOf(Color(0xFF1E3A8A), Color(0xFF3B82F6))))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stock.companyName,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Button(
                onClick = { navController.popBackStack() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF5350))
            ) {
                Text("Back")
            }
        }
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .clip(RoundedCornerShape(16.dp))
                .shadow(8.dp, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(text = "Symbol: ${stock.symbol}", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text(text = "Price: $${stock.price}", fontSize = 18.sp)
                Text(
                    text = "Change: ${stock.changePercent}%",
                    fontSize = 18.sp,
                    color = if (stock.changePercent >= 0) Color(0xFF4CAF50) else Color(0xFFF44336)
                )
                Text(text = "Market Cap: ${stock.marketCap}", fontSize = 18.sp)
                Text(text = "P/E Ratio: ${stock.peRatio}", fontSize = 18.sp)
                Text(text = "Volume: ${stock.volume}", fontSize = 18.sp)
                Text(text = "Description: ${stock.description}", fontSize = 16.sp, modifier = Modifier.padding(top = 8.dp))
            }
        }
    }
}

fun getRandomNiceColor(): Color {
    val niceColors = listOf(
        Color(0xFFEF5350), // Red
        Color(0xFFAB47BC), // Purple
        Color(0xFF42A5F5), // Blue
        Color(0xFF26A69A), // Teal
        Color(0xFF66BB6A)  // Green
    )
    return niceColors[Random.nextInt(niceColors.size)]
}
