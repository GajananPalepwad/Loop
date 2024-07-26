import com.gn4k.loop.models.request.AddBadgeRequest
import com.gn4k.loop.models.request.AddProjectRequest
import com.gn4k.loop.models.request.CreateMeetRequest
import com.gn4k.loop.models.response.Post
import com.gn4k.loop.models.request.CreatePostRequestForLinkNCode
import com.gn4k.loop.models.request.FollowUnfollowRequest
import com.gn4k.loop.models.request.GetPostsByAuthorRequest
import com.gn4k.loop.models.request.HomeFeedRequest
import com.gn4k.loop.models.request.JoinLeaveMeetRequest
import com.gn4k.loop.models.request.JoinRequest
import com.gn4k.loop.models.request.LikeDislikeCommentRequest
import com.gn4k.loop.models.request.LikeDislikeReplyRequest
import com.gn4k.loop.models.request.LikeDislikeRequest
import com.gn4k.loop.models.request.LoginRequest
import com.gn4k.loop.models.request.MakeCommentRequest
import com.gn4k.loop.models.request.MarkSeenPostRequest
import com.gn4k.loop.models.request.MarkSeenRequest
import com.gn4k.loop.models.request.RegisterRequest
import com.gn4k.loop.models.request.ReplyComment
import com.gn4k.loop.models.request.SendMsgRequest
import com.gn4k.loop.models.request.UpdateProfileRequest
import com.gn4k.loop.models.request.UserIdRequest
import com.gn4k.loop.models.request.UserRequest
import com.gn4k.loop.models.response.ChattingRespnse
import com.gn4k.loop.models.response.ConversationsResponse
import com.gn4k.loop.models.response.CreateMeetingResponse
import com.gn4k.loop.models.response.CreatePostResponse
import com.gn4k.loop.models.response.ExploreResponse
import com.gn4k.loop.models.response.FetchCommentsResponse
import com.gn4k.loop.models.response.FollowUnfollowResponse
import com.gn4k.loop.models.response.FollowerResponse
import com.gn4k.loop.models.response.FollowingResponse
import com.gn4k.loop.models.response.GetProjects
import com.gn4k.loop.models.response.MarkSeenResponse
import com.gn4k.loop.models.response.MeetingResponse
import com.gn4k.loop.models.response.Posts
import com.gn4k.loop.models.response.ReplyListResponse
import com.gn4k.loop.models.response.SearchUserResponse
import com.gn4k.loop.models.response.SendMsgResponse
import com.gn4k.loop.models.response.Skill
import com.gn4k.loop.models.response.UserAllDataResponse
import com.gn4k.loop.models.response.UserResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
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

    @POST("accept_reject_project_request.php")
    fun acceptRejectProjectRequest(@Body request: JoinRequest): Call<CreateMeetingResponse>
}
