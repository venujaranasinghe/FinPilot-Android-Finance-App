package com.bpeople.finpilot.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import okhttp3.OkHttpClient
import android.content.Context
import androidx.room.Room
import dagger.hilt.android.qualifiers.ApplicationContext
import com.bpeople.finpilot.data.local.FinPilotDatabase
import com.bpeople.finpilot.data.local.FinPilotDatabaseMigrations
import com.bpeople.finpilot.data.local.dao.IncomeDao
import com.bpeople.finpilot.data.local.dao.ExpenseDao
import com.bpeople.finpilot.data.local.dao.GoalDao
import com.bpeople.finpilot.data.local.dao.FreelanceProjectDao

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder().build()

    @Provides
    @Singleton
    fun provideGson(): Gson = Gson()

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext appContext: Context): FinPilotDatabase {
        return Room.databaseBuilder(appContext, FinPilotDatabase::class.java, "finpilot.db")
            .addMigrations(*FinPilotDatabaseMigrations.ALL)
            // Downgrades are typically only seen during development; keep the app usable.
            .fallbackToDestructiveMigrationOnDowngrade()
            .build()
    }

    @Provides
    fun provideIncomeDao(db: FinPilotDatabase): IncomeDao = db.incomeDao()

    @Provides
    fun provideExpenseDao(db: FinPilotDatabase): ExpenseDao = db.expenseDao()

    @Provides
    fun provideGoalDao(db: FinPilotDatabase): GoalDao = db.goalDao()

    @Provides
    fun provideProjectDao(db: FinPilotDatabase): FreelanceProjectDao = db.freelanceProjectDao()
}
