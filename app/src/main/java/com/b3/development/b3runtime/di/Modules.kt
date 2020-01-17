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
import com.b3.development.b3runtime.data.repository.track.TrackRepository
import com.b3.development.b3runtime.data.repository.track.TrackRepositoryImpl
import com.b3.development.b3runtime.data.repository.useraccount.UserAccountRepository
import com.b3.development.b3runtime.data.repository.useraccount.UserAccountRepositoryImpl
import com.b3.development.b3runtime.geofence.GeofenceManager
import com.b3.development.b3runtime.geofence.GeofenceManagerImpl
import com.google.firebase.firestore.FirebaseFirestore
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
    // ViewModel injection in Java is not supported yet in Koin-Java 2.0.1 (only supported for Kotlin) at 2019-11-27
//    viewModel { MapsViewModel(get(), get()) }
//    viewModel { QuestionViewModel(get()) }
    single { Room.databaseBuilder(androidApplication(), B3RuntimeDatabase::class.java, "b3Runtime_db").build() }
    single { get<B3RuntimeDatabase>().checkpointDao() }
    single { get<B3RuntimeDatabase>().questionDao() }
    single { get<B3RuntimeDatabase>().attendeeDao() }
    single { get<B3RuntimeDatabase>().userAccountDao() }
    single {
        BackendInteractorImpl(get(StringQualifier("questions")), get(StringQualifier("competitions")),
                get(StringQualifier("tracks")), get(StringQualifier("attendees")),
                get(StringQualifier("results")), get(StringQualifier("user_accounts"))) as BackendInteractor
    }
    single(StringQualifier("questions")) { FirebaseFirestore.getInstance().collection("questions") }
    single(StringQualifier("competitions")) { FirebaseFirestore.getInstance().collection("competitions") }
    single(StringQualifier("tracks")) { FirebaseFirestore.getInstance().collection("tracks") }
    single(StringQualifier("attendees")) { FirebaseFirestore.getInstance().collection("attendees") }
    single(StringQualifier("results")) { FirebaseFirestore.getInstance().collection("results") }
    single(StringQualifier("user_accounts")) { FirebaseFirestore.getInstance().collection("user_accounts") }
    single { CheckpointRepositoryImpl(get(), get()) as CheckpointRepository }
    single { QuestionRepositoryImpl(get(), get()) as QuestionRepository }
    single { CompetitionRepositoryImpl(get()) as CompetitionRepository }
    single { AttendeeRepositoryImpl(get(), get()) as AttendeeRepository }
    single { ResultRepositoryImpl(get()) as ResultRepository }
    single { UserAccountRepositoryImpl(get(), get()) as UserAccountRepository }
    single { TrackRepositoryImpl(get()) as TrackRepository }
    single { GeofenceManagerImpl(androidContext()) as GeofenceManager }
}