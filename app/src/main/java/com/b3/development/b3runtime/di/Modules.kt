package com.b3.development.b3runtime.di

import androidx.room.Room
import com.b3.development.b3runtime.data.local.B3RuntimeDatabase
import com.b3.development.b3runtime.data.remote.BackendInteractor
import com.b3.development.b3runtime.data.remote.BackendInteractorImpl
import com.b3.development.b3runtime.data.repository.pin.PinRepository
import com.b3.development.b3runtime.data.repository.pin.PinRepositoryImpl
import com.b3.development.b3runtime.data.repository.question.QuestionRepository
import com.b3.development.b3runtime.data.repository.question.QuestionRepositoryImpl
import com.b3.development.b3runtime.geofence.GeofenceManager
import com.b3.development.b3runtime.geofence.GeofenceManagerImpl
import com.b3.development.b3runtime.ui.map.MapsViewModel
import com.b3.development.b3runtime.ui.question.QuestionViewModel
import com.google.firebase.database.FirebaseDatabase
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.qualifier.StringQualifier
import org.koin.dsl.module

/**
 * A module class that contains the dependency injection implementation.
 * Contains all the logic of injecting implementations of interfaces in dependant classes,
 * without holding a reference to the implementation in order to decouple and modularise the code
 *
 * Please note that this is a Kotlin class due to the simplicity of implementation
 * Note also that even though it is suggested by IDE that cast of interfaces can be safely removed,
 * this has proven to cause errors. Please use as suggested.
 */
val b3RuntimeModule = module {
//    viewModel { MapsViewModel(get(), get()) }
//    viewModel { QuestionViewModel(get()) }
    single { Room.databaseBuilder(androidApplication(), B3RuntimeDatabase::class.java, "b3Runtime_db").build() }
    single { get<B3RuntimeDatabase>().pinDao() }
    single { get<B3RuntimeDatabase>().questionDao() }
    single { BackendInteractorImpl(get(StringQualifier("pins")), get(StringQualifier("questions"))) as BackendInteractor }
    single(StringQualifier("pins")) { FirebaseDatabase.getInstance().getReference("control_points") }
    single(StringQualifier("questions")) { FirebaseDatabase.getInstance().getReference("questions") }
    single { PinRepositoryImpl(get(), get()) as PinRepository }
    single { QuestionRepositoryImpl(get(), get()) as QuestionRepository }
    single { GeofenceManagerImpl(androidContext()) as GeofenceManager }
}