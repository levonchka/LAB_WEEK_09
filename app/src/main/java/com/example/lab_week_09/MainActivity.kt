package com.example.lab_week_09

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.lab_week_09.ui.theme.LAB_WEEK_09Theme
import com.example.lab_week_09.ui.theme.OnBackgroundItemText
import com.example.lab_week_09.ui.theme.OnBackgroundTitleText
import com.example.lab_week_09.ui.theme.PrimaryTextButton
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory


data class Student(val name: String = "")


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LAB_WEEK_09Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    App(navController = navController)
                }
            }
        }
    }
}


@Composable
fun App(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") {
            Home { jsonString ->
                navController.navigate("resultContent/?listData=$jsonString")
            }
        }

        composable(
            "resultContent/?listData={listData}",
            arguments = listOf(navArgument("listData") { type = NavType.StringType })
        ) {
            val json = it.arguments?.getString("listData").orEmpty()
            ResultContent(json)
        }
    }
}


@Composable
fun Home(navigateFromHomeToResult: (String) -> Unit) {
    var inputField by remember { mutableStateOf(Student()) }
    val listData = remember { mutableStateListOf<Student>() }

    // Moshi setup
    val moshi = remember {
        Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    }
    val type = remember {
        Types.newParameterizedType(List::class.java, Student::class.java)
    }
    val adapter = remember { moshi.adapter<List<Student>>(type) }

    HomeContent(
        listData = listData,
        inputField = inputField,
        onInputValueChange = { inputField = inputField.copy(name = it) },
        onButtonClick = {
            if (inputField.name.isNotBlank()) {
                listData.add(inputField)
                inputField = Student()
            }
        },
        navigateFromHomeToResult = {
            val jsonString = adapter.toJson(listData)
            navigateFromHomeToResult(jsonString)
        }
    )
}


@Composable
fun HomeContent(
    listData: SnapshotStateList<Student>,
    inputField: Student,
    onInputValueChange: (String) -> Unit,
    onButtonClick: () -> Unit,
    navigateFromHomeToResult: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            OnBackgroundTitleText(text = stringResource(id = R.string.enter_item))
            TextField(
                value = inputField.name,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                onValueChange = { onInputValueChange(it) },
                label = { Text("Enter student name") },
                modifier = Modifier.fillMaxWidth()
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                PrimaryTextButton(text = stringResource(id = R.string.button_click)) {
                    onButtonClick()
                }
                PrimaryTextButton(text = stringResource(id = R.string.button_navigate)) {
                    navigateFromHomeToResult()
                }
            }
        }

        items(listData) { item ->
            OnBackgroundItemText(text = item.name)
        }
    }
}


@Composable
fun ResultContent(listData: String) {
    val moshi = remember {
        Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    }
    val type = remember {
        Types.newParameterizedType(List::class.java, Student::class.java)
    }
    val adapter = remember { moshi.adapter<List<Student>>(type) }

    val studentList = remember(listData) {
        adapter.fromJson(listData) ?: emptyList()
    }

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OnBackgroundTitleText(text = "Result Page")

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(studentList) { student ->
                OnBackgroundItemText(text = student.name)
            }
        }
    }
}
