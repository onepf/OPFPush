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

package org.onepf.opfpush;

import android.support.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author Roman Savin
 * @since 25.12.14
 */
@Retention(RetentionPolicy.SOURCE)
@StringDef({
        OPFConstants.ACTION_RECEIVE,
        OPFConstants.ACTION_REGISTRATION,
        OPFConstants.ACTION_UNREGISTRATION,
        OPFConstants.ACTION_NO_AVAILABLE_PROVIDER
})
@interface OPFAction {
}
