package com.yields.yieldsbpmn;

import com.yields.yieldsbpmn.models.ModelsFetcher;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
class TestRunner implements CommandLineRunner {

    private final ModelsFetcher modelsFetcher;

    @Override
    public void run(String... args) throws Exception {
        modelsFetcher.fetchModels().forEach(System.out::println);
    }
}
