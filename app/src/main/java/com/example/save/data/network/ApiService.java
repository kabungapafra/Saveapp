package com.example.save.data.network;

import com.example.save.data.models.MemberEntity;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface ApiService {
    @POST("auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);

    @GET("members")
    Call<List<MemberEntity>> getMembers();

    @POST("members")
    Call<MemberRegistrationResponse> createMember(@Body MemberRegistrationRequest request);
}
