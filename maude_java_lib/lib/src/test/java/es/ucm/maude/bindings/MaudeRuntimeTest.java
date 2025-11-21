package es.ucm.maude.bindings;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Order;
import static org.junit.jupiter.api.Assertions.*;

class MaudeRuntimeTest {
    @Order(1)
    @Test void weCanUseTimeWithResourcesPath() {
        // NOTE no explicit init
        MaudeRuntime.getInstance().loadFromResources("maude/stdlib/time.maude");
        Module time = maude.getModule("TIME");
        assertNotNull(time);
    }

    @Order(2)
    @Test void weCanUseNATModuleForSimpleArithmetic() {
        MaudeRuntime.getInstance().init();
        // Calling init() after the first time is safe and noop
        MaudeRuntime.getInstance().init();

        Module nat = maude.getModule("NAT");
        assertNotNull(nat);
        Term term = nat.parseTerm("2 * 3");
        term.reduce();
        System.out.println(term);

        assertEquals(6, term.toInt());
    }

    @Order(3)
    @Test void weCanUseTimeFromStdlib() {
        MaudeRuntime.getInstance().init();

        MaudeRuntime.getInstance().loadStdlibFileFromResources("time.maude");
        // loading the same Maude source twice is ok
        MaudeRuntime.getInstance().loadStdlibFileFromResources("time.maude");
        Module time = maude.getModule("TIME");
        assertNotNull(time);
    }
}
