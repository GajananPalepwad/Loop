import com.gn4k.loop.models.request.*
import com.gn4k.loop.models.response.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Query

interface ApiService {
    @POST("register.php")
    fun registerUser(@Body user: RegisterRequest?): Call<UserResponse?>?

    @POST("login.php")
    fun loginUser(@Body user: LoginRequest?): Call<UserResponse?>?

    @POST("getUser.php")
    fun getUserData(@Body user: UserRequest?): Call<UserAllDataResponse?>?

    @POST("get_followers_list.php")
    fun getFollowersList(@Body user: UserRequest?): Call<FollowerResponse?>?

    @POST("get_following_list.php")
    fun getFollowingList(@Body user: UserRequest?): Call<FollowingResponse?>?

    @Multipart
    @POST("create_post_with_img.php")
    fun createPost(@Part("author_id") authorId: RequestBody, @Part("context") context: RequestBody,
        @Part("type") type: RequestBody, @Part("tags") tags: RequestBody?,
        @Part("parent_post_id") parentPostId: RequestBody?, @Part image: MultipartBody.Part?): Call<CreatePostResponse>

    @POST("create_post_with_link_and_code.php")
    fun createPostWithLinkAndCode(@Body postData: CreatePostRequestForLinkNCode): Call<CreatePostResponse>

    @POST("get_posts_by_author.php")
    fun getPostsByAuthor(@Body request: GetPostsByAuthorRequest): Call<List<Post>>

    @POST("get_post_by_id.php")
    fun getPostById(@Body request: GetPostByIdRequest): Call<Post>

    @POST("home_feed.php")
    fun getHomeFeed(@Body request: HomeFeedRequest): Call<Posts>

    @Multipart
    @POST("upload_profile_photo.php")
    fun uploadProfilePhoto(@Part("user_id") userId: RequestBody, @Part profilePhoto: MultipartBody.Part): Call<UserResponse>

    @POST("edit_profile.php")
    fun editProfile(@Body request: UpdateProfileRequest): Call<UserResponse>

    @GET("get_skill_list.php")
    fun getSkills(): Call<Skill>

    @POST("like_a_post.php")
    fun doLike(@Body request: LikeDislikeRequest): Call<UserResponse>

    @POST("unlike_a_post.php")
    fun doUnlike(@Body request: LikeDislikeRequest): Call<UserResponse>

    @POST("make_a_comment.php")
    fun doComment(@Body request: MakeCommentRequest): Call<UserResponse>

    @POST("fetch_comments.php")
    fun fetchComments(@Body request: LikeDislikeRequest): Call<FetchCommentsResponse>

    @POST("like_dislike_comment.php")
    fun likeDislikeComment(@Body request: LikeDislikeCommentRequest): Call<UserResponse>

    @POST("reply_to_comment.php")
    fun doReply(@Body request: ReplyComment): Call<UserResponse>

    @GET("fetch_reply.php")
    fun fetchReply(@Query("comment_id") commentId: Int, @Query("login_id") loginId: Int): Call<ReplyListResponse>

    @POST("like_unlike_reply.php")
    fun likeDislikeReply(@Body request: LikeDislikeReplyRequest): Call<UserResponse>

    @GET("search_user.php")
    fun searchUser(@Query("q") query: String): Call<SearchUserResponse>

    @POST("follow_unfollow.php")
    fun followUnfollow(@Body request: FollowUnfollowRequest): Call<FollowUnfollowResponse>

    @GET("explore.php")
    fun fetchExplore(@Query("user_id") userId: Int): Call<ExploreResponse>

    @GET("get_request_conversation.php")
    fun fetchRequestedConversation(@Query("user_id") userId: Int): Call<ConversationsResponse>

    @GET("get_conversations.php")
    fun fetchConversation(@Query("user_id") userId: Int): Call<ConversationsResponse>

    @GET("get_messages.php")
    fun getConversation(@Query("user_id") userId: Int, @Query("last_message_time") lastMessageTime: String): Call<ConversationsResponse>

    @POST("send_message.php")
    fun sendMsg(@Body request: SendMsgRequest): Call<SendMsgResponse>

    @POST("mark_message_seen.php")
    fun markMsgSeen(@Body request: MarkSeenRequest): Call<MarkSeenResponse>

    @POST("mark_post_seen.php")
    fun markPostSeen(@Body requestBody: MarkSeenPostRequest): Call<MarkSeenResponse>

    @GET("get_messages.php")
    fun getMsg(@Query("conversation_id") conversationId: Int, @Query("last_message_id") lastMessageId: Int): Call<ChattingRespnse>

    @GET("fetch_msg_history.php")
    fun fetchAllMsg(@Query("conversation_id") conversationId: Int): Call<ChattingRespnse>

    @POST("create_meet.php")
    fun createMeeting(@Body request: CreateMeetRequest): Call<CreateMeetingResponse>

    @GET("fetch_meet_list.php")
    fun fetchAllMeetings(): Call<MeetingResponse>

    @POST("get_interested_meeting.php")
    fun fetchInterestedMeetings(@Body request: UserIdRequest): Call<MeetingResponse>

    @POST("join_leave_meet.php")
    fun joinLeaveMeet(@Body request: JoinLeaveMeetRequest): Call<CreateMeetingResponse>

    @POST("interest_meeting.php")
    fun showInterestMeet(@Body request: JoinLeaveMeetRequest): Call<CreateMeetingResponse>

    @POST("add_badge.php")
    fun addBadges(@Body request: AddBadgeRequest): Call<CreateMeetingResponse>

    @GET("get_all_projects.php")
    fun fetchAllProjects(@Query("status") status: String): Call<GetProjects>

    @GET("get_projects_by_author.php")
    fun fetchProjectByAuthor(@Query("authorId") authorId: Int): Call<GetProjects>

    @POST("project_join_request.php")
    fun sendProjectJoinRequest(@Body request: JoinRequest): Call<CreateMeetingResponse>

    @POST("add_project.php")
    fun addProject(@Body request: AddProjectRequest): Call<CreateMeetingResponse>

    @PUT("update_project.php")
    fun updateProject(@Body request: UpdateProjectRequest): Call<CreateMeetingResponse>

    @POST("accept_reject_project_request.php")
    fun acceptRejectProjectRequest(@Body request: JoinRequest): Call<CreateMeetingResponse>

    @GET("remove_contributor.php")
    fun removeContributor(@Query("projectId") projectId: Int, @Query("userId") userId: Int): Call<GetProjects>

    @POST("send_notification.php")
    fun saveNotification(@Body request: NotificationRequest): Call<CreateMeetingResponse>

    @GET("fetch_notifications.php")
    fun fetchNotifications(@Query("userId") userId: Int, @Query("type") type: String): Call<NotificationListResponse>

    @GET("recent_like.php")
    fun fetchRecentLikes(@Query("login_id") userId: Int): Call<List<Post>>

    @GET("recent_comments.php")
    fun fetchRecentComments(@Query("login_id") userId: Int): Call<List<Post>>


}
