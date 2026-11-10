package com.hostelmanagement.app.core.common

data class UiState<T>(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val data: T? = null,
    val errorMessage: String? = null,
    val userMessage: String? = null
) {
    val isSuccess: Boolean get() = data != null && errorMessage == null
    val isEmpty: Boolean get() = !isLoading && data == null && errorMessage == null
    val isError: Boolean get() = errorMessage != null
}
