/*  __    __  __  __    __  ___
 * \  \  /  /    \  \  /  /  __/
 *  \  \/  /  /\  \  \/  /  /
 *   \____/__/  \__\____/__/
 *
 * Copyright 2014-2019 Vavr, http://vavr.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.vavr;

import io.vavr.collection.*;
import org.openjdk.jol.info.GraphLayout;

import java.text.DecimalFormat;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class MemoryUsage {
    private static final DecimalFormat FORMAT = new DecimalFormat("#,##0");
    private static Map<Integer, LinkedHashSet<Seq<CharSeq>>> memoryUsages = TreeMap.empty(); // if forked, this will be reset every time

    static void storeMemoryUsages(int elementCount, Object target) {
        memoryUsages = memoryUsages.put(elementCount, memoryUsages.get(elementCount).getOrElse(LinkedHashSet.empty()).add(Array.of(
                toHumanReadableName(target),
                toHumanReadableByteSize(target)
        ).map(CharSeq::of)));
    }
    private static String toHumanReadableByteSize(Object target) { return FORMAT.format(byteSize(target)); }
    private static long byteSize(Object target) { return GraphLayout.parseInstance(target).totalSize(); }

    private static HashMap<Predicate<String>, String> names = HashMap.ofEntries(
            Tuple.of("^java\\.", "Java mutable @ "),
            Tuple.of("^fj\\.", "Functional Java persistent @ "),
            Tuple.of("^org\\.pcollections", "PCollections persistent @ "),
            Tuple.of("^org\\.eclipse\\.collections", "Eclipse Collections persistent @ "),
            Tuple.of("^clojure\\.", "Clojure persistent @ "),
            Tuple.of("^scalaz\\.Heap", "Scalaz persistent @ "),
            Tuple.of("^scala\\.collection.immutable", "Scala persistent @ "),
            Tuple.of("^scala\\.collection.mutable", "Scala mutable @ "),
            Tuple.of("^io\\.usethesource", "Capsule persistent @ "),
            Tuple.of("^io\\.vavr\\.", "Vavr persistent @ ")
    ).mapKeys(r -> Pattern.compile(r).asPredicate());
    private static String toHumanReadableName(Object target) {
        final Class<?> type = target.getClass();
        return prefix(type) + type.getSimpleName();
    }
    private static String prefix(Class<?> type) { return names.find(p -> p._1.test(type.getName())).get()._2; }
}
