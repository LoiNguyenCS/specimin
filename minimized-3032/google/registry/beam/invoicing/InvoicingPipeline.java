package google.registry.beam.invoicing;

import google.registry.beam.invoicing.BillingEvent.InvoiceGroupingKey;
import google.registry.beam.invoicing.BillingEvent.InvoiceGroupingKey.InvoiceGroupingKeyCoder;
import org.apache.beam.sdk.transforms.Count;
import org.apache.beam.sdk.transforms.Filter;
import org.apache.beam.sdk.transforms.MapElements;
import org.apache.beam.sdk.transforms.PTransform;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.TypeDescriptor;
import org.apache.beam.sdk.values.TypeDescriptors;

public class InvoicingPipeline {

    private static class GenerateInvoiceRows extends PTransform<PCollection<BillingEvent>, PCollection<String>> {

        public PCollection<String> expand(PCollection<BillingEvent> input) {BillingEvent
            return input.apply("Map to invoicing key", MapElements.into(TypeDescriptor.of(InvoiceGroupingKey.class)).via(::getInvoiceGroupingKey)).apply(Filter.by((InvoiceGroupingKey key) -> key.unitPrice() != 0)).setCoder(new InvoiceGroupingKeyCoder()).apply("Count occurrences", Count.perElement()).apply("Format as CSVs", MapElements.into(TypeDescriptors.strings()).via((KV<InvoiceGroupingKey, Long> kv) -> kv.getKey().toCsv(kv.getValue())));
        }
    }
}
