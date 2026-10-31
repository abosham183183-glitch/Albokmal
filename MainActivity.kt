package com.alousta.app // تأكد من تغيير هذا لحزمة مشروعك الصحيحة

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

// تعريف ألوان الهوية الجديدة ALO USTA
val NavyBlue = Color(0xFF0A1E3C)
val SoftGold = Color(0xFFD4A017)
val LightBg = Color(0xFFEEF2F8)
val White = Color.White

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // ربط Firebase (تأكد من إضافة ملف google-services.json)
        val db = FirebaseFirestore.getInstance()

        setContent {
            MaterialTheme(
                colorScheme = lightColorScheme(primary = NavyBlue, secondary = SoftGold)
            ) {
                Surface(modifier = Modifier.fillMaxSize(), color = LightBg) {
                    AloUstaApp(db = db)
                }
            }
        }
    }
}

@Composable
fun AloUstaApp(db: FirebaseFirestore) {
    var craftsmen by remember { mutableStateOf(listOf<Map<String, Any>>()) }
    
    // جلب البيانات من Firestore (نفس مجموعة بيانات الموقع)
    LaunchedEffect(Unit) {
        db.collection("cards").limit(20).get()
            .addOnSuccessListener { result ->
                craftsmen = result.documents.map { it.data ?: emptyMap() }
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text("ALO USTA", color = White, fontWeight = FontWeight.Bold, fontSize = 22.sp) 
                },
                subtitle = { 
                    Text("منصة الحرفيين والمحافظات", color = SoftGold, fontSize = 12.sp) 
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NavyBlue),
                actions = {
                    IconButton(onClick = { /* تفعيل الإشعارات لاحقاً */ }) {
                        Icon(Icons.Default.Notifications, contentDescription = "تنبيهات", tint = White)
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(containerColor = White) {
                NavigationBarItem(
                    selected = true,
                    onClick = {},
                    icon = { Icon(Icons.Default.Home, contentDescription = "الرئيسية") },
                    label = { Text("الرئيسية") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = {},
                    icon = { Icon(Icons.Default.Search, contentDescription = "بحث") },
                    label = { Text("بحث") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = {},
                    icon = { Icon(Icons.Default.LocationOn, contentDescription = "المحافظات") },
                    label = { Text("المحافظات") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = {},
                    icon = { Icon(Icons.Default.Person, contentDescription = "حسابي") },
                    label = { Text("حسابي") }
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier.padding(paddingValues).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // رسالة الترحيب
            Text("أهلاً بك في ALO USTA", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = NavyBlue)
            Text("اعثر على الحرفي المناسب قريباً منك", fontSize = 14.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 16.dp))

            // شريط البحث
            OutlinedTextField(
                value = "",
                onValueChange = {},
                placeholder = { Text("ابحث عن حرفي، مهنة أو محافظة...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = SoftGold) },
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                singleLine = true
            )

            // أزرار الأقسام الرئيسية
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                FilterChip(
                    onClick = { /* فتح صفحة المحافظات */ },
                    label = { Text("كل المحافظات") },
                    leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = NavyBlue) }
                )
                FilterChip(
                    onClick = { /* فتح صفحة المهن */ },
                    label = { Text("تصفح المهن") },
                    leadingIcon = { Icon(Icons.Default.Build, contentDescription = null, tint = NavyBlue) }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // قائمة الحرفيين
            Text("حرفيون وبطاقات حديثة", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = NavyBlue, modifier = Modifier.fillMaxWidth())
            
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(top = 8.dp)) {
                items(craftsmen) { card ->
                    CraftCard(card = card)
                }
            }
        }
    }
}

@Composable
fun CraftCard(card: Map<String, Any>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = White)
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
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = card["province"] as? String ?: "المحافظة", fontSize = 12.sp, color = Color.Gray)
            }
        }
    }
}
