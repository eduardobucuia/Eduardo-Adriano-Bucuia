package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val username: String,
    val password: String,
    val role: String // Administrador, Director Geral, Farmaceutico, Balconista, Entregador
)

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val description: String
)

@Entity(tableName = "medicines")
data class MedicineEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val categoryId: Int,
    val name: String,
    val price: Double,
    val stock: Int,
    val expiryDate: Long, // timestamp
    val isFeatured: Boolean = false,
    val promotionPrice: Double? = null, // null if no promotion
    val imageUri: String? = null
)

@Entity(tableName = "sales")
data class SaleEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val buyerName: String,
    val saleDate: Long,
    val totalAmount: Double,
    val paymentMethod: String, // Dinheiro, M-Pesa, e-Mola, POS, Transferência
    val sellerId: Int,
    val sellerName: String
)

@Entity(tableName = "sale_items")
data class SaleItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val saleId: Int,
    val medicineId: Int,
    val medicineName: String,
    val quantity: Int,
    val price: Double
)

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val customerName: String,
    val phone: String,
    val address: String,
    val status: String, // Pendente, Em Preparação, Pronto para Entrega, Entregue, Cancelado, Rejeitado
    val totalAmount: Double,
    val paymentMethod: String, // Dinheiro, M-Pesa, e-Mola, POS, Transferência
    val deliveryPersonId: Int? = null,
    val deliveryPersonName: String? = null,
    val orderDate: Long = System.currentTimeMillis(),
    val prescriptionImageUri: String? = null,
    val deliveryType: String = "Levantamento", // Levantamento, Entrega ao domicílio
    val rejectionReason: String? = null,
    val approvalDate: Long? = null
)

@Entity(tableName = "reservations")
data class ReservationEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val customerName: String,
    val phone: String,
    val medicineId: Int,
    val medicineName: String,
    val quantity: Int,
    val reservationDate: Long,
    val expiryDate: Long,
    val status: String, // Pendente, Aprovado, Rejeitado, Levantado, Expirado
    val rejectionReason: String? = null
)

@Entity(tableName = "deliveries")
data class DeliveryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val orderId: Int,
    val deliveryPersonId: Int,
    val deliveryPersonName: String,
    val status: String, // Pendente, A Caminho, Entregue
    val estimatedTime: String,
    val recipientPhone: String,
    val updateDate: Long = System.currentTimeMillis()
)

@Entity(tableName = "suppliers")
data class SupplierEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val contactPhone: String,
    val email: String
)

@Entity(tableName = "stock_movements")
data class StockMovementEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val medicineId: Int,
    val medicineName: String,
    val type: String, // Entrada, Saída
    val quantity: Int,
    val reason: String, // "Venda", "Abastecimento", "Avaria", "Vencido"
    val date: Long = System.currentTimeMillis()
)
