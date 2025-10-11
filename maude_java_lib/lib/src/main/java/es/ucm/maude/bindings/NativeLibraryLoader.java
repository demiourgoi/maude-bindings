package es.ucm.maude.bindings;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

/**
 * Utility class for loading native libraries embedded in JAR files.
 * This class extracts the native libraries to a temporary directory
 * and loads them using System.load().
 */
public class NativeLibraryLoader {
    
    private static final String NATIVE_LIB_PATH = "native/linux/";
    private static final String[] LIBRARIES = {"libmaude.so", "libmaudejni.so"};
    private static boolean librariesLoaded = false;
    private static File tempDir;
    
    /**
     * Loads the native libraries required by the Maude bindings.
     * This method should be called before any Maude operations.
     */
    public static synchronized void loadNativeLibraries() {
        if (librariesLoaded) {
            return;
        }
        
        try {
            // Create a temporary directory for extracted libraries
            tempDir = Files.createTempDirectory("maude-native").toFile();
            tempDir.deleteOnExit();
            
            // Extract and load each library
            for (String library : LIBRARIES) {
                String resourcePath = NATIVE_LIB_PATH + library;
                File extractedFile = extractLibrary(resourcePath, library);
                System.load(extractedFile.getAbsolutePath());
            }
            
            librariesLoaded = true;
            System.out.println("Maude native libraries loaded successfully");
            
        } catch (IOException e) {
            throw new RuntimeException("Failed to load native libraries", e);
        }
    }
    
    /**
     * Extracts a library from the JAR resources to a temporary file.
     */
    private static File extractLibrary(String resourcePath, String libraryName) throws IOException {
        ClassLoader classLoader = NativeLibraryLoader.class.getClassLoader();
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
     * Checks if the native libraries have been loaded.
     */
    public static boolean areLibrariesLoaded() {
        return librariesLoaded;
    }
    
    /**
     * Cleans up the temporary directory (useful for testing).
     */
    public static synchronized void cleanup() {
        if (tempDir != null && tempDir.exists()) {
            deleteDirectory(tempDir);
        }
        librariesLoaded = false;
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
}
