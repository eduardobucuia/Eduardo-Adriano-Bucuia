package com.example.data

import kotlinx.coroutines.flow.Flow

class PharmacyRepository(private val db: AppDatabase) {

    private val userDao = db.userDao()
    private val categoryDao = db.categoryDao()
    private val medicineDao = db.medicineDao()
    private val saleDao = db.saleDao()
    private val orderDao = db.orderDao()
    private val reservationDao = db.reservationDao()
    private val deliveryDao = db.deliveryDao()
    private val supplierDao = db.supplierDao()
    private val stockMovementDao = db.stockMovementDao()

    // Users
    val allUsers: Flow<List<UserEntity>> = userDao.getAllUsers()
    suspend fun getUserByUsername(username: String): UserEntity? = userDao.getUserByUsername(username)
    suspend fun insertUser(user: UserEntity): Long = userDao.insertUser(user)
    suspend fun updateUser(user: UserEntity) = userDao.updateUser(user)
    suspend fun deleteUser(user: UserEntity) = userDao.deleteUser(user)

    // Categories
    val allCategories: Flow<List<CategoryEntity>> = categoryDao.getAllCategories()
    suspend fun insertCategory(category: CategoryEntity): Long = categoryDao.insertCategory(category)
    suspend fun deleteCategory(category: CategoryEntity) = categoryDao.deleteCategory(category)

    // Medicines
    val allMedicines: Flow<List<MedicineEntity>> = medicineDao.getAllMedicines()
    suspend fun getMedicineById(id: Int): MedicineEntity? = medicineDao.getMedicineById(id)
    fun getMedicinesByCategory(categoryId: Int): Flow<List<MedicineEntity>> = medicineDao.getMedicinesByCategory(categoryId)
    suspend fun insertMedicine(medicine: MedicineEntity): Long = medicineDao.insertMedicine(medicine)
    suspend fun updateMedicine(medicine: MedicineEntity) = medicineDao.updateMedicine(medicine)
    suspend fun deleteMedicine(medicine: MedicineEntity) = medicineDao.deleteMedicine(medicine)

    // Sales
    val allSales: Flow<List<SaleEntity>> = saleDao.getAllSales()
    suspend fun getItemsForSale(saleId: Int): List<SaleItemEntity> = saleDao.getItemsForSale(saleId)
    
    // Transactional Sale
    suspend fun executeSale(sale: SaleEntity, items: List<SaleItemEntity>): Long {
        val saleId = saleDao.insertSale(sale).toInt()
        val itemsWithSaleId = items.map { it.copy(saleId = saleId) }
        saleDao.insertSaleItems(itemsWithSaleId)
        
        // Update stock & add movement
        for (item in itemsWithSaleId) {
            val medicine = medicineDao.getMedicineById(item.medicineId)
            if (medicine != null) {
                val newStock = (medicine.stock - item.quantity).coerceAtLeast(0)
                medicineDao.updateMedicine(medicine.copy(stock = newStock))
                
                stockMovementDao.insertMovement(
                    StockMovementEntity(
                        medicineId = item.medicineId,
                        medicineName = item.medicineName,
                        type = "Saída",
                        quantity = item.quantity,
                        reason = "Venda POS #${saleId}"
                    )
                )
            }
        }
        return saleId.toLong()
    }

    // Orders
    val allOrders: Flow<List<OrderEntity>> = orderDao.getAllOrders()
    suspend fun insertOrder(order: OrderEntity): Long = orderDao.insertOrder(order)
    suspend fun updateOrder(order: OrderEntity) = orderDao.updateOrder(order)

    // Reservations
    val allReservations: Flow<List<ReservationEntity>> = reservationDao.getAllReservations()
    suspend fun insertReservation(reservation: ReservationEntity): Long = reservationDao.insertReservation(reservation)
    suspend fun updateReservation(reservation: ReservationEntity) = reservationDao.updateReservation(reservation)

    // Deliveries
    val allDeliveries: Flow<List<DeliveryEntity>> = deliveryDao.getAllDeliveries()
    fun getDeliveriesForPerson(userId: Int): Flow<List<DeliveryEntity>> = deliveryDao.getDeliveriesForPerson(userId)
    suspend fun insertDelivery(delivery: DeliveryEntity): Long = deliveryDao.insertDelivery(delivery)
    suspend fun updateDelivery(delivery: DeliveryEntity) = deliveryDao.updateDelivery(delivery)

    // Suppliers
    val allSuppliers: Flow<List<SupplierEntity>> = supplierDao.getAllSuppliers()
    suspend fun insertSupplier(supplier: SupplierEntity): Long = supplierDao.insertSupplier(supplier)
    suspend fun deleteSupplier(supplier: SupplierEntity) = supplierDao.deleteSupplier(supplier)

    // Stock Movements
    val allMovements: Flow<List<StockMovementEntity>> = stockMovementDao.getAllMovements()
    suspend fun insertMovement(movement: StockMovementEntity): Long {
        val medicine = medicineDao.getMedicineById(movement.medicineId)
        if (medicine != null) {
            val newStock = if (movement.type == "Entrada") {
                medicine.stock + movement.quantity
            } else {
                (medicine.stock - movement.quantity).coerceAtLeast(0)
            }
            medicineDao.updateMedicine(medicine.copy(stock = newStock))
        }
        return stockMovementDao.insertMovement(movement)
    }
}
