package org.hisp.dhis.android.eventcapture.mapper;

import org.hisp.dhis.android.eventcapture.views.ProgramPickable;
import org.hisp.dhis.client.sdk.models.program.Program;
import org.hisp.dhis.client.sdk.ui.views.chainablepickerview.IPickable;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;

import static org.hisp.dhis.client.sdk.models.utils.Preconditions.isNull;

public class ProgramPickableMapper {

    public IPickable transform(Program program) {
        isNull(program, "Program must not be null");

        IPickable programPickable = new ProgramPickable(program.getName(), program.getUId());
        return programPickable;
    }

    public List<IPickable> transform(List<Program> programs) {
        List<IPickable> programPickables = new ArrayList<>();

        for(Program program : programs) {
            IPickable programPickable = transform(program);
            programPickables.add(programPickable);
        }

        return programPickables;
    }
}
