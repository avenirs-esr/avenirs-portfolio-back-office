package fr.avenirsesr.portfolio.backoffice.cgu.infrastructure.adapter.repository;

import fr.avenirsesr.portfolio.backoffice.cgu.domain.model.Cgu;
import fr.avenirsesr.portfolio.backoffice.cgu.domain.port.output.repository.CguRepository;
import fr.avenirsesr.portfolio.backoffice.cgu.infrastructure.adapter.mapper.CguMapper;
import fr.avenirsesr.portfolio.backoffice.cgu.infrastructure.adapter.model.CguEntity;
import fr.avenirsesr.portfolio.common.data.infrastructure.adapter.repository.GenericJpaRepositoryAdapter;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class CguDatabaseRepository extends GenericJpaRepositoryAdapter<Cgu, CguEntity>
    implements CguRepository {

  private final CguJpaRepository cguJpaRepository;

  public CguDatabaseRepository(CguJpaRepository cguJpaRepository) {
    super(cguJpaRepository, cguJpaRepository, CguEntity.class, CguMapper.INSTANCE);
    this.cguJpaRepository = cguJpaRepository;
  }

  @Override
  public Optional<Cgu> findLatest() {
    return cguJpaRepository.findFirstByOrderByVersionDesc().map(CguMapper.INSTANCE::toDomain);
  }
}
