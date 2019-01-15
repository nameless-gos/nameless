/*
 * Disq
 *
 * MIT License
 *
 * Copyright (c) 2018-2019 Disq contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.disq_bio.disq.impl.formats.cram;

import htsjdk.samtools.CRAMContainerStreamWriter;
import htsjdk.samtools.SAMFileHeader;
import htsjdk.samtools.SAMRecord;
import htsjdk.samtools.cram.ref.CRAMReferenceSource;
import java.io.IOException;
import java.io.OutputStream;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.RecordWriter;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.disq_bio.disq.HtsjdkReadsRdd;

/**
 * An output format for writing {@link SAMRecord} objects to CRAM files. Should not be used
 * directly.
 *
 * @see HtsjdkReadsRdd
 */
public class CramOutputFormat extends FileOutputFormat<Void, SAMRecord> {

  private static SAMFileHeader header;
  private static CRAMReferenceSource refSource;

  public static void setHeader(SAMFileHeader samFileHeader) {
    header = samFileHeader;
  }

  public static void setReferenceSource(CRAMReferenceSource referenceSource) {
    refSource = referenceSource;
  }

  @Override
  public RecordWriter<Void, SAMRecord> getRecordWriter(TaskAttemptContext taskAttemptContext)
      throws IOException {
    Path file = getDefaultWorkFile(taskAttemptContext, "");
    return new CramRecordWriter(taskAttemptContext.getConfiguration(), file, header, refSource);
  }

  static class CramRecordWriter extends RecordWriter<Void, SAMRecord> {

    private final OutputStream out;
    private final CRAMContainerStreamWriter cramWriter;

    public CramRecordWriter(
        Configuration conf, Path file, SAMFileHeader header, CRAMReferenceSource refSource)
        throws IOException {
      this.out = file.getFileSystem(conf).create(file);
      cramWriter = new CRAMContainerStreamWriter(out, null, refSource, header, file.toString());
    }

    @Override
    public void write(Void ignore, SAMRecord samRecord) {
      cramWriter.writeAlignment(samRecord);
    }

    @Override
    public void close(TaskAttemptContext taskAttemptContext) throws IOException {
      cramWriter.finish(false); // don't write terminator
      out.close();
    }
  }
}
