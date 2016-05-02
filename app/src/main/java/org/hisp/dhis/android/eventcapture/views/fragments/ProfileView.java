package org.hisp.dhis.android.eventcapture.views.fragments;

import android.support.annotation.NonNull;
import android.support.annotation.StringDef;

import org.hisp.dhis.android.eventcapture.views.View;
import org.hisp.dhis.client.sdk.ui.models.FormEntity;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

public interface ProfileView extends View {

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({
            ID_FIRST_NAME,
            ID_SURNAME,
            ID_BIRTHDAY,
            ID_INTRODUCTION,
            ID_EDUCATION,
            ID_EMPLOYER,
            ID_INTERESTS,
            ID_JOB_TITLE,
            ID_LANGUAGES,
            ID_EMAIL,
            ID_PHONE_NUMBER
    })
    @interface UserAccountFieldId {
    }

    String ID_FIRST_NAME = "id:firstName";
    String ID_SURNAME = "id:surname";
    String ID_BIRTHDAY = "id:birthday";
    String ID_INTRODUCTION = "id:introduction";
    String ID_EDUCATION = "id:education";
    String ID_EMPLOYER = "id:employer";
    String ID_INTERESTS = "id:interests";
    String ID_JOB_TITLE = "id:jobTitle";
    String ID_LANGUAGES = "id:languages";
    String ID_EMAIL = "id:email";
    String ID_PHONE_NUMBER = "id:phoneNumber";

    void showUserAccountForm(List<FormEntity> formEntities);

    String getUserAccountFieldLabel(@NonNull @UserAccountFieldId String fieldId);
}
