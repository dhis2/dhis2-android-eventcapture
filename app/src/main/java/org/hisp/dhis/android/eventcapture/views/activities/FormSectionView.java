package org.hisp.dhis.android.eventcapture.views.activities;

import org.hisp.dhis.android.eventcapture.views.View;
import org.hisp.dhis.client.sdk.ui.models.FormSection;

import java.util.List;

public interface FormSectionView extends View {

    /**
     * Should be called in cases when ProgramStage
     * does not contain any explicit sections
     */
    void showFormDefaultSection(String formSectionId);

    /**
     * Tells view to render form sections
     * @param formSections List of FormSections
     */
    void showFormSections(List<FormSection> formSections);

    void showTitle(String title);

    void showSubtitle(String subtitle);
}
