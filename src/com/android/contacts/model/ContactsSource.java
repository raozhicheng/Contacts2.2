/*
 * Copyright (C) 2009 The Android Open Source Project
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

package com.android.contacts.model;

import com.google.android.collect.Lists;
import com.google.android.collect.Maps;

import android.accounts.Account;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.RawContacts;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.StructuredPostal;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

/**
 * Internal structure that represents constraints and styles for a specific data
 * source, such as the various data types they support, including details on how
 * those types should be rendered and edited.
 * <p>
 * In the future this may be inflated from XML defined by a data source.
 * 内部结构，代表了一个特定的数据源，如各种数据类型，他们的支持，包括如何提供这些类型的应和编辑细节，制约因素和风格。
 * 在将来，这可能被扩大从XML数据源定义
 */
public abstract class ContactsSource {
    /**
     * The {@link RawContacts#ACCOUNT_TYPE} these constraints apply to.
     * 对应于 RawContacts表的account_type字段
     */
    public String accountType = null;

    /**
     * Package that resources should be loaded from, either defined through an
     * {@link Account} or for matching against {@link Data#RES_PACKAGE}.
     * 包资源的加载（账号从网络 或根据资源包路径） 根据账号或者 资源包名称res_package
     */
    public String resPackageName;//资源包名称
    public String summaryResPackageName;//概要资源包名称 

    public int titleRes;//资源标题
    public int iconRes;//资源图标

    public boolean readOnly;//只读

    /**
     * Set of {@link DataKind} supported by this source.
     * 设置数据源的支持者 对应Data表的多个数据 DataKind就是Data表的多行数据
     * 在我们向数据源条件一个 DataKind（Data多行数据，一般对应一个数据类型的多行数据）的时候，同时添加到mKinds 和  mMimeKinds；
     * mKinds主要方便共同排序
     */
    private ArrayList<DataKind> mKinds = Lists.newArrayList();

    /**
     * Lookup map of {@link #mKinds} on {@link DataKind#mimeType}.
     * 对应一个用户的在 Data表中的 mimeType的集合数据  key就是mimeType  DataKind就是这个类型下的多个数据行 
     */
    private HashMap<String, DataKind> mMimeKinds = Maps.newHashMap();
    
    //展示的层级
    public static final int LEVEL_NONE = 0;//默认
    public static final int LEVEL_SUMMARY = 1;//简要
    public static final int LEVEL_MIMETYPES = 2;//数据类型的
    public static final int LEVEL_CONSTRAINTS = 3;//强制

    private int mInflatedLevel = LEVEL_NONE;
    
    //判断是不是扩展  如果等级高于自己  就说明是自己这个基础上扩展的详细信息 
    public synchronized boolean isInflated(int inflateLevel) {
        return mInflatedLevel >= inflateLevel;
    }

    /** @hide exposed for unit tests */
    public void setInflatedLevel(int inflateLevel) {
        mInflatedLevel = inflateLevel;
    }

    /**
     * Ensure that this {@link ContactsSource} has been inflated to the
     * requested level.
     * 确保扩展  同步任务
     */
    public synchronized void ensureInflated(Context context, int inflateLevel) {
        if (!isInflated(inflateLevel)) {
            inflate(context, inflateLevel);
        }
    }

    /**
     * Perform the actual inflation to the requested level. Called by
     * {@link #ensureInflated(Context, int)} when inflation is needed.
     */
    protected abstract void inflate(Context context, int inflateLevel);

    /**
     * Invalidate any cache for this {@link ContactsSource}, removing all
     * inflated data. Calling {@link #ensureInflated(Context, int)} will
     * populate again from scratch.
     */
    public synchronized void invalidateCache() {
        this.mKinds.clear();
        this.mMimeKinds.clear();
        setInflatedLevel(LEVEL_NONE);
    }

    public CharSequence getDisplayLabel(Context context) {
        if (this.titleRes != -1 && this.summaryResPackageName != null) {
            final PackageManager pm = context.getPackageManager();
            return pm.getText(this.summaryResPackageName, this.titleRes, null);
        } else if (this.titleRes != -1) {
            return context.getText(this.titleRes);
        } else {
            return this.accountType;
        }
    }

    public Drawable getDisplayIcon(Context context) {
        if (this.titleRes != -1 && this.summaryResPackageName != null) {
            final PackageManager pm = context.getPackageManager();
            return pm.getDrawable(this.summaryResPackageName, this.iconRes, null);
        } else if (this.titleRes != -1) {
            return context.getResources().getDrawable(this.iconRes);
        } else {
            return null;
        }
    }

    abstract public int getHeaderColor(Context context);

    abstract public int getSideBarColor(Context context);

    /**
     * {@link Comparator} to sort by {@link DataKind#weight}.
     */
    private static Comparator<DataKind> sWeightComparator = new Comparator<DataKind>() {
        public int compare(DataKind object1, DataKind object2) {
            return object1.weight - object2.weight;
        }
    };

    /**
     * Return list of {@link DataKind} supported, sorted by
     * {@link DataKind#weight}.
     */
    public ArrayList<DataKind> getSortedDataKinds() {
        // TODO: optimize by marking if already sorted
        Collections.sort(mKinds, sWeightComparator);
        return mKinds;
    }

    /**
     * Find the {@link DataKind} for a specific MIME-type, if it's handled by
     * this data source. If you may need a fallback {@link DataKind}, use
     * {@link Sources#getKindOrFallback(String, String, Context, int)}.
     */
    public DataKind getKindForMimetype(String mimeType) {
        return this.mMimeKinds.get(mimeType);
    }

    /**
     * Add given {@link DataKind} to list of those provided by this source.
     */
    public DataKind addKind(DataKind kind) {
        kind.resPackageName = this.resPackageName;
        this.mKinds.add(kind);
        this.mMimeKinds.put(kind.mimeType, kind);
        return kind;
    }

    /**
     * Description of a specific data type, usually marked by a unique
     * {@link Data#MIMETYPE}. Includes details about how to view and edit
     * {@link Data} rows of this kind, including the possible {@link EditType}
     * labels and editable {@link EditField}.
     * 描述一个数据结构 通常是一个数据的类型 Data#MIMETYPE 
     * 描述如何查看和修改 Data的行数据的细节
     * 包含 可呢标签和 可编辑性
     * 通常是对应一个数据类型的在data表的多行数据
     */
    public static class DataKind {
        public String resPackageName;//资源包名称
        public String mimeType;//数据类型
        public int titleRes;//资源标题
        public int iconRes;//资源图标 
        public int iconAltRes;//资源图标
        public int weight;//权重  排序时候使用
        public boolean secondary;//第二 次要？
        public boolean editable;//可以编辑

        /**
         * If this is true (default), the user can add and remove values.
         * If false, the editor will always show a single field (which might be empty).
         */
        public boolean isList;//说明这个类型的数据 是单个的还是 list的 

        public StringInflater actionHeader;//行为头
        public StringInflater actionAltHeader;//行为提示头
        public StringInflater actionBody;//行为体

        public boolean actionBodySocial = false;//行为体关系

        public String typeColumn;//类型的列

        /**
         * Maximum number of values allowed in the list. -1 represents infinity.
         * If {@link DataKind#isList} is false, this value is ignored.
         */
        public int typeOverallMax;//如果该数据类型允许多个，则最大数量是多少

        public List<EditType> typeList;//具体数据的列表   对应data表的一行数据
        public List<EditField> fieldList;//具体数据的编辑方式列表   对应data表的一行数据

        public ContentValues defaultValues;//默认值

        public DataKind() {
        }

        public DataKind(String mimeType, int titleRes, int iconRes, int weight, boolean editable) {
            this.mimeType = mimeType;
            this.titleRes = titleRes;
            this.iconRes = iconRes;
            this.weight = weight;
            this.editable = editable;
            this.isList = true;
            this.typeOverallMax = -1;
        }
    }

    /**
     * Description of a specific "type" or "label" of a {@link DataKind} row,
     * such as {@link Phone#TYPE_WORK}. Includes constraints on total number of
     * rows a {@link Contacts} may have of this type, and details on how
     * user-defined labels are stored.
     * 描述这个数据类型的显示标签 例如显示一个电话  是工作电话还是手机  代表data中的一个数据行
     */
    public static class EditType {
        public int rawValue;//在data表中  代表这个子类型的值  例如 家庭电话  在data表   data2字段中 用2来标识
        public int labelRes;//标签的标题资源  资源的标识
//        public int actionRes;
//        public int actionAltRes;
        public boolean secondary;//次要的
        public int specificMax;//特定大小
        public String customColumn;//data表中的列的 自定义的列名

        public EditType(int rawValue, int labelRes) {
            this.rawValue = rawValue;
            this.labelRes = labelRes;
            this.specificMax = -1;
        }

        public EditType setSecondary(boolean secondary) {
            this.secondary = secondary;
            return this;
        }

        public EditType setSpecificMax(int specificMax) {
            this.specificMax = specificMax;
            return this;
        }

        public EditType setCustomColumn(String customColumn) {
            this.customColumn = customColumn;
            return this;
        }

        @Override
        public boolean equals(Object object) {
            if (object instanceof EditType) {
                final EditType other = (EditType)object;
                return other.rawValue == rawValue;
            }
            return false;
        }

        @Override
        public int hashCode() {
            return rawValue;
        }
    }

    /**
     * Description of a user-editable field on a {@link DataKind} row, such as
     * {@link Phone#NUMBER}. Includes flags to apply to an {@link EditText}, and
     * the column where this field is stored.
     * 定义一个用户可以编辑的字段来描述一个 DataKind的数据行
     * 包括一个标志适用于一个EditText
     * 标示这列的存储想关信息
     */
    public static class EditField {
        public String column;//列名称 即这个数据存储在 data中 对应的列名称  类似  data2
        public int titleRes;//标题资源
        public int inputType;//输入类型  用来标识 小键盘的样式
        public int minLines;//最小行长度
        public boolean optional;//是不是可选的

        public EditField(String column, int titleRes) {
            this.column = column;
            this.titleRes = titleRes;
        }

        public EditField(String column, int titleRes, int inputType) {
            this(column, titleRes);
            this.inputType = inputType;
        }

        public EditField setOptional(boolean optional) {
            this.optional = optional;
            return this;
        }
    }

    /**
     * Generic method of inflating a given {@link Cursor} into a user-readable
     * {@link CharSequence}. For example, an inflater could combine the multiple
     * columns of {@link StructuredPostal} together using a string resource
     * before presenting to the user.
     */
    public interface StringInflater {
        public CharSequence inflateUsing(Context context, Cursor cursor);
        public CharSequence inflateUsing(Context context, ContentValues values);
    }

}
