package com.mungkive.application.navigation

import FeedListView
import ProfileView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.mungkive.application.models.Routes
import com.mungkive.application.ui.feed.FeedDetailView
import com.mungkive.application.ui.feed.FeedViewModel
import com.mungkive.application.ui.feed.FeedAddView
import com.mungkive.application.ui.map.MapView
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import com.mungkive.application.core.TokenManager
import com.mungkive.application.network.NetworkModule
import com.mungkive.application.repository.PostRepository
import com.mungkive.application.ui.feed.FeedViewModelProviderFactory
import com.mungkive.application.ui.tip.TipListView
import com.mungkive.application.viewmodels.ApiTestViewModel

@Composable
fun MainNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    viewModel: ApiTestViewModel
) {
    val context = LocalContext.current
    val tokenManager = remember { TokenManager(context) }
    val loginService = remember { NetworkModule.provideLoginService() }
    val apiService = remember { NetworkModule.provideApiService(tokenManager, loginService) }
    val postRepository = remember { PostRepository(apiService) }
    val feedViewModelFactory = remember { FeedViewModelProviderFactory(postRepository) }

    NavHost(
        navController = navController,
        startDestination = Routes.Feed.route,
        modifier = modifier
    ) {
        composable(Routes.Map.route) {
            val feedViewModel: FeedViewModel = viewModel(factory = feedViewModelFactory)
            val feedList by feedViewModel.feedList.collectAsState()

            // ViewModel에서 데이터를 불러오도록 호출
            LaunchedEffect(Unit) {
                feedViewModel.fetchFeeds()
            }

            MapView(
                feedList = feedList,
                onFeedSelected = { feedId ->
                    navController.navigate("${Routes.DetailFeed.route}/$feedId")
                }
            )
        }
        composable(Routes.Feed.route) { backStackEntry ->
            // FeedViewModel을 Feed.route 스코프로 생성
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry(Routes.Feed.route)
            }
            val feedViewModel: FeedViewModel = viewModel(parentEntry, factory = feedViewModelFactory)

            FeedListView(
                viewModel = feedViewModel,
                onFeedClick = { feed ->
                    navController.navigate("${Routes.DetailFeed.route}/${feed.id}")
                }
            )
        }

        composable(Routes.Tip.route) { TipListView() }
        composable(Routes.Profile.route) {
            ProfileView(
                viewModel = viewModel
            )
        }
        composable(Routes.FeedWrite.route) { FeedAddView(navController) }

        // 상세 페이지
        composable(Routes.DetailFeed.route + "/{feedId}") { backStackEntry ->
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry(Routes.Feed.route)
            }
            val feedViewModel: FeedViewModel = viewModel(parentEntry, factory = feedViewModelFactory)
            val feedId = backStackEntry.arguments?.getString("feedId") ?: ""
            FeedDetailView(
                feedId = feedId,
                viewModel = feedViewModel,
                navController = navController
            )
        }
    }
}
