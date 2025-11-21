package es.ucm.maude.bindings;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Set;
import java.util.HashSet;
import java.util.logging.Logger;

/**
 * Runtime initialization class for Maude bindings.
 * This class handles loading native libraries and Maude prelude files.
 */
public class MaudeRuntime {
    private static final String NATIVE_LIB_PATH = "native/linux/";
    private static final String[] LIBRARIES = {"libmaude.so", "libmaudejni.so"};
    private static final String MAUDE_STDLIB_RESOURCE_PREFIX = "maude/stdlib/" ;
    private static final String MAUDE_PRELUDE_MODULE_NAME = "prelude.maude";
    // Since we can't easily list resources in a directory with ClassLoader,
    // we'll manually specify the modules to load (excluding prelude.maude which is loaded separately)
    private static final  String[] MAUDE_STDLIB_MODULE_FILES = {
        "file.maude", "linear.maude", "machine-int.maude", "metaInterpreter.maude",
        "model-checker.maude", "prng.maude", "process.maude", "smt.maude",
        "socket.maude", "term-order.maude", "time.maude"
    };
    
    private static final MaudeRuntime INSTANCE = new MaudeRuntime();
    
    private final Set<String> loadedMaudeSources = new HashSet<>();
    private final Logger logger = Logger.getLogger(MaudeRuntime.class.getName());
    private boolean initialized = false;
    private File tempDir;
    
    /**
     * Private constructor to enforce singleton pattern.
     */
    private MaudeRuntime() {
        // Private constructor to prevent instantiation
    }
    
    /**
     * Returns the singleton instance of MaudeRuntime.
     */
    public static MaudeRuntime getInstance() {
        return INSTANCE;
    }
    
    /**
     * Loads the native libraries required by the Maude bindings.
     * This method should be called before any Maude operations.
     */
    private synchronized void loadNativeLibraries() {
        if (initialized) {
            return;
        }

        if (tempDir == null) {
            throw new IllegalStateException("Temporary directory must be created before loading native libraries");
        }
        
        try {            
            // Extract and load each library
            for (String library : LIBRARIES) {
                String resourcePath = NATIVE_LIB_PATH + library;
                File extractedFile = extractLibrary(resourcePath, library);
                System.load(extractedFile.getAbsolutePath());
            }
            
            logger.info("Maude native libraries loaded successfully");
            
        } catch (IOException e) {
            throw new RuntimeException("Failed to load native libraries", e);
        }
    }
    
    /**
     * Extracts a library from the JAR resources to a temporary file.
     */
    private File extractLibrary(String resourcePath, String libraryName) throws IOException {
        ClassLoader classLoader = MaudeRuntime.class.getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream(resourcePath);
        
        if (inputStream == null) {
            throw new IOException("Native library not found in JAR: " + resourcePath);
        }
        
        File outputFile = new File(tempDir, libraryName);
        outputFile.deleteOnExit();
        
        try (FileOutputStream outputStream = new FileOutputStream(outputFile)) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        } finally {
            inputStream.close();
        }
        
        // Set executable permission (important for Linux)
        outputFile.setExecutable(true);
        
        return outputFile;
    }
    
    /**
     * Returns the temporary directory where libraries are extracted.
     * Useful for debugging or if other libraries need to be loaded.
     */
    public File getTempDir() {
        return tempDir;
    }
    

    /**
     * Cleans up the temporary directory (useful for testing).
     */
    public synchronized void cleanup() {
        if (tempDir != null && tempDir.exists()) {
            deleteDirectory(tempDir);
        }
    }
    
    private void deleteDirectory(File directory) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    file.delete();
                }
            }
        }
        directory.delete();
    }
    
    /**
     * Loads a Maude program from the jar resources
     * Extracts the Maude program file from the JAR and calls maude.load() on it.
     * Note a Maude source file can contain several modules, to use them you will have
     * to use `maude.getModule` on each of those.
     * 
     * @param maudeProgramResourcePath the name of the module file (e.g., "prelude.maude")
     */
    public synchronized void loadFromResources(String maudeProgramResourcePath) {
        if (tempDir == null) {
            throw new IllegalStateException("Temporary directory must be created before loading modules");
        }
        if (loadedMaudeSources.contains(maudeProgramResourcePath)) {
            logger.info("Skipping loading of previously loaded Maude source file: " + maudeProgramResourcePath);
            return;
        }
        
        ClassLoader classLoader = MaudeRuntime.class.getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream(maudeProgramResourcePath);
        
        if (inputStream == null) {
            throw new RuntimeException("Maude module not found in JAR: " + maudeProgramResourcePath);
        }
        
        File outputFile = new File(tempDir, maudeProgramResourcePath);
        outputFile.getParentFile().mkdirs();
        outputFile.deleteOnExit();
        
        try (FileOutputStream outputStream = new FileOutputStream(outputFile)) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to extract Maude source file: " + maudeProgramResourcePath, e);
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                logger.warning("Failed to close input stream for Maude source file: " + maudeProgramResourcePath);
            }
        }
        
        maude.load(outputFile.getAbsolutePath());
        loadedMaudeSources.add(maudeProgramResourcePath);
        logger.info("Loaded Maude source file: " + maudeProgramResourcePath);
    }

    /**
     * Loads a Maude source file from the bundled standard library resources.
     * Extracts the Maude source file from the JAR and calls maude.load() on it.
     * 
     * @param moduleName the name of the file (e.g., "prelude.maude")
     */
    public synchronized void loadStdlibFileFromResources(String moduleName) {
        String moduleResourcePath = MAUDE_STDLIB_RESOURCE_PREFIX + moduleName ;
        loadFromResources(moduleResourcePath);
    }

    /**
     * Loads the Maude standard library files from the bundled resources.
     * Traverses the standard library directory and loads all .maude files except prelude.maude.
     * This method is public so users can call it at their convenience.
     */
    public synchronized void loadMaudeStdlib() {
        if (tempDir == null) {
            throw new IllegalStateException("Temporary directory must be created before loading standard library");
        }
        
        for (String module : MAUDE_STDLIB_MODULE_FILES) {
            loadStdlibFileFromResources(module);
        }
        
        logger.info("Maude standard library modules loaded successfully");
    }
    
    /**
     * Initializes the Maude runtime by loading native libraries, initializing Maude,
     * and loading the prelude files. This method is thread-safe and runs only once.
     */
    public synchronized void init() {
        if (initialized) {
            return;
        }
        // Create a temporary directory for extracted jar resources
        try {
            tempDir = Files.createTempDirectory("maude-native").toFile();
        } catch (IOException e) {
            throw new RuntimeException("Failed to create temporary directory", e);
        }
        tempDir.deleteOnExit();
        
        loadNativeLibraries();
        // load without prelude because we have not extracted the prelude from resources yet
        maude.init(false);
        loadStdlibFileFromResources(MAUDE_PRELUDE_MODULE_NAME);
        
        initialized = true;
        logger.info("Maude runtime initialized successfully");
    }
    
    /**
     * Checks if the Maude runtime has been initialized.
     */
    public boolean isInitialized() {
        return initialized;
    }
}
