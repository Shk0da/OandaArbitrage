package com.oanda.arbitrage.repository;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Repository
public class GraphRepository {

    public static final int RING_SIZE = 5;
    public static final int TRIANGULAR_SIZE = 3;

    @Getter
    private final List<Set<String>> ringList = Lists.newArrayList();

    @Getter
    private final List<Set<String>> triangularList = Lists.newArrayList();

    public GraphRepository(InstrumentRepository instrumentRepository, String balanceCurrency) {
        Set<String> symbols = instrumentRepository.getSymbols()
                .stream()
                .filter(symbol -> symbol.contains("/"))
                .collect(Collectors.toSet());

        // triangular
        symbols.stream()
                .filter(symbol -> symbol.contains("/" + balanceCurrency))
                .forEach(symbol -> {
                    String[] currencies = symbol.split("/");
                    Set<String> vertexsMiddle = symbols.stream()
                            .filter(s -> s.contains(currencies[0] + "/") && !s.contains("/" + currencies[1]))
                            .collect(Collectors.toSet());
                    vertexsMiddle.forEach(vertexMiddle -> {
                        String[] currenciesVertexMiddle = vertexMiddle.split("/");
                        symbols.stream()
                                .filter(s -> s.contains(currenciesVertexMiddle[1] + "/" + currencies[1]))
                                .collect(Collectors.toSet())
                                .forEach(vertexEnd -> {
                                    Set<String> triangular = Sets.newLinkedHashSet();
                                    triangular.add(symbol);
                                    triangular.add(vertexMiddle);
                                    triangular.add(vertexEnd);
                                    triangularList.add(triangular);
                                });
                    });
                });

        // ring todo
        log.debug("");
    }
}
