package com.example.tipapp

import android.os.Bundle
import android.util.Log
import android.widget.Space
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Slider
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.tipapp.components.InputField
import com.example.tipapp.ui.theme.TipAppTheme
import com.example.tipapp.util.calculateTotalPerPerson
import com.example.tipapp.util.calculateTotalTip
import com.example.tipapp.widgets.RoundIconButton
import kotlin.math.ceil

@ExperimentalComposeUiApi
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApp {
                //TopHeader()
                MainContent()
            }
        }
    }
}

@Composable
fun MyApp(content: @Composable () -> Unit){

    TipAppTheme {
        // A surface container using the 'background' color from the theme
        Surface(
            color = MaterialTheme.colors.background
        ) {
            content()
        }
    }
}

@Preview
@Composable
fun TopHeader(totalPerPerson: Double = 134.0){
    Surface(modifier = Modifier
        .fillMaxWidth()
        .height(150.dp)
        .padding(15.dp)
        , shape = RoundedCornerShape(corner = CornerSize(12.dp))
        , color = Color(0xFF8D6BB2)) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            val total = "%.2f".format(totalPerPerson)
            Text(text = "Total Per Person",
            style = MaterialTheme.typography.h5)
            Text(text = "$$total",
            style = MaterialTheme.typography.h4,
            fontWeight = FontWeight.ExtraBold)
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Preview
@Composable
fun MainContent(){

    //Split States
    val splitState = remember{
        mutableStateOf(1)
    }

    val tipAmount = remember {
        mutableStateOf(0.0)
    }

    val totalPerPersonState = remember {
        mutableStateOf(0.0)
    }

    BillForm(splitState = splitState, tipAmount = tipAmount, totalPerPersonState = totalPerPersonState) {}
}

@ExperimentalComposeUiApi
@Composable
fun BillForm(modifier: Modifier = Modifier,
             range: IntRange = 1..100,
             splitState: MutableState<Int>,
             tipAmount: MutableState<Double>,
             totalPerPersonState: MutableState<Double>,
        onValChange: (String) -> Unit = {}
        ){
    
    val totalBillState = remember{
        mutableStateOf("")
    }

    //state for not empty bill field
    val validState = remember(totalBillState.value) {
        totalBillState.value.trim().isNotEmpty()
    }

    val keyboardController = LocalSoftwareKeyboardController.current


    val sliderPositionState = remember{
        mutableStateOf(0f)
    }

    val tipPercentage = (sliderPositionState.value*100).toInt()



    Surface(modifier = modifier
        .padding(2.dp)
        .fillMaxWidth()
        .fillMaxHeight(),
        shape = RoundedCornerShape(CornerSize(8.dp)),
        border = BorderStroke(width = 1.dp,color = Color.LightGray)
    ) {
        Column(modifier = modifier.padding(6.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start) {
            TopHeader(totalPerPerson = totalPerPersonState.value)
            InputField(valueState = totalBillState,
                labelId = "Enter Bill",
                enabled = true,
                isSingleLine = true,
                onAction = KeyboardActions{
                    if(!validState) return@KeyboardActions
                    //Todo - onvaluechanged
                    onValChange(totalBillState.value.trim())
                    keyboardController?.hide()
                }
            )
            if(validState){
                Row(modifier = modifier.padding(3.dp),
                horizontalArrangement = Arrangement.Start){
                    Text(text = "Split",
                        modifier = Modifier.align(
                            alignment = Alignment.CenterVertically
                        ))
                    Spacer(modifier = Modifier.width(120.dp))
                    Row(modifier = Modifier.padding(horizontal = 3.dp),
                        horizontalArrangement = Arrangement.End) {

                        RoundIconButton(imageVector = Icons.Default.Remove,
                            onClick = {
                            if(splitState.value != 1) splitState.value -= 1
                                totalPerPersonState.value =
                                    calculateTotalPerPerson(totalBill = totalBillState.value.toDouble(),
                                        splitBy = splitState.value,
                                        tipPercentage = tipPercentage)}
                        )
                        Text(text = "${splitState.value}", modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .padding(start = 9.dp, end = 9.dp))
                        RoundIconButton(imageVector = Icons.Default.Add, onClick = {
                            splitState.value += 1
                            totalPerPersonState.value =
                                calculateTotalPerPerson(totalBill = totalBillState.value.toDouble(),
                                    splitBy = splitState.value,
                                    tipPercentage = tipPercentage)})
                    }
                    

                }

                //Tip Row
                Row(modifier = Modifier
                    .padding(horizontal = 3.dp, vertical = 12.dp)){
                    Text(text = "Text",modifier = Modifier.align(alignment = Alignment.CenterVertically))
                    Spacer(modifier = Modifier.width(200.dp))

                    Text(text = "$ ${tipAmount.value}",modifier = Modifier.align(alignment = Alignment.CenterVertically))
                    Spacer(modifier = Modifier.width(200.dp))
                }

                Column(verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "$tipPercentage%")
                    Spacer(modifier = Modifier.height(14.dp))

                    //Slider
                    Slider(value = sliderPositionState.value, onValueChange = { newVal ->
                        if(totalBillState.toString().isNotEmpty()){
                            sliderPositionState.value = newVal
                            tipAmount.value =
                                calculateTotalTip(totalBill = totalBillState.value.toDouble(), tipPercentage = tipPercentage)

                            totalPerPersonState.value =
                                calculateTotalPerPerson(totalBill = totalBillState.value.toDouble(),
                                    splitBy = splitState.value,
                                    tipPercentage = tipPercentage)

                        }

                    },
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp),
                        steps = 3)
                }
            }
            else{
                Box(){}
            }




        }

    }
}



@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    TipAppTheme {
        MyApp {
            Text(text = "Hello Again")
        }
    }
}