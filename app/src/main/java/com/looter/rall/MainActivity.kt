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
import androidx.navigation.NavOptions
import androidx.navigation.NavType.Companion.StringType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.looter.rall.ui.feed.FeedScreen
import com.looter.rall.ui.fullscreen.GalleryScreen
import com.looter.rall.ui.fullscreen.ImageScreen
import com.looter.rall.ui.fullscreen.VideoScreen
import com.looter.rall.ui.post.PostCardControllerImpl
import com.looter.rall.ui.postdetail.PostDetailScreen
import com.looter.rall.ui.theme.AppTheme
import com.looter.rall.ui.videoplayer.LocalVideoPlayerController
import com.looter.rall.ui.videoplayer.VideoPlayerController
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
            FeedScreen(controller = PostCardControllerImpl(navController, context))
        }
        composable(
            "postDetail/{itemId}",
            arguments = listOf(navArgument("itemId") { type = StringType })
        ) {
            PostDetailScreen()
        }
        composable(
            "imageViewer/{imageUrl}",
            arguments = listOf(navArgument("imageUrl") { type = StringType })
        ) {
            ImageScreen(it.arguments?.getString("imageUrl")!!)
        }
        composable(
            "videoViewer/{videoUrl}/{mediaKey}/{postId}",
            arguments = listOf(
                navArgument("videoUrl") { type = StringType },
                navArgument("mediaKey") { type = StringType },
                navArgument("postId") { type = StringType }
            )
        ) {
            VideoScreen(
                it.arguments?.getString("videoUrl")!!,
                it.arguments?.getString("mediaKey")!!
            ) {
                navController.navigate(
                    "postDetail/${it.arguments?.getString("postId")!!}",
                    NavOptions.Builder().setPopUpTo("feed", false).build()
                )
            }
        }
        composable(
            "galleryViewer/{urls}",
            arguments = listOf(navArgument("urls") { type = StringType })
        ) {
            GalleryScreen(it.arguments?.getString("urls")!!.split(",").map(String::trim))
        }
    }
}