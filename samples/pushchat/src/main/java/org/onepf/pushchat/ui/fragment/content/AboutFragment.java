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

package org.onepf.pushchat.ui.fragment.content;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import org.onepf.pushchat.R;

/**
 * @author Roman Savin
 * @since 29.04.2015
 */
public class AboutFragment extends BaseContentFragment {

    public static final int POSITION = 4;

    @NonNull
    public static AboutFragment newInstance() {
        return new AboutFragment();
    }

    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater,
                             @Nullable final ViewGroup container,
                             @Nullable final Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_about, container, false);

        final TextView onepfTextView = (TextView) view.findViewById(R.id.onepf_text);
        final Button viewOnGithubButton = (Button) view.findViewById(R.id.view_on_github);

        onepfTextView.setOnClickListener(onOnepfTextClickListener());
        viewOnGithubButton.setOnClickListener(onViewGithubClickListener());

        return view;
    }

    @Override
    public int getTitleResId() {
        return R.string.title_about_fragment;
    }

    @Override
    public int getPosition() {
        return POSITION;
    }

    private View.OnClickListener onOnepfTextClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.onepf.org"));
                startActivity(browserIntent);
            }
        };
    }

    private View.OnClickListener onViewGithubClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/onepf/OPFPush"));
                startActivity(browserIntent);
            }
        };
    }
}
