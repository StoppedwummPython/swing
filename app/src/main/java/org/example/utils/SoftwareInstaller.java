package org.example.utils; // Optional: Use a package appropriate for your project

import java.io.*;
import java.net.URL;
import java.nio.file.*;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.stream.Stream;
import java.io.UnsupportedEncodingException; // Import for encoding

/**
 * A class responsible for downloading a zip file, extracting it, and adding
 * a specified subdirectory to the Windows User Environment PATH.
 */
public class SoftwareInstaller {

    private static final int BUFFER_SIZE = 4096;

    /**
     * Default constructor.
     */
    public SoftwareInstaller() {
        // Constructor can be used for potential configuration in the future
    }

    /**
     * Performs the full software installation process:
     * 1. Downloads a zip file from a given URL.
     * 2. Extracts the contents of the zip file to a specified directory under the user's home.
     * 3. Attempts to add a specified subdirectory within the extracted contents
     *    to the Windows User Environment PATH (if running on Windows).
     *
     * @param zipFileUrl The URL of the zip file to download.
     * @param appName The name to use for the root directory where the software will be extracted
     *                (created under the user's home directory). E.g., "MyApplication".
     * @param relativeBinPath The path relative to the *extracted root directory* that should be
     *                        added to the PATH. E.g., "bin", "app/cli". If the zip extracts
     *                        directly into the target directory, this path is relative to `appName`.
     * @return The absolute {@link Path} that was attempted to be added to the system PATH.
     *         Returns null if running on a non-Windows OS or if the target bin directory was not found.
     * @throws IOException If an I/O error occurs during download, extraction, or PATH modification.
     * @throws InterruptedException If the process modifying the PATH is interrupted.
     * @throws IllegalArgumentException If input parameters are invalid (e.g., null or empty URL).
     */
    public Path installSoftware(String zipFileUrl, String appName, String relativeBinPath,boolean skipPath)
            throws IOException, InterruptedException, IllegalArgumentException {

        // --- Input Validation ---
        if (zipFileUrl == null || zipFileUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("Zip file URL cannot be null or empty.");
        }
        if (appName == null || appName.trim().isEmpty()) {
            throw new IllegalArgumentException("App name cannot be null or empty. This is used for the installation directory name.");
        }
        // relativeBinPath can theoretically be empty if you want to add the root extracted directory itself,
        // but usually you target a 'bin' dir. Let's allow empty but warn.
        if (relativeBinPath == null) {
             System.out.println("Warning: relativeBinPath is null. Assuming empty string (targetting the extracted root directory).");
             relativeBinPath = ""; // Treat null as empty
        }

        // --- Define Paths ---
        Path userHomeDir = Paths.get(System.getProperty("user.home"));
        // Consider using a temp directory for the download for better cleanup:
        // Path downloadDir = Files.createTempDirectory("software_download_");
        Path downloadDir = userHomeDir.resolve("Downloads"); // Using Downloads for visibility
        Files.createDirectories(downloadDir); // Ensure download directory exists

        String zipFileName = getFileNameFromUrl(zipFileUrl);
        Path downloadedFilePath = downloadDir.resolve(zipFileName);
        Path extractionDir = userHomeDir.resolve(appName); // Extract here

        System.out.println("--- Software Installation Process ---");
        System.out.println("Target URL: " + zipFileUrl);
        System.out.println("App Name (Extraction Directory): " + extractionDir);
        System.out.println("Relative PATH target: " + relativeBinPath);
        System.out.println("Download Path: " + downloadedFilePath);

        // 1. Download the Zip File
        System.out.println("\nStep 1: Downloading...");
        downloadFile(zipFileUrl, downloadedFilePath);
        System.out.println("Step 1: Download complete.");

        // 2. Extract the Zip File
        System.out.println("\nStep 2: Extracting...");
        // Clean up previous installations in the extraction directory if needed
        if (Files.exists(extractionDir)) {
            System.out.println("Step 2: Existing extraction directory found. Deleting contents...");
            deleteDirectory(extractionDir); // Use the robust delete helper
        }
        Files.createDirectories(extractionDir); // Ensure extraction directory exists

        // We need the root directory name from the zip to correctly construct the bin path
        // Assumes a common zip structure like "myapp-1.0/...".
        String zipRootDirectoryName = extractZipFile(downloadedFilePath, extractionDir);
        Path absoluteBinPath;

        if (zipRootDirectoryName == null || zipRootDirectoryName.isEmpty()) {
            System.out.println("Step 2: No discernible root directory found in zip. Assuming contents extract directly to: " + extractionDir);
            // If no root directory was found (e.g., zip contained "bin/file"), use extractionDir as the base
            absoluteBinPath = extractionDir.resolve(relativeBinPath);
        } else {
             System.out.println("Step 2: Detected potential root directory in zip: " + zipRootDirectoryName);
             // If a root directory like "myapp-1.0" was found, resolve relativeBinPath from there
             absoluteBinPath = extractionDir.resolve(zipRootDirectoryName).resolve(relativeBinPath);
        }

        System.out.println("Step 2: Extraction complete.");
        System.out.println("Step 2: Determined target directory for PATH: " + absoluteBinPath);

        if (skipPath) {
            System.out.println("Skipping PATH modification as per user request.");
            return null; // Skip PATH modification
        }

        // 3. Add the 'bin' directory to the Windows User PATH
        Path addedPath = null; // Will store the path actually added if successful
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            System.out.println("\nStep 3: Attempting to add to Windows User PATH...");
            if (Files.exists(absoluteBinPath)) {
                System.out.println("Step 3: Target directory exists. Proceeding to add to PATH.");
                addUserPath(absoluteBinPath.toString());
                System.out.println("Step 3: Attempt to modify PATH complete. IMPORTANT: Please open a NEW command prompt or terminal session for changes to take effect.");
                addedPath = absoluteBinPath; // Set the path that was added
            } else {
                System.err.println("Step 3: WARNING: The target directory for PATH was not found after extraction: " + absoluteBinPath);
                System.err.println("Step 3: Skipping PATH modification.");
            }
        } else {
            System.out.println("\nStep 3: Skipping Windows PATH modification: Not running on Windows.");
            // For other OSes (Linux, macOS), you would typically provide instructions
            // or potentially create symlinks, but modifying the global user PATH
            // involves different mechanisms (e.g., shell profile scripts like .bashrc, .zshrc).
        }

        System.out.println("\n--- Installation Process Finished ---");

        // Optional: Clean up the downloaded zip file
        // try {
        //    if (downloadedFilePath != null && Files.exists(downloadedFilePath)) {
        //       // Files.delete(downloadedFilePath); // Uncomment to delete the downloaded zip
        //       // System.out.println("Cleaned up downloaded zip file: " + downloadedFilePath);
        //    }
        // } catch (IOException e) {
        //     System.err.println("Failed to clean up downloaded file: " + e.getMessage());
        // }

        return addedPath; // Return the path that was added (or null if not applicable/successful)
    }

    /**
     * Downloads a file from a URL to a specific path.
     * @param fileURL The URL of the file to download.
     * @param savePath The path where the file should be saved.
     * @throws IOException If an I/O error occurs during download.
     */
    private void downloadFile(String fileURL, Path savePath) throws IOException {
        URL url = new URL(fileURL);
        try (InputStream in = url.openStream();
             BufferedInputStream bis = new BufferedInputStream(in);
             OutputStream fos = Files.newOutputStream(savePath)) { // Use Files.newOutputStream with Path

            byte[] data = new byte[BUFFER_SIZE];
            int bytesRead;
            long totalBytesRead = 0;
            long contentLength = -1;
            try {
                 contentLength = url.openConnection().getContentLengthLong(); // Get content length if available
            } catch (IOException e) {
                 System.err.println("Could not get content length for download progress: " + e.getMessage());
            }


            while ((bytesRead = bis.read(data, 0, BUFFER_SIZE)) != -1) {
                fos.write(data, 0, bytesRead);
                totalBytesRead += bytesRead;
                // Optional: Print progress (crude, but works in console)
                if (contentLength > 0) {
                    int progress = (int) ((totalBytesRead * 100) / contentLength);
                    // Use carriage return to overwrite the line
                    System.out.print("\rDownloading: " + progress + "% (" + totalBytesRead + "/" + contentLength + " bytes)");
                }
            }
            if (contentLength > 0) {
                System.out.println(); // New line after progress is done
            }
        } // Streams are closed automatically by try-with-resources
    }

    /**
     * Extracts a zip file to a destination directory.
     * Attempts to return the name of the presumed root directory within the zip.
     * @param zipFilePath The path to the zip file.
     * @param destDir The directory where contents should be extracted.
     * @return The name of the first directory encountered at the root level of the zip, or null if none found.
     * @throws IOException If an I/O error occurs during extraction.
     */
    private String extractZipFile(Path zipFilePath, Path destDir) throws IOException {
        String zipRootDirectoryName = null; // To capture the root directory name

        try (ZipFile zipFile = new ZipFile(zipFilePath.toFile())) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();

            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                // Resolve and normalize path relative to the destination directory
                Path entryDestPath = destDir.resolve(entry.getName()).normalize();

                // Basic security check: prevent Zip Slip vulnerability
                // Ensure the resolved path is still inside the destination directory
                if (!entryDestPath.startsWith(destDir)) {
                    throw new IOException("Zip entry attempts to write outside of target directory: " + entry.getName());
                }

                // Try to capture the root directory name from the first entry
                if (zipRootDirectoryName == null) {
                     String name = entry.getName();
                     // Split by both forward and back slashes
                     String[] pathParts = name.split("[/\\\\]");
                     // Find the first non-empty path part at the root level
                     if (pathParts.length > 0) {
                         String potentialRoot = pathParts[0];
                         if (!potentialRoot.isEmpty()) {
                             zipRootDirectoryName = potentialRoot;
                             // Special case: if the first part is empty (e.g., "/root/..."), use the second part
                             if (zipRootDirectoryName.isEmpty() && pathParts.length > 1) {
                                  zipRootDirectoryName = pathParts[1];
                             }
                         } else if (pathParts.length > 1 && !pathParts[1].isEmpty()) { // Handle leading slash
                             zipRootDirectoryName = pathParts[1];
                         }
                     }
                     // Note: This heuristic is not perfect and relies on the common zip structure
                     // where all contents are under a single root directory.
                }


                if (entry.isDirectory()) {
                    Files.createDirectories(entryDestPath);
                } else {
                    // Ensure parent directory exists for file entries
                    Files.createDirectories(entryDestPath.getParent());
                    try (InputStream entryIn = zipFile.getInputStream(entry);
                         BufferedInputStream bis = new BufferedInputStream(entryIn);
                         OutputStream fos = Files.newOutputStream(entryDestPath)) { // Use Files.newOutputStream

                        byte[] buffer = new byte[BUFFER_SIZE];
                        int bytesRead;
                        while ((bytesRead = bis.read(buffer)) != -1) {
                            fos.write(buffer, 0, bytesRead);
                        }
                    }
                }
            }
        }
        return zipRootDirectoryName;
    }


    /**
     * Adds a given path to the Windows User Environment PATH variable using setx.
     * This change is persistent but requires opening a new shell or relogging to take effect.
     * It correctly handles passing the PATH value as a single argument to setx via ProcessBuilder.
     * @param pathToAdd The absolute path string to add to the user's PATH.
     * @throws IOException If an I/O error occurs during command execution.
     * @throws InterruptedException If the command execution is interrupted.
     */
    private void addUserPath(String pathToAdd) throws IOException, InterruptedException {
        // Get current user path
        String currentPath = System.getenv("PATH");
        System.out.println("Current User PATH: " + (currentPath != null ? currentPath : "(empty)"));

        // Check if path is already present (case-insensitive comparison for Windows paths)
        // Split by platform-specific separator (semicolon on Windows)
        List<String> pathElements = new ArrayList<>();
        if (currentPath != null && !currentPath.trim().isEmpty()) {
             pathElements.addAll(Arrays.asList(currentPath.split(File.pathSeparator)));
        }

        boolean alreadyExists = false;
        Path pathToAddNormalized = Paths.get(pathToAdd).normalize();

        for (String pathElement : pathElements) {
             if (pathElement == null || pathElement.trim().isEmpty()) continue;
             try {
                 Path existingPathNormalized = Paths.get(pathElement).normalize();
                 // Use normalized string comparison for case-insensitivity on Windows
                 if (existingPathNormalized.toString().equalsIgnoreCase(pathToAddNormalized.toString())) {
                     alreadyExists = true;
                     break;
                 }
             } catch (InvalidPathException e) {
                 // Ignore invalid paths in the existing PATH
                 System.err.println("Warning: Found potentially invalid path in current PATH: '" + pathElement + "'. Skipping comparison for this element.");
             }
        }

        if (alreadyExists) {
            System.out.println("Path '" + pathToAdd + "' already exists in the User PATH. Skipping modification.");
            return;
        }

        // Construct the new PATH value: current PATH + ; + new_path
        String newPathValue = currentPath;
        if (newPathValue == null || newPathValue.trim().isEmpty()) {
            newPathValue = pathToAdd;
        } else {
            // Ensure the existing path doesn't end with a separator already
             if (!newPathValue.endsWith(File.pathSeparator)) {
                 newPathValue += File.pathSeparator;
             }
            newPathValue += pathToAdd;
        }

        // setx might have length limits. Check this.
        // User environment variable value limit is often cited as 1024 or 2048 bytes/characters.
        // The maximum total size of the environment block is 32767 characters.
        // setx itself seems to have limitations around 1024 characters for a single value.
        int setxValueMaxLength = 1024;
        if (newPathValue.length() > setxValueMaxLength) {
            System.err.println("WARNING: Resulting PATH variable length (" + newPathValue.length() + ") might exceed common setx value limit (" + setxValueMaxLength + "). setx might fail.");
             // Option: Truncate the path? Find common parts? This gets complex.
             // For now, just warn.
        }


        // Use ProcessBuilder to execute the setx command
        List<String> command = new ArrayList<>();
        command.add("setx");
        command.add("PATH");
        // Pass the raw value string. ProcessBuilder handles necessary quoting for spaces.
        command.add(newPathValue);

        System.out.println("Executing command list: " + command); // Print the list ProcessBuilder will use
        // Note: The actual command executed by setx based on this list *should* be equivalent to
        // setx PATH "value with spaces and semicolons" but ProcessBuilder manages the complex quoting.


        ProcessBuilder pb = new ProcessBuilder(command);
        // Redirect standard error to standard output for easier logging
        pb.redirectErrorStream(true);

        Process process = pb.start();
        int exitCode = process.waitFor(); // Wait for the command to complete

        // Read the output from the command (both stdout and stderr due to redirectErrorStream)
        // Attempt to use Cp850 for potentially better display of Windows console output (like German errors)
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), "Cp850"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("SETX Output: " + line);
            }
        } catch (UnsupportedEncodingException e) {
             // Fallback if Cp850 isn't supported or needed
             try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                 String line;
                 while ((line = reader.readLine()) != null) {
                     System.out.println("SETX Output (fallback encoding): " + line);
                 }
             }
        }


        if (exitCode != 0) {
            System.err.println("WARNING: setx command finished with exit code " + exitCode + ". Check output above for details. The PATH may NOT have been updated successfully.");
            // It's often safer to throw an exception if the command failed explicitly.
            // throw new IOException("setx command failed with exit code " + exitCode);
        } else {
             System.out.println("SETX command reported success (exit code 0).");
        }
    }

    /**
     * Helper to get file name from URL.
     * @param fileUrl The URL string.
     * @return The file name extracted from the URL path.
     */
    private static String getFileNameFromUrl(String fileUrl) {
        try {
            URL url = new URL(fileUrl);
            String path = url.getPath();
            // Find the last slash or backslash
            int lastSlash = Math.max(path.lastIndexOf('/'), path.lastIndexOf('\\'));
            return (lastSlash > 0) ? path.substring(lastSlash + 1) : path;
        } catch (Exception e) {
            System.err.println("Could not parse URL '" + fileUrl + "' to get file name: " + e.getMessage());
            return "downloaded_file.zip"; // fallback name
        }
    }

     /**
      * Helper to recursively delete a directory using Java NIO2.
      * More robust than recursive File.delete().
      * @param directoryToBeDeleted The path to the directory to delete.
      * @throws IOException If deletion fails.
      */
    private static void deleteDirectory(Path directoryToBeDeleted) throws IOException {
        if (!Files.exists(directoryToBeDeleted)) {
            System.out.println("Directory does not exist, no need to delete: " + directoryToBeDeleted);
            return;
        }
         System.out.println("Deleting directory contents: " + directoryToBeDeleted);
        try (Stream<Path> walk = Files.walk(directoryToBeDeleted)) {
            walk.sorted(Comparator.reverseOrder()) // Delete contents before directory itself
                .forEach(path -> {
                    try {
                        Files.delete(path);
                        // System.out.println("Deleted: " + path); // Uncomment for verbose deletion log
                    } catch (IOException e) {
                         // Log error but continue deleting other files if possible
                        System.err.println("Failed to delete " + path + ": " + e.getMessage());
                        // Depending on strictness, you might wrap and re-throw here:
                        // throw new UncheckedIOException(e);
                    }
                });
        } catch (IOException e) {
            System.err.println("Error walking directory for deletion: " + directoryToBeDeleted);
            // Re-throw the initial walking exception or wrap in UncheckedIOException
             throw new IOException("Failed to delete directory fully: " + directoryToBeDeleted, e);
        }
        // Optional: Add a check here to see if the directory *itself* was deleted,
        // as the walk + delete might fail on the root directory if it wasn't empty
        // due to permissions or other issues during the stream processing.
         if (Files.exists(directoryToBeDeleted)) {
             System.err.println("WARNING: Directory was not fully deleted: " + directoryToBeDeleted);
         } else {
              System.out.println("Successfully cleared directory: " + directoryToBeDeleted);
         }
    }
}