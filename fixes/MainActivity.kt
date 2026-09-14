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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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

class MainViewModel(private val db: AppDatabase) : ViewModel() {

    val complexes: Flow<List<Complex>> = db.complexDao().all()

    fun seed() {
        viewModelScope.launch(Dispatchers.IO) {
            if (db.complexDao().count() == 0) {
                listOf("Меркурий", "Континент", "Маршака").forEach {
                    db.complexDao().insert(Complex(name = it))
                }
            }
        }
    }
}

class MainViewModelFactory(
    private val db: AppDatabase
) : androidx.lifecycle.ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MainViewModel(db) as T
    }
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val db = AppDatabase.get(this)

        setContent {
            val vm: MainViewModel =
                viewModel(factory = MainViewModelFactory(db))

            LaunchedEffect(Unit) {
                vm.seed()
            }

            Dashboard(vm.complexes)
        }
    }
}

@Composable
fun Dashboard(complexes: Flow<List<Complex>>) {

    val list by complexes.collectAsState(initial = emptyList())

    var selectedScreen by remember {
        mutableStateOf(0)
    }

    var selectedComplex by remember {
        mutableStateOf<Complex?>(null)
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
                        list = list,
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
                        list = list,
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
                Text("План проверок")
            },

            text = {
                Column {

                    Text(
                        complex.name,
                        style = MaterialTheme.typography.titleLarge
                    )

                    Spacer(
                        modifier = Modifier.height(12.dp)
                    )

                    Text("Ежемесячная комплексная проверка")

                    Spacer(
                        modifier = Modifier.height(8.dp)
                    )

                    Text("Январь — плановая дата")
                    Text("Февраль — плановая дата")
                    Text("Март — плановая дата")
                    Text("Апрель — плановая дата")
                    Text("Май — плановая дата")
                    Text("Июнь — плановая дата")
                    Text("Июль — плановая дата")
                    Text("Август — плановая дата")
                    Text("Сентябрь — плановая дата")
                    Text("Октябрь — плановая дата")
                    Text("Ноябрь — плановая дата")
                    Text("Декабрь — плановая дата")
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

        verticalArrangement = Arrangement.spacedBy(12.dp)
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

        items(filteredList) { complex ->

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
                        modifier = Modifier.height(4.dp)
                    )

                    Text(
                        "Ежемесячная комплексная проверка"
                    )

                    Text(
                        "План проверок: январь–декабрь 2026",
                        style = MaterialTheme.typography.bodySmall
                    )

                    Spacer(
                        modifier = Modifier.height(8.dp)
                    )

                    Button(
                        onClick = {
                            onOpenPlan(complex)
                        }
                    ) {
                        Text("Открыть план")
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

        verticalArrangement = Arrangement.spacedBy(12.dp)
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

        items(list) { complex ->

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
