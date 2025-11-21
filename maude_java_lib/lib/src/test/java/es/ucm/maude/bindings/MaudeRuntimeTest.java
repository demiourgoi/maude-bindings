package es.ucm.maude.bindings;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.AfterAll;
import static org.junit.jupiter.api.Assertions.*;

class MaudeRuntimeTest {
    
    @BeforeAll
    static void initializeMaudeRuntime() {
        System.out.println("Initializing Maude runtime");
        MaudeRuntime.getInstance().init();
        // Calling init() after the first time is safe and noop
        MaudeRuntime.getInstance().init();
    }
    
    @AfterAll
    static void cleanup() {
        MaudeRuntime.getInstance().cleanup();
    }

    @Test void weCanUseNATModuleForSimpleArithmetic() {
        Module nat = maude.getModule("NAT");
        assertNotNull(nat);
        Term term = nat.parseTerm("2 * 3");
        term.reduce();
        System.out.println(term);

        assertEquals(6, term.toInt());
    }

    @Test void weCanUseTimeFromStdlib() {
        MaudeRuntime.getInstance().loadStdlibFileFromResources("time.maude");
        // loading the same Maude source twice is ok
        MaudeRuntime.getInstance().loadStdlibFileFromResources("time.maude");
        Module time = maude.getModule("TIME");
        assertNotNull(time);
    }
    
    @Test void weCanUseTimeWithResourcesPath() {
        MaudeRuntime.getInstance().loadFromResources("maude/stdlib/time.maude");
        Module time = maude.getModule("TIME");
        assertNotNull(time);
    }
}
