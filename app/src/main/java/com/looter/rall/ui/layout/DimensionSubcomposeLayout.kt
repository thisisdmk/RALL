package com.looter.rall.ui.layout

import androidx.annotation.OptIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntSize
import androidx.media3.common.util.UnstableApi

@OptIn(UnstableApi::class)
@Composable
internal fun DimensionSubcomposeLayout(
    modifier: Modifier = Modifier,
    mainContent: @Composable () -> Unit,
    dependentContent: @Composable (IntSize) -> Unit
) {

    SubcomposeLayout(
        modifier = modifier
    ) { constraints: Constraints ->

        val mainPlaceable: Placeable = subcompose(SlotsEnum.Main, mainContent)
            .map {
                it.measure(constraints.copy(minWidth = 0, minHeight = 0))
            }.first()

        val dependentPlaceable: Placeable =
            subcompose(SlotsEnum.Dependent) {
                dependentContent(
                    IntSize(
                        mainPlaceable.width,
                        mainPlaceable.height
                    )
                )
            }.map { it.measure(constraints) }.first()


        layout(mainPlaceable.width, mainPlaceable.height) {
            dependentPlaceable.placeRelative(0, 0)
        }
    }
}

private enum class SlotsEnum { Main, Dependent }