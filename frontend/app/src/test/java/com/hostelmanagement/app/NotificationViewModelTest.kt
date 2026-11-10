package com.hostelmanagement.app

import com.hostelmanagement.app.core.common.Resource
import com.hostelmanagement.app.data.remote.dto.NotificationResponseDto
import com.hostelmanagement.app.domain.repository.NotificationRepository
import com.hostelmanagement.app.ui.owner.notifications.NotificationFilter
import com.hostelmanagement.app.ui.owner.notifications.OwnerNotificationsViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NotificationViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val notificationRepository: NotificationRepository = mockk(relaxed = true)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun loadNotifications_defaultAllFilter_loadsAll() = runTest(testDispatcher) {
        val mockNotifs = listOf(
            NotificationResponseDto(
                id = 1L,
                user_id = 10L,
                title = "Payment Approved",
                message = "Payment of 5000 verified",
                notification_type = "PAYMENT_APPROVED",
                category = "PAYMENT",
                is_read = false,
                created_at = "2026-11-10T10:00:00"
            )
        )
        coEvery { notificationRepository.listNotifications(null) } returns Resource.Success(mockNotifs)

        val viewModel = OwnerNotificationsViewModel(notificationRepository)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(1, viewModel.uiState.value.notifications.size)
        assertEquals(NotificationFilter.ALL, viewModel.uiState.value.selectedFilter)
    }

    @Test
    fun selectFilter_payments_queriesPaymentCategory() = runTest(testDispatcher) {
        val mockNotifs = listOf(
            NotificationResponseDto(
                id = 2L,
                user_id = 10L,
                title = "Rent Payment Received",
                message = "Payment of 4000 received",
                notification_type = "PAYMENT_SUBMITTED",
                category = "PAYMENT",
                is_read = false,
                created_at = "2026-11-10T11:00:00"
            )
        )
        coEvery { notificationRepository.listNotifications("PAYMENT") } returns Resource.Success(mockNotifs)

        val viewModel = OwnerNotificationsViewModel(notificationRepository)
        advanceUntilIdle()

        viewModel.selectFilter(NotificationFilter.PAYMENTS)
        advanceUntilIdle()

        assertEquals(NotificationFilter.PAYMENTS, viewModel.uiState.value.selectedFilter)
        coVerify(exactly = 1) { notificationRepository.listNotifications("PAYMENT") }
    }
}
