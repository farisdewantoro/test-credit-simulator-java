package com.creditsimulator.model;

import com.creditsimulator.exception.InvalidInputException;
import com.creditsimulator.exception.SheetNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorkspaceTest {

    private final Workspace workspace = new Workspace();

    @Test
    void startsWithADefaultSheet() {
        assertEquals("default", workspace.active().name());
        assertEquals(List.of("default"), names());
    }

    @Test
    void saveAsCopiesTheActiveSheetAndMakesTheCopyActive() {
        workspace.active().edit(draft -> draft.vehicleType(VehicleType.MOBIL));

        Sheet saved = workspace.saveAs("mobil");

        assertSame(saved, workspace.active());
        assertEquals("mobil", saved.name());
        assertEquals(VehicleType.MOBIL, saved.draft().vehicleType().orElseThrow());
        assertEquals(List.of("default", "mobil"), names());
    }

    @Test
    void editsAfterSaveAsDoNotChangeTheOriginal() {
        workspace.active().edit(draft -> draft.tenor(3));
        workspace.saveAs("copy");

        workspace.active().edit(draft -> draft.tenor(1));

        assertEquals(3, workspace.switchTo("default").draft().tenor().orElseThrow());
        assertEquals(1, workspace.switchTo("copy").draft().tenor().orElseThrow());
    }

    @Test
    void saveAsCopiesTheLastResult() {
        InstallmentSchedule result = new InstallmentSchedule(new LoanApplication(VehicleType.MOTOR,
                VehicleCondition.BEKAS, 2019, new BigDecimal("20000000"), 1, new BigDecimal("5000000")), List.of());
        workspace.active().recordResult(result);

        assertSame(result, workspace.saveAs("copy").lastResult().orElseThrow());
    }

    @Test
    void saveAsOverwritesAnExistingSheetKeepingItsPosition() {
        workspace.saveAs("a");
        workspace.saveAs("b");
        workspace.switchTo("default").edit(draft -> draft.tenor(5));

        workspace.saveAs("A");

        assertEquals(List.of("default", "A", "b"), names());
        assertEquals(5, workspace.switchTo("a").draft().tenor().orElseThrow());
    }

    @Test
    void switchToIsCaseInsensitive() {
        workspace.saveAs("Mobil-Bekas");
        workspace.switchTo("default");

        assertEquals("Mobil-Bekas", workspace.switchTo("mobil-bekas").name());
        assertTrue(workspace.contains("MOBIL-BEKAS"));
        assertFalse(workspace.contains("motor"));
    }

    @Test
    void switchToAnUnknownSheetListsTheAvailableOnes() {
        workspace.saveAs("mobil");

        SheetNotFoundException error = assertThrows(SheetNotFoundException.class, () -> workspace.switchTo("motor"));

        assertEquals("Sheet 'motor' not found. Available sheets: default, mobil", error.getMessage());
        assertEquals("mobil", workspace.active().name());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"has space", "slash/name", "titik.dua", "this-name-is-way-longer-than-32-chars"})
    void rejectsInvalidSheetNames(String name) {
        assertThrows(InvalidInputException.class, () -> workspace.saveAs(name));
        assertEquals(List.of("default"), names());
    }

    @Test
    void editingASheetClearsItsLastResult() {
        Sheet sheet = workspace.active();
        sheet.recordResult(new InstallmentSchedule(new LoanApplication(VehicleType.MOTOR, VehicleCondition.BEKAS,
                2019, new BigDecimal("20000000"), 1, new BigDecimal("5000000")), List.of()));

        sheet.edit(draft -> draft.tenor(2));

        assertFalse(sheet.lastResult().isPresent());
    }

    private List<String> names() {
        return workspace.list().stream().map(Sheet::name).toList();
    }
}
