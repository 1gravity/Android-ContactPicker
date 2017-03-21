/*
 * Copyright (C) 2015-2017 Emanuel Moecklin
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

package com.onegravity.contactpicker.picture;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources.Theme;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.graphics.drawable.shapes.PathShape;
import android.graphics.drawable.shapes.RectShape;
import android.graphics.drawable.shapes.Shape;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.PhoneLookup;
import android.provider.ContactsContract.QuickContact;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.onegravity.contactpicker.Helper;
import com.onegravity.contactpicker.R;

import static com.onegravity.contactpicker.picture.Constants.EMAIL_LOOKUP_PROJECTION;
import static com.onegravity.contactpicker.picture.Constants.PHONE_LOOKUP_PROJECTION;
import static com.onegravity.contactpicker.picture.Constants.TOKEN_EMAIL_LOOKUP;
import static com.onegravity.contactpicker.picture.Constants.TOKEN_PHONE_LOOKUP;

/**
 * The ContactBadge.
 *
 * Derived from {@link android.widget.QuickContactBadge}.
 */
public class ContactBadge extends View implements OnClickListener {
    /**
     * Resize the pictures to the following value (device-independent pixels).
     */
    public static final float STANDARD_PICTURE_SIZE = 40f;
    private Uri mContactUri;
    private String mContactEmail;
    private String mContactPhone;
    private ContactQueryHandler mQueryHandler;
    private Bundle mExtras = null;

    protected String[] mExcludeMimes = null;

    private final int mSizeInPx;

    // Bitmap
    private BitmapDrawable mDrawable;

    // Character
    private String mChar;
    private Paint mTextPaint;
    private Rect mRect;

    // circle (round contact picture)
    private Paint mBackground;

    // Chip/Triangle (square contact picture)
    private ShapeDrawable mTriangle;
    private Paint mLinePaint;
    private float mOffset;

    // pressed overlay
    private boolean mIsPressed;
    private ShapeDrawable mPressedOverlay;

    private boolean mRoundContactPictures = true;

    private String mKey;

    private float mDensity;

    public ContactBadge(Context context) {
        this(context, null);
    }

    public ContactBadge(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ContactBadge(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        if (!isInEditMode()) {
            mQueryHandler = new ContactQueryHandler(context, mExcludeMimes);
        }
        setOnClickListener(this);

        mDensity = Helper.getDisplayMetrics(context).density;
        mSizeInPx = Math.round(STANDARD_PICTURE_SIZE * mDensity);

        initBadge(context, mRoundContactPictures);
    }

    private void initBadge(Context context, boolean roundContactPictures) {
        if (mTextPaint == null) {
            // Character
            mTextPaint = new Paint();
            mTextPaint.setAntiAlias(true);
            mTextPaint.setStyle(Paint.Style.FILL);
            mTextPaint.setARGB(255, 255, 255, 255);
            mTextPaint.setTextSize(mSizeInPx * 0.7f); // just scale this down a bit
            mRect = new Rect();
        }

        if (roundContactPictures) {
            initRound(context);
        } else {
            initSquare(context);
        }
    }

    private void initRound(Context context) {
        if (mBackground == null) {
            // background (if there's no bitmap)
            mBackground = new Paint();
            mBackground.setStyle(Paint.Style.FILL);
            mBackground.setAntiAlias(true);
        }

        initOverlay(context, new OvalShape());
    }

    private void initSquare(Context context) {
        if (mTriangle == null) {
            TypedValue typedValue = new TypedValue();
            Theme theme = context.getTheme();

            // triangle
            Path chipPath = new Path();
            chipPath.moveTo(500f, 0f);
            chipPath.lineTo(500f, 500f);
            chipPath.lineTo(0f, 500f);
            chipPath.close();
            mTriangle = new ShapeDrawable(new PathShape(chipPath, 500f, 500f));
            mTriangle.setDither(true);
            int triangleColor = Color.parseColor("#cc1f1f1f");
            if (theme.resolveAttribute(R.attr.cp_badgeTriangleColor, typedValue, true)) {
                triangleColor = typedValue.data;
            }
            mTriangle.getPaint().setColor(triangleColor);

            // line
            mLinePaint = new Paint();
            int lineColor = Color.parseColor("#ffffffff");
            if (theme.resolveAttribute(R.attr.cp_badgeLineColor, typedValue, true)) {
                lineColor = typedValue.data;
            }
            mLinePaint.setColor(lineColor);
            mOffset = 1.5f * mDensity;
            mLinePaint.setStrokeWidth(mOffset);
        }

        initOverlay(context, new RectShape());
    }

    private void initOverlay(Context context, Shape shape) {
        // pressed state
        TypedValue typedValue = new TypedValue();
        Theme theme = context.getTheme();

        mPressedOverlay = new ShapeDrawable(shape);
        int overlayColor = Color.parseColor("#aa888888");
        if (theme.resolveAttribute(R.attr.cp_badgeOverlayColor, typedValue, true)) {
            overlayColor = typedValue.data;
        }
        Paint paint = mPressedOverlay.getPaint();
        paint.setColor(overlayColor);
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);
    }

    public synchronized void onDestroy() {
        mDrawable = null;
        if (mQueryHandler != null) {
            mQueryHandler.cancelOperation();
        }
        mQueryHandler = null;
    }

    // ****************************************** Contact Picture Methods *******************************************

    public void setBadgeType(ContactPictureType contactPictureType) {
        mRoundContactPictures = contactPictureType == ContactPictureType.ROUND;
        initBadge(getContext(), mRoundContactPictures);
    }

    /**
     * The "key" is set by the ContactPictureManager to identify the ContactPictureLoader associated
     * with this ContactBadge.
     * Because ContactBadges may be re-used in a layout (list view, grid view etc.) a new loader
     * might be started with a different key while the current loader is still running. To prevent
     * multiple concurrent loaders from overriding each others result, the keys need to match.
     */
    synchronized String getKey() {
        return mKey;
    }

    synchronized void setKey(String key) {
        mKey = key;
    }

    public void setCharacter(Character c, int color) {
        mChar = Character.toString(c);
        mDrawable = null;
        if (mRoundContactPictures) {
            mBackground.setColor(color);
        } else {
            setBackgroundColor(color);
        }
        invalidate();
    }

    public void setBitmap(Bitmap bitmap) {
        mChar = null;
        if (mDrawable == null || mDrawable.getBitmap() != bitmap) {
            mDrawable = new BitmapDrawable(getContext().getResources(), bitmap);
            mKey = null;
            invalidate();
        }
    }

    // ****************************************** View Methods *******************************************

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();

        boolean isPressed = false;
        for (int state : getDrawableState()) {
            if (state == android.R.attr.state_pressed || state == android.R.attr.state_focused) {
                isPressed = true;
                break;
            }
        }

        if (isPressed != mIsPressed) {
            mIsPressed = isPressed;
            invalidate();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int w = getWidth();
        int h = getHeight();

        if (mRoundContactPictures) {
            onDrawCircle(canvas, w, h);
        } else {
            onDrawSquare(canvas, w, h);
        }

        if (mIsPressed) {
            mPressedOverlay.setBounds(0, 0, w, h);
            mPressedOverlay.draw(canvas);
        }
    }

    private void onDrawCircle(Canvas canvas, int w, int h) {
        if (mDrawable != null) {
            // picture
            mDrawable.setBounds(0, 0, w, h);
            mDrawable.draw(canvas);
        } else if (!TextUtils.isEmpty(mChar)) {
            // circle
            float radius = Math.min(w, h) / 2f;
            canvas.drawCircle(radius, radius, radius, mBackground);

            // letter
            mTextPaint.getTextBounds(mChar, 0, 1, mRect);
            float width = mTextPaint.measureText(mChar);
            canvas.drawText(mChar, (mSizeInPx - width) / 2f, (mSizeInPx + mRect.height()) / 2f, mTextPaint);
        }
    }

    private void onDrawSquare(Canvas canvas, int w, int h) {
        if (mDrawable != null) {
            // picture
            mDrawable.setBounds(0, 0, w, h);
            mDrawable.draw(canvas);
        } else if (!TextUtils.isEmpty(mChar)) {
            // letter
            mTextPaint.getTextBounds(mChar, 0, 1, mRect);
            float width = mTextPaint.measureText(mChar);
            canvas.drawText(mChar, (mSizeInPx - width) / 2f, (mSizeInPx + mRect.height()) / 2f, mTextPaint);
        }

        // chip / triangle
        if (isEnabled() && mTriangle != null) {
            // triangle
            int size = Math.round(mSizeInPx / 3f - mOffset / 2f);
            mTriangle.setBounds(w - size, h - size, w, h);
            mTriangle.draw(canvas);

            // line
            size = Math.round(mSizeInPx / 3f);
            canvas.drawLine(w - size - mOffset, h + mOffset, w + mOffset, h - size - mOffset, mLinePaint);
        }
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Override
    public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
        super.onInitializeAccessibilityEvent(event);
        event.setClassName(ContactBadge.class.getName());
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Override
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        info.setClassName(ContactBadge.class.getName());
    }

    // ****************************************** Assign Methods *******************************************

    /**
     * True if a contact, an email address or a phone number has been assigned
     */
    private boolean isAssigned() {
        return mContactUri != null || mContactEmail != null || mContactPhone != null;
    }

    /**
     * Assign the contact uri that this QuickContactBadge should be associated
     * with. Note that this is only used for displaying the QuickContact window and
     * won't bind the contact's photo for you.
     *
     * @param contactUri Either a {@link Contacts#CONTENT_URI} or {@link Contacts#CONTENT_LOOKUP_URI} style URI.
     */
    public void assignContactUri(Uri contactUri) {
        mContactUri = contactUri;
        mContactEmail = null;
        mContactPhone = null;
        onContactUriChanged();
    }

    /**
     * Assign a contact based on an email address. This should only be used when
     * the contact's URI is not available, as an extra query will have to be
     * performed to lookup the URI based on the email.
     *
     * @param emailAddress The email address of the contact.
     * @param lazyLookup   If this is true, the lookup query will not be performed
     *                     until this view is clicked.
     */
    public void assignContactFromEmail(String emailAddress, boolean lazyLookup) {
        assignContactFromEmail(emailAddress, lazyLookup, null);
    }

    /**
     * Assign a contact based on an email address. This should only be used when
     * the contact's URI is not available, as an extra query will have to be
     * performed to lookup the URI based on the email.
     *
     * @param emailAddress The email address of the contact.
     * @param lazyLookup   If this is true, the lookup query will not be performed until this view is
     *                     clicked.
     * @param extras       A bundle of extras to populate the contact edit page with if the contact is not
     *                     found and the user chooses to add the email address to an existing contact or
     *                     create a new contact. Uses the same string constants as those found in
     *                     {@link android.provider.ContactsContract.Intents.Insert}
     */
    public void assignContactFromEmail(String emailAddress, boolean lazyLookup, Bundle extras) {
        mContactEmail = emailAddress;
        mExtras = extras;
        if (!lazyLookup && mQueryHandler != null) {
            mQueryHandler.startQuery(TOKEN_EMAIL_LOOKUP, null,
                    Uri.withAppendedPath(Email.CONTENT_LOOKUP_URI, Uri.encode(mContactEmail)),
                    EMAIL_LOOKUP_PROJECTION, null, null, null, mContactQueryHandlerCallback);
        } else {
            mContactUri = null;
            onContactUriChanged();
        }
    }

    /**
     * Assign a contact based on a phone number. This should only be used when the contact's URI is
     * not available, as an extra query will have to be performed to lookup the URI based on the
     * phone number.
     *
     * @param phoneNumber The phone number of the contact.
     * @param lazyLookup  If this is true, the lookup query will not be performed  until this view is
     *                    clicked.
     */
    public void assignContactFromPhone(String phoneNumber, boolean lazyLookup) {
        assignContactFromPhone(phoneNumber, lazyLookup, new Bundle());
    }

    /**
     * Assign a contact based on a phone number. This should only be used when the contact's URI is
     * not available, as an extra query will have to be performed to lookup the URI based on the
     * phone number.
     *
     * @param phoneNumber The phone number of the contact.
     * @param lazyLookup  If this is true, the lookup query will not be performed  until this view is
     *                    clicked.
     * @param extras      A bundle of extras to populate the contact edit page with if the contact is not
     *                    found and the user chooses to add the phone number to an existing contact or
     *                    create a new contact. Uses the same string constants as those found in
     *                    {@link android.provider.ContactsContract.Intents.Insert}
     */
    public void assignContactFromPhone(String phoneNumber, boolean lazyLookup, Bundle extras) {
        mContactPhone = phoneNumber;
        mExtras = extras;
        if (!lazyLookup && mQueryHandler != null) {
            mQueryHandler.startQuery(TOKEN_PHONE_LOOKUP, null,
                    Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, mContactPhone),
                    PHONE_LOOKUP_PROJECTION, null, null, null, mContactQueryHandlerCallback);
        } else {
            mContactUri = null;
            onContactUriChanged();
        }
    }

    private void onContactUriChanged() {
        setEnabled(isAssigned());
    }

    @Override
    public void onClick(View v) {
        // If contact has been assigned, mExtras should no longer be null, but do a null check
        // anyway just in case assignContactFromPhone or Email was called with a null bundle or
        // wasn't assigned previously.
        final Bundle extras = (mExtras == null) ? new Bundle() : mExtras;
        if (mContactUri != null) {
            QuickContact.showQuickContact(getContext(), ContactBadge.this, mContactUri, QuickContact.MODE_LARGE, mExcludeMimes);
        } else if (mContactEmail != null && mQueryHandler != null) {
            extras.putString(Constants.EXTRA_URI_CONTENT, mContactEmail);
            mQueryHandler.startQuery(Constants.TOKEN_EMAIL_LOOKUP_AND_TRIGGER, extras,
                    Uri.withAppendedPath(Email.CONTENT_LOOKUP_URI, Uri.encode(mContactEmail)),
                    EMAIL_LOOKUP_PROJECTION, null, null, null, mContactQueryHandlerCallback);
        } else if (mContactPhone != null && mQueryHandler != null) {
            extras.putString(Constants.EXTRA_URI_CONTENT, mContactPhone);
            mQueryHandler.startQuery(Constants.TOKEN_PHONE_LOOKUP_AND_TRIGGER, extras,
                    Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, mContactPhone),
                    PHONE_LOOKUP_PROJECTION, null, null, null, mContactQueryHandlerCallback);
        } else {
            // If a contact hasn't been assigned, don't react to click.
            return;
        }
    }

    ContactQueryHandler.ContactQueryHandlerCallback mContactQueryHandlerCallback =
            new ContactQueryHandler.ContactQueryHandlerCallback() {
                @Override
                public void onQueryComplete(int token, Uri uri, Bundle extras, boolean trigger, Uri createUri) {
                    assignContactUri(uri);
                    if (trigger && uri != null) {
                        // Found contact, so trigger QuickContact
                        ContactsContract.QuickContact.showQuickContact(getContext(),
                                ContactBadge.this, uri, ContactsContract.QuickContact.MODE_LARGE, mExcludeMimes);
                    } else if (createUri != null) {
                        // Prompt user to add this person to contacts
                        final Intent intent = new Intent(ContactsContract.Intents.SHOW_OR_CREATE_CONTACT, createUri);
                        if (extras != null) {
                            extras.remove(Constants.EXTRA_URI_CONTENT);
                            intent.putExtras(extras);
                        }
                        getContext().startActivity(intent);
                    }

                }
            };

    /**
     * Set a list of specific MIME-types to exclude and not display. For
     * example, this can be used to hide the {@link Contacts#CONTENT_ITEM_TYPE}
     * profile icon.
     */
    public void setExcludeMimes(String[] excludeMimes) {
        mExcludeMimes = excludeMimes;
    }

}
