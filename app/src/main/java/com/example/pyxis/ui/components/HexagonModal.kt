package com.example.pyxis.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.pyxis.R

/**
 * The + modal using the real Figma SVG assets.
 *
 * The modal background (ic_modal_background) has a natural aspect ratio of 943:1070.
 * Each button SVG (ic_btn_*) is 261×294 — all identical in size.
 *
 * Button centre positions as fractions of the 943×1070 modal canvas,
 * derived by locating each button's hex centre in the full-modal reference SVG:
 *
 *   category:  cx=0.2063  cy=0.4769
 *   item:      cx=0.4926  cy=0.4768
 *   container: cx=0.7800  cy=0.4769
 *   room:      cx=0.3505  cy=0.6928
 *   cancel:    cx=0.6368  cy=0.6928
 *
 * Button width  fraction of modal width:  261/943 = 0.2768
 * Button height fraction of modal height: 294/1070 = 0.2748
 */

private const val MODAL_ASPECT = 1070f / 943f   // height / width

private const val BTN_W_FRAC = 261f / 943f
private const val BTN_H_FRAC = 294f / 1070f

private data class BtnPos(val cx: Float, val cy: Float)

// 5-button (dashboard) layout
private val POS_CATEGORY  = BtnPos(0.2063f, 0.4769f)
private val POS_ITEM      = BtnPos(0.4926f, 0.4768f)
private val POS_CONTAINER = BtnPos(0.7800f, 0.4769f)
private val POS_ROOM      = BtnPos(0.3505f, 0.6928f)
private val POS_CANCEL    = BtnPos(0.6368f, 0.6928f)

// 3-button (room/container) layout — item and container pulled inward, cancel centred below
private val POS_3_ITEM      = BtnPos(0.3505f, 0.4769f)
private val POS_3_CONTAINER = BtnPos(0.6368f, 0.4769f)
private val POS_3_CANCEL    = BtnPos(0.4926f, 0.6928f)

// ── Dashboard modal — 5 buttons ────────────────────────────────────────────────

@Composable
fun DashboardAddModal(
    onCategory:  () -> Unit,
    onItem:      () -> Unit,
    onContainer: () -> Unit,
    onRoom:      () -> Unit,
    onDismiss:   () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            val modalW = maxWidth
            val modalH = modalW * MODAL_ASPECT

            ModalCanvas(modalW = modalW, modalH = modalH) {
                // Title — positioned in the upper portion of the modal background
                androidx.compose.material3.Text(
                    text = "Select what you want\nto create:",
                    style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                    color = androidx.compose.ui.graphics.Color.White,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier
                        .align(androidx.compose.ui.Alignment.TopCenter)
                        .padding(top = modalH * 0.18f)
                        .fillMaxWidth()
                )
                ModalButton(R.drawable.ic_btn_category,  "category",  POS_CATEGORY,  modalW, modalH, onCategory)
                ModalButton(R.drawable.ic_btn_item,      "item",      POS_ITEM,      modalW, modalH, onItem)
                ModalButton(R.drawable.ic_btn_container, "container", POS_CONTAINER, modalW, modalH, onContainer)
                ModalButton(R.drawable.ic_btn_room,      "room",      POS_ROOM,      modalW, modalH, onRoom)
                ModalButton(R.drawable.ic_btn_cancel,    "cancel",    POS_CANCEL,    modalW, modalH, onDismiss)
            }
        }
    }
}

// ── Room / Container modal — 3 buttons ────────────────────────────────────────

@Composable
fun RoomAddModal(
    onItem:      () -> Unit,
    onContainer: () -> Unit,
    onDismiss:   () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
        ) {
            val modalW = maxWidth
            val modalH = modalW * MODAL_ASPECT

            ModalCanvas(modalW = modalW, modalH = modalH) {
                androidx.compose.material3.Text(
                    text = "Select what you want\nto create:",
                    style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                    color = androidx.compose.ui.graphics.Color.White,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier
                        .align(androidx.compose.ui.Alignment.TopCenter)
                        .padding(top = modalH * 0.18f)
                        .fillMaxWidth()
                )
                ModalButton(R.drawable.ic_btn_item,      "item",      POS_3_ITEM,      modalW, modalH, onItem)
                ModalButton(R.drawable.ic_btn_container, "container", POS_3_CONTAINER, modalW, modalH, onContainer)
                ModalButton(R.drawable.ic_btn_cancel,    "cancel",    POS_3_CANCEL,    modalW, modalH, onDismiss)
            }
        }
    }
}

// ── Shared modal canvas ────────────────────────────────────────────────────────

@Composable
private fun ModalCanvas(
    modalW: Dp,
    modalH: Dp,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = Modifier
            .width(modalW)
            .height(modalH)
    ) {
        // Background
        Image(
            painter = painterResource(R.drawable.ic_modal_background),
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.matchParentSize()
        )
        content()
    }
}

// ── Single button placed by centre fraction ────────────────────────────────────

@Composable
private fun BoxScope.ModalButton(
    drawableRes: Int,
    contentDesc: String,
    pos: BtnPos,
    modalW: Dp,
    modalH: Dp,
    onClick: () -> Unit
) {
    val btnW = modalW * BTN_W_FRAC
    val btnH = modalH * BTN_H_FRAC
    val offsetX = modalW * pos.cx - btnW / 2
    val offsetY = modalH * pos.cy - btnH / 2

    Column(
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
        modifier = Modifier
            .width(btnW)
            .absoluteOffset(x = offsetX, y = offsetY)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = false),
                onClick = onClick
            )
    ) {
        Image(
            painter = painterResource(drawableRes),
            contentDescription = contentDesc,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier
                .size(btnW, btnH)
                .shadow(
                    elevation = 8.dp,
                    shape = androidx.compose.foundation.shape.GenericShape { size, _ ->
                        addRect(
                            androidx.compose.ui.geometry.Rect(0f, 0f, size.width, size.height)
                        )
                    },
                    ambientColor = Color(0x40000000),
                    spotColor = Color(0x40000000)
                )
        )
    }
}

// ── Custom FAB ────────────────────────────────────────────────────────────────

@Composable
fun PyxisFab(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(56.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = false),
                onClick = onClick
            )
    ) {
        Image(
            painter = painterResource(R.drawable.ic_fab_add),
            contentDescription = "Add",
            modifier = Modifier.fillMaxSize()
        )
    }
}