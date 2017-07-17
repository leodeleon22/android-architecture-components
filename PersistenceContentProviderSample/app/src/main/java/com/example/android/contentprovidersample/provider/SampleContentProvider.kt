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

package com.example.android.contentprovidersample.provider

import android.content.ContentProvider
import android.content.ContentProviderOperation
import android.content.ContentProviderResult
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.OperationApplicationException
import android.content.UriMatcher
import android.database.Cursor
import android.net.Uri

import com.example.android.contentprovidersample.data.Cheese
import com.example.android.contentprovidersample.data.CheeseDao
import com.example.android.contentprovidersample.data.SampleDatabase

import java.util.ArrayList


/**
 * A [ContentProvider] based on a Room database.

 *
 * Note that you don't need to implement a ContentProvider unless you want to expose the data
 * outside your process or your application already uses a ContentProvider.
 */
class SampleContentProvider : ContentProvider() {

    override fun onCreate(): Boolean {
        return true
    }

    override fun query(uri: Uri, projection: Array<String>?, selection: String?,
                       selectionArgs: Array<String>?, sortOrder: String?): Cursor? {
        val code = MATCHER.match(uri)
        if (code == CODE_CHEESE_DIR || code == CODE_CHEESE_ITEM) {
            val context = context ?: return null
            val cheese = SampleDatabase.getInstance(context).cheese()
            val cursor: Cursor
            if (code == CODE_CHEESE_DIR) {
                cursor = cheese.selectAll()
            } else {
                cursor = cheese.selectById(ContentUris.parseId(uri))
            }
            cursor.setNotificationUri(context.contentResolver, uri)
            return cursor
        } else {
            throw IllegalArgumentException("Unknown URI: " + uri)
        }
    }

    override fun getType(uri: Uri): String? {
        when (MATCHER.match(uri)) {
            CODE_CHEESE_DIR -> return "vnd.android.cursor.dir/" + AUTHORITY + "." + Cheese.TABLE_NAME
            CODE_CHEESE_ITEM -> return "vnd.android.cursor.item/" + AUTHORITY + "." + Cheese.TABLE_NAME
            else -> throw IllegalArgumentException("Unknown URI: " + uri)
        }
    }

    override fun insert(uri: Uri, values: ContentValues): Uri? {
        when (MATCHER.match(uri)) {
            CODE_CHEESE_DIR -> {
                val context = context ?: return null
                val id = SampleDatabase.getInstance(context).cheese()
                        .insert(Cheese.fromContentValues(values))
                context.contentResolver.notifyChange(uri, null)
                return ContentUris.withAppendedId(uri, id)
            }
            CODE_CHEESE_ITEM -> throw IllegalArgumentException("Invalid URI, cannot insert with ID: " + uri)
            else -> throw IllegalArgumentException("Unknown URI: " + uri)
        }
    }

    override fun delete(uri: Uri, selection: String?,
                        selectionArgs: Array<String>?): Int {
        when (MATCHER.match(uri)) {
            CODE_CHEESE_DIR -> throw IllegalArgumentException("Invalid URI, cannot update without ID" + uri)
            CODE_CHEESE_ITEM -> {
                val context = context ?: return 0
                val count = SampleDatabase.getInstance(context).cheese()
                        .deleteById(ContentUris.parseId(uri))
                context.contentResolver.notifyChange(uri, null)
                return count
            }
            else -> throw IllegalArgumentException("Unknown URI: " + uri)
        }
    }

    override fun update(uri: Uri, values: ContentValues, selection: String?,
                        selectionArgs: Array<String>?): Int {
        when (MATCHER.match(uri)) {
            CODE_CHEESE_DIR -> throw IllegalArgumentException("Invalid URI, cannot update without ID" + uri)
            CODE_CHEESE_ITEM -> {
                val context = context ?: return 0
                val cheese = Cheese.fromContentValues(values)
                cheese.id = ContentUris.parseId(uri)
                val count = SampleDatabase.getInstance(context).cheese()
                        .update(cheese)
                context.contentResolver.notifyChange(uri, null)
                return count
            }
            else -> throw IllegalArgumentException("Unknown URI: " + uri)
        }
    }

    @Throws(OperationApplicationException::class)
    override fun applyBatch(
            operations: ArrayList<ContentProviderOperation>): Array<ContentProviderResult?> {
        val context = context ?: return arrayOfNulls(0)
        val database = SampleDatabase.getInstance(context)
        database.beginTransaction()
        try {
            val result = super.applyBatch(operations)
            database.setTransactionSuccessful()
            return result
        } finally {
            database.endTransaction()
        }
    }

    override fun bulkInsert(uri: Uri, valuesArray: Array<ContentValues>): Int {
        when (MATCHER.match(uri)) {
            CODE_CHEESE_DIR -> {
                val context = context ?: return 0
                val database = SampleDatabase.getInstance(context)
                val cheeses = arrayOfNulls<Cheese>(valuesArray.size)
                for (i in valuesArray.indices) {
                    cheeses[i] = Cheese.fromContentValues(valuesArray[i])
                }
                return database.cheese().insertAll(cheeses).size
            }
            CODE_CHEESE_ITEM -> throw IllegalArgumentException("Invalid URI, cannot insert with ID: " + uri)
            else -> throw IllegalArgumentException("Unknown URI: " + uri)
        }
    }

    companion object {

        /** The authority of this content provider.  */
        val AUTHORITY = "com.example.android.contentprovidersample.provider"

        /** The URI for the Cheese table.  */
        val URI_CHEESE = Uri.parse("content://" + AUTHORITY + "/" + Cheese.TABLE_NAME)

        /** The match code for some items in the Cheese table.  */
        private val CODE_CHEESE_DIR = 1

        /** The match code for an item in the Cheese table.  */
        private val CODE_CHEESE_ITEM = 2

        /** The URI matcher.  */
        private val MATCHER = UriMatcher(UriMatcher.NO_MATCH)

        init {
            MATCHER.addURI(AUTHORITY, Cheese.TABLE_NAME, CODE_CHEESE_DIR)
            MATCHER.addURI(AUTHORITY, Cheese.TABLE_NAME + "/*", CODE_CHEESE_ITEM)
        }
    }

}
