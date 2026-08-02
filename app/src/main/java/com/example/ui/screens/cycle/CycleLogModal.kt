package com.example.ui.screens.cycle

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import compose.icons.TablerIcons
import compose.icons.tablericons.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.CycleLog
import com.example.ui.theme.LocalIsDarkTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CycleLogModal(
    dateStr: String,
    viewModel: CycleViewModel,
    prefilledFlow: String? = null,
    onDismiss: () -> Unit
) {
    val date = LocalDate.parse(dateStr)
    val dayOfWeekLabel = date.format(DateTimeFormatter.ofPattern("EEEE")).uppercase()
    val dateMonthLabel = date.format(DateTimeFormatter.ofPattern("MMMM d"))

    var flow by remember { mutableStateOf<String?>(null) }
    var painLevel by remember { mutableFloatStateOf(0f) }
    val selectedMoods = remember { mutableStateListOf<String>() }
    val symptoms = remember { mutableStateListOf<String>() }
    var emotionalScore by remember { mutableFloatStateOf(3f) }
    var stressScore by remember { mutableFloatStateOf(3f) }
    var supportedScore by remember { mutableFloatStateOf(3f) }
    var anxietyScore by remember { mutableFloatStateOf(3f) }
    var lovedScore by remember { mutableFloatStateOf(3f) }
    var confidenceScore by remember { mutableFloatStateOf(3f) }
    var energyScore by remember { mutableFloatStateOf(3f) }
    var waterMl by remember { mutableStateOf("") }
    var exerciseMin by remember { mutableStateOf("") }
    var medication by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var currentLogId by remember { mutableStateOf(UUID.randomUUID().toString()) }

    val darkTheme = LocalIsDarkTheme.current
    val bgColor = if (darkTheme) Color(0xFF0F172A) else Color.White
    val cardBg = if (darkTheme) Color(0xFF1E293B) else Color(0xFFF8FAFC)
    val softLineColor = if (darkTheme) Color(0xFF334155) else Color(0xFFE2E8F0)
    val titleTextColor = if (darkTheme) Color.White else Color(0xFF1E1E1E)
    val subtextColor = if (darkTheme) Color(0xFF94A3B8) else Color(0xFF71717A)

    LaunchedEffect(dateStr, prefilledFlow) {
        val log = viewModel.getLogForDate(dateStr)
        if (log != null) {
            currentLogId = log.id
            flow = prefilledFlow ?: log.flow
            painLevel = log.painLevel?.toFloat() ?: 0f
            selectedMoods.clear()
            if (!log.mood.isNullOrEmpty()) {
                selectedMoods.addAll(log.mood.split(",").map { it.trim() }.filter { it.isNotEmpty() })
            }
            symptoms.clear()
            if (!log.symptoms.isNullOrEmpty()) {
                symptoms.addAll(log.symptoms.split(","))
            }
            emotionalScore = log.emotionalScore?.toFloat() ?: 3f
            stressScore = log.stressScore?.toFloat() ?: 3f
            supportedScore = log.supportedScore?.toFloat() ?: 3f
            anxietyScore = log.anxietyScore?.toFloat() ?: 3f
            lovedScore = log.lovedScore?.toFloat() ?: 3f
            confidenceScore = log.confidenceScore?.toFloat() ?: 3f
            energyScore = log.energyScore?.toFloat() ?: 3f
            waterMl = log.waterMl?.toString() ?: ""
            exerciseMin = log.exerciseMin?.toString() ?: ""
            medication = log.medication ?: ""
            notes = log.notes ?: ""
        } else {
            flow = prefilledFlow ?: "None"
            selectedMoods.clear()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = bgColor,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .imePadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            // Header row with uppercase day, large bold date, and X close button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = dayOfWeekLabel,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = subtextColor,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = dateMonthLabel,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = titleTextColor
                    )
                }
                
                IconButton(
                    onClick = onDismiss,
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = if (darkTheme) Color(0xFF334155) else Color(0xFFF1F5F9),
                        contentColor = subtextColor
                    ),
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = TablerIcons.X,
                        contentDescription = "Close",
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // FLOW SELECTOR
            Text(
                "Flow",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = titleTextColor
            )
            Spacer(modifier = Modifier.height(10.dp))
            
            // None, Spotting, Light, Medium, Heavy, Very heavy as pill chips
            val flowOptions = listOf(
                "None" to 1,
                "Spotting" to 1,
                "Light" to 2,
                "Medium" to 3,
                "Heavy" to 4,
                "Very heavy" to 5
            )
            
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                flowOptions.forEach { (optionName, dotCount) ->
                    val isSelected = flow == optionName
                    val textCol = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else titleTextColor
                    val backCol = if (isSelected) MaterialTheme.colorScheme.primaryContainer else cardBg
                    val borderCol = if (isSelected) MaterialTheme.colorScheme.primary else softLineColor
                    
                    Surface(
                        shape = CircleShape,
                        color = backCol,
                        border = BorderStroke(1.dp, borderCol),
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable { flow = optionName }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Render dots
                            repeat(dotCount) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) Color(0xFF2E1065) else Color(0xFFF43F5E))
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            Text(
                                text = optionName,
                                color = textCol,
                                fontSize = 14.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // PAIN SLIDER
            Text(
                "Pain",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = titleTextColor
            )
            val painLabel = when (painLevel.toInt()) {
                0 -> "None"
                in 1..3 -> "Mild"
                in 4..6 -> "Moderate"
                else -> "Severe"
            }
            Text(
                text = painLabel,
                fontSize = 13.sp,
                color = subtextColor
            )
            
            Spacer(modifier = Modifier.height(10.dp))
            
            // Large Bold Score Label matching screenshot "0 / 10" with 0 huge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = "${painLevel.toInt()}",
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    color = titleTextColor
                )
                Text(
                    text = " / 10",
                    fontSize = 16.sp,
                    color = subtextColor,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Slider(
                value = painLevel,
                onValueChange = { painLevel = it },
                valueRange = 0f..10f,
                steps = 9,
                modifier = Modifier.fillMaxWidth(),
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary,
                    inactiveTrackColor = softLineColor
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // MOOD SELECTOR
            Text(
                "Mood",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = titleTextColor
            )
            Spacer(modifier = Modifier.height(12.dp))
            
            val moodOptions = listOf(
                Triple("Happy", TablerIcons.Sun, Color(0xFFFEF3C7)),
                Triple("Calm", TablerIcons.Moon, Color(0xFFF1F5F9)),
                Triple("Neutral", TablerIcons.MoodSmile, Color(0xFFECFDF5)),
                Triple("Sensitive", TablerIcons.Star, Color(0xFFFCE7F3)),
                Triple("Sad", TablerIcons.CloudRain, Color(0xFFE0F2FE)),
                Triple("Angry", TablerIcons.Flame, Color(0xFFFEE2E2)),
                Triple("Anxious", TablerIcons.Wind, Color(0xFFF3E8FF)),
                Triple("Exhausted", TablerIcons.Cloud, Color(0xFFF5F5F4)),
                Triple("Loved", TablerIcons.Heart, Color(0xFFFFE4E6)),
                Triple("Lonely", TablerIcons.Users, Color(0xFFE0F2FE))
            )
            
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Line 1
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    moodOptions.take(5).forEach { (moodName, icon, baseBg) ->
                        val isSelected = selectedMoods.contains(moodName)
                        MoodItem(
                            name = moodName,
                            icon = icon,
                            baseColor = baseBg,
                            isSelected = isSelected,
                            titleTextColor = titleTextColor,
                            subtextColor = subtextColor,
                            softLineColor = softLineColor,
                            onClick = {
                                if (isSelected) selectedMoods.remove(moodName) else selectedMoods.add(moodName)
                            }
                        )
                    }
                }
                // Line 2
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    moodOptions.drop(5).forEach { (moodName, icon, baseBg) ->
                        val isSelected = selectedMoods.contains(moodName)
                        MoodItem(
                            name = moodName,
                            icon = icon,
                            baseColor = baseBg,
                            isSelected = isSelected,
                            titleTextColor = titleTextColor,
                            subtextColor = subtextColor,
                            softLineColor = softLineColor,
                            onClick = {
                                if (isSelected) selectedMoods.remove(moodName) else selectedMoods.add(moodName)
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // SYMPTOMS CHIPS
            Text(
                "Symptoms",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = titleTextColor
            )
            Spacer(modifier = Modifier.height(12.dp))
            val symptomOptions = listOf(
                "Cramps", "Headache", "Back Pain", "Bloating", "Acne", "Mood Swings", 
                "Fatigue", "Breast Tenderness", "Nausea", "Cravings", "Insomnia", 
                "Anxiety", "Low Motivation", "Sensitive Skin", "Constipation", "Diarrhea", 
                "Depression", "None"
            )
            
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                symptomOptions.forEach { s ->
                    val isSelected = symptoms.contains(s)
                    val backCol = if (isSelected) MaterialTheme.colorScheme.primaryContainer else cardBg
                    val textCol = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else titleTextColor
                    val borderCol = if (isSelected) MaterialTheme.colorScheme.primary else softLineColor
                    
                    Surface(
                        shape = CircleShape,
                        color = backCol,
                        border = BorderStroke(1.dp, borderCol),
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable { 
                                if (s == "None") {
                                    symptoms.clear()
                                    symptoms.add("None")
                                } else {
                                    symptoms.remove("None")
                                    if (isSelected) symptoms.remove(s) else symptoms.add(s)
                                }
                            }
                    ) {
                        Text(
                            text = s,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                            fontSize = 14.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = textCol
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // HOW YOU FEEL SLIDERS
            Text(
                "How you feel",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = titleTextColor
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                "Slide to where you are right now",
                fontSize = 13.sp,
                color = subtextColor
            )
            Spacer(modifier = Modifier.height(12.dp))

            FeelSliderCustom("How are you feeling emotionally today?", emotionalScore, titleTextColor, subtextColor, softLineColor) { emotionalScore = it }
            FeelSliderCustom("How stressed do you feel?", stressScore, titleTextColor, subtextColor, softLineColor) { stressScore = it }
            FeelSliderCustom("Do you feel emotionally supported today?", supportedScore, titleTextColor, subtextColor, softLineColor) { supportedScore = it }
            FeelSliderCustom("How anxious do you feel?", anxietyScore, titleTextColor, subtextColor, softLineColor) { anxietyScore = it }
            FeelSliderCustom("How loved do you feel?", lovedScore, titleTextColor, subtextColor, softLineColor) { lovedScore = it }
            FeelSliderCustom("How confident do you feel?", confidenceScore, titleTextColor, subtextColor, softLineColor) { confidenceScore = it }
            FeelSliderCustom("How is your energy today?", energyScore, titleTextColor, subtextColor, softLineColor) { energyScore = it }

            Spacer(modifier = Modifier.height(28.dp))

            // BODY CARE inputs
            Text(
                "Body care",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = titleTextColor
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = waterMl,
                    onValueChange = { waterMl = it },
                    label = { Text("WATER", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = subtextColor) },
                    placeholder = { Text("0 ml", fontSize = 14.sp, color = subtextColor) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = cardBg,
                        unfocusedContainerColor = cardBg,
                        focusedBorderColor = Color(0xFF8B5CF6),
                        unfocusedBorderColor = softLineColor,
                    ),
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp, color = titleTextColor)
                )
                OutlinedTextField(
                    value = exerciseMin,
                    onValueChange = { exerciseMin = it },
                    label = { Text("EXERCISE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = subtextColor) },
                    placeholder = { Text("0 min", fontSize = 14.sp, color = subtextColor) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = cardBg,
                        unfocusedContainerColor = cardBg,
                        focusedBorderColor = Color(0xFF8B5CF6),
                        unfocusedBorderColor = softLineColor,
                    ),
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp, color = titleTextColor)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            OutlinedTextField(
                value = medication,
                onValueChange = { medication = it },
                placeholder = { Text("Medication or supplements", fontSize = 14.sp, color = subtextColor) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = cardBg,
                    unfocusedContainerColor = cardBg,
                    focusedBorderColor = Color(0xFF8B5CF6),
                    unfocusedBorderColor = softLineColor,
                ),
                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp, color = titleTextColor)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // NOTES input block
            Text(
                "Notes",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = titleTextColor
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                placeholder = { Text("Anything you want to remember about today...", fontSize = 14.sp, color = subtextColor) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = cardBg,
                    unfocusedContainerColor = cardBg,
                    focusedBorderColor = Color(0xFF8B5CF6),
                    unfocusedBorderColor = softLineColor,
                ),
                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp, color = titleTextColor)
            )

            Spacer(modifier = Modifier.height(28.dp))

            // BOTTOM ACTION BUTTONS: Close & Save log
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 36.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .clip(CircleShape)
                        .clickable { onDismiss() },
                    shape = CircleShape,
                    color = cardBg,
                    border = BorderStroke(1.dp, softLineColor)
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = TablerIcons.X,
                            contentDescription = null,
                            tint = titleTextColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Close",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = titleTextColor
                        )
                    }
                }
                
                Surface(
                    modifier = Modifier
                        .weight(1.3f)
                        .height(50.dp)
                        .clip(CircleShape)
                        .clickable {
                            val log = CycleLog(
                                id = currentLogId,
                                date = dateStr,
                                flow = flow,
                                painLevel = painLevel.toInt(),
                                mood = selectedMoods.joinToString(",").takeIf { it.isNotBlank() },
                                symptoms = symptoms.joinToString(","),
                                emotionalScore = emotionalScore.toInt(),
                                stressScore = stressScore.toInt(),
                                supportedScore = supportedScore.toInt(),
                                anxietyScore = anxietyScore.toInt(),
                                lovedScore = lovedScore.toInt(),
                                confidenceScore = confidenceScore.toInt(),
                                energyScore = energyScore.toInt(),
                                waterMl = waterMl.toIntOrNull(),
                                exerciseMin = exerciseMin.toIntOrNull(),
                                medication = medication.takeIf { it.isNotBlank() },
                                notes = notes.takeIf { it.isNotBlank() },
                                createdAt = System.currentTimeMillis()
                            )
                            viewModel.saveLog(log)
                            onDismiss()
                        },
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = TablerIcons.Bookmark,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Save log",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MoodItem(
    name: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    baseColor: Color,
    isSelected: Boolean,
    titleTextColor: Color,
    subtextColor: Color,
    softLineColor: Color,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clickable { onClick() }
            .width(58.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .background(baseColor)
                .border(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else softLineColor,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = name,
                tint = Color(0xFF334155),
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = name,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) Color(0xFF8B5CF6) else subtextColor,
            maxLines = 1
        )
    }
}

@Composable
fun FeelSliderCustom(
    label: String,
    value: Float,
    titleTextColor: Color,
    subtextColor: Color,
    softLineColor: Color,
    onValueChange: (Float) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = titleTextColor
            )
            Text(
                text = "${value.toInt()}/5",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = 1f..5f,
            steps = 3,
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = softLineColor
            )
        )
    }
}
