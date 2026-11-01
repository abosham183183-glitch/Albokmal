package com.alkolhana.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.FirebaseFirestore

// ألوان الهوية البصرية لـ ALO USTA
val NavyBlue = Color(0xFF0A1E3C)
val SoftGold = Color(0xFFD4A017)
val LightBg = Color(0xFFEEF2F8)
val White = Color.White

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val db = FirebaseFirestore.getInstance()

        setContent {
            MaterialTheme(
                colorScheme = lightColorScheme(
                    primary = NavyBlue, 
                    secondary = SoftGold
                )
            ) {
                Surface(modifier = Modifier.fillMaxSize(), color = LightBg) {
                    MainScreenContainer(db = db)
                }
            }
        }
    }
}

enum class Screen {
    HOME, CREATE_CARD, LOGIN
}

@Composable
fun MainScreenContainer(db: FirebaseFirestore) {
    var currentScreen by remember { mutableStateOf(Screen.HOME) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("ALO USTA", color = White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Text("دليل الحرفيين والخدمات", color = SoftGold, fontSize = 11.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NavyBlue),
                actions = {
                    IconButton(onClick = { /* إشعارات */ }) {
                        Icon(Icons.Default.Notifications, contentDescription = "تنبيهات", tint = White)
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(containerColor = White) {
                NavigationBarItem(
                    selected = currentScreen == Screen.HOME,
                    onClick = { currentScreen = Screen.HOME },
                    icon = { Icon(Icons.Default.Home, contentDescription = "الرئيسية") },
                    label = { Text("الرئيسية") }
                )
                NavigationBarItem(
                    selected = currentScreen == Screen.CREATE_CARD,
                    onClick = { currentScreen = Screen.CREATE_CARD },
                    icon = { Icon(Icons.Default.AddCircle, contentDescription = "إنشاء بطاقة") },
                    label = { Text("إنشاء بطاقة") }
                )
                NavigationBarItem(
                    selected = currentScreen == Screen.LOGIN,
                    onClick = { currentScreen = Screen.LOGIN },
                    icon = { Icon(Icons.Default.Person, contentDescription = "حسابي / دخول") },
                    label = { Text("حسابي") }
                )
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when (currentScreen) {
                Screen.HOME -> HomeScreen(db = db)
                Screen.CREATE_CARD -> CreateCardScreen(db = db, onCardCreated = { currentScreen = Screen.HOME })
                Screen.LOGIN -> LoginScreen(onLoginSuccess = { currentScreen = Screen.HOME })
            }
        }
    }
}

// ----------------------------------------------------
// 1. الشاشة الرئيسية مع زر التحديث الحقيقي
// ----------------------------------------------------
@Composable
fun HomeScreen(db: FirebaseFirestore) {
    var craftsmen by remember { mutableStateOf(listOf>()) }
    var searchQuery by remember { mutableStateOf("") }
    var isRefreshing by remember { mutableStateOf(false) }

    // دالة جلب البيانات المحدثة من Firestore
    val fetchCards = {
        isRefreshing = true
        db.collection("cards").get()
            .addOnSuccessListener { result ->
                craftsmen = result.documents.map { it.data ?: emptyMap() }
                isRefreshing = false
            }
            .addOnFailureListener {
                isRefreshing = false
            }
    }

    // جلب البيانات عند فتح الشاشة لأول مرة
    LaunchedEffect(Unit) {
        fetchCards()
    }

    val filteredCraftsmen = craftsmen.filter { card ->
        val name = card["name"] as? String ?: ""
        val profession = card["profession"] as? String ?: ""
        val province = card["province"] as? String ?: ""
        name.contains(searchQuery, ignoreCase = true) ||
                profession.contains(searchQuery, ignoreCase = true) ||
                province.contains(searchQuery, ignoreCase = true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("أهلاً بك في ALO USTA", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = NavyBlue)
        Text("منصتك المباشرة للبحث عن أفضل الحرفيين", fontSize = 13.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 12.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("ابحث عن حرفي، مهنة، أو محافظة") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = SoftGold) },
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            singleLine = true
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("البطاقات المتاحة", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = NavyBlue)
            
            // زر التحديث الفعلي والنشط
            TextButton(
                onClick = { fetchCards() },
                enabled = !isRefreshing
            ) {
                Text(if (isRefreshing) "جاري التحديث..." else "تحديث 🔄", color = SoftGold, fontWeight = FontWeight.Bold)
            }
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(top = 8.dp)) {
            items(filteredCraftsmen) { card ->
                CraftCard(card = card)
            }
        }
    }
}

@Composable
fun CraftCard(card: Map<String, Any>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = card["name"] as? String ?: "اسم الحرفي",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = NavyBlue
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = card["profession"] as? String ?: "المهنة",
                fontSize = 14.sp,
                color = SoftGold,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = card["province"] as? String ?: "المحافظة", fontSize = 12.sp, color = Color.Gray)
                
                Spacer(modifier = Modifier.weight(1f))
                
                val phone = card["phone"] as? String ?: ""
                if (phone.isNotEmpty()) {
                    Text(text = phone, fontSize = 12.sp, color = NavyBlue, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ----------------------------------------------------
// 2. شاشة إنشاء بطاقة حرفي (CreateCardScreen)
// ----------------------------------------------------
@Composable
fun CreateCardScreen(db: FirebaseFirestore, onCardCreated: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var profession by remember { mutableStateOf("") }
    var province by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var statusMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBg)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("إضافة بطاقة جديدة إلى ALO USTA", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = NavyBlue, modifier = Modifier.padding(vertical = 12.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("اسم الحرفي الكامل") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = profession,
            onValueChange = { profession = it },
            label = { Text("المهنة (مثال: كهربائي، بلاط)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = province,
            onValueChange = { province = it },
            label = { Text("المحافظة / المنطقة") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("رقم للتواصل") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                if (name.isNotBlank() && profession.isNotBlank() && province.isNotBlank()) {
                    isLoading = true
                    val cardData = hashMapOf(
                        "name" to name,
                        "profession" to profession,
                        "province" to province,
                        "phone" to phone,
                        "createdAt" to com.google.firebase.Timestamp.now()
                    )

                    db.collection("cards")
                        .add(cardData)
                        .addOnSuccessListener {
                            isLoading = false
                            statusMessage = "تم نشر البطاقة بنجاح!"
                            onCardCreated()
                        }
                        .addOnFailureListener { e ->
                            isLoading = false
                            statusMessage = "فشل الحفظ: ${e.localizedMessage}"
                        }
                } else {
                    statusMessage = "يرجى ملء الحقول الأساسية."
                }
            },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = SoftGold),
            enabled = !isLoading
        ) {
            Text("نشر البطاقة فوراً", color = NavyBlue, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }

        if (statusMessage.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(statusMessage, color = NavyBlue, fontWeight = FontWeight.Medium)
        }
    }
}

// ----------------------------------------------------
// 3. شاشة تسجيل الدخول (LoginScreen)
// ----------------------------------------------------
@Composable
fun LoginScreen(onLoginSuccess: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBg)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.AccountCircle, contentDescription = null, tint = NavyBlue, modifier = Modifier.size(72.dp))
        Spacer(modifier = Modifier.height(12.dp))
        Text("تسجيل دخول المشرفين", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = NavyBlue)
        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("البريد الإلكتروني") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("كلمة المرور") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                if (email.isNotBlank() && password.isNotBlank()) {
                    message = "تم تسجيل الدخول بنجاح"
                    onLoginSuccess()
                } else {
                    message = "يرجى إدخال بيانات صحيحة"
                }
            },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = NavyBlue)
        ) {
            Text("دخول", color = White, fontSize = 16.sp)
        }

        if (message.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(message, color = Color.Red, fontSize = 13.sp)
        }
    }
}
