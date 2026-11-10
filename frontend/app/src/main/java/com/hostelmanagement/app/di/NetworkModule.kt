package com.hostelmanagement.app.di

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.hostelmanagement.app.BuildConfig
import com.hostelmanagement.app.core.network.AuthInterceptor
import com.hostelmanagement.app.core.network.TokenAuthenticator
import com.hostelmanagement.app.data.remote.api.AuthApi
import com.hostelmanagement.app.data.remote.api.BillingApi
import com.hostelmanagement.app.data.remote.api.ComplaintApi
import com.hostelmanagement.app.data.remote.api.HealthApi
import com.hostelmanagement.app.data.remote.api.NotificationApi
import com.hostelmanagement.app.data.remote.api.PaymentApi
import com.hostelmanagement.app.data.remote.api.PaymentQrApi
import com.hostelmanagement.app.data.remote.api.ReportApi
import com.hostelmanagement.app.data.remote.api.RoomApi
import com.hostelmanagement.app.data.remote.api.StayApi
import com.hostelmanagement.app.data.remote.api.TenantApi
import com.hostelmanagement.app.data.remote.api.UserApi
import com.hostelmanagement.app.data.remote.api.VacateApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideGson(): Gson {
        return GsonBuilder()
            .setLenient()
            .create()
    }

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
    }

    @Provides
    @Singleton
    @Named("unauthenticated")
    fun provideUnauthenticatedOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideAuthenticatedOkHttpClient(
        authInterceptor: AuthInterceptor,
        tokenAuthenticator: TokenAuthenticator,
        loggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .authenticator(tokenAuthenticator)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        gson: Gson
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    @Provides
    @Singleton
    fun provideAuthApi(retrofit: Retrofit): AuthApi = retrofit.create(AuthApi::class.java)

    @Provides
    @Singleton
    fun provideUserApi(retrofit: Retrofit): UserApi = retrofit.create(UserApi::class.java)

    @Provides
    @Singleton
    fun provideTenantApi(retrofit: Retrofit): TenantApi = retrofit.create(TenantApi::class.java)

    @Provides
    @Singleton
    fun provideRoomApi(retrofit: Retrofit): RoomApi = retrofit.create(RoomApi::class.java)

    @Provides
    @Singleton
    fun provideStayApi(retrofit: Retrofit): StayApi = retrofit.create(StayApi::class.java)

    @Provides
    @Singleton
    fun provideBillingApi(retrofit: Retrofit): BillingApi = retrofit.create(BillingApi::class.java)

    @Provides
    @Singleton
    fun providePaymentApi(retrofit: Retrofit): PaymentApi = retrofit.create(PaymentApi::class.java)

    @Provides
    @Singleton
    fun providePaymentQrApi(retrofit: Retrofit): PaymentQrApi = retrofit.create(PaymentQrApi::class.java)

    @Provides
    @Singleton
    fun provideComplaintApi(retrofit: Retrofit): ComplaintApi = retrofit.create(ComplaintApi::class.java)

    @Provides
    @Singleton
    fun provideVacateApi(retrofit: Retrofit): VacateApi = retrofit.create(VacateApi::class.java)

    @Provides
    @Singleton
    fun provideNotificationApi(retrofit: Retrofit): NotificationApi = retrofit.create(NotificationApi::class.java)

    @Provides
    @Singleton
    fun provideReportApi(retrofit: Retrofit): ReportApi = retrofit.create(ReportApi::class.java)

    @Provides
    @Singleton
    fun provideHealthApi(retrofit: Retrofit): HealthApi = retrofit.create(HealthApi::class.java)
}
