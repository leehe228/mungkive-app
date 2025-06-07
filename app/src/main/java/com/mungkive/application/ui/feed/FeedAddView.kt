package com.mungkive.application.ui.feed

import android.Manifest
import android.location.Location
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import com.mungkive.application.network.NetworkModule
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil3.compose.AsyncImage
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.location.LocationServices
import com.mungkive.application.R
import com.mungkive.application.repository.PostRepository
import com.mungkive.application.network.dto.PostCreateRequest
import com.mungkive.application.ui.feed.map.getPlaceNameFromLocation
import com.mungkive.application.util.uriToBase64
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch



@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun FeedAddView(
    navController: NavHostController,
    postRepository: PostRepository,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var location by remember { mutableStateOf<String?>(null) }
    var content by remember { mutableStateOf("") }
    var locationRequesting by remember { mutableStateOf(false) }
    var uploadRequesting by remember { mutableStateOf(false) }
    var latLng by remember { mutableStateOf<Pair<Double, Double>?>(null) }


    // 위치 권한
    val locationPermissionState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    // 이미지 권한(갤러리/미디어)
    val galleryPermission = if (Build.VERSION.SDK_INT >= 33) {
        Manifest.permission.READ_MEDIA_IMAGES
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }
    val galleryPermissionState = rememberPermissionState(galleryPermission)

    // 갤러리 런처
    val pickImageLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) imageUri = uri
    }

    // 권한 최초 진입 시 요청
    LaunchedEffect(Unit) {
        // 위치 권한 요청
        locationPermissionState.launchMultiplePermissionRequest()
        // 이미지 권한 요청
        galleryPermissionState.launchPermissionRequest()
    }

    Box(
        modifier = modifier
            .background(Color.White)
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // 상단 바
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = ImageVector.vectorResource(R.drawable.ic_back),
                        contentDescription = "뒤로 가기"
                    )
                }
                Text(
                    text = "피드",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.W700,
                    fontSize = 24.sp
                )
            }

            // 사진 추가 영역
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                contentAlignment = Alignment.Center
            ) {
                if (imageUri != null) {
                    AsyncImage(
                        model = imageUri,
                        contentDescription = "피드 이미지",
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .wrapContentSize()
                            .clickable {
                                if (galleryPermissionState.status.isGranted) {
                                    pickImageLauncher.launch("image/*")
                                } else {
                                    galleryPermissionState.launchPermissionRequest()
                                    Toast.makeText(context, "갤러리 권한이 필요합니다.", Toast.LENGTH_SHORT)
                                        .show()
                                }
                            }
                            .padding(vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = ImageVector.vectorResource(R.drawable.ic_add),
                            contentDescription = "사진 추가",
                            tint = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "사진 추가",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }
            }

            // 구분선
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color(0xFFEDEDED))
            )

            // 현재 위치 (위치값 표시 및 추가/삭제 버튼)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // "현재 위치" 라벨
                Text(
                    text = "현재 위치",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color.Black
                )
                Spacer(Modifier.width(16.dp))
                // 위치가 있을 때만 주소 표시
                if (location != null) {
                    Text(
                        text = location ?: "",
                        fontSize = 12.sp,
                        color = Color.Black,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(
                        onClick = { location = null },
                    ) {
                        Text("위치삭제", fontSize = 12.sp, color = Color(0xFFBDBDBD))
                    }
                } else {
                    // 위치가 없을 때는 추가하기 버튼만
                    Spacer(Modifier.weight(1f))
                    TextButton(
                        onClick = {
                            if (locationPermissionState.allPermissionsGranted) {
                                locationRequesting = true
                                val fusedLocationClient =
                                    LocationServices.getFusedLocationProviderClient(context)
                                if (ActivityCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.ACCESS_FINE_LOCATION
                                    ) == android.content.pm.PackageManager.PERMISSION_GRANTED ||
                                    ActivityCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                                ) {
                                    fusedLocationClient.lastLocation.addOnSuccessListener { loc: Location? ->
                                        if (loc != null) {
                                            latLng = loc.latitude to loc.longitude
                                            getPlaceNameFromLocation(
                                                context,
                                                loc.longitude,
                                                loc.latitude
                                            ) { result ->
                                                location = result
                                                locationRequesting = false
                                            }
                                        } else {
                                            location = "위치 정보 없음"
                                            locationRequesting = false
                                        }
                                    }
                                }
                            } else {
                                locationPermissionState.launchMultiplePermissionRequest()
                                Toast.makeText(context, "위치 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    ) {
                        Text("추가하기", fontSize = 12.sp, color = Color(0xFF3396FF))
                    }
                }
            }


            // 구분선
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color(0xFFEDEDED))
            )

            Column(
                modifier = modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 18.dp, vertical = 18.dp)
            ) {
                Text(
                    text = "캡션 작성",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color.Black
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .background(Color.Transparent)
                ) {
                    BasicTextField(
                        value = content,
                        onValueChange = { content = it },
                        textStyle = TextStyle(
                            color = Color.Black,
                            fontSize = 14.sp
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 200.dp),
                        decorationBox = { innerTextField ->
                            if (content.isEmpty()) {
                                Text(
                                    "여기에 내용을 작성하세요",
                                    color = Color(0xFFBDBDBD),
                                    style = TextStyle(
                                        fontSize = 12.sp,
                                        color = Color(0xFFBDBDBD),
                                        lineHeight = 24.sp
                                    ),
                                )
                            }
                            innerTextField()
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
            // 업로드 버튼
            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 18.dp),
                contentAlignment = Alignment.Center
            ) {
                Button(
                    onClick = {
                        uploadRequesting = true
                        CoroutineScope(Dispatchers.IO).launch {
                            val base64Image = imageUri?.let { uriToBase64(context, it) } ?: ""
                            val latitude = latLng?.first
                            val longitude = latLng?.second
                            val locateString = if (latitude != null && longitude != null) {
                                "$longitude,$latitude"
                            } else {
                                ""
                            }

                            val request = PostCreateRequest(
                                content = content,
                                picture = base64Image,
                                locate = locateString,
                                locName = location ?: "",
                                likes = 0
                            )
                            try {
                                val result = postRepository.createPost(request)
                                launch(Dispatchers.Main) {
                                    uploadRequesting = false
                                    Toast.makeText(context, "업로드 성공", Toast.LENGTH_SHORT).show()
                                    // 피드 화면으로 이동하고 백스택 정리
                                    navController.navigate("feed") {
                                        popUpTo("feed") { inclusive = true }
                                    }
                                }
                            } catch (e: Exception) {
                                launch(Dispatchers.Main) {
                                    uploadRequesting = false
                                    Toast.makeText(
                                        context,
                                        "업로드 실패: ${e.message}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                    },
                    enabled = imageUri != null && location != null && content.isNotBlank() && !uploadRequesting,
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .height(52.dp),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF3378F6)
                    )
                ) {
                    if (uploadRequesting) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = "업로드 중...",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    } else {
                        Text(
                            text = "업로드",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                }
            }
        }

        // 위치 요청 중이면 오버레이 표시
        if (locationRequesting) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color(0x33000000)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}
