package com.example.pyxis.ui.nav

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.pyxis.ui.screens.*
import com.example.pyxis.viewmodel.InventoryViewModel

object Routes {
    const val DASHBOARD        = "dashboard"
    const val ROOM_DETAIL      = "room/{locationId}"
    const val CONTAINER_DETAIL = "container/{containerId}"
    const val ITEM_DETAIL      = "item/{itemId}"
    const val CATEGORIES       = "categories"

    fun roomDetail(locationId: Long)       = "room/$locationId"
    fun containerDetail(containerId: Long) = "container/$containerId"
    fun itemDetail(itemId: Long)           = "item/$itemId"
}

@Composable
fun AppNavHost(viewModel: InventoryViewModel) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.DASHBOARD) {

        composable(Routes.DASHBOARD) {
            DashboardScreen(
                viewModel        = viewModel,
                onLocationClick  = { id -> navController.navigate(Routes.roomDetail(id)) },
                onItemClick      = { id -> navController.navigate(Routes.itemDetail(id)) },
                onCategoriesClick = { navController.navigate(Routes.CATEGORIES) }
            )
        }

        composable(
            route = Routes.ROOM_DETAIL,
            arguments = listOf(navArgument("locationId") { type = NavType.LongType })
        ) { back ->
            RoomDetailScreen(
                locationId        = back.arguments!!.getLong("locationId"),
                viewModel         = viewModel,
                onContainerClick  = { id -> navController.navigate(Routes.containerDetail(id)) },
                onItemClick       = { id -> navController.navigate(Routes.itemDetail(id)) },
                onBack            = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.CONTAINER_DETAIL,
            arguments = listOf(navArgument("containerId") { type = NavType.LongType })
        ) { back ->
            ContainerDetailScreen(
                containerId           = back.arguments!!.getLong("containerId"),
                viewModel             = viewModel,
                onChildContainerClick = { id -> navController.navigate(Routes.containerDetail(id)) },
                onItemClick           = { id -> navController.navigate(Routes.itemDetail(id)) },
                onBack                = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.ITEM_DETAIL,
            arguments = listOf(navArgument("itemId") { type = NavType.LongType })
        ) { back ->
            ItemDetailScreen(
                itemId    = back.arguments!!.getLong("itemId"),
                viewModel = viewModel,
                onBack    = { navController.popBackStack() },
                onDeleted = { navController.popBackStack() }
            )
        }

        composable(Routes.CATEGORIES) {
            CategoriesScreen(
                viewModel = viewModel,
                onBack    = { navController.popBackStack() }
            )
        }
    }
}