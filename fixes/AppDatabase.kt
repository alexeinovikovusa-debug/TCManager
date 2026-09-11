package com.example.tcmanager.data

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Dao
interface ComplexDao {

    @Query("SELECT * FROM complexes WHERE active=1 ORDER BY name")
    fun all(): Flow<List<Complex>>

    @Query("SELECT COUNT(*) FROM complexes WHERE active=1")
    suspend fun count(): Int

    @Insert
    suspend fun insert(c: Complex): Long
}

@Dao
interface InspectionDao {

    @Query("SELECT * FROM inspections WHERE complexId=:complexId ORDER BY plannedDate")
    fun forComplex(complexId: Long): Flow<List<Inspection>>

    @Insert
    suspend fun insert(i: Inspection): Long
}

@Database(
    entities = [
        Complex::class,
        Floor::class,
        Queue::class,
        Section::class,
        Tenant::class,
        Lease::class,
        WorkType::class,
        Work::class,
        FaultType::class,
        Fault::class,
        ProjectType::class,
        Project::class,
        ProjectFile::class,
        Inspection::class,
        InspectionAct::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun complexDao(): ComplexDao
    abstract fun inspectionDao(): InspectionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun get(context: android.content.Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "tc_manager.db"
                )
                    .build()
                    .also {
                        INSTANCE = it
                    }
            }
    }
}
