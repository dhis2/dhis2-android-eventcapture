/*
 * Copyright (c) 2015, University of Oslo
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * Neither the name of the HISP project nor the names of its contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.hisp.dhis2.android.eventcapture.fragments.dataentry;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.structure.Model;

import org.hisp.dhis2.android.eventcapture.R;
import org.hisp.dhis2.android.eventcapture.adapters.SimpleAdapter;
import org.hisp.dhis2.android.eventcapture.loaders.DbLoader;
import org.hisp.dhis2.android.eventcapture.loaders.Query;
import org.hisp.dhis2.android.sdk.persistence.models.Option;

import java.util.ArrayList;
import java.util.List;

public class OptionDialogFragment extends DialogFragment
        implements LoaderCallbacks<List<Option>> {
    private static final int LOADER_ID = 1;

    private ListView mListView;
    // private ProgressBar mProgressBar;
    private SimpleAdapter<Option> mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE,
                R.style.Theme_AppCompat_Light_Dialog);
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_fragment_options, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        // mProgressBar = (ProgressBar) view.findViewById(R.id.progress_bar);
        // mProgressBar.setVisibility(View.INVISIBLE);

        mListView = (ListView) view.findViewById(R.id.simple_listview);

        mAdapter = new SimpleAdapter<>(LayoutInflater.from(getActivity()));
        mAdapter.setStringExtractor(new StringExtractor());
        mListView.setAdapter(mAdapter);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // mProgressBar.setVisibility(View.VISIBLE);
        getLoaderManager().initLoader(LOADER_ID, getArguments(), this);
    }

    @Override
    public Loader<List<Option>> onCreateLoader(int id, Bundle args) {
        if (LOADER_ID == id && isAdded()) {
            List<Class<? extends Model>> modelsToTrack = new ArrayList<>();
            modelsToTrack.add(Option.class);
            return new DbLoader<>(
                    getActivity().getBaseContext(), modelsToTrack, new OptionQuery()
            );
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<List<Option>> loader,
                               List<Option> data) {
        if (loader.getId() == LOADER_ID) {
            // mProgressBar.setVisibility(View.GONE);
            mAdapter.swapData(data);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<Option>> loader) {
        mAdapter.swapData(null);
    }

    static class OptionQuery implements Query<List<Option>> {

        @Override public List<Option> query(Context context) {
            return Select.all(Option.class);
        }
    }

    static class StringExtractor implements SimpleAdapter.ExtractStringCallback<Option> {

        @Override
        public String getString(Option object) {
            return object.getName();
        }
    }
}
