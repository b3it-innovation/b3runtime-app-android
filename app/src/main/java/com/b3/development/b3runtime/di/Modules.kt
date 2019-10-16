package com.b3.development.b3runtime.di

import androidx.room.Room
import com.b3.development.b3runtime.data.local.B3RuntimeDatabase
import com.b3.development.b3runtime.data.remote.BackendInteractor
import com.b3.development.b3runtime.data.remote.BackendInteractorImpl
import com.b3.development.b3runtime.data.repository.attendee.AttendeeRepository
import com.b3.development.b3runtime.data.repository.attendee.AttendeeRepositoryImpl
import com.b3.development.b3runtime.data.repository.competition.CompetitionRepository
import com.b3.development.b3runtime.data.repository.competition.CompetitionRepositoryImpl
import com.b3.development.b3runtime.data.repository.checkpoint.CheckpointRepository
import com.b3.development.b3runtime.data.repository.checkpoint.CheckpointRepositoryImpl
import com.b3.development.b3runtime.data.repository.question.QuestionRepository
import com.b3.development.b3runtime.data.repository.question.QuestionRepositoryImpl
import com.b3.development.b3runtime.data.repository.result.ResultRepository
import com.b3.development.b3runtime.data.repository.result.ResultRepositoryImpl
import com.b3.development.b3runtime.geofence.GeofenceManager
import com.b3.development.b3runtime.geofence.GeofenceManagerImpl
import com.google.firebase.database.FirebaseDatabase
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
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
    single { get<B3RuntimeDatabase>().checkpointDao() }
    single { get<B3RuntimeDatabase>().questionDao() }
    single { get<B3RuntimeDatabase>().attendeeDao() }
    single { BackendInteractorImpl(get(StringQualifier("questions")), get(StringQualifier("competitions")),
            get(StringQualifier("tracks_checkpoints")), get(StringQualifier("attendees")), get(StringQualifier("results"))) as BackendInteractor }
    single(StringQualifier("questions")) { FirebaseDatabase.getInstance().getReference("questions") }
    single(StringQualifier("competitions")) { FirebaseDatabase.getInstance().getReference("competitions") }
    single(StringQualifier("tracks_checkpoints")) { FirebaseDatabase.getInstance().getReference("tracks_checkpoints") }
    single(StringQualifier("attendees")) { FirebaseDatabase.getInstance().getReference("attendees") }
    single(StringQualifier("results")) { FirebaseDatabase.getInstance().getReference("results") }
    single { CheckpointRepositoryImpl(get(), get()) as CheckpointRepository }
    single { QuestionRepositoryImpl(get(), get()) as QuestionRepository }
    single { CompetitionRepositoryImpl(get()) as CompetitionRepository }
    single { AttendeeRepositoryImpl(get(), get()) as AttendeeRepository }
    single { ResultRepositoryImpl(get()) as ResultRepository }
    single { GeofenceManagerImpl(androidContext()) as GeofenceManager }
}