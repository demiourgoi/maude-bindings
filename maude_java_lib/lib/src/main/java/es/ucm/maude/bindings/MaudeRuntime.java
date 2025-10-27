package es.ucm.maude.bindings;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;
import java.util.LinkedList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Runtime initialization class for Maude bindings.
 * This class handles loading native libraries and Maude prelude files.
 */
public class MaudeRuntime {
    
    private static final String NATIVE_LIB_PATH = "native/linux/";
    private static final String[] LIBRARIES = {"libmaude.so", "libmaudejni.so"};
    private static final String PRELUDE_ZIP_RESOURCE = "maude-prelude.zip";
    private static boolean initialized = false;
    private static File tempDir;
    
    /**
     * Loads the native libraries required by the Maude bindings.
     * This method should be called before any Maude operations.
     */
    private static synchronized void loadNativeLibraries() {
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
            
            System.out.println("Maude native libraries loaded successfully");
            
        } catch (IOException e) {
            throw new RuntimeException("Failed to load native libraries", e);
        }
    }
    
    /**
     * Extracts a library from the JAR resources to a temporary file.
     */
    private static File extractLibrary(String resourcePath, String libraryName) throws IOException {
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
    public static File getTempDir() {
        return tempDir;
    }
    

    /**
     * Cleans up the temporary directory (useful for testing).
     */
    public static synchronized void cleanup() {
        if (tempDir != null && tempDir.exists()) {
            deleteDirectory(tempDir);
        }
    }
    
    private static void deleteDirectory(File directory) {
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
     * Loads the Maude prelude files from the bundled ZIP resource.
     * Extracts the ZIP to a temporary directory and calls maude.load() for each .maude file.
     * Ensures prelude.maude is loaded first, followed by other files.
     */
    private static synchronized void loadPrelude() {
        if (initialized) {
            return;
        }

        if (tempDir == null) {
            throw new IllegalStateException("Temporary directory must be created before loading prelude");
        }
        
        ClassLoader classLoader = MaudeRuntime.class.getClassLoader();
        InputStream zipStream = classLoader.getResourceAsStream(PRELUDE_ZIP_RESOURCE);
        
        if (zipStream == null) {
            throw new RuntimeException("Prelude ZIP resource not found: " + PRELUDE_ZIP_RESOURCE);
        }
        
        File preludeDir = new File(tempDir, "maude-prelude");
        preludeDir.mkdirs();
        preludeDir.deleteOnExit();
        
        try (ZipInputStream zis = new ZipInputStream(zipStream)) {
            ZipEntry entry;
            List<File> otherMaudeFiles = new LinkedList<>();

            while ((entry = zis.getNextEntry()) != null) {
                if (!entry.isDirectory() && entry.getName().toLowerCase().endsWith(".maude")) {
                    File outputFile = new File(preludeDir, new File(entry.getName()).getName());
                    outputFile.deleteOnExit();
                    
                    try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                        byte[] buffer = new byte[8192];
                        int bytesRead;
                        while ((bytesRead = zis.read(buffer)) != -1) {
                            fos.write(buffer, 0, bytesRead);
                        }
                    }
                    
                    // Load prelude.maude first, store others for later
                    if (outputFile.getName().equalsIgnoreCase("prelude.maude")) {
                        maude.load(outputFile.getAbsolutePath());
                        System.out.println("Loaded Maude prelude file: " + outputFile.getName());
                    } else {
                        otherMaudeFiles.add(outputFile);
                    }
                }
                zis.closeEntry();
            }

            // Load all other .maude files after prelude.maude
            for (File file : otherMaudeFiles) {
                maude.load(file.getAbsolutePath());
                System.out.println("Loaded Maude file: " + file.getName());
            }

            System.out.println("Maude prelude files loaded successfully");
        } catch (IOException e) {
            throw new RuntimeException("Failed to load Maude prelude files", e);
        }
    }
    
    /**
     * Initializes the Maude runtime by loading native libraries, initializing Maude,
     * and loading the prelude files. This method is thread-safe and runs only once.
     */
    public static synchronized void init() {
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
        maude.init();
        loadPrelude();
        
        initialized = true;
        System.out.println("Maude runtime initialized successfully");
    }
    
    /**
     * Checks if the Maude runtime has been initialized.
     */
    public static boolean isInitialized() {
        return initialized;
    }
}
