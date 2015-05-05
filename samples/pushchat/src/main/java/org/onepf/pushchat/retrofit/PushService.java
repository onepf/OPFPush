/*
 * Copyright 2012-2015 One Platform Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.onepf.pushchat.retrofit;

import android.support.annotation.NonNull;

import org.onepf.pushchat.model.request.RegistrationRequestBody;
import org.onepf.pushchat.model.request.UnregistrationRequestBody;
import org.onepf.pushchat.model.request.push.PushMessageRequestBody;
import org.onepf.pushchat.model.response.ExistResponse;
import org.onepf.pushchat.model.response.RegistrationResponse;
import org.onepf.pushchat.model.response.UnregistrationResponse;
import org.onepf.pushchat.model.response.push.PushMessageResponse;

import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Query;

/**
 * @author Roman Savin
 * @since 19.04.2015
 */
public interface PushService {

    @POST("/register")
    void register(@Body @NonNull final RegistrationRequestBody body,
                  @NonNull final Callback<RegistrationResponse> callback);

    @POST("/unregister")
    void unregister(@Body @NonNull final UnregistrationRequestBody body,
                    @NonNull final Callback<UnregistrationResponse> callback);

    @POST("/push")
    void push(@Body @NonNull final PushMessageRequestBody body,
              @NonNull final Callback<PushMessageResponse> callback);

    @GET("/exist")
    void exist(@Query("uuid") @NonNull final String uuid,
               final Callback<ExistResponse> callback);
}
