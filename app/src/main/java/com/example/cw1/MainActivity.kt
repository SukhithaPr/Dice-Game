package com.example.cw1

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.cw1.ui.theme.CW1Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CW1Theme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen() {
    var showAbout by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = {
                val intent = Intent(context, GameActivity::class.java)
                context.startActivity(intent)
            },
            modifier = Modifier.width(200.dp)
        ) {
            Text("New Game")
        }

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = { showAbout = true },
            modifier = Modifier.width(200.dp)
        ) {
            Text("About")
        }
    }

    if (showAbout) {
        AlertDialog(
            onDismissRequest = { showAbout = false },
            title = { Text("About") },
            text = {
                Text("Student ID: YOUR_ID\nName: YOUR_NAME\n\n" +
                        "I confirm that I understand what plagiarism is and have read and " +
                        "understood the section on Assessment Offences in the Essential " +
                        "Information for Students. The work that I have submitted is " +
                        "entirely my own. Any work from other authors is duly referenced " +
                        "and acknowledged.")
            },
            confirmButton = {
                Button(onClick = { showAbout = false }) {
                    Text("OK")
                }
            }
        )
    }
}