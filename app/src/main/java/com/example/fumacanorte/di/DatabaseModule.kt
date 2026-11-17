package com.example.fumacanorte.di

import androidx.room.Room
import com.example.fumacanorte.data.local.dao.WeatherDao
import com.example.fumacanorte.data.local.db.FumacaNorteDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val databaseModule = module {
    single<FumacaNorteDatabase> {
        Room.databaseBuilder(
            androidContext(),
            FumacaNorteDatabase::class.java,
            FumacaNorteDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()
    }

    single<WeatherDao> {
        get<FumacaNorteDatabase>().weatherDao()
    }
}
