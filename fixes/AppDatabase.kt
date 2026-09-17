package com.example.tcmanager.data

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
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

@Entity(
    tableName = "inspection_act_attachments",
    primaryKeys = ["complexName", "dateKey"]
)
data class InspectionActAttachment(
    val complexName: String,
    val dateKey: String,
    val uri: String,
    val displayName: String
)

@Dao
interface InspectionActAttachmentDao {

    @Query(
        "SELECT * FROM inspection_act_attachments " +
            "WHERE complexName = :complexName AND dateKey = :dateKey LIMIT 1"
    )
    fun observe(complexName: String, dateKey: String): Flow<InspectionActAttachment?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(attachment: InspectionActAttachment)
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
        InspectionAct::class,
        InspectionActAttachment::class,
        ImportedTenant::class,
        TenantImportBackup::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun complexDao(): ComplexDao
    abstract fun inspectionDao(): InspectionDao
    abstract fun inspectionActAttachmentDao(): InspectionActAttachmentDao
    abstract fun tenantImportDao(): TenantImportDao

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
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .build()
                    .also {
                        INSTANCE = it
                    }
            }

        private val MIGRATION_1_2 = object : androidx.room.migration.Migration(1, 2) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS inspection_act_attachments (
                        complexName TEXT NOT NULL,
                        dateKey TEXT NOT NULL,
                        uri TEXT NOT NULL,
                        displayName TEXT NOT NULL,
                        PRIMARY KEY(complexName, dateKey)
                    )
                    """.trimIndent()
                )
            }

            private val MIGRATION_2_3 = object : androidx.room.migration.Migration(2, 3) {
                override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                    db.execSQL("""CREATE TABLE IF NOT EXISTS imported_tenants (
                        section TEXT NOT NULL, tenant TEXT NOT NULL, floorOrType TEXT NOT NULL,
                        leaseEnd TEXT NOT NULL, brand TEXT NOT NULL, activity TEXT NOT NULL,
                        phone TEXT NOT NULL, email TEXT NOT NULL, address TEXT NOT NULL,
                        PRIMARY KEY(section, tenant))""")
                    db.execSQL("""CREATE TABLE IF NOT EXISTS tenant_import_backups (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, backedUpAt INTEGER NOT NULL,
                        section TEXT NOT NULL, tenant TEXT NOT NULL, floorOrType TEXT NOT NULL,
                        leaseEnd TEXT NOT NULL, brand TEXT NOT NULL, activity TEXT NOT NULL,
                        phone TEXT NOT NULL, email TEXT NOT NULL, address TEXT NOT NULL)""")
                }
            }
        }
    }
}
