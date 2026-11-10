package com.hostelmanagement.app.ui.owner.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hostelmanagement.app.core.common.Resource
import com.hostelmanagement.app.data.remote.dto.FinancialReportDto
import com.hostelmanagement.app.domain.repository.ReportRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class OwnerReportsUiState(
    val isLoading: Boolean = false,
    val selectedYear: Int = Calendar.getInstance().get(Calendar.YEAR),
    val report: FinancialReportDto? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class OwnerReportsViewModel @Inject constructor(
    private val reportRepository: ReportRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OwnerReportsUiState())
    val uiState: StateFlow<OwnerReportsUiState> = _uiState.asStateFlow()

    init {
        loadReport(_uiState.value.selectedYear)
    }

    fun setYear(year: Int) {
        _uiState.update { it.copy(selectedYear = year) }
        loadReport(year)
    }

    fun loadReport(year: Int = _uiState.value.selectedYear) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = reportRepository.getRevenueReport(year)) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            report = result.data
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.message
                        )
                    }
                }
                is Resource.Loading -> {}
            }
        }
    }
}
