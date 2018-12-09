package edu.uoc.gruizto.mybooks;

import android.app.Activity;
import android.content.ClipboardManager;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import androidx.test.espresso.contrib.DrawerActions;
import androidx.test.filters.SmallTest;
import androidx.test.rule.ActivityTestRule;
import edu.uoc.gruizto.mybooks.activity.BookListActivity;

import static android.content.Context.CLIPBOARD_SERVICE;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.assertion.ViewAssertions.*;
import static androidx.test.espresso.matcher.RootMatchers.withDecorView;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static org.hamcrest.Matchers.*;

@SmallTest
public class ShareToClipboardTest {

    private String expectedClipboardContent;
    private String shareToClipboardDrawerOptionLabel;
    private String expectedToastMessage;
    private Activity activity;

    @Rule
    public ActivityTestRule<BookListActivity> mActivityRule
            = new ActivityTestRule<>(BookListActivity.class);

    @Before
    public void onBefore() {
        activity = mActivityRule.getActivity();
        shareToClipboardDrawerOptionLabel = activity.getResources().getString(R.string.drawer_item_copy_to_clipboard);
        expectedClipboardContent = activity.getResources().getString(R.string.app_description);
        expectedToastMessage = activity.getResources().getString(R.string.message_share_to_clipboard_success);
    }

    @Test
    public void test() {

        // open drawer

        onView(withId(R.id.material_drawer_layout)).perform(DrawerActions.open());

        // click option we're testing

        onView(withText(shareToClipboardDrawerOptionLabel)).perform(click());

        // check toast is shown
        // see: https://stackoverflow.com/a/28606603

        onView(withText(expectedToastMessage))
            .inRoot(withDecorView(not(mActivityRule.getActivity().getWindow().getDecorView())))
            .check(matches(isDisplayed()));

        // check clipboard content

        ClipboardManager clipboardManager = (ClipboardManager) activity.getSystemService(CLIPBOARD_SERVICE);
        assertThat(clipboardManager.hasPrimaryClip(), is(true));
        String actualClipboardContent = clipboardManager.getPrimaryClip().getItemAt(0).getText().toString();
        assertThat(actualClipboardContent, is(expectedClipboardContent));
    }
}
