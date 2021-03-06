/*
 * Copyright 2015 Realm Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.realm.internal;

import io.realm.RealmFieldType;

/**
 * Checked wrapper for Row data in Realm Core. All methods called through this will check that input parameters are
 * valid or throw an appropriate exception.
 * <p>
 * For low-level access to a Realm where safety checks were already performed, use {@link UncheckedRow} instead for
 * improved performance.
 */
public class CheckedRow extends UncheckedRow {

    // Used if created from other row. This keeps a strong reference to avoid GC'ing the original object, and it's
    // underlying native data.
    @SuppressWarnings("unused")
    private UncheckedRow originalRow;

    private CheckedRow(Context context, Table parent, long nativePtr) {
        super(context, parent, nativePtr);
    }

    private CheckedRow(UncheckedRow row) {
        super(row.context, row.parent, row.nativePointer);
        this.originalRow = row;
    }

    /**
     * Gets the row object associated to an index in a {@link Table}.
     *
     * @param context the Realm context.
     * @param table the {@link Table} that holds the row.
     * @param index the index of the row.
     * @return an instance of Row for the table and index specified.
     */
    public static CheckedRow get(Context context, Table table, long index) {
        long nativeRowPointer = table.nativeGetRowPtr(table.nativePtr, index);
        CheckedRow row = new CheckedRow(context, table, nativeRowPointer);
        context.addReference(NativeObjectReference.TYPE_ROW, row);
        return row;
    }

    /**
     * Gets the row object associated to an index in a {@link LinkView}.
     *
     * @param context the Realm context.
     * @param linkView the {@link LinkView} holding the row.
     * @param index the index of the row.
     * @return a checked instance of {@link Row} for the {@link LinkView} and index specified.
     */
    public static CheckedRow get(Context context, LinkView linkView, long index) {
        long nativeRowPointer = linkView.nativeGetRow(linkView.nativePointer, index);
        CheckedRow row = new CheckedRow(context, linkView.getTargetTable(),
                nativeRowPointer);
        context.addReference(NativeObjectReference.TYPE_ROW, row);
        return row;
    }

    /**
     * Converts a {@link UncheckedRow} to a {@link CheckedRow}.
     *
     * @return an checked instance of {@link Row}.
     */
    public static CheckedRow getFromRow(UncheckedRow row) {
        return new CheckedRow(row);
    }

    @Override
    public boolean isNullLink(long columnIndex) {
        RealmFieldType columnType = getColumnType(columnIndex);
        if (columnType == RealmFieldType.OBJECT || columnType == RealmFieldType.LIST) {
            return super.isNullLink(columnIndex);
        } else {
            return false; // Unsupported types always return false
        }
    }

    @Override
    public boolean isNull(long columnIndex) {
        return super.isNull(columnIndex);
    }

    /**
     * Set null to a row pointer with checking if a column is nullable, except when the column type
     * is binary.
     *
     * @param columnIndex 0 based index value of the cell column.
     */
    @Override
    public void setNull(long columnIndex) {
        RealmFieldType columnType = getColumnType(columnIndex);
        if (columnType == RealmFieldType.BINARY) {
            super.setBinaryByteArray(columnIndex, null);
        } else {
            super.setNull(columnIndex);
        }
    }

    protected native long nativeGetColumnCount(long nativeTablePtr);
    protected native String nativeGetColumnName(long nativeTablePtr, long columnIndex);
    protected native long nativeGetColumnIndex(long nativeTablePtr, String columnName);
    protected native int nativeGetColumnType(long nativeTablePtr, long columnIndex);
    protected native long nativeGetLong(long nativeRowPtr, long columnIndex);
    protected native boolean nativeGetBoolean(long nativeRowPtr, long columnIndex);
    protected native float nativeGetFloat(long nativeRowPtr, long columnIndex);
    protected native double nativeGetDouble(long nativeRowPtr, long columnIndex);
    protected native long nativeGetTimestamp(long nativeRowPtr, long columnIndex);
    protected native String nativeGetString(long nativePtr, long columnIndex);
    protected native boolean nativeIsNullLink(long nativeRowPtr, long columnIndex);
    protected native byte[] nativeGetByteArray(long nativePtr, long columnIndex);
    protected native long nativeGetLinkView(long nativePtr, long columnIndex);
    protected native void nativeSetLong(long nativeRowPtr, long columnIndex, long value);
    protected native void nativeSetBoolean(long nativeRowPtr, long columnIndex, boolean value);
    protected native void nativeSetFloat(long nativeRowPtr, long columnIndex, float value);
    protected native long nativeGetLink(long nativeRowPtr, long columnIndex);
    protected native void nativeSetDouble(long nativeRowPtr, long columnIndex, double value);
    protected native void nativeSetTimestamp(long nativeRowPtr, long columnIndex, long dateTimeValue);
    protected native void nativeSetString(long nativeRowPtr, long columnIndex, String value);
    protected native void nativeSetByteArray(long nativePtr, long columnIndex, byte[] data);
    protected native void nativeSetLink(long nativeRowPtr, long columnIndex, long value);
    protected native void nativeNullifyLink(long nativeRowPtr, long columnIndex);
}
