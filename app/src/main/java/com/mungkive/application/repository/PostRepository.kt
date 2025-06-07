package com.mungkive.application.repository

import com.mungkive.application.network.ApiService
import com.mungkive.application.network.dto.CommentResponse
import com.mungkive.application.network.dto.PostCreateRequest
import com.mungkive.application.network.dto.PostResponse
import retrofit2.Response

class PostRepository(
    private val apiService: ApiService
) {

    // TODO: 헤더에 토큰 넣는거 뺴기
    suspend fun createPost(request: PostCreateRequest): Response<Void> {
        return apiService.createPost(request)
    }

    suspend fun getComments(postId: Long): List<CommentResponse> {
        return apiService.getComments(postId)
    }

    suspend fun getFeeds(): List<PostResponse> {
        return apiService.listPosts()
    }
}
