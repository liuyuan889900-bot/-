package com.example.ui.ambiance

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EnergySavingsLeaf
import androidx.compose.material.icons.filled.FilterVintage
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.outlined.Window
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.StitchTokens

/**
 * Stitch Aesthetic Ambiance Picker Sheet
 * Harmonious dark OLED styling matching the global minimalism.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AmbiancePickerSheet(
    onDismissRequest: () -> Unit
) {
    val context = LocalContext.current
    val currentScene by AmbianceManager.currentScene.collectAsState()
    val isLowPower by AmbianceManager.isLowPowerMode.collectAsState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = StitchTokens.SurfaceContainerLow,
        tonalElevation = 0.dp,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 6.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = StitchTokens.SurfaceContainerHigh,
                        modifier = Modifier.size(34.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Filled.AutoAwesome,
                                contentDescription = null,
                                tint = StitchTokens.Primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "场景氛围 · 身临其境",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = StitchTokens.Primary,
                            fontFamily = FontFamily.Serif
                        )
                        Text(
                            text = "营造沉浸灵动的阅读心境",
                            fontSize = 12.sp,
                            color = StitchTokens.Outline
                        )
                    }
                }

                IconButton(onClick = onDismissRequest) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "关闭",
                        tint = StitchTokens.Outline
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = StitchTokens.BorderDivider, thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(12.dp))

            // Scene List
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 12.dp)
            ) {
                items(ReadingAmbiance.entries) { scene ->
                    val isSelected = scene == currentScene
                    val sceneIcon: ImageVector = when (scene) {
                        ReadingAmbiance.NONE -> Icons.AutoMirrored.Filled.MenuBook
                        ReadingAmbiance.AI_FLOW -> Icons.Filled.Bolt
                        ReadingAmbiance.RAIN -> Icons.Filled.WaterDrop
                        ReadingAmbiance.WINDOW_RAIN -> Icons.Outlined.Window
                        ReadingAmbiance.SNOW -> Icons.Filled.AcUnit
                        ReadingAmbiance.STARS -> Icons.Filled.AutoAwesome
                        ReadingAmbiance.PETALS -> Icons.Filled.FilterVintage
                    }

                    Card(
                        onClick = {
                            AmbianceManager.setScene(context, scene)
                        },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) StitchTokens.SurfaceContainerHigh else StitchTokens.SurfaceContainerLowest
                        ),
                        border = BorderStroke(
                            if (isSelected) 1.5.dp else 0.8.dp,
                            if (isSelected) StitchTokens.Primary else StitchTokens.Border
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = if (isSelected) StitchTokens.Primary else StitchTokens.SurfaceContainer,
                                    modifier = Modifier.size(38.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = sceneIcon,
                                            contentDescription = null,
                                            tint = if (isSelected) StitchTokens.OnPrimary else StitchTokens.Outline,
                                            modifier = Modifier.size(19.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "《${scene.title}》",
                                            fontSize = 15.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) StitchTokens.Primary else StitchTokens.OnSurface,
                                            fontFamily = FontFamily.Serif
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            color = if (isSelected) StitchTokens.Primary.copy(alpha = 0.15f) else StitchTokens.SurfaceContainer,
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = scene.poeticTag,
                                                fontSize = 10.sp,
                                                color = if (isSelected) StitchTokens.Primary else StitchTokens.Outline,
                                                fontWeight = FontWeight.Medium,
                                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(3.dp))

                                    Text(
                                        text = scene.poemSnippet,
                                        fontSize = 12.sp,
                                        color = if (isSelected) StitchTokens.Primary.copy(alpha = 0.85f) else StitchTokens.Outline,
                                        fontFamily = FontFamily.Serif
                                    )
                                }
                            }

                            if (isSelected) {
                                Surface(
                                    shape = CircleShape,
                                    color = StitchTokens.Primary,
                                    modifier = Modifier.size(22.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "已选择",
                                            tint = Color.Black,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Low power toggle item
                item {
                    Spacer(modifier = Modifier.height(6.dp))
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = StitchTokens.SurfaceContainerLowest
                        ),
                        border = BorderStroke(0.8.dp, StitchTokens.Border),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.EnergySavingsLeaf,
                                    contentDescription = null,
                                    tint = StitchTokens.Outline,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "低功耗流畅模式",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = StitchTokens.Primary
                                    )
                                    Text(
                                        text = "减少粒子密度与复杂计算，降低能耗",
                                        fontSize = 11.sp,
                                        color = StitchTokens.Outline
                                    )
                                }
                            }

                            Switch(
                                checked = isLowPower,
                                onCheckedChange = { AmbianceManager.setLowPowerMode(context, it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.Black,
                                    checkedTrackColor = StitchTokens.Primary,
                                    uncheckedThumbColor = StitchTokens.Outline,
                                    uncheckedTrackColor = StitchTokens.SurfaceContainerHigh
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
