import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.mungkive.application.ui.feed.FeedCard
import com.mungkive.application.ui.feed.data.FeedData
import com.mungkive.application.ui.feed.FeedViewModel

@Composable
fun FeedListView(
    viewModel: FeedViewModel = viewModel(),
    onFeedClick: (FeedData) -> Unit
) {
    val feedList by viewModel.feedList.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing = isRefreshing)

    // 최초 진입 시 서버에서 피드 데이터 가져오기
    LaunchedEffect(Unit) {
        viewModel.fetchFeeds()
    }

    SwipeRefresh(
        state = swipeRefreshState,
        onRefresh = { viewModel.refreshFeeds() },
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        if (isLoading && feedList.isEmpty()) {
            // 첫 로딩 시에만 중앙에 로딩 표시
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (feedList.isEmpty() && !isLoading) {
            // 데이터가 없을 때
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "피드가 없습니다",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Gray
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                item {
                    Text(
                        text = "피드",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.W700,
                        fontSize = 24.sp,
                        modifier = Modifier.padding(start = 19.dp, top = 24.dp, bottom = 4.dp)
                    )
                }

                items(feedList) { feedData ->
                    FeedCard(feed = feedData, onClick = { onFeedClick(feedData) })
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
fun FeedListViewPreview() {
    FeedListView(onFeedClick = {})
}