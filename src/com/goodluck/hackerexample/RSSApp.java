package com.goodluck.hackerexample;

import android.net.Uri;
import android.provider.BaseColumns;

public final class RSSApp {

    public static final String AUTHORITY = "com.goodluck.hackerexample.provider";

    public static final class RssItems implements BaseColumns {
        /**
         * The table name offered by this provider
         */
        public static final String TABLE_NAME = "rss_items";

        /*
         * URI definitions
         */
        /**
         * The scheme part for this provider's URI
         */
        private static final String SCHEME = "content://";

        /**
         * Path part for the Notes URI
         */
        private static final String PATH_ITEMS = "/rss_items";

        /**
         * Path part for the Note ID URI
         */
        private static final String PATH_ITEM_ID = "/rss_items/";

        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI =  Uri.parse(SCHEME + AUTHORITY + PATH_ITEMS);
        /**
         * The content URI base for a single note. Callers must
         * append a numeric note id to this Uri to retrieve a note
         */
        public static final Uri CONTENT_ID_URI_BASE
            = Uri.parse(SCHEME + AUTHORITY + PATH_ITEM_ID);
        /**
         * Column name for the title of the items
         * <P>Type: TEXT</P>
         */
        public static final String COLUMN_NAME_TITLE = "title";

        /**
         * Column name for the description of the items
         * <P>Type: TEXT</P>
         */
        public static final String COLUMN_NAME_DESCRIPTION = "description";

        /**
         * Column name for the publish date of the items
         * <P>Type: TEXT</P>
         */
        public static final String COLUMN_NAME_PUBDATE = "pubDate";
        /**
         * The default sort order for this table
         */
        /**
         * Column name for the creation timestamp
         * <P>Type: INTEGER (long from System.curentTimeMillis())</P>
         */
        public static final String COLUMN_NAME_CREATE_DATE = "created";

        /**
         * Column name for the modification timestamp
         * <P>Type: INTEGER (long from System.curentTimeMillis())</P>
         */
        public static final String COLUMN_NAME_MODIFICATION_DATE = "modified";

        public static final String DEFAULT_SORT_ORDER = "modified DESC";

        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.com.goodluck.hackerexample.provider.rss_item";

        /**
         * The MIME type of a {@link #CONTENT_URI} sub-directory of a single
         * note.
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.com.goodluck.hackerexample.provider.rss_item";


    }
}
