package com.neptune.crb.processing.model;

import java.io.File;

/**
 * @author NJINU <alexnyiha@gmail.com>
 * @version 1.0
 * @since 11/4/2025
 */
public class CsvFileResult {
 private final File file;
 private final int recordCount;

 public CsvFileResult(File file, int recordCount) {
  this.file = file;
  this.recordCount = recordCount;
 }

 public File getFile() {
  return file;
 }

 public int getRecordCount() {
  return recordCount;
 }
}

