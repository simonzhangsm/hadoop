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

package org.apache.hadoop.metrics2.lib;

import static com.google.common.base.Preconditions.*;

import java.util.HashMap;

import org.apache.hadoop.classification.InterfaceAudience;
import org.apache.hadoop.classification.InterfaceStability;
import org.apache.hadoop.metrics2.MetricsInfo;
import org.apache.hadoop.metrics2.MetricsRecordBuilder;

/**
 * The mutable gauge metric interface
 */
@InterfaceAudience.Public
@InterfaceStability.Evolving
public class MutableHashMap extends MutableMetric {
	private final MetricsInfo info;
	private volatile HashMap<String, Number> value = new HashMap<String, Number>();

	protected MutableHashMap(MetricsInfo info) {
		this.info = checkNotNull(info, "HashMap metric info");
	}

	protected MetricsInfo info() {
		return info;
	}

	public HashMap<String, Number> value() {
		return value;
	}

	/**
	 * Record new <K,V> pair
	 */
	public synchronized void put(String key) {
		if (this.value.containsKey(key))
			this.value.put(key, this.value.get(key).longValue() + 1L);
		else {
			this.value.put(key, 1L);
		}
		setChanged();
	}

	public synchronized void put(String key, Number val) {
		if (this.value.containsKey(key))
			this.value.put(key,
					this.value.get(key).longValue() + val.longValue());
		else {
			this.value.put(key, val.longValue());
		}
		setChanged();
	}

	/**
	 * Delete the value of the HashMap metric record by key
	 */
	public synchronized void del(String key) {
		if (!(this.value.isEmpty()) && this.value.containsKey(key)) {
			this.value.remove(key);
			setChanged();
		}
	}

	/**
	 * Clear the HashMap metric record
	 */
	public synchronized void clear() {
		this.value.clear();
		clearChanged();
	}

	@Override
	public synchronized void snapshot(MetricsRecordBuilder builder, boolean all) {
		if ((all || changed()) && !value.isEmpty() ) {
			builder.addHashMap(info(), value);
			clear();
		}
	}
}
