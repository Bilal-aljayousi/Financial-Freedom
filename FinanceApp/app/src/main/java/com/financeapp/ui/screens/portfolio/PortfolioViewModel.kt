package com.financeapp.ui.screens.portfolio

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.financeapp.data.FinanceDatabase
import com.financeapp.data.FinanceRepository
import com.financeapp.data.PortfolioHolding
import com.financeapp.data.StockQuote
import com.financeapp.data.FinanceApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PortfolioViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: FinanceRepository

    val holdings: StateFlow<List<PortfolioHolding>>
    val totalInvested: StateFlow<Double?>
    val currentValue: StateFlow<Double?>

    private val _searchResults = MutableStateFlow<List<StockQuote>>(emptyList())
    val searchResults: StateFlow<List<StockQuote>> = _searchResults.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _showAddDialog = MutableStateFlow(false)
    val showAddDialog: StateFlow<Boolean> = _showAddDialog.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        val db = FinanceDatabase.getDatabase(application)
        repository = FinanceRepository(db)

        holdings = repository.getAllHoldings()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        totalInvested = repository.getTotalInvested()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

        currentValue = repository.getCurrentValue()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    }

    fun addHolding(symbol: String, name: String, quantity: Double, purchasePrice: Double) {
        viewModelScope.launch {
            val holding = PortfolioHolding(
                symbol = symbol.uppercase(),
                name = name,
                quantity = quantity,
                purchasePrice = purchasePrice
            )
            repository.insertHolding(holding)
            _showAddDialog.value = false
        }
    }

    fun updateHoldingPrice(holding: PortfolioHolding, newPrice: Double) {
        viewModelScope.launch {
            repository.updateHolding(holding.copy(currentPrice = newPrice))
        }
    }

    fun deleteHolding(holding: PortfolioHolding) {
        viewModelScope.launch {
            repository.deleteHolding(holding)
        }
    }

    fun fetchStockPrice(symbol: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val response = FinanceApi.retrofit.getStockData(symbol)
                val quote = response.chart?.result?.firstOrNull()?.meta
                if (quote != null) {
                    val stockQuote = StockQuote(
                        symbol = quote.symbol,
                        name = quote.symbol,
                        price = quote.regularMarketPrice,
                        change = quote.regularMarketPrice - quote.previousClose,
                        changePercent = ((quote.regularMarketPrice - quote.previousClose) / quote.previousClose) * 100,
                        previousClose = quote.previousClose
                    )
                    _searchResults.value = listOf(stockQuote)
                } else {
                    _errorMessage.value = "No data found for $symbol"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to fetch stock data: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateCurrentPrices() {
        viewModelScope.launch {
            holdings.value.forEach { holding ->
                try {
                    val response = FinanceApi.retrofit.getStockData(holding.symbol)
                    val quote = response.chart?.result?.firstOrNull()?.meta
                    if (quote != null) {
                        updateHoldingPrice(holding, quote.regularMarketPrice)
                    }
                } catch (_: Exception) {}
            }
        }
    }

    fun toggleAddDialog() {
        _showAddDialog.value = !_showAddDialog.value
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
