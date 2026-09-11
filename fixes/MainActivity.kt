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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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

    MaterialTheme {
        Scaffold(
            bottomBar = {
                NavigationBar {

                    NavigationBarItem(
                        selected = true,
                        onClick = {},
                        icon = {},
                        label = { Text("Главная") }
                    )

                    NavigationBarItem(
                        selected = false,
                        onClick = {},
                        icon = {},
                        label = { Text("Объекты") }
                    )

                    NavigationBarItem(
                        selected = false,
                        onClick = {},
                        icon = {},
                        label = { Text("Работы") }
                    )

                    NavigationBarItem(
                        selected = false,
                        onClick = {},
                        icon = {},
                        label = { Text("Ещё") }
                    )
                }
            }
        ) { paddingValues ->

            LazyColumn(
                modifier = Modifier
                    .padding(paddingValues)
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
                    Text(
                        "КОМПЛЕКСНЫЕ ПРОВЕРКИ",
                        style = MaterialTheme.typography.titleLarge
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
                                onClick = {}
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

                item {
                    OutlinedTextField(
                        value = "",
                        onValueChange = {},
                        modifier = Modifier.fillMaxWidth(),
                        label = {
                            Text("Поиск арендатора или секции")
                        }
                    )
                }
            }
        }
    }
}
