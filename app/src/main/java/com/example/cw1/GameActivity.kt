package com.example.cw1

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
    var humanScore by rememberSaveable { mutableStateOf(0) }
    var computerScore by rememberSaveable { mutableStateOf(0) }
    var humanWins by rememberSaveable { mutableStateOf(0) }
    var computerWins by rememberSaveable { mutableStateOf(0) }
    var rollsLeft by rememberSaveable { mutableStateOf(3) }
    var targetScore by rememberSaveable { mutableStateOf(101) }
    var humanDice by rememberSaveable { mutableStateOf(List(5) { 1 }) }
    var computerDice by rememberSaveable { mutableStateOf(List(5) { 1 }) }
    var selectedDice by rememberSaveable { mutableStateOf<List<Int>>(emptyList()) }
    var gameOver by rememberSaveable { mutableStateOf(false) }
    var winner by rememberSaveable { mutableStateOf("") }
    var showTargetDialog by remember { mutableStateOf(false) }
    var tempTarget by remember { mutableStateOf(targetScore.toString()) }

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
            Text("Wins: H:$humanWins/C:$computerWins", style = MaterialTheme.typography.titleSmall)
            Text("Target: $targetScore", style = MaterialTheme.typography.titleSmall)
            Button(
                onClick = { showTargetDialog = true },
                modifier = Modifier.width(100.dp)
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
            Text("Your dice:", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            DiceRow(
                diceValues = humanDice,
                selected = selectedDice,
                enabled = rollsLeft < 3 && !gameOver
            ) { index ->
                selectedDice = if (selectedDice.contains(index)) {
                    selectedDice - index
                } else {
                    selectedDice + index
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        Column {
            Text("Computer dice:", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            DiceRow(diceValues = computerDice)
        }

        Spacer(Modifier.height(32.dp))

        // Game controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = {
                    // Human roll logic
                    humanDice = humanDice.mapIndexed { index, value ->
                        if (selectedDice.contains(index)) value else (1..6).random()
                    }

                    // Computer roll logic
                    computerDice = if (rollsLeft < 3) {
                        val keep = computerRollStrategy(computerDice, computerScore, targetScore)
                        computerDice.mapIndexed { index, value ->
                            if (keep[index]) value else (1..6).random()
                        }
                    } else {
                        List(5) { (1..6).random() }
                    }

                    rollsLeft--
                    if (rollsLeft == 0) {
                        // Auto-score when no rolls left
                        scoreRound(
                            humanDice, computerDice,
                            { humanScore += it },
                            { computerScore += it },
                            {
                                gameOver = true
                                winner = it
                                if (it == "human") humanWins++ else computerWins++
                            },
                            targetScore
                        )
                        rollsLeft = 3
                        selectedDice = emptyList()
                    }
                },
                enabled = rollsLeft > 0 && !gameOver,
                modifier = Modifier.width(150.dp)
            ) {
                Text("Roll (${rollsLeft} left)")
            }

            Button(
                onClick = {
                    scoreRound(
                        humanDice, computerDice,
                        { humanScore += it },
                        { computerScore += it },
                        {
                            gameOver = true
                            winner = it
                            if (it == "human") humanWins++ else computerWins++
                        },
                        targetScore
                    )
                    rollsLeft = 3
                    selectedDice = emptyList()
                },
                enabled = rollsLeft < 3 && !gameOver,
                modifier = Modifier.width(150.dp)
            ) {
                Text("Score")
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
                        gameOver = false
                        humanScore = 0
                        computerScore = 0
                        rollsLeft = 3
                        humanDice = List(5) { 1 }
                        computerDice = List(5) { 1 }
                        selectedDice = emptyList()
                    }
                ) {
                    Text("New Game")
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
            // If close to target, keep dice that reach the target
            threshold <= 6 -> value >= threshold
            // If moderately close, keep high dice (5-6)
            threshold <= 15 -> value >= 5
            // Otherwise keep decent dice (4-6)
            else -> value >= 4
        }
    }
}

fun scoreRound(
    humanDice: List<Int>,
    computerDice: List<Int>,
    updateHumanScore: (Int) -> Unit,
    updateComputerScore: (Int) -> Unit,
    onGameOver: (String) -> Unit,
    targetScore: Int
) {
    val humanRoundScore = humanDice.sum()
    val computerRoundScore = computerDice.sum()

    updateHumanScore(humanRoundScore)
    updateComputerScore(computerRoundScore)

    val humanTotal = humanRoundScore
    val computerTotal = computerRoundScore

    if (humanTotal >= targetScore || computerTotal >= targetScore) {
        val winner = when {
            humanTotal > computerTotal -> "human"
            computerTotal > humanTotal -> "computer"
            else -> { // Tie-breaker
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