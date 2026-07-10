package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        UserEntity::class,
        CategoryEntity::class,
        MedicineEntity::class,
        SaleEntity::class,
        SaleItemEntity::class,
        OrderEntity::class,
        ReservationEntity::class,
        DeliveryEntity::class,
        SupplierEntity::class,
        StockMovementEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun categoryDao(): CategoryDao
    abstract fun medicineDao(): MedicineDao
    abstract fun saleDao(): SaleDao
    abstract fun orderDao(): OrderDao
    abstract fun reservationDao(): ReservationDao
    abstract fun deliveryDao(): DeliveryDao
    abstract fun supplierDao(): SupplierDao
    abstract fun stockMovementDao(): StockMovementDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "pharmacy_la_reference_db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
