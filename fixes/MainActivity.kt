package com.example.tcmanager

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.OpenableColumns
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import android.net.Uri
import java.time.DateTimeException
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.format.ResolverStyle
import java.time.temporal.ChronoUnit
import java.util.Locale
import java.util.Calendar
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.room.withTransaction
import com.example.tcmanager.data.AppDatabase
import com.example.tcmanager.data.Complex
import com.example.tcmanager.data.TenantRecord
import com.example.tcmanager.data.TenantSeed
import com.example.tcmanager.data.ImportedTenant
import com.example.tcmanager.data.InspectionActAttachment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.zip.ZipInputStream
import android.util.Xml
import org.xmlpull.v1.XmlPullParser
import java.io.ByteArrayInputStream

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

    fun attachment(complexName: String, dateKey: String): Flow<InspectionActAttachment?> =
        db.inspectionActAttachmentDao().observe(complexName, dateKey)

    fun saveAttachment(
        complexName: String,
        dateKey: String,
        uri: String,
        displayName: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            db.inspectionActAttachmentDao().upsert(
                InspectionActAttachment(complexName, dateKey, uri, displayName)
            )
        }
    }

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

            Dashboard(vm)
        }
    }
}

@Composable
fun Dashboard(
    viewModel: MainViewModel
) {

    val list by viewModel.complexes.collectAsState(
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

    var notificationTime by remember {
        mutableStateOf(
            notificationPreferences(context)
                .getString(NOTIFICATION_TIME_KEY, DEFAULT_NOTIFICATION_TIME)
                ?: DEFAULT_NOTIFICATION_TIME
        )
    }
    var showTimeSettings by remember { mutableStateOf(false) }
    var pickerTarget by remember { mutableStateOf<Pair<String, String>?>(null) }
    val pickDocument = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        val target = pickerTarget
        pickerTarget = null
        if (uri != null && target != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: SecurityException) {
                // Providers without persistable permissions can still be opened now.
            }
            viewModel.saveAttachment(
                target.first,
                target.second,
                uri.toString(),
                displayNameForUri(context, uri)
            )
        }
    }

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
                        onOpenPlan = { complex, monthIndex ->
                            selectedComplex = complex
                            selectedMonthIndex = monthIndex
                        }
                    )
                }

                1 -> {

                    TenantsScreen(
                        list = uniqueList
                    )
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
        val complexName = selectedComplex?.name.orEmpty()
        val dateKey = inspection.date
        val attachment by viewModel.attachment(complexName, dateKey)
            .collectAsState(initial = null)
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
                Column {
                    if (inspection.details.isEmpty()) {
                        Text("Детали проверки не заданы.")
                    } else {
                        inspection.details.forEach { detail ->
                            Text(detail)
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        attachment?.let { "Акт: ${it.displayName}" } ?: "Акт не прикреплён",
                        color = if (attachment == null) {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        } else {
                            Color.Unspecified
                        }
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        pickerTarget = complexName to dateKey
                        pickDocument.launch(
                            arrayOf(
                                "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                            )
                        )
                    }
                ) {
                    Text("Прикрепить акт")
                }
            },
            confirmButton = {
                Row {
                    if (attachment != null) {
                        TextButton(
                            onClick = {
                                attachment?.let { openAttachment(context, it.uri) }
                            }
                        ) {
                            Text("Открыть акт")
                        }
                    }
                    TextButton(onClick = { selectedInspection = null }) {
                        Text("Закрыть")
                    }
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
                    items = list,
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

private fun normalizedSearchValue(value: String): String =
    value.filter { it.isLetterOrDigit() }.lowercase(Locale.ROOT)

private fun tenantMatchesSearch(tenant: TenantRecord, searchText: String): Boolean {
    val query = searchText.trim()
    if (query.isBlank()) {
        return false
    }

    val normalizedQuery = normalizedSearchValue(query)
    return listOf(
        tenant.section,
        tenant.floorOrType,
        tenant.tenant,
        tenant.brand,
        tenant.activity,
        tenant.phone,
        tenant.email
    ).any { value ->
        value.isNotBlank() &&
                (
                    value.contains(query, ignoreCase = true) ||
                        (
                            normalizedQuery.isNotBlank() &&
                                normalizedSearchValue(value).contains(normalizedQuery)
                            )
                    )
    }
}

private enum class LeaseStatus {
    EXPIRING_30,
    EXPIRING_60,
    EXPIRING_90,
    EXPIRED,
    NOT_SOON,
    UNKNOWN
}

private enum class LeaseStatusFilter(val label: String) {
    ALL("Все"),
    SOON("Скоро"),
    EXPIRED("Просрочено"),
    NOT_SOON("Не скоро")
}

private val LEASE_DATE_FORMATTERS = listOf(
    DateTimeFormatter.ofPattern("dd.MM.uuuu").withResolverStyle(ResolverStyle.STRICT),
    DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT),
    DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(ResolverStyle.STRICT)
)

private fun parseLeaseEnd(value: String): LocalDate? {
    val normalized = value.trim().trim('"', '\'')
    if (normalized.isBlank()) {
        return null
    }

    return LEASE_DATE_FORMATTERS.firstNotNullOfOrNull { formatter ->
        try {
            LocalDate.parse(normalized, formatter)
        } catch (_: DateTimeParseException) {
            null
        }
    }
}

private fun leaseStatus(
    leaseEnd: String,
    today: LocalDate = LocalDate.now()
): LeaseStatus {
    val endDate = parseLeaseEnd(leaseEnd) ?: return LeaseStatus.UNKNOWN
    val daysUntilEnd = ChronoUnit.DAYS.between(today, endDate)
    return when {
        daysUntilEnd < 0 -> LeaseStatus.EXPIRED
        daysUntilEnd <= 30 -> LeaseStatus.EXPIRING_30
        daysUntilEnd <= 60 -> LeaseStatus.EXPIRING_60
        daysUntilEnd <= 90 -> LeaseStatus.EXPIRING_90
        else -> LeaseStatus.NOT_SOON
    }
}

private fun leaseStatusColor(status: LeaseStatus): Color =
    when (status) {
        LeaseStatus.EXPIRING_30, LeaseStatus.EXPIRED -> Color(0xFFD32F2F)
        LeaseStatus.EXPIRING_60 -> Color(0xFFEF6C00)
        LeaseStatus.EXPIRING_90 -> Color(0xFFF9A825)
        LeaseStatus.NOT_SOON, LeaseStatus.UNKNOWN -> Color.Transparent
    }

private fun leaseStatusLabel(status: LeaseStatus): String =
    when (status) {
        LeaseStatus.EXPIRING_30 -> "Истекает ≤ 30 дней"
        LeaseStatus.EXPIRING_60 -> "Истекает ≤ 60 дней"
        LeaseStatus.EXPIRING_90 -> "Истекает ≤ 90 дней"
        LeaseStatus.EXPIRED -> "Просрочен"
        LeaseStatus.NOT_SOON -> "Не скоро"
        LeaseStatus.UNKNOWN -> "Дата не указана"
    }

private fun leaseStatusMatchesFilter(
    tenant: TenantRecord,
    filter: LeaseStatusFilter
): Boolean {
    val status = leaseStatus(tenant.leaseEnd)
    return when (filter) {
        LeaseStatusFilter.ALL -> true
        LeaseStatusFilter.SOON -> status == LeaseStatus.EXPIRING_30 ||
            status == LeaseStatus.EXPIRING_60 ||
            status == LeaseStatus.EXPIRING_90
        LeaseStatusFilter.EXPIRED -> status == LeaseStatus.EXPIRED
        LeaseStatusFilter.NOT_SOON -> status == LeaseStatus.NOT_SOON
    }
}

@Composable
private fun TenantsFilterDialog(
    filters: TenantFilters,
    allSections: List<String>,
    sectionsByFloor: Map<String, Set<String>>,
    allFloors: List<String>,
    allActivities: List<String>,
    leaseStatusFilters: List<LeaseStatusFilter>,
    onFiltersChanged: (TenantFilters) -> Unit,
    onDismiss: () -> Unit
) {
    val availableSections = if (filters.floors.isEmpty()) {
        allSections
    } else {
        sectionsByFloor
            .filterKeys { it in filters.floors }
            .values
            .flatten()
            .distinct()
            .sorted()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Фильтры") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Срок договора", style = MaterialTheme.typography.titleSmall)
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(leaseStatusFilters) { statusFilter ->
                        Button(
                            onClick = {
                                onFiltersChanged(
                                    filters.copy(leaseStatusFilter = statusFilter)
                                )
                            },
                            modifier = Modifier
                                .widthIn(min = 64.dp)
                                .heightIn(min = 32.dp)
                        ) {
                            Text(
                                if (filters.leaseStatusFilter == statusFilter) {
                                    "✓ ${statusFilter.label}"
                                } else {
                                    statusFilter.label
                                },
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }

                if (availableSections.isNotEmpty()) {
                    Text("Секция", style = MaterialTheme.typography.titleSmall)
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(availableSections) { section ->
                            Button(
                                onClick = {
                                    val newSections = filters.sections.toMutableSet()
                                    if (section in newSections) {
                                        newSections.remove(section)
                                    } else {
                                        newSections.add(section)
                                    }
                                    onFiltersChanged(filters.copy(sections = newSections))
                                },
                                modifier = Modifier
                                    .widthIn(min = 64.dp)
                                    .heightIn(min = 32.dp)
                            ) {
                                Text(
                                    if (section in filters.sections) "✓ $section" else section,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }
                }

                if (allFloors.isNotEmpty()) {
                    Text("Этаж/тип помещения", style = MaterialTheme.typography.titleSmall)
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 150.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(allFloors) { floor ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        val newFloors = filters.floors.toMutableSet()
                                        if (floor in newFloors) {
                                            newFloors.remove(floor)
                                        } else {
                                            newFloors.add(floor)
                                        }
                                        val availableSectionsForFloors = if (newFloors.isEmpty()) {
                                            allSections.toSet()
                                        } else {
                                            sectionsByFloor
                                                .filterKeys { it in newFloors }
                                                .values
                                                .flatten()
                                                .toSet()
                                        }
                                        onFiltersChanged(
                                            filters.copy(
                                                floors = newFloors,
                                                sections = filters.sections.intersect(availableSectionsForFloors)
                                            )
                                        )
                                    }
                                    .padding(8.dp),
                                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    if (floor in filters.floors) "☑" else "☐",
                                    modifier = Modifier.size(20.dp),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(floor, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }

                if (allActivities.isNotEmpty()) {
                    Text("Вид деятельности", style = MaterialTheme.typography.titleSmall)
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 200.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(allActivities) { activity ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        val newActivities = filters.activities.toMutableSet()
                                        if (activity in newActivities) {
                                            newActivities.remove(activity)
                                        } else {
                                            newActivities.add(activity)
                                        }
                                        onFiltersChanged(filters.copy(activities = newActivities))
                                    }
                                    .padding(8.dp),
                                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    if (activity in filters.activities) "☑" else "☐",
                                    modifier = Modifier.size(20.dp),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(activity, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }

                Text("Наличие контактов", style = MaterialTheme.typography.titleSmall)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onFiltersChanged(filters.copy(hasPhone = !filters.hasPhone))
                        }
                        .padding(8.dp),
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        if (filters.hasPhone) "☑" else "☐",
                        modifier = Modifier.size(20.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text("Имеется телефон", style = MaterialTheme.typography.bodySmall)
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onFiltersChanged(filters.copy(hasEmail = !filters.hasEmail))
                        }
                        .padding(8.dp),
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        if (filters.hasEmail) "☑" else "☐",
                        modifier = Modifier.size(20.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text("Имеется email", style = MaterialTheme.typography.bodySmall)
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onFiltersChanged(filters.copy(hasAddress = !filters.hasAddress))
                        }
                        .padding(8.dp),
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        if (filters.hasAddress) "☑" else "☐",
                        modifier = Modifier.size(20.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text("Имеется адрес", style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = {
                onFiltersChanged(TenantFilters())
                onDismiss()
            }) {
                Text("Сброс")
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("ОК")
            }
        }
    )
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
        Spacer(modifier = Modifier.height(12.dp))
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("О приложении", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Версия: v${BuildConfig.VERSION_NAME}")
                Text("Разработчик: Новиков А.С.")
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

private data class TenantImportSkippedRow(val rowNumber: Int, val reason: String)
private data class TenantImportPreview(
    val rows: List<TenantRecord>,
    val total: Int,
    val sourceRows: Int,
    val skipped: List<TenantImportSkippedRow>
)

private fun normalizeHeader(value: String): String =
    value.lowercase(Locale("ru")).replace("ё", "е").replace(Regex("[^а-яa-z0-9]"), "")

private fun importedSectionAndType(value: String): Pair<String, String> {
    val source = value.trim()
    val beforeSlash = source.substringBefore('/').trim()
    val afterSlash = source.substringAfter('/', "").trim()
    val section = if (beforeSlash.length == 1 && beforeSlash.uppercase() in setOf("Ц", "П", "С") &&
        afterSlash.any(Char::isDigit)
    ) source else beforeSlash
    val upper = source.uppercase()
    val type = when {
        upper == "88-58" -> "склады"
        upper == "88-91" -> "3 этаж"
        upper == "СТ-8" -> "4 этаж, помещение РТС"
        upper == "П4" -> "4 этаж, автомойка, паркинг"
        upper == "П-1" -> "1 этаж, авторемонт, паркинг"
        upper == "О" -> "4 этаж, офис"
        upper.startsWith("СТ-") -> "помещение РТС"
        upper.startsWith("ЦБ") || upper.contains("ЦБ") -> "банкомат"
        upper.startsWith("Ц") || afterSlash.contains("Ц", ignoreCase = true) -> "цоколь"
        upper.startsWith("П") || afterSlash.contains("П", ignoreCase = true) -> "паркинг"
        upper.startsWith("С") || afterSlash.contains("С", ignoreCase = true) -> "склады"
        afterSlash.contains("О", ignoreCase = true) || beforeSlash.endsWith("-О", ignoreCase = true) -> "офис"
        else -> ""
    }
    return section to type
}

private fun normalizeImportedLeaseDate(value: String): String {
    val normalized = value.trim().trim('"', '\'')
    val serial = normalized.toDoubleOrNull()
    if (serial != null && serial >= 1 && serial <= 2958465) {
        return try {
            LocalDate.of(1899, 12, 30)
                .plusDays(serial.toLong())
                .format(DateTimeFormatter.ofPattern("dd.MM.uuuu"))
        } catch (_: DateTimeException) {
            normalized
        }
    }
    return normalized
}

private fun parseTenantXlsx(context: Context, uri: Uri): TenantImportPreview {
    val entries = mutableMapOf<String, ByteArray>()
    context.contentResolver.openInputStream(uri)?.use { input ->
        ZipInputStream(input).use { zip ->
            var entry = zip.nextEntry
            while (entry != null) {
                if (!entry.isDirectory) entries[entry.name] = zip.readBytes()
                entry = zip.nextEntry
            }
        }
    } ?: error("Не удалось открыть файл")
    val shared = mutableListOf<String>()
    entries["xl/sharedStrings.xml"]?.let { bytes ->
        val parser = Xml.newPullParser().apply { setInput(ByteArrayInputStream(bytes), "UTF-8") }
        var text = ""
        while (parser.next() != XmlPullParser.END_DOCUMENT) {
            if (parser.eventType == XmlPullParser.TEXT) text += parser.text
            if (parser.eventType == XmlPullParser.END_TAG && parser.name == "si") {
                shared += text.trim(); text = ""
            }
        }
    }
    val sheet = entries["xl/worksheets/sheet1.xml"] ?: error("В XLSX не найден первый лист")
    data class RawRow(val number: Int, val values: List<String>)
    val rows = mutableListOf<RawRow>()
    val parser = Xml.newPullParser().apply { setInput(ByteArrayInputStream(sheet), "UTF-8") }
    var current = mutableMapOf<Int, String>()
    var rowNumber = 0
    var cellIndex = 0
    var cellType = ""
    var value = ""
    var inValue = false
    fun columnNumber(ref: String): Int {
        val letters = ref.takeWhile { it.isLetter() }
        return letters.fold(0) { n, c -> n * 26 + (c.uppercaseChar().code - 'A'.code + 1) }
    }
    while (parser.next() != XmlPullParser.END_DOCUMENT) {
        when (parser.eventType) {
            XmlPullParser.START_TAG -> when (parser.name) {
                "row" -> {
                    current = mutableMapOf()
                    rowNumber = parser.getAttributeValue(null, "r")?.toIntOrNull() ?: (rowNumber + 1)
                }
                "c" -> {
                    cellIndex = columnNumber(parser.getAttributeValue(null, "r") ?: "A$rowNumber")
                    cellType = parser.getAttributeValue(null, "t") ?: ""
                    value = ""; inValue = false
                }
                "v", "t" -> if (cellType != "inlineStr" || parser.name == "t") inValue = true
            }
            XmlPullParser.TEXT -> if (inValue) value += parser.text
            XmlPullParser.END_TAG -> when (parser.name) {
                "v", "t" -> inValue = false
                "c" -> {
                    val resolved = if (cellType == "s") shared.getOrNull(value.trim().toIntOrNull() ?: -1).orEmpty() else value
                    current[cellIndex] = resolved.trim()
                }
                "row" -> if (current.isNotEmpty()) {
                    val max = current.keys.maxOrNull() ?: 0
                    rows += RawRow(rowNumber, (1..max).map { current[it].orEmpty() })
                }
            }
        }
    }
    if (rows.isEmpty()) error("Лист XLSX пуст")
    val aliases = mapOf(
        "section" to setOf("номер", "секция", "секцияпомещение", "помещение", "section", "номерпомещения", "номерсекции", "секцияномер"),
        "floor" to setOf("этаж", "этажтип", "этажтиппомещения", "этажтиппомещения", "floor"),
        "lease" to setOf("окончаниедоговора", "срокдоговора", "датаокончания", "датаокончаниядоговора", "leaseend", "дата"),
        "tenant" to setOf("арендатор", "наименованиеарендатора", "наименование", "tenant", "контрагент"),
        "brand" to setOf("бренд", "торговаямарка", "торговыйбренд", "brand"),
        "activity" to setOf("виддеятельности", "виддеятельностиарендатора", "деятельность", "activity"),
        "phone" to setOf("телефон", "телефоны", "телефонарендатора", "phone"),
        "email" to setOf("email", "электроннаяпочта", "почта", "emailарендатора"),
        "address" to setOf("адрес", "адресарендатора", "address")
    )
    val headers = rows.first().values.map { normalizeHeader(it) }
    val columns = aliases.mapValues { (_, names) -> headers.indexOfFirst { it in names } }.toMutableMap()
    val sourceFormat = headers.getOrNull(0) == "номер" &&
        headers.getOrNull(2) in aliases.getValue("tenant")
    if (sourceFormat) {
        mapOf(
            "section" to 0,
            "lease" to 1,
            "tenant" to 2,
            "brand" to 3,
            "activity" to 4,
            "phone" to 5,
            "email" to 6,
            "address" to 7
        ).forEach { (key, index) ->
            if ((columns[key] ?: -1) < 0 && index < headers.size) columns[key] = index
        }
    }
    if (columns["section"] ?: -1 < 0 || columns["tenant"] ?: -1 < 0)
        error("Не найдены обязательные заголовки. Ожидаются «Номер» (или «Секция») и «Наименование арендатора» (или «Арендатор»).")
    fun value(row: List<String>, key: String) = row.getOrNull(columns[key] ?: -1).orEmpty().trim()
    val skipped = mutableListOf<TenantImportSkippedRow>()
    val records = rows.drop(1).mapNotNull { rawRow ->
        val row = rawRow.values
        val (section, sourceType) = importedSectionAndType(value(row, "section"))
        val tenant = value(row, "tenant")
        if (section.isBlank() || tenant.isBlank()) {
            if (row.any { it.isNotBlank() }) {
                skipped += TenantImportSkippedRow(
                    rawRow.number,
                    when {
                        section.isBlank() && tenant.isBlank() -> "пустые «Номер» и «Наименование арендатора»"
                        section.isBlank() -> "пустой «Номер»"
                        else -> "пустое «Наименование арендатора»"
                    }
                )
            }
            null
        }
        else TenantRecord(
            section,
            value(row, "floor").ifBlank { sourceType },
            normalizeImportedLeaseDate(value(row, "lease")),
            tenant,
            value(row, "brand"),
            value(row, "activity"),
            value(row, "phone"),
            value(row, "email"),
            value(row, "address")
        )
    }
    if (records.isEmpty()) error("В XLSX нет строк арендаторов")
    return TenantImportPreview(records, records.size, rows.size - 1, skipped)
}

private data class TenantMergeSummary(val added: Int, val updated: Int, val skipped: Int)

private suspend fun mergeTenantImport(db: AppDatabase, rows: List<TenantRecord>): TenantMergeSummary {
    return db.withTransaction {
        val dao = db.tenantImportDao()
        val existing = dao.all().associateBy { it.section }
        val current = if (existing.isEmpty()) TenantSeed.all.map { ImportedTenant.from(it) } else existing.values.toList()
        dao.backup(current.map {
            com.example.tcmanager.data.TenantImportBackup(
                backedUpAt = System.currentTimeMillis(), section = it.section, tenant = it.tenant,
                floorOrType = it.floorOrType, leaseEnd = it.leaseEnd, brand = it.brand,
                activity = it.activity, phone = it.phone, email = it.email, address = it.address
            )
        })
        var added = 0; var updated = 0; var skipped = 0
        rows.forEach { record ->
            val old = existing[record.section]
            when {
                old == null -> { added++; dao.upsert(ImportedTenant.from(record)) }
                old.record() == record -> skipped++
                else -> { updated++; dao.upsert(ImportedTenant.from(record)) }
            }
        }
        TenantMergeSummary(added, updated, skipped)
    }
}

@Composable
fun TenantsScreen(
    list: List<Complex>,
    initialSearchText: String = "",
    openComplex: String? = null
) {
    var selectedComplex by remember(openComplex) {
        mutableStateOf(openComplex)
    }

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
                    .clickable(enabled = complex.name == "Континент") {
                        selectedComplex = complex.name
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
                    if (complex.name == "Континент") {
                        Text(
                            "${TenantSeed.all.size} арендаторов • Нажмите, чтобы открыть список",
                            style = MaterialTheme.typography.bodySmall
                        )
                    } else {
                        Text(
                            "Данные арендаторов пока не загружены",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }

    if (selectedComplex == "Континент") {
        TenantListDialog(
            initialSearchText = initialSearchText,
            onDismiss = { selectedComplex = null }
        )
    }
}

private data class TenantFilters(
    val sections: Set<String> = emptySet(),
    val floors: Set<String> = emptySet(),
    val activities: Set<String> = emptySet(),
    val leaseStatusFilter: LeaseStatusFilter = LeaseStatusFilter.ALL,
    val hasPhone: Boolean = false,
    val hasEmail: Boolean = false,
    val hasAddress: Boolean = false
) {
    fun isEmpty(): Boolean =
        sections.isEmpty() && floors.isEmpty() && activities.isEmpty() &&
        leaseStatusFilter == LeaseStatusFilter.ALL &&
        !hasPhone && !hasEmail && !hasAddress

    fun countActive(): Int =
        sections.size + floors.size + activities.size +
        (if (leaseStatusFilter != LeaseStatusFilter.ALL) 1 else 0) +
        (if (hasPhone) 1 else 0) + (if (hasEmail) 1 else 0) + (if (hasAddress) 1 else 0)
}

private fun tenantMatchesFilters(tenant: TenantRecord, filters: TenantFilters): Boolean {
    if (filters.isEmpty()) return true

    if (filters.sections.isNotEmpty() && tenant.section !in filters.sections) return false
    if (filters.floors.isNotEmpty() && tenant.floorOrType !in filters.floors) return false
    if (filters.activities.isNotEmpty() && tenant.activity !in filters.activities) return false
    if (!leaseStatusMatchesFilter(tenant, filters.leaseStatusFilter)) return false
    if (filters.hasPhone && tenant.phone.isBlank()) return false
    if (filters.hasEmail && tenant.email.isBlank()) return false
    if (filters.hasAddress && tenant.address.isBlank()) return false

    return true
}

@Composable
private fun TenantListDialog(
    initialSearchText: String = "",
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val db = remember { AppDatabase.get(context) }
    val imported by db.tenantImportDao().observe().collectAsState(initial = emptyList())
    val tenants = remember(imported) { if (imported.isEmpty()) TenantSeed.all else imported.map { it.record() } }
    var importPreview by remember { mutableStateOf<TenantImportPreview?>(null) }
    var importError by remember { mutableStateOf<String?>(null) }
    var importSummary by remember { mutableStateOf<TenantMergeSummary?>(null) }
    val openXlsx = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        kotlinx.coroutines.CoroutineScope(Dispatchers.IO).launch {
            runCatching { parseTenantXlsx(context, uri) }
                .onSuccess { preview -> withContext(Dispatchers.Main) { importPreview = preview } }
                .onFailure { error -> withContext(Dispatchers.Main) { importError = error.message ?: "Ошибка разбора XLSX" } }
        }
    }
    var searchText by remember(initialSearchText) {
        mutableStateOf(initialSearchText)
    }
    var selectedTenant by remember { mutableStateOf<TenantRecord?>(null) }
    var filters by remember { mutableStateOf(TenantFilters()) }
    var showFiltersDialog by remember { mutableStateOf(false) }

    val displayedTenants = tenants
        .filter { tenant -> tenantMatchesFilters(tenant, filters) }
        .filter { tenant ->
            searchText.isBlank() || tenantMatchesSearch(tenant, searchText)
        }

    val allSections = remember(tenants) { tenants.map { it.section }.distinct().sorted() }
    val sectionsByFloor = remember(tenants) {
        tenants
            .groupBy { it.floorOrType }
            .mapValues { (_, tenants) -> tenants.map { it.section }.toSet() }
    }
    val allFloors = remember(tenants) { tenants.map { it.floorOrType }.filter { it.isNotBlank() }.distinct().sorted() }
    val allActivities = remember(tenants) { tenants.map { it.activity }.filter { it.isNotBlank() }.distinct().sorted() }
    val leaseStatusFilters = remember { LeaseStatusFilter.values().toList() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    "Арендаторы — Континент",
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.headlineSmall
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { showFiltersDialog = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            "Фильтры${if (filters.countActive() > 0) " (${filters.countActive()})" else ""}",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Button(
                        onClick = {
                            openXlsx.launch(
                                arrayOf(
                                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                                )
                            )
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Импорт XLSX", maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 520.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = searchText,
                    onValueChange = { searchText = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Поиск по арендатору, бренду или секции") },
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (filters.countActive() > 0) {
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (filters.sections.isNotEmpty()) {
                            items(filters.sections.sorted()) { section ->
                                Card(
                                    modifier = Modifier
                                        .border(1.dp, MaterialTheme.colorScheme.primary)
                                        .padding(4.dp)
                                ) {
                                    Text(
                                        section,
                                        modifier = Modifier.padding(6.dp),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        }
                        if (filters.floors.isNotEmpty()) {
                            items(filters.floors.sorted()) { floor ->
                                Card(
                                    modifier = Modifier
                                        .border(1.dp, MaterialTheme.colorScheme.secondary)
                                        .padding(4.dp)
                                ) {
                                    Text(
                                        "Эт: $floor",
                                        modifier = Modifier.padding(6.dp),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        }
                        if (filters.activities.isNotEmpty()) {
                            items(filters.activities.sorted()) { activity ->
                                Card(
                                    modifier = Modifier
                                        .border(1.dp, MaterialTheme.colorScheme.tertiary)
                                        .padding(4.dp)
                                ) {
                                    Text(
                                        activity.take(20),
                                        modifier = Modifier.padding(6.dp),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        }
                        if (filters.leaseStatusFilter != LeaseStatusFilter.ALL) {
                            item {
                                Card(
                                    modifier = Modifier
                                        .border(
                                            1.dp,
                                            leaseStatusColor(
                                                when (filters.leaseStatusFilter) {
                                                    LeaseStatusFilter.SOON -> LeaseStatus.EXPIRING_90
                                                    LeaseStatusFilter.EXPIRED -> LeaseStatus.EXPIRED
                                                    LeaseStatusFilter.NOT_SOON -> LeaseStatus.NOT_SOON
                                                    LeaseStatusFilter.ALL -> LeaseStatus.UNKNOWN
                                                }
                                            )
                                        )
                                        .padding(4.dp)
                                ) {
                                    Text(
                                        filters.leaseStatusFilter.label,
                                        modifier = Modifier.padding(6.dp),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        }
                        if (filters.hasPhone) {
                            item {
                                Card(
                                    modifier = Modifier
                                        .border(1.dp, MaterialTheme.colorScheme.onBackground)
                                        .padding(4.dp)
                                ) {
                                    Text(
                                        "Телефон",
                                        modifier = Modifier.padding(6.dp),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        }
                        if (filters.hasEmail) {
                            item {
                                Card(
                                    modifier = Modifier
                                        .border(1.dp, MaterialTheme.colorScheme.onBackground)
                                        .padding(4.dp)
                                ) {
                                    Text(
                                        "Email",
                                        modifier = Modifier.padding(6.dp),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        }
                        if (filters.hasAddress) {
                            item {
                                Card(
                                    modifier = Modifier
                                        .border(1.dp, MaterialTheme.colorScheme.onBackground)
                                        .padding(4.dp)
                                ) {
                                    Text(
                                        "Адрес",
                                        modifier = Modifier.padding(6.dp),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        }
                    }
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (displayedTenants.isEmpty()) {
                        Text(
                            if (filters.leaseStatusFilter == LeaseStatusFilter.ALL) {
                                "Ничего не найдено"
                            } else {
                                "Нет арендаторов для статуса «${filters.leaseStatusFilter.label}» " +
                                    "с выбранными дополнительными фильтрами"
                            }
                        )
                    } else {
                        displayedTenants.forEach { tenant ->
                            val status = leaseStatus(tenant.leaseEnd)
                            val statusColor = leaseStatusColor(status)
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .then(
                                        if (statusColor != Color.Transparent) {
                                            Modifier
                                                .background(statusColor.copy(alpha = 0.10f))
                                                .border(1.dp, statusColor.copy(alpha = 0.65f))
                                        } else {
                                            Modifier
                                        }
                                    )
                                    .clickable { selectedTenant = tenant }
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        tenant.section,
                                        modifier = Modifier.clickable {
                                            selectedTenant = tenant
                                        },
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(tenant.tenant, style = MaterialTheme.typography.titleSmall)
                                    if (tenant.brand.isNotBlank()) {
                                        Text(tenant.brand, style = MaterialTheme.typography.bodyMedium)
                                    }
                                    if (tenant.activity.isNotBlank() || tenant.floorOrType.isNotBlank()) {
                                        Text(
                                            listOf(tenant.activity, tenant.floorOrType)
                                                .filter { it.isNotBlank() }
                                                .joinToString(" • "),
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                    if (tenant.leaseEnd.isNotBlank()) {
                                        Text(
                                            "${tenant.leaseEnd} • ${leaseStatusLabel(status)}",
                                            modifier = Modifier
                                                .padding(top = 4.dp)
                                                .then(
                                                    if (statusColor != Color.Transparent) {
                                                        Modifier
                                                            .background(statusColor.copy(alpha = 0.18f))
                                                            .padding(horizontal = 6.dp, vertical = 3.dp)
                                                    } else {
                                                        Modifier
                                                    }
                                                ),
                                            color = if (statusColor != Color.Transparent) {
                                                statusColor
                                            } else {
                                                Color.Unspecified
                                            },
                                            style = MaterialTheme.typography.labelMedium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Закрыть") } }
    )

    if (showFiltersDialog) {
        TenantsFilterDialog(
            filters = filters,
            allSections = allSections,
            sectionsByFloor = sectionsByFloor,
            allFloors = allFloors,
            allActivities = allActivities,
            leaseStatusFilters = leaseStatusFilters,
            onFiltersChanged = { filters = it },
            onDismiss = { showFiltersDialog = false }
        )
    }

    selectedTenant?.let { tenant ->
        val context = LocalContext.current
        val emailContacts = tenantEmailContacts(tenant.email)
        val phoneContacts = tenantPhoneContacts(tenant.phone)
        val address = tenant.address.trim().trim('"', '\'')
        val addressIntent = address.takeIf(::isMapAddress)?.let(::mapIntent)
        AlertDialog(
            onDismissRequest = { selectedTenant = null },
            title = { Text(tenant.tenant) },
            text = {
                Column(
                    modifier = Modifier
                        .heightIn(max = 520.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    TenantDetailField("Секция", tenant.section)
                    TenantDetailField("Этаж/тип помещения", tenant.floorOrType)
                    TenantLeaseEndField(tenant)
                    TenantDetailField("Бренд", tenant.brand)
                    TenantDetailField("Вид деятельности", tenant.activity)
                    TenantPhoneFields(
                        originalValue = tenant.phone,
                        contacts = phoneContacts,
                        context = context
                    )
                    TenantEmailFields(
                        originalValue = tenant.email,
                        contacts = emailContacts,
                        context = context
                    )
                    TenantDetailField(
                        "Адрес",
                        tenant.address,
                        onClick = addressIntent?.let {
                            {
                                launchTenantIntent(
                                    context,
                                    it,
                                    "Не удалось открыть приложение карт"
                                )
                            }
                        }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedTenant = null }) { Text("Закрыть") }
            }
        )
    }
    importError?.let { message ->
        AlertDialog(
            onDismissRequest = { importError = null },
            title = { Text("Ошибка импорта") },
            text = { Text(message) },
            confirmButton = { TextButton(onClick = { importError = null }) { Text("OK") } }
        )
    }
    importPreview?.let { preview ->
        AlertDialog(
            onDismissRequest = { importPreview = null },
            title = { Text("Предпросмотр XLSX") },
            text = {
                Column {
                    Text("Найдено записей: ${preview.total} из строк данных: ${preview.sourceRows}")
                    preview.rows.take(5).forEach { row -> Text("${row.section} — ${row.tenant}", style = MaterialTheme.typography.bodySmall) }
                    if (preview.skipped.isNotEmpty()) {
                        Text(
                            "Пропущены строки: ${preview.skipped.joinToString("; ") { "${it.rowNumber} (${it.reason})" }}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            dismissButton = { TextButton(onClick = { importPreview = null }) { Text("Отмена") } },
            confirmButton = {
                TextButton(onClick = {
                    val rows = preview.rows
                    importPreview = null
                    kotlinx.coroutines.CoroutineScope(Dispatchers.IO).launch {
                        val result = mergeTenantImport(db, rows)
                        withContext(Dispatchers.Main) { importSummary = result }
                    }
                }) { Text("Применить") }
            }
        )
    }
    importSummary?.let { summary ->
        AlertDialog(
            onDismissRequest = { importSummary = null },
            title = { Text("Импорт завершён") },
            text = { Text("Добавлено: ${summary.added}\nОбновлено: ${summary.updated}\nПропущено: ${summary.skipped}") },
            confirmButton = { TextButton(onClick = { importSummary = null }) { Text("OK") } }
        )
    }
}

@Composable
private fun TenantPhoneFields(
    originalValue: String,
    contacts: List<TenantPhoneContact>,
    context: Context
) {
    if (contacts.isEmpty()) {
        TenantDetailField("Телефон", originalValue)
        return
    }

    Text(
        "Телефон:",
        modifier = Modifier.padding(top = 3.dp),
        style = MaterialTheme.typography.bodyMedium
    )
    contacts.forEach { contact ->
        TenantDetailField(
            label = "",
            value = contact.displayValue,
            onClick = {
                launchTenantPhoneIntent(
                    context,
                    contact.dialValue,
                    "Не удалось открыть приложение для звонков"
                )
            }
        )
    }
}

@Composable
private fun TenantEmailFields(
    originalValue: String,
    contacts: List<String>,
    context: Context
) {
    if (contacts.isEmpty()) {
        TenantDetailField("E-mail", originalValue)
        return
    }

    Text(
        "E-mail:",
        modifier = Modifier.padding(top = 3.dp),
        style = MaterialTheme.typography.bodyMedium
    )
    contacts.forEach { email ->
        val intent = Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", email, null))
        TenantDetailField(
            label = "",
            value = email,
            onClick = {
                launchTenantIntent(
                    context,
                    intent,
                    "Не удалось открыть почтовое приложение"
                )
            }
        )
    }
}

@Composable
private fun TenantDetailField(
    label: String,
    value: String,
    onClick: (() -> Unit)? = null
) {
    Text(
        if (label.isBlank()) value.ifBlank { "—" } else "$label: ${value.ifBlank { "—" }}",
        modifier = Modifier
            .padding(vertical = 3.dp)
            .clickable(enabled = onClick != null) { onClick?.invoke() },
        color = if (onClick != null) MaterialTheme.colorScheme.primary else Color.Unspecified
    )
}

@Composable
private fun TenantLeaseEndField(tenant: TenantRecord) {
    val status = leaseStatus(tenant.leaseEnd)
    val statusColor = leaseStatusColor(status)
    val highlighted = statusColor != Color.Transparent
    Text(
        "Дата окончания: ${tenant.leaseEnd.ifBlank { "—" }} • ${leaseStatusLabel(status)}",
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
            .then(
                if (highlighted) {
                    Modifier
                        .background(statusColor.copy(alpha = 0.16f))
                        .border(1.dp, statusColor.copy(alpha = 0.7f))
                        .padding(horizontal = 8.dp, vertical = 5.dp)
                } else {
                    Modifier
                }
            ),
        color = if (highlighted) statusColor else Color.Unspecified
    )
}

private data class TenantPhoneContact(
    val displayValue: String,
    val dialValue: String
)

private fun tenantPhoneContacts(value: String): List<TenantPhoneContact> {
    if (value.isBlank()) return emptyList()

    val withoutExtensions = value
        .replace(Regex("""(?iu)\s*\(?\s*доб\.?\s*\d[\d\s-]*\s*\)?"""), "")
        .replace(Regex("""(?<=\d)\s+(?=\+?7(?:[\s(-])|8(?:[\s(-]))"""), ";")
    val phonePattern = Regex("""(?<!\d)\+?\d[\d\s().-]*\d(?!\d)""")

    return withoutExtensions
        .split(';', ',', '\n')
        .flatMap { part ->
            phonePattern.findAll(part).mapNotNull { match ->
                val display = match.value
                    .trim()
                    .trim('"', '\'')
                    .replace(Regex("""\s*\(\s*\d{3,5}\s*\)$"""), "")
                val digits = display.filter(Char::isDigit)
                if (digits.length < 5) {
                    null
                } else {
                    TenantPhoneContact(
                        displayValue = display,
                        dialValue = normalizeDialValue(display, digits)
                    )
                }
            }.toList()
        }
        .distinctBy { it.dialValue }
}

private fun normalizeDialValue(display: String, digits: String): String =
    when {
        display.trimStart().startsWith("+") -> "+$digits"
        digits.length == 11 && digits.startsWith('8') -> "+7${digits.drop(1)}"
        else -> digits
    }

private val TENANT_EMAIL_PATTERN =
    Regex("""(?i)^[^@\s]+@[^@\s]+\.[^@\s]+$""")

private fun tenantEmailContacts(value: String): List<String> =
    value.split(';', ',', '\n')
        .asSequence()
        .map { it.trim().trim('"', '\'', '<', '>') }
        .filter { TENANT_EMAIL_PATTERN.matches(it) }
        .distinct()
        .toList()

private fun isMapAddress(address: String): Boolean =
    address.length >= 5 &&
        address.any(Char::isLetter) &&
        address.any(Char::isDigit)

private fun mapIntent(address: String): Intent =
    Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=${Uri.encode(address)}"))

private fun hasIntentHandler(context: Context, intent: Intent): Boolean =
    context.packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY) != null

private fun displayNameForUri(context: Context, uri: Uri): String {
    val projection = arrayOf(OpenableColumns.DISPLAY_NAME)
    context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (nameIndex >= 0 && cursor.moveToFirst()) {
            cursor.getString(nameIndex)?.takeIf { it.isNotBlank() }?.let { return it }
        }
    }
    return uri.lastPathSegment?.substringAfterLast('/')?.takeIf { it.isNotBlank() }
        ?: "Акт проверки.docx"
}

private fun openAttachment(context: Context, uriValue: String) {
    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(
            Uri.parse(uriValue),
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
        )
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    if (!hasIntentHandler(context, intent)) {
        Toast.makeText(
            context,
            "Не найдено приложение для открытия DOCX. Установите Word или офисный редактор.",
            Toast.LENGTH_LONG
        ).show()
        return
    }
    try {
        context.startActivity(intent)
    } catch (_: ActivityNotFoundException) {
        Toast.makeText(
            context,
            "Не удалось открыть акт. Установите Word или офисный редактор.",
            Toast.LENGTH_LONG
        ).show()
    } catch (_: SecurityException) {
        Toast.makeText(
            context,
            "Нет доступа к сохранённому файлу акта.",
            Toast.LENGTH_LONG
        ).show()
    }
}

private fun launchTenantIntent(context: Context, intent: Intent, errorMessage: String) {
    try {
        context.startActivity(intent)
    } catch (_: ActivityNotFoundException) {
        Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
    } catch (_: SecurityException) {
        Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
    }
}

private fun launchTenantPhoneIntent(
    context: Context,
    dialValue: String,
    errorMessage: String
) {
    val uri = Uri.parse("tel:$dialValue")
    val dialIntent = Intent(Intent.ACTION_DIAL, uri)
    try {
        context.startActivity(dialIntent)
        return
    } catch (_: ActivityNotFoundException) {
        // Try the generic URI handler below.
    } catch (_: SecurityException) {
        // Try the generic URI handler below.
    }

    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, uri))
    } catch (_: ActivityNotFoundException) {
        Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
    } catch (_: SecurityException) {
        Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
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
