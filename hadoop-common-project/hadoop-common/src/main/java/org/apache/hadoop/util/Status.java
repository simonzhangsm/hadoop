/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.hadoop.util;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.classification.InterfaceAudience;
import org.apache.hadoop.classification.InterfaceStability;

/** Operation */
@InterfaceAudience.Private
@InterfaceStability.Evolving
public enum Status {
  RUNNING((byte)0),
  STARTING((byte)1),
  STOPPING((byte)2),
  SHUTDOWN((byte)3),
  FAILED((byte)4),
  SUCCEEDED((byte)5),
  NORMAL((byte)6),
  KILLED((byte)7),
  PEDDING((byte)8),
  UNDEF((byte)9);

  /** The code for this status. */
  public final byte status;
  
  private Status(byte status) {
    this.status = status;
  }
  
  public byte getStatus() {
      return status; 
    }
  
  private static final int FIRST_CODE = values()[0].status;
  /** Return the object represented by the status. */
  private static Status valueOf(byte status) {
    final int i = (status & 0xff) - FIRST_CODE;
    return i < 0 || i >= values().length? null: values()[i];
  }

  /** Read from in */
  public Status read(DataInput in) throws IOException {
    return valueOf(in.readByte());
  }

  /** Write to out */
  public void write(DataOutput out) throws IOException {
    out.write(status);
  }
}
