@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class, androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.ui

import android.widget.Toast
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.*
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.launch
import coil.compose.SubcomposeAsyncImage
import androidx.compose.ui.layout.ContentScale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PharmacyApp(viewModel: PharmacyViewModel) {
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        if (currentUser == null) {
            LoginScreen(viewModel)
        } else {
            MainPharmacyScreen(viewModel, currentUser!!)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(viewModel: PharmacyViewModel) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isRegisterMode by remember { mutableStateOf(false) }
    var isRecoveryMode by remember { mutableStateOf(false) }
    
    // For Registration
    var regName by remember { mutableStateOf("") }
    var regUsername by remember { mutableStateOf("") }
    var regPassword by remember { mutableStateOf("") }
    var regRole by remember { mutableStateOf("Cliente") }

    val rolesList = listOf("Administrador", "Director Geral", "Farmaceutico", "Balconista", "Entregador", "Cliente")
    var rolesDropdownExpanded by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f),
                            MaterialTheme.colorScheme.background
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .widthIn(max = 480.dp)
                    .padding(16.dp),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Stylized logo
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalPharmacy,
                            contentDescription = "Logo",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(40.dp)
                        )
                    }

                    Text(
                        text = "LA REFERENCE",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        ),
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "Sistema de Gestão de Farmácia",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    if (isRecoveryMode) {
                        Text(
                            text = "Recuperar Palavra-passe",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.align(Alignment.Start)
                        )
                        Text(
                            text = "Introduza o e-mail associado à sua conta para recuperar a sua palavra-passe.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.align(Alignment.Start)
                        )
                        var recoveryEmail by remember { mutableStateOf("") }
                        OutlinedTextField(
                            value = recoveryEmail,
                            onValueChange = { recoveryEmail = it },
                            label = { Text("E-mail") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("recovery_email_input")
                        )
                        Button(
                            onClick = {
                                viewModel.recoverPassword(recoveryEmail) { success, msg ->
                                    scope.launch {
                                        snackbarHostState.showSnackbar(msg)
                                        if (success) {
                                            isRecoveryMode = false
                                        }
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("recover_button"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Recuperar", style = MaterialTheme.typography.titleMedium)
                        }
                        TextButton(onClick = { isRecoveryMode = false }) {
                            Text("Voltar para Iniciar Sessão")
                        }
                    } else if (!isRegisterMode) {
                        // Login fields
                        OutlinedTextField(
                            value = username,
                            onValueChange = { username = it },
                            label = { Text("E-mail") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("username_input")
                        )

                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Palavra-passe") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("password_input")
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(
                                onClick = { isRecoveryMode = true },
                                modifier = Modifier.testTag("forgot_password_button")
                            ) {
                                Text("Esqueceu-se da palavra-passe?", style = MaterialTheme.typography.bodySmall)
                            }
                        }

                        Button(
                            onClick = {
                                viewModel.login(username, password) { success, msg ->
                                    if (!success) {
                                        scope.launch {
                                            snackbarHostState.showSnackbar(msg)
                                        }
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("login_button"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Entrar", style = MaterialTheme.typography.titleMedium)
                        }

                        TextButton(onClick = { isRegisterMode = true }) {
                            Text("Não tem conta? Registe-se aqui")
                        }

                    } else {
                        // Register fields
                        OutlinedTextField(
                            value = regName,
                            onValueChange = { regName = it },
                            label = { Text("Nome Completo") },
                            leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("reg_name_input")
                        )

                        OutlinedTextField(
                            value = regUsername,
                            onValueChange = { regUsername = it },
                            label = { Text("E-mail") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("reg_username_input")
                        )

                        OutlinedTextField(
                            value = regPassword,
                            onValueChange = { regPassword = it },
                            label = { Text("Palavra-passe") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("reg_password_input")
                        )

                        // Profile display locked strictly to "Cliente" (all other profiles blocked)
                        OutlinedTextField(
                            value = "Cliente",
                            onValueChange = {},
                            label = { Text("Perfil de Acesso") },
                            leadingIcon = { Icon(Icons.Default.AccountCircle, contentDescription = null) },
                            readOnly = true,
                            enabled = false,
                            modifier = Modifier.fillMaxWidth().testTag("reg_role_locked")
                        )

                        Button(
                            onClick = {
                                viewModel.register(regName, regUsername, regPassword, regRole) { success, msg ->
                                    scope.launch {
                                        snackbarHostState.showSnackbar(msg)
                                        if (success) {
                                            isRegisterMode = false
                                            username = regUsername
                                            password = regPassword
                                        }
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("register_button"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Criar Conta", style = MaterialTheme.typography.titleMedium)
                        }

                        TextButton(onClick = { isRegisterMode = false }) {
                            Text("Já tem conta? Inicie sessão")
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MainPharmacyScreen(viewModel: PharmacyViewModel, user: UserEntity) {
    // Current module screen selection
    // Allowed modules depend on permissions
    val allowedTabs = remember(user.role) {
        val list = mutableListOf<PharmacyTab>()
        
        val isAdmin = user.role == "Administrador"
        val isCliente = user.role == "Cliente"
        val isStaff = user.role == "Administrador" || user.role == "Director Geral" || user.role == "Farmaceutico" || user.role == "Balconista"
        
        // Always include Home as first tab (Página Principal)
        list.add(PharmacyTab.Home)
        
        if (isCliente || isStaff) {
            list.add(PharmacyTab.Pos)
        }
        if (isAdmin) {
            list.add(PharmacyTab.Medicines)
        }
        if (isAdmin || user.role == "Farmaceutico") {
            list.add(PharmacyTab.Stock)
        }
        if (isCliente || isAdmin || user.role == "Farmaceutico" || user.role == "Balconista" || user.role == "Entregador") {
            list.add(PharmacyTab.Orders)
        }
        if (isAdmin || user.role == "Entregador") {
            list.add(PharmacyTab.Deliveries)
        }
        
        // Always include Settings at the end (consolidates Dashboard, Info, Profile)
        list.add(PharmacyTab.Settings)
        list
    }

    var selectedTab by remember { mutableStateOf(allowedTabs.first()) }

    Scaffold(
        topBar = {
            PharmacyTopBar(user = user, onLogout = { viewModel.logout() })
        },
        bottomBar = {
            PharmacyBottomNavigation(
                tabs = allowedTabs,
                selectedTab = selectedTab,
                isCliente = user.role == "Cliente",
                onTabSelected = { selectedTab = it }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Crossfade(targetState = selectedTab, label = "ScreenTransition") { tab ->
                when (tab) {
                    PharmacyTab.Home -> HomeScreen(viewModel)
                    PharmacyTab.Pos -> PosScreen(viewModel)
                    PharmacyTab.Medicines -> MedicinesScreen(viewModel)
                    PharmacyTab.Stock -> StockScreen(viewModel)
                    PharmacyTab.Orders -> OrdersScreen(viewModel)
                    PharmacyTab.Deliveries -> DeliveriesScreen(viewModel, user)
                    PharmacyTab.Suppliers -> SuppliersScreen(viewModel)
                    PharmacyTab.Settings -> SettingsScreen(viewModel, user)
                }
            }
        }
    }
}

@Composable
fun SettingsScreen(viewModel: PharmacyViewModel, user: UserEntity) {
    var activeSubScreen by remember { mutableStateOf<String?>(null) } // null = Menu, "dashboard", "info", "profile", "admin_panel", "receipt_history"

    if (activeSubScreen != null) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f))
                    .clickable { activeSubScreen = null }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Voltar",
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = when (activeSubScreen) {
                        "dashboard" -> "Definições > Dashboard"
                        "info" -> "Definições > Farmácia"
                        "profile" -> "Definições > Meu Perfil"
                        "admin_panel" -> "Definições > Painel de Administração"
                        "receipt_history" -> "Definições > Histórico de Recibos"
                        else -> "Voltar"
                    },
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                )
            }
            Box(modifier = Modifier.weight(1f)) {
                when (activeSubScreen) {
                    "dashboard" -> DashboardScreen(viewModel)
                    "info" -> InfoScreen()
                    "profile" -> ProfileScreen(viewModel)
                    "admin_panel" -> SuppliersScreen(viewModel)
                    "receipt_history" -> ReceiptHistoryScreen(viewModel)
                }
            }
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Configurações do Sistema",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )

            val isAdminOrDirector = user.role == "Administrador" || user.role == "Director Geral"

            if (isAdminOrDirector) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { activeSubScreen = "dashboard" },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Dashboard, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Dashboard de Vendas", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                            Text("Estatísticas, relatórios e métricas da farmácia", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null)
                    }
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { activeSubScreen = "info" },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(MaterialTheme.colorScheme.secondaryContainer, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Informações da Farmácia", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        Text("Morada, contactos e detalhes institucionais", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = null)
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { activeSubScreen = "profile" },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(MaterialTheme.colorScheme.tertiaryContainer, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Meu Perfil de Utilizador", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        Text("Dados de login, permissões e detalhes da conta", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = null)
                }
            }

            // Receipt History Option Card (available to all users)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { activeSubScreen = "receipt_history" },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Histórico de Recibos", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        Text("Visualizar, pesquisar e imprimir todos os recibos de vendas guardados", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = null)
                }
            }

            // Administration Panel Card (Only visible to Administrador)
            val isAdmin = user.role == "Administrador"
            if (isAdmin) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { activeSubScreen = "admin_panel" },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(MaterialTheme.colorScheme.errorContainer, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.SupervisorAccount, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Painel de Administração", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                            Text("Gerir utilizadores do sistema e fornecedores parceiros", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null)
                    }
                }
            }
        }
    }
}

@Composable
fun ReceiptHistoryScreen(viewModel: PharmacyViewModel) {
    val sales by viewModel.sales.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    var searchQuery by remember { mutableStateOf("") }
    
    var activeReceiptSale by remember { mutableStateOf<SaleEntity?>(null) }
    var activeReceiptItems by remember { mutableStateOf<List<SaleItemEntity>>(emptyList()) }
    
    val filteredSales = remember(sales, searchQuery) {
        sales.filter { sale ->
            sale.buyerName.contains(searchQuery, ignoreCase = true) ||
            sale.paymentMethod.contains(searchQuery, ignoreCase = true) ||
            "#VND-${sale.id}".contains(searchQuery, ignoreCase = true) ||
            "VND-${sale.id}".contains(searchQuery, ignoreCase = true)
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Pesquisar por Cliente, Método ou ID") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        
        if (filteredSales.isEmpty()) {
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Nenhum recibo encontrado.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredSales) { sale ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                scope.launch {
                                    val items = viewModel.getItemsForSale(sale.id)
                                    activeReceiptSale = sale
                                    activeReceiptItems = items
                                }
                            },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "Venda Nº: #VND-${sale.id}",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "Cliente: ${sale.buyerName}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date(sale.saleDate)),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "${sale.totalAmount} MZN",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = SuccessGreen)
                                )
                                Text(
                                    text = sale.paymentMethod,
                                    style = MaterialTheme.typography.labelSmall
                                )
                                Icon(
                                    imageVector = Icons.Default.ReceiptLong,
                                    contentDescription = "Ver Detalhes",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    
    if (activeReceiptSale != null) {
        ReceiptPreviewAndPrintDialog(
            sale = activeReceiptSale!!,
            items = activeReceiptItems,
            onDismiss = {
                activeReceiptSale = null
                activeReceiptItems = emptyList()
            },
            viewModel = viewModel
        )
    }
}

enum class PharmacyTab(val label: String, val icon: ImageVector) {
    Home("Início", Icons.Default.Home),
    Pos("POS / Vendas", Icons.Default.PointOfSale),
    Medicines("Medicamentos", Icons.Default.Medication),
    Stock("Stock / Validade", Icons.Default.Inventory),
    Orders("Pedidos / Reservas", Icons.Default.LibraryBooks),
    Deliveries("Entregas", Icons.Default.LocalShipping),
    Suppliers("Fornecedores", Icons.Default.Business),
    Settings("Configurações", Icons.Default.Settings)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PharmacyTopBar(user: UserEntity, onLogout: () -> Unit) {
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary
        ),
        title = {
            Column {
                Text(
                    text = "LA REFERENCE",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "Gestão de Farmácia",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f))
                )
            }
        },
        actions = {
            // Profile Badge
            val roleColor = when (user.role) {
                "Administrador" -> WarningRed
                "Director Geral" -> HighlightOrange
                "Farmaceutico" -> Teal80
                "Balconista" -> SuccessGreen
                else -> Color.Blue
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = roleColor.copy(alpha = 0.2f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = null,
                        tint = roleColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = user.name.split(" ").firstOrNull() ?: user.role,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "(${user.role})",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            IconButton(onClick = onLogout) {
                Icon(
                    imageVector = Icons.Default.Logout,
                    contentDescription = "Sair",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    )
}

@Composable
fun PharmacyBottomNavigation(
    tabs: List<PharmacyTab>,
    selectedTab: PharmacyTab,
    isCliente: Boolean,
    onTabSelected: (PharmacyTab) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        tabs.forEach { tab ->
            val label = when {
                tab == PharmacyTab.Orders && isCliente -> "Meus Pedidos"
                tab == PharmacyTab.Suppliers -> "Admin"
                else -> tab.label
            }
            NavigationBarItem(
                selected = selectedTab == tab,
                onClick = { onTabSelected(tab) },
                icon = { Icon(tab.icon, contentDescription = label) },
                label = { Text(label, maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 10.sp) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    }
}

// -------------------------------------------------------------
// MODULE 1: DASHBOARD & REPORTING
// -------------------------------------------------------------
@Composable
fun DashboardScreen(viewModel: PharmacyViewModel) {
    val medicines by viewModel.medicines.collectAsStateWithLifecycle()
    val sales by viewModel.sales.collectAsStateWithLifecycle()
    val orders by viewModel.orders.collectAsStateWithLifecycle()
    val reservations by viewModel.reservations.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    var reportPeriod by remember { mutableStateOf("Mensal") } // Diário, Semanal, Mensal, Anual
    var selectedMedForDetail by remember { mutableStateOf<MedicineEntity?>(null) }
    var activeReceiptPreviewSale by remember { mutableStateOf<SaleEntity?>(null) }
    var activeReceiptPreviewItems by remember { mutableStateOf<List<SaleItemEntity>>(emptyList()) }

    val totalRevenue = remember(sales, reportPeriod) {
        // filter or simulate period
        val factor = when (reportPeriod) {
            "Diário" -> 0.15
            "Semanal" -> 0.4
            "Mensal" -> 1.0
            "Anual" -> 12.0
            else -> 1.0
        }
        sales.sumOf { it.totalAmount } * factor
    }

    val totalSalesCount = remember(sales, reportPeriod) {
        val factor = when (reportPeriod) {
            "Diário" -> 1
            "Semanal" -> 2
            "Mensal" -> sales.size
            "Anual" -> sales.size * 10
            else -> sales.size
        }
        factor
    }

    val lowStockCount = remember(medicines) {
        medicines.count { it.stock <= 20 }
    }

    val expiringSoonCount = remember(medicines) {
        val now = System.currentTimeMillis()
        val thirtyDays = 30L * 24 * 60 * 60 * 1000
        medicines.count { (it.expiryDate - now) <= thirtyDays }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Report Period Selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Visão Geral",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf("Diário", "Semanal", "Mensal", "Anual").forEach { period ->
                    FilterChip(
                        selected = reportPeriod == period,
                        onClick = { reportPeriod = period },
                        label = { Text(period, fontSize = 11.sp) }
                    )
                }
            }
        }

        // Key indicators Grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            IndicatorCard(
                title = "Faturação",
                value = String.format(Locale.US, "%,.2f", totalRevenue) + " MZN",
                icon = Icons.Default.MonetizationOn,
                color = SuccessGreen,
                modifier = Modifier.weight(1f)
            )
            IndicatorCard(
                title = "Vendas POS",
                value = "$totalSalesCount",
                icon = Icons.Default.ShoppingCart,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            IndicatorCard(
                title = "Rutura Stock (<=20)",
                value = "$lowStockCount itens",
                icon = Icons.Default.Warning,
                color = HighlightOrange,
                modifier = Modifier.weight(1f)
            )
            IndicatorCard(
                title = "Expira Breve (<30d)",
                value = "$expiringSoonCount itens",
                icon = Icons.Default.NotificationImportant,
                color = WarningRed,
                modifier = Modifier.weight(1f)
            )
        }

        // Charts Section (Canvas drawings)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Distribuição de Pagamentos (${reportPeriod})",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )

                // Simple custom Pie Chart inside canvas
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val colors = listOf(SuccessGreen, HighlightOrange, WarningRed, Color.Blue, Color.Cyan)
                    val methods = listOf("Dinheiro", "M-Pesa", "e-Mola", "POS", "Transf.")
                    val rawShares = listOf(40f, 30f, 15f, 10f, 5f) // simulated values based on portuguese system

                    Canvas(modifier = Modifier.size(120.dp)) {
                        var startAngle = -90f
                        val totalShares = rawShares.sum()
                        
                        rawShares.forEachIndexed { index, share ->
                            val sweepAngle = (share / totalShares) * 360f
                            drawArc(
                                color = colors[index % colors.size],
                                startAngle = startAngle,
                                sweepAngle = sweepAngle,
                                useCenter = false,
                                style = Stroke(width = 24.dp.toPx(), cap = StrokeCap.Round)
                            )
                            startAngle += sweepAngle
                        }
                    }

                    // Legends custom box
                    Column(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        methods.forEachIndexed { index, method ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(colors[index % colors.size])
                                )
                                Text(
                                    text = "$method (${rawShares[index].toInt()}%)",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }
                }
            }
        }

        // Dynamic Segmented Sales bar
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Vendas Recentes",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )

                if (sales.isEmpty()) {
                    Text("Nenhuma venda registada ainda.", style = MaterialTheme.typography.bodyMedium)
                } else {
                    val context = LocalContext.current
                    sales.take(4).forEach { sale ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(sale.buyerName, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                                Text(
                                    SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(sale.saleDate)),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        "${sale.totalAmount} MZN",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = SuccessGreen)
                                    )
                                    Text(sale.paymentMethod, style = MaterialTheme.typography.labelSmall)
                                }
                                IconButton(
                                    onClick = {
                                        scope.launch {
                                            val saleItems = viewModel.getItemsForSale(sale.id)
                                            activeReceiptPreviewSale = sale
                                            activeReceiptPreviewItems = saleItems
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Print,
                                        contentDescription = "Visualizar e Imprimir Recibo",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                        HorizontalDivider()
                    }
                }
            }
        }

        // ---------------------------------------------------------
        // ADDED SECTIONS: FEATURED AND PROMOTIONAL PRODUCTS IN CARDS
        // ---------------------------------------------------------
        val featuredList = remember(medicines) { medicines.filter { it.isFeatured } }
        val promoList = remember(medicines) { medicines.filter { it.promotionPrice != null } }

        if (featuredList.isNotEmpty()) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = HighlightOrange,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Medicamentos em Destaque",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp)
                ) {
                    items(featuredList) { med ->
                        Card(
                            modifier = Modifier
                                .width(180.dp)
                                .clickable { selectedMedForDetail = med },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                            ),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                        ) {
                            Column {
                                ProductImage(
                                    imageUri = med.imageUri,
                                    categoryId = med.categoryId,
                                    categories = categories,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(110.dp)
                                        .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                                )
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = med.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                val catName = categories.find { it.id == med.categoryId }?.name ?: "Geral"
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = catName,
                                        style = MaterialTheme.typography.labelSmall,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        if (med.promotionPrice != null) {
                                            Text(
                                                text = "${med.promotionPrice} MZN",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = WarningRed,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "${med.price} MZN",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                                                ),
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        } else {
                                            Text(
                                                text = "${med.price} MZN",
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    IconButton(
                                        onClick = { viewModel.addToCart(med) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AddShoppingCart,
                                            contentDescription = "Adicionar ao carrinho",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

        if (promoList.isNotEmpty()) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalOffer,
                            contentDescription = null,
                            tint = WarningRed,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Super Promoções",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = WarningRed.copy(alpha = 0.15f)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "DESCONTO",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = WarningRed,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp)
                ) {
                    items(promoList) { med ->
                        Card(
                            modifier = Modifier
                                .width(180.dp)
                                .clickable { selectedMedForDetail = med },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = WarningRed.copy(alpha = 0.04f)
                            ),
                            border = BorderStroke(1.dp, WarningRed.copy(alpha = 0.2f))
                        ) {
                            Column {
                                ProductImage(
                                    imageUri = med.imageUri,
                                    categoryId = med.categoryId,
                                    categories = categories,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(110.dp)
                                        .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                                )
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = med.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                val catName = categories.find { it.id == med.categoryId }?.name ?: "Geral"
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = catName,
                                        style = MaterialTheme.typography.labelSmall,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "${med.promotionPrice} MZN",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = WarningRed,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "${med.price} MZN",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                                            ),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    IconButton(
                                        onClick = { viewModel.addToCart(med) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AddShoppingCart,
                                            contentDescription = "Adicionar",
                                            tint = WarningRed,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (selectedMedForDetail != null) {
        val med = selectedMedForDetail!!
        Dialog(onDismissRequest = { selectedMedForDetail = null }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ProductImage(
                        imageUri = med.imageUri,
                        categoryId = med.categoryId,
                        categories = categories,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )

                    Text(med.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

                    val catName = categories.find { it.id == med.categoryId }?.name ?: "Sem Categoria"
                    Text("Categoria: $catName", style = MaterialTheme.typography.bodyMedium)

                    Text("Quantidade em Stock: ${med.stock} unidades", style = MaterialTheme.typography.bodyMedium)

                    val expiryStr = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(med.expiryDate))
                    Text("Data de Validade: $expiryStr", style = MaterialTheme.typography.bodyMedium)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Preço:", style = MaterialTheme.typography.bodyMedium)
                        if (med.promotionPrice != null) {
                            Text(
                                "${med.promotionPrice} MZN",
                                style = MaterialTheme.typography.titleMedium,
                                color = WarningRed,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "${med.price} MZN",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            Text(
                                "${med.price} MZN",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { selectedMedForDetail = null }) { Text("Fechar") }
                        Button(onClick = {
                            viewModel.addToCart(med)
                            selectedMedForDetail = null
                        }) {
                            Icon(Icons.Default.AddShoppingCart, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Adicionar")
                        }
                    }
                }
            }
        }
    }

    if (activeReceiptPreviewSale != null) {
        ReceiptPreviewAndPrintDialog(
            sale = activeReceiptPreviewSale!!,
            items = activeReceiptPreviewItems,
            onDismiss = {
                activeReceiptPreviewSale = null
                activeReceiptPreviewItems = emptyList()
            },
            viewModel = viewModel
        )
    }
}
}

@Composable
fun IndicatorCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = title, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
            }
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// -------------------------------------------------------------
// MODULE 2: INTERACTIVE POS TERMINAL (VENDAS POS)
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PosScreen(viewModel: PharmacyViewModel) {
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val isCliente = remember(currentUser) { currentUser?.role == "Cliente" }

    val medicines by viewModel.medicines.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val cart by viewModel.cart.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableStateOf<Int?>(null) }
    
    var buyerName by remember { mutableStateOf(currentUser?.name ?: "") }
    var customerPhone by remember { mutableStateOf("") }
    var deliveryAddress by remember { mutableStateOf("") }
    var clientCheckoutMode by remember { mutableStateOf("pedido") } // "pedido" or "reserva"
    var chosenDeliveryType by remember { mutableStateOf("Levantamento") } // "Levantamento" or "Entrega ao domicílio"
    var selectedPaymentMethod by remember { mutableStateOf("Dinheiro") }
    var discountPercent by remember { mutableStateOf(0) }
    var amountReceivedText by remember { mutableStateOf("") }
    var showCartOverlay by remember { mutableStateOf(false) }

    var selectedPrescriptionUri by remember { mutableStateOf<String?>(null) }
    val prescriptionPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedPrescriptionUri = uri?.toString()
    }

    LaunchedEffect(currentUser) {
        if (currentUser != null && buyerName.isEmpty()) {
            buyerName = currentUser?.name ?: ""
        }
    }

    var checkoutResultDialogText by remember { mutableStateOf<String?>(null) }
    var lastSaleId by remember { mutableStateOf<Int?>(null) }
    var activeReceiptPreviewSale by remember { mutableStateOf<SaleEntity?>(null) }
    var activeReceiptPreviewItems by remember { mutableStateOf<List<SaleItemEntity>>(emptyList()) }
    val scope = rememberCoroutineScope()

    val filteredMedicines = remember(medicines, searchQuery, selectedCategoryId) {
        medicines.filter { med ->
            val matchesSearch = med.name.contains(searchQuery, ignoreCase = true)
            val matchesCategory = selectedCategoryId == null || med.categoryId == selectedCategoryId
            matchesSearch && matchesCategory
        }
    }

    var layoutOverride by remember { mutableStateOf<String?>(null) }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWide = maxWidth >= 600.dp
        val currentLayout = layoutOverride ?: if (isWide) "side" else "bottom"

        @Composable
        fun CatalogSection(modifier: Modifier) {
            Column(
                modifier = modifier,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isCliente) "Medicamentos & Catálogo" else "Vendas POS",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.weight(1f)
                    )
                    
                    // Elegant custom pharmacy cart button with item count badge
                    val itemCount = cart.values.sumOf { it.quantity }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.85f))
                            .clickable { showCartOverlay = !showCartOverlay }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = "Ver Carrinho",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Carrinho",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        if (itemCount > 0) {
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(WarningRed)
                                    .padding(horizontal = 6.dp, vertical = 2.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$itemCount",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                            }
                        }
                    }
                }

                // Search field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Pesquisar medicamento...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                // Category filter pills
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    InputChip(
                        selected = selectedCategoryId == null,
                        onClick = { selectedCategoryId = null },
                        label = { Text("Todos") }
                    )
                    categories.forEach { cat ->
                        InputChip(
                            selected = selectedCategoryId == cat.id,
                            onClick = { selectedCategoryId = cat.id },
                            label = { Text(cat.name) }
                        )
                    }
                }

                // Products list
                if (filteredMedicines.isEmpty()) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("Nenhum medicamento encontrado.", style = MaterialTheme.typography.bodyMedium)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (searchQuery.isEmpty() && selectedCategoryId == null) {
                            // 1. Featured Products section
                            val featuredList = medicines.filter { it.isFeatured }
                            if (featuredList.isNotEmpty()) {
                                item {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Star,
                                                    contentDescription = null,
                                                    tint = HighlightOrange,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                                Text(
                                                    text = "Medicamentos em Destaque",
                                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                                )
                                            }
                                        }

                                        LazyRow(
                                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                                            contentPadding = PaddingValues(horizontal = 4.dp)
                                        ) {
                                            items(featuredList) { med ->
                                                Card(
                                                    modifier = Modifier
                                                        .width(180.dp)
                                                        .clickable { viewModel.addToCart(med); showCartOverlay = true },
                                                    shape = RoundedCornerShape(12.dp),
                                                    colors = CardDefaults.cardColors(
                                                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                                                    ),
                                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                                                ) {
                                                    Column {
                                                        ProductImage(
                                                            imageUri = med.imageUri,
                                                            categoryId = med.categoryId,
                                                            categories = categories,
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .height(110.dp)
                                                                .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                                                        )
                                                        Column(
                                                            modifier = Modifier.padding(10.dp),
                                                            verticalArrangement = Arrangement.spacedBy(6.dp)
                                                        ) {
                                                            Text(
                                                                text = med.name,
                                                            style = MaterialTheme.typography.bodyMedium,
                                                            fontWeight = FontWeight.Bold,
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis
                                                        )

                                                        Row(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            horizontalArrangement = Arrangement.SpaceBetween,
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            Column {
                                                                if (med.promotionPrice != null) {
                                                                    Text(
                                                                        text = "${med.promotionPrice} MZN",
                                                                        style = MaterialTheme.typography.bodySmall,
                                                                        color = WarningRed,
                                                                        fontWeight = FontWeight.Bold
                                                                    )
                                                                    Text(
                                                                        text = "${med.price} MZN",
                                                                        style = MaterialTheme.typography.labelSmall.copy(
                                                                            textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                                                                        ),
                                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                                    )
                                                                } else {
                                                                    Text(
                                                                        text = "${med.price} MZN",
                                                                        style = MaterialTheme.typography.bodySmall,
                                                                        fontWeight = FontWeight.Bold
                                                                    )
                                                                }
                                                            }

                                                            IconButton(
                                                                onClick = { viewModel.addToCart(med); showCartOverlay = true },
                                                                modifier = Modifier.size(32.dp)
                                                            ) {
                                                                Icon(
                                                                    imageVector = Icons.Default.AddShoppingCart,
                                                                    contentDescription = "Adicionar ao carrinho",
                                                                    tint = MaterialTheme.colorScheme.primary,
                                                                    modifier = Modifier.size(18.dp)
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            }

                            // 2. Promotional Products section
                            val promoList = medicines.filter { it.promotionPrice != null }
                            if (promoList.isNotEmpty()) {
                                item {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.LocalOffer,
                                                    contentDescription = null,
                                                    tint = WarningRed,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Text(
                                                    text = "Ofertas & Promoções",
                                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                                )
                                            }
                                            
                                            Card(
                                                colors = CardDefaults.cardColors(containerColor = WarningRed.copy(alpha = 0.15f)),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Text(
                                                    text = "DESCONTO",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = WarningRed,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }

                                        LazyRow(
                                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                                            contentPadding = PaddingValues(horizontal = 4.dp)
                                        ) {
                                            items(promoList) { med ->
                                                Card(
                                                    modifier = Modifier
                                                        .width(180.dp)
                                                        .clickable { viewModel.addToCart(med); showCartOverlay = true },
                                                    shape = RoundedCornerShape(12.dp),
                                                    colors = CardDefaults.cardColors(
                                                        containerColor = WarningRed.copy(alpha = 0.05f)
                                                    ),
                                                    border = BorderStroke(1.dp, WarningRed.copy(alpha = 0.2f))
                                                ) {
                                                    Column {
                                                        ProductImage(
                                                            imageUri = med.imageUri,
                                                            categoryId = med.categoryId,
                                                            categories = categories,
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .height(110.dp)
                                                                .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                                                        )
                                                        Column(
                                                            modifier = Modifier.padding(10.dp),
                                                            verticalArrangement = Arrangement.spacedBy(6.dp)
                                                        ) {
                                                            Text(
                                                                text = med.name,
                                                            style = MaterialTheme.typography.bodyMedium,
                                                            fontWeight = FontWeight.Bold,
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis
                                                        )

                                                        Row(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            horizontalArrangement = Arrangement.SpaceBetween,
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            Column {
                                                                Text(
                                                                    text = "${med.promotionPrice} MZN",
                                                                    style = MaterialTheme.typography.bodySmall,
                                                                    color = WarningRed,
                                                                    fontWeight = FontWeight.Bold
                                                                )
                                                                Text(
                                                                    text = "${med.price} MZN",
                                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                                        textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                                                                    ),
                                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                                )
                                                            }

                                                            IconButton(
                                                                onClick = { viewModel.addToCart(med); showCartOverlay = true },
                                                                modifier = Modifier.size(32.dp)
                                                            ) {
                                                                Icon(
                                                                    imageVector = Icons.Default.AddShoppingCart,
                                                                    contentDescription = "Adicionar",
                                                                    tint = WarningRed,
                                                                    modifier = Modifier.size(18.dp)
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                            // 3. Divider before "Todos os Medicamentos" list
                            item {
                                Text(
                                    text = "Todos os Medicamentos",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                                )
                            }

                        items(filteredMedicines) { med ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.addToCart(med); showCartOverlay = true },
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    ProductImage(
                                        imageUri = med.imageUri,
                                        categoryId = med.categoryId,
                                        categories = categories,
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                    )

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(med.name, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            if (med.promotionPrice != null) {
                                                Text(
                                                    "${med.promotionPrice} MZN",
                                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = WarningRed)
                                                )
                                                Text(
                                                    "${med.price} MZN",
                                                    style = MaterialTheme.typography.bodySmall.copy(textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough),
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            } else {
                                                Text("${med.price} MZN", style = MaterialTheme.typography.bodyMedium)
                                            }

                                            // Stock indicator
                                            Card(
                                                colors = CardDefaults.cardColors(
                                                    containerColor = if (med.stock <= 20) WarningRed.copy(alpha = 0.1f) else SuccessGreen.copy(alpha = 0.1f)
                                                )
                                            ) {
                                                Text(
                                                    text = "Stock: ${med.stock}",
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = if (med.stock <= 20) WarningRed else SuccessGreen
                                                )
                                            }
                                        }
                                    }

                                    IconButton(onClick = { viewModel.addToCart(med); showCartOverlay = true }) {
                                        Icon(
                                            imageVector = Icons.Default.AddShoppingCart,
                                            contentDescription = "Adicionar",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        @Composable
        fun CartSection(modifier: Modifier, onClose: () -> Unit) {
            Column(
                modifier = modifier,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Carrinho de Vendas", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, contentDescription = "Fechar")
                    }
                }

                if (cart.isEmpty()) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(
                                imageVector = Icons.Outlined.ShoppingCart,
                                contentDescription = null,
                                modifier = Modifier.size(36.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text("Carrinho vazio", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(cart.values.toList()) { cartItem ->
                            val med = cartItem.medicine
                            val activePrice = med.promotionPrice ?: med.price
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(med.name, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text("${activePrice * cartItem.quantity} MZN", style = MaterialTheme.typography.labelSmall, color = SuccessGreen)
                                }

                                // Quantity selector
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    IconButton(onClick = { viewModel.decreaseQuantity(med) }, modifier = Modifier.size(24.dp)) {
                                        Icon(Icons.Default.Remove, contentDescription = null, modifier = Modifier.size(16.dp))
                                    }
                                    Text("${cartItem.quantity}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                    IconButton(onClick = { viewModel.addToCart(med) }, modifier = Modifier.size(24.dp)) {
                                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }

                        item {
                            HorizontalDivider()
                        }

                        item {
                            // Quick Discount Selector
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = "Aplicar Desconto:",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    listOf(0, 5, 10, 15, 20).forEach { pct ->
                                        val isSelected = discountPercent == pct
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                                .border(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                                .clickable { discountPercent = pct }
                                                .padding(vertical = 6.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "$pct%",
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        val subtotal = cart.values.sumOf { (it.medicine.promotionPrice ?: it.medicine.price) * it.quantity }
                        val discountAmount = subtotal * (discountPercent / 100.0)
                        val totalToPay = subtotal - discountAmount
                        val ivaAmount = totalToPay * 0.16 // 16% IVA included

                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.15f)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(10.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Subtotal:", style = MaterialTheme.typography.bodySmall)
                                        Text("${String.format("%.2f", subtotal)} MZN", style = MaterialTheme.typography.bodySmall)
                                    }
                                    if (discountPercent > 0) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("Desconto ($discountPercent%):", style = MaterialTheme.typography.bodySmall, color = WarningRed)
                                            Text("-${String.format("%.2f", discountAmount)} MZN", style = MaterialTheme.typography.bodySmall, color = WarningRed)
                                        }
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("IVA (16% incluído):", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text("${String.format("%.2f", ivaAmount)} MZN", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Total a Pagar:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                        Text("${String.format("%.2f", totalToPay)} MZN", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = SuccessGreen)
                                    }
                                }
                            }
                        }

                        if (isCliente) {
                            item {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    // Segmented selection between Online Order (Delivery) and Reservation (Pickup)
                                    Text(
                                        text = "Opção de Aquisição:",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedButton(
                                            onClick = { clientCheckoutMode = "pedido" },
                                            modifier = Modifier.weight(1f),
                                            colors = ButtonDefaults.outlinedButtonColors(
                                                containerColor = if (clientCheckoutMode == "pedido") MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                                                contentColor = if (clientCheckoutMode == "pedido") MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                            ),
                                            border = BorderStroke(1.dp, if (clientCheckoutMode == "pedido") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Icon(Icons.Default.LocalShipping, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Text("Entrega", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }

                                        OutlinedButton(
                                            onClick = { clientCheckoutMode = "reserva" },
                                            modifier = Modifier.weight(1f),
                                            colors = ButtonDefaults.outlinedButtonColors(
                                                containerColor = if (clientCheckoutMode == "reserva") MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                                                contentColor = if (clientCheckoutMode == "reserva") MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                            ),
                                            border = BorderStroke(1.dp, if (clientCheckoutMode == "reserva") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Icon(Icons.Default.Storefront, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Text("Reserva Loja", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }

                                    OutlinedTextField(
                                        value = buyerName,
                                        onValueChange = { buyerName = it },
                                        label = { Text("Nome do Destinatário / Beneficiário", fontSize = 11.sp) },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    OutlinedTextField(
                                        value = customerPhone,
                                        onValueChange = { customerPhone = it },
                                        label = { Text("Contacto Telefónico (Obrigatório)", fontSize = 11.sp) },
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    if (clientCheckoutMode == "pedido") {
                                        Text(
                                            text = "Método de Entrega / Levantamento:",
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                            modifier = Modifier.padding(top = 4.dp)
                                        )
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            OutlinedButton(
                                                onClick = { chosenDeliveryType = "Levantamento" },
                                                modifier = Modifier.weight(1f),
                                                colors = ButtonDefaults.outlinedButtonColors(
                                                    containerColor = if (chosenDeliveryType == "Levantamento") MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                                                    contentColor = if (chosenDeliveryType == "Levantamento") MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                                ),
                                                border = BorderStroke(1.dp, if (chosenDeliveryType == "Levantamento") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline)
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                ) {
                                                    Icon(Icons.Default.Storefront, contentDescription = null, modifier = Modifier.size(14.dp))
                                                    Text("Levantar na Loja", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }

                                            OutlinedButton(
                                                onClick = { chosenDeliveryType = "Entrega ao domicílio" },
                                                modifier = Modifier.weight(1f),
                                                colors = ButtonDefaults.outlinedButtonColors(
                                                    containerColor = if (chosenDeliveryType == "Entrega ao domicílio") MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                                                    contentColor = if (chosenDeliveryType == "Entrega ao domicílio") MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                                ),
                                                border = BorderStroke(1.dp, if (chosenDeliveryType == "Entrega ao domicílio") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline)
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                ) {
                                                    Icon(Icons.Default.LocalShipping, contentDescription = null, modifier = Modifier.size(14.dp))
                                                    Text("Entrega Domicílio", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }

                                        if (chosenDeliveryType == "Entrega ao domicílio") {
                                            OutlinedTextField(
                                                value = deliveryAddress,
                                                onValueChange = { deliveryAddress = it },
                                                label = { Text("Endereço de Entrega Completo", fontSize = 11.sp) },
                                                singleLine = false,
                                                maxLines = 2,
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        }
                                    }

                                    // Attached Prescription section
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(12.dp),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.ReceiptLong,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Text(
                                                    text = "Receita Médica (Opcional)",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }

                                            if (selectedPrescriptionUri == null) {
                                                OutlinedButton(
                                                    onClick = { prescriptionPickerLauncher.launch("image/*") },
                                                    modifier = Modifier.fillMaxWidth(),
                                                    shape = RoundedCornerShape(8.dp)
                                                ) {
                                                    Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(16.dp))
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text("Carregar da Galeria", fontSize = 11.sp)
                                                }
                                            } else {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    SubcomposeAsyncImage(
                                                        model = selectedPrescriptionUri,
                                                        contentDescription = "Pre-visualização da receita",
                                                        modifier = Modifier
                                                            .size(50.dp)
                                                            .clip(RoundedCornerShape(6.dp))
                                                            .background(Color.LightGray),
                                                        contentScale = ContentScale.Crop
                                                    )
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Text("Receita anexada", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                                        Text("Pronto para submeter", style = MaterialTheme.typography.labelSmall, color = SuccessGreen)
                                                    }
                                                    IconButton(
                                                        onClick = { selectedPrescriptionUri = null },
                                                        modifier = Modifier.size(24.dp)
                                                    ) {
                                                        Icon(Icons.Default.Close, contentDescription = "Remover", tint = WarningRed, modifier = Modifier.size(16.dp))
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    // Payment Method Selector
                                    var payExpanded by remember { mutableStateOf(false) }
                                    val clientMethods = listOf("M-Pesa", "e-Mola", "Dinheiro no Acto", "Cartão de Crédito")
                                    Box {
                                        OutlinedButton(
                                            onClick = { payExpanded = true },
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                                Text("Pagam: $selectedPaymentMethod", fontSize = 11.sp)
                                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                            }
                                        }
                                        DropdownMenu(expanded = payExpanded, onDismissRequest = { payExpanded = false }) {
                                            clientMethods.forEach { m ->
                                                DropdownMenuItem(text = { Text(m) }, onClick = { selectedPaymentMethod = m; payExpanded = false })
                                            }
                                        }
                                    }

                                    Button(
                                        onClick = {
                                            viewModel.checkoutCartAsClient(
                                                customerName = buyerName,
                                                phone = customerPhone,
                                                address = if (clientCheckoutMode == "pedido" && chosenDeliveryType == "Entrega ao domicílio") deliveryAddress else "Levantamento na Loja",
                                                paymentMethod = selectedPaymentMethod,
                                                isOnlineOrder = clientCheckoutMode == "pedido",
                                                deliveryType = if (clientCheckoutMode == "pedido") chosenDeliveryType else "Levantamento",
                                                discountPercent = discountPercent,
                                                prescriptionImageUri = selectedPrescriptionUri
                                            ) { success, msg ->
                                                checkoutResultDialogText = msg
                                                if (success) {
                                                    customerPhone = ""
                                                    deliveryAddress = ""
                                                    discountPercent = 0
                                                    amountReceivedText = ""
                                                    showCartOverlay = false
                                                    lastSaleId = null
                                                    selectedPrescriptionUri = null
                                                }
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                    ) {
                                        Icon(if (clientCheckoutMode == "pedido") Icons.Default.CheckCircle else Icons.Default.Bookmark, contentDescription = null)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(if (clientCheckoutMode == "pedido") "Submeter Encomenda" else "Confirmar Reserva")
                                    }
                                }
                            }
                        } else {
                            item {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = buyerName,
                                        onValueChange = { buyerName = it },
                                        label = { Text("Nome do Cliente", fontSize = 11.sp) },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    // Payment Method Selector
                                    var payExpanded by remember { mutableStateOf(false) }
                                    val methodsList = listOf("Dinheiro", "M-Pesa", "e-Mola", "POS", "Transferência")
                                    Box {
                                        OutlinedButton(
                                            onClick = { payExpanded = true },
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                                Text("Pagam: $selectedPaymentMethod", fontSize = 11.sp)
                                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                            }
                                        }
                                        DropdownMenu(expanded = payExpanded, onDismissRequest = { payExpanded = false }) {
                                            methodsList.forEach { m ->
                                                DropdownMenuItem(text = { Text(m) }, onClick = { selectedPaymentMethod = m; payExpanded = false })
                                            }
                                        }
                                    }

                                    // Cash Change (Troco) Calculator
                                    if (selectedPaymentMethod == "Dinheiro") {
                                        OutlinedTextField(
                                            value = amountReceivedText,
                                            onValueChange = { amountReceivedText = it },
                                            label = { Text("Valor Entregue pelo Cliente (MZN)", fontSize = 11.sp) },
                                            singleLine = true,
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            modifier = Modifier.fillMaxWidth()
                                        )

                                        val amountReceived = amountReceivedText.toDoubleOrNull() ?: 0.0
                                        if (amountReceived > 0.0) {
                                            val change = amountReceived - totalToPay
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(
                                                        if (change >= 0) SuccessGreen.copy(alpha = 0.15f) else WarningRed.copy(alpha = 0.15f),
                                                        RoundedCornerShape(8.dp)
                                                    )
                                                    .border(
                                                        1.dp,
                                                        if (change >= 0) SuccessGreen.copy(alpha = 0.3f) else WarningRed.copy(alpha = 0.3f),
                                                        RoundedCornerShape(8.dp)
                                                    )
                                                    .padding(8.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = if (change >= 0) "Troco a Devolver:" else "Valor em Falta:",
                                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                                    color = if (change >= 0) SuccessGreen else WarningRed
                                                )
                                                Text(
                                                    text = "${String.format("%.2f", Math.abs(change))} MZN",
                                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                                    color = if (change >= 0) SuccessGreen else WarningRed
                                                )
                                            }
                                        }
                                    }

                                    val context = LocalContext.current
                                    Button(
                                        onClick = {
                                            viewModel.checkoutCart(buyerName, selectedPaymentMethod, discountPercent) { success, msg, saleId ->
                                                val amountReceived = amountReceivedText.toDoubleOrNull() ?: 0.0
                                                val changeText = if (selectedPaymentMethod == "Dinheiro" && amountReceived >= totalToPay) {
                                                    "\n\nValor Recebido: ${String.format("%.2f", amountReceived)} MZN\nTroco: ${String.format("%.2f", amountReceived - totalToPay)} MZN"
                                                } else if (selectedPaymentMethod == "Dinheiro" && amountReceived > 0.0) {
                                                    "\n\nValor Recebido parcial: ${String.format("%.2f", amountReceived)} MZN (Falta pagar: ${String.format("%.2f", totalToPay - amountReceived)} MZN)"
                                                } else ""

                                                if (success && saleId != null) {
                                                    scope.launch {
                                                        val sale = viewModel.sales.value.find { it.id == saleId }
                                                        if (sale != null) {
                                                            val saleItems = viewModel.getItemsForSale(saleId)
                                                            activeReceiptPreviewSale = sale
                                                            activeReceiptPreviewItems = saleItems
                                                        }
                                                    }
                                                    buyerName = ""
                                                    discountPercent = 0
                                                    amountReceivedText = ""
                                                    showCartOverlay = false
                                                } else {
                                                    checkoutResultDialogText = msg + changeText
                                                    lastSaleId = saleId
                                                }
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                                    ) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Finalizar Venda")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            CatalogSection(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
            )

            // Backdrop Scrim to close cart easily when clicking outside
            if (showCartOverlay) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.35f))
                        .clickable { showCartOverlay = false }
                )
            }

            // Elegant cart slide overlay on top right corner
            AnimatedVisibility(
                visible = showCartOverlay,
                enter = slideInHorizontally(initialOffsetX = { it }) + fadeIn(),
                exit = slideOutHorizontally(targetOffsetX = { it }) + fadeOut(),
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxHeight()
                        .widthIn(max = 420.dp)
                        .fillMaxWidth(0.85f)
                        .padding(8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                ) {
                    CartSection(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        onClose = { showCartOverlay = false }
                    )
                }
            }
        }

    // Checkout receipt dialog
    if (checkoutResultDialogText != null) {
        val context = LocalContext.current
        Dialog(onDismissRequest = { 
            checkoutResultDialogText = null
            lastSaleId = null
        }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ReceiptLong,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = SuccessGreen
                    )
                    Text("Recibo de Venda", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        text = checkoutResultDialogText!!,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    if (lastSaleId != null) {
                        Button(
                            onClick = {
                                scope.launch {
                                    val saleItems = viewModel.getItemsForSale(lastSaleId!!)
                                    val sale = viewModel.sales.value.find { it.id == lastSaleId }
                                    if (sale != null) {
                                        val html = generateReceiptHtml(sale, saleItems)
                                        printReceipt(context, html, "Recibo_${sale.id}")
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.Print, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Imprimir Recibo")
                        }
                    }

                    Button(
                        onClick = { 
                            checkoutResultDialogText = null
                            lastSaleId = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Text("Fechar")
                    }
                }
            }
        }
    }

    if (activeReceiptPreviewSale != null) {
        ReceiptPreviewAndPrintDialog(
            sale = activeReceiptPreviewSale!!,
            items = activeReceiptPreviewItems,
            onDismiss = {
                activeReceiptPreviewSale = null
                activeReceiptPreviewItems = emptyList()
            },
            viewModel = viewModel
        )
    }
}
}

// Helper to provide simple Icon size Modifier
// (Removed to use standard Modifier.size)

// -------------------------------------------------------------
// MODULE 3: MEDICINES MANAGEMENT (MEDICAMENTOS)
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicinesScreen(viewModel: PharmacyViewModel) {
    val medicines by viewModel.medicines.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }
    var medName by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableStateOf<Int?>(null) }
    var medPrice by remember { mutableStateOf("") }
    var medStock by remember { mutableStateOf("") }
    var medExpiryDays by remember { mutableStateOf("180") } // default 6 months
    var medFeatured by remember { mutableStateOf(false) }
    var medPromoPrice by remember { mutableStateOf("") }
    var medImageUri by remember { mutableStateOf("") }

    var showEditDialog by remember { mutableStateOf(false) }
    var selectedMedForEdit by remember { mutableStateOf<MedicineEntity?>(null) }
    var editMedName by remember { mutableStateOf("") }
    var editSelectedCategoryId by remember { mutableStateOf<Int?>(null) }
    var editMedPrice by remember { mutableStateOf("") }
    var editMedStock by remember { mutableStateOf("") }
    var editMedFeatured by remember { mutableStateOf(false) }
    var editMedPromoPrice by remember { mutableStateOf("") }
    var editMedImageUri by remember { mutableStateOf("") }

    val addMedicineImagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            medImageUri = uri.toString()
        }
    }

    val editMedicineImagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            editMedImageUri = uri.toString()
        }
    }

    var categoryDialogExpanded by remember { mutableStateOf(false) }
    var showCategoryAddDialog by remember { mutableStateOf(false) }
    var catName by remember { mutableStateOf("") }
    var catDesc by remember { mutableStateOf("") }

    Scaffold(
        floatingActionButton = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SmallFloatingActionButton(
                    onClick = { showCategoryAddDialog = true },
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Icon(Icons.Default.Category, contentDescription = "Nova Categoria")
                }
                FloatingActionButton(
                    onClick = {
                        if (categories.isNotEmpty()) {
                            selectedCategoryId = categories.first().id
                        }
                        showAddDialog = true
                    },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Adicionar Medicamento")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Gestão de Catálogo de Medicamentos", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))

            if (medicines.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("Nenhum medicamento registado no sistema ainda.")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(medicines) { med ->
                        val catName = categories.find { it.id == med.categoryId }?.name ?: "Sem Categoria"
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                ProductImage(
                                    imageUri = med.imageUri,
                                    categoryId = med.categoryId,
                                    categories = categories,
                                    modifier = Modifier
                                        .size(60.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                )

                                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(med.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        SuggestionChip(onClick = {}, label = { Text(catName, fontSize = 10.sp) })
                                        if (med.isFeatured) {
                                            SuggestionChip(
                                                onClick = {},
                                                label = { Text("Destaque", fontSize = 10.sp, color = HighlightOrange) }
                                            )
                                        }
                                    }
                                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                        Text("Preço: ${med.price} MZN", style = MaterialTheme.typography.bodySmall)
                                        if (med.promotionPrice != null) {
                                            Text("Promo: ${med.promotionPrice} MZN", style = MaterialTheme.typography.bodySmall, color = WarningRed, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    Text(
                                        text = "Validade: ${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(med.expiryDate))}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                
                                Row {
                                    IconButton(onClick = {
                                        selectedMedForEdit = med
                                        editMedName = med.name
                                        editSelectedCategoryId = med.categoryId
                                        editMedPrice = med.price.toString()
                                        editMedStock = med.stock.toString()
                                        editMedFeatured = med.isFeatured
                                        editMedPromoPrice = med.promotionPrice?.toString() ?: ""
                                        editMedImageUri = med.imageUri ?: ""
                                        showEditDialog = true
                                    }) {
                                        Icon(Icons.Default.Edit, contentDescription = "Editar", tint = MaterialTheme.colorScheme.primary)
                                    }
                                    
                                    IconButton(onClick = { viewModel.deleteMedicine(med) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Apagar", tint = WarningRed)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add category dialog
    if (showCategoryAddDialog) {
        Dialog(onDismissRequest = { showCategoryAddDialog = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Registar Nova Categoria", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = catName,
                        onValueChange = { catName = it },
                        label = { Text("Nome da Categoria") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = catDesc,
                        onValueChange = { catDesc = it },
                        label = { Text("Descrição") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { showCategoryAddDialog = false }) { Text("Cancelar") }
                        Button(onClick = {
                            viewModel.addCategory(catName, catDesc) { success ->
                                if (success) {
                                    showCategoryAddDialog = false
                                    catName = ""
                                    catDesc = ""
                                }
                            }
                        }) { Text("Salvar") }
                    }
                }
            }
        }
    }

    // Add medicine dialog
    if (showAddDialog) {
        Dialog(onDismissRequest = { showAddDialog = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Registar Novo Medicamento", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    
                    OutlinedTextField(
                        value = medName,
                        onValueChange = { medName = it },
                        label = { Text("Nome do Medicamento") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Category Selector Dropdown
                    var catExpanded by remember { mutableStateOf(false) }
                    val currentCatName = categories.find { it.id == selectedCategoryId }?.name ?: "Selecione Categoria"
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            OutlinedButton(onClick = { catExpanded = true }, modifier = Modifier.fillMaxWidth()) {
                                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                    Text("Cat: $currentCatName", maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                }
                            }
                            DropdownMenu(expanded = catExpanded, onDismissRequest = { catExpanded = false }) {
                                categories.forEach { cat ->
                                    DropdownMenuItem(
                                        text = { Text(cat.name) },
                                        onClick = {
                                            selectedCategoryId = cat.id
                                            catExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                        IconButton(
                            onClick = { showCategoryAddDialog = true },
                            colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Nova Categoria", tint = MaterialTheme.colorScheme.onSecondaryContainer)
                        }
                    }

                    OutlinedTextField(
                        value = medPrice,
                        onValueChange = { medPrice = it },
                        label = { Text("Preço Base (MZN)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = medPromoPrice,
                        onValueChange = { medPromoPrice = it },
                        label = { Text("Preço Promoção (MZN) - Opcional") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = medStock,
                        onValueChange = { medStock = it },
                        label = { Text("Stock Inicial") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = medExpiryDays,
                        onValueChange = { medExpiryDays = it },
                        label = { Text("Dias até expiração (Ex: 180)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = medImageUri,
                        onValueChange = { medImageUri = it },
                        label = { Text("URL ou URI da Imagem") },
                        placeholder = { Text("https://exemplo.com/imagem.jpg ou galeria") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedButton(
                        onClick = { addMedicineImagePickerLauncher.launch("image/*") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Carregar da Galeria do Dispositivo", fontSize = 12.sp)
                    }

                    if (medImageUri.isNotBlank()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            SubcomposeAsyncImage(
                                model = medImageUri,
                                contentDescription = "Pre-visualização",
                                modifier = Modifier
                                    .size(45.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color.LightGray),
                                contentScale = ContentScale.Crop
                            )
                            Text(
                                text = if (medImageUri.startsWith("content://")) "Imagem selecionada da Galeria" else "URL de imagem definida",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Checkbox(checked = medFeatured, onCheckedChange = { medFeatured = it })
                        Text("Medicamento em Destaque")
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showAddDialog = false }) { Text("Cancelar") }
                        Button(onClick = {
                            val price = medPrice.toDoubleOrNull() ?: 0.0
                            val stock = medStock.toIntOrNull() ?: 0
                            val days = medExpiryDays.toIntOrNull() ?: 180
                            val promo = medPromoPrice.toDoubleOrNull()
                            val catId = selectedCategoryId ?: 0

                            viewModel.addMedicine(medName, catId, price, stock, days, medFeatured, promo, medImageUri) { success ->
                                if (success) {
                                    showAddDialog = false
                                    medName = ""
                                    medPrice = ""
                                    medStock = ""
                                    medPromoPrice = ""
                                    medFeatured = false
                                    medImageUri = ""
                                }
                            }
                        }) { Text("Salvar") }
                    }
                }
            }
        }
    }

    // Edit medicine dialog
    if (showEditDialog && selectedMedForEdit != null) {
        val originalMed = selectedMedForEdit!!
        Dialog(onDismissRequest = { showEditDialog = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Editar Medicamento", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    
                    OutlinedTextField(
                        value = editMedName,
                        onValueChange = { editMedName = it },
                        label = { Text("Nome do Medicamento") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Category Selector Dropdown
                    var catExpanded by remember { mutableStateOf(false) }
                    val currentCatName = categories.find { it.id == editSelectedCategoryId }?.name ?: "Selecione Categoria"
                    Box {
                        OutlinedButton(onClick = { catExpanded = true }, modifier = Modifier.fillMaxWidth()) {
                            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                Text("Cat: $currentCatName")
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                        }
                        DropdownMenu(expanded = catExpanded, onDismissRequest = { catExpanded = false }) {
                            categories.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat.name) },
                                    onClick = {
                                        editSelectedCategoryId = cat.id
                                        catExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = editMedPrice,
                        onValueChange = { editMedPrice = it },
                        label = { Text("Preço Base (MZN)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = editMedPromoPrice,
                        onValueChange = { editMedPromoPrice = it },
                        label = { Text("Preço Promoção (MZN) - Opcional") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = editMedStock,
                        onValueChange = { editMedStock = it },
                        label = { Text("Stock") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = editMedImageUri,
                        onValueChange = { editMedImageUri = it },
                        label = { Text("URL ou URI da Imagem") },
                        placeholder = { Text("https://exemplo.com/imagem.jpg ou galeria") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedButton(
                        onClick = { editMedicineImagePickerLauncher.launch("image/*") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Carregar da Galeria do Dispositivo", fontSize = 12.sp)
                    }

                    if (editMedImageUri.isNotBlank()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            SubcomposeAsyncImage(
                                model = editMedImageUri,
                                contentDescription = "Pre-visualização",
                                modifier = Modifier
                                    .size(45.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color.LightGray),
                                contentScale = ContentScale.Crop
                            )
                            Text(
                                text = if (editMedImageUri.startsWith("content://")) "Imagem selecionada da Galeria" else "URL de imagem definida",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Checkbox(checked = editMedFeatured, onCheckedChange = { editMedFeatured = it })
                        Text("Medicamento em Destaque")
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showEditDialog = false }) { Text("Cancelar") }
                        Button(onClick = {
                            val price = editMedPrice.toDoubleOrNull() ?: originalMed.price
                            val stock = editMedStock.toIntOrNull() ?: originalMed.stock
                            val promo = if (editMedPromoPrice.isBlank()) null else editMedPromoPrice.toDoubleOrNull()
                            val catId = editSelectedCategoryId ?: originalMed.categoryId

                            val updatedMed = originalMed.copy(
                                name = editMedName,
                                categoryId = catId,
                                price = price,
                                stock = stock,
                                isFeatured = editMedFeatured,
                                promotionPrice = promo,
                                imageUri = if (editMedImageUri.isBlank()) null else editMedImageUri
                            )

                            viewModel.updateMedicine(updatedMed) { success ->
                                if (success) {
                                    showEditDialog = false
                                    selectedMedForEdit = null
                                }
                            }
                        }) { Text("Salvar") }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// MODULE 4: STOCK CONTROL & VALIDATION (CONTROLO STOCK & VALIDADE)
// -------------------------------------------------------------
@Composable
fun StockScreen(viewModel: PharmacyViewModel) {
    val medicines by viewModel.medicines.collectAsStateWithLifecycle()
    val movements by viewModel.stockMovements.collectAsStateWithLifecycle()

    var showAdjustDialog by remember { mutableStateOf(false) }
    var selectedMedForAdjust by remember { mutableStateOf<MedicineEntity?>(null) }
    var adjustType by remember { mutableStateOf("Entrada") } // Entrada, Saída
    var adjustQty by remember { mutableStateOf("") }
    var adjustReason by remember { mutableStateOf("Abastecimento") }

    val now = System.currentTimeMillis()
    val thirtyDays = 30L * 24 * 60 * 60 * 1000

    val lowStockMedicines = remember(medicines) {
        medicines.filter { it.stock <= 20 }
    }

    val expiringMedicines = remember(medicines, now) {
        medicines.filter { (it.expiryDate - now) <= thirtyDays }
    }

    var selectedTabState by remember { mutableStateOf(0) } // 0: Alertas, 1: Histórico Movimentos

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Painel de Controlo de Stock e Validade", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))

        TabRow(selectedTabIndex = selectedTabState) {
            Tab(selected = selectedTabState == 0, onClick = { selectedTabState = 0 }, text = { Text("Alertas Críticos") })
            Tab(selected = selectedTabState == 1, onClick = { selectedTabState = 1 }, text = { Text("Histórico Mov.") })
        }

        if (selectedTabState == 0) {
            // Alerts screen
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Section 1: Low Stock
                item {
                    Text(
                        "Ruptura ou Stock Baixo (<=20 unidades)",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = HighlightOrange
                    )
                }

                if (lowStockMedicines.isEmpty()) {
                    item { Text("Nenhum item com stock baixo no momento.", style = MaterialTheme.typography.bodySmall) }
                } else {
                    items(lowStockMedicines) { med ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = HighlightOrange.copy(alpha = 0.08f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(med.name, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                                    Text("Unidades Restantes: ${med.stock}", style = MaterialTheme.typography.bodySmall, color = WarningRed)
                                }
                                Button(
                                    onClick = {
                                        selectedMedForAdjust = med
                                        adjustType = "Entrada"
                                        adjustReason = "Abastecimento"
                                        showAdjustDialog = true
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = HighlightOrange)
                                ) {
                                    Text("Reabastecer")
                                }
                            }
                        }
                    }
                }

                // Section 2: Expiration Warnings
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Alertas de Validade (Expira em menos de 30 dias)",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = WarningRed
                    )
                }

                if (expiringMedicines.isEmpty()) {
                    item { Text("Nenhum item prestes a expirar.", style = MaterialTheme.typography.bodySmall) }
                } else {
                    items(expiringMedicines) { med ->
                        val diffDays = ((med.expiryDate - now) / (24 * 60 * 60 * 1000)).coerceAtLeast(0)
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = WarningRed.copy(alpha = 0.08f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(med.name, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                                    Text(
                                        text = if (diffDays == 0L) "EXPIRADO!" else "Expira em $diffDays dias!",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = WarningRed,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Button(
                                    onClick = {
                                        selectedMedForAdjust = med
                                        adjustType = "Saída"
                                        adjustReason = "Avaria"
                                        showAdjustDialog = true
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = WarningRed)
                                ) {
                                    Text("Retirar Stock")
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Stock Movements History list
            if (movements.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("Nenhum movimento de stock registado.")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(movements) { mov ->
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(mov.medicineName, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                                    Text("Motivo: ${mov.reason}", style = MaterialTheme.typography.bodySmall)
                                    Text(
                                        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(mov.date)),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                
                                val qtyColor = if (mov.type == "Entrada") SuccessGreen else WarningRed
                                val prefix = if (mov.type == "Entrada") "+" else "-"
                                Text(
                                    "$prefix${mov.quantity}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = qtyColor
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Adjust Stock Dialog
    if (showAdjustDialog && selectedMedForAdjust != null) {
        val med = selectedMedForAdjust!!
        Dialog(onDismissRequest = { showAdjustDialog = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Ajuste de Stock: ${med.name}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                    // Type Selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { adjustType = "Entrada" },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = if (adjustType == "Entrada") SuccessGreen else MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Text("Entrada", color = if (adjustType == "Entrada") Color.White else MaterialTheme.colorScheme.onSurface)
                        }
                        Button(
                            onClick = { adjustType = "Saída" },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = if (adjustType == "Saída") WarningRed else MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Text("Saída", color = if (adjustType == "Saída") Color.White else MaterialTheme.colorScheme.onSurface)
                        }
                    }

                    OutlinedTextField(
                        value = adjustQty,
                        onValueChange = { adjustQty = it },
                        label = { Text("Quantidade") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = adjustReason,
                        onValueChange = { adjustReason = it },
                        label = { Text("Motivo do Ajuste") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showAdjustDialog = false }) { Text("Cancelar") }
                        Button(onClick = {
                            val qty = adjustQty.toIntOrNull() ?: 0
                            if (qty > 0) {
                                viewModel.addStockMovement(med.id, med.name, adjustType, qty, adjustReason) { success ->
                                    if (success) {
                                        showAdjustDialog = false
                                        adjustQty = ""
                                    }
                                }
                            }
                        }) { Text("Aplicar") }
                    }
                }
            }
        }
    }
}
// MODULE 5: ORDERS & RESERVATIONS (PEDIDOS E RESERVAS)
// -------------------------------------------------------------
@Composable
fun OrdersScreen(viewModel: PharmacyViewModel) {
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val userVal = currentUser
    val isCliente = remember(userVal) { userVal?.role == "Cliente" }
    val canApprove = remember(userVal) {
        userVal != null && (userVal.role == "Administrador" || userVal.role == "Director Geral" || userVal.role == "Farmaceutico")
    }

    val orders by viewModel.orders.collectAsStateWithLifecycle()
    val reservations by viewModel.reservations.collectAsStateWithLifecycle()
    val users by viewModel.users.collectAsStateWithLifecycle()

    val deliveryDrivers = remember(users) {
        users.filter { it.role == "Entregador" }
    }

    val filteredOrders = remember(orders, userVal, isCliente) {
        if (isCliente && userVal != null) {
            orders.filter { it.customerName.equals(userVal.name, ignoreCase = true) }
        } else {
            orders
        }
    }

    val filteredReservations = remember(reservations, userVal, isCliente) {
        if (isCliente && userVal != null) {
            reservations.filter { it.customerName.equals(userVal.name, ignoreCase = true) }
        } else {
            reservations
        }
    }

    var selectedTabState by remember { mutableStateOf(0) } // 0: Pedidos Online, 1: Reservas

    // Dialog state for Rejections
    var rejectionOrderIdForDialog by remember { mutableStateOf<Int?>(null) }
    var rejectionResIdForDialog by remember { mutableStateOf<Int?>(null) }
    var rejectionReasonText by remember { mutableStateOf("") }

    if (rejectionOrderIdForDialog != null || rejectionResIdForDialog != null) {
        Dialog(onDismissRequest = {
            rejectionOrderIdForDialog = null
            rejectionResIdForDialog = null
            rejectionReasonText = ""
        }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Justificação de Rejeição",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = WarningRed
                    )
                    Text(
                        text = "Por favor, indique o motivo ou justificação para a rejeição deste pedido:",
                        style = MaterialTheme.typography.bodySmall
                    )
                    OutlinedTextField(
                        value = rejectionReasonText,
                        onValueChange = { rejectionReasonText = it },
                        label = { Text("Justificação (Obrigatório)") },
                        singleLine = false,
                        maxLines = 4,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = {
                            rejectionOrderIdForDialog = null
                            rejectionResIdForDialog = null
                            rejectionReasonText = ""
                        }) {
                            Text("Cancelar")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (rejectionOrderIdForDialog != null) {
                                    viewModel.rejectOrder(rejectionOrderIdForDialog!!, rejectionReasonText)
                                } else if (rejectionResIdForDialog != null) {
                                    viewModel.rejectReservation(rejectionResIdForDialog!!, rejectionReasonText)
                                }
                                rejectionOrderIdForDialog = null
                                rejectionResIdForDialog = null
                                rejectionReasonText = ""
                            },
                            enabled = rejectionReasonText.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(containerColor = WarningRed)
                        ) {
                            Text("Rejeitar Pedido", color = Color.White)
                        }
                    }
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isCliente) "Meus Pedidos e Reservas de Medicamentos" else "Pedidos Online e Reservas de Clientes",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.weight(1f)
            )
            
            // Testing / Simulation tool
            if (canApprove) {
                Button(
                    onClick = { viewModel.simulate48HoursPassed() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Icon(Icons.Default.Timelapse, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.White)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Simular +48h", fontSize = 10.sp, color = Color.White)
                }
            }
        }

        TabRow(selectedTabIndex = selectedTabState) {
            Tab(selected = selectedTabState == 0, onClick = { selectedTabState = 0 }, text = { Text(if (isCliente) "Meus Pedidos Online" else "Pedidos Online") })
            Tab(selected = selectedTabState == 1, onClick = { selectedTabState = 1 }, text = { Text(if (isCliente) "Minhas Reservas" else "Reservas de Loja") })
        }

        if (selectedTabState == 0) {
            // Pedidos Online List
            if (filteredOrders.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text(if (isCliente) "Ainda não efetuou nenhuma encomenda online." else "Nenhum pedido online recebido.")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredOrders) { order ->
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("Pedido #${order.id}", fontWeight = FontWeight.Bold)
                                        // Delivery type tag
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            modifier = Modifier.padding(top = 2.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (order.deliveryType == "Entrega ao domicílio") Icons.Default.LocalShipping else Icons.Default.Storefront,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Text(
                                                text = order.deliveryType ?: "Levantamento",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.primary,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    // Status Badge
                                    val color = when (order.status) {
                                        "Pendente" -> HighlightOrange
                                        "Aprovado" -> SuccessGreen
                                        "Em Preparação" -> Color.Blue
                                        "Pronto para Entrega" -> SuccessGreen
                                        "A Caminho" -> Color.Cyan
                                        "Entregue" -> MaterialTheme.colorScheme.primary
                                        else -> WarningRed
                                    }
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.15f))
                                    ) {
                                        Text(
                                            text = order.status,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = color,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                Text(if (isCliente) "Destinatário: ${order.customerName} (${order.phone})" else "Cliente: ${order.customerName} (${order.phone})", style = MaterialTheme.typography.bodySmall)
                                Text("Endereço: ${order.address}", style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                
                                if (!order.prescriptionImageUri.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    var showPrescriptionDialog by remember { mutableStateOf(false) }
                                    Row(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                                            .clickable { showPrescriptionDialog = true }
                                            .padding(horizontal = 8.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ReceiptLong,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = "Ver Receita Médica Anexada",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }

                                    if (showPrescriptionDialog) {
                                        Dialog(onDismissRequest = { showPrescriptionDialog = false }) {
                                            Card(
                                                shape = RoundedCornerShape(16.dp),
                                                modifier = Modifier.fillMaxWidth().padding(16.dp)
                                            ) {
                                                Column(
                                                    modifier = Modifier.padding(16.dp),
                                                    verticalArrangement = Arrangement.spacedBy(12.dp),
                                                    horizontalAlignment = Alignment.CenterHorizontally
                                                ) {
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Text("Receita Médica - Pedido #${order.id}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                                        IconButton(onClick = { showPrescriptionDialog = false }) {
                                                            Icon(Icons.Default.Close, contentDescription = "Fechar")
                                                        }
                                                    }
                                                    SubcomposeAsyncImage(
                                                        model = order.prescriptionImageUri,
                                                        contentDescription = "Receita Médica",
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .heightIn(max = 400.dp)
                                                            .clip(RoundedCornerShape(8.dp)),
                                                        contentScale = ContentScale.Fit,
                                                        loading = {
                                                            Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                                                                CircularProgressIndicator()
                                                            }
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Total: ${order.totalAmount} MZN", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                                    Text("Pagam: ${order.paymentMethod}", style = MaterialTheme.typography.labelSmall)
                                }

                                // 48h expiration check display
                                if (order.status == "Aprovado" && order.approvalDate != null) {
                                    val timeLeftMillis = (order.approvalDate ?: 0L) + (48L * 60 * 60 * 1000) - System.currentTimeMillis()
                                    if (timeLeftMillis > 0) {
                                        val hours = timeLeftMillis / (1000 * 60 * 60)
                                        val minutes = (timeLeftMillis % (1000 * 60 * 60)) / (1000 * 60)
                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = HighlightOrange.copy(alpha = 0.1f)),
                                            border = BorderStroke(1.dp, HighlightOrange.copy(alpha = 0.3f)),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(8.dp),
                                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(Icons.Default.Timelapse, contentDescription = null, tint = HighlightOrange, modifier = Modifier.size(14.dp))
                                                Text(
                                                    text = "Prazo p/ Levantamento/Entrega: $hours h $minutes min restantes (Expira após 48h)",
                                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                    color = HighlightOrange
                                                )
                                            }
                                        }
                                    }
                                }

                                // Rejection Reason Card
                                if ((order.status == "Rejeitado" || order.status == "Cancelado") && !order.rejectionReason.isNullOrBlank()) {
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = WarningRed.copy(alpha = 0.1f)),
                                        border = BorderStroke(1.dp, WarningRed.copy(alpha = 0.3f)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(8.dp)) {
                                            Text(
                                                text = "Motivo da Rejeição / Cancelamento:",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = WarningRed
                                            )
                                            Text(
                                                text = order.rejectionReason ?: "",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }

                                // Acompanhamento timeline/stepper for Client tracking
                                HorizontalDivider()
                                OrderStatusTimeline(order.status)

                                // Action Buttons (staff authorization check)
                                if (canApprove) {
                                    HorizontalDivider()
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        if (order.status == "Pendente") {
                                            OutlinedButton(
                                                onClick = { rejectionOrderIdForDialog = order.id },
                                                colors = ButtonDefaults.outlinedButtonColors(contentColor = WarningRed),
                                                border = BorderStroke(1.dp, WarningRed)
                                            ) {
                                                Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Rejeitar")
                                            }

                                            Button(
                                                onClick = { viewModel.approveOrder(order.id) },
                                                colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                                            ) {
                                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Aprovar")
                                            }
                                        } else if (order.status == "Aprovado") {
                                            Button(
                                                onClick = { viewModel.updateOrderStatus(order.id, "Em Preparação") },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color.Blue)
                                            ) {
                                                Text("Preparar")
                                            }
                                        } else if (order.status == "Em Preparação") {
                                            // Dropdown to assign Driver
                                            var driverExpanded by remember { mutableStateOf(false) }
                                            Box {
                                                Button(onClick = { driverExpanded = true }) {
                                                    Text("Despachar Entregador")
                                                }
                                                DropdownMenu(expanded = driverExpanded, onDismissRequest = { driverExpanded = false }) {
                                                    deliveryDrivers.forEach { driver ->
                                                        DropdownMenuItem(
                                                            text = { Text(driver.name) },
                                                            onClick = {
                                                                viewModel.assignOrderDelivery(order.id, driver.id, driver.name)
                                                                driverExpanded = false
                                                            }
                                                        )
                                                    }
                                                    if (deliveryDrivers.isEmpty()) {
                                                        DropdownMenuItem(text = { Text("Nenhum entregador registado") }, onClick = {})
                                                    }
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    // Non-approvers / Client help text
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.Start),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Info,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = when (order.status) {
                                                "Pendente" -> "A sua encomenda foi recebida e aguarda validação da farmácia."
                                                "Aprovado" -> "A sua encomenda foi aprovada! Tem 48h para levantamento ou aguarda envio."
                                                "Em Preparação" -> "Os seus medicamentos estão a ser embalados com segurança."
                                                "Pronto para Entrega" -> "A sua encomenda já está embalada e aguarda transportador."
                                                "A Caminho" -> "O estafeta está a caminho do seu endereço."
                                                "Entregue" -> "Encomenda entregue com sucesso! Obrigado pela preferência."
                                                "Rejeitado" -> "O seu pedido foi rejeitado. Consulte a justificação acima."
                                                "Cancelado" -> "A encomenda foi cancelada. Prazo limite de 48h excedido ou cancelamento manual."
                                                else -> "Estado atualizado."
                                            },
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Reservas List
            if (filteredReservations.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text(if (isCliente) "Ainda não efetuou nenhuma reserva de loja." else "Nenhuma reserva de medicamentos pendente.")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredReservations) { res ->
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(res.medicineName, fontWeight = FontWeight.Bold)
                                    val c = when (res.status) {
                                        "Pendente" -> HighlightOrange
                                        "Aprovado" -> SuccessGreen
                                        "Levantado" -> SuccessGreen
                                        else -> WarningRed
                                    }
                                    Card(colors = CardDefaults.cardColors(containerColor = c.copy(alpha = 0.15f))) {
                                        Text(
                                            res.status,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = c,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                Text(if (isCliente) "Beneficiário: ${res.customerName} (${res.phone})" else "Cliente: ${res.customerName} (${res.phone})", style = MaterialTheme.typography.bodySmall)
                                Text("Quantidade Reservada: ${res.quantity} unidades", style = MaterialTheme.typography.bodySmall)
                                
                                // Reservation timeline/help text expiration countdown
                                if (res.status == "Aprovado") {
                                    val timeLeftMillis = res.expiryDate - System.currentTimeMillis()
                                    if (timeLeftMillis > 0) {
                                        val hours = timeLeftMillis / (1000 * 60 * 60)
                                        val minutes = (timeLeftMillis % (1000 * 60 * 60)) / (1000 * 60)
                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = HighlightOrange.copy(alpha = 0.1f)),
                                            border = BorderStroke(1.dp, HighlightOrange.copy(alpha = 0.3f)),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(8.dp),
                                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(Icons.Default.Timelapse, contentDescription = null, tint = HighlightOrange, modifier = Modifier.size(14.dp))
                                                Text(
                                                    text = "Prazo de Levantamento: $hours h $minutes min restantes (Limite 48h)",
                                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                    color = HighlightOrange
                                                )
                                            }
                                        }
                                    }
                                } else {
                                    Text(
                                        "Validade Reserva: ${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(res.expiryDate))}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                // Rejection Reason Card
                                if (res.status == "Rejeitado" && !res.rejectionReason.isNullOrBlank()) {
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = WarningRed.copy(alpha = 0.1f)),
                                        border = BorderStroke(1.dp, WarningRed.copy(alpha = 0.3f)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(8.dp)) {
                                            Text(
                                                text = "Motivo de Rejeição:",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = WarningRed
                                            )
                                            Text(
                                                text = res.rejectionReason ?: "",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }

                                if (isCliente) {
                                    HorizontalDivider()
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.Start),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Info,
                                            contentDescription = null,
                                            tint = if (res.status == "Aprovado") SuccessGreen else HighlightOrange,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = when (res.status) {
                                                "Pendente" -> "A sua reserva aguarda aprovação da farmácia."
                                                "Aprovado" -> "Reserva aprovada! Dirija-se à farmácia física em até 48h para pagar e levantar."
                                                "Levantado" -> "Reserva finalizada e paga com sucesso!"
                                                "Rejeitado" -> "A sua reserva foi rejeitada. Consulte o motivo acima."
                                                "Expirado" -> "A reserva expirou por ter ultrapassado as 48 horas limite."
                                                else -> "Estado atualizado."
                                            },
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (res.status == "Aprovado") SuccessGreen else HighlightOrange
                                        )
                                    }
                                } else {
                                    HorizontalDivider()
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
                                    ) {
                                        if (res.status == "Pendente") {
                                            if (canApprove) {
                                                OutlinedButton(
                                                    onClick = { rejectionResIdForDialog = res.id },
                                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = WarningRed),
                                                    border = BorderStroke(1.dp, WarningRed)
                                                ) {
                                                    Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("Rejeitar")
                                                }

                                                Button(
                                                    onClick = { viewModel.approveReservation(res.id) },
                                                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                                                ) {
                                                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("Aprovar")
                                                }
                                            }
                                        } else if (res.status == "Aprovado") {
                                            OutlinedButton(onClick = { viewModel.cancelReservation(res.id) }) {
                                                Text("Expirar/Cancelar", color = WarningRed)
                                            }
                                            Button(
                                                onClick = { viewModel.pickUpReservation(res.id) { _ -> } },
                                                colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                                            ) {
                                                Text("Levantar (Pagar)")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OrderStatusTimeline(status: String) {
    val steps = listOf("Pendente", "Aprovado", "Em Preparação", "Pronto", "Entregue")
    val currentIndex = when (status) {
        "Pendente" -> 0
        "Aprovado" -> 1
        "Em Preparação" -> 2
        "Pronto para Entrega", "Pronto" -> 3
        "Entregue" -> 4
        else -> -1 // Rejeitado / Cancelado
    }

    if (currentIndex == -1) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Cancel, contentDescription = null, tint = WarningRed, modifier = Modifier.size(16.dp))
            Text("Acompanhamento: Pedido Rejeitado / Cancelado", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = WarningRed)
        }
    } else {
        Column(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
            Text("Acompanhamento do Pedido:", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                steps.forEachIndexed { index, step ->
                    val isActive = index <= currentIndex
                    val isCurrent = index == currentIndex
                    val color = if (isCurrent) MaterialTheme.colorScheme.primary else if (isActive) SuccessGreen else Color.LightGray
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(color),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isActive) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(10.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = step,
                            fontSize = 7.5.sp,
                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                            color = if (isCurrent) MaterialTheme.colorScheme.primary else Color.Gray,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    if (index < steps.size - 1) {
                        Box(
                            modifier = Modifier
                                .height(2.dp)
                                .weight(0.5f)
                                .background(if (index < currentIndex) SuccessGreen else Color.LightGray)
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// MODULE 6: DELIVERIES (ENTREGAS)
// -------------------------------------------------------------
@Composable
fun DeliveriesScreen(viewModel: PharmacyViewModel, user: UserEntity) {
    val deliveries by viewModel.deliveries.collectAsStateWithLifecycle()

    val filteredDeliveries = remember(deliveries, user) {
        if (user.role == "Administrador") {
            deliveries
        } else {
            deliveries.filter { it.deliveryPersonId == user.id }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Fila de Entregas ao Domicílio", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))

        if (filteredDeliveries.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("Nenhuma entrega pendente para si no momento.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredDeliveries) { del ->
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Entrega Pedido #${del.orderId}", fontWeight = FontWeight.Bold)
                                val color = when (del.status) {
                                    "Pendente" -> HighlightOrange
                                    "A Caminho" -> Color.Cyan
                                    else -> SuccessGreen
                                }
                                Card(colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.15f))) {
                                    Text(
                                        del.status,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = color,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Text("Destinatário: ${del.recipientPhone}", style = MaterialTheme.typography.bodySmall)
                            Text("Est. Entrega: ${del.estimatedTime}", style = MaterialTheme.typography.bodySmall)
                            Text("Entregador: ${del.deliveryPersonName}", style = MaterialTheme.typography.bodySmall)

                            if (del.status != "Entregue") {
                                HorizontalDivider()
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    if (del.status == "Pendente") {
                                        Button(
                                            onClick = { viewModel.updateDeliveryStatus(del.id, "A Caminho") },
                                            colors = ButtonDefaults.buttonColors(containerColor = HighlightOrange)
                                        ) {
                                            Text("Iniciar Rota")
                                        }
                                    } else if (del.status == "A Caminho") {
                                        Button(
                                            onClick = { viewModel.updateDeliveryStatus(del.id, "Entregue") },
                                            colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                                        ) {
                                            Text("Confirmar Entrega")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// MODULE 7: PARTNERS / SUPPLIERS (FORNECEDORES)
// -------------------------------------------------------------
// -------------------------------------------------------------
// MODULE 7: ADMINISTRATION - USERS & SUPPLIERS (ADMINISTRAÇÃO)
// -------------------------------------------------------------
@Composable
fun SuppliersScreen(viewModel: PharmacyViewModel) {
    val suppliers by viewModel.suppliers.collectAsStateWithLifecycle()
    val users by viewModel.users.collectAsStateWithLifecycle()

    var selectedTabState by remember { mutableStateOf(0) } // 0: Utilizadores, 1: Fornecedores

    // Dialog state for Suppliers
    var showAddSupplierDialog by remember { mutableStateOf(false) }
    var supName by remember { mutableStateOf("") }
    var supPhone by remember { mutableStateOf("") }
    var supEmail by remember { mutableStateOf("") }

    // Dialog state for Users
    var showAddUserDialog by remember { mutableStateOf(false) }
    var regName by remember { mutableStateOf("") }
    var regUsername by remember { mutableStateOf("") }
    var regPassword by remember { mutableStateOf("") }
    var regRole by remember { mutableStateOf("Cliente") }
    var roleDropdownExpanded by remember { mutableStateOf(false) }
    val rolesList = listOf("Administrador", "Director Geral", "Farmaceutico", "Balconista", "Entregador", "Cliente")

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (selectedTabState == 0) {
                        showAddUserDialog = true
                    } else {
                        showAddSupplierDialog = true
                    }
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = if (selectedTabState == 0) "Novo Utilizador" else "Novo Fornecedor")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Painel de Administração",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )

            TabRow(selectedTabIndex = selectedTabState) {
                Tab(selected = selectedTabState == 0, onClick = { selectedTabState = 0 }, text = { Text("Utilizadores") })
                Tab(selected = selectedTabState == 1, onClick = { selectedTabState = 1 }, text = { Text("Fornecedores") })
            }

            if (selectedTabState == 0) {
                // Users list
                if (users.isEmpty()) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("Nenhum utilizador registado ainda.")
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(users) { u ->
                            Card(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text(u.name, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                                        Text("Nome de utilizador: ${u.username}", style = MaterialTheme.typography.bodySmall)
                                        
                                        // Role badge
                                        val badgeColor = when (u.role) {
                                            "Administrador" -> WarningRed
                                            "Director Geral" -> Color.Blue
                                            "Farmaceutico" -> SuccessGreen
                                            "Balconista" -> HighlightOrange
                                            "Entregador" -> Color.Cyan
                                            else -> MaterialTheme.colorScheme.primary
                                        }
                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = badgeColor.copy(alpha = 0.15f))
                                        ) {
                                            Text(
                                                text = u.role,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = badgeColor,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                    IconButton(onClick = { viewModel.deleteUser(u) }) {
                                        Icon(Icons.Default.Delete, contentDescription = null, tint = WarningRed)
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // Suppliers list
                if (suppliers.isEmpty()) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("Nenhum fornecedor registado ainda.")
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(suppliers) { sup ->
                            Card(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text(sup.name, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                                        Text("Tel: ${sup.contactPhone}", style = MaterialTheme.typography.bodySmall)
                                        Text("Email: ${sup.email}", style = MaterialTheme.typography.bodySmall)
                                    }
                                    IconButton(onClick = { viewModel.deleteSupplier(sup) }) {
                                        Icon(Icons.Default.Delete, contentDescription = null, tint = WarningRed)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Supplier Dialog
    if (showAddSupplierDialog) {
        Dialog(onDismissRequest = { showAddSupplierDialog = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Adicionar Fornecedor", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                    OutlinedTextField(
                        value = supName,
                        onValueChange = { supName = it },
                        label = { Text("Nome da Empresa/Parceiro") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = supPhone,
                        onValueChange = { supPhone = it },
                        label = { Text("Telefone") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = supEmail,
                        onValueChange = { supEmail = it },
                        label = { Text("E-mail") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showAddSupplierDialog = false }) { Text("Cancelar") }
                        Button(onClick = {
                            viewModel.addSupplier(supName, supPhone, supEmail) { success ->
                                if (success) {
                                    showAddSupplierDialog = false
                                    supName = ""
                                    supPhone = ""
                                    supEmail = ""
                                }
                            }
                        }) { Text("Salvar") }
                    }
                }
            }
        }
    }

    // Add User Dialog
    if (showAddUserDialog) {
        Dialog(onDismissRequest = { showAddUserDialog = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Adicionar Utilizador (Interno)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                    OutlinedTextField(
                        value = regName,
                        onValueChange = { regName = it },
                        label = { Text("Nome Completo") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = regUsername,
                        onValueChange = { regUsername = it },
                        label = { Text("Nome de utilizador") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = regPassword,
                        onValueChange = { regPassword = it },
                        label = { Text("Palavra-passe") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Role selection drop down
                    Box {
                        OutlinedButton(
                            onClick = { roleDropdownExpanded = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Perfil: $regRole")
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                        }
                        DropdownMenu(
                            expanded = roleDropdownExpanded,
                            onDismissRequest = { roleDropdownExpanded = false }
                        ) {
                            rolesList.forEach { r ->
                                DropdownMenuItem(
                                    text = { Text(r) },
                                    onClick = {
                                        regRole = r
                                        roleDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showAddUserDialog = false }) { Text("Cancelar") }
                        Button(onClick = {
                            if (regName.isNotBlank() && regUsername.isNotBlank() && regPassword.isNotBlank()) {
                                viewModel.register(regName, regUsername, regPassword, regRole) { success, _ ->
                                    if (success) {
                                        showAddUserDialog = false
                                        regName = ""
                                        regUsername = ""
                                        regPassword = ""
                                        regRole = "Cliente"
                                    }
                                }
                            }
                        }) { Text("Adicionar") }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// MODULE 8: PHARMACY INFORMATION (SOBRE A FARMÁCIA)
// -------------------------------------------------------------
@Composable
fun InfoScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Banner/Illustration drawn using canvas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(Teal40, Mint40)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.LocalPharmacy,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Farmácia La Reference",
                    style = MaterialTheme.typography.titleLarge.copy(color = Color.White, fontWeight = FontWeight.Bold)
                )
                Text("Sua saúde é nossa maior referência", style = MaterialTheme.typography.bodySmall.copy(color = Color.White.copy(alpha = 0.8f)))
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = Teal40)
                    Text("Informações de Contacto", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                }
                HorizontalDivider()
                InfoRow(icon = Icons.Default.LocationOn, title = "Endereço", desc = "Av. de Moçambique, Km 3, Prédio La Reference, Maputo")
                InfoRow(icon = Icons.Default.Phone, title = "Telefone Geral", desc = "+258 21 445 566 / +258 84 900 1100")
                InfoRow(icon = Icons.Default.Email, title = "E-mail Geral", desc = "contacto@lareference.co.mz")
                InfoRow(icon = Icons.Default.WatchLater, title = "Horário", desc = "Segunda a Sábado: 07:30 - 21:00\nDomingos e Feriados: 08:00 - 18:00")
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = Teal40)
                    Text("Certificação e Licenciamento", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                }
                HorizontalDivider()
                Text(
                    "Farmácia devidamente autorizada pelo Ministério da Saúde de Moçambique sob licença nº 890/DF-MISAU.\n\nResponsável Técnico:\nDr. Carlos Tembe (Inscrição na Ordem nº 245-ORFM)",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
fun InfoRow(icon: ImageVector, title: String, desc: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        Column {
            Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
            Text(desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun ProductImage(
    imageUri: String?,
    categoryId: Int,
    categories: List<CategoryEntity>,
    modifier: Modifier = Modifier
) {
    val categoryName = remember(categoryId, categories) {
        categories.find { it.id == categoryId }?.name?.lowercase() ?: ""
    }

    if (!imageUri.isNullOrBlank()) {
        SubcomposeAsyncImage(
            model = imageUri,
            contentDescription = "Imagem do produto",
            modifier = modifier,
            contentScale = ContentScale.Crop,
            loading = {
                Box(
                    modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            },
            error = {
                ProductImageFallback(categoryName, Modifier.fillMaxSize())
            }
        )
    } else {
        ProductImageFallback(categoryName, modifier)
    }
}

@Composable
fun ProductImageFallback(categoryName: String, modifier: Modifier) {
    val (backgroundColor, tintColor, icon) = when {
        categoryName.contains("cardio") -> Triple(
            Color(0xFFFFEBEE), // Soft red
            Color(0xFFC62828), // Deep red
            Icons.Default.Favorite
        )
        categoryName.contains("derm") -> Triple(
            Color(0xFFE8F5E9), // Soft green
            Color(0xFF2E7D32), // Dark green
            Icons.Default.Opacity
        )
        categoryName.contains("vitam") || categoryName.contains("suple") -> Triple(
            Color(0xFFFFF3E0), // Soft orange
            Color(0xFFEF6C00), // Dark orange
            Icons.Default.AddCircle
        )
        categoryName.contains("antib") -> Triple(
            Color(0xFFE3F2FD), // Soft blue
            Color(0xFF1565C0), // Dark blue
            Icons.Default.MedicalServices
        )
        categoryName.contains("analg") -> Triple(
            Color(0xFFFFFDE7), // Soft yellow
            Color(0xFFF57F17), // Dark yellow
            Icons.Default.Healing
        )
        else -> Triple(
            Color(0xFFF3E5F5), // Soft purple
            Color(0xFF6A1B9A), // Dark purple
            Icons.Default.LocalPharmacy
        )
    }

    Box(
        modifier = modifier.background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tintColor,
            modifier = Modifier.fillMaxSize(0.4f)
        )
    }
}

// -------------------------------------------------------------
// PRINTING UTILITIES
// -------------------------------------------------------------
fun printReceipt(context: android.content.Context, htmlContent: String, jobName: String = "Recibo") {
    val webView = android.webkit.WebView(context)
    webView.webViewClient = object : android.webkit.WebViewClient() {
        override fun onPageFinished(view: android.webkit.WebView?, url: String?) {
            val printManager = context.getSystemService(android.content.Context.PRINT_SERVICE) as android.print.PrintManager
            val printAdapter = webView.createPrintDocumentAdapter(jobName)
            printManager.print(jobName, printAdapter, android.print.PrintAttributes.Builder().build())
        }
    }
    webView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
}

@Composable
fun ReceiptPreviewAndPrintDialog(
    sale: SaleEntity,
    items: List<SaleItemEntity>,
    onDismiss: () -> Unit,
    viewModel: PharmacyViewModel
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // Tax (IVA) rate selection state. Default Mozambican IVA is 16%. Let's default to 16.0%, but allow 0%, 5%, 17%, and Custom.
    var ivaRateInput by remember { mutableStateOf("16.0") }
    var selectedIvaRate by remember { mutableStateOf(16.0) }
    var isCustomIvaSelected by remember { mutableStateOf(false) }
    var dropdownExpanded by remember { mutableStateOf(false) }
    
    val itemsSubtotal = items.sumOf { it.price * it.quantity }
    val discountPercent = if (itemsSubtotal > 0 && sale.totalAmount < itemsSubtotal) {
        val diff = itemsSubtotal - sale.totalAmount
        val pct = (diff / itemsSubtotal * 100)
        Math.round(pct).toInt()
    } else {
        0
    }
    
    val discountAmount = itemsSubtotal * (discountPercent / 100.0)
    val baseAmount = itemsSubtotal - discountAmount
    val ivaAmount = baseAmount * (selectedIvaRate / 100.0)
    val finalTotal = baseAmount + ivaAmount
    
    val formattedSubtotal = String.format(java.util.Locale.US, "%.2f", itemsSubtotal)
    val formattedDiscount = String.format(java.util.Locale.US, "%.2f", discountAmount)
    val formattedIva = String.format(java.util.Locale.US, "%.2f", ivaAmount)
    val formattedTotal = String.format(java.util.Locale.US, "%.2f", finalTotal)

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .heightIn(max = 650.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ReceiptLong,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "Recibo - Pré-visualização",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Fechar")
                    }
                }

                HorizontalDivider()

                // Ticket Preview Box (Real Paper Receipt Ticket Aesthetic)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(Color(0xFFFAFAFA), RoundedCornerShape(8.dp))
                        .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(8.dp))
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Paper Header
                        Text(
                            text = "FARMÁCIA VIDA",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.Black,
                                textAlign = TextAlign.Center
                            )
                        )
                        Text(
                            text = "Av. Eduardo Mondlane, Maputo\nNUIT: 400123456 | Tel: +258 84 123 4567",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.DarkGray,
                                textAlign = TextAlign.Center,
                                fontSize = 11.sp
                            )
                        )
                        
                        Text(
                            text = "-".repeat(32),
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodySmall
                        )

                        // Meta details
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Venda Nº: #VND-${sale.id}", style = MaterialTheme.typography.labelSmall.copy(color = Color.Black, fontWeight = FontWeight.Bold))
                            Text(
                                java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date(sale.saleDate)),
                                style = MaterialTheme.typography.labelSmall.copy(color = Color.DarkGray)
                            )
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Cliente: ${sale.buyerName}", style = MaterialTheme.typography.labelSmall.copy(color = Color.Black))
                            Text("Op: ${sale.sellerName}", style = MaterialTheme.typography.labelSmall.copy(color = Color.DarkGray))
                        }

                        Text(
                            text = "-".repeat(32),
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodySmall
                        )

                        // Listed Items Table Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Medicamento", modifier = Modifier.weight(1.5f), style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Color.Black))
                            Text("Qtd", modifier = Modifier.weight(0.5f), style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Color.Black, textAlign = TextAlign.Center))
                            Text("Unit (MZN)", modifier = Modifier.weight(0.9f), style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Color.Black, textAlign = TextAlign.End))
                            Text("Total", modifier = Modifier.weight(1.1f), style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Color.Black, textAlign = TextAlign.End))
                        }

                        Text(
                            text = "-".repeat(32),
                            color = Color.LightGray,
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodySmall
                        )

                        // Listed Items Rows
                        items.forEach { item ->
                            val itemTotal = item.price * item.quantity
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(item.medicineName, modifier = Modifier.weight(1.5f), style = MaterialTheme.typography.bodySmall.copy(color = Color.Black))
                                Text("${item.quantity}", modifier = Modifier.weight(0.5f), style = MaterialTheme.typography.bodySmall.copy(color = Color.Black, textAlign = TextAlign.Center))
                                Text(String.format(java.util.Locale.US, "%.2f", item.price), modifier = Modifier.weight(0.9f), style = MaterialTheme.typography.bodySmall.copy(color = Color.Black, textAlign = TextAlign.End))
                                Text(String.format(java.util.Locale.US, "%.2f", itemTotal), modifier = Modifier.weight(1.1f), style = MaterialTheme.typography.bodySmall.copy(color = Color.Black, textAlign = TextAlign.End))
                            }
                        }

                        Text(
                            text = "-".repeat(32),
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodySmall
                        )

                        // Calculations summary
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Subtotal:", style = MaterialTheme.typography.bodySmall.copy(color = Color.Black))
                                Text("$formattedSubtotal MZN", style = MaterialTheme.typography.bodySmall.copy(color = Color.Black))
                            }
                            if (discountPercent > 0) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Desconto ($discountPercent%):", style = MaterialTheme.typography.bodySmall.copy(color = Color.Black))
                                    Text("-$formattedDiscount MZN", style = MaterialTheme.typography.bodySmall.copy(color = Color.Black))
                                }
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("IVA (${selectedIvaRate}%):", style = MaterialTheme.typography.bodySmall.copy(color = Color.Black))
                                Text("$formattedIva MZN", style = MaterialTheme.typography.bodySmall.copy(color = Color.Black))
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(), 
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("TOTAL PAGO:", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color.Black))
                                Text("$formattedTotal MZN", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color.Black))
                            }
                        }

                        Text(
                            text = "-".repeat(32),
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodySmall
                        )

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("MÉTODO PAGAMENTO:", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Color.Black))
                            Text(sale.paymentMethod, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Color.Black))
                        }

                        Text(
                            text = "-".repeat(32),
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodySmall
                        )

                        // Paper Footer
                        Text(
                            text = "Obrigado pela visita!\nGuarde este recibo como prova de compra.",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color.DarkGray,
                                textAlign = TextAlign.Center,
                                fontSize = 10.sp
                            )
                        )
                    }
                }

                HorizontalDivider()

                // IVA Percentage configuration via dropdown / dropup
                Text(
                    text = "Taxa de IVA (Não Obrigatório)",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(modifier = Modifier.weight(1.5f)) {
                        val labelText = if (isCustomIvaSelected) "IVA: Personalizado (${selectedIvaRate}%)" else "IVA: ${if (selectedIvaRate == 0.0) "Isento (0%)" else "${selectedIvaRate}%"}"
                        OutlinedButton(
                            onClick = { dropdownExpanded = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(labelText, fontSize = 12.sp)
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                        }
                        DropdownMenu(
                            expanded = dropdownExpanded,
                            onDismissRequest = { dropdownExpanded = false }
                        ) {
                            val options = listOf(
                                Pair(0.0, "Isento (0%)"),
                                Pair(5.0, "Mínimo (5%)"),
                                Pair(16.0, "Normal (16%)"),
                                Pair(17.0, "Elevado (17%)"),
                                Pair(-1.0, "Personalizado...")
                            )
                            options.forEach { (rate, label) ->
                                DropdownMenuItem(
                                    text = { Text(label) },
                                    onClick = {
                                        dropdownExpanded = false
                                        if (rate == -1.0) {
                                            isCustomIvaSelected = true
                                        } else {
                                            isCustomIvaSelected = false
                                            selectedIvaRate = rate
                                        }
                                    }
                                )
                            }
                        }
                    }

                    if (isCustomIvaSelected) {
                        OutlinedTextField(
                            value = ivaRateInput,
                            onValueChange = { newValue ->
                                if (newValue.isEmpty() || newValue.toDoubleOrNull() != null) {
                                    ivaRateInput = newValue
                                    selectedIvaRate = newValue.toDoubleOrNull() ?: 0.0
                                }
                            },
                            label = { Text("Taxa %", fontSize = 10.sp) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            scope.launch {
                                val html = generateReceiptHtml(sale, items, selectedIvaRate, discountPercent)
                                printReceipt(context, html, "Recibo_${sale.id}")
                            }
                        },
                        modifier = Modifier.weight(1.5f),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Print, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Imprimir Recibo", fontSize = 13.sp)
                    }

                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Text("Fechar", fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

fun generateReceiptHtml(
    sale: SaleEntity, 
    items: List<SaleItemEntity>, 
    ivaPercent: Double = 16.0,
    discountPercent: Int = 0
): String {
    val dateStr = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date(sale.saleDate))
    val subtotal = items.sumOf { it.price * it.quantity }
    val discountAmount = subtotal * (discountPercent / 100.0)
    val baseAmount = subtotal - discountAmount
    val ivaAmount = baseAmount * (ivaPercent / 100.0)
    val totalAmount = baseAmount + ivaAmount

    val itemsRows = StringBuilder()
    for (item in items) {
        val totalItem = item.price * item.quantity
        itemsRows.append("""
            <tr>
                <td align="left">${item.medicineName}</td>
                <td align="center">${item.quantity}</td>
                <td align="right">${String.format(java.util.Locale.US, "%.2f", item.price)}</td>
                <td align="right">${String.format(java.util.Locale.US, "%.2f", totalItem)}</td>
            </tr>
        """.trimIndent())
    }

    return """
        <!DOCTYPE html>
        <html>
        <head>
        <meta charset="utf-8">
        <style>
            body {
                font-family: 'Courier New', Courier, monospace;
                font-size: 13px;
                color: #000;
                margin: 10px;
                padding: 0;
            }
            .text-center {
                text-align: center;
            }
            .text-right {
                text-align: right;
            }
            .bold {
                font-weight: bold;
            }
            .header {
                margin-bottom: 12px;
            }
            .divider {
                border-top: 1px dashed #000;
                margin: 8px 0;
            }
            table {
                width: 100%;
                border-collapse: collapse;
            }
            th, td {
                padding: 3px 0;
                font-size: 13px;
            }
            .footer {
                margin-top: 15px;
                font-size: 11px;
            }
        </style>
        </head>
        <body>
            <div class="header text-center">
                <h3 style="margin: 0 0 4px 0;">FARMÁCIA VIDA</h3>
                <p style="margin: 2px 0; font-size: 11px;">Av. Eduardo Mondlane, Maputo</p>
                <p style="margin: 2px 0; font-size: 11px;">NUIT: 400123456 | Tel: +258 84 123 4567</p>
            </div>
            
            <div class="divider"></div>
            
            <div>
                <p style="margin: 3px 0;"><span class="bold">VENDA Nº:</span> #VND-${sale.id}</p>
                <p style="margin: 3px 0;"><span class="bold">DATA:</span> $dateStr</p>
                <p style="margin: 3px 0;"><span class="bold">CLIENTE:</span> ${sale.buyerName}</p>
                <p style="margin: 3px 0;"><span class="bold">OPERADOR:</span> ${sale.sellerName}</p>
            </div>
            
            <div class="divider"></div>
            
            <table>
                <thead>
                    <tr style="border-bottom: 1px dashed #000;">
                        <th align="left">Medicamento</th>
                        <th align="center">Qtd</th>
                        <th align="right">Preço</th>
                        <th align="right">Total</th>
                    </tr>
                </thead>
                <tbody>
                    $itemsRows
                </tbody>
            </table>
            
            <div class="divider"></div>
            
            <table style="width: 100%;">
                <tr>
                    <td class="bold">Subtotal:</td>
                    <td class="text-right">${String.format(java.util.Locale.US, "%.2f", subtotal)} MZN</td>
                </tr>
                ${if (discountPercent > 0) """
                <tr>
                    <td>Desconto ($discountPercent%):</td>
                    <td class="text-right">-${String.format(java.util.Locale.US, "%.2f", discountAmount)} MZN</td>
                </tr>
                """ else ""}
                <tr>
                    <td>IVA (${String.format(java.util.Locale.US, "%.1f", ivaPercent)}%):</td>
                    <td class="text-right">${String.format(java.util.Locale.US, "%.2f", ivaAmount)} MZN</td>
                </tr>
                <tr style="font-size: 15px; font-weight: bold;">
                    <td>TOTAL PAGO:</td>
                    <td class="text-right">${String.format(java.util.Locale.US, "%.2f", totalAmount)} MZN</td>
                </tr>
            </table>
            
            <div class="divider"></div>
            
            <p style="margin: 3px 0;"><span class="bold">MÉTODO PAGAMENTO:</span> ${sale.paymentMethod}</p>
            
            <div class="divider"></div>
            
            <div class="footer text-center">
                <p class="bold" style="margin: 3px 0;">Obrigado pela visita!</p>
                <p style="margin: 2px 0;">Conserve este recibo como prova de compra.</p>
                <p style="margin: 2px 0;">Processado por AI Studio POS</p>
            </div>
        </body>
        </html>
    """.trimIndent()
}

// -------------------------------------------------------------
// SCREEN: HOME (PÁGINA PRINCIPAL)
// -------------------------------------------------------------
@Composable
fun HomeScreen(viewModel: PharmacyViewModel) {
    val medicines by viewModel.medicines.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableStateOf<Int?>(null) }
    val context = LocalContext.current

    var selectedProductForDetail by remember { mutableStateOf<MedicineEntity?>(null) }

    val featuredMedicines = remember(medicines) { medicines.filter { it.isFeatured } }
    val promotionMedicines = remember(medicines) { medicines.filter { it.promotionPrice != null } }
    val filteredMedicines = remember(medicines, searchQuery, selectedCategoryId) {
        medicines.filter { med ->
            val matchesSearch = med.name.contains(searchQuery, ignoreCase = true)
            val matchesCategory = selectedCategoryId == null || med.categoryId == selectedCategoryId
            matchesSearch && matchesCategory
        }
    }
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome Header Banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Medication,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "LA REFERENCE",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Olá, ${currentUser?.name ?: "Utilizador"}!",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "Encontre os melhores medicamentos em promoção, em destaque e todo o catálogo de forma fácil.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
            }
        }

        // Search and Filters
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Procurar produto...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
        }

        // Category pills
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                InputChip(
                    selected = selectedCategoryId == null,
                    onClick = { selectedCategoryId = null },
                    label = { Text("Todos") }
                )
                categories.forEach { cat ->
                    InputChip(
                        selected = selectedCategoryId == cat.id,
                        onClick = { selectedCategoryId = cat.id },
                        label = { Text(cat.name) }
                    )
                }
            }
        }

        // Only show featured and promotions sections when not searching
        if (searchQuery.isEmpty() && selectedCategoryId == null) {
            // Featured Row
            if (featuredMedicines.isNotEmpty()) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = HighlightOrange,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Produtos em Destaque",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(featuredMedicines) { med ->
                                FeaturedProductCard(med, categories) {
                                    selectedProductForDetail = med
                                }
                            }
                        }
                    }
                }
            }

            // Promotions Row
            if (promotionMedicines.isNotEmpty()) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalOffer,
                                contentDescription = null,
                                tint = WarningRed,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Ofertas & Promoções",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(promotionMedicines) { med ->
                                PromoProductCard(med, categories) {
                                    selectedProductForDetail = med
                                }
                            }
                        }
                    }
                }
            }
        }

        // All Products Title
        item {
            Text(
                text = if (searchQuery.isEmpty() && selectedCategoryId == null) "Todos os Produtos" else "Resultado da Pesquisa",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        if (filteredMedicines.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Nenhum produto encontrado.", style = MaterialTheme.typography.bodyMedium)
                }
            }
        } else {
            items(filteredMedicines) { med ->
                HorizontalProductItem(med, categories) {
                    selectedProductForDetail = med
                }
            }
        }
    }

    // Detail Dialog
    if (selectedProductForDetail != null) {
        ProductDetailDialog(
            product = selectedProductForDetail!!,
            categories = categories,
            onDismiss = { selectedProductForDetail = null },
            onAddToCart = {
                viewModel.addToCart(selectedProductForDetail!!)
                Toast.makeText(context, "${selectedProductForDetail!!.name} adicionado ao carrinho!", Toast.LENGTH_SHORT).show()
                selectedProductForDetail = null
            }
        )
    }
}

@Composable
fun FeaturedProductCard(
    med: MedicineEntity,
    categories: List<CategoryEntity>,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(180.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column {
            Box(modifier = Modifier.fillMaxWidth()) {
                ProductImage(
                    imageUri = med.imageUri,
                    categoryId = med.categoryId,
                    categories = categories,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                )
                // Highlight Star Badge
                Box(
                    modifier = Modifier
                        .padding(8.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(HighlightOrange)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                        .align(Alignment.TopEnd)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
            Column(
                modifier = Modifier.padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = med.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        if (med.promotionPrice != null) {
                            Text(
                                text = "${med.promotionPrice} MZN",
                                style = MaterialTheme.typography.bodySmall,
                                color = WarningRed,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${med.price} MZN",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            Text(
                                text = "${med.price} MZN",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "Ver Detalhes",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun PromoProductCard(
    med: MedicineEntity,
    categories: List<CategoryEntity>,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(180.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = WarningRed.copy(alpha = 0.05f)),
        border = BorderStroke(1.dp, WarningRed.copy(alpha = 0.2f))
    ) {
        Column {
            Box(modifier = Modifier.fillMaxWidth()) {
                ProductImage(
                    imageUri = med.imageUri,
                    categoryId = med.categoryId,
                    categories = categories,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                )
                // Discount tag
                Box(
                    modifier = Modifier
                        .padding(8.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(WarningRed)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                        .align(Alignment.TopStart)
                ) {
                    Text(
                        text = "PROMO",
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Column(
                modifier = Modifier.padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = med.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "${med.promotionPrice} MZN",
                            style = MaterialTheme.typography.bodySmall,
                            color = WarningRed,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${med.price} MZN",
                            style = MaterialTheme.typography.labelSmall.copy(
                                textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "Ver Detalhes",
                        tint = WarningRed,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun HorizontalProductItem(
    med: MedicineEntity,
    categories: List<CategoryEntity>,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProductImage(
                imageUri = med.imageUri,
                categoryId = med.categoryId,
                categories = categories,
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(8.dp))
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = med.name,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (med.promotionPrice != null) {
                        Text(
                            text = "${med.promotionPrice} MZN",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = WarningRed)
                        )
                        Text(
                            text = "${med.price} MZN",
                            style = MaterialTheme.typography.bodySmall.copy(
                                textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Text(
                            text = "${med.price} MZN",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    // Stock badge
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (med.stock <= 20) WarningRed.copy(alpha = 0.1f) else SuccessGreen.copy(alpha = 0.1f)
                        )
                    ) {
                        Text(
                            text = "Stock: ${med.stock}",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (med.stock <= 20) WarningRed else SuccessGreen
                        )
                    }
                }
            }

            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun ProductDetailDialog(
    product: MedicineEntity,
    categories: List<CategoryEntity>,
    onDismiss: () -> Unit,
    onAddToCart: () -> Unit
) {
    val categoryName = remember(product.categoryId, categories) {
        categories.find { it.id == product.categoryId }?.name ?: "Medicamento"
    }
    val dateStr = remember(product.expiryDate) {
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(product.expiryDate))
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Image
                ProductImage(
                    imageUri = product.imageUri,
                    categoryId = product.categoryId,
                    categories = categories,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(12.dp))
                )

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = product.name,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Categoria: $categoryName",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                HorizontalDivider()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Validade:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = dateStr,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Stock Disponível:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${product.stock} unidades",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (product.stock <= 20) WarningRed else SuccessGreen
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Preço:",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Column(horizontalAlignment = Alignment.End) {
                        if (product.promotionPrice != null) {
                            Text(
                                text = "${product.promotionPrice} MZN",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = WarningRed
                            )
                            Text(
                                text = "${product.price} MZN",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            Text(
                                text = "${product.price} MZN",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Voltar")
                    }

                    Button(
                        onClick = onAddToCart,
                        modifier = Modifier.weight(1.5f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddShoppingCart,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Adicionar")
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// SCREEN: USER PROFILE (PERFIL)
// -------------------------------------------------------------
@Composable
fun ProfileScreen(viewModel: PharmacyViewModel) {
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val sales by viewModel.sales.collectAsStateWithLifecycle()
    val orders by viewModel.orders.collectAsStateWithLifecycle()
    val reservations by viewModel.reservations.collectAsStateWithLifecycle()
    val medicines by viewModel.medicines.collectAsStateWithLifecycle()

    val context = LocalContext.current

    var showEditDialog by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf(currentUser?.name ?: "") }
    var newPassword by remember { mutableStateOf(currentUser?.password ?: "") }

    LaunchedEffect(currentUser) {
        if (currentUser != null) {
            newName = currentUser?.name ?: ""
            newPassword = currentUser?.password ?: ""
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Upper Profile Header
        item {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(vertical = 16.dp)
            ) {
                // Circle Avatar
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    val initials = remember(currentUser) {
                        currentUser?.name?.split(" ")?.mapNotNull { it.firstOrNull() }?.take(2)?.joinToString("")?.uppercase() ?: "U"
                    }
                    Text(
                        text = initials,
                        style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = currentUser?.name ?: "Utilizador",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "@${currentUser?.username ?: "username"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Role badge
                val roleColor = when (currentUser?.role) {
                    "Administrador" -> WarningRed
                    "Director Geral" -> HighlightOrange
                    "Farmaceutico" -> Teal40
                    "Balconista" -> SuccessGreen
                    else -> MaterialTheme.colorScheme.primary
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = roleColor.copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = currentUser?.role ?: "Cliente",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = roleColor
                    )
                }
            }
        }

        // Account statistics based on role
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Resumo da Atividade",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (currentUser?.role == "Cliente") {
                            // Orders placed
                            val clientOrders = remember(orders, currentUser) {
                                orders.filter { it.customerName == currentUser?.name }
                            }
                            // Reservations
                            val clientReservations = remember(reservations, currentUser) {
                                reservations.filter { it.customerName == currentUser?.name }
                            }

                            ProfileStatItem(
                                title = "Meus Pedidos",
                                value = "${clientOrders.size}",
                                icon = Icons.Default.LibraryBooks,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.weight(1f)
                            )

                            ProfileStatItem(
                                title = "Reservas",
                                value = "${clientReservations.size}",
                                icon = Icons.Default.Bookmark,
                                color = HighlightOrange,
                                modifier = Modifier.weight(1f)
                            )
                        } else {
                            // Staff/Admin metrics
                            val staffSales = remember(sales, currentUser) {
                                sales.filter { it.sellerId == currentUser?.id || currentUser?.role == "Administrador" }
                            }
                            val totalMedicines = medicines.size

                            ProfileStatItem(
                                title = if (currentUser?.role == "Administrador") "Vendas Farmácia" else "Minhas Vendas",
                                value = "${staffSales.size}",
                                icon = Icons.Default.PointOfSale,
                                color = SuccessGreen,
                                modifier = Modifier.weight(1f)
                            )

                            ProfileStatItem(
                                title = "Medicamentos",
                                value = "$totalMedicines",
                                icon = Icons.Default.Medication,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        // Action Options
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column {
                    ProfileOptionRow(
                        title = "Editar Perfil",
                        subtitle = "Alterar nome e palavra-passe",
                        icon = Icons.Default.Edit,
                        onClick = { showEditDialog = true }
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    ProfileOptionRow(
                        title = "Termos e Privacidade",
                        subtitle = "Termos de uso e privacidade da LA REFERENCE",
                        icon = Icons.Default.Security,
                        onClick = {
                            Toast.makeText(context, "LA REFERENCE v1.5.0 - Privacidade garantida localmente.", Toast.LENGTH_LONG).show()
                        }
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    ProfileOptionRow(
                        title = "Terminar Sessão",
                        subtitle = "Sair da conta e voltar ao login",
                        icon = Icons.Default.Logout,
                        tint = WarningRed,
                        onClick = {
                            viewModel.logout()
                        }
                    )
                }
            }
        }
    }

    // Edit profile dialog
    if (showEditDialog && currentUser != null) {
        Dialog(onDismissRequest = { showEditDialog = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Editar Perfil",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text("Nome Completo") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = { Text("Palavra-Passe") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { showEditDialog = false }) {
                            Text("Cancelar")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (newName.isNotBlank() && newPassword.isNotBlank()) {
                                    val updatedUser = currentUser!!.copy(name = newName, password = newPassword)
                                    viewModel.updateUser(updatedUser) { success, msg ->
                                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                        if (success) {
                                            showEditDialog = false
                                        }
                                    }
                                } else {
                                    Toast.makeText(context, "Por favor preencha todos os campos.", Toast.LENGTH_SHORT).show()
                                }
                            }
                        ) {
                            Text("Salvar")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileStatItem(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.08f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ProfileOptionRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    tint: Color = MaterialTheme.colorScheme.primary,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(24.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = if (tint == WarningRed) WarningRed else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            imageVector = Icons.Default.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.size(16.dp)
        )
    }
}
