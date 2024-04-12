// Copyright 2018 The Nomulus Authors. All Rights Reserved.
// 
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
// 
// http://www.apache.org/licenses/LICENSE-2.0
// 
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package google.registry.beam.invoicing;

import org.apache.beam.sdk.coders.AtomicCoder;

/**
 * A POJO representing a single billable event, parsed from a {@code SchemaAndRecord}.
 *
 * <p>This is a trivially serializable class that allows Beam to transform the results of a Bigquery
 * query into a standard Java representation, giving us the type guarantees and ease of manipulation
 * Bigquery lacks, while localizing any Bigquery-side failures to the {@link #parseFromRecord}
 * function.
 */
public abstract class BillingEvent {

    /**
     * Key for each {@code BillingEvent}, when aggregating for the overall invoice.
     */
    abstract static class InvoiceGroupingKey {

        /**
         * Returns the cost per invoice item.
         */
        abstract Double unitPrice();

        /**
         * Coder that provides deterministic (de)serialization for {@code InvoiceGroupingKey}.
         */
        static class InvoiceGroupingKeyCoder extends AtomicCoder<InvoiceGroupingKey> {
        }
    }

    InvoiceGroupingKey getInvoiceGroupingKey() {
        throw new Error();
      }
}
