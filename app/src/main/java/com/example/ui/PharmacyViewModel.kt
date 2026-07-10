package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class PharmacyViewModel(private val repository: PharmacyRepository) : ViewModel() {

    // Auth state
    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    // POS Cart State: Map of Medicine ID to CartItem (medicine, quantity)
    data class CartItem(val medicine: MedicineEntity, val quantity: Int)
    private val _cart = MutableStateFlow<Map<Int, CartItem>>(emptyMap())
    val cart: StateFlow<Map<Int, CartItem>> = _cart.asStateFlow()

    // Reactive streams from database
    val users: StateFlow<List<UserEntity>> = repository.allUsers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categories: StateFlow<List<CategoryEntity>> = repository.allCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val medicines: StateFlow<List<MedicineEntity>> = repository.allMedicines
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val sales: StateFlow<List<SaleEntity>> = repository.allSales
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val orders: StateFlow<List<OrderEntity>> = repository.allOrders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val reservations: StateFlow<List<ReservationEntity>> = repository.allReservations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val deliveries: StateFlow<List<DeliveryEntity>> = repository.allDeliveries
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val suppliers: StateFlow<List<SupplierEntity>> = repository.allSuppliers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val stockMovements: StateFlow<List<StockMovementEntity>> = repository.allMovements
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Seed default database values on startup
        seedDatabase()
        checkAndAutoCancelExpired()
    }

    private fun seedDatabase() {
        viewModelScope.launch {
            // Wait for data emission to check if database is empty
            val existingUsers = repository.allUsers.first()
            if (existingUsers.isEmpty()) {
                // Seed default users (role profiles)
                repository.insertUser(UserEntity(name = "Admin Master", username = "edadriano14@gmail.com", password = "1234", role = "Administrador"))
                repository.insertUser(UserEntity(name = "Director Geral", username = "director@farmacia.com", password = "director", role = "Director Geral"))
                repository.insertUser(UserEntity(name = "Carlos (Farmacêutico)", username = "farmaceutico@farmacia.com", password = "farmaceutico", role = "Farmaceutico"))
                repository.insertUser(UserEntity(name = "Ana (Balconista)", username = "balconista@farmacia.com", password = "balconista", role = "Balconista"))
                repository.insertUser(UserEntity(name = "João (Entregador)", username = "entregador@farmacia.com", password = "entregador", role = "Entregador"))
                repository.insertUser(UserEntity(name = "Cliente Demo", username = "cliente@farmacia.com", password = "cliente", role = "Cliente"))
            } else {
                // Ensure the requested administrator exists
                val specAdmin = repository.getUserByUsername("edadriano14@gmail.com")
                if (specAdmin == null) {
                    repository.insertUser(UserEntity(name = "Administrador Geral", username = "edadriano14@gmail.com", password = "1234", role = "Administrador"))
                }
            }

            val existingCategories = repository.allCategories.first()
            if (existingCategories.isEmpty()) {
                // Seed default categories
                val analg = repository.insertCategory(CategoryEntity(name = "Analgésicos", description = "Alívio de dores e febre"))
                val antib = repository.insertCategory(CategoryEntity(name = "Antibióticos", description = "Combate a infecções bacterianas"))
                val vitam = repository.insertCategory(CategoryEntity(name = "Vitaminas & Suplementos", description = "Nutrição e fortalecimento"))
                val cardio = repository.insertCategory(CategoryEntity(name = "Cardiovascular", description = "Controle de pressão e coração"))
                val derm = repository.insertCategory(CategoryEntity(name = "Dermatologia", description = "Cuidado da pele"))

                // Seed medicines
                val now = System.currentTimeMillis()
                val oneMonth = 30L * 24 * 60 * 60 * 1000
                val sixMonths = 180L * 24 * 60 * 60 * 1000
                
                // Medicines
                repository.insertMedicine(MedicineEntity(
                    categoryId = analg.toInt(),
                    name = "Paracetamol 500mg",
                    price = 150.00,
                    stock = 120,
                    expiryDate = now + sixMonths,
                    isFeatured = true,
                    imageUri = "https://images.unsplash.com/photo-1584308666744-24d5c474f2ae?w=400&auto=format&fit=crop"
                ))
                repository.insertMedicine(MedicineEntity(
                    categoryId = analg.toInt(),
                    name = "Ibuprofeno 400mg",
                    price = 220.00,
                    stock = 85,
                    expiryDate = now + (oneMonth / 2), // Expiring in 15 days!
                    isFeatured = false,
                    promotionPrice = 180.00,
                    imageUri = "https://images.unsplash.com/photo-1550572017-edd951b55104?w=400&auto=format&fit=crop"
                ))
                repository.insertMedicine(MedicineEntity(
                    categoryId = antib.toInt(),
                    name = "Amoxicilina 500mg",
                    price = 450.00,
                    stock = 40,
                    expiryDate = now + sixMonths * 2,
                    isFeatured = true,
                    imageUri = "https://images.unsplash.com/photo-1607619275048-24722480f875?w=400&auto=format&fit=crop"
                ))
                repository.insertMedicine(MedicineEntity(
                    categoryId = vitam.toInt(),
                    name = "Vitamina C 1000mg",
                    price = 300.00,
                    stock = 95,
                    expiryDate = now + sixMonths,
                    isFeatured = true,
                    promotionPrice = 250.00,
                    imageUri = "https://images.unsplash.com/photo-1616679911721-eff6eec18fcd?w=400&auto=format&fit=crop"
                ))
                repository.insertMedicine(MedicineEntity(
                    categoryId = cardio.toInt(),
                    name = "Amlodipina 5mg",
                    price = 350.00,
                    stock = 15, // Low stock!
                    expiryDate = now + sixMonths,
                    isFeatured = false,
                    imageUri = "https://images.unsplash.com/photo-1471864190281-a93a3070b6de?w=400&auto=format&fit=crop"
                ))
                repository.insertMedicine(MedicineEntity(
                    categoryId = derm.toInt(),
                    name = "Creme Hidratante Cetaphil",
                    price = 850.00,
                    stock = 25,
                    expiryDate = now + (oneMonth * 12),
                    isFeatured = false,
                    imageUri = "https://images.unsplash.com/photo-1608248597481-496100c8c836?w=400&auto=format&fit=crop"
                ))

                // Seed Suppliers
                val supId1 = repository.insertSupplier(SupplierEntity(name = "MedMoz Distribuidora", contactPhone = "+258 84 123 4567", email = "vendas@medmoz.co.mz"))
                repository.insertSupplier(SupplierEntity(name = "Farmanet Lda", contactPhone = "+258 82 987 6543", email = "geral@farmanet.co.mz"))

                // Seed past sales
                val sale1Id = repository.executeSale(
                    SaleEntity(buyerName = "Manuel Sitoe", saleDate = now - (2 * 60 * 60 * 1000), totalAmount = 370.00, paymentMethod = "M-Pesa", sellerId = 4, sellerName = "Ana (Balconista)"),
                    listOf(
                        SaleItemEntity(saleId = 0, medicineId = 1, medicineName = "Paracetamol 500mg", quantity = 1, price = 150.00),
                        SaleItemEntity(saleId = 0, medicineId = 2, medicineName = "Ibuprofeno 400mg", quantity = 1, price = 220.0)
                    )
                )

                val sale2Id = repository.executeSale(
                    SaleEntity(buyerName = "Sofia Tembe", saleDate = now - (24 * 60 * 60 * 1000), totalAmount = 900.00, paymentMethod = "POS", sellerId = 3, sellerName = "Carlos (Farmacêutico)"),
                    listOf(
                        SaleItemEntity(saleId = 0, medicineId = 3, medicineName = "Amoxicilina 500mg", quantity = 2, price = 450.00)
                    )
                )

                // Seed orders (Online orders)
                val orderId1 = repository.insertOrder(OrderEntity(
                    customerName = "Artur Nhaca", phone = "845551212", address = "Av. Julius Nyerere, 1234, Maputo",
                    status = "Pendente", totalAmount = 450.00, paymentMethod = "e-Mola", orderDate = now - (4 * 60 * 60 * 1000)
                ))
                val orderId2 = repository.insertOrder(OrderEntity(
                    customerName = "Beatriz Cossa", phone = "829990101", address = "Bairro Central, Rua da Resistência, 45, Maputo",
                    status = "Pronto para Entrega", totalAmount = 300.00, paymentMethod = "Dinheiro", orderDate = now - (12 * 60 * 60 * 1000),
                    deliveryPersonId = 5, deliveryPersonName = "João (Entregador)"
                ))

                // Seed reservations
                repository.insertReservation(ReservationEntity(
                    customerName = "Claudio Langa", phone = "840001122", medicineId = 1, medicineName = "Paracetamol 500mg",
                    quantity = 2, reservationDate = now - (1 * 60 * 60 * 1000), expiryDate = now + (24 * 60 * 60 * 1000), status = "Pendente"
                ))

                // Seed deliveries
                repository.insertDelivery(DeliveryEntity(
                    orderId = orderId2.toInt(), deliveryPersonId = 5, deliveryPersonName = "João (Entregador)",
                    status = "A Caminho", estimatedTime = "20 min", recipientPhone = "829990101", updateDate = now - (10 * 60 * 1000)
                ))
            }
        }
    }

    // Login function
    fun login(username: String, password: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val user = repository.getUserByUsername(username)
            if (user == null) {
                onResult(false, "Utilizador não encontrado.")
                return@launch
            }

            val email = if (username.contains("@")) username else "$username@farmacia.com"

            try {
                val mAuth = FirebaseAuth.getInstance()
                mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        viewModelScope.launch {
                            if (task.isSuccessful) {
                                _currentUser.value = user
                                onResult(true, "Autenticado com sucesso via Firebase: Bem-vindo, ${user.name}!")
                            } else {
                                // Firebase authentication failed (maybe offline or credentials mismatch)
                                // Let's check local credentials as fallback so offline works flawlessly!
                                if (user.password == password) {
                                    _currentUser.value = user
                                    onResult(true, "Entrou no modo offline (Firebase offline: ${task.exception?.localizedMessage})")
                                } else {
                                    onResult(false, "Senha incorreta.")
                                }
                            }
                        }
                    }
            } catch (e: Exception) {
                // Firebase not initialized -> fall back to local only
                if (user.password == password) {
                    _currentUser.value = user
                    onResult(true, "Autenticado localmente (Modo Offline): Bem-vindo, ${user.name}!")
                } else {
                    onResult(false, "Senha incorreta.")
                }
            }
        }
    }

    // Register function
    fun register(name: String, username: String, password: String, role: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            if (name.isBlank() || username.isBlank() || password.isBlank()) {
                onResult(false, "Preencha todos os campos obrigatórios.")
                return@launch
            }
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(username).matches()) {
                onResult(false, "O nome de utilizador tem de ser um e-mail válido (exemplo: seu@email.com).")
                return@launch
            }
            val existing = repository.getUserByUsername(username)
            if (existing != null) {
                onResult(false, "Este e-mail já está registado.")
                return@launch
            }

            val email = username
            
            try {
                val mAuth = FirebaseAuth.getInstance()
                mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        viewModelScope.launch {
                            val newUser = UserEntity(name = name, username = username, password = password, role = role)
                            val id = repository.insertUser(newUser)
                            if (task.isSuccessful) {
                                onResult(true, "Registado com sucesso via Firebase Auth!")
                            } else {
                                // Firebase failed (maybe offline or weak password)
                                val firebaseMsg = task.exception?.localizedMessage ?: "Erro Firebase desconhecido"
                                if (id > 0) {
                                    onResult(true, "Registado localmente (Firebase offline: $firebaseMsg)")
                                } else {
                                    onResult(false, "Erro ao registar utilizador localmente.")
                                }
                            }
                        }
                    }
            } catch (e: Exception) {
                // Firebase not initialized or other error -> fall back to local only
                val newUser = UserEntity(name = name, username = username, password = password, role = role)
                val id = repository.insertUser(newUser)
                if (id > 0) {
                    onResult(true, "Registado com sucesso localmente (Modo Offline)!")
                } else {
                    onResult(false, "Erro ao registar utilizador localmente.")
                }
            }
        }
    }

    // Recover Password function
    fun recoverPassword(email: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            if (email.isBlank()) {
                onResult(false, "Por favor, introduza o seu e-mail.")
                return@launch
            }
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                onResult(false, "Por favor, introduza um e-mail válido.")
                return@launch
            }
            
            try {
                val mAuth = FirebaseAuth.getInstance()
                mAuth.sendPasswordResetEmail(email)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            onResult(true, "E-mail de recuperação de palavra-passe enviado via Firebase!")
                        } else {
                            val firebaseMsg = task.exception?.localizedMessage ?: "Erro desconhecido"
                            viewModelScope.launch {
                                val user = repository.getUserByUsername(email)
                                if (user != null) {
                                    onResult(true, "Sucesso (Local): Palavra-passe do utilizador é '${user.password}'.")
                                } else {
                                    onResult(false, "Não foi possível recuperar via Firebase ($firebaseMsg) e nenhum utilizador local foi encontrado.")
                                }
                            }
                        }
                    }
            } catch (e: Exception) {
                // Offline/No firebase fallback
                val user = repository.getUserByUsername(email)
                if (user != null) {
                    onResult(true, "Sucesso (Offline): A sua palavra-passe é '${user.password}'.")
                } else {
                    onResult(false, "E-mail de utilizador não encontrado no sistema local.")
                }
            }
        }
    }

    // Logout
    fun logout() {
        _currentUser.value = null
        clearCart()
    }

    // POS Cart management
    fun addToCart(medicine: MedicineEntity) {
        val currentCart = _cart.value.toMutableMap()
        val existingItem = currentCart[medicine.id]
        val activePrice = medicine.promotionPrice ?: medicine.price
        
        if (existingItem != null) {
            if (existingItem.quantity < medicine.stock) {
                currentCart[medicine.id] = existingItem.copy(quantity = existingItem.quantity + 1)
            }
        } else {
            if (medicine.stock > 0) {
                currentCart[medicine.id] = CartItem(medicine, 1)
            }
        }
        _cart.value = currentCart
    }

    fun removeFromCart(medicine: MedicineEntity) {
        val currentCart = _cart.value.toMutableMap()
        currentCart.remove(medicine.id)
        _cart.value = currentCart
    }

    fun decreaseQuantity(medicine: MedicineEntity) {
        val currentCart = _cart.value.toMutableMap()
        val existingItem = currentCart[medicine.id] ?: return
        if (existingItem.quantity > 1) {
            currentCart[medicine.id] = existingItem.copy(quantity = existingItem.quantity - 1)
        } else {
            currentCart.remove(medicine.id)
        }
        _cart.value = currentCart
    }

    fun clearCart() {
        _cart.value = emptyMap()
    }

    // Checkout POS Cart
    fun checkoutCart(buyerName: String, paymentMethod: String, discountPercent: Int = 0, onResult: (Boolean, String, Int?) -> Unit) {
        viewModelScope.launch {
            val cartItems = _cart.value.values.toList()
            if (cartItems.isEmpty()) {
                onResult(false, "O carrinho está vazio.", null)
                return@launch
            }
            val seller = _currentUser.value
            if (seller == null) {
                onResult(false, "Sessão inválida. Inicie sessão novamente.", null)
                return@launch
            }

            val finalBuyer = if (buyerName.isBlank()) "Cliente Geral" else buyerName
            val subtotal = cartItems.sumOf { (it.medicine.promotionPrice ?: it.medicine.price) * it.quantity }
            val discountAmount = subtotal * (discountPercent / 100.0)
            val total = subtotal - discountAmount

            val sale = SaleEntity(
                buyerName = finalBuyer,
                saleDate = System.currentTimeMillis(),
                totalAmount = total,
                paymentMethod = paymentMethod,
                sellerId = seller.id,
                sellerName = seller.name
            )

            val saleItems = cartItems.map {
                SaleItemEntity(
                    saleId = 0,
                    medicineId = it.medicine.id,
                    medicineName = it.medicine.name,
                    quantity = it.quantity,
                    price = it.medicine.promotionPrice ?: it.medicine.price
                )
            }

            try {
                val saleId = repository.executeSale(sale, saleItems).toInt()
                clearCart()
                onResult(true, "Venda realizada com sucesso! Total: ${String.format("%.2f", total)} MZN (Desconto: $discountPercent%)", saleId)
            } catch (e: Exception) {
                onResult(false, "Erro ao processar venda: ${e.message}", null)
            }
        }
    }

    // Checkout Client Cart (Online Order or Reservation)
    fun checkoutCartAsClient(
        customerName: String,
        phone: String,
        address: String,
        paymentMethod: String,
        isOnlineOrder: Boolean,
        deliveryType: String = "Levantamento",
        discountPercent: Int = 0,
        prescriptionImageUri: String? = null,
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            val cartItems = _cart.value.values.toList()
            if (cartItems.isEmpty()) {
                onResult(false, "O carrinho está vazio.")
                return@launch
            }
            if (customerName.isBlank() || phone.isBlank()) {
                onResult(false, "Nome e Contacto de Telefone são obrigatórios.")
                return@launch
            }
            if (isOnlineOrder && deliveryType == "Entrega ao domicílio" && address.isBlank()) {
                onResult(false, "Endereço de entrega é obrigatório para Entrega ao Domicílio.")
                return@launch
            }

            try {
                if (isOnlineOrder) {
                    val subtotal = cartItems.sumOf { (it.medicine.promotionPrice ?: it.medicine.price) * it.quantity }
                    val discountAmount = subtotal * (discountPercent / 100.0)
                    val total = subtotal - discountAmount
                    val order = OrderEntity(
                        customerName = customerName,
                        phone = phone,
                        address = if (deliveryType == "Levantamento") "Levantamento na Loja" else address,
                        status = "Pendente",
                        totalAmount = total,
                        paymentMethod = paymentMethod,
                        prescriptionImageUri = prescriptionImageUri,
                        deliveryType = deliveryType
                    )
                    val id = repository.insertOrder(order)
                    if (id > 0) {
                        // Subtract stock for the online order items
                        for (item in cartItems) {
                            val med = item.medicine
                            val updatedMed = med.copy(stock = (med.stock - item.quantity).coerceAtLeast(0))
                            repository.updateMedicine(updatedMed)
                            
                            // Log stock movement
                            repository.insertMovement(StockMovementEntity(
                                medicineId = med.id,
                                medicineName = med.name,
                                type = "Saída",
                                quantity = item.quantity,
                                reason = "Encomenda Online #${id} do Cliente ${customerName}"
                            ))
                        }
                        clearCart()
                        onResult(true, "Encomenda Online #${id} submetida com sucesso! Total: $total MZN. A preparar a sua entrega.")
                    } else {
                        onResult(false, "Falha ao processar o seu pedido online.")
                    }
                } else {
                    // It's a Reservation
                    var reservedCount = 0
                    val now = System.currentTimeMillis()
                    for (item in cartItems) {
                        val med = item.medicine
                        if (med.stock >= item.quantity) {
                            val reservation = ReservationEntity(
                                customerName = customerName,
                                phone = phone,
                                medicineId = med.id,
                                medicineName = med.name,
                                quantity = item.quantity,
                                reservationDate = now,
                                expiryDate = now + (48L * 60 * 60 * 1000), // 48 hours validity
                                status = "Pendente"
                            )
                            repository.insertReservation(reservation)
                            
                            // Hold stock
                            val updatedMed = med.copy(stock = med.stock - item.quantity)
                            repository.updateMedicine(updatedMed)
                            
                            // Log stock movement
                            repository.insertMovement(StockMovementEntity(
                                medicineId = med.id,
                                medicineName = med.name,
                                type = "Saída",
                                quantity = item.quantity,
                                reason = "Reserva do Cliente: ${customerName}"
                            ))
                            reservedCount++
                        }
                    }
                    if (reservedCount > 0) {
                        clearCart()
                        onResult(true, "Reserva de $reservedCount item(ns) efetuada com sucesso! Guardaremos por 48 horas para levantamento.")
                    } else {
                        onResult(false, "Lamentamos, mas não há stock suficiente para os itens selecionados.")
                    }
                }
            } catch (e: Exception) {
                onResult(false, "Ocorreu um erro ao processar: ${e.message}")
            }
        }
    }

    // Medicine CRUD
    fun addMedicine(name: String, categoryId: Int, price: Double, stock: Int, expiryDays: Int, isFeatured: Boolean, promotionPrice: Double?, imageUri: String? = null, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            if (name.isBlank() || price <= 0 || stock < 0 || expiryDays <= 0) {
                onResult(false)
                return@launch
            }
            val expiryDate = System.currentTimeMillis() + (expiryDays.toLong() * 24 * 60 * 60 * 1000)
            val med = MedicineEntity(
                categoryId = categoryId,
                name = name,
                price = price,
                stock = stock,
                expiryDate = expiryDate,
                isFeatured = isFeatured,
                promotionPrice = if (promotionPrice != null && promotionPrice > 0) promotionPrice else null,
                imageUri = if (imageUri.isNullOrBlank()) null else imageUri
            )
            val id = repository.insertMedicine(med)
            if (id > 0) {
                repository.insertMovement(StockMovementEntity(
                    medicineId = id.toInt(),
                    medicineName = name,
                    type = "Entrada",
                    quantity = stock,
                    reason = "Registo Inicial"
                ))
                onResult(true)
            } else {
                onResult(false)
            }
        }
    }

    fun updateMedicine(med: MedicineEntity, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                repository.updateMedicine(med)
                onResult(true)
            } catch (e: Exception) {
                onResult(false)
            }
        }
    }

    fun deleteMedicine(med: MedicineEntity) {
        viewModelScope.launch {
            repository.deleteMedicine(med)
        }
    }

    // Category actions
    fun addCategory(name: String, description: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            if (name.isBlank()) {
                onResult(false)
                return@launch
            }
            val cat = CategoryEntity(name = name, description = description)
            val id = repository.insertCategory(cat)
            onResult(id > 0)
        }
    }

    fun deleteCategory(cat: CategoryEntity) {
        viewModelScope.launch {
            repository.deleteCategory(cat)
        }
    }

    // Stock Movement Manual Adjustment
    fun addStockMovement(medicineId: Int, medicineName: String, type: String, quantity: Int, reason: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            if (quantity <= 0) {
                onResult(false)
                return@launch
            }
            val movement = StockMovementEntity(
                medicineId = medicineId,
                medicineName = medicineName,
                type = type,
                quantity = quantity,
                reason = reason
            )
            repository.insertMovement(movement)
            onResult(true)
        }
    }

    // Online Orders
    fun addOnlineOrder(customerName: String, phone: String, address: String, totalAmount: Double, paymentMethod: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            if (customerName.isBlank() || phone.isBlank() || address.isBlank() || totalAmount <= 0) {
                onResult(false)
                return@launch
            }
            val order = OrderEntity(
                customerName = customerName,
                phone = phone,
                address = address,
                status = "Pendente",
                totalAmount = totalAmount,
                paymentMethod = paymentMethod
            )
            val id = repository.insertOrder(order)
            onResult(id > 0)
        }
    }

    fun updateOrderStatus(orderId: Int, newStatus: String) {
        viewModelScope.launch {
            val ordersList = repository.allOrders.first()
            val order = ordersList.find { it.id == orderId } ?: return@launch
            val updated = order.copy(status = newStatus)
            repository.updateOrder(updated)

            // If changing to "Pronto para Entrega" and an entregador is already assigned, create/update delivery status
            if (newStatus == "Pronto para Entrega" || newStatus == "Em Preparação") {
                // Auto sync or trigger delivery creation if needed
            }
        }
    }

    fun assignOrderDelivery(orderId: Int, deliveryPersonId: Int, deliveryPersonName: String) {
        viewModelScope.launch {
            val ordersList = repository.allOrders.first()
            val order = ordersList.find { it.id == orderId } ?: return@launch
            val updatedOrder = order.copy(
                deliveryPersonId = deliveryPersonId,
                deliveryPersonName = deliveryPersonName,
                status = "Pronto para Entrega"
            )
            repository.updateOrder(updatedOrder)

            // Insert into Deliveries table
            val delivery = DeliveryEntity(
                orderId = orderId,
                deliveryPersonId = deliveryPersonId,
                deliveryPersonName = deliveryPersonName,
                status = "Pendente",
                estimatedTime = "30-45 min",
                recipientPhone = order.phone
            )
            repository.insertDelivery(delivery)
        }
    }

    // Reservations
    fun addReservation(customerName: String, phone: String, medicine: MedicineEntity, quantity: Int, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            if (customerName.isBlank() || phone.isBlank() || quantity <= 0 || medicine.stock < quantity) {
                onResult(false)
                return@launch
            }
            val now = System.currentTimeMillis()
            val reservation = ReservationEntity(
                customerName = customerName,
                phone = phone,
                medicineId = medicine.id,
                medicineName = medicine.name,
                quantity = quantity,
                reservationDate = now,
                expiryDate = now + (48L * 60 * 60 * 1000), // 48 hours validity
                status = "Pendente"
            )
            repository.insertReservation(reservation)
            
            // Hold medicine stock
            val updatedMed = medicine.copy(stock = medicine.stock - quantity)
            repository.updateMedicine(updatedMed)
            
            // Log stock movement
            repository.insertMovement(StockMovementEntity(
                medicineId = medicine.id,
                medicineName = medicine.name,
                type = "Saída",
                quantity = quantity,
                reason = "Reserva Cliente: ${customerName}"
            ))

            onResult(true)
        }
    }

    fun pickUpReservation(reservationId: Int, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val reservationsList = repository.allReservations.first()
            val reservation = reservationsList.find { it.id == reservationId } ?: return@launch
            if (reservation.status != "Pendente") {
                onResult(false)
                return@launch
            }

            // Create POS sale for this reservation
            val seller = _currentUser.value ?: UserEntity(name = "Balconista Geral", username = "balconista", password = "123", role = "Balconista")
            val sale = SaleEntity(
                buyerName = reservation.customerName,
                saleDate = System.currentTimeMillis(),
                totalAmount = 0.0, // calculated from medicine price
                paymentMethod = "Dinheiro",
                sellerId = seller.id,
                sellerName = seller.name
            )

            val med = repository.getMedicineById(reservation.medicineId)
            val price = med?.promotionPrice ?: med?.price ?: 0.0
            val total = price * reservation.quantity

            val finalSale = sale.copy(totalAmount = total)
            val item = SaleItemEntity(
                saleId = 0,
                medicineId = reservation.medicineId,
                medicineName = reservation.medicineName,
                quantity = reservation.quantity,
                price = price
            )

            // Execute sale (does not subtract stock again since it was held on reservation, but let's register the sale item correctly)
            val saleId = repository.insertUser(UserEntity(name = "", username = "", password = "", role = "")) // placeholder
            // To prevent double subtraction, we can just log a direct sale insert
            val insertedSaleId = repository.executeSale(finalSale, listOf(item))
            
            // Update reservation status
            val updatedRes = reservation.copy(status = "Levantado")
            repository.updateReservation(updatedRes)
            
            // Adjust movement log (adjust text)
            repository.insertMovement(StockMovementEntity(
                medicineId = reservation.medicineId,
                medicineName = reservation.medicineName,
                type = "Saída",
                quantity = 0, // already accounted
                reason = "Reserva #${reservationId} Levantada (Venda POS #${insertedSaleId})"
            ))

            onResult(true)
        }
    }

    fun cancelReservation(reservationId: Int) {
        viewModelScope.launch {
            val reservationsList = repository.allReservations.first()
            val reservation = reservationsList.find { it.id == reservationId } ?: return@launch
            if (reservation.status != "Pendente") return@launch

            // Return stock to medicine
            val med = repository.getMedicineById(reservation.medicineId)
            if (med != null) {
                repository.updateMedicine(med.copy(stock = med.stock + reservation.quantity))
                repository.insertMovement(StockMovementEntity(
                    medicineId = reservation.medicineId,
                    medicineName = reservation.medicineName,
                    type = "Entrada",
                    quantity = reservation.quantity,
                    reason = "Cancelamento Reserva #${reservationId}"
                ))
            }

            repository.updateReservation(reservation.copy(status = "Expirado"))
        }
    }

    // Deliveries
    fun updateDeliveryStatus(deliveryId: Int, newStatus: String) {
        viewModelScope.launch {
            val deliveriesList = repository.allDeliveries.first()
            val delivery = deliveriesList.find { it.id == deliveryId } ?: return@launch
            val updated = delivery.copy(status = newStatus, updateDate = System.currentTimeMillis())
            repository.updateDelivery(updated)

            // Sync status to the corresponding Order
            if (newStatus == "Entregue") {
                updateOrderStatus(delivery.orderId, "Entregue")
            } else if (newStatus == "A Caminho") {
                updateOrderStatus(delivery.orderId, "A Caminho")
            }
        }
    }

    // Supplier CRUD
    fun addSupplier(name: String, phone: String, email: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            if (name.isBlank() || phone.isBlank()) {
                onResult(false)
                return@launch
            }
            val supplier = SupplierEntity(name = name, contactPhone = phone, email = email)
            val id = repository.insertSupplier(supplier)
            onResult(id > 0)
        }
    }

    fun deleteSupplier(supplier: SupplierEntity) {
        viewModelScope.launch {
            repository.deleteSupplier(supplier)
        }
    }

    fun deleteUser(user: UserEntity) {
        viewModelScope.launch {
            repository.deleteUser(user)
        }
    }

    fun updateUser(user: UserEntity, onResult: (Boolean, String) -> Unit = { _, _ -> }) {
        viewModelScope.launch {
            try {
                repository.updateUser(user)
                if (_currentUser.value?.id == user.id) {
                    _currentUser.value = user
                }
                onResult(true, "Perfil atualizado com sucesso!")
            } catch (e: Exception) {
                onResult(false, "Erro ao atualizar perfil: ${e.message}")
            }
        }
    }

    fun approveOrder(orderId: Int) {
        viewModelScope.launch {
            val ordersList = repository.allOrders.first()
            val order = ordersList.find { it.id == orderId } ?: return@launch
            val updated = order.copy(status = "Aprovado", approvalDate = System.currentTimeMillis())
            repository.updateOrder(updated)
        }
    }

    fun rejectOrder(orderId: Int, reason: String) {
        viewModelScope.launch {
            val ordersList = repository.allOrders.first()
            val order = ordersList.find { it.id == orderId } ?: return@launch
            val updated = order.copy(status = "Rejeitado", rejectionReason = reason)
            repository.updateOrder(updated)
            
            // Restore medicine stock
            try {
                val movements = repository.allMovements.first()
                val orderMovements = movements.filter { it.reason.contains("Encomenda Online #${orderId}") }
                for (mov in orderMovements) {
                    val med = repository.allMedicines.first().find { it.id == mov.medicineId }
                    if (med != null) {
                        repository.updateMedicine(med.copy(stock = med.stock + mov.quantity))
                        repository.insertMovement(StockMovementEntity(
                            medicineId = med.id,
                            medicineName = med.name,
                            type = "Entrada",
                            quantity = mov.quantity,
                            reason = "Devolução Stock Rejeição Encomenda #${orderId}"
                        ))
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun approveReservation(resId: Int) {
        viewModelScope.launch {
            val reservationsList = repository.allReservations.first()
            val res = reservationsList.find { it.id == resId } ?: return@launch
            val updated = res.copy(
                status = "Aprovado",
                expiryDate = System.currentTimeMillis() + (48L * 60 * 60 * 1000)
            )
            repository.updateReservation(updated)
        }
    }

    fun rejectReservation(resId: Int, reason: String) {
        viewModelScope.launch {
            val reservationsList = repository.allReservations.first()
            val res = reservationsList.find { it.id == resId } ?: return@launch
            val updated = res.copy(status = "Rejeitado", rejectionReason = reason)
            repository.updateReservation(updated)
            
            // Restore medicine stock
            val med = repository.allMedicines.first().find { it.id == res.medicineId }
            if (med != null) {
                repository.updateMedicine(med.copy(stock = med.stock + res.quantity))
                repository.insertMovement(StockMovementEntity(
                    medicineId = med.id,
                    medicineName = med.name,
                    type = "Entrada",
                    quantity = res.quantity,
                    reason = "Reserva Rejeitada #${resId}: Stock Devolvido"
                ))
            }
        }
    }

    fun checkAndAutoCancelExpired() {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val fortyEightHours = 48L * 60 * 60 * 1000
            
            // Check Orders
            val ordersList = repository.allOrders.first()
            for (order in ordersList) {
                if (order.status == "Aprovado" && order.approvalDate != null) {
                    if (now - order.approvalDate > fortyEightHours) {
                        repository.updateOrder(order.copy(status = "Cancelado", rejectionReason = "Cancelado automaticamente: Excedeu prazo de 48h após aprovação."))
                        
                        // Restore stock
                        try {
                            val movements = repository.allMovements.first()
                            val orderMovements = movements.filter { it.reason.contains("Encomenda Online #${order.id}") }
                            for (mov in orderMovements) {
                                val med = repository.allMedicines.first().find { it.id == mov.medicineId }
                                if (med != null) {
                                    repository.updateMedicine(med.copy(stock = med.stock + mov.quantity))
                                    repository.insertMovement(StockMovementEntity(
                                        medicineId = med.id,
                                        medicineName = med.name,
                                        type = "Entrada",
                                        quantity = mov.quantity,
                                        reason = "Auto-Cancelamento Encomenda #${order.id}: Stock Devolvido"
                                    ))
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
            
            // Check Reservations
            val reservationsList = repository.allReservations.first()
            for (res in reservationsList) {
                if (res.status == "Aprovado" && now > res.expiryDate) {
                    repository.updateReservation(res.copy(status = "Expirado"))
                    
                    // Restore stock
                    val med = repository.allMedicines.first().find { it.id == res.medicineId }
                    if (med != null) {
                        repository.updateMedicine(med.copy(stock = med.stock + res.quantity))
                        repository.insertMovement(StockMovementEntity(
                            medicineId = med.id,
                            medicineName = med.name,
                            type = "Entrada",
                            quantity = res.quantity,
                            reason = "Auto-Cancelamento Reserva #${res.id}: Stock Devolvido"
                        ))
                    }
                }
            }
        }
    }

    fun simulate48HoursPassed() {
        viewModelScope.launch {
            val fortyEightHoursPlus = 49L * 60 * 60 * 1000
            
            // Shift approval dates of orders back by 49 hours
            val ordersList = repository.allOrders.first()
            for (order in ordersList) {
                if (order.status == "Aprovado" && order.approvalDate != null) {
                    val shiftedOrder = order.copy(approvalDate = order.approvalDate!! - fortyEightHoursPlus)
                    repository.updateOrder(shiftedOrder)
                }
            }
            
            // Shift expiry dates of reservations back
            val reservationsList = repository.allReservations.first()
            for (res in reservationsList) {
                if (res.status == "Aprovado") {
                    val shiftedRes = res.copy(expiryDate = System.currentTimeMillis() - 1000)
                    repository.updateReservation(shiftedRes)
                }
            }
            
            // Run check immediately
            checkAndAutoCancelExpired()
        }
    }

    suspend fun getItemsForSale(saleId: Int): List<SaleItemEntity> {
        return repository.getItemsForSale(saleId)
    }
}

class PharmacyViewModelFactory(private val repository: PharmacyRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PharmacyViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PharmacyViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
