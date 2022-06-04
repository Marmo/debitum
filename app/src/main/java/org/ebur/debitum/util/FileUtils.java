package org.ebur.debitum.util;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public abstract class FileUtils {
    public static final String TAG = "FileUtils";


    public static void copyFile(File source, File dest) throws IOException {
        // try-with-resources
        try (FileChannel sourceChannel = new FileInputStream(source).getChannel();
             FileChannel destChannel = new FileOutputStream(dest).getChannel()) {
            destChannel.transferFrom(sourceChannel, 0, sourceChannel.size());
        }
    }

    public static void copyFile(Uri source, File dest, ContentResolver resolver) throws IOException {
        // try-with-resources
        try (ParcelFileDescriptor pfdSource = resolver.openFileDescriptor(source, "r");
             FileChannel sourceChannel = new FileInputStream(pfdSource.getFileDescriptor()).getChannel();
             FileChannel destChannel = new FileOutputStream(dest).getChannel()) {
            destChannel.transferFrom(sourceChannel, 0, sourceChannel.size());
        }
    }

    /**
     * Deletes a directory, recursively deleting its contents if it is not empty
     * @param dir directory to be deleted
     * @return true if the directory was deleted successfully (i.e. all of its contents was
     * deleted, too), false otherwise
     */
    public static boolean deleteDir(@Nullable File dir) {
        if (dir == null || !dir.exists()) return false;
        File[] children = dir.listFiles();
        if (children == null) return false;

        // delete all children
        for (File child:children) {
            if (child.isDirectory()) {
                deleteDir(child);
            } else if (child.isFile()) {
                child.delete();
            }
        }
        // finally delete the now-empty dir
        // Note: there is no need to evaluate the success of the internal delete calls, as
        // dir.delete() will return false if one or more files/directories failed to be deleted
        return dir.delete();
    }

    /**
     *
     * @param dir directory in which to look for existing images
     * @return for all files in dir whose name represents a 8 digit hex value, the maximum is
     * determined and max + 1 as 8 digit hex value returned
     */
    @NonNull
    public static String getNextImageFilename(@NonNull File dir) {
        if (dir.exists()) {
            // create sorted list of all files whose names represent 8-digit hex numbers
            // and find its maximum
            String[] dirlist = dir.list();
            if (dirlist == null || dirlist.length == 0) {
                return "00000001";
            } else {
                String maxFilename =  new ArrayList<String>(Arrays.asList(dirlist))
                        .stream()
                        .filter(s -> s.matches("^[0123456789abcdef]{8}\\.[^.]+$")) // only consider 8-digit-hex representations
                        .map(s -> s.replaceAll("\\.[^.]+$", "")) // remove extension
                        .max(String::compareTo)
                        .orElse("00000000");
                Long num = Long.parseLong(maxFilename, 16) + 1;
                return String.format(Locale.getDefault(), "%08x", num);
            }
        } else {
            dir.mkdirs();
            return "00000000";
        }
    }

    @NonNull
    public static String getFileExtension(@NonNull Uri uri, @NonNull ContentResolver resolver) {
        Cursor cursor = resolver.query(uri, null, null, null, null);
        int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
        cursor.moveToFirst();
        String filename = cursor.getString(nameIndex);
        cursor.close();
        return filename.replaceAll(".*\\.", "");
    }

    /*public static void zip(List<File> source, File dest) throws IOException {
        // check preconditions
        if (dest.exists())
            throw new IOException("Target file already exists.");
        if (dest.getParentFile() != null && !dest.getParentFile().canWrite())
            throw new IOException("Target file's parent directory cannot be written into.");

        zipInternal(source, dest);
    }*/

    public static void zip(List<File> source, Uri destUri, @NonNull Context context) throws IOException {
        // https://www.baeldung.com/java-compress-and-uncompress
        ParcelFileDescriptor pfd = context.getContentResolver().openFileDescriptor(destUri, "w");
        FileOutputStream fos = new FileOutputStream(pfd.getFileDescriptor());

        ZipOutputStream zipOut = new ZipOutputStream(fos);
        for (File fileToZip : source) {
            FileInputStream fis = new FileInputStream(fileToZip);
            ZipEntry zipEntry = new ZipEntry(fileToZip.getName());
            zipOut.putNextEntry(zipEntry);

            byte[] bytes = new byte[1024];
            int length;
            while((length = fis.read(bytes)) >= 0) {
                zipOut.write(bytes, 0, length);
            }
            fis.close();
        }
        zipOut.close();
        fos.close();
        pfd.close();
    }

    // https://www.baeldung.com/java-compress-and-uncompress
    public static void unzip(File source, File dest) throws IOException {
        // check preconditions
        if (!source.isFile())
            throw new IOException("The File instance to unzip does not denote a file.");
        if (!dest.isDirectory())
            throw new IOException("The File instance to unzip into does not denote a directory.");
        if (!source.canRead())
            throw new IOException("The file to unzip cannot be read.");
        if (!dest.canWrite())
            throw new IOException("The directory to unzip into cannot be written into.");

        ZipInputStream zis = new ZipInputStream(new FileInputStream(source));
        unzip(zis, dest);
    }

    public static void unzip(Uri source, File dest, Context context) throws IOException {
        // check preconditions
        if (!dest.isDirectory())
            throw new IOException("The File instance to unzip into does not denote a directory.");
        if (!dest.canWrite())
            throw new IOException("The directory to unzip into cannot be written into.");

        ZipInputStream zis = new ZipInputStream(context.getContentResolver().openInputStream(source));
        unzip(zis, dest);
    }

    public static void unzip(ZipInputStream zis, File dest) throws IOException {
        byte[] buffer = new byte[1024];
        ZipEntry zipEntry = zis.getNextEntry();
        while (zipEntry != null) {
            File newFile = newFile(dest, zipEntry);
            if (zipEntry.isDirectory()) {
                if (!newFile.isDirectory() && !newFile.mkdirs()) {
                    throw new IOException("Failed to create directory " + newFile);
                }
            } else {
                // fix for Windows-created archives
                File parent = newFile.getParentFile();
                if (parent == null || !parent.isDirectory() && !parent.mkdirs()) {
                    throw new IOException("Failed to create directory " + parent);
                }

                // write file content
                FileOutputStream fos = new FileOutputStream(newFile);
                int length;
                while ((length = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, length);
                }
                fos.close();
            }
            zipEntry = zis.getNextEntry();
        }
        zis.closeEntry();
        zis.close();
    }

    /**
     * Creates a new File object and guards against writing files to the file system outside of the
     * target folder. This vulnerability is called Zip Slip and you can read more about it here:
     * https://snyk.io/research/zip-slip-vulnerability.
     * @param destinationDir directory where the new file should be created
     * @param zipEntry zipEntry that contains the file to be created
     * @return the file object if everything is fine (file to be created is inside the target directory)
     * @throws IOException
     */
    private static File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
        File destFile = new File(destinationDir, zipEntry.getName());

        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();

        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }

        return destFile;
    }

    // copies backup file from original location to one decided via SAF picker
    /*public static void copyZip(File originalFile, Uri destUri, Context context) throws IOException {
        ParcelFileDescriptor pfd = context.getContentResolver().openFileDescriptor(destUri, "w");
        FileOutputStream destFos = new FileOutputStream(pfd.getFileDescriptor());
        FileInputStream originalFis = new FileInputStream(originalFile);
        FileChannel originalChannel = originalFis.getChannel();
        FileChannel destChannel = destFos.getChannel();

        originalChannel.transferTo(0, originalChannel.size(), destChannel);

        destChannel.close();
        originalChannel.close();
        destFos.close();
        originalFis.close();
        pfd.close();
    }*/
}
