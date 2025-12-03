package com.high.orchestration.infrastructure.repository;

import com.high.orchestration.domain.repository.SagaStateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SagaStateRepositoryAdaptor implements SagaStateRepository {

    private final JpaSagaStateRepository jpaSagaStateRepository;
}
