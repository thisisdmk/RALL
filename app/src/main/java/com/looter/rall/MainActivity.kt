package com.looter.rall

import android.content.Context
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavType.Companion.StringType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.looter.rall.ui.feedlist.FeedScreen
import com.looter.rall.ui.postdetail.PostDetailScreen
import com.looter.rall.ui.theme.AppTheme
import com.looter.rall.videoplayer.LocalVideoPlayerController
import com.looter.rall.videoplayer.VideoPlayerController
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @OptIn(UnstableApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                CompositionLocalProvider(
                    LocalVideoPlayerController provides VideoPlayerController(LocalContext.current).apply { SyncWithLifecycleOwner() }
                ) {
                    Surface(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        ScreenTransition(this)
                    }
                }
            }
        }
    }
}

@Composable
fun ScreenTransition(context: Context) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "feed") {
        composable("feed") {
            FeedScreen(navController = navController)
        }
        composable(
            "subreddit/{subredditName}",
            arguments = listOf(navArgument("subredditName") { type = StringType })
        ) { backStackEntry ->
            FeedScreen(
                navController = navController,
                subreddit = backStackEntry.arguments?.getString("subredditName")
            )
        }
        composable(
            "postDetail/{itemId}",
            arguments = listOf(navArgument("itemId") { type = StringType })
        ) {
            PostDetailScreen(isFullScreen = false, navController = navController)
        }
        composable(
            "imageViewer/{itemId}",
            arguments = listOf(navArgument("itemId") { type = StringType })
        ) {
            PostDetailScreen(isFullScreen = true, navController = navController)
        }
        composable(
            "videoViewer/{itemId}",
            arguments = listOf(navArgument("itemId") { type = StringType })
        ) {
            PostDetailScreen(isFullScreen = true, navController = navController)
        }
        composable(
            "galleryViewer/{urls}",
            arguments = listOf(navArgument("urls") { type = StringType })
        ) {
            PostDetailScreen(isFullScreen = true, navController = navController)
        }
    }
}