package com.example.tcmanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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

    var selectedInspection by remember {
        mutableStateOf<InspectionSchedule?>(null)
    }

    var searchText by remember {
        mutableStateOf("")
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
                            Text("Объекты")
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
                        onOpenPlan = {
                            selectedComplex = it
                        }
                    )
                }

                1 -> {

                    ObjectsScreen(
                        list = uniqueList,
                        onOpenPlan = {
                            selectedComplex = it
                        }
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

                    SimpleScreen(
                        title = "ЕЩЁ",
                        text = "Документы, проекты, настройки и резервное копирование."
                    )
                }
            }
        }
    }

    selectedComplex?.let { complex ->

        AlertDialog(

            onDismissRequest = {
                selectedComplex = null
            },

            title = {
                Text("Календарь проверок — 2026")
            },

            text = {

                Column {

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

                    LazyColumn(
                        modifier = Modifier.height(360.dp)
                    ) {
                        items(MONTHS) { month ->
                            val schedule =
                                INSPECTION_DATES_BY_COMPLEX[complex.name]?.get(month)

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(enabled = schedule != null) {
                                        selectedInspection = schedule
                                    }
                                    .padding(vertical = 8.dp)
                            ) {
                                Text("$month — ${schedule?.date ?: "дата не задана"}")
                                if (schedule != null) {
                                    Text(
                                        if (schedule.details.isEmpty()) {
                                            "Нажмите, чтобы посмотреть детали"
                                        } else {
                                            "Нажмите, чтобы посмотреть объекты проверки"
                                        },
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }
                }
            },

            confirmButton = {

                TextButton(
                    onClick = {
                        selectedComplex = null
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
}

@Composable
fun HomeScreen(
    list: List<Complex>,
    searchText: String,
    onSearchChange: (String) -> Unit,
    onOpenPlan: (Complex) -> Unit
) {

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
                    Card(
                        modifier = Modifier
                            .size(156.dp)
                            .clickable {
                                onOpenPlan(complex)
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
fun ObjectsScreen(
    list: List<Complex>,
    onOpenPlan: (Complex) -> Unit
) {

    LazyColumn(

        modifier = Modifier.padding(16.dp),

        verticalArrangement =
            Arrangement.spacedBy(12.dp)

    ) {

        item {

            Text(
                "ОБЪЕКТЫ",
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
                modifier = Modifier.fillMaxWidth()
            ) {

                Column(
                    modifier = Modifier.padding(16.dp)
                ) {

                    Text(
                        complex.name,
                        style = MaterialTheme.typography.titleLarge
                    )

                    Spacer(
                        modifier = Modifier.height(6.dp)
                    )

                    Text("Этажи")
                    Text("Очереди")
                    Text("Секции")
                    Text("Арендаторы")

                    Spacer(
                        modifier = Modifier.height(8.dp)
                    )

                    OutlinedButton(
                        onClick = {
                            onOpenPlan(complex)
                        }
                    ) {
                        Text("План проверок")
                    }
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
