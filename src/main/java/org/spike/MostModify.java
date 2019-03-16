package org.spike;

import java.util.*;
import java.util.Map.Entry;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class MostModify {
    public static final Predicate<String> NO_FILTER = filename -> true;
    public static final int NO_LIMIT = -1;

    private Predicate<String> filenameFilter = NO_FILTER;
    private int atLeast = NO_LIMIT;

    public List<Entry<String, Integer>> exec(List<Commit> commits) {
        HashMap<String, Integer> modificationNumber = new HashMap<>();
        for (Commit commit : commits) {
            Set<String> files = commit.getFiles().keySet();

            files.stream()
                    .filter(filenameFilter)
                    .forEach(file -> modificationNumber.put(file, modificationNumber.getOrDefault(file, 0) + 1));
        }

        List<Entry<String, Integer>> list = new ArrayList<>(modificationNumber.entrySet());

        return list.stream()
                .filter(hasEnoughCommit())
                .sorted(Entry.<String, Integer>comparingByValue().reversed())
                .collect(Collectors.toList());

    }

    private Predicate<Entry<String, Integer>> hasEnoughCommit() {
        return e -> (atLeast == NO_LIMIT || e.getValue() >= atLeast);
    }

    public MostModify filter(Predicate<String> filenameFilter) {
        this.filenameFilter = filenameFilter;
        return this;
    }

    public MostModify atLeast(int atLeast) {
        this.atLeast = atLeast;
        return this;
    }

    public Map<String, List<Integer>> group(List<List<Entry<String, Integer>>> lists) {
        List<Map<String, Integer>> collect = lists.stream()
                .map(MostModify::listOfEntryToMap)
                .collect(Collectors.toList());

        return groupListOfMap(collect);

    }

    public Map<String, List<Integer>> groupListOfMap(List<Map<String, Integer>> collect) {
        Set<String> keys = extractKeys(collect);

        return listOfEntryToMap(keys.stream()
                .map(key -> new AbstractMap.SimpleEntry<>(key, groupByKey(key, collect)))
                .collect(Collectors.toList()));
    }

    private Set<String> extractKeys(List<Map<String, Integer>> collect) {
        return collect.stream()
                .map(Map::keySet)
                .flatMap(Set::stream)
                .collect(Collectors.toSet());
    }

    private static <K, V> Map<K, V> listOfEntryToMap(List<Entry<K, V>> list) {
        return list.stream()
                    .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
    }

    private List<Integer> groupByKey(String key, List<Map<String, Integer>> collect) {
        return collect.stream()
                .map(list -> list.getOrDefault(key, 0))
                .collect(Collectors.toList());

    }
}
