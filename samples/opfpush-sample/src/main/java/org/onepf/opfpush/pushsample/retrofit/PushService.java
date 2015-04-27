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

package org.onepf.opfpush.pushsample.retrofit;

import org.onepf.opfpush.pushsample.model.request.push.PushMessageRequestBody;
import org.onepf.opfpush.pushsample.model.request.RegistrationRequestBody;
import org.onepf.opfpush.pushsample.model.request.UnregistrationRequestBody;
import org.onepf.opfpush.pushsample.model.response.push.PushMessageResponse;
import org.onepf.opfpush.pushsample.model.response.RegistrationResponse;
import org.onepf.opfpush.pushsample.model.response.UnregistrationResponse;

import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.POST;

/**
 * @author Roman Savin
 * @since 19.04.2015
 */
public interface PushService {

    @POST("/register")
    void register(@Body final RegistrationRequestBody body,
                  final Callback<RegistrationResponse> callback);

    @POST("/unregister")
    void unregister(@Body final UnregistrationRequestBody body,
                    final Callback<UnregistrationResponse> callback);

    @POST("/push")
    void push(@Body final PushMessageRequestBody body,
              final Callback<PushMessageResponse> callback);
}
