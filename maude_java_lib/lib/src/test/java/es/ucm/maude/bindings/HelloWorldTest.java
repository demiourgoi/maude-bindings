package es.ucm.maude.bindings;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.AfterAll;
import static org.junit.jupiter.api.Assertions.*;

class HelloWorldTest {
    
    @BeforeAll
    static void initializeMaudeRuntime() {
        System.out.println("Initializing Maude runtime");
        MaudeRuntime.init();
    }
    
    @AfterAll
    static void cleanup() {
        MaudeRuntime.cleanup();
    }
    
    @Test void someLibraryMethodReturnsTrue() {
        Module nat = maude.getModule("NAT");
        assertNotNull(nat);
        Term term = nat.parseTerm("2 * 3");
        term.reduce();
        System.out.println(term);

        assertEquals(6, term.toInt());
    }
}
