package com.example.tip_calculator

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Slider
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.tip_calculator.components.InputField
import com.example.tip_calculator.ui.theme.TipCalculatorTheme
import com.example.tip_calculator.util.calculateTotalPerPerson
import com.example.tip_calculator.util.calculateTotalTip
import com.example.tip_calculator.widgets.RoundedIconButton

@ExperimentalComposeUiApi
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApp {
                TipCalculator()
            }
        }
    }
}


@Composable
fun MyApp(content: @Composable () -> Unit) {
    TipCalculatorTheme {
        content()
    }
}

@ExperimentalComposeUiApi
@Composable
fun TipCalculator() {
    Column(modifier = Modifier.padding(12.dp)) {
        MainContent()
    }
}

@Preview
@Composable
fun TopHeader(totalPerPers: Double = 0.0) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .padding(12.dp)
            .clip(shape = CircleShape.copy(all = CornerSize(12.dp))),
        color = Color(0xFFe9d7f7)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = "Total Per Person", style = MaterialTheme.typography.subtitle1)
            val total = "%.2f".format(totalPerPers)
            Text(text = "\$$total", style = MaterialTheme.typography.h4)
        }
    }
}

@ExperimentalComposeUiApi
@Composable
fun MainContent() {
    val splitBy = remember {
        mutableStateOf(1)
    }

    val totalTipAmt = remember {
        mutableStateOf(0.0)
    }

    val totalPerPerson = remember {
        mutableStateOf(0.0)
    }
    BillForm(
        splitByState = splitBy,
        tipAmountState = totalTipAmt,
        totalPerPersonState = totalPerPerson
    ) {

    }
}


@ExperimentalComposeUiApi
@Composable
fun BillForm(
    range: IntRange = 1..100,
    splitByState: MutableState<Int>,
    tipAmountState: MutableState<Double>,
    totalPerPersonState: MutableState<Double>,
    onValChange: (String) -> Unit = {},
) {

    var sliderPosition by remember {
        mutableStateOf(0f)
    }

    val tipPercentage = (sliderPosition * 100).toInt()
    val totalBill = rememberSaveable { mutableStateOf("") } //or just remember {}
    val keyboardController = LocalSoftwareKeyboardController.current
    val valid = remember(totalBill.value) {
        totalBill.value.trim().isNotEmpty()
    }

    TopHeader(totalPerPers = totalPerPersonState.value)

    Surface(
        modifier = Modifier
            .padding(2.dp)
            .fillMaxWidth(),
        shape = CircleShape.copy(all = CornerSize(8.dp)),
        border = BorderStroke(width = 1.dp, color = Color.LightGray)
    ) {
        Column(
            modifier = Modifier.padding(6.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {
            InputField(
                valueState = totalBill, labelId = "Enter Bill",
                enabled = true,
                onAction = KeyboardActions {
                    if (!valid) return@KeyboardActions
                    onValChange(totalBill.value.trim())
                    keyboardController?.hide()
                },
            )

            if (valid) {
                Row(
                    modifier = Modifier.padding(3.dp),
                    horizontalArrangement = Arrangement.Start
                ) {
                    Text(
                        text = "Split",
                        modifier = Modifier.align(alignment = Alignment.CenterVertically)
                    )
                    Spacer(modifier = Modifier.width(120.dp))

                    Row(
                        modifier = Modifier.padding(horizontal = 3.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        RoundedIconButton(
                            imageVector = Icons.Default.Remove,
                            onClick = {
                                splitByState.value =
                                    if (splitByState.value > 1) splitByState.value - 1 else 1
                                totalPerPersonState.value =
                                    calculateTotalPerPerson(totalBill = totalBill.value.toDouble(),
                                        splitBy = splitByState.value,
                                        tipPercent = tipPercentage)
                            }
                        )
                        Text(
                            text = "${splitByState.value}",
                            Modifier
                                .align(alignment = Alignment.CenterVertically)
                                .padding(start = 9.dp, end = 9.dp)
                        )
                        RoundedIconButton(
                            imageVector = Icons.Default.Add,
                            onClick = {
                                if (splitByState.value < range.last) {
                                    splitByState.value = splitByState.value + 1

                                    totalPerPersonState.value =
                                        calculateTotalPerPerson(totalBill = totalBill.value.toDouble(),
                                            splitBy = splitByState.value,
                                            tipPercent = tipPercentage)
                                }
                            }
                        )

                    }
                }
                Row(
                    modifier = Modifier
                        .padding(horizontal = 3.dp)
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = "Tip",
                        modifier = Modifier.align(alignment = Alignment.CenterVertically)
                    )
                    Spacer(modifier = Modifier.width(200.dp))
                    Text(
                        text = "$${tipAmountState.value}",
                        modifier = Modifier.align(alignment = Alignment.CenterVertically)
                    )

                }
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "$tipPercentage %")
                    Spacer(modifier = Modifier.height(14.dp))
                    Slider(
                        value = sliderPosition,
                        onValueChange = { newVal ->
                            sliderPosition = newVal
                            tipAmountState.value =
                                calculateTotalTip(totalBill = totalBill.value.toDouble(),
                                    tipPercent = tipPercentage)

                            totalPerPersonState.value =
                                calculateTotalPerPerson(totalBill = totalBill.value.toDouble(),
                                    splitBy = splitByState.value,
                                    tipPercent = tipPercentage)
                            Log.d("Slider",
                                "Total Bill-->: ${"%.2f".format(totalPerPersonState.value)}")

                        },
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp),
                        steps = 5,
                        onValueChangeFinished = {
                            Log.d("Finished", "BillForm: $tipPercentage")
                        }
                    )
                }
            }
        }
    }
}

@ExperimentalComposeUiApi
@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    MyApp {
        TipCalculator()
    }
}