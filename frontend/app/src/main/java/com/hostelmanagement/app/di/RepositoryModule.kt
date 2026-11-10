package com.hostelmanagement.app.di

import com.hostelmanagement.app.data.repository.AuthRepositoryImpl
import com.hostelmanagement.app.data.repository.BillingRepositoryImpl
import com.hostelmanagement.app.data.repository.ComplaintRepositoryImpl
import com.hostelmanagement.app.data.repository.NotificationRepositoryImpl
import com.hostelmanagement.app.data.repository.PaymentQrRepositoryImpl
import com.hostelmanagement.app.data.repository.PaymentRepositoryImpl
import com.hostelmanagement.app.data.repository.ReportRepositoryImpl
import com.hostelmanagement.app.data.repository.RoomRepositoryImpl
import com.hostelmanagement.app.data.repository.StayRepositoryImpl
import com.hostelmanagement.app.data.repository.TenantRepositoryImpl
import com.hostelmanagement.app.data.repository.UserRepositoryImpl
import com.hostelmanagement.app.domain.repository.AuthRepository
import com.hostelmanagement.app.domain.repository.BillingRepository
import com.hostelmanagement.app.domain.repository.ComplaintRepository
import com.hostelmanagement.app.domain.repository.NotificationRepository
import com.hostelmanagement.app.domain.repository.PaymentQrRepository
import com.hostelmanagement.app.domain.repository.PaymentRepository
import com.hostelmanagement.app.domain.repository.ReportRepository
import com.hostelmanagement.app.domain.repository.RoomRepository
import com.hostelmanagement.app.domain.repository.StayRepository
import com.hostelmanagement.app.domain.repository.TenantRepository
import com.hostelmanagement.app.domain.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindRoomRepository(impl: RoomRepositoryImpl): RoomRepository

    @Binds
    @Singleton
    abstract fun bindTenantRepository(impl: TenantRepositoryImpl): TenantRepository

    @Binds
    @Singleton
    abstract fun bindStayRepository(impl: StayRepositoryImpl): StayRepository

    @Binds
    @Singleton
    abstract fun bindBillingRepository(impl: BillingRepositoryImpl): BillingRepository

    @Binds
    @Singleton
    abstract fun bindPaymentRepository(impl: PaymentRepositoryImpl): PaymentRepository

    @Binds
    @Singleton
    abstract fun bindPaymentQrRepository(impl: PaymentQrRepositoryImpl): PaymentQrRepository

    @Binds
    @Singleton
    abstract fun bindComplaintRepository(impl: ComplaintRepositoryImpl): ComplaintRepository

    @Binds
    @Singleton
    abstract fun bindVacateRepository(impl: com.hostelmanagement.app.data.repository.VacateRepositoryImpl): com.hostelmanagement.app.domain.repository.VacateRepository

    @Binds
    @Singleton
    abstract fun bindNotificationRepository(impl: NotificationRepositoryImpl): NotificationRepository

    @Binds
    @Singleton
    abstract fun bindReportRepository(impl: ReportRepositoryImpl): ReportRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(impl: UserRepositoryImpl): UserRepository
}
