package com.example.tcmanager

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.clickable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.Calendar
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tcmanager.data.AppDatabase
import com.example.tcmanager.data.Complex
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch

private data class InspectionSchedule(
    val date: String,
    val details: List<String> = emptyList()
)

// Редактируйте расписание проверок здесь: комплекс -> месяц -> дата и детали.
private val INSPECTION_DATES_BY_COMPLEX = mapOf(
    "Меркурий" to mapOf(
        "Январь" to InspectionSchedule("15.01.2026"),
        "Февраль" to InspectionSchedule("12.02.2026"),
        "Март" to InspectionSchedule("12.03.2026"),
        "Апрель" to InspectionSchedule("16.04.2026"),
        "Май" to InspectionSchedule("14.05.2026"),
        "Июнь" to InspectionSchedule("11.06.2026"),
        "Июль" to InspectionSchedule("16.07.2026"),
        "Август" to InspectionSchedule("13.08.2026"),
        "Сентябрь" to InspectionSchedule("17.09.2026"),
        "Октябрь" to InspectionSchedule("15.10.2026"),
        "Ноябрь" to InspectionSchedule("12.11.2026"),
        "Декабрь" to InspectionSchedule("17.12.2026")
    ),
    "Континент" to mapOf(
        "Январь" to InspectionSchedule(
            date = "20.01.2026",
            details = listOf("Цоколь 1 и 2 очередь", "Паркинг")
        ),
        "Февраль" to InspectionSchedule(
            date = "17.02.2026",
            details = listOf("1 этаж 1 и 2 очередь", "2 этаж 2 очередь")
        ),
        "Март" to InspectionSchedule(
            date = "17.03.2026",
            details = listOf("3 этаж 1 и 2 очередь", "2 этаж 2 очередь", "Fun City")
        ),
        "Апрель" to InspectionSchedule(
            date = "21.04.2026",
            details = listOf("4 этаж 1 и 2 очередь")
        ),
        "Май" to InspectionSchedule(
            date = "19.05.2026",
            details = listOf("Цоколь 1 и 2 очередь", "Паркинг")
        ),
        "Июнь" to InspectionSchedule(
            date = "16.06.2026",
            details = listOf("1 этаж 1 и 2 очередь", "2 этаж 2 очередь")
        ),
        "Июль" to InspectionSchedule(
            date = "21.07.2026",
            details = listOf("3 этаж 1 и 2 очередь", "2 этаж 2 очередь", "Fun City")
        ),
        "Август" to InspectionSchedule(
            date = "18.08.2026",
            details = listOf("4 этаж 1 и 2 очередь")
        ),
        "Сентябрь" to InspectionSchedule(
            date = "22.09.2026",
            details = listOf("Цоколь 1 и 2 очередь", "Паркинг")
        ),
        "Октябрь" to InspectionSchedule(
            date = "20.10.2026",
            details = listOf("1 этаж 1 и 2 очередь", "2 этаж 2 очередь")
        ),
        "Ноябрь" to InspectionSchedule(
            date = "17.11.2026",
            details = listOf("3 этаж 1 и 2 очередь", "2 этаж 2 очередь", "Fun City")
        ),
        "Декабрь" to InspectionSchedule(
            date = "15.12.2026",
            details = listOf("4 этаж 1 и 2 очередь")
        )
    ),
    "Маршака" to mapOf(
        "Январь" to InspectionSchedule("27.01.2026"),
        "Февраль" to InspectionSchedule("24.02.2026"),
        "Март" to InspectionSchedule("24.03.2026"),
        "Апрель" to InspectionSchedule("28.04.2026"),
        "Май" to InspectionSchedule("26.05.2026"),
        "Июнь" to InspectionSchedule("23.06.2026"),
        "Июль" to InspectionSchedule("28.07.2026"),
        "Август" to InspectionSchedule("25.08.2026"),
        "Сентябрь" to InspectionSchedule("29.09.2026"),
        "Октябрь" to InspectionSchedule("27.10.2026"),
        "Ноябрь" to InspectionSchedule("24.11.2026"),
        "Декабрь" to InspectionSchedule("22.12.2026")
    )
)

private val MONTHS = listOf(
    "Январь",
    "Февраль",
    "Март",
    "Апрель",
    "Май",
    "Июнь",
    "Июль",
    "Август",
    "Сентябрь",
    "Октябрь",
    "Ноябрь",
    "Декабрь"
)

// Редактируйте праздничные нерабочие даты производственного календаря здесь.
// Выходные дни дополнительно определяются по субботе и воскресенью.
private val HOLIDAYS_2026 = setOf(
    "01.01.2026",
    "02.01.2026",
    "03.01.2026",
    "04.01.2026",
    "05.01.2026",
    "06.01.2026",
    "07.01.2026",
    "08.01.2026",
    "23.02.2026",
    "08.03.2026",
    "09.03.2026",
    "01.05.2026",
    "09.05.2026",
    "11.05.2026",
    "12.06.2026",
    "04.11.2026"
)

private val INSPECTION_DATE_FORMATTER =
    DateTimeFormatter.ofPattern("dd.MM.yyyy", Locale("ru"))
private val INSPECTION_LABEL_DATE_FORMATTER =
    DateTimeFormatter.ofPattern("dd.MM", Locale("ru"))

private fun nearestInspectionDate(
    complexName: String,
    today: LocalDate = LocalDate.now()
): LocalDate? {
    val dates = INSPECTION_DATES_BY_COMPLEX[complexName]
        ?.values
        ?.map { LocalDate.parse(it.date, INSPECTION_DATE_FORMATTER) }
        ?.sorted()
        .orEmpty()

    return dates.firstOrNull { !it.isBefore(today) } ?: dates.lastOrNull()
}

private const val NOTIFICATION_PREFS = "inspection_notifications"
private const val NOTIFICATION_TIME_KEY = "notification_time"
private const val DEFAULT_NOTIFICATION_TIME = "09:00"

private fun notificationPreferences(context: Context) =
    context.getSharedPreferences(NOTIFICATION_PREFS, Context.MODE_PRIVATE)

private fun notificationEnabled(context: Context, complexName: String): Boolean =
    notificationPreferences(context).getBoolean("enabled_$complexName", false)

private fun notificationDate(complexName: String, today: LocalDate = LocalDate.now()): LocalDate? =
    INSPECTION_DATES_BY_COMPLEX[complexName]
        ?.values
        ?.map { LocalDate.parse(it.date, INSPECTION_DATE_FORMATTER) }
        ?.sorted()
        ?.firstOrNull { !it.isBefore(today) }

private fun scheduleInspectionNotification(
    context: Context,
    complexName: String,
    enabled: Boolean
) {
    val alarmManager = context.getSystemService(AlarmManager::class.java)
    val intent = Intent(context, InspectionNotificationReceiver::class.java).apply {
        putExtra(InspectionNotificationReceiver.EXTRA_COMPLEX_NAME, complexName)
        putExtra(
            InspectionNotificationReceiver.EXTRA_DATE,
            notificationDate(complexName)?.format(INSPECTION_DATE_FORMATTER)
        )
    }
    val requestCode = complexName.hashCode()
    val pendingIntent = PendingIntent.getBroadcast(
        context,
        requestCode,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    alarmManager.cancel(pendingIntent)
    if (!enabled) return

    val date = notificationDate(complexName) ?: return
    val timeParts = notificationPreferences(context)
        .getString(NOTIFICATION_TIME_KEY, DEFAULT_NOTIFICATION_TIME)
        .orEmpty()
        .split(":")
    val hour = timeParts.getOrNull(0)?.toIntOrNull() ?: 9
    val minute = timeParts.getOrNull(1)?.toIntOrNull() ?: 0
    val calendar = Calendar.getInstance().apply {
        set(date.year, date.monthValue - 1, date.dayOfMonth - 1, hour, minute, 0)
        set(Calendar.MILLISECOND, 0)
    }
    if (calendar.timeInMillis <= System.currentTimeMillis()) return
    alarmManager.setAndAllowWhileIdle(
        AlarmManager.RTC_WAKEUP,
        calendar.timeInMillis,
        pendingIntent
    )
}

private fun rescheduleEnabledNotifications(context: Context, complexes: List<Complex>) {
    complexes.forEach { complex ->
        scheduleInspectionNotification(context, complex.name, notificationEnabled(context, complex.name))
    }
}

class MainViewModel(private val db: AppDatabase) : ViewModel() {

    val complexes: Flow<List<Complex>> = db.complexDao().all()

    fun seed() {
        viewModelScope.launch(Dispatchers.IO) {
            if (db.complexDao().count() == 0) {
                listOf(
                    "Меркурий",
                    "Континент",
                    "Маршака"
                ).forEach { name ->
                    db.complexDao().insert(
                        Complex(name = name)
                    )
                }
            }
        }
    }
}

class MainViewModelFactory(
    private val db: AppDatabase
) : androidx.lifecycle.ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {
        return MainViewModel(db) as T
    }
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val db = AppDatabase.get(this)

        setContent {

            val vm: MainViewModel =
                viewModel(
                    factory = MainViewModelFactory(db)
                )

            LaunchedEffect(Unit) {
                vm.seed()
            }

            Dashboard(vm.complexes)
        }
    }
}

@Composable
fun Dashboard(
    complexes: Flow<List<Complex>>
) {

    val list by complexes.collectAsState(
        initial = emptyList()
    )
    val context = LocalContext.current

    // Убираем дубликаты комплексов по названию
    val uniqueList = list.distinctBy {
        it.name.trim()
    }

    var selectedScreen by remember {
        mutableStateOf(0)
    }

    var selectedComplex by remember {
        mutableStateOf<Complex?>(null)
    }

    var selectedMonthIndex by remember {
        mutableStateOf<Int?>(null)
    }

    var selectedInspection by remember {
        mutableStateOf<InspectionSchedule?>(null)
    }

    var searchText by remember {
        mutableStateOf("")
    }
    var notificationTime by remember {
        mutableStateOf(
            notificationPreferences(context)
                .getString(NOTIFICATION_TIME_KEY, DEFAULT_NOTIFICATION_TIME)
                ?: DEFAULT_NOTIFICATION_TIME
        )
    }
    var showTimeSettings by remember { mutableStateOf(false) }

    LaunchedEffect(list) {
        rescheduleEnabledNotifications(context, list)
    }

    MaterialTheme {

        Scaffold(

            bottomBar = {

                NavigationBar {

                    NavigationBarItem(
                        selected = selectedScreen == 0,
                        onClick = {
                            selectedScreen = 0
                        },
                        icon = {},
                        label = {
                            Text("Главная")
                        }
                    )

                    NavigationBarItem(
                        selected = selectedScreen == 1,
                        onClick = {
                            selectedScreen = 1
                        },
                        icon = {},
                        label = {
                            Text("Арендаторы")
                        }
                    )

                    NavigationBarItem(
                        selected = selectedScreen == 2,
                        onClick = {
                            selectedScreen = 2
                        },
                        icon = {},
                        label = {
                            Text("Работы")
                        }
                    )

                    NavigationBarItem(
                        selected = selectedScreen == 3,
                        onClick = {
                            selectedScreen = 3
                        },
                        icon = {},
                        label = {
                            Text("Неисправности")
                        }
                    )

                    NavigationBarItem(
                        selected = selectedScreen == 4,
                        onClick = {
                            selectedScreen = 4
                        },
                        icon = {},
                        label = {
                            Text("Ещё")
                        }
                    )
                }
            }

        ) { paddingValues ->

            when (selectedScreen) {

                0 -> {

                    HomeScreen(
                        list = uniqueList,
                        searchText = searchText,
                        onSearchChange = {
                            searchText = it
                        },
                        onOpenPlan = { complex, monthIndex ->
                            selectedComplex = complex
                            selectedMonthIndex = monthIndex
                        }
                    )
                }

                1 -> {

                    TenantsScreen(list = uniqueList)
                }

                2 -> {

                    SimpleScreen(
                        title = "РАБОТЫ",
                        text = "Здесь будут отображаться запланированные и выполняемые работы."
                    )
                }

                3 -> {

                    SimpleScreen(
                        title = "НЕИСПРАВНОСТИ",
                        text = "Здесь будут отображаться новые и текущие неисправности."
                    )
                }

                4 -> {

                    SettingsScreen(
                        notificationTime = notificationTime,
                        onOpenTimeSettings = { showTimeSettings = true }
                    )
                }
            }
        }
    }

    selectedComplex?.let { complex ->

        AlertDialog(

            onDismissRequest = {
                selectedComplex = null
                selectedMonthIndex = null
            },

            title = {
                Text("Календарь проверок — 2026")
            },

            text = {

                val calendarListState = rememberLazyListState()
                LaunchedEffect(complex.id, selectedMonthIndex) {
                    calendarListState.scrollToItem(selectedMonthIndex?.plus(1) ?: 0)
                }

                LazyColumn(
                    state = calendarListState,
                    modifier = Modifier
                        .heightIn(max = 600.dp)
                ) {

                    item {
                        if (complex.name != "Континент") {
                            Text(
                                complex.name,
                                style = MaterialTheme.typography.titleLarge
                            )

                            Spacer(
                                modifier = Modifier.height(12.dp)
                            )
                        }

                        Text(
                            "КАЛЕНДАРЬ КОМПЛЕКСНЫХ ПРОВЕРОК",
                            style = MaterialTheme.typography.labelSmall
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            WEEKDAYS.forEachIndexed { index, day ->
                                Text(
                                    text = day,
                                    modifier = Modifier.weight(1f),
                                    color = if (index >= 5) {
                                        Color.Red
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }

                    items(MONTHS.size) { monthIndex ->
                        MonthCalendar(
                            monthIndex = monthIndex,
                            monthName = MONTHS[monthIndex],
                            schedule = INSPECTION_DATES_BY_COMPLEX[complex.name]?.get(MONTHS[monthIndex]),
                            onInspectionClick = { inspection ->
                                selectedInspection = inspection
                            }
                        )
                    }
                }
            },

            confirmButton = {

                TextButton(
                    onClick = {
                        selectedComplex = null
                        selectedMonthIndex = null
                    }
                ) {
                    Text("Закрыть")
                }
            }
        )
    }

    selectedInspection?.let { inspection ->
        AlertDialog(
            onDismissRequest = {
                selectedInspection = null
            },
            title = if (inspection.details.isEmpty()) {
                { Text("Проверка ${inspection.date}") }
            } else {
                null
            },
            text = {
                if (inspection.details.isEmpty()) {
                    Text("Детали проверки не заданы.")
                } else {
                    Column {
                        inspection.details.forEach { detail ->
                            Text(detail)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedInspection = null
                    }
                ) {
                    Text("Закрыть")
                }
            }
        )
    }

    if (showTimeSettings) {
        TimeSettingsDialog(
            initialTime = notificationTime,
            onDismiss = { showTimeSettings = false },
            onSave = { time ->
                notificationTime = time
                notificationPreferences(context).edit()
                    .putString(NOTIFICATION_TIME_KEY, time)
                    .apply()
                rescheduleEnabledNotifications(context, uniqueList)
                showTimeSettings = false
            }
        )
    }
}

private val WEEKDAYS = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")

@Composable
private fun MonthCalendar(
    monthIndex: Int,
    monthName: String,
    schedule: InspectionSchedule?,
    onInspectionClick: (InspectionSchedule) -> Unit
) {
    val yearMonth = YearMonth.of(2026, monthIndex + 1)
    val firstDayOffset = (yearMonth.atDay(1).dayOfWeek.value + 6) % 7
    val scheduleDate = schedule?.let {
        LocalDate.parse(it.date, INSPECTION_DATE_FORMATTER)
    }
    val days = buildList<LocalDate?> {
        repeat(firstDayOffset) {
            add(null)
        }
        for (day in 1..yearMonth.lengthOfMonth()) {
            add(yearMonth.atDay(day))
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            Text(
                text = monthName,
                style = MaterialTheme.typography.titleMedium
            )

            days.chunked(7).forEach { week ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    week.forEach { date ->
                        CalendarDay(
                            date = date,
                            isInspectionDate = date != null && date == scheduleDate,
                            onClick = {
                                if (schedule != null) {
                                    onInspectionClick(schedule)
                                }
                            }
                        )
                    }
                    repeat(7 - week.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun RowScope.CalendarDay(
    date: LocalDate?,
    isInspectionDate: Boolean,
    onClick: () -> Unit
) {
    if (date == null) {
        Spacer(modifier = Modifier.weight(1f))
        return
    }

    val dateKey = date.format(INSPECTION_DATE_FORMATTER)
    val isWeekend = date.dayOfWeek.value >= 6
    val isHoliday = dateKey in HOLIDAYS_2026
    val isNonWorkingDay = isWeekend || isHoliday
    val textColor = when {
        isInspectionDate -> Color.White
        isNonWorkingDay -> Color.Red
        else -> MaterialTheme.colorScheme.onSurface
    }

    Box(
        modifier = Modifier
            .weight(1f)
            .padding(vertical = 2.dp)
            .aspectRatio(1f)
            .background(
                color = if (isInspectionDate) {
                    MaterialTheme.colorScheme.primary
                } else {
                    Color.Transparent
                }
            )
            .then(
                if (isInspectionDate && isNonWorkingDay) {
                    Modifier.border(2.dp, Color.Red)
                } else {
                    Modifier
                }
            )
            .clickable(enabled = isInspectionDate, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = date.dayOfMonth.toString(),
            color = textColor,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
fun HomeScreen(
    list: List<Complex>,
    searchText: String,
    onSearchChange: (String) -> Unit,
    onOpenPlan: (Complex, Int?) -> Unit
) {
    val context = LocalContext.current
    var refreshNotifications by remember { mutableStateOf(0) }
    var pendingEnable by remember { mutableStateOf<String?>(null) }
    val requestNotificationsPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        val complexName = pendingEnable
        pendingEnable = null
        if (granted && complexName != null) {
            notificationPreferences(context).edit()
                .putBoolean("enabled_$complexName", true)
                .apply()
            scheduleInspectionNotification(context, complexName, true)
            refreshNotifications++
        } else if (!granted) {
            Toast.makeText(
                context,
                "Разрешение на уведомления не выдано",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    val filteredList = list.filter {

        searchText.isBlank() ||
                it.name.contains(
                    searchText,
                    ignoreCase = true
                )
    }

    LazyColumn(

        modifier = Modifier
            .padding(16.dp),

        verticalArrangement =
            Arrangement.spacedBy(12.dp)

    ) {

        item {

            Text(
                "ТехКонтроль",
                style = MaterialTheme.typography.headlineMedium
            )

            Text(
                "Управление торговыми комплексами",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        item {

            OutlinedTextField(

                value = searchText,

                onValueChange = onSearchChange,

                modifier = Modifier.fillMaxWidth(),

                label = {
                    Text("Поиск арендатора или секции")
                },

                singleLine = true
            )
        }

        item {

            Text(
                "КОМПЛЕКСНЫЕ ПРОВЕРКИ",
                style = MaterialTheme.typography.titleLarge
            )
        }

        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = filteredList,
                    key = { it.id }
                ) { complex ->
                    val enabled = remember(refreshNotifications, complex.name) {
                        notificationEnabled(context, complex.name)
                    }
                    Card(
                        modifier = Modifier
                            .size(156.dp)
                            .clickable {
                                onOpenPlan(complex, null)
                            }
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                complex.name,
                                style = MaterialTheme.typography.titleMedium
                            )

                            Spacer(
                                modifier = Modifier.height(8.dp)
                            )

                            Text(
                                "КАЛЕНДАРЬ КОМПЛЕКСНЫХ ПРОВЕРОК",
                                style = MaterialTheme.typography.labelSmall
                            )

                            nearestInspectionDate(complex.name)?.let { inspectionDate ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = inspectionDate.format(INSPECTION_LABEL_DATE_FORMATTER),
                                        color = MaterialTheme.colorScheme.primary,
                                        style = MaterialTheme.typography.headlineMedium,
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable {
                                                onOpenPlan(
                                                    complex,
                                                    inspectionDate.monthValue - 1
                                                )
                                            }
                                    )
                                    Text(
                                        text = if (enabled) "🔔" else "🔕",
                                        color = if (enabled) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                        },
                                        modifier = Modifier
                                            .clickable {
                                                val newValue = !enabled
                                                if (
                                                    newValue &&
                                                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                                                    context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
                                                ) {
                                                    pendingEnable = complex.name
                                                    requestNotificationsPermission.launch(
                                                        Manifest.permission.POST_NOTIFICATIONS
                                                    )
                                                } else {
                                                    notificationPreferences(context).edit()
                                                        .putBoolean("enabled_${complex.name}", newValue)
                                                        .apply()
                                                    scheduleInspectionNotification(
                                                        context,
                                                        complex.name,
                                                        newValue
                                                    )
                                                    refreshNotifications++
                                                }
                                            }
                                            .padding(4.dp)
                                    )
                                }
                            }
                        }
                    }
                }

            }
        }

        item {

            Text(
                "ТРЕБУЕТ ВНИМАНИЯ",
                style = MaterialTheme.typography.titleLarge
            )

            Card(
                modifier = Modifier.fillMaxWidth()
            ) {

                Column(
                    modifier = Modifier.padding(16.dp)
                ) {

                    Text("Просроченные работы: 0")
                    Text("Неисправности: 0")
                    Text("Работы в процессе: 0")
                }
            }
        }
    }
}

@Composable
private fun SettingsScreen(
    notificationTime: String,
    onOpenTimeSettings: () -> Unit
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("ЕЩЁ", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(12.dp))
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Уведомления о проверках", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Время отправки: $notificationTime")
                TextButton(onClick = onOpenTimeSettings) {
                    Text("Изменить время")
                }
            }
        }
    }
}

@Composable
private fun TimeSettingsDialog(
    initialTime: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var time by remember { mutableStateOf(initialTime) }
    val valid = Regex("^([01][0-9]|2[0-3]):[0-5][0-9]$").matches(time)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Время уведомлений") },
        text = {
            OutlinedTextField(
                value = time,
                onValueChange = { time = it.take(5) },
                label = { Text("ЧЧ:ММ") },
                singleLine = true,
                supportingText = { Text("Например, 09:00") },
                isError = time.isNotEmpty() && !valid
            )
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } },
        confirmButton = {
            TextButton(enabled = valid, onClick = { onSave(time) }) {
                Text("Сохранить")
            }
        }
    )
}

@Composable
fun TenantsScreen(
    list: List<Complex>
) {

    LazyColumn(

        modifier = Modifier.padding(16.dp),

        verticalArrangement =
            Arrangement.spacedBy(12.dp)

    ) {

        item {

            Text(
                "АРЕНДАТОРЫ",
                style = MaterialTheme.typography.headlineMedium
            )

            Text(
                "Торговые комплексы"
            )
        }

        items(
            items = list,
            key = { it.id }
        ) { complex ->

            Card(
                modifier = Modifier
                    .fillMaxWidth()
            ) {

                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.Center
                ) {

                    Text(
                        complex.name,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}

@Composable
fun SimpleScreen(
    title: String,
    text: String
) {

    Column(
        modifier = Modifier.padding(16.dp)
    ) {

        Text(
            title,
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {

            Column(
                modifier = Modifier.padding(16.dp)
            ) {

                Text(text)

                Spacer(
                    modifier = Modifier.height(16.dp)
                )

                Button(
                    onClick = {}
                ) {
                    Text("Добавить")
                }
            }
        }
    }
}
