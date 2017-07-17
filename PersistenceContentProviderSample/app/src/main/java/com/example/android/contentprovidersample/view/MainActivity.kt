/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.contentprovidersample.view

import android.database.Cursor
import android.os.Bundle
import android.support.v4.app.LoaderManager
import android.support.v4.content.CursorLoader
import android.support.v4.content.Loader
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import com.example.android.contentprovidersample.R
import kotlinx.android.synthetic.main.main_activity.list

import com.example.android.contentprovidersample.data.Cheese
import com.example.android.contentprovidersample.provider.SampleContentProvider


/**
 * Not very relevant to Room. This just shows data from [SampleContentProvider].

 *
 * Since the data is exposed through the ContentProvider, other apps can read and write the
 * content in a similar manner to this.
 */
class MainActivity : AppCompatActivity() {

    companion object {
        private val LOADER_CHEESES = 1
    }

    private var mCheeseAdapter = CheeseAdapter()

    private val mLoaderCallbacks = object : LoaderManager.LoaderCallbacks<Cursor> {

        override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {
            when (id) {
                LOADER_CHEESES -> return CursorLoader(applicationContext,
                        SampleContentProvider.URI_CHEESE,
                        arrayOf(Cheese.COLUMN_NAME), null, null, null)
                else -> throw IllegalArgumentException()
            }
        }

        override fun onLoadFinished(loader: Loader<Cursor>, data: Cursor) {
            when (loader.id) {
                LOADER_CHEESES -> mCheeseAdapter.setCheeses(data)
            }
        }

        override fun onLoaderReset(loader: Loader<Cursor>) {
            when (loader.id) {
                LOADER_CHEESES -> mCheeseAdapter.setCheeses(null)
            }
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        list.layoutManager = LinearLayoutManager(list.context)
        list.adapter = mCheeseAdapter

        supportLoaderManager.initLoader(LOADER_CHEESES, null, mLoaderCallbacks)
    }


}
