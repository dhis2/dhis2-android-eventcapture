package org.hisp.dhis.android.eventcapture;

import org.hisp.dhis.android.eventcapture.views.activities.FormSectionActivity;
import org.hisp.dhis.android.eventcapture.views.fragments.DataEntryFragment;

import dagger.Subcomponent;

@PerActivity
@Subcomponent(
        modules = {
                FormModule.class
        }
)
public interface FormComponent {

    //------------------------------------------------------------------------
    // Injection targets
    //------------------------------------------------------------------------

    void inject(FormSectionActivity formSectionActivity);

    void inject(DataEntryFragment dataEntryFragment);
}
