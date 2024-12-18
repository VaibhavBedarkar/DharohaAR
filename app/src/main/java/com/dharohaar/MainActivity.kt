package com.dharohaar

import android.graphics.Bitmap
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import java.util.Calendar
import java.util.Hashtable
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.google.android.material.datepicker.MaterialDatePicker
import java.util.Date
import java.util.Locale
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import java.text.SimpleDateFormat

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DharooharApp()
        }
    }
}

@Composable
fun DharooharApp() {
    var showSplashScreen by remember { mutableStateOf(true) }

    if (showSplashScreen) {
        SplashScreen(onTimeout = { showSplashScreen = false })
    } else {
        AuthenticationScreen()
    }
}

fun generateQRCode(content: String, width: Int = 512, height: Int = 512): Bitmap {
    val hints = Hashtable<EncodeHintType, String>()
    hints[EncodeHintType.CHARACTER_SET] = "UTF-8"

    val bitMatrix: BitMatrix = MultiFormatWriter().encode(
        content, BarcodeFormat.QR_CODE, width, height, hints
    )

    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
    for (x in 0 until width) {
        for (y in 0 until height) {
            bitmap.setPixel(x, y, if (bitMatrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
        }
    }
    return bitmap
}

@Composable
fun SplashScreen(onTimeout: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(3000) // 3 seconds delay
        onTimeout()
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(R.drawable.app),
            contentDescription = "DharohAR Logo"
        )
    }
}

@Composable
fun AuthenticationScreen() {
    var isLogin by remember { mutableStateOf(true) }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var mobile by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    var loginSuccess by remember { mutableStateOf(false) }
    val visitorNationalities = listOf("Indian", "Foreigner", "SAARC", "BIMSTEC")
    var visitorNationality by remember { mutableStateOf("") }
    if (loginSuccess) {
       HomeScreen()
    } else {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(16.dp)
            ) {

                Image(
                    painter = painterResource(R.drawable.culture),
                    contentDescription = "Culture asset"
                )

                Text(text = if (isLogin) "Login" else "Sign Up", style = MaterialTheme.typography.titleLarge)

                if (!isLogin) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = mobile,
                        onValueChange = { mobile = it.filter { char -> char.isDigit() } },
                        label = { Text("Mobile") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    // Monument Dropdown
                    CustomDropdownField(
                        label = "Select Nationality",
                        options = visitorNationalities,
                        selectedOption = visitorNationality,
                        onOptionSelected = { visitorNationality = it }
                    )


                }

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        val image = if (passwordVisible)
                            Icons.Filled.Visibility
                        else Icons.Filled.VisibilityOff

                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(image, contentDescription = "Toggle password visibility")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = {
                        coroutineScope.launch {
                            if (isLogin) {
                                val success = performLogin(email, password)
                                if (success) {
                                    loginSuccess = true


                                } else {
                                    snackbarHostState.showSnackbar("Login Failed")
                                }
                            } else {
                                performSignup(name, email, mobile, password)
                                snackbarHostState.showSnackbar("Signup Successful, Please Login")
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = if (isLogin) "Login" else "Sign Up")
                }

                TextButton(onClick = { isLogin = !isLogin }) {
                    Text(text = if (isLogin) "Don't have an account? Sign Up" else "Already have an account? Login")
                }
            }

            SnackbarHost(hostState = snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter))
        }
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen() {
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val monumentNames = listOf("Taj Mahal", "Qutub Minar", "Red Fort", "Hawa Mahal")
    val slots = listOf("Forenoon", "Afternoon", "Evening")

    var monumentName by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf("") }
    var selectedSlot by remember { mutableStateOf("") }

    var showDatePicker by remember { mutableStateOf(false) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = { BottomNavigationBar() } // Integrated into Scaffold's bottomBar
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            // Header Image
            Spacer(modifier = Modifier.height(8.dp))
            Image(
                painter = painterResource(id = R.drawable.monumentgraphic), // Replace with your image
                contentDescription = "Colorful Monument Illustration",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(MaterialTheme.shapes.medium)
            )

            Text(
                text = "Experience just a few clicks away...!!",
                fontSize = 22.sp,
                fontFamily = FontFamily.Cursive,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))
            CustomDropdownField(
                label = "Select Monument",
                options = monumentNames,
                selectedOption = monumentName,
                onOptionSelected = { monumentName = it }
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = selectedDate ?: "Select Visit Date",
                onValueChange = {},
                label = { Text("Visit Date") },
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Select Visit Date"
                        )
                    }
                },
                readOnly = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Show Date Picker Modal when the state is true
            if (showDatePicker) {
                DatePickerModalInput(
                    onDateSelected = { dateMillis ->
                        // Convert the selected date millis to a formatted string
                        selectedDate = dateMillis?.let { convertMillisToDate(it) }.toString()
                    },
                    onDismiss = { showDatePicker = false }
                )
            }




            Spacer(modifier = Modifier.height(12.dp))
            CustomDropdownField(
                label = "Select Slot",
                options = slots,
                selectedOption = selectedSlot,
                onOptionSelected = { selectedSlot = it }
            )

            // Book Button
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = { /* Call your booking logic */ },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text("Book", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}
// Reusable Custom Dropdown Field
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomDropdownField(
    label: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedText by remember { mutableStateOf(selectedOption) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        TextField(
            value = selectedText,
            onValueChange = {},
            label = { Text(label) },
            readOnly = true,
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        selectedText = option
                        onOptionSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

// Custom TextField for Visit Date
@Composable
fun CustomTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    readOnly: Boolean = false
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        readOnly = readOnly,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = readOnly) {
            }
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerModalInput(
    onDateSelected: (Long?) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState(initialDisplayMode = DisplayMode.Input)

    // Today's date in milliseconds (midnight)
    val today = remember {
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                val selectedMillis = datePickerState.selectedDateMillis
                if (selectedMillis != null && selectedMillis >= today) {
                    onDateSelected(selectedMillis)
                    onDismiss()
                } else {
                    errorMessage = "Please select today or a future date"
                }
            }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            DatePicker(
                state = datePickerState
            )
            errorMessage?.let {
                Text(it, color = Color.Red, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

fun convertMillisToDate(millis: Long): String {
    val formatter = SimpleDateFormat("dd/MM/YYYY", Locale.getDefault())
    return formatter.format(Date(millis))
}
// Dropdown Menu Composable
// Visitor Nationality Dialog
@Composable
fun VisitorNationalityDialog(
    visitorNationalities: List<String>,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedNationality by remember { mutableStateOf(visitorNationalities[0]) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Visitor Nationality") },
        text = {
            Column {
                visitorNationalities.forEach { nationality ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .clickable { selectedNationality = nationality }
                    ) {
                        RadioButton(
                            selected = (nationality == selectedNationality),
                            onClick = { selectedNationality = nationality }
                        )
                        Text(nationality)
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(selectedNationality) }) {
                Text("Confirm")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}



fun bookTicketApi(
    monument: String,
    date: String,
    slot: String,
    nationality: String
): String {
    return "$monument|$date|$slot|$nationality" // Simulate response
}

// Generate QR Code (Replace with actual QR library like ZXing)
fun generateQRCode(content: String): Bitmap {
    val size = 512
    return Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
}

// Date Picker Stub
@Composable
fun DatePickerField( onDateSelected: (String) -> Unit) {
    val context = LocalContext.current
    var selectedDate by remember { mutableStateOf("") }

    // Function to open Material Date Picker
    fun openDatePicker() {
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .build()

        datePicker.show((context as androidx.fragment.app.FragmentActivity).supportFragmentManager, "DATE_PICKER")

        datePicker.addOnPositiveButtonClickListener { timeInMillis ->
            val formattedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(timeInMillis)
            selectedDate = formattedDate
            onDateSelected(formattedDate)
        }

        datePicker.addOnNegativeButtonClickListener {
            Toast.makeText(context, "No date selected", Toast.LENGTH_SHORT).show()
        }
    }

    // OutlinedTextField as clickable input
    OutlinedTextField(
        value = selectedDate,
        onValueChange = {},
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { openDatePicker() },
        readOnly = true
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewDatePickerField() {
    Surface {
        DatePickerField() { selectedDate ->
            println("Selected Date: $selectedDate")
        }
    }
}


@Composable
fun DropdownMenuField(label: String, options: List<String>, selectedValue: String, onValueChange: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        TextField(
            value = selectedValue,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true }
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    onClick = {
                        onValueChange(option)
                        expanded = false
                    },
                    text = { Text(option) }
                )
            }
        }
    }
}


fun checkAvailabilityApi(monument: String, date: String, slot: String): Boolean {
   // return monument.isNotEmpty() && date.isNotEmpty() && slot.isNotEmpty()
    return true
}

data class NavItem(val label: String, val icon: ImageVector)
@Composable
fun BottomNavigationBar() {
    val navItems = listOf(
        NavItem("Home", Icons.Default.Home),
        NavItem("Bookings", Icons.Default.Book),
        NavItem("Logout", Icons.Default.Logout)
    )

    var selectedItem by remember { mutableStateOf(0) }

    NavigationBar(
        tonalElevation = 8.dp // Adds elevation for better visibility
    ) {
        navItems.forEachIndexed { index, item ->
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label
                    )
                },
                label = {
                    Text(item.label)
                },
                selected = selectedItem == index,
                onClick = { selectedItem = index },
                alwaysShowLabel = true // Always shows the label for each item
            )
        }
    }
//val selectedItemIndex = selectedItem;
//
//    when(selectedItemIndex) {
//        0->{
//
//        }
//        1->{
//
//        }
//        2->{
//            AuthenticationScreen()
//        }
//    }
}



val client = HttpClient(CIO) {
    install(ContentNegotiation) {
        json(Json { ignoreUnknownKeys = true })
    }
    install(Logging) {
        level = LogLevel.ALL
    }
}

suspend fun performSignup(name: String, email: String, mobile: String, password: String) {
    val response: HttpResponse = client.post("https://your-api-endpoint/signup") {
        parameter("name", name)
        parameter("email", email)
        parameter("mobile", mobile)
        parameter("password", password)
    }
    println("Signup Response: ${response.status}")
}

suspend fun performLogin(email: String, password: String): Boolean {
    return try {
        val response: HttpResponse = client.post("https://your-api-endpoint/login") {
            parameter("email", email)
            parameter("password", password)
        }
        if (response.status.isSuccess()) {
            val jwtToken = response.bodyAsText()
            println("JWT Token: $jwtToken")
            true
        } else {
            true
        }
    } catch (e: Exception) {
        e.printStackTrace()
        true
    }
}

