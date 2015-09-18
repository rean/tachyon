/*
 * Licensed to the University of California, Berkeley under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to You under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package tachyon.client.file;

import java.io.IOException;

import com.google.common.base.Preconditions;

import tachyon.TachyonURI;
import tachyon.annotation.PublicApi;
import tachyon.client.ClientOptions;
import tachyon.client.FileSystemMasterClient;
import tachyon.thrift.BlockInfoException;
import tachyon.thrift.DependencyDoesNotExistException;
import tachyon.thrift.FileAlreadyExistException;
import tachyon.thrift.FileDoesNotExistException;
import tachyon.thrift.FileInfo;
import tachyon.thrift.InvalidPathException;

/**
 * A TachyonFileSystem implementation including convenience methods as well as a streaming API to
 * read and write files. This class does not access the master client directly but goes through the
 * implementations provided in {@link AbstractTachyonFileSystem}. The create api for creating files
 * is not supported by this TachyonFileSystem because the files should only be written once, thus
 * getOutStream is sufficient for creating and writing to a file.
 */
@PublicApi
public class TachyonFileSystem extends AbstractTachyonFileSystem {

  private static TachyonFileSystem sTachyonFileSystem;

  public static synchronized TachyonFileSystem get() {
    if (sTachyonFileSystem == null) {
      sTachyonFileSystem = new TachyonFileSystem();
    }
    return sTachyonFileSystem;
  }

  private TachyonFileSystem() {
    super();
  }

  /**
   * Create is not supported in StreamingTachyonFileSystem. Files should only be written once.
   * Use getOutStream instead to create files.
   */
  @Override
  public long create(TachyonURI path, long blockSize, boolean recursive) {
    throw new UnsupportedOperationException("Create is not supported, use getOutStream instead.");
  }

  /**
   * Convenience function for delete recursively. This is the same as calling delete(file, true).
   *
   * @param file the TachyonFile to delete recursively
   * @throws FileDoesNotExistException if the file does not exist in Tachyon space
   * @throws IOException if the master cannot delete the file
   */
  public void delete(TachyonFile file) throws FileDoesNotExistException, IOException {
    delete(file, true);
  }

  /**
   * Convenience function for free recursively. This is the same as calling free(file, true).
   *
   * @param file the TachyonFile to free recursively
   * @throws FileDoesNotExistException if the file does not exist in Tachyon space
   * @throws IOException if the master cannot delete the file
   */
  public void free(TachyonFile file) throws FileDoesNotExistException,
      IOException {
    free(file, true);
  }

  /**
   * Gets a {@link FileInStream} for the specified file. The stream's settings can be customized by
   * setting the options parameter. The caller should close the stream after finishing the
   * operations on it.
   *
   * @param file the handler for the file.
   * @param options the set of options specific to this operation.
   * @return an input stream to read the file
   * @throws FileDoesNotExistException if the file does not exist
   * @throws IOException if the stream cannot be opened for some other reason
   */
  public FileInStream getInStream(TachyonFile file, ClientOptions options) throws IOException,
      FileDoesNotExistException {
    FileInfo info = getInfo(file);
    Preconditions.checkState(!info.isIsFolder(), "Cannot read from a folder");
    return new FileInStream(info, options);
  }

  /**
   * Creates a file and gets the {@link FileOutStream} for the specified file. If the parent
   * directories do not exist, they will be created. This should only be called to write a file that
   * does not exist. Once close is called on the output stream, the file will be completed. Append
   * or update of a completed file is currently not supported.
   *
   * @param path the Tachyon path of the file
   * @param options the set of options specific to this operation
   * @return an output stream to write the file
   * @throws InvalidPathException if the provided path is invalid
   * @throws FileAlreadyExistException if the file being written to already exists
   * @throws BlockInfoException if the provided block size is invalid
   * @throws IOException if the file already exists or if the stream cannot be opened
   */
  public FileOutStream getOutStream(TachyonURI path, ClientOptions options) throws IOException,
      InvalidPathException, FileAlreadyExistException, BlockInfoException {
    long fileId = super.create(path, options.getBlockSize(), true);
    return new FileOutStream(fileId, options);
  }

  /**
   * Alternative way to get a FileOutStream to a file that has already been created. This should
   * not be used. Deprecated in version v0.8 and will be removed in v0.9.
   *
   * @see #getOutStream(TachyonURI path, ClientOptions options)
   */
  // TODO(calvin): We should remove this when the TachyonFS code is fully deprecated.
  @Deprecated
  public FileOutStream getOutStream(long fileId, ClientOptions options) throws IOException {
    return new FileOutStream(fileId, options);
  }

  /**
   * Convenience method to mkdirs recursively.
   *
   * @param path the Tachyon path of the folder to create recursively
   * @return true if the directory is created
   * @throws FileAlreadyExistException if the path already exists or a parent is a file
   * @throws InvalidPathException if the path is invalid
   * @throws IOException if the master cannot create the folder
   */
  public boolean mkdirs(TachyonURI path) throws FileAlreadyExistException, InvalidPathException,
      IOException {
    return mkdirs(path, true);
  }

  // TODO: Move this to lineage client
  public void reportLostFile(TachyonFile file) throws IOException, FileDoesNotExistException {
    FileSystemMasterClient masterClient = mContext.acquireMasterClient();
    try {
      masterClient.reportLostFile(file.getFileId());
    } finally {
      mContext.releaseMasterClient(masterClient);
    }
  }

  // TODO: Move this to lineage client
  public void requestFilesInDependency(int depId) throws IOException,
      DependencyDoesNotExistException {
    FileSystemMasterClient masterClient = mContext.acquireMasterClient();
    try {
      masterClient.requestFilesInDependency(depId);
    } finally {
      mContext.releaseMasterClient(masterClient);
    }
  }
}