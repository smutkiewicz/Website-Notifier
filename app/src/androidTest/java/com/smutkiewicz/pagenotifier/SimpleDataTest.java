package com.smutkiewicz.pagenotifier;

import android.content.Context;
import android.net.Uri;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.smutkiewicz.pagenotifier.database.DbDescription;
import com.smutkiewicz.pagenotifier.model.Website;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class SimpleDataTest {

    List<Website> list;

    @Rule
    public ActivityTestRule<MainActivity> mActivityRule =
            new ActivityTestRule<>(MainActivity.class);

    @Before
    private void setTestData() {
        list = new ArrayList<>();
        list.add(new Website("Nowe zadanko",
                "https://github.com/smutkiewicz/Android-Soundbank/blob/master/app/src/main/" +
                        "java/com/smutkiewicz/soundbank/model/SoundArrayAdapter.java"));
        list.add(new Website("Poczta", "https://medusa.elka.pw.edu.pl/"));
        list.add(new Website("Mrow dyd", "http://www.if.pw.edu.pl/~mrow/dyd/"));
        list.add(new Website("Staty", "https://msoundtech.bandcamp.com/stats#zplays"));
    }

    @Test
    public void makeSimpleDataInsertThemToDbAndShowInAdapter() {
        for(Website w : list) {
            Uri newItemUri = mActivityRule.getActivity().getContentResolver().insert(
                    DbDescription.CONTENT_URI, w.getContentValues());
        }
    }

    @Test
    public void useAppContext() throws Exception {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        assertEquals("com.smutkiewicz.pagenotifier", appContext.getPackageName());
    }
}
