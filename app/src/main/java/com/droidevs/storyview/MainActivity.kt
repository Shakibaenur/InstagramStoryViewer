package com.droidevs.storyview

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.droidevs.storyview.ui.theme.StoryViewTheme
import kotlin.math.max
import kotlin.math.min

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StoryViewTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(Modifier.padding(innerPadding)) {
                        StoryViewer()
                    }
                }
            }
        }
    }
}

@Composable
fun StoryViewer() {
    val images = remember {
        listOf(
            R.drawable.ic_story_1,
            R.drawable.ic_story_2
        )
    }

    val stepCount = images.size
    val currentStep = remember { mutableStateOf(0) }
    val isPaused = remember { mutableStateOf(false) }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
    ) {
        val imageModifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { offset ->
                        currentStep.value = if (offset.x < constraints.maxWidth / 2) {
                            max(0, currentStep.value - 1)
                        } else {
                            min(stepCount - 1, currentStep.value + 1)
                        }
                        isPaused.value = false
                    },
                    onPress = {
                        try {
                            isPaused.value = true
                            awaitRelease()
                        } finally {
                            isPaused.value = false
                        }
                    }
                )
            }
        Image(
            painter = painterResource(id = images[currentStep.value]),
            contentDescription = "StoryImage",
            contentScale = ContentScale.FillHeight,
            modifier = imageModifier
        )
        StoryProgressIndicator(
            modifier = Modifier
                .fillMaxWidth()
                .padding(5.dp),
            stepCount = stepCount,
            stepDuration = 2_000,
            unSelectedColor = Color.LightGray,
            selectedColor = Color.White,
            currentStep = currentStep.value,
            onStepChanged = { currentStep.value = it },
            isPaused = isPaused.value,
            onComplete = { }
        )
    }
}

@Composable
fun StoryProgressIndicator(
    modifier: Modifier = Modifier,
    stepCount: Int,
    stepDuration: Int,
    unSelectedColor: Color,
    selectedColor: Color,
    currentStep: Int,
    onStepChanged: (Int) -> Unit,
    isPaused: Boolean = false,
    onComplete: () -> Unit,
) {
    val currentStepState = remember(currentStep) { mutableStateOf(currentStep) }
    val progress = remember(currentStep) { Animatable(0f) }

    Row(
        modifier = modifier
    ) {
        for (i in 0 until stepCount) {
            val stepProgress = when {
                i == currentStepState.value -> progress.value
                i > currentStepState.value -> 0f
                else -> 1f
            }

            LinearProgressIndicator(
                progress = { stepProgress },
                color = selectedColor,
                trackColor = unSelectedColor,
                modifier = Modifier
                    .weight(1f)
                    .padding(5.dp)
                    .height(5.dp) // Indicator height
            )
        }
    }


    LaunchedEffect(
        isPaused, currentStep
    ) {
        if (isPaused) {
            progress.stop()
        } else {
            for (i in currentStep until stepCount) {
                progress.animateTo(
                    1f,
                    animationSpec = tween(
                        durationMillis = ((1f - progress.value) * stepDuration).toInt(),
                        easing = LinearEasing
                    )
                )
                if (currentStepState.value + 1 <= stepCount - 1) {
                    progress.snapTo(0f)
                    currentStepState.value += 1
                    onStepChanged(currentStepState.value)
                } else {
                    onComplete()
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    StoryViewTheme {
        StoryViewer()
    }
}