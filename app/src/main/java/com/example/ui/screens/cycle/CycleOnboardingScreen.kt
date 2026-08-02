package com.example.ui.screens.cycle

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalIsDarkTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter

import androidx.compose.animation.SizeTransform
import androidx.compose.foundation.border
import compose.icons.TablerIcons
import compose.icons.tablericons.Calendar

data class CycleOnboardingData(
    val lastPeriodStart: LocalDate = LocalDate.now(),
    val periodLength: Int = 5,
    val cycleLength: Int = 28,
    val flow: String = "Medium",
    val cramps: String = "Mild",
    val moods: Set<String> = emptySet(),
    val energy: String = "Normal",
    val symptoms: Set<String> = emptySet(),
    val aiSupport: Boolean = true,
    val reminders: Boolean = true
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CycleOnboardingScreen(
    onComplete: (CycleOnboardingData) -> Unit
) {
    var currentStep by remember { mutableStateOf(1) }
    var data by remember { mutableStateOf(CycleOnboardingData()) }
    val totalSteps = 10

    val darkTheme = LocalIsDarkTheme.current
    val bgColor = MaterialTheme.colorScheme.background
    val cardBg = MaterialTheme.colorScheme.surface
    val textMain = MaterialTheme.colorScheme.onBackground
    val textSub = MaterialTheme.colorScheme.onSurfaceVariant
    val brandPurple = MaterialTheme.colorScheme.primary
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor),
    ) {
        // Top Header
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = "Cycle",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = textMain
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "A few gentle questions to set things up.",
                fontSize = 16.sp,
                color = textSub
            )
        }

        // Card Content
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 24.dp, vertical = 8.dp),
            shape = RoundedCornerShape(24.dp),
            color = cardBg,
            shadowElevation = 4.dp
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                // Progress Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    for (i in 1..totalSteps) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(4.dp)
                                .padding(horizontal = 2.dp)
                                .clip(CircleShape)
                                .background(if (i <= currentStep) brandPurple else brandPurple.copy(alpha = 0.2f))
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = "STEP $currentStep OF $totalSteps",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = textSub,
                    letterSpacing = 1.sp
                )
                
                Spacer(modifier = Modifier.height(16.dp))

                AnimatedContent(
                    targetState = currentStep,
                    transitionSpec = {
                        if (targetState > initialState) {
                            (slideInHorizontally { width -> width } + fadeIn()).togetherWith(slideOutHorizontally { width -> -width } + fadeOut())
                        } else {
                            (slideInHorizontally { width -> -width } + fadeIn()).togetherWith(slideOutHorizontally { width -> width } + fadeOut())
                        }.using(SizeTransform(clip = false))
                    },
                    modifier = Modifier.weight(1f)
                ) { step ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        when (step) {
                            1 -> Step1Date(data, { data = data.copy(lastPeriodStart = it) }, textMain, textSub)
                            2 -> Step2PeriodLength(data, { data = data.copy(periodLength = it) }, textMain, textSub, brandPurple)
                            3 -> Step3CycleLength(data, { data = data.copy(cycleLength = it) }, textMain, textSub, brandPurple)
                            4 -> Step4Flow(data, { data = data.copy(flow = it) }, textMain, brandPurple)
                            5 -> Step5Cramps(data, { data = data.copy(cramps = it) }, textMain, brandPurple)
                            6 -> Step6Moods(data, { data = data.copy(moods = it) }, textMain, textSub, brandPurple)
                            7 -> Step7Energy(data, { data = data.copy(energy = it) }, textMain, brandPurple)
                            8 -> Step8Symptoms(data, { data = data.copy(symptoms = it) }, textMain, textSub, brandPurple)
                            9 -> Step9AiSupport(data, { data = data.copy(aiSupport = it) }, textMain, textSub, brandPurple)
                            10 -> Step10Reminders(data, { data = data.copy(reminders = it) }, textMain, textSub, brandPurple)
                        }
                    }
                }

                // Bottom Buttons
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (currentStep > 1) {
                        TextButton(onClick = { currentStep-- }) {
                            Icon(Icons.Default.ChevronLeft, contentDescription = null, modifier = Modifier.size(20.dp), tint = textMain)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Back", color = textMain, fontWeight = FontWeight.Medium)
                        }
                    } else {
                        Spacer(modifier = Modifier.width(80.dp))
                    }

                    Button(
                        onClick = {
                            if (currentStep < totalSteps) {
                                currentStep++
                            } else {
                                onComplete(data)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = brandPurple.copy(alpha = 0.5f), contentColor = textMain),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier.height(48.dp)
                    ) {
                        Text(if (currentStep == totalSteps) "All done" else "Continue", fontWeight = FontWeight.Medium)
                        if (currentStep < totalSteps) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Default.ChevronRight, contentDescription = null, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}

// Below are the step composables. We'll fill them in based on the screenshots.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Step1Date(data: CycleOnboardingData, onDateChanged: (LocalDate) -> Unit, textMain: Color, textSub: Color) {
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = System.currentTimeMillis())

    Text("When did your last period start?", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = textMain, lineHeight = 32.sp)
    Spacer(modifier = Modifier.height(8.dp))
    Text("We use this to gently learn your rhythm.", fontSize = 16.sp, color = textSub)
    Spacer(modifier = Modifier.height(32.dp))

    OutlinedButton(
        onClick = { showDatePicker = true },
        modifier = Modifier.fillMaxWidth().height(56.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = textMain)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(TablerIcons.Calendar, contentDescription = null, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Text(data.lastPeriodStart.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")), fontSize = 16.sp)
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        // Very rough conversion for UI
                        val date = java.time.Instant.ofEpochMilli(it).atZone(java.time.ZoneId.systemDefault()).toLocalDate()
                        onDateChanged(date)
                    }
                    showDatePicker = false
                }) { Text("OK") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
fun Step2PeriodLength(data: CycleOnboardingData, onChanged: (Int) -> Unit, textMain: Color, textSub: Color, brandColor: Color) {
    Text("How many days did your period last?", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = textMain, lineHeight = 32.sp)
    Spacer(modifier = Modifier.height(8.dp))
    Text("Drag the slider to roughly match.", fontSize = 16.sp, color = textSub)
    Spacer(modifier = Modifier.height(48.dp))
    
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text("${data.periodLength}", fontSize = 64.sp, fontWeight = FontWeight.Bold, color = textMain)
            Text(" days", fontSize = 16.sp, color = textSub, modifier = Modifier.padding(bottom = 12.dp))
        }
        Spacer(modifier = Modifier.height(32.dp))
        Slider(
            value = data.periodLength.toFloat(),
            onValueChange = { onChanged(it.toInt()) },
            valueRange = 2f..10f,
            steps = 7,
            colors = SliderDefaults.colors(thumbColor = brandColor, activeTrackColor = brandColor, inactiveTrackColor = brandColor.copy(alpha = 0.2f))
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("2", color = textSub)
            Text("10", color = textSub)
        }
    }
}

@Composable
fun Step3CycleLength(data: CycleOnboardingData, onChanged: (Int) -> Unit, textMain: Color, textSub: Color, brandColor: Color) {
    Text("What's your average cycle length?", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = textMain, lineHeight = 32.sp)
    Spacer(modifier = Modifier.height(8.dp))
    Text("If you don't know, that's okay.", fontSize = 16.sp, color = textSub)
    Spacer(modifier = Modifier.height(48.dp))
    
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text("${data.cycleLength}", fontSize = 64.sp, fontWeight = FontWeight.Bold, color = textMain)
            Text(" days", fontSize = 16.sp, color = textSub, modifier = Modifier.padding(bottom = 12.dp))
        }
        Spacer(modifier = Modifier.height(32.dp))
        Slider(
            value = data.cycleLength.toFloat(),
            onValueChange = { onChanged(it.toInt()) },
            valueRange = 21f..40f,
            steps = 18,
            colors = SliderDefaults.colors(thumbColor = brandColor, activeTrackColor = brandColor, inactiveTrackColor = brandColor.copy(alpha = 0.2f))
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("21", color = textSub)
            Text("40", color = textSub)
        }
        Spacer(modifier = Modifier.height(32.dp))
        OutlinedButton(
            onClick = { onChanged(28) },
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = textMain),
            modifier = Modifier.align(Alignment.Start)
        ) {
            Text("I Don't Know")
        }
    }
}
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun Step4Flow(data: CycleOnboardingData, onChanged: (String) -> Unit, textMain: Color, brandColor: Color) {
    Text("How is your flow usually?", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = textMain, lineHeight = 32.sp)
    Spacer(modifier = Modifier.height(32.dp))
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf("Light", "Medium", "Heavy", "Very Heavy").forEach { flow ->
            val selected = data.flow == flow
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .background(if (selected) brandColor.copy(alpha = 0.5f) else Color.Transparent)
                    .border(1.dp, if (selected) Color.Transparent else textMain.copy(alpha = 0.2f), RoundedCornerShape(24.dp))
                    .clickable { onChanged(flow) }
                    .padding(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Text(flow, color = textMain, fontSize = 16.sp)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun Step5Cramps(data: CycleOnboardingData, onChanged: (String) -> Unit, textMain: Color, brandColor: Color) {
    Text("Do you usually experience cramps?", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = textMain, lineHeight = 32.sp)
    Spacer(modifier = Modifier.height(32.dp))
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf("None", "Mild", "Moderate", "Severe").forEach { cramp ->
            val selected = data.cramps == cramp
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .background(if (selected) brandColor.copy(alpha = 0.5f) else Color.Transparent)
                    .border(1.dp, if (selected) Color.Transparent else textMain.copy(alpha = 0.2f), RoundedCornerShape(24.dp))
                    .clickable { onChanged(cramp) }
                    .padding(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Text(cramp, color = textMain, fontSize = 16.sp)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun Step6Moods(data: CycleOnboardingData, onChanged: (Set<String>) -> Unit, textMain: Color, textSub: Color, brandColor: Color) {
    Text("How do you feel before your period?", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = textMain, lineHeight = 32.sp)
    Spacer(modifier = Modifier.height(8.dp))
    Text("Pick all that feel familiar.", fontSize = 16.sp, color = textSub)
    Spacer(modifier = Modifier.height(32.dp))
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf("Happy", "Calm", "Neutral", "Sensitive", "Sad", "Angry", "Anxious", "Exhausted", "Loved", "Lonely").forEach { mood ->
            val selected = data.moods.contains(mood)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .background(if (selected) brandColor.copy(alpha = 0.5f) else Color.Transparent)
                    .border(1.dp, if (selected) Color.Transparent else textMain.copy(alpha = 0.2f), RoundedCornerShape(24.dp))
                    .clickable { 
                        val newMoods = data.moods.toMutableSet()
                        if (selected) newMoods.remove(mood) else newMoods.add(mood)
                        onChanged(newMoods) 
                    }
                    .padding(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Text(mood, color = textMain, fontSize = 16.sp)
            }
        }
    }
    Spacer(modifier = Modifier.height(16.dp))
    Text("Select any that apply.", fontSize = 14.sp, color = textSub)
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun Step7Energy(data: CycleOnboardingData, onChanged: (String) -> Unit, textMain: Color, brandColor: Color) {
    Text("And your energy before your period?", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = textMain, lineHeight = 32.sp)
    Spacer(modifier = Modifier.height(32.dp))
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf("High", "Normal", "Low", "Very Low").forEach { energy ->
            val selected = data.energy == energy
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .background(if (selected) brandColor.copy(alpha = 0.5f) else Color.Transparent)
                    .border(1.dp, if (selected) Color.Transparent else textMain.copy(alpha = 0.2f), RoundedCornerShape(24.dp))
                    .clickable { onChanged(energy) }
                    .padding(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Text(energy, color = textMain, fontSize = 16.sp)
            }
        }
    }
}
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun Step8Symptoms(data: CycleOnboardingData, onChanged: (Set<String>) -> Unit, textMain: Color, textSub: Color, brandColor: Color) {
    Text("Symptoms you often experience", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = textMain, lineHeight = 32.sp)
    Spacer(modifier = Modifier.height(8.dp))
    Text("Pick all that often show up.", fontSize = 16.sp, color = textSub)
    Spacer(modifier = Modifier.height(32.dp))
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf("Cramps", "Headache", "Back Pain", "Bloating", "Acne", "Mood Swings", "Fatigue", "Breast Tenderness", "Nausea", "Cravings", "Insomnia", "Anxiety", "Low Motivation", "Sensitive Skin", "Constipation", "Diarrhea", "Depression", "None").forEach { symptom ->
            val selected = data.symptoms.contains(symptom)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .background(if (selected) brandColor.copy(alpha = 0.5f) else Color.Transparent)
                    .border(1.dp, if (selected) Color.Transparent else textMain.copy(alpha = 0.2f), RoundedCornerShape(24.dp))
                    .clickable { 
                        val newSymptoms = data.symptoms.toMutableSet()
                        if (symptom == "None") {
                            newSymptoms.clear()
                            newSymptoms.add("None")
                        } else {
                            newSymptoms.remove("None")
                            if (selected) newSymptoms.remove(symptom) else newSymptoms.add(symptom)
                        }
                        onChanged(newSymptoms) 
                    }
                    .padding(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Text(symptom, color = textMain, fontSize = 16.sp)
            }
        }
    }
}

@Composable
fun Step9AiSupport(data: CycleOnboardingData, onChanged: (Boolean) -> Unit, textMain: Color, textSub: Color, brandColor: Color) {
    Text("Emotional support", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = textMain, lineHeight = 32.sp)
    Spacer(modifier = Modifier.height(8.dp))
    Text("AI may share supportive messages on difficult cycle days.", fontSize = 16.sp, color = textSub, lineHeight = 24.sp)
    Spacer(modifier = Modifier.height(32.dp))
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, textMain.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
            .padding(24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Allow gentle AI emotional support on hard days", fontSize = 16.sp, color = textMain, modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.width(16.dp))
        Switch(
            checked = data.aiSupport,
            onCheckedChange = onChanged,
            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = brandColor)
        )
    }
}

@Composable
fun Step10Reminders(data: CycleOnboardingData, onChanged: (Boolean) -> Unit, textMain: Color, textSub: Color, brandColor: Color) {
    Text("Gentle reminders", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = textMain, lineHeight = 32.sp)
    Spacer(modifier = Modifier.height(8.dp))
    Text("We'll let you know a day or two before your period is expected.", fontSize = 16.sp, color = textSub, lineHeight = 24.sp)
    Spacer(modifier = Modifier.height(32.dp))
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, textMain.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
            .padding(24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Remind me before my period begins", fontSize = 16.sp, color = textMain, modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.width(16.dp))
        Switch(
            checked = data.reminders,
            onCheckedChange = onChanged,
            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = brandColor)
        )
    }
}
