/*
 * Copyright 2012-2014 One Platform Foundation
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

package org.onepf.opfpush.model;

import android.support.annotation.NonNull;

/**
 * Registration or unregistering operation error.
 * Divided in two categories: <b>recoverable</b> and <b>unrecoverable</b>.
 * <p/>
 * The <b>recoverable</b> error means that the service can't handle an operation
 * at the moment. The {@link org.onepf.opfpush.OPFPushHelper} retries an operation that cause
 * the recoverable error in the background.
 * <p/>
 * <b>Unrecoverable</b> error means that the error is fatal and the {@link org.onepf.opfpush.OPFPushHelper}
 * can't register this provider. In this case the {@link org.onepf.opfpush.OPFPushHelper} chooses the next
 * available provider. If there are no any other available providers,
 * the {@link org.onepf.opfpush.listener.EventListener#onNoAvailableProvider(android.content.Context, java.util.Map)} method will be called.
 *
 * @author Roman Savin
 * @since 18.03.2015
 */
//todo javadoc
public abstract class PushError<T extends ErrorType> {

    @NonNull
    private final T type;

    @NonNull
    private final String providerName;

    @NonNull
    private final String errorId;

    protected PushError(@NonNull final T type,
                        @NonNull final String providerName,
                        @NonNull final String errorId) {
        this.type = type;
        this.providerName = providerName;
        this.errorId = errorId;
    }

    public abstract boolean isRecoverable();

    @NonNull
    public T getType() {
        return type;
    }

    @NonNull
    public String getProviderName() {
        return providerName;
    }

    @NonNull
    public String getErrorId() {
        return errorId;
    }
}
