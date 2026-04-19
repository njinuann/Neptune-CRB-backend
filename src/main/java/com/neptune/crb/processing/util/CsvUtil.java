package com.neptune.crb.processing.util;

import com.neptune.crb.processing.model.CsvFileResult;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static com.neptune.crb.processing.util.Logger.logEvent;

/**
 * @author
 * @version 2.0
 * @since 11/3/2025
 */
public class CsvUtil
{

    private static final String CRB_PREFIX = "CRB";
    private static final String INSTITUTION_CODE = "MM";
    private static final String INSTITUTION_CODE_DP = "MD";
    private static final String FILE_VERSION = "001";
    private static final String FILE_EXTENSION = ".N179";
    private static final String SAVE_FOLDER = "/app/crb/reports";

    /**
     * Generate both:
     * - Temp File  (for upload)
     * - Optional Local Copy (for storage / inspection)
     */
    public static CsvFileResult toCsvFile(
            String reportType,
            List<Map<String, Object>> data,
            boolean saveLocalCopy
    ) throws IOException
    {

        validateData(data);

        File tempFile = createTempCsv(reportType);
        File realFile = null;
        BufferedWriter realWriter = null;

        int recordCount = 0;

        try (BufferedWriter tempWriter = new BufferedWriter(new FileWriter(tempFile)))
        {

            // Optional permanent local copy
            if (saveLocalCopy)
            {
                realFile = createLocalPermanentFile(reportType);
                realWriter = new BufferedWriter(new FileWriter(realFile));
            }

            // Determine consistent column order
            Set<String> headers = extractHeaders(data);

            // Write only data rows (NO HEADERS)
            for (Map<String, Object> row : data)
            {

                String line = formatRow(headers, row);

                tempWriter.write(line);
                tempWriter.newLine();

                if (realWriter != null)
                {
                    realWriter.write(line);
                    realWriter.newLine();
                }

                recordCount++;
            }

        } finally
        {
            if (realWriter != null)
            {
                try
                {
                    realWriter.close();
                } catch (IOException ignored)
                {
                }
            }
        }

        return new CsvFileResult(tempFile, recordCount);
    }



    /* --------------------- Utility Helpers ------------------------- */

    private static void validateData(List<Map<String, Object>> data)
    {
        if (data == null || data.isEmpty())
        {
            throw new IllegalArgumentException("Cannot create CSV from empty/null data");
        }
    }

    private static Set<String> extractHeaders(List<Map<String, Object>> data)
    {
        // Preserves insert order if Map is LinkedHashMap
        return data.get(0).keySet();
    }

    private static void writeLine(BufferedWriter writer, Set<String> headers) throws IOException
    {
        writer.write(String.join("|", headers));
        writer.newLine();
    }

    private static String formatRow(Set<String> headers, Map<String, Object> row)
    {
        List<String> values = headers.stream()
                .map(h -> Optional.ofNullable(row.get(h))
                        .map(Object::toString)
                        .orElse(""))
                .collect(Collectors.toList());
        return String.join("|", values);
    }


    /* ---------------- File Creation ---------------- */

//    private static File createTempCsv(String reportType) throws IOException
//    {
//
//        String prefix = generateFileNameWithoutExtension(reportType) + "_";
//        File tempFile = File.createTempFile(prefix, FILE_EXTENSION);
//
//        logEvent("Temp CSV File Created: " + tempFile.getAbsolutePath());
//        return tempFile;
//    }


// ...

    private static File createTempCsv(String reportType) throws IOException
    {
        // Choose system temp directory
        String tmpDir = System.getProperty("java.io.tmpdir");
        String fileName = generateFileName(reportType); // e.g. CRBMMCE20251104001.N179

        Path tempPath = Paths.get(tmpDir).resolve(fileName);

        // Option A: Delete existing and create new (keeps filename deterministic)
        if (Files.exists(tempPath))
        {
            try
            {
                Files.delete(tempPath);
            } catch (IOException e)
            {
                throw new IOException("Failed to delete existing temp file: " + tempPath, e);
            }
        }

        // Atomically create the file
        Path created = Files.createFile(tempPath);

        logEvent("Temp CSV File Created: " + created.toAbsolutePath());
        return created.toFile();

    /*
    // Option B (safer for concurrency): If you don't want to delete an existing file,
    // try to create atomically and on failure append a small counter suffix (e.g. _1, _2).
    // This avoids Java's long random suffix while still providing uniqueness.
    //
    // int counter = 0;
    // Path candidate;
    // do {
    //     String candidateName = (counter == 0) ? fileName :
    //         fileName.replace(FILE_EXTENSION, "_" + counter + FILE_EXTENSION);
    //     candidate = Paths.get(tmpDir).resolve(candidateName);
    //     try {
    //         Path created = Files.createFile(candidate); // atomic create -> throws if exists
    //         logEvent("Temp CSV File Created: " + created.toAbsolutePath());
    //         return created.toFile();
    //     } catch (FileAlreadyExistsException faee) {
    //         counter++;
    //         // loop and try next candidate
    //     }
    // } while (counter < 1000);
    //
    // throw new IOException("Failed to create unique temp file after attempts");
    */
    }


    private static File createLocalPermanentFile(String reportType) throws IOException
    {

        File dir = new File(SAVE_FOLDER);

        if (!dir.exists())
        {
            boolean created = dir.mkdirs();
            if (!created)
            {
                throw new IOException("Failed to create directory: " + SAVE_FOLDER);
            }
        }

        String fileName = generateFileName(reportType);
        File file = new File(dir, fileName);

        if (file.exists() && !file.delete())
        {
            throw new IOException("Failed to delete existing file: " + file.getAbsolutePath());
        }

        if (!file.createNewFile())
        {
            throw new IOException("Failed to create file: " + file.getAbsolutePath());
        }

        logEvent("Local Copy CSV File Created: " + file.getAbsolutePath());
        return file;
    }

    /* ---------------- Filename Helpers ---------------- */

    public static String generateFileName(String reportType)
    {
        return generateFileNameWithoutExtension(reportType) + FILE_EXTENSION;
    }

    public static String generateFileNameWithoutExtension(String reportType)
    {
        String date = new SimpleDateFormat("yyyyMMdd").format(new Date());
        return CRB_PREFIX + (Objects.equals(reportType, "DP") ? INSTITUTION_CODE_DP : INSTITUTION_CODE) + reportType + date + FILE_VERSION;
    }
}
