package com.example.cw1

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import com.example.cw1.ui.theme.CW1Theme

val diceImages = listOf(
    R.drawable.dice_1,
    R.drawable.dice_2,
    R.drawable.dice_3,
    R.drawable.dice_4,
    R.drawable.dice_5,
    R.drawable.dice_6
)

class GameActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CW1Theme {
                GameScreen()
            }
        }
    }
}

@Composable
fun GameScreen() {
    // Game state
    var humanWins by rememberSaveable { mutableStateOf(0) }
    var computerWins by rememberSaveable { mutableStateOf(0) }
    var humanScore by rememberSaveable { mutableStateOf(0) }
    var computerScore by rememberSaveable { mutableStateOf(0) }
    var currentTurn by rememberSaveable { mutableStateOf(1) }
    var targetScore by rememberSaveable { mutableStateOf(101) }
    var humanDice by rememberSaveable { mutableStateOf(List(5) { 1 }) }
    var computerDice by rememberSaveable { mutableStateOf(List(5) { 1 }) }
    var selectedDice by rememberSaveable { mutableStateOf<List<Int>>(emptyList()) }
    var gameOver by rememberSaveable { mutableStateOf(false) }
    var winner by rememberSaveable { mutableStateOf("") }
    var showTargetDialog by remember { mutableStateOf(false) }
    var tempTarget by remember { mutableStateOf(targetScore.toString()) }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Game header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("H:$humanWins/C:$computerWins", style = MaterialTheme.typography.titleSmall)
                Text(
                    "Target: $targetScore",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Button(
                    onClick = { showTargetDialog = true },
                    modifier = Modifier.width(130.dp)
                ) {
                    Text("Set Target")
                }
            }

            Spacer(Modifier.height(16.dp))

        // Scores display
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Your Score", style = MaterialTheme.typography.titleMedium)
                    Text("$humanScore", style = MaterialTheme.typography.displaySmall)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Computer Score", style = MaterialTheme.typography.titleMedium)
                    Text("$computerScore", style = MaterialTheme.typography.displaySmall)
                }
            }

            Spacer(Modifier.height(24.dp))

        // Dice display
        Column {
            Text("Computer dice:", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            DiceRow(diceValues = computerDice)
        }

            Spacer(Modifier.height(24.dp))

        Column {
            Text("Your dice:", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            DiceRow(
                diceValues = humanDice,
                selected = selectedDice,
                enabled = currentTurn > 1 && !gameOver
            ) { index ->
                selectedDice = if (selectedDice.contains(index)) {
                    selectedDice - index
                } else {
                    selectedDice + index
                }
            }
        }

            Spacer(Modifier.height(32.dp))

            // Game controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = {
                        // Human roll
                        humanDice = humanDice.mapIndexed { index, value ->
                            if (selectedDice.contains(index)) value else (1..6).random()
                        }

                        // Computer roll
                        computerDice = if (currentTurn > 1) {
                            val keep =
                                computerRollStrategy(computerDice, computerScore, targetScore)
                            computerDice.mapIndexed { index, value ->
                                if (keep[index]) value else (1..6).random()
                            }
                        } else {
                            List(5) { (1..6).random() }
                        }

                        currentTurn++
                        if (currentTurn > 3) {
                            scoreRound(
                                humanDice, computerDice,
                                humanScore,
                                computerScore,
                                { humanScore += it },
                                { computerScore += it },
                                {
                                    gameOver = true
                                    winner = it
                                    if (it == "human") humanWins++ else computerWins++
                                },
                                targetScore
                            )

                            currentTurn = 1
                            selectedDice = emptyList()
                        }
                    },
                    enabled = currentTurn <= 3 && !gameOver,
                    modifier = Modifier.width(150.dp)
                ) {
                    Text("Roll (${4 - currentTurn} left)")
                }

                // Show Score button only after first roll
                if (currentTurn > 1) {
                    Button(
                        onClick = {
                            scoreRound(
                                humanDice, computerDice,
                                humanScore,
                                computerScore,
                                { humanScore += it },
                                { computerScore += it },
                                {
                                    gameOver = true
                                    winner = it
                                    if (it == "human") humanWins++ else computerWins++
                                },
                                targetScore
                            )

                            currentTurn = 1
                            selectedDice = emptyList()
                        },
                        enabled = !gameOver,
                        modifier = Modifier.width(150.dp)
                    ) {
                        Text("Score")
                    }
                } else {
                    Spacer(modifier = Modifier.width(150.dp))
                }
            }
        }

    // Target score dialog
    if (showTargetDialog) {
        AlertDialog(
            onDismissRequest = { showTargetDialog = false },
            title = { Text("Set Target Score") },
            text = {
                Column {
                    Text("Current target: $targetScore")
                    Spacer(Modifier.height(8.dp))
                    TextField(
                        value = tempTarget,
                        onValueChange = { tempTarget = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        targetScore = tempTarget.toIntOrNull()?.takeIf { it > 0 } ?: 101
                        showTargetDialog = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                Button(
                    onClick = { showTargetDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    // Game over dialog
    if (gameOver) {
        AlertDialog(
            onDismissRequest = { /* Can't dismiss */ },
            title = { Text("Game Over") },
            text = {
                Text(
                    if (winner == "human") "You win!" else "You lose!",
                    color = if (winner == "human") Color(0xFF4CAF50) else Color(0xFFF44336),
                    style = MaterialTheme.typography.headlineMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        // Return to MainActivity
                        val intent = Intent(context, MainActivity::class.java)
                        context.startActivity(intent)
                    }
                ) {
                    Text("Return to Menu")
                }
            }
        )
    }
}

@Composable
fun DiceRow(
    diceValues: List<Int>,
    selected: List<Int> = emptyList(),
    enabled: Boolean = true,
    onDiceSelected: (Int) -> Unit = {}
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        diceValues.forEachIndexed { index, value ->
            Image(
                painter = painterResource(id = diceImages[value - 1]),
                contentDescription = "Dice showing $value",
                modifier = Modifier
                    .size(48.dp)
                    .clickable(enabled = enabled) { onDiceSelected(index) }
                    .border(
                        width = if (selected.contains(index)) 3.dp else 0.dp,
                        color = if (selected.contains(index)) Color.Blue else Color.Transparent
                    )
            )
        }
    }
}

fun computerRollStrategy(currentDice: List<Int>, currentScore: Int, targetScore: Int): List<Boolean> {
    val threshold = targetScore - currentScore
    return currentDice.map { value ->
        when {
            threshold <= 6 -> value >= threshold
            threshold <= 15 -> value >= 5
            else -> value >= 4
        }
    }
}

fun scoreRound(
    humanDice: List<Int>,
    computerDice: List<Int>,
    currentHumanScore: Int,
    currentComputerScore: Int,
    updateHumanScore: (Int) -> Unit,
    updateComputerScore: (Int) -> Unit,
    onGameOver: (String) -> Unit,
    targetScore: Int
) {
    val humanRoundScore = humanDice.sum()
    val computerRoundScore = computerDice.sum()

    val humanTotal = currentHumanScore + humanRoundScore
    val computerTotal = currentComputerScore + computerRoundScore

    updateHumanScore(humanRoundScore)
    updateComputerScore(computerRoundScore)

    if (humanTotal >= targetScore || computerTotal >= targetScore) {
        val winner = when {
            humanTotal > computerTotal -> "human"
            computerTotal > humanTotal -> "computer"
            else -> {
                var humanTie = humanDice.sum()
                var computerTie = computerDice.sum()
                while (humanTie == computerTie) {
                    humanTie = List(5) { (1..6).random() }.sum()
                    computerTie = List(5) { (1..6).random() }.sum()
                }
                if (humanTie > computerTie) "human" else "computer"
            }
        }
        onGameOver(winner)
    }
}