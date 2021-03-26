package com.getcapacitor.community.media;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

public class IOUtils {
    private static final String LOGTAG = "MediaPlugin";
    private static Random r = new Random();

    public static File downloadFile(URL inputUrl, File albumDir) {
        String targetFileName = generateTargetFileName(inputUrl.toString());
        File targetFile = new File(albumDir, targetFileName);

        // Read and write image files
        ReadableByteChannel inChannel = null;
        try (InputStream inputStream = inputUrl.openStream()) {
            inChannel = Channels.newChannel(inputUrl.openStream());
            return copyChannel(inChannel, targetFile);
        } catch (Exception e) {
            throw new RuntimeException("Error trying download file: " + String.valueOf(inputUrl) + ", error: " + e.getMessage(), e);
        }
    }

    public static File copyFile(File inputFile, File albumDir) {
        String targetFileName = generateTargetFileName(inputFile.getAbsolutePath());
        File targetFile = new File(albumDir, targetFileName);

        try (FileInputStream is = new FileInputStream(inputFile)) {
            FileChannel inChannel = is.getChannel();
            return copyChannel(inChannel, targetFile);
        } catch (Exception e) {
            throw new RuntimeException("Error trying copy file: " + String.valueOf(inputFile) + ", error: " + e.getMessage(), e);
        }
    }

    private static File copyChannel(ReadableByteChannel inChannel, File targetFile) throws Exception {
        int BUFFER_SIZE = 1 * 1024 * 1024;

        try (FileOutputStream os = new FileOutputStream(targetFile)) {
            FileChannel outChannel = os.getChannel();
            ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
            while (inChannel.read(buffer) != -1) {
                buffer.flip();
                outChannel.write(buffer);
                buffer.clear();
            }

            return targetFile;
        } catch (Exception e) {
            throw new Exception("Error transferring file: " + e.getMessage(), e);
        }
    }


    private static String generateTargetFileName(String inputFileName) {
        String extension = "";

        // retrieves basename
        int index = inputFileName.lastIndexOf("/");
        if (index != -1) {
            String basename = inputFileName.substring(index);

            index = basename.lastIndexOf(".");
            if (index != -1) {
                extension = basename.substring(index);
            }
        }

        // generate image file name using current date and time + random
        String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        Integer nextInt = r.nextInt(1000) + 1000;

        String targetFileName = "media_" + timeStamp + "_" + nextInt + extension;

        return targetFileName;
    }
}
